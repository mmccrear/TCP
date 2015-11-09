package TCP;
import java.io.IOException;

/**
 * Implements simulator using rdt3.0 protocol
 * 
 * @author rms
 *
 */
public class RDT30 extends RTDBase {
	int timeout;
	/**
	 * Constructs an RDT22 simulator with given munge factor, loss factor and file feed
	 * @param pmunge		probability of character errors
	 * @param plost			probability of packet loss
	 * @param timeout		receive timeout in milliseconds 
	 * @param filename		file used for automatic data feed
	 * @throws IOException	if channel transmissions fail
	 */
	public RDT30(double pmunge, double plost, int timeout, String filename) throws IOException {
		super(pmunge, plost, filename);
		this.timeout = timeout;
		backward = new TUChannel(pmunge, plost);
		sender = new RSender30();
		receiver = new RReceiver30();
	}

	/**
	 * Packet appropriate for rdt3.0;
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
	 * RSender Class implementing rdt3.0 protocol
	 * @author rms
	 *
	 */
	public class RSender30 extends RSender {
		Packet packet = null;
		TUChannel backward = (TUChannel)RDT30.this.backward;
		@Override
		public int loop(int myState) throws IOException {
			switch(myState) {
			    // Your code here
			}
			return myState;
		}
	}

	/**
	 * RReceiver Class implementing rdt3.0 protocol
	 * @author rms
	 *
	 */
	public class RReceiver30 extends RReceiver {
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
	 * @param args	[-m pmunge][-l ploss][-t timeout][-f filename]
	 * @throws IOException	if i/o error occurs
	 */
	public static void main(String[] args) throws IOException {
		Object[] pargs = argParser("RDT10", args);
		RDT30 rdt30 = new RDT30((Double)pargs[0], (Double)pargs[1], (Integer)pargs[2], (String)pargs[3]);
		rdt30.run();
	}
	
}
