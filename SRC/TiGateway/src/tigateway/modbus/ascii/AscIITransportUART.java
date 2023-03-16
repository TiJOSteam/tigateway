package tigateway.modbus.ascii;


import tigateway.modbus.Modbus;
import tigateway.modbus.protocol.ModbusLRC;
import tigateway.modbus.transport.IMBTransport;
import tigateway.serialport.TiSerialPort;
import tijos.framework.util.Formatter;
import tijos.framework.util.logging.Logger;

/**
 * MODBUS RTU Transport
 */
public class AscIITransportUART implements IMBTransport {

	TiSerialPort serialPort;

	private static final byte START = ':';
	private static final byte[] END = { '\r', '\n' };

	protected int timeout;
	protected int expectedBytes; // for logging

	/**
	 * Initialize with UART and timeout
	 *
	 * @param uart
	 * @param timeout timeout for receiving data from UART
	 */
	public AscIITransportUART(TiSerialPort serialPort, int timeout) {
		this.serialPort = serialPort;

		this.timeout = timeout;
	}
	
	/**
	 * Set read timeout for UART
	 * @param timeout
	 */
	@Override
	public void setCommTimout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * Send MODBUS request
	 */
	@Override
	public void sendRequest(Modbus modbusClient) throws Exception {

		//clear buffer before new request 
		this.serialPort.clearInput();

		byte[] buffer = new byte[modbusClient.getPduSize() + 16]; // ADU: [ID(1), PDU(n), LRC(2)]

		buffer[0] = modbusClient.getDeviceId();
		modbusClient.readFromPdu(0, modbusClient.getPduSize(), buffer, 1);
		int size = modbusClient.getPduSize() + 1; // including 1 byte for serverId
		byte lrc = (byte) ModbusLRC.calculateLRC(buffer, 0, size);

		Logger.info("Modbus", "Write: " + Formatter.toHexString(buffer, 0, size, ""));

		this.serialPort.write(new byte[] { START }, 0, 1);
		byte[] data = Formatter.toHexString(buffer, 0, size, "").toUpperCase().getBytes();
		this.serialPort.write(data);

		byte[] blrc = Formatter.toHexString(lrc).toUpperCase().getBytes();

		Logger.info("Modbus", "Write lrc: " + Formatter.toHexString(blrc, 0, blrc.length, ""));

		this.serialPort.write(blrc);

		this.serialPort.write(END, 0, END.length);
	}

	private boolean readAscBytes(byte[] buffer, int start, int length, int timeout) throws Exception {

		byte[] data = new byte[length * 2];

		if (!this.serialPort.readToBuffer(data, 0, data.length, timeout)) {
			return false;
		}

		String pdu = new String(data);
		byte[] temp = Formatter.hexStringToByte(pdu);

		System.arraycopy(temp, 0, buffer, start, length);
		return true;
	}

	/**
	 * Waiting for response
	 */
	@Override
	public int waitResponse(Modbus modbusClient) throws Exception {

		expectedBytes = modbusClient.getExpectedPduSize() + 2; // id(1), PDU(n), lrc(1)

		byte[] buffer = new byte[expectedBytes + 16];

		// start
		if (!this.serialPort.readToBuffer(buffer, 0, 1, this.timeout))
			return ModbusASCII.RESULT_TIMEOUT;

		if (buffer[0] != START)
			return ModbusASCII.RESULT_BAD_RESPONSE;

		// read id
		if (!readAscBytes(buffer, 0, 1, this.timeout))
			return ModbusASCII.RESULT_TIMEOUT;

		if (buffer[0] != modbusClient.getDeviceId()) {
			logData("bad id", buffer, 0, 1);
			Logger.warning("Modbus",
					"waitResponse(): Invalid id: " + buffer[0] + "expected:" + modbusClient.getDeviceId());
			return ModbusASCII.RESULT_BAD_RESPONSE;
		}

		// read function (bit7 means exception)
		if (!readAscBytes(buffer, 1, 1, this.timeout))
			return ModbusASCII.RESULT_TIMEOUT;

		if ((buffer[1] & 0x7f) != modbusClient.getFunction()) {
			logData("bad function", buffer, 0, 2);
			Logger.warning("Modbus",
					"waitResponse(): Invalid function: " + buffer[1] + "expected: " + modbusClient.getFunction());
			return ModbusASCII.RESULT_BAD_RESPONSE;
		}

		if ((buffer[1] & 0x80) != 0) {
			// EXCEPTION
			expectedBytes = 4; // id(1), function(1), exception code(1), lrc(1)
			if (!readAscBytes(buffer, 2, 2, this.timeout)) // exception code + LRC
				return ModbusASCII.RESULT_TIMEOUT;

			if (lrcValid(buffer, 3)) {
				logData("exception", buffer, 0, expectedBytes);
				modbusClient.setPduSize(2); // function + exception code
				modbusClient.writeToPdu(buffer, 1, modbusClient.getPduSize(), 0);
				return ModbusASCII.RESULT_EXCEPTION;
			} else {
				logData("bad crc (exception)", buffer, 0, expectedBytes);
				return ModbusASCII.RESULT_BAD_RESPONSE;
			}
		} else {
			// NORMAL RESPONSE
			// data + LRC (without function) pdu lengh -1 + 1
			if (!readAscBytes(buffer, 2, modbusClient.getExpectedPduSize(), this.timeout))
				return ModbusASCII.RESULT_TIMEOUT;

			// end
			byte[] end = new byte[2];
			if (!this.serialPort.readToBuffer(end, 0, end.length, timeout)) {
				return ModbusASCII.RESULT_TIMEOUT;
			}
			// LRC check of (serverId + PDU)
			if (lrcValid(buffer, 1 + modbusClient.getExpectedPduSize())) {
				logData("normal", buffer, 0, expectedBytes);
				modbusClient.setPduSize(modbusClient.getExpectedPduSize());
				modbusClient.writeToPdu(buffer, 1, modbusClient.getPduSize(), 0);
				return ModbusASCII.RESULT_OK;
			} else {
				logData("bad lrc", buffer, 0, expectedBytes);
				return ModbusASCII.RESULT_BAD_RESPONSE;
			}
		}

	}

	protected void logData(String kind, byte[] buffer, int start, int length) {
		Logger.info("Modbus", kind + ": " + Formatter.toHexString(buffer, start, length, ""));
	}

	/**
	 * CRC validation
	 *
	 * @param size
	 * @return
	 */
	protected boolean lrcValid(byte[] buffer, int size) {
		int lrc = ModbusLRC.calculateLRC(buffer, 0, size);
		int lrc2 = buffer[size] & 0xff;
		if (lrc == lrc2)
			return true;
		else {
			Logger.warning("Modbus",
					"LRC error calc:" + Integer.toHexString(lrc) + " in response: " + Integer.toHexString(lrc2));
			return false;
		}
	}

}
