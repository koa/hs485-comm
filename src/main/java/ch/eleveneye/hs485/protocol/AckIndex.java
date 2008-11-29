/**
 * 
 */
package ch.eleveneye.hs485.protocol;

class AckIndex extends IntByteIndex {
	public AckIndex(int address, byte ackNumber) {
		super(address, ackNumber);
	}

	public synchronized byte getAckNumber() {
		return byteValue;
	}

	public synchronized void setAckNumber(byte ackNumber) {
		byteValue = ackNumber;
	}

	public synchronized int getAddress() {
		return intValue;
	}

	public synchronized void setAddress(int address) {
		intValue = address;
	}

	@Override
	public String toString() {
		return "AckIndex: " + Integer.toHexString(intValue) + ":" + byteValue;
	}
}