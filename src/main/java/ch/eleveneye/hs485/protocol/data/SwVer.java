package ch.eleveneye.hs485.protocol.data;

public class SwVer {
	byte majorVersion;

	byte minorVersion;

	public SwVer(byte majorVersion, byte minorVersion) {
		super();
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	public byte getMajorVersion() {
		return majorVersion;
	}

	public byte getMinorVersion() {
		return minorVersion;
	}

	@Override
	public String toString() {
		return "SwVer: " + majorVersion + "." + minorVersion;
	}

	@Override
	public int hashCode() {
		return minorVersion ^ (majorVersion << 8);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final SwVer other = (SwVer) obj;
		if (majorVersion != other.majorVersion)
			return false;
		if (minorVersion != other.minorVersion)
			return false;
		return true;
	}
}
