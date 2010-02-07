package ch.eleveneye.hs485.protocol.data;

public class HwVer {
	byte hwType;

	byte hwVer;

	public HwVer(byte type, byte hwVer) {
		super();
		hwType = type;
		this.hwVer = hwVer;
	}

	public byte getHwType() {
		return hwType;
	}

	public byte getHwVer() {
		return hwVer;
	}

	@Override
	public String toString() {
		return "HwVer: " + hwType + ":" + hwVer;
	}

	@Override
	public int hashCode() {
		return hwType ^ (hwVer << 8);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final HwVer other = (HwVer) obj;
		if (hwType != other.hwType)
			return false;
		if (hwVer != other.hwVer)
			return false;
		return true;
	}
}
