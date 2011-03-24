package ch.eleveneye.hs485.dummy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;
import ch.eleveneye.hs485.dummy.device.Device;
import ch.eleveneye.hs485.dummy.device.HS485D;
import ch.eleveneye.hs485.dummy.device.HS485S;
import ch.eleveneye.hs485.event.EventHandler;

public class HS485Dummy implements HS485 {
	private final Map<Integer, Device>	devices	= new HashMap<Integer, Device>();

	public HS485Dummy() {
		devices.put(0x440, new HS485S());
		devices.put(0x441, new HS485S());
		devices.put(0x442, new HS485S());
		devices.put(0x443, new HS485S());
		devices.put(0x450, new HS485D());
		devices.put(0x451, new HS485D());
		devices.put(0x452, new HS485D());
		devices.put(0x453, new HS485D());
	}

	public void addKeyHandler(final int targetAddress, final byte actorNr, final EventHandler handler) throws IOException {
		// TODO Auto-generated method stub

	}

	public List<Integer> listClients() throws IOException {
		return new ArrayList<Integer>(devices.keySet());
	}

	public int[] listOwnAddresse() {
		return new int[] { 3 };
	}

	public byte readActor(final int moduleAddress, final byte actor) throws IOException {
		return devices.get(moduleAddress).readActor(actor);
	}

	public HwVer readHwVer(final int address) throws IOException {
		return devices.get(address).readHwVer();
	}

	public int readLux(final int address) throws IOException {
		return devices.get(address).readLux();
	}

	public byte[] readModuleEEPROM(final int address, final int count) throws IOException {
		return devices.get(address).readModuleEEPROM(count);
	}

	public SwVer readSwVer(final int address) throws IOException {
		return devices.get(address).readSwVer();
	}

	public TFSValue readTemp(final int address) throws IOException {
		return devices.get(address).readTemp();
	}

	public void reloadModule(final int address) throws IOException {
		// Not needed for emulate Module
	}

	public void resetModule(final int address) throws IOException {
		// Not needed for emulate Module
	}

	public void writeActor(final int moduleAddress, final byte actor, final byte action) throws IOException {
		devices.get(moduleAddress).writeActor(actor, action);
	}

	public void writeModuleEEPROM(final int deviceAddress, final int memOffset, final byte[] data, final int dataOffset, final int length)
			throws IOException {
		devices.get(deviceAddress).writeEEPROM(memOffset, data, dataOffset, length);

	}

}
