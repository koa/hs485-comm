package ch.eleveneye.hs485.api;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.KeyMessage;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;

public interface HS485 extends Closeable {

	void addBroadcastHandler(final MessageHandler handler);

	void addKeyHandler(final int moduleAddress, final byte sensorNr, final MessageHandler handler) throws IOException;

	List<Integer> listClients() throws IOException;

	int[] listOwnAddresse();

	byte readActor(final int moduleAddress, final byte actor) throws IOException;

	HwVer readHwVer(final int address) throws IOException;

	int readLux(final int address) throws IOException;

	byte[] readModuleEEPROM(final int address, final int count) throws IOException;

	SwVer readSwVer(final int address) throws IOException;

	TFSValue readTemp(final int address) throws IOException;

	void reloadModule(final int address) throws IOException;

	void removeBroadcastHandler(MessageHandler broadcastHandler);

	void removeHandlers();

	void resetModule(final int address) throws IOException;

	void sendKeyMessage(KeyMessage keyMessage) throws IOException;

	void writeActor(final int moduleAddress, final byte actor, final byte action) throws IOException;

	void writeModuleEEPROM(final int deviceAddress, final int memOffset, final byte[] data, final int dataOffset, final int length) throws IOException;

}