import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

class Connection extends Thread
{
    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    int peerPort;
    int peer_listen_port;
    int peerID;
    InetAddress peerIP;
    char FILE_VECTOR[];
    ArrayList<Connection> connectionList;    
    boolean runFlag=true;

    public Connection(Socket socket, ArrayList<Connection> connectionList) throws IOException
    {
        this.connectionList=connectionList;
        this.socket=socket;
        this.outputStream=new ObjectOutputStream(socket.getOutputStream());
        this.inputStream=new ObjectInputStream(socket.getInputStream());
        this.peerIP=socket.getInetAddress();
        this.peerPort=socket.getPort();
        
    }

    @Override
    public void run() {
        //wait for register packet.
        Packet p= new Packet();

        try {
        	p = (Packet) inputStream.readObject();
        	eventHandler(p);
        }
        catch (Exception e) {System.out.println("Could not register client");return;} 
        
        
        while (runFlag){
            try { 
                //printConnections();
                p = (Packet) inputStream.readObject();
                eventHandler(p);
               // p.printPacket();

            }
            catch (Exception e) {break;}

        }

    }

    public void printConnections()
    {
        System.out.println("---------------"); 
        for(int i = 0; i < connectionList.size(); i++) {  
            
            System.out.println("Peer ID :"+connectionList.get(i).peerID);
            System.out.println("FILE_VECTOR :"+String.valueOf(connectionList.get(i).FILE_VECTOR));
            System.out.println("---------------"); 
        }
    }

    public void send_packet_to_client(Packet p)
    {
        try
        { 
            outputStream.writeObject(p);
            System.out.println("Packet Sent ");
            //p.printPacket();
        }
        catch(Exception e){
            System.out.println ("Could not send packet! ");
        }
    }
    public void closeConnection()
    {
        try { 
                outputStream.close();
                inputStream.close();
                socket.close();
                System.out.println("Closed clientSocket");
            }
            catch (Exception e) { System.out.println("Couldn't close socket!");
            //e.printStackTrace(); 
                
        }
    }

    void send_quit_message()
    {
        Packet p = new Packet();
        p.event_type=6;
        send_packet_to_client(p);
    }

    public void eventHandler(Packet p) throws InterruptedException
    {
        int event_type = p.event_type;
        switch (event_type)
        {
            case 0: //client register
            clientRegister(p);break;
            
            case 1: // client is requesting a file 
            clientReqFile(p);break;

            case 5:
            clientWantsToQuit(p);break;
            
            case 3:
            clientGotFile(p);break; 
            
           // To Do
           case 4: clientReqFileFromPeer(p);break;
        };
    }

    public void clientRegister(Packet p)
    {
        FILE_VECTOR=p.FILE_VECTOR;
        peer_listen_port=p.peer_listen_port;
        peerID=p.sender;
        connectionList.add(this);
        System.out.println("Client connected. Total Registered Clients : "+connectionList.size() );
        printConnections();
    }

    public void clientReqFile(Packet p)
    {
       System.out.println("Client "+p.sender+" is requesting file "+p.req_file_index);
       int findex = p.req_file_index;
       Packet packet = new Packet();
        packet.event_type=2;
        packet.req_file_index=findex;

         for (int i=0;i<connectionList.size();i++)
        {
            if (connectionList.get(i).FILE_VECTOR[findex]=='1')
            {
                packet.peerID=connectionList.get(i).peerID;
                packet.peer_listen_port=connectionList.get(i).peer_listen_port;
                
                // Generate File
                byte[] file = generate_file(findex, 20000);
                            
                packet.fileHash = find_file_hash(file);
                break;   
            }
        }
        send_packet_to_client(packet);
    }
    
    
    public void clientReqFileFromPeer(Packet p) throws InterruptedException {
    	System.out.println("Client " + p.sender+ " is requesting file " +p.req_file_index);
    	int findex = p.req_file_index;
    	byte[] file = generate_file(findex, 20000);   	
    	
    	
    	int packetNum = 1;
    	int indexCount = 0;
    	
    	while (packetNum <= (file.length/p.data_block_size)) {
    		Packet fileSend = new Packet();
    		for(int i = 0; i < p.data_block_size; i++) {
    			fileSend.DATA_BLOCK[i] = file[indexCount];
    			indexCount++;
    		}
    		
    		fileSend.event_type = 4;
    		fileSend.peerID = p.recipient;
    		fileSend.sender = p.recipient;
    		fileSend.recipient = p.sender;
    		
    		
    		
    		try {
				outputStream.writeObject(fileSend);
				System.out.println("Packet " + packetNum + " sent to Peer " + fileSend.recipient);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    		Thread.sleep(1000);
    		packetNum++;
    	} // END OF WHILE LOOP
    	
		try {
			Packet ackPacket;
			ackPacket = (Packet) inputStream.readObject();
			if(ackPacket.gotFile) {
				System.out.println("Client received file");
				closeConnection();
			} else {
				System.out.println("Client did not receive file");
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

    public void clientWantsToQuit(Packet p)
    {
        //remove client from list. close thread.
        int clientPos=searchForClient(p.sender);
        System.out.println("Removing client "+p.sender);
        connectionList.remove(clientPos);
        System.out.println("Total Registered Clients : "+connectionList.size() );
        closeConnection();
    }

    public int searchForClient(int ID)
    {
        for (int i=0;i<connectionList.size();i++)
        {
            if (connectionList.get(i).peerID==ID)
                return i;
        }
        return -1;
    }
    
     public void clientGotFile(Packet p)
    {
    	 System.out.println("Client Received File");
    	 System.out.println("Updating file vector");
    	 
    	 for(Connection c: connectionList) {
    		 if(c.peerID == p.peerID) {
    			 c.FILE_VECTOR[p.req_file_index] = '1';
    		 }
    	 }
    	 
    	 printConnections();
    }
     
    /*
     * FILE HASH METHODS
     */
     
     public byte[] generate_file(int findex, int length)
     {
         byte[] buf= new byte[length];
         Random r = new Random();
         r.setSeed(findex);
         r.nextBytes(buf);
         try{
             System.out.println("File hash: " + SHAsum(buf));
         }
         catch (Exception e){System.out.println("SHA1 error!");}
         return buf;
     }

     public String find_file_hash(byte[] buf)
     {
         String h = "";
          try {
             h = SHAsum(buf);
         } catch (NoSuchAlgorithmException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return h;
     }

     public String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         return byteArray2Hex(md.digest(convertme));
     }

     private static String byteArray2Hex(byte[] hash) {
         Formatter formatter = new Formatter();
         for (byte b : hash) {
             formatter.format("%02x", b);
         }
         return formatter.toString();
     }
}
