
import java.util.*;
import java.io.*;
import java.io.*;
import java.net.*;
import java.util.*;

class Packet implements Serializable{
    int sender;
    int recipient;
    int event_type; 
    int port_number;  // for reporting listening port number
    int req_file_index; //
    InetAddress peerIP; // for telling the client the peer IP
    int peerID; 
    int peer_listen_port;
    char FILE_VECTOR[];
    int data_block_size=1000; //each packet can carry 1000 bytes of data
    byte DATA_BLOCK[]; //byte array that holds the file.
    String fileHash; // contains the hash of the file, will be used by server when client requests file.
    boolean gotFile; // used for ACKs, true for positive , false for negative

    public Packet()
    {
        sender=-1;
        recipient=-1;
        event_type=-1;
        port_number=-1;
        req_file_index=-1;
        try{peerIP=InetAddress.getByName("127.0.0.1");}catch(Exception e){};
        //Arrays.fill(peerIP,'0');
        peerID=-1;
        peer_listen_port=-1;
        FILE_VECTOR=new char[64];
        Arrays.fill(FILE_VECTOR,'0');
        DATA_BLOCK = new byte[data_block_size];
        //Arrays.fill(DATA_BLOCK,'0');
        
    }


    void printPacket()
    {
        System.out.println("Packet Contents");
        System.out.println("---------------");
        System.out.println("Sender ID : "+sender);
        System.out.println("Receiver ID : "+recipient);
        System.out.println("Event Type : "+event_type);
        System.out.println("Port Number : "+port_number);
        System.out.println("Requested File Index : "+req_file_index);
        System.out.println("Peer IP : "+String.valueOf(peerIP));
        System.out.println("Peer ID : "+peerID);
        System.out.println("Peer Listen Port : "+peer_listen_port);
        System.out.println("File Vector : "+String.valueOf(FILE_VECTOR));
    }


}

