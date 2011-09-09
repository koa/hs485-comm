package ch.eleveneye.hs485.api;

import java.io.IOException;
import java.util.List;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;
import ch.eleveneye.hs485.event.EventHandler;

public interface HS485 {

	public void addBroadcastHandler(final BroadcastHandler handler);

	public abstract void addKeyHandler(final int targetAddress, final byte actorNr, final EventHandler handler) throws IOException;

	public abstract List<Integer> listClients() throws IOException;

	public abstract int[] listOwnAddresse();

	public abstract byte readActor(final int moduleAddress, final byte actor) throws IOException;

	public abstract HwVer readHwVer(final int address) throws IOException;

	public abstract int readLux(final int address) throws IOException;

	public abstract byte[] readModuleEEPROM(final int address, final int count) throws IOException;

	public abstract SwVer readSwVer(final int address) throws IOException;

	public abstract TFSValue readTemp(final int address) throws IOException;

	public abstract void reloadModule(final int address) throws IOException;

	public abstract void resetModule(final int address) throws IOException;

	public abstract void writeActor(final int moduleAddress, final byte actor, final byte action) throws IOException;

	public abstract void writeModuleEEPROM(final int deviceAddress, final int memOffset, final byte[] data, final int dataOffset, final int length)
			throws IOException;

}