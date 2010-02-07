package ch.eleveneye.hs485.protocol;

public class ACKMessage extends HS485Message {
	byte receiveNumber;

	public byte getReceiveNumber() {
		return receiveNumber;
	}

	public void setReceiveNumber(byte receiveNumber) {
		this.receiveNumber = receiveNumber;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer("Ack: ");
		dumpAddresses(ret);
		ret.append(" R:");
		ret.append(getReceiveNumber());
		return ret.toString();
	}
}
