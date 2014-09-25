/**
 * Class CreatePacket creates RRQ and ACK packets. 
 *
 * @author  Shobhit Dutia
 * @version 24th-September-2014
 */
public class CreatePacket {
	byte opCode[];
	byte fileName[];
	byte zero[];
	byte mode[];
	byte rrqData[];
	byte ackData[];
	String tftpMode="octet";
	/**
	 * Creates an RRQ packet. 
	 * 
	 * @param filePath Path of the destination file. 
	 * @return RRQ packet
	 */
	public byte[] createRRQPacket(String filePath) {
		opCode=new byte[2];
		opCode[1]=(byte)(1&0xff);
		fileName=new byte[filePath.length()];
		fileName=filePath.getBytes();
		zero=new byte[1];
		mode=new byte[tftpMode.length()];
		mode=tftpMode.getBytes();
		rrqData=new byte[opCode.length+fileName.length+mode.length+2];
		System.arraycopy(opCode, 0, rrqData, 0, opCode.length);
		System.arraycopy(fileName, 0, rrqData, opCode.length, fileName.length);
		System.arraycopy(zero, 0, rrqData, opCode.length+fileName.length, zero.length);
		System.arraycopy(mode, 0, rrqData, opCode.length+fileName.length+zero.length, mode.length);
		System.arraycopy(zero, 0, rrqData, opCode.length+fileName.length+zero.length+mode.length, zero.length);
		return rrqData;
	} 
	/**
	 * Creates ACK packet for the given block number. 
	 * 
	 * @param blockNo Block number. 
	 * @return ACK packet
	 */
	public byte[] createACKPacket(byte blockNo[]) {
		opCode=new byte[2];
		opCode[1]=(byte)(4&0xff);
		ackData=new byte[opCode.length+blockNo.length];
		System.arraycopy(opCode, 0, ackData, 0, opCode.length);
		System.arraycopy(blockNo, 0, ackData, opCode.length, blockNo.length);
		return ackData;
	}
}