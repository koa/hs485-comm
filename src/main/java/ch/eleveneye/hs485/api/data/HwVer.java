package ch.eleveneye.hs485.api.data;

public class HwVer {
	private final byte	hwType;

	private final byte	hwVer;

	public HwVer(final byte type, final byte hwVer) {
		super();
		hwType = type;
		this.hwVer = hwVer;
	}

	@Override
	public boolean equals(final Object obj) {
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

	public byte getHwType() {
		return hwType;
	}

	public byte getHwVer() {
		return hwVer;
	}

	@Override
	public int hashCode() {
		return hwType ^ hwVer << 8;
	}

	@Override
	public String toString() {
		return "HwVer: " + hwType + ":" + hwVer;
	}
}
