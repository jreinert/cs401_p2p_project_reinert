
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class ServerSocketHandler extends Thread
{

    Server s;
    ArrayList<Connection> connectionList;

    public ServerSocketHandler(Server s, ArrayList<Connection> connectionList){
        this.s=s;
        this.connectionList=connectionList;
    }

    public void run(){
        Socket clientSocket;
        while (true){
            clientSocket = null;
            try{

                clientSocket=s.listener.accept();
                System.out.println("A new client is connecting.. : " + clientSocket);
                System.out.println("Port : " + clientSocket.getPort());
                System.out.println("IP : " + clientSocket.getInetAddress().toString());
                Connection conn = new Connection(clientSocket, s.connectionList);
                //connectionList.add(conn);
                conn.start();

            }
            catch (SocketException e){  
                System.out.println("Shutting down Server....");
                // send a message to all clients that I want to quit.
                for (Connection c: connectionList)
                {
                    c.send_quit_message();
                    c.closeConnection();
                }
                connectionList.clear();
                break;

            }
            catch (IOException e){  
                e.printStackTrace(); 
            }


        }
    }

}