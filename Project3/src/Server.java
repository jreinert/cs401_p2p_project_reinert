
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    
    int serverPort;
    int MAX_CONNECTED_CLIENTS;
    ServerSocket listener;
    int numClients;
    ArrayList<Connection> connectionList;

    
    public Server(){
        //serverPort= 0;
        MAX_CONNECTED_CLIENTS=20;
        listener=null;
        numClients=0;
        connectionList= new ArrayList<Connection>();
    }
    
    public static void main(String args[])
    {
        
        //First, let's start our server and bind it to a port(5000).
        Server s = new Server();
        s.serverPort = 5000;
        boolean runServer=true;
        try{
            s.listener = new ServerSocket(s.serverPort); 
        }
        catch (Exception e){ 
            e.printStackTrace();
        }
        System.out.println("Started Server. Waiting for a client ..."); 
        
        //Next let's start a thread that will handle incoming connections
        ServerSocketHandler ssHandler = new ServerSocketHandler(s, s.connectionList);
        ssHandler.start();


        //Done! Now main() will just loop for user input!.
        Scanner input = new Scanner(System.in);
        System.out.println ("Enter query");

        while (runServer)
        {
            char cmd=input.next().charAt(0);

            switch (cmd)
            {
                case 'q':
                //send a message to SShandler that server wants to quit
                // closing the socket will force ssHandler, to break out of accept()
                //System.out.println("User requested shutdown..");
                runServer=false;
                System.out.println(java.lang.Thread.activeCount());
                try{s.listener.close();
                Thread.sleep(2000);}
                catch(Exception e){ e.printStackTrace();}
                System.out.println("Active Threads : "+java.lang.Thread.activeCount());
                break;

                case 'p':
                s.printConnections();
                break;

            }

        }
        //will quit on user input
        
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

}

