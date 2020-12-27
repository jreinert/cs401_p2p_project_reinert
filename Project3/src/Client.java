
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;
import java.lang.*; 

public class Client {

     int serverPort;
     InetAddress ip=null; 
     Socket s;
     Socket peer;
     ServerSocket listener;
     ObjectOutputStream outputStream ;
     ObjectInputStream inputStream ;
     ObjectOutputStream p2pOS ;
     ObjectInputStream p2pIS ;
     int peerID;
     int peer_listen_port;
     char FILE_VECTOR[];
     ArrayList<Connection> connectionList;
    // To do , create each peers own ServerSocket listener to monitor for incoming peer requests. start a listener thread in main();
    // I used the ServerSocketHandler to handle both client-server and peer-to-peer listeners. You can use a separate class. 90% of the code is repeated.
    // For the individual connections, again you can re-use the Connection class, and add some event handlers to process event codes that will be used to distibguis betwwen peer-to-peer or cleint-server communications, or create a separate class called peerConnection. It is completely your choice.
    public static void main(String args[])
    {
        
        Client client = new Client();
        boolean runClient=true;
        Scanner input = new Scanner(System.in);
        if (args.length==0 || args.length % 2 == 1){
            System.out.println("Parameters Required/Incorrect Format. See usage list");
            System.exit(0);
        }

        client.cmdLineParser(args);

        try
        { 
            if (client.ip==null)
                client.ip = InetAddress.getByName("localhost"); 

            client.s = new Socket(client.ip, client.serverPort); 
            client.outputStream = new ObjectOutputStream(client.s.getOutputStream());
            client.inputStream = new ObjectInputStream(client.s.getInputStream());
            System.out.println("Connected to Server ..." +client.s); 

            Packet p = new Packet();
            p.event_type=0;
            p.sender=client.peerID;
            p.peer_listen_port=client.peer_listen_port;
            p.FILE_VECTOR = client.FILE_VECTOR;

            client.send_packet_to_server(p);

            System.out.println("Packet Sent");
            
            Thread r = new PacketHandler(client);
            r.start();
            
            // Client Server Socket for P2P Connections
            Server clientServ = new Server();
            clientServ.serverPort = client.peer_listen_port;
            clientServ.listener = new ServerSocket(client.peer_listen_port);
            
            ServerSocketHandler ssHandler = new ServerSocketHandler(clientServ, clientServ.connectionList);
            ssHandler.start();
            
            System.out.println("Peer " + client.peerID + " is waiting for other peers to connect...");
            
            while (runClient){
                
                System.out.println ("Enter query");
                char cmd=input.next().charAt(0);
                switch(cmd)
                {
                    case 'q':
                    System.out.println("Getting ready to quit ..." +client.s); 
                    clientServ.listener.close();
                    client.send_quit_to_server();
                    runClient=false;
                    break;

                    case 'f':
                    System.out.println("Enter the file index you want ");
                    int findex = input.nextInt();
                    client.send_req_for_file(findex);
                    break;

                    default:
                    System.out.println("Command not recognized. Try again ");

                }
                //Packet p = (Packet) inputStream.readObject();
                //p.printPacket();
            }
        }
        catch(Exception e){ 
            e.printStackTrace(); 
        }

    }

    void send_packet_to_server(Packet p)
    {
        try
        { 
            outputStream.writeObject(p);
        }
        catch(Exception e){
            System.out.println ("Could not send packet! ");
        }
    }

    void send_quit_to_server()
    {
        Packet p = new Packet();
        p.sender=peerID;
        p.event_type=5;
        p.port_number=peer_listen_port;
        send_packet_to_server(p); 
    }

    public void cmdLineParser(String args[])
    {
        int i;
        for (i=0;i<args.length;i+=2)
        {
            String option=args[i];
            switch (option)
            {
                case "-c": //config_file
                File file = new File(args[i+1]);
                read_config_from_file(file);
                break;

                case "-i": //my ID
                peerID=Integer.parseInt(args[i+1]);
                break;

                case "-p": // my listen port
                peer_listen_port=Integer.parseInt(args[i+1]);
                break;

                case "-s": //server port
                serverPort=Integer.parseInt(args[i+1]);
                break;

                case "-n":
                try{ip = InetAddress.getByName(args[i+1]);} catch(Exception e){
                System.out.println ("Could not resolve hostname! " +args[i+1]);}
                break;

                default: System.out.println("Unknown flag "+args[i]);

            }

        }
    }

