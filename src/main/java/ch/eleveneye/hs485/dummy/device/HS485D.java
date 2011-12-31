package ch.eleveneye.hs485.dummy.device;

import ch.eleveneye.hs485.api.MessageHandler;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;

public class HS485D extends Device {
	public HS485D(final int address, final MessageHandler handler) {
		super(1, address, handler);
	}

	@Override
	public HwVer readHwVer() {
		return new HwVer((byte) 0, (byte) 1);
	}

	@Override
	public SwVer readSwVer() {
		return new SwVer((byte) 2, (byte) 0);
	}
}
