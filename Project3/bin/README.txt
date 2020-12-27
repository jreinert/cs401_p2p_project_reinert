Description: Project3 is a Peer to Peer program that allows a Client to query the server
to discover which other connected Peers holds the requested file and then connects the Peer
requesting the file to the Peer that holds the file. The Peer that holds the file sends the
file to the requesting Peer, a file hash sent from the Server to the requesting Peer is compared
to a computed file hash after receiving the file and if they match, the connection is closed.
Otherwise, the file is transfered until the correct file is received. 
Author: Jeremy Reinert
Date: 12/4/2019
Version: 1.0

Project3 - readme.txt
 
== SERVER ==

How to Project3 Server:

1) Open terminal and navigate to the directory where ../Project3/src is located
2) Compile the program with: javac *.java
3) Start the Server with: java Server

Server Commands:

-'q': Quit Server and any Clients connected to the Server
-'p': Print out Clients connected to the server

== CLIENT ==

1) Open up a terminal for each Client and navigate to the directory where ../Project3/src is located
2) If program has not been compiled, compile with: javac *.java
3) Start Client with: java Client -c "NAMEOFCLIENTCONFIGFILEHERE.txt"

Client Commands:

-'q': Quit Client
-'f': Start querying process for a file index

== CLIENTCONFIG FILES ==

- "clientconfig1.txt"
- "clientconfig2.txt"
- "clientconfig3.txt"
- "clientconfig4.txt"
- "clientconfig5.txt"
- "clientconfig6.txt"
- "clientconfig7.txt"
- "clientconfig8.txt"
- "clientconfig9.txt"
- "clientconfig10.txt"




