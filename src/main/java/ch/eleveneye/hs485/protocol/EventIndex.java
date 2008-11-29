package ch.eleveneye.hs485.protocol;

public class EventIndex extends IntByteIndex {

	public EventIndex(int address, byte sensor) {
		super(address, sensor);
	}

	public byte getSensor() {
		return byteValue;
	}

	public int getAddress() {
		return intValue;
	}

}
