package ch.eleveneye.hs485.api.data;

public class KeyMessage {
	private KeyEventType	keyEventType;
	private KeyType				keyType;
	private int						sourceAddress;
	private int						sourceSensor;
	private int						targetActor;
	private int						targetAddress;
	private int						hitCount;

	public int getHitCount() {
		return hitCount;
	}

	public KeyEventType getKeyEventType() {
		return keyEventType;
	}

	public KeyType getKeyType() {
		return keyType;
	}

	public int getSourceAddress() {
		return sourceAddress;
	}

	public int getSourceSensor() {
		return sourceSensor;
	}

	public int getTargetActor() {
		return targetActor;
	}

	public int getTargetAddress() {
		return targetAddress;
	}

	public void setHitCount(final int hitCount) {
		this.hitCount = hitCount;
	}

	public void setKeyEventType(final KeyEventType keyEventType) {
		this.keyEventType = keyEventType;
	}

	public void setKeyType(final KeyType keyType) {
		this.keyType = keyType;
	}

	public void setSourceAddress(final int sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public void setSourceSensor(final int sourceSensor) {
		this.sourceSensor = sourceSensor;
	}

	public void setTargetActor(final int targetActor) {
		this.targetActor = targetActor;
	}

	public void setTargetAddress(final int targetAddress) {
		this.targetAddress = targetAddress;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("KeyMessage [source=");
		builder.append(Integer.toHexString(sourceAddress));
		builder.append(":");
		builder.append(sourceSensor);
		builder.append(", target=");
		builder.append(Integer.toHexString(targetAddress));
		builder.append(":");
		builder.append(targetActor);
		builder.append(", ");
		if (keyType != null) {
			builder.append("keyType=");
			builder.append(keyType);
			builder.append(", ");
		}
		if (keyEventType != null) {
			builder.append("keyEventType=");
			builder.append(keyEventType);
			builder.append(", ");
		}
		builder.append("hitCount=");
		builder.append(hitCount);
		builder.append("]");
		return builder.toString();
	}
}
