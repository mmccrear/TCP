package TCP;
import java.io.IOException;
/**
 * Implements simulator using rdt1.0 protocol
 * 
 * @author rms
 *
 */
public class RDT10 extends RTDBase {
	
	/**
	 * Constructs an RDT10 simulator with given munge factor
	 * @param pmunge		probability of character errors
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT10(double pmunge) throws IOException {this(pmunge, 0.0, null);}

	/**
	 * Constructs an RDT10 simulator with given munge factor, loss factor and file feed
	 * @param pmunge		probability of character errors
	 * @param plost			probability of packet loss
	 * @param filename		file used for automatic data feed
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT10(double pmunge, double plost, String filename) throws IOException {
		super(pmunge, plost, filename);
		sender = new RSender10();
		receiver = new RReceiver10();
	}
	/**
	 * Packet appropriate for rdt1.0;
	 * contains data and checksum
	 * @author rms
	 *
	 */
	public static class Packet implements PacketType{
		protected String checksum;
		protected String data;
		/**
		 * Constructs a packet out of data with computed checksum
		 * @param data	content of this packet
		 */
		public Packet(String data){
			this(data, CkSum.genCheck(data));
		}
		/**
		 * Constructs a packet out of data with assigned checksum
		 * @param data	content of this packet
		 * @param checksum	assigned checksum	
		 */
		public Packet(String data, String checksum) {
			this.data = data;
			this.checksum = checksum;
		}
		/**
		 * Static method to create a packet from serialized data 
		 * @param data	serialized version of a packet created by the serialize method 
		 * @return	packet constructed from data
		 */
		public static Packet deserialize(String data) {
			String hex = data.substring(0, 4);
			String dat = data.substring(4);
			return new Packet(dat, hex);
		}
		@Override
		/**
		* Implements serialize method of PacketType
		*/
		public String serialize() {
			return checksum+data;
		}
		@Override
		/**
		 * Implements isCorrupt method of PacketType
		 */
		public boolean isCorrupt() {
			return !CkSum.checkString(data, checksum);
		}
		@Override
		/**
		 * For printing in output log
		 */
		public String toString() {
			return String.format("%s (%s/%s)", data, checksum, CkSum.genCheck(data));
		}
	}

	/**
	 * RSender Class implementing rdt1.0 protocol
	 * @author rms
	 *
	 */

	public class RSender10 extends RSender {
		@Override
		public int loop(int myState) throws IOException {
			switch(myState) {
			case 0:
				String dat = getFromApp(0);
				forward.send(new Packet(dat));
				return 0;
			}
			return myState;				
		}
	}
	
	/**
	 * RReceiver Class implementing rdt1.0 protocol
	 * @author rms
	 *
	 */
	public class RReceiver10 extends RReceiver {
		@Override
		public int loop(int myState) throws IOException {
			switch (myState) {
			case 0:
				String dat = forward.receive();
				Packet packet = Packet.deserialize(dat);
				deliverToApp(packet.data);
				return 0;
			}
			return myState;				
		}
	}
	/**
	 * Runs rdt1.0 simulation
	 * @param args	[-m pmunge][-l ploss][-f filename]
	 * @throws IOException	if i/o error occurs
	 */
	public static void main(String[] args) throws IOException {
		Object[] pargs = argParser("RDT10", args);
		RDT10 rdt10 = new RDT10((Double)pargs[0], (Double)pargs[1], (String)pargs[3]);
		rdt10.run();
	}
}