package ch.eleveneye.hs485.protocol.handler;

import java.util.List;

public interface ClientListEventHandler {
	public void notifyClientList(List<Integer> clients);
}
