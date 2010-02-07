package ch.eleveneye.hs485.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.eleveneye.hs485.protocol.handler.RawDataHandler;

public class PacketDecoder {
	class ReaderThread extends Thread {

		private boolean running = true;

		synchronized boolean isRunning() {
			return running;
		}

		@Override
		public void run() {

			boolean lastWasFC = false;

			int readPtr = -1;

			final byte[] buffer = new byte[64];
			int packetLength = 100;
			int lengthPos = 10;
			boolean shortPacket = false;
			final CRC16 crc = new CRC16();
			while (isRunning()) {
				try {
					final int nextChar = inStream.read();
					// System.out.print(Integer.toHexString(nextChar) + ", ");
					switch (nextChar) {
					case -1: // EOF ,vielleicht Port geschlossen
						setRunning(false);
						break;
					case 0xfe:
						setReceivingPacket(true);
						readPtr = 0;
						packetLength = 100;
						lengthPos = 4;
						shortPacket = true;
						lastWasFC = false;
						crc.reset();
						crc.shift(nextChar);
						break;
					case 0xfd: // Paket-Start
						// System.out.println(System.currentTimeMillis()
						// - getLastPacketReceived());
						setReceivingPacket(true);
						readPtr = 0;
						packetLength = 100; // muss grösser als 5 sein
						lengthPos = 10;
						lastWasFC = false;
						shortPacket = false;
						crc.reset();
						crc.shift(nextChar);
						break;
					case 0xfc: // Escape-Zeichen
						lastWasFC = true;
						break;
					default: // normales Zeichen
						if (readPtr >= buffer.length) { // Pufferüberlauf
							readPtr = -1;
						} else if (readPtr >= 0) { // Packet-Start wurde
							// empfangen
							byte currentB = (byte) nextChar;
							if (lastWasFC) {
								currentB |= 0x80;
							}
							buffer[readPtr++] = currentB;
							crc.shift(currentB);
							if (readPtr == 5 && !shortPacket) { // Kontrollzeichen
								if (controllIsI(currentB)
										|| controllIsACK(currentB)) {
									// I oder ACK-Nachricht
									if (controlHasSourceAddr(currentB)) {
										lengthPos = 10;
										// Absender-Adresse
									} else {
										lengthPos = 6; // keine
										// Absender-Adresse
									}
								} else { // Discovery-Nachricht
									packetLength = 7;
									lengthPos = 10;
								}
							}
							if (readPtr == lengthPos) {
								packetLength = currentB + readPtr;
							}
							if (readPtr == packetLength) {
								// System.out.println();
								// war letztes Zeichen -> Packet dekodieren
								crc.shift(0);
								crc.shift(0);
								if (crc.checkCRC()) {
									handlePacket(buffer, readPtr, lengthPos,
											shortPacket);
								} else {
									log.warn("CRC-Error");
								}
								readPtr = -1;
								setLastPacketReceived(System
										.currentTimeMillis());
								setReceivingPacket(false);
							}
						} else {
							// Byte ausserhalb eines Packetes
							// log.info("Byte ausserhalb eines Packetes
							// empfangen: " + Integer.toHexString(nextChar));
						}
						lastWasFC = false;
						break;
					}
				} catch (final IOException e) {
					log.warn("IO-Fehler beim dekodieren der empfangenen Daten",
							e);
				}
			}
		}

		synchronized void setRunning(final boolean running) {
			this.running = running;
		}

	}

	static protected Logger log = Logger.getLogger(PacketDecoder.class);

	static private boolean controlHasSourceAddr(final byte currentB) {
		return (currentB & 0x08) == 8;
	}

	static private boolean controllIsACK(final byte currentB) {
		return (currentB & 0x07) == 0x01;
	}

	static private boolean controllIsI(final byte currentB) {
		return (currentB & 0x01) == 0x00;
	}

	List<Integer> clientList;

	List<RawDataHandler> clientListListeners;

	InputStream inStream;

	long lastPacketReceived = 0;

	ReaderThread myThread;

	HashMap<Integer, Byte> receivedSenderNumber;

	boolean receivingPacket;

	public PacketDecoder(final InputStream stream) {
		inStream = stream;
		receivedSenderNumber = new HashMap<Integer, Byte>();
		clientListListeners = new ArrayList<RawDataHandler>();

		myThread = new ReaderThread();
		myThread.setName("HS485 Reader Thread");
		myThread.setDaemon(true);
		myThread.start();
		try {
			Thread.sleep(10);
		} catch (final InterruptedException e) {
		}

	}

	public void addDataHandler(final RawDataHandler handler) {
		synchronized (clientListListeners) {
			clientListListeners.add(handler);
		}
	}

	public synchronized long getLastPacketReceived() {
		return lastPacketReceived;
	}

