TFTP client
The TFTP client connects glados.cs.rit.edu(or any other TFTP server) and implements the methods 'get', 'quit', '?'
To run the TFTP client, follow the steps below:
1. Compile:
javac Client.java CreatePacket.java

2. Run:
java Client

The following commands are implemented:
connect 	Connect to TFTP server (Usage: connect hostname)
get             receive file (Usage: get srcPath destPath)
quit            exit tftp (Usage: quit)
?               print help information(Usage: ?)