    public void read_config_from_file(File file )
    {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String opt[]= line.split(" ",2);
                switch(opt[0])
                {
                    case "SERVERPORT": serverPort=Integer.parseInt(opt[1]);break;
                    case "CLIENTID": peerID=Integer.parseInt(opt[1]);break;
                    case "MYPORT": peer_listen_port=Integer.parseInt(opt[1]);break;
                    case "FILE_VECTOR": FILE_VECTOR = opt[1].toCharArray();break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void send_req_for_file(int findex)
    {
        if (FILE_VECTOR[findex] =='1'){
            System.out.println("I already have this file block!");
            return;
        }
        System.out.println(" I don't have this file. Let me contact server...");
        //request file from server
        Packet p = new Packet();
        p.sender=peerID;
        p.event_type=1;
        p.peer_listen_port=peer_listen_port;
        p.req_file_index=findex;
        send_packet_to_server(p);
        //disconnect();

    }

    void disconnect()
    {
        try { 
                outputStream.close();
                inputStream.close();
                s.close();
                System.out.println("Closed Socket");
            }
        catch (Exception e) { System.out.println("Couldn't close socket!");}
    }
}

class PacketHandler extends Thread
{
    Client client;

    public PacketHandler(Client client)
    {
        this.client=client;
    }

    public void run()
    {
        Packet p;

        while(true){
        try { 
            p = (Packet) client.inputStream.readObject();
            process_packet_from_server(p);
        }
        catch (Exception e) { 
             //e.printStackTrace();
             break;
        }
    }

    }

    void process_packet_from_server(Packet p)
    {
     int e = p.event_type;

     switch (e)
     {
        case 2: //server reply for req. file
        if (p.peerID==-1)
            System.out.println("Server says that no client has file "+p.req_file_index);
        else{
            System.out.println("Server says that peer "+p.peerID+" on listening port "+p.peer_listen_port+" has file "+p.req_file_index);
            System.out.println("File hash: " + p.fileHash);
            PeerToPeerHandler(p.peerIP,p.peer_listen_port,p.peerID,p.req_file_index, p.fileHash); // TO DO
            }
        break;
        case 4:
        	System.out.println(p.DATA_BLOCK[p.data_block_size-1]);
        	break;

        case 6: //server wants to quit. I should too.
            System.out.println("Server wants to quit. I should too! ");
            client.disconnect();
           System.exit(0);

     }

    }
    
    void PeerToPeerHandler(InetAddress remotePeerIP, int remotePortNum, int remotePeerID, int findex, String fileHash)
    {
        boolean fileNotRec = true;
        
    	
    	// 1: Connect to Peer
    	try {
			client.peer = new Socket(remotePeerIP, remotePortNum);
			client.p2pOS = new ObjectOutputStream(client.peer.getOutputStream());
	        client.p2pIS = new ObjectInputStream(client.peer.getInputStream());
	        System.out.println("Connected to Client... " + client.peer);
	        
//	        Thread r = new PacketHandler(client);
//	        r.start();
	        
	        // Write Packet Request
	        Packet p = new Packet();
        	
        	p.peerID = client.peerID;
            p.sender = client.peerID;
            p.recipient = remotePeerID;
            p.event_type = 4;
            p.req_file_index = findex;
	        
         // 2: Request file from peer
        	System.out.println("Requesting file: " + findex + " from Peer: " + remotePeerID);
            client.p2pOS.writeObject(p);
            
            int indexCount = 0;
            byte[] file = new byte[20000];
            
	        while(fileNotRec) {
	        	
	        	// 3: Receive file from peer
	        	int packCount = 1;	        	
	        	while(packCount <= 20) {	        		
	        		Packet p2 = new Packet();
	            	p2 = (Packet) client.p2pIS.readObject();
	            	System.out.println("Packet number " + packCount + " received from Peer " + remotePeerID);
	            	// Reassemble
	            	for(int i = 0; i < p2.data_block_size; i++) {
	            		file[indexCount] = p2.DATA_BLOCK[i];
	            		indexCount++;
	            	}
	            	packCount++;
	        	}
            	
	        	// 4: Verify file hash
            	String rcvdFileHash = find_file_hash(file);
            	
            	if(rcvdFileHash.contentEquals(fileHash)) {
            		System.out.println("File hashes are the same");
            		Packet ackPack = new Packet();
            		ackPack.event_type = 3;
            		ackPack.gotFile = true;
            		ackPack.peerID = client.peerID;
            		ackPack.sender = ackPack.peerID;
            		ackPack.req_file_index = findex;
            		
            		client.p2pOS.writeObject(ackPack);
            		client.outputStream.writeObject(ackPack);
            		
            		client.FILE_VECTOR[findex] = '1';
            		
            		fileNotRec = false;
            	} else {
            		System.out.println("File hashes are not the same");
            		Packet ackPack = new Packet();
            		ackPack.event_type = 3;
            		ackPack.gotFile = false;
            		ackPack.peerID = client.peerID;
            		ackPack.sender = ackPack.peerID;
            		ackPack.req_file_index = findex;
            		
            		client.p2pOS.writeObject(ackPack);
            		
            		System.out.println("Requesting the file again...");
            		System.out.println("Requesting file: " + findex + " from Peer: " + remotePeerID);
                    indexCount = 0;
            		client.p2pOS.writeObject(p);
            	}
            	
	        	// if correct send + ack else send -ack & loop again
	        	
	        }
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
            
        //once, file has been received, send update file request to server.
        
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
