package ch.eleveneye.hs485.protocol;

import gnu.io.RXTXCommDriver;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eleveneye.hs485.api.BroadcastHandler;
import ch.eleveneye.hs485.api.HS485;
import ch.eleveneye.hs485.api.data.HwVer;
import ch.eleveneye.hs485.api.data.SwVer;
import ch.eleveneye.hs485.api.data.TFSValue;
import ch.eleveneye.hs485.event.EventHandler;
import ch.eleveneye.hs485.protocol.handler.RawDataHandler;

public class HS485Impl implements HS485 {
	class AckRunnable implements Runnable {
		IMessage	origMessage;

		public AckRunnable(final IMessage origMessage) {
			this.origMessage = origMessage;
		}

		public void run() {
			try {
				sendAck(origMessage);
			} catch (final IOException e) {
				log.warn("Konnte Asynchronen Ack nicht schicken", e);
			}

		}

	}

	class DataHandler implements RawDataHandler {

		public void handleClientList(final List<Integer> clients) {
			synchronized (clientListMutex) {
				clientList = clients;
				lastClientListUpdate = System.currentTimeMillis();
			}

		}

		public void handleLongPacket(final HS485Message packet) {
			if (packet instanceof IMessage) {
				final IMessage iMsg = (IMessage) packet;
				if (iMsg.isSync()) {
					final byte[] data = iMsg.getData();
					if (data.length == 4 && data[0] == 'K') {
						final EventIndex handlerIndex = new EventIndex(packet.getTargetAddress(), data[2]);
						synchronized (keyEventHandlers) {
							final EventHandler handler = keyEventHandlers.get(handlerIndex);
							if (handler == null) {
								if (packet.getTargetAddress() == ownAddress) {
									doAck(iMsg);
									executorService.execute(new Runnable() {
										public void run() {
											try {
												unRegisterEventAt(packet.getSourceAddress(), data[1], data[2]);
											} catch (final IOException e) {
												log.warn("Konnte Event nicht deregistrieren: " + Integer.toHexString(packet.getSourceAddress()) + "," + data[1] + ","
														+ data[2], e);
											}
										}
									});
								} else if (packet.getTargetAddress() == BROADCAST_ADDRESS)
									handleBroadcast(iMsg);

							} else {
								doAck(iMsg);
								final byte eventByte = data[3];
								executorService.execute(new Runnable() {
									public void run() {
										try {
											handler.doEvent(eventByte);
										} catch (final IOException e) {
											log.error("Fehler bei der Eventverarbeitung", e);
										}
									}
								});
							}
						}
					}
				} else if (packet.getTargetAddress() == ownAddress) {
					synchronized (expectedReceiveQueue) {
						LinkedList<IMessage> queue = expectedReceiveQueue.get(iMsg.getSourceAddress());
						if (queue == null) {
							queue = new LinkedList<IMessage>();
							expectedReceiveQueue.put(iMsg.getSourceAddress(), queue);
						}
						queue.addLast(iMsg);
					}
					executorService.execute(new Runnable() {
						public void run() {
							synchronized (expectedReceiveQueue) {
								expectedReceiveQueue.notifyAll();
							}
						}
					});
				} else if (packet.getTargetAddress() == BROADCAST_ADDRESS)
					handleBroadcast(iMsg);

			} else if (packet instanceof ACKMessage && packet.getTargetAddress() == ownAddress) {
				final ACKMessage ackPacket = (ACKMessage) packet; // handle
				// ack-Message
				final AckIndex index = new AckIndex(ackPacket.getSourceAddress(), ackPacket.getReceiveNumber());
				synchronized (receivedAckCount) {
					final Integer countBefore = receivedAckCount.get(index);
					if (countBefore == null)
						receivedAckCount.put(index, 1);
					else
						receivedAckCount.put(index, countBefore + 1);
				}
				executorService.execute(new Runnable() {
					public void run() {
						synchronized (receivedAckCount) {
							receivedAckCount.notifyAll();
						}
					}
				});
			}
			/*
			 * } else if (packet.getTargetAddress() == 0xffffffff) { if (packet
			 * instanceof IMessage) { IMessage iMsg = (IMessage) packet; }
			 */
		}

		private void doAck(final IMessage iMsg) {
			executorService.execute(new AckRunnable(iMsg));
		}
	}

