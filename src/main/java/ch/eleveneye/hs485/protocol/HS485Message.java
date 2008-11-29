package ch.eleveneye.hs485.protocol;

public abstract class HS485Message {
	protected boolean	hasSourceAddr;

	protected int	    sourceAddress;

	protected int	    targetAddress;

	public boolean hasSourceAddr() {
		return hasSourceAddr;
	}

	public void setHasSourceAddr(boolean hasSourceAddr) {
		this.hasSourceAddr = hasSourceAddr;
	}

	public int getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(int sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public int getTargetAddress() {
		return targetAddress;
	}

	public void setTargetAddress(int targetAddress) {
		this.targetAddress = targetAddress;
	}

	protected void dumpAddresses(StringBuffer ret) {
		if (hasSourceAddr()) {
			ret.append(Integer.toHexString(getSourceAddress()));
			ret.append(" ");
		}
		ret.append("-> ");
		ret.append(Integer.toHexString(getTargetAddress()));
	}
}
