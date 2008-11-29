package ch.eleveneye.hs485.protocol;

public abstract class IntByteIndex {

	protected byte	byteValue;

	protected int	 intValue;

	public IntByteIndex(int intValue, byte byteValue) {
		this.intValue = intValue;
		this.byteValue = byteValue;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + intValue;
		result = PRIME * result + byteValue;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final IntByteIndex other = (IntByteIndex) obj;
		if (byteValue != other.byteValue) return false;
		if (intValue != other.intValue) return false;
		return true;
	}
}