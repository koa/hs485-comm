package ch.eleveneye.hs485.protocol;

public class IMessage extends HS485Message {
	public enum KeyEventType {
		UNKNOWN, PRESS, HOLD, RELEASE
	}

	public enum KeyType {
		UNKNOWN, TOGGLE, DOWN, UP
	}

	protected byte[] data;

	protected boolean duplicatePacket;

	protected boolean lastPacket = true;

	protected byte receiveNumber;

	protected byte senderNumber;

	protected boolean sync;

	public byte[] getData() {
		return data;
	}

	public byte getReceiveNumber() {
		return receiveNumber;
	}

	public byte getSenderNumber() {
		return senderNumber;
	}

	public boolean isDuplicatePacket() {
		return duplicatePacket;
	}

	public boolean isLastPacket() {
		return lastPacket;
	}

	public boolean isSync() {
		return sync;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setDuplicatePacket(boolean duplicatePacket) {
		this.duplicatePacket = duplicatePacket;
	}

	public void setLastPacket(boolean lastPacket) {
		this.lastPacket = lastPacket;
	}

	public void setReceiveNumber(byte receiveNumber) {
		this.receiveNumber = receiveNumber;
	}

	public void setSenderNumber(byte senderNumber) {
		this.senderNumber = senderNumber;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public KeyEventType readKeyEventType() {
		if (data.length < 4)
			return KeyEventType.UNKNOWN;
		return decodeEventType(data[3]);
	}

	public static KeyEventType decodeEventType(byte keyValue) {
		switch ((keyValue >> 6) & 0x03) {
		case 0x00:
			return KeyEventType.PRESS;
		case 0x01:
			return KeyEventType.HOLD;
		case 0x02:
			return KeyEventType.RELEASE;
		default:
			return KeyEventType.UNKNOWN;
		}
	}

	public KeyType readKeyType() {
		if (data.length < 4)
			return KeyType.UNKNOWN;
		return decodeKeyType(data[3]);
	}

	public static KeyType decodeKeyType(byte keyValue) {
		switch ((keyValue >> 2) & 0x03) {
		case 0x00:
			return KeyType.TOGGLE;
		case 0x01:
			return KeyType.UP;
		case 0x02:
			return KeyType.DOWN;
		default:
			return KeyType.UNKNOWN;
		}
	}

	public byte readKeyPressCount() {
		if (data.length < 4)
			return 0;
		return decodeKeyPressCount(data[3]);
	}

	public static byte decodeKeyPressCount(byte key) {
		return (byte) ((key >> 4) & 0x03);
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		if (isDuplicatePacket()) {
			ret.append("DUP! ");
		}
		ret.append("I: ");
		dumpAddresses(ret);
		ret.append(" ");
		if (isSync()) {
			ret.append("Sync ");
		}
		ret.append("S:");
		ret.append(getSenderNumber());
		ret.append(" R:");
		ret.append(getReceiveNumber());
		ret.append(" :: ");
		for (byte element : data) {
			ret.append(Integer.toHexString(element & 0xff));
			ret.append(" ");
		}

		return ret.toString();
	}

}
