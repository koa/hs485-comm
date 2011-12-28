package ch.eleveneye.hs485.dummy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ch.eleveneye.hs485.api.BroadcastHandler;
import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;
import ch.eleveneye.hs485.dummy.device.Device;
import ch.eleveneye.hs485.dummy.device.HS485D;
import ch.eleveneye.hs485.dummy.device.HS485S;
import ch.eleveneye.hs485.dummy.device.TFS;
import ch.eleveneye.hs485.event.EventHandler;
import ch.eleveneye.hs485.protocol.IMessage;

public class HS485Dummy implements HS485, BroadcastHandler {
	ScheduledExecutorService							executorService		= Executors.newScheduledThreadPool(3);
	private final List<BroadcastHandler>	broadcasHandlers	= new ArrayList<BroadcastHandler>();
	private final Map<Integer, Device>		devices						= new HashMap<Integer, Device>();

	public HS485Dummy() {
		addHS485S(0x440);
		addHS485S(0x441);
		addHS485S(0x442);
		addHS485S(0x443);
		addHS485D(0x450);
		addHS485D(0x451);
		addHS485D(0x452);
		addHS485D(0x453);
		addTFS(0x500);
		addTFS(0x501);
		addTFS(0x502);
	}

	public void addBroadcastHandler(final BroadcastHandler handler) {
		broadcasHandlers.add(handler);
	}

	public void addKeyHandler(final int targetAddress, final byte actorNr, final EventHandler handler) throws IOException {
		// TODO Auto-generated method stub

	}

	public void handleBroadcastMessage(final IMessage message) {
		for (final BroadcastHandler handler : broadcasHandlers)
			handler.handleBroadcastMessage(message);
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

	private void addHS485D(final int address) {
		final HS485D device = new HS485D(address, this);
		device.setExecutorService(executorService);
		devices.put(address, device);
	}

	private void addHS485S(final int address) {
		final HS485S device = new HS485S(address, this);
		device.setExecutorService(executorService);
		devices.put(address, device);
	}

	private void addTFS(final int address) {
		devices.put(address, new TFS(address));

	}

}
