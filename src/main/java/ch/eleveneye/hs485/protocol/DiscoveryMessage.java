package ch.eleveneye.hs485.protocol;

public class DiscoveryMessage extends HS485Message {
	byte maskCount;

	public byte getMaskCount() {
		return maskCount;
	}

	public void setMaskCount(byte maskCount) {
		this.maskCount = maskCount;
	}
}
