package ch.eleveneye.hs485.api;

import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;

import ch.eleveneye.hs485.dummy.HS485Dummy;
import ch.eleveneye.hs485.protocol.HS485Impl;

/**
 * @author akoenig A Factory for access to HS485 PCI
 */
public class HS485Factory {
	private static HS485Factory	instance;
	private static final String	SERIAL_TTY	= "/dev/ttyUSB0";

	public static synchronized HS485Factory getInstance() throws UnsupportedCommOperationException, IOException {
		if (instance == null)
			instance = new HS485Factory();
		return instance;
	}

	private HS485	impl;

	private HS485Factory() throws UnsupportedCommOperationException, IOException {
		if (new File(SERIAL_TTY).exists())
			// Serial port found, try real impl
			impl = new HS485Impl(SERIAL_TTY, 3);
		else
			// no Serial interface, take dummy for testing purposes
			impl = new HS485Dummy();
	}

	public HS485 getHS485() {
		return impl;
	}
}
