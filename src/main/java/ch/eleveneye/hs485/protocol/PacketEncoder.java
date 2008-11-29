package ch.eleveneye.hs485.protocol;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class PacketEncoder {
	public static final Logger	log	= Logger.getLogger(PacketEncoder.class);

	private CRC16	             crc;

	PacketDecoder	             decoder;

	OutputStream	             outStream;

	public PacketEncoder(final OutputStream outStream, final PacketDecoder decoder) {
		this.outStream = outStream;
		this.decoder = decoder;
	}

	public void initiateDiscovering() throws IOException {
		sendRawPacket(false, new byte[] { 0x04, 0x00 });
	}

	public void sendPacket(final HS485Message msg) throws IOException {

		byte controlChar = 0;
		int lengthPos = 5;
		if (msg.hasSourceAddr()) {
			lengthPos = 9;
			controlChar |= 0x08;
		}
		byte[] rawData;
		if (msg instanceof IMessage) {
			final IMessage iMsg = (IMessage) msg;
			if (iMsg.isSync()) {
				controlChar |= 0x80;
			}
			controlChar |= (iMsg.getReceiveNumber() & 0x03) << 5;
			controlChar |= (iMsg.getSenderNumber() & 0x03) << 1;
			if (iMsg.isLastPacket()) {
				controlChar |= 0x10;
			}
			final byte[] data = iMsg.getData();
			rawData = new byte[lengthPos + 1 + data.length];
			System.arraycopy(data, 0, rawData, lengthPos + 1, data.length);
			rawData[lengthPos] = (byte) (data.length + 2);
		} else if (msg instanceof ACKMessage) {
			controlChar |= 0x11;
			final ACKMessage ackMsg = (ACKMessage) msg;
			controlChar |= (ackMsg.getReceiveNumber() & 0x03) << 5;
			rawData = new byte[lengthPos + 1];
			rawData[lengthPos] = 2;
		} else {
			return;
		}
		final int targetAddress = msg.getTargetAddress();
		rawData[0] = (byte) ((targetAddress >> 24) & 0xff);
		rawData[1] = (byte) ((targetAddress >> 16) & 0xff);
		rawData[2] = (byte) ((targetAddress >> 8) & 0xff);
		rawData[3] = (byte) (targetAddress & 0xff);
		rawData[4] = controlChar;
		if (msg.hasSourceAddr) {
			final int senderAddress = msg.getSourceAddress();
			rawData[5] = (byte) ((senderAddress >> 24) & 0xff);
			rawData[6] = (byte) ((senderAddress >> 16) & 0xff);
			rawData[7] = (byte) ((senderAddress >> 8) & 0xff);
			rawData[8] = (byte) (senderAddress & 0xff);
		}
		sendRawPacket(true, rawData);
		log.trace(msg);
	}

	synchronized public void sendRawPacket(final boolean longPacket,
	    final byte[] rawPacketData) throws IOException {
		final int minWaitTime = (int) (Math.random() * 50);
		long sleepTime = decoder.getLastPacketReceived()
		    - System.currentTimeMillis() - minWaitTime;
		while (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				break;
			}
			sleepTime = System.currentTimeMillis() - decoder.getLastPacketReceived()
			    - minWaitTime;
		}
		final byte startChar = (byte) (longPacket ? 0xfd : 0xfe);
		// Calculate Checksum
		crc = new CRC16();
		crc.reset();
		crc.shift(startChar);
		for (final byte element : rawPacketData) {
			crc.shift(element);
		}
		crc.shift(0);
		crc.shift(0);
		final int checkSum = crc.getCurrentCRC();

		while (!decoder.isBusFree()) {
			try {
				Thread.sleep(3);
			} catch (final InterruptedException e) {
			}
		}
		// send Data
		outStream.write(startChar);
		for (final byte sendb : rawPacketData) {
			switch (sendb) {
			case (byte) 0xfc:
			case (byte) 0xfd:
			case (byte) 0xfe:
				outStream.write(0xfc);
				outStream.write(sendb & 0x7f);
				break;
			default:
				outStream.write(sendb);
				break;
			}
		}
		outStream.write((checkSum >> 8) & 0xff);
		outStream.write(checkSum & 0xff);
		outStream.flush();
	}
}
