package ch.eleveneye.hs485.protocol.handler;

import java.util.List;

import ch.eleveneye.hs485.protocol.HS485Message;

public interface RawDataHandler {
	public void handleLongPacket(HS485Message packet);

	public void handleClientList(List<Integer> clientList);
}
