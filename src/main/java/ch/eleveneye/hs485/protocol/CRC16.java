package ch.eleveneye.hs485.protocol;

public class CRC16 {
	protected static final int	CRC16_POLYGON	= 0x1002; // Konstante zur

	// Berechnung

	protected int	             currentCRC	    = 0;

	public void shift(int w) {
		byte q;
		boolean flag;

		w &= 0xff;
		for (q = 0; q < 8; q++) {
			flag = ((currentCRC & 0x8000) != 0);
			currentCRC <<= 1;
			if ((w & 0x80) != 0) {
				currentCRC |= 1;
			}

			w <<= 1;
			if (flag) {
				currentCRC ^= CRC16_POLYGON;
			}
		}
	}

	public void reset() {
		currentCRC = 0xFFFF;
	}

	public int getCurrentCRC() {
		return currentCRC;
	}

	public boolean checkCRC() {
		return currentCRC == 0;
	}
}
