package ch.eleveneye.hs485.event;

import java.io.IOException;

public abstract class AbstractEventHandler implements EventHandler {

	boolean	pressFired	 = false;

	boolean	releaseFired	= false;

	boolean	holdFired	   = false;

	byte	  lastCounter	 = 4;

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.berg.hs485.event.EventHandler#doEvent(byte)
	 */
	public void doEvent(byte eventCode) throws IOException {
		byte currentCounter = (byte) ((eventCode >> 2) & 0x3);
		if (currentCounter != lastCounter) {
			pressFired = false;
			releaseFired = false;
			holdFired = false;
			lastCounter = currentCounter;
		}
		byte keyType = (byte) ((eventCode >> 6) & 0x3);
		byte eventType = (byte) (eventCode & 0x3);
		switch (eventType) {
		case EVENT_TYPE_PRESSED:
			doPressKey(keyType);
			break;
		case EVENT_TYPE_HOLDED:
			doHoldKey(keyType);
			break;
		case EVENT_TYPE_RELEASED:
			doReleaseKey(keyType);
			break;
		}
	}

	protected void doPressKey(byte keyType) throws IOException {
	}

	protected void doHoldKey(byte keyType) throws IOException {
	}

	protected void doReleaseKey(byte keyType) throws IOException {
	}
}
