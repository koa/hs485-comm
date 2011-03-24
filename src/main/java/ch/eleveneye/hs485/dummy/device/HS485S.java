package ch.eleveneye.hs485.dummy.device;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;

public class HS485S extends Device {

	public HS485S() {
		super(2);
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
