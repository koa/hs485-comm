package ch.eleveneye.hs485.dummy.device;

import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;

public class TFS extends Device {

	public TFS(final int address) {
		super(0, address, null);
	}

	@Override
	public HwVer readHwVer() {
		return new HwVer((byte) 4, (byte) 0);
	}

	@Override
	public SwVer readSwVer() {
		return new SwVer((byte) 1, (byte) 3);
	}

	@Override
	public TFSValue readTemp() {
		final TFSValue tfsValue = new TFSValue();
		tfsValue.setTemperatur(249);
		tfsValue.setHumidity(45);
		return tfsValue;
	}

}
