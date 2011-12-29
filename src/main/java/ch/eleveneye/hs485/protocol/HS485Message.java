package ch.eleveneye.hs485.protocol;

public abstract class HS485Message {
	protected boolean	hasSourceAddr;

	protected int			sourceAddress;

	protected int			targetAddress;

	public int getSourceAddress() {
		return sourceAddress;
	}

	public int getTargetAddress() {
		return targetAddress;
	}

	public boolean hasSourceAddr() {
		return hasSourceAddr;
	}

	public void setHasSourceAddr(final boolean hasSourceAddr) {
		this.hasSourceAddr = hasSourceAddr;
	}

	public void setSourceAddress(final int sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public void setTargetAddress(final int targetAddress) {
		this.targetAddress = targetAddress;
	}

	protected void dumpAddresses(final StringBuffer ret) {
		if (hasSourceAddr()) {
			ret.append(Integer.toHexString(getSourceAddress()));
			ret.append(" ");
		}
		ret.append("-> ");
		ret.append(Integer.toHexString(getTargetAddress()));
	}
}
