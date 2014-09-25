import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
/**
 * Class Client is a TFTP client which connects a TFTP server. 
 * This client implements only a single TFTP method 'get'.
 *
 * @author  Shobhit Dutia
 * @version 24th-September-2014
 */
public class Client {
	CreatePacket cp;
	String hostName;
	InetAddress hostNameAddress;
	/**
	 * Main program.
	 * 
	 * @param  args  Command line arguments.
	 */
	public static void main(String[] args) {
		Client c=new Client();
		c.init();	
	}
	/**
	 * Gets arguments viz. connect, get, quit from standard in.
	 * Runs until 'quit' is called.
	 * 
	 * @exception IOException Thrown when an input/output operation is failed.
	 */
	private void init() {
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("tftp> ");
			String command[]=br.readLine().split(" ");
			while(!command[0].equalsIgnoreCase("quit")) {
				if(command.length==1) {
					if(command[0].equalsIgnoreCase("connect")) {
						System.out.println("(to) ");
						hostName=br.readLine();
						if(!checkAddress()) {
							command=br.readLine().split(" ");
							continue;
						}					
					}
					else if(command[0].equalsIgnoreCase("?")) {
						System.out.println("1. To connect to a server-Usage: connect <hostName>");
						System.out.println("2. After connecting, to get file from server-"
								+ "Usage: get srcPath destPath");
						System.out.print("tftp> ");
						command=br.readLine().split(" ");
						continue;
					}
				}
				else if(command.length==2&&command[0].equalsIgnoreCase("connect")) {			
					hostName=command[1];
					if(!checkAddress()) {
						command=br.readLine().split(" ");
						continue;
					}
				}
				else {
					System.out.println("Invalid command. Usage: connect <hostName>");
					System.out.print("tftp> ");
					command=br.readLine().split(" ");
					continue;
				}
				while(true) {
					command=br.readLine().split(" ");
					if(command[0].equalsIgnoreCase("get")&&command.length==3) {
						this.getFile(command[1], command[2]);	
						System.out.print("tftp> ");
						continue;
					}
					else if(command[0].equalsIgnoreCase("quit")) {
						break;
					}
					else if(command[0].equalsIgnoreCase("?")) {
						System.out.println("1. To connect to a server-Usage: connect <hostName>");
						System.out.println("2. After connecting, to get file from server-"
								+ "Usage: get srcPath destPath");
						System.out.print("tftp> ");
						continue;
					}
					else {
						System.out.println("Invalid command. Usage: get srcPath destPath");
						System.out.print("tftp> ");
					}
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Resolves the host name address.  
	 * 
	 * @exception UnknownHostException Thrown when the host name is invalid.
	 * @return True is the host name is valid. False otherwise.
	 */
	private boolean checkAddress() {
		try {
			hostNameAddress=java.net.InetAddress.getByName(hostName);
			System.out.println("Connected to "+hostName);
			System.out.print("tftp> ");		
			return true;
		} catch (java.net.UnknownHostException e) {
			System.out.println(hostName+": Unknown host");
			System.out.print("tftp> ");
			return false;
		}
	}
	/**
	 * Sends an RRQ request for the specific filename and retrieves 
	 * the data from the TFTserver in blocks of 512 bytes.
	 * While retrieving the data, it also appends the data to the 
	 * appropriate destination. 
	 * 
	 * @exception SocketTimeoutException 
	 * @exception FileNotFoundException
	 * @exception IOException
	 */
	private void getFile(String fileName, String destination) {
		cp=new CreatePacket();
		byte rrqData[]=cp.createRRQPacket(fileName);
		DatagramSocket socket = null;
		DatagramPacket sendPacket;
		DatagramPacket recievePacket;
		try {
			socket=new DatagramSocket(4549);
			socket.setSoTimeout(5000);
			
			sendPacket = new DatagramPacket(rrqData,rrqData.length, 
					hostNameAddress, 69);
			socket.send(sendPacket);
			byte receiveBuffer[]=new byte[516];
			recievePacket=new DatagramPacket(receiveBuffer,receiveBuffer.length);
			byte blockNo[]=new byte[2];
			byte opCode[]=new byte[2];
			byte prevBlockNo[]=new byte[2];
			
			File dest=new File(destination);
			dest.createNewFile();
			if(dest.isDirectory()) {
				throw new FileNotFoundException();
			}
			RandomAccessFile rf=new RandomAccessFile(dest, "rw");
			int blockNoInt,opCodeInt,prevBlockNoInt, fileLength=0;
			byte ackData[] = null;
			do {
				receiveBuffer=new byte[516];
				recievePacket=new DatagramPacket(receiveBuffer,receiveBuffer.length);
				socket.receive(recievePacket);
				System.arraycopy(receiveBuffer, 0, opCode, 0, 2);
				System.arraycopy(receiveBuffer, 2, blockNo, 0, 2);
				opCodeInt=((opCode[0]&0xff)<<8)|(opCode[1]&0xff);
				blockNoInt=((blockNo[0]&0xff)<<8)|(blockNo[1]&0xff);
				if(opCodeInt==5) {
					switch(blockNoInt) {
					case 0:
						System.out.println("Error code 0 received from server:"
								+ "Not defined, see error message (if any).");
						break;
					case 1:
						System.out.println("Error code 1 received from server:File not found");
						break;
					case 2:
						System.out.println("Error code 2 received from server:Access violation");
						break;
					case 3:
						System.out.println("Error code 3 received from server:Disk"
								+ " full or allocation exceeded");
						break;
					case 4:
						System.out.println("Error code 4 received from server:Illegal TFTP operation");
						break;
					case 5:
						System.out.println("Error code 5 received from server:Unknown transfer ID");
					case 6:
						System.out.println("Error code 6 received from server:File already exists");
						break;
					case 7:
						System.out.println("Error code 7 received from server:No such user");
						break;
					default:
						System.out.println("Error code from server");
					}
					System.exit(0);
				}
				prevBlockNoInt=((prevBlockNo[0]&0xff)<<8)|(prevBlockNo[1]&0xff);
				if(!(blockNoInt==prevBlockNoInt)) {
					ackData=cp.createACKPacket(blockNo);
					sendPacket = new DatagramPacket(ackData,ackData.length, 
							hostNameAddress, recievePacket.getPort());
					System.arraycopy(blockNo, 0, prevBlockNo, 0, blockNo.length);
					rf.seek(fileLength);
					fileLength+=recievePacket.getLength()-4;
					rf.write(receiveBuffer,4,recievePacket.getLength()-4);
					socket.send(sendPacket);
				} else {
					System.out.println("in else");
					ackData=cp.createACKPacket(blockNo);
					sendPacket = new DatagramPacket(ackData,ackData.length, 
							hostNameAddress, recievePacket.getPort());
					socket.send(sendPacket);
				}
			} while(recievePacket.getLength()==516);
			rf.close();
		} catch(SocketTimeoutException e) {
			System.out.println("Error: Time out occured!");
		} catch (FileNotFoundException e) {
			System.out.println("Invalid desination file name");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socket.close(); 			
		}
	}
}