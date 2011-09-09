package ch.eleveneye.hs485.api;

import ch.eleveneye.hs485.protocol.IMessage;

public interface BroadcastHandler {
	public void handleBroadcastMessage(IMessage message);
}
