package ch.eleveneye.hs485.protocol.data;

import java.text.NumberFormat;

public class TFSValue {
	int temperatur;

	int humidity;

	public TFSValue() {

	}

	public TFSValue(final byte[] answerData) {
		temperatur = (answerData[0] << 8 | (answerData[1] & 0xff)) & 0xffff;
		humidity = answerData[2] & 0xff;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TFSValue other = (TFSValue) obj;
		if (humidity != other.humidity)
			return false;
		if (temperatur != other.temperatur)
			return false;
		return true;
	}

	public int getHumidity() {
		return humidity;
	}

	public int getTemperatur() {
		return temperatur;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + humidity;
		result = PRIME * result + temperatur;
		return result;
	}

	public double readTemperatur() {
		return temperatur / 10.0;
	}

	public void setHumidity(final int humidity) {
		this.humidity = humidity;
	}

	public void setTemperatur(final int temperatur) {
		this.temperatur = temperatur;
	}

	@Override
	public String toString() {
		return "Temp: " + NumberFormat.getInstance().format(readTemperatur())
				+ "°C, Hum: " + getHumidity() + "%";
	}
}