	private static final int												BROADCAST_ADDRESS			= 0xffffffff;

	private static final int												INC_REPEAT_COUNT			= 8;

	private static final Logger											log										= LoggerFactory.getLogger(HS485Impl.class);

	private static final int												PACKET_REPEAT_COUNT		= 4;

	private static final int												PACKET_WAIT_TIME			= 200;

	private final List<BroadcastHandler>						broadcastHandlers			= new ArrayList<BroadcastHandler>();

	private List<Integer>														clientList;

	private final Object														clientListMutex				= new Object();

	private RXTXPort																commPort;

	private int																			currentSenderNumber;

	private PacketDecoder														decoder;

	private PacketEncoder														encoder;

	private ExecutorService													executorService				= Executors.newCachedThreadPool();

	private HashMap<Integer, LinkedList<IMessage>>	expectedReceiveQueue;

	private HashMap<EventIndex, EventHandler>				keyEventHandlers;

	private long																		lastClientListUpdate	= 0;

	private int																			ownAddress;
	private final Object														readClientListMutex		= new Object();

	private HashMap<AckIndex, Integer>							receivedAckCount;

	public HS485Impl(final String port, final int myAddress) throws UnsupportedCommOperationException, IOException {
		init(port, myAddress);
		currentSenderNumber = 0;
	}

