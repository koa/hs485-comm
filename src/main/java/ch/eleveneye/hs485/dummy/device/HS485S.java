package ch.eleveneye.hs485.dummy.device;

import ch.eleveneye.hs485.api.MessageHandler;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;

public class HS485S extends Device {

	public HS485S(final int address, final MessageHandler handler) {
		super(2, address, handler);
	}

	@Override
	public HwVer readHwVer() {
		return new HwVer((byte) 1, (byte) 1);
	}

	@Override
	public SwVer readSwVer() {
		return new SwVer((byte) 2, (byte) 0);
	}

}
