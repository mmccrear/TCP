package TCP;
import java.io.IOException;

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
			switch(myState) {
			    // Your code here
			}
			return myState;
		}
	}

	/**
	 * RReceiver Class implementing rdt2.2 protocol
	 * @author rms
	 *
	 */
	public class RReceiver22 extends RReceiver {
		@Override
		public int loop(int myState) throws IOException {
			switch (myState) {
			    // Your code here
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
