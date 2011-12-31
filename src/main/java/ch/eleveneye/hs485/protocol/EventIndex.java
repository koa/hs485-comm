package ch.eleveneye.hs485.protocol;

public class EventIndex {

	private final int	address;
	private final int	sensor;

	public EventIndex(final int address, final int sensor) {
		this.address = address;
		this.sensor = sensor;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EventIndex other = (EventIndex) obj;
		if (address != other.address)
			return false;
		if (sensor != other.sensor)
			return false;
		return true;
	}

	public int getAddress() {
		return address;
	}

	public int getSensor() {
		return sensor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
		result = prime * result + sensor;
		return result;
	}

}
