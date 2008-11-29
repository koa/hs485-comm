package ch.eleveneye.hs485.protocol.handler;

import ch.eleveneye.hs485.protocol.HS485Message;

public interface AnswerHandler {
	HS485Message notifyDirectMessage(HS485Message query);
}
