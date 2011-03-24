package ch.eleveneye.hs485.dummy.device;

import java.util.Arrays;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;

/**
 * Abstract Dummy-Device for Test
 * 
 */
public abstract class Device {
	protected byte[]	actor;
	protected byte[]	data	= new byte[512];

	public Device(final int actorCount) {
		actor = new byte[actorCount];
		Arrays.fill(data, (byte) 0xff);
		Arrays.fill(actor, (byte) 0xff);
	}

	/**
	 * read current Value of a Actor
	 * 
	 * @param actorNr
	 *          Index of Actor
	 * @return currentValue of Actor
	 */
	public synchronized byte readActor(final byte actorNr) {
		return actor[actorNr];
	}

	/**
	 * Reads HW-Version of current device
	 * 
	 * @return
	 */
	public abstract HwVer readHwVer();

	/**
	 * Reads the Brightness from Lux-Sensor
	 * 
	 * @return Brightness
	 */
	public int readLux() {
		return 0;
	}

	/**
	 * Reads config-Data from Module
	 * 
	 * @param count
	 *          count of Bytes
	 * @return Data
	 */
	public synchronized byte[] readModuleEEPROM(final int count) {
		return Arrays.copyOf(data, count);
	}

	/**
	 * Reads Firmware-Version from dummy-Device
	 * 
	 * @return Firmware-Version
	 */

	public abstract SwVer readSwVer();

	/**
	 * Reads temperature and humidity from TFS-Sensor
	 * 
	 * @return Values
	 */
	public TFSValue readTemp() {
		return null;
	}

	public synchronized void writeActor(final byte actorNr, final byte value) {
		actor[actorNr] = value;
	}

	public synchronized void writeEEPROM(final int memOffset, final byte[] data2, final int dataOffset, final int length) {
		System.arraycopy(data2, dataOffset, data, memOffset, length);
	}
}