	public void addBroadcastHandler(final BroadcastHandler handler) {
		synchronized (broadcastHandlers) {
			broadcastHandlers.add(handler);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#addKeyHandler(int, byte,
	 * ch.eleveneye.hs485.event.EventHandler)
	 */
	public void addKeyHandler(final int targetAddress, final byte actorNr, final EventHandler handler) throws IOException {
		synchronized (keyEventHandlers) {
			final EventIndex eventIndex = new EventIndex(targetAddress, actorNr);
			keyEventHandlers.put(eventIndex, handler);
		}
		// registerEventAt(targetAddress, actorNr, (byte) 0);
	}

	public void handleBroadcast(final IMessage iMsg) {
		executorService.execute(new Runnable() {

			public void run() {
				synchronized (broadcastHandlers) {
					for (final BroadcastHandler handler : broadcastHandlers)
						handler.handleBroadcastMessage(iMsg);

				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#listClients()
	 */
	public List<Integer> listClients() throws IOException {
		synchronized (readClientListMutex) {

			synchronized (clientListMutex) {
				if (lastClientListUpdate + 60 * 1000 > System.currentTimeMillis())
					return clientList;
				else
					clientList = null;
			}
			encoder.initiateDiscovering();
			while (true) {
				synchronized (clientListMutex) {
					if (clientList != null)
						return clientList;
				}
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					synchronized (clientListMutex) {
						return new ArrayList<Integer>(clientList);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#listOwnAddresse()
	 */
	public int[] listOwnAddresse() {
		return new int[] { ownAddress };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readActor(int, byte)
	 */
	public byte readActor(final int moduleAddress, final byte actor) throws IOException {
		final IMessage msg = prepareIMessage(moduleAddress);
		final byte[] data = new byte[] { 'S', actor };
		msg.setData(data);
		final IMessage answer = sendAndWaitForAnswer(msg);

		sendAck(answer);

		return answer.getData()[1];

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readHwVer(int)
	 */
	public HwVer readHwVer(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'h' });
		final IMessage answer = sendAndWaitForAnswer(msg);
		sendAck(answer);

		final byte[] answerData = answer.getData();
		return new HwVer(answerData[0], answerData[1]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readLux(int)
	 */
	public int readLux(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'L' });
		final IMessage answer = sendAndWaitForAnswer(msg);
		sendAck(answer);

		final byte[] answerData = answer.getData();
		return (answerData[0] & 0xff) << 24 | (answerData[1] & 0xff) << 16 | (answerData[2] & 0xff) << 8 | answerData[3] & 0xff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readModuleEEPROM(int, int)
	 */
	public byte[] readModuleEEPROM(final int address, final int count) throws IOException {
		final byte[] ret = new byte[count];
		int partOffset = 0;
		while (partOffset < count) {
			int partSize = count - partOffset;
			if (partSize > 32)
				partSize = 32;
			readEEPROMRaw(address, partOffset, ret, partOffset, partSize);
			partOffset += partSize;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readSwVer(int)
	 */
	public SwVer readSwVer(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'v' });
		final IMessage answer = sendAndWaitForAnswer(msg);
		sendAck(answer);

		final byte[] answerData = answer.getData();
		if (answerData.length < 1)
			return new SwVer((byte) -1, (byte) 0);
		if (answerData.length < 2)
			return new SwVer(answerData[0], (byte) 0);

		return new SwVer(answerData[0], answerData[1]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#readTemp(int)
	 */
	public TFSValue readTemp(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'F' });
		final IMessage answer = sendAndWaitForAnswer(msg);
		sendAck(answer);

		final byte[] answerData = answer.getData();
		final TFSValue value = new TFSValue(answerData);
		if (value.getTemperatur() == 0x8002)
			throw new HardwareError("Sensor " + Integer.toHexString(address) + " defekt");
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#registerEventAt(int, byte,
	 * byte)
	 */
	public void registerEventAt(final int moduleAddress, final byte sensor, final byte actor) throws IOException {
		final IMessage msg = prepareIMessage(moduleAddress);
		final byte[] data = new byte[] { 'q', sensor, actor };
		msg.setData(data);
		sendAndWaitForAck(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#reloadModule(int)
	 */
	public void reloadModule(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'C' });
		sendAndWaitForAck(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#resetModule(int)
	 */
	public void resetModule(final int address) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { '!', '!' });
		sendAndWaitForAck(msg);
	}

	public void setExecutorService(final ExecutorService executorService) {
		this.executorService = executorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#unRegisterEventAt(int,
	 * byte, byte)
	 */
	public void unRegisterEventAt(final int moduleAddress, final byte sensor, final byte actor) throws IOException {
		final IMessage msg = prepareIMessage(moduleAddress);
		final byte[] data = new byte[] { 'c', sensor, actor };
		msg.setData(data);
		sendAndWaitForAck(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#writeActor(int, byte, byte)
	 */
	public void writeActor(final int moduleAddress, final byte actor, final byte action) throws IOException {
		final IMessage msg = prepareIMessage(moduleAddress);
		final byte[] data = new byte[] { 's', 0, actor, action };
		msg.setData(data);
		sendAndWaitForAck(msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.eleveneye.hs485.protocol.HS485Interface#writeModuleEEPROM(int, int,
	 * byte[], int, int)
	 */
	public void writeModuleEEPROM(final int deviceAddress, final int memOffset, final byte[] data, final int dataOffset, final int length)
			throws IOException {
		int partOffset = 0;
		while (partOffset < length) {
			int partSize = length - partOffset;
			if (partSize > 32)
				partSize = 32;
			writeModuleEEPROMRaw(deviceAddress, memOffset + partOffset, data, dataOffset + partOffset, partSize);
			partOffset += partSize;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		commPort.close();
	}

	private void init(final String port, final int myAddress) throws UnsupportedCommOperationException, IOException {
		ownAddress = myAddress;
		receivedAckCount = new HashMap<AckIndex, Integer>();
		expectedReceiveQueue = new HashMap<Integer, LinkedList<IMessage>>();
		keyEventHandlers = new HashMap<EventIndex, EventHandler>();

		final RXTXCommDriver commDriver = new RXTXCommDriver();
		commDriver.initialize();
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			log.info("Interrupted Exception aufgetreten", e);
		}
		commPort = (RXTXPort) commDriver.getCommPort(port, 1);
		if (commPort == null)
			throw new IOException("Auf Gerät " + port + " kann nicht zugegriffen werden");
		commPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
		commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

		decoder = new PacketDecoder(commPort.getInputStream());
		encoder = new PacketEncoder(commPort.getOutputStream(), decoder);
		decoder.addDataHandler(new DataHandler());
	}

	private IMessage prepareIMessage(final int moduleAddress) {
		final IMessage msg = new IMessage();
		msg.setSourceAddress(ownAddress);
		msg.setTargetAddress(moduleAddress);
		msg.setSync(true);
		msg.setSenderNumber((byte) (currentSenderNumber-- & 0x03));
		msg.setReceiveNumber((byte) 0);
		msg.setHasSourceAddr(true);
		return msg;
	}

	private void readEEPROMRaw(final int address, final int offset, final byte[] data, final int dataOffset, final int dataCount) throws IOException {
		final IMessage msg = prepareIMessage(address);
		msg.setData(new byte[] { 'R', (byte) (offset >> 8 & 0xff), (byte) (offset & 0xff), (byte) (dataCount & 0xff) });
		final IMessage answer = sendAndWaitForAnswer(msg);
		sendAck(answer);
		assert answer.getData().length == dataCount;
		System.arraycopy(answer.getData(), 0, data, dataOffset, dataCount);
	}

	private void sendAck(final IMessage answer) throws IOException {
		final ACKMessage ack = new ACKMessage();
		ack.setSourceAddress(ownAddress);
		ack.setTargetAddress(answer.getSourceAddress());
		ack.setReceiveNumber(answer.getSenderNumber());
		ack.setHasSourceAddr(true);
		encoder.sendPacket(ack);
	}

	synchronized private void sendAndWaitForAck(final IMessage msg) throws IOException {
		final byte origSenderNumber = msg.getSenderNumber();
		for (int k = 0; k < INC_REPEAT_COUNT; k++) {
			final byte tempSenderNummer = (byte) (origSenderNumber + k & 0x3);
			msg.setSenderNumber(tempSenderNummer);
			final AckIndex ackIndex = new AckIndex(msg.getTargetAddress(), msg.getSenderNumber());
			synchronized (receivedAckCount) {
				receivedAckCount.put(ackIndex, 0);
			}
			for (int i = 0; i < PACKET_REPEAT_COUNT; i++) {
				encoder.sendPacket(msg);
				// for (int j = 0; j < PACKET_WAIT_TIME; j++) {
				synchronized (receivedAckCount) {
					try {
						receivedAckCount.wait(PACKET_WAIT_TIME);
						// Thread.sleep(1);
					} catch (final InterruptedException e) {
						log.warn("Exception beim Warten auf ACK", e);
					}
					if (receivedAckCount.get(ackIndex) > 0)
						// System.out.println("time: " + j);
						return;
				}
				// }
			}
		}
		throw new TimeoutException("Expected Ack not requested");
	}

	synchronized private IMessage sendAndWaitForAnswer(final IMessage msg) throws IOException {

		final byte origSenderNumber = msg.getSenderNumber();
		for (int k = 0; k < INC_REPEAT_COUNT; k++) {
			final byte tempSenderNummer = (byte) (origSenderNumber + k & 0x3);
			msg.setSenderNumber(tempSenderNummer);

			synchronized (expectedReceiveQueue) {
				expectedReceiveQueue.put(msg.getTargetAddress(), new LinkedList<IMessage>());
			}
			for (int i = 0; i < PACKET_REPEAT_COUNT; i++) {
				encoder.sendPacket(msg);
				// for (int j = 0; j < PACKET_WAIT_TIME; j++) {
				synchronized (expectedReceiveQueue) {
					try {
						expectedReceiveQueue.wait(PACKET_WAIT_TIME);
						// Thread.sleep(1);
					} catch (final InterruptedException e) {
						log.warn("Exception beim Warten auf Antwort", e);
					}
					final LinkedList<IMessage> queue = expectedReceiveQueue.get(msg.getTargetAddress());
					while (queue.size() > 0) {
						final IMessage nextCandidate = queue.removeFirst();
						if (nextCandidate.getReceiveNumber() == msg.getSenderNumber())
							return nextCandidate;
						else {
							// log.warn("Wrong answer: " + nextCandidate);
						}
					}
				}
				// }
			}
		}
		throw new TimeoutException("Expected Answer not requested");
	}

	private void writeModuleEEPROMRaw(final int deviceAddress, final int offset, final byte[] data, final int dataOffset, final int dataCount)
			throws IOException {
		final IMessage msg = prepareIMessage(deviceAddress);
		final byte[] pckData = new byte[4 + dataCount];
		pckData[0] = 'W';
		pckData[1] = (byte) (offset >> 8 & 0xff);
		pckData[2] = (byte) (offset & 0xff);
		pckData[3] = (byte) dataCount;
		System.arraycopy(data, dataOffset, pckData, 4, dataCount);
		msg.setData(pckData);
		sendAndWaitForAck(msg);
	}
}
