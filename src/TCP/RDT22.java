package TCP;
import java.io.IOException;

import TCP.RDT21.Packet;
import TCP.RTDBase.RReceiver;
import TCP.RTDBase.RSender;

/**
 * Implements simulator using rdt2.2 protocol
 * 
 * @author rms
 *
 */
public class RDT22 extends RTDBase {
	
	/**
	 * Constructs an RDT22 simulator with given munge factor
	 * @param pmunge		probability of character errors
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT22(double pmunge) throws IOException {this(pmunge, 0.0, null);}

	/**
	 * Constructs an RDT22 simulator with given munge factor, loss factor and file feed
	 * @param pmunge		probability of character errors
	 * @param plost			probability of packet loss
	 * @param filename		file used for automatic data feed
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT22(double pmunge, double plost, String filename) throws IOException {
		super(pmunge, plost, filename);
		sender = new RSender22();
		receiver = new RReceiver22();
	}

	/**
	 * Packet appropriate for rdt2.2;
	 * contains data, seqnum and checksum
	 * @author rms
	 *
	 */
	public static class Packet extends RDT21.Packet {
		public Packet(String data){
			super(data);
		}
		public Packet(String data, String seqnum){
			super(data, seqnum);
		}
		public Packet(String data, String seqnum, String checksum) {
			super(data, seqnum, checksum);
		}
		public static Packet deserialize(String data) {
			String hex = data.substring(0, 4);
			String seqnum = data.substring(4,5);
			String dat = data.substring(5);
			return new Packet(dat, seqnum, hex);
		}
	}
	
	/**
	 * RSender Class implementing rdt2.2 protocol
	 * @author rms
	 *
	 */
	public class RSender22 extends RSender {
		Packet packet = null;
		@Override
		public int loop(int myState) throws IOException {
			String dat;
			Packet backwardPacket;
			switch(myState) {
			case 0:
				dat = getFromApp(0);
				packet = new Packet(dat, "0");
				printSender(myState, 1, packet.data, packet.checksum, packet.seqnum);
				forward.send(packet);
				return 1;
			case 1:
				backwardPacket = Packet.deserialize(backward.receive());
				if(backwardPacket.data.equals("ACK") && backwardPacket.checksum.equals(CkSum.genCheck(packet.seqnum+"ACK"))){
					printSender(myState, 2, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
					return 2;
				}
				printSender(myState, 1, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
				forward.send(packet);
				return 1;
			case 2:
				dat = getFromApp(0);
				packet = new Packet(dat, "1");
				printSender(myState, 3, packet.data, packet.checksum, packet.seqnum);
				forward.send(packet);
				return 3;
			case 3:
				backwardPacket = Packet.deserialize(backward.receive());
				if(backwardPacket.data.equals("ACK") && backwardPacket.checksum.equals(CkSum.genCheck(packet.seqnum+"ACK"))){
					printSender(myState, 0, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
					return 0;
				}
				printSender(myState, 3, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
				forward.send(packet);
				return 3;
			}
			return myState;
		}
	}

	/**
	 * RReceiver Class implementing rdt2.1 protocol
	 * @author rms
	 *
	 */
	public class RReceiver22 extends RReceiver {
		String seqNum="0";
		String badSeqNum = "1";
		@Override
		public int loop(int myState) throws IOException {
			String dat;
			Packet packet, confirm;
			switch (myState) {
			case 0: 
				dat = forward.receive();
				packet = Packet.deserialize(dat);
				seqNum = packet.seqnum;
				if(CkSum.checkString(packet.seqnum+packet.data, packet.checksum)){
					confirm = new Packet("ACK", seqNum);
					if(seqNum.equals(packet.seqnum)){
						printRec(0, 1, packet.data, packet.checksum, packet.seqnum, false, false);
						deliverToApp(packet.data);
						backward.send(confirm);
						seqNum="1";
						badSeqNum="0";
						return 1;
					}
				}
				confirm = new Packet("ACK", badSeqNum);
				printRec(0, 0, packet.data, packet.checksum, packet.seqnum, true, false);
				backward.send(confirm);
				return 0;
			case 1:
				dat = forward.receive();
				packet = Packet.deserialize(dat);
				seqNum = packet.seqnum;
				if(CkSum.checkString(packet.seqnum+packet.data, packet.checksum)){
					confirm = new Packet("ACK", seqNum);
					if(seqNum.equals(packet.seqnum)){
						printRec(1, 0, packet.data, packet.checksum, packet.seqnum, false, false);
						deliverToApp(packet.data);
						backward.send(confirm);
						seqNum="0";
						badSeqNum="1";
						return 0;
					}
				}
				confirm = new Packet("ACK", badSeqNum);
				printRec(1, 1, packet.data, packet.checksum, packet.seqnum, true, false);
				backward.send(confirm);
				return 1;
			}
			return myState;
		}
	}

	
	/**
	 * Runs rdt2.2 simulation
	 * @param args	[-m pmunge][-l ploss][-f filename]
	 * @throws IOException	if i/o error occurs
	 */
	public static void main(String[] args) throws IOException {
		Object[] pargs = argParser("RDT22", args);
		RDT22 rdt22 = new RDT22((Double)pargs[0], (Double)pargs[1], (String)pargs[3]);
		rdt22.run();
	}
	
}
