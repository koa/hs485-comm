package ch.eleveneye.hs485.event;

import java.io.IOException;

public interface EventHandler {

	public static final byte	KEY_TYPE_TOGGLE	    = 0x0;

	public static final byte	KEY_TYPE_UP	        = 0x1;

	public static final byte	KEY_TYPE_DOWN	      = 0x2;

	public static final byte	KEY_TYPE_RESERVE	  = 0x3;

	public static final byte	EVENT_TYPE_PRESSED	= 0x0;

	public static final byte	EVENT_TYPE_HOLDED	  = 0x1;

	public static final byte	EVENT_TYPE_RELEASED	= 0x2;

	public static final byte	EVENT_TYPE_RESERVED	= 0x3;

	public abstract void doEvent(byte eventCode) throws IOException;

}