	void handlePacket(final byte[] buffer, final int readPtr,
			final int lengthPos, final boolean shortPacket) {

		if (shortPacket) {
			switch (buffer[4]) {
			case (byte) 0x80:
				if (clientList == null) {
					clientList = new LinkedList<Integer>();
				}
				final int clientAddr = ((buffer[5] & 0xff) << 24)
						| ((buffer[6] & 0xff) << 16)
						| ((buffer[7] & 0xff) << 8) | (buffer[8] & 0xff);
				log.trace("Client-Resultat empfangen: "
						+ Integer.toHexString(clientAddr));
				if (clientAddr == 0xffffffff) {
					synchronized (clientListListeners) {
						for (final RawDataHandler listener : clientListListeners) {
							listener.handleClientList(clientList);
						}
					}
					clientList = null;
				} else {
					clientList.add(clientAddr);
				}
				break;
			default:
				break;
			}
		} else {

			final int targetAddr = ((buffer[0] & 0xff) << 24)
					| ((buffer[1] & 0xff) << 16) | ((buffer[2] & 0xff) << 8)
					| (buffer[3] & 0xff);
			int sourceAddr = -1;
			final byte controllChar = buffer[4];
			final boolean hasSenderAddress = (controllIsACK(controllChar) || controllIsI(controllChar))
					&& controlHasSourceAddr(controllChar);
			if (hasSenderAddress) {
				sourceAddr = (((buffer[5] & 0xff) << 24)
						| ((buffer[6] & 0xff) << 16)
						| ((buffer[7] & 0xff) << 8) | (buffer[8] & 0xff));
			}
			HS485Message packet;
			final StringBuffer descString = new StringBuffer();
			if (controllIsI(controllChar)) {
				final IMessage iMessage = new IMessage();
				iMessage.setSync((controllChar & 0x80) != 0);
				iMessage.setReceiveNumber((byte) ((controllChar & 0x60) >> 5));
				iMessage.setLastPacket((controllChar & 0x10) != 0);
				iMessage.setSenderNumber((byte) ((controllChar & 0x06) >> 1));
				final byte[] data = new byte[readPtr - lengthPos - 2];
				for (int i = 0; i < data.length; i++) {
					data[i] = buffer[lengthPos + i];
				}
				iMessage.setData(data);

				iMessage.setDuplicatePacket(false);
				if (hasSenderAddress && !iMessage.isSync()
						&& receivedSenderNumber.containsKey(sourceAddr)) {
					iMessage
							.setDuplicatePacket(iMessage.getSenderNumber() == receivedSenderNumber
									.get(sourceAddr).byteValue());
				}
				receivedSenderNumber
						.put(sourceAddr, iMessage.getSenderNumber());

				receivedSenderNumber.remove(targetAddr);

				packet = iMessage;

				descString.append("I ");
				if ((controllChar & 0x80) != 0) {
					descString.append("Sync ");
				}
				descString.append("R:");
				descString.append(Integer.toString((controllChar & 0x60) >> 5));
				descString.append(" ");
				if ((controllChar & 0x10) != 0) {
					descString.append("F ");
				}
				descString.append("S:");
				descString.append(Integer.toString((controllChar & 0x06) >> 1));
				descString.append(" ");
			} else if (controllIsACK(controllChar)) {
				final ACKMessage ackMessage = new ACKMessage();
				ackMessage
						.setReceiveNumber((byte) ((controllChar & 0x60) >> 5));
				packet = ackMessage;
				descString.append("Ack ");
				descString.append("R:");
				descString.append(Integer.toString((controllChar & 0x60) >> 5));
				descString.append(" ");
			} else {
				final DiscoveryMessage discoveryMessage = new DiscoveryMessage();
				discoveryMessage
						.setMaskCount((byte) ((controllChar & 0xf8) >> 3));
				packet = discoveryMessage;
				descString.append("Discovery ");
				descString.append("M:");
				descString.append(Integer.toString((controllChar & 0xf8) >> 3));
				descString.append(" ");
			}
			packet.setHasSourceAddr(hasSenderAddress);
			packet.setTargetAddress(targetAddr);
			packet.setSourceAddress(sourceAddr);
			descString.append(":: ");
			for (int i = lengthPos; i < readPtr - 2; i += 1) {
				descString.append(Integer.toHexString(buffer[i] & 0xff));
				descString.append(" ");
			}
			/*
			 * System.out.println(Integer.toHexString(sourceAddr) + " -> " +
			 * Integer.toHexString(targetAddr) + ": " + descString);
			 */
			log.trace("Received: " + packet);
			synchronized (clientListListeners) {
				for (final RawDataHandler listener : clientListListeners) {
					listener.handleLongPacket(packet);
				}
			}
		}
	}

	public boolean isBusFree() {
		final int randomDelay = (int) Math.random() * 30 + 3;
		return !isReceivingPacket()
				&& getLastPacketReceived() + randomDelay < System
						.currentTimeMillis();
	}

	public synchronized boolean isReceivingPacket() {
		return receivingPacket;
	}

	public synchronized void setLastPacketReceived(final long lastPacketReceived) {
		this.lastPacketReceived = lastPacketReceived;
	}

	public synchronized void setReceivingPacket(final boolean receivingPacket) {
		this.receivingPacket = receivingPacket;
	}
}
