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

				System.out.printf("Sender(%d): %s\n", myState, packet.toString());
				System.out.printf(" **Sender(0->1) **\n");
				//printSender(myState, 1, packet.data, packet.checksum, packet.seqnum);
				forward.send(packet);
				return 1;
			case 1:
				backwardPacket = Packet.deserialize(backward.receive());
				System.out.printf(" **Sender(%d): %s **\n", myState, backwardPacket.toString());
				if(backwardPacket.data.equals("ACK") && !backwardPacket.isCorrupt() && backwardPacket.seqnum.equals("0")){
					//printSender(myState, 2, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
					System.out.printf(" **Sender(1->2)\n");
					return 2;
				}
				System.out.printf(" **Sender(1->1): wrong or corrupt acknowledgement; resending **\n", myState);
				//printSender(myState, 1, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
				forward.send(packet);
				return 1;
			case 2:
				dat = getFromApp(0);
				packet = new Packet(dat, "1");
				System.out.printf("Sender(%d): %s\n", myState, packet.toString());
				System.out.printf(" **Sender(2->3)\n");
				//printSender(myState, 3, packet.data, packet.checksum, packet.seqnum);
				forward.send(packet);
				return 3;
			case 3:
				backwardPacket = Packet.deserialize(backward.receive());
				System.out.printf(" **Sender(%d): %s **\n", myState, backwardPacket.toString());
				if(backwardPacket.data.equals("ACK") && !backwardPacket.isCorrupt() && backwardPacket.seqnum.equals("1")){
					//printSender(myState, 0, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
					System.out.printf(" **Sender(3->0)\n");
					return 0;
				}
				//printSender(myState, 3, backwardPacket.data, backwardPacket.checksum, backwardPacket.seqnum);
				System.out.printf(" **Sender(3->3): wrong or corrupt acknowledgement; resending **\n", myState);
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
		@Override
		public int loop(int myState) throws IOException {
			String dat;
			Packet packet;
			switch (myState) {
			case 0: 
				dat = forward.receive();
				packet = Packet.deserialize(dat);
				System.out.printf("\t **Receiver(%d): %s **\n", myState, packet.toString());
				if(!packet.isCorrupt()){
					if(packet.seqnum.equals("1")){
						System.out.printf("\t **Receiver(0->0): duplicate 1 packet; discarding; replying ACK/1 **\n");
						backward.send(new Packet("ACK", "1"));
						return 0;
					}
					//printRec(0, 1, packet.data, packet.checksum, packet.seqnum, false, false);
					System.out.printf("\t **Receiver(0->1): ok 0 data; replying ACK/0 **\n");
					deliverToApp(packet.data);
					backward.send(new Packet("ACK", "0"));
					return 1;
				}

				//printRec(0, 0, packet.data, packet.checksum, packet.seqnum, true, false);
				System.out.printf("\t **Receiver(0->0): corrupt data; replying ACK/1 **\n");
				backward.send(new Packet("ACK", "1"));
				return 0;

			case 1:
				dat = forward.receive();
				packet = Packet.deserialize(dat);
				System.out.printf("\t **Receiver(%d): %s **\n", myState, packet.toString());
				if(!packet.isCorrupt()){
					if(packet.seqnum.equals("0")){
						System.out.printf("\t **Receiver(1->1): duplicate 0 packet; discarding; replying ACK/0 **\n");
						backward.send(new Packet("ACK", "0"));
						return 1;
					}
					//printRec(1, 0, packet.data, packet.checksum, packet.seqnum, false, false);
					System.out.printf("\t **Receiver(1->0): ok 1 data; replying ACK/1 **\n");
					deliverToApp(packet.data);
					backward.send(new Packet("ACK", "1"));
					return 0;
				}

				//printRec(1, 1, packet.data, packet.checksum, packet.seqnum, true, false);
				System.out.printf("\t **Receiver(1->1): corrupt data; replying ACK/0 **\n");
				backward.send(new Packet("ACK", "0"));
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
