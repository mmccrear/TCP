package TCP;
import java.io.IOException;

/**
 * Implements simulator using rdt2.0 protocol
 * 
 * @author rms
 *
 */
public class RDT20 extends RTDBase {

	/**
	 * Constructs an RDT20 simulator with given munge factor
	 * @param pmunge		probability of character errors
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT20(double pmunge) throws IOException {this(pmunge, 0.0, null);}

	/**
	 * Constructs an RDT20 simulator with given munge factor, loss factor and file feed
	 * @param pmunge		probability of character errors
	 * @param plost			probability of packet loss
	 * @param filename		file used for automatic data feed
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT20(double pmunge, double plost, String filename) throws IOException {
		super(pmunge, plost, filename);
		sender = new RSender20();
		receiver = new RReceiver20();
	}

	/**
	 * Packet appropriate for rdt2.0;
	 * contains data and checksum
	 * @author rms
	 *
	 */
	public static class Packet extends RDT10.Packet {
		public Packet(String data){
			super(data);
		}
		public Packet(String data, String checksum) {
			super(data, checksum);
		}
		public static Packet deserialize(String data) {
			String hex = data.substring(0, 4);
			String dat = data.substring(4);
			return new Packet(dat, hex);
		}
	}

	/**
	 * RSender Class implementing rdt2.0 protocol
	 * @author rms
	 *
	 */
	public class RSender20 extends RSender {
		Packet packet = null;
		@Override
		public int loop(int myState) throws IOException {
			switch(myState) {
			case 0:
				String dat = getFromApp(0);
				packet = new Packet(dat);
				forward.send(packet);
				printSender(myState, 1, packet.data, packet.checksum,"");
				return 1;
				
			case 1: 
				Packet backwardPacket = Packet.deserialize(backward.receive());
				if(backwardPacket.data.equals("ACK") && !backwardPacket.isCorrupt()){
					printSender(myState, 0, backwardPacket.data, backwardPacket.checksum, "");
					return 0;
				}
				forward.send(packet);
				printSender(myState, 1, backwardPacket.data, backwardPacket.checksum, "");
				return 1;
				
			}
			
			return myState;						
		}
	}

	/**
	 * RReceiver Class implementing rdt2.0 protocol
	 * @author rms
	 *
	 */
	public class RReceiver20 extends RReceiver {
		
		@Override
		public int loop(int myState) throws IOException {
			switch (myState) {
			case 0:
				String dat = forward.receive();
				Packet packet = Packet.deserialize(dat);
				if(packet.isCorrupt()){
					Packet confirm = new Packet("ACK");
					deliverToApp(packet.data);
					printRec(0, 0, packet.data, packet.checksum, "", false, false);
					backward.send(confirm);
				}
				else{
					Packet confirm = new Packet("NAK");
					printRec(0, 0, packet.data, packet.checksum, "", true, false);
					backward.send(confirm);
				}
				return 0;
			}
			return myState;
		}
	}

	/**
	 * Runs rdt2.0 simulation
	 * @param args	[-m pmunge][-l ploss][-f filename]
	 * @throws IOException	if i/o error occurs
	 */
	public static void main(String[] args) throws IOException {
		Object[] pargs = argParser("RDT20", args);
		RDT20 rdt20 = new RDT20((Double)pargs[0], (Double)pargs[1], (String)pargs[3]);
		rdt20.run();
	}

}
