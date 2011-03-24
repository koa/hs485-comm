package ch.eleveneye.hs485.api.data;

public class SwVer {
	private final byte	majorVersion;

	private final byte	minorVersion;

	public SwVer(final byte majorVersion, final byte minorVersion) {
		super();
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	@Override
	public boolean equals(final Object obj) {
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

	public byte getMajorVersion() {
		return majorVersion;
	}

	public byte getMinorVersion() {
		return minorVersion;
	}

	@Override
	public int hashCode() {
		return minorVersion ^ majorVersion << 8;
	}

	@Override
	public String toString() {
		return "SwVer: " + majorVersion + "." + minorVersion;
	}
}
