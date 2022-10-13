package tigateway.modbus.rtu;

import tigateway.modbus.Modbus;
import tigateway.modbus.protocol.ModbusCrc16;
import tigateway.modbus.protocol.ModbusPdu;
import tigateway.modbus.transport.IMBTransport;
import tigateway.serialport.TiSerialPort;
import tijos.framework.util.logging.Logger;

/**
 * MODBUS RTU Transport
 */
public class RtuTransportUART implements IMBTransport {

	TiSerialPort serialPort;

	protected int timeout;

	/**
	 * Initialize with UART and timeout
	 *
	 * @param uart
	 * @param timeout timeout for receiving data from UART
	 */
	public RtuTransportUART(TiSerialPort serialPort, int timeout) {
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

		byte[] buffer = new byte[modbusClient.getPduSize() + 16]; // ADU: [ID(1), PDU(n), CRC(2)]

		buffer[0] = modbusClient.getDeviceId();
		modbusClient.readFromPdu(0, modbusClient.getPduSize(), buffer, 1);
		int size = modbusClient.getPduSize() + 1; // including 1 byte for serverId
		int crc = ModbusCrc16.calcCRC16(buffer, 0, size);
		buffer[size] = ModbusPdu.lowByte(crc);
		buffer[size + 1] = ModbusPdu.highByte(crc);
		size = size + 2;

		Logger.info("Modbus", "Write: " + ModbusPdu.toHex(buffer, 0, size));

		this.serialPort.write(buffer, 0, size);
	}

	/**
	 * Waiting for response
	 */
	@Override
	public int waitResponse(Modbus modbusClient) throws Exception {

		int expectedBytes = modbusClient.getExpectedPduSize() + 3; // id(1), PDU(n), crc(2)

		byte[] buffer = new byte[expectedBytes + 16];

		// read id
		if (!this.serialPort.readToBuffer(buffer, 0, 1, this.timeout))
			return ModbusRTU.RESULT_TIMEOUT;

		if (buffer[0] != modbusClient.getDeviceId()) {
			logData("bad id", buffer, 0, 1);
			Logger.warning("Modbus",
					"waitResponse(): Invalid id: " + buffer[0] + "expected:" + modbusClient.getDeviceId());
			return ModbusRTU.RESULT_BAD_RESPONSE;
		}

		// read function (bit7 means exception)
		if (!this.serialPort.readToBuffer(buffer, 1, 1, this.timeout))
			return ModbusRTU.RESULT_TIMEOUT;

		if ((buffer[1] & 0x7f) != modbusClient.getFunction()) {
			logData("bad function", buffer, 0, 2);
			Logger.warning("Modbus",
					"waitResponse(): Invalid function: " + buffer[1] + "expected: " + modbusClient.getFunction());
			return ModbusRTU.RESULT_BAD_RESPONSE;
		}

		if ((buffer[1] & 0x80) != 0) {
			// EXCEPTION
			expectedBytes = 5; // id(1), function(1), exception code(1), crc(2)
			if (!this.serialPort.readToBuffer(buffer, 2, 3, this.timeout)) // exception code + CRC
				return ModbusRTU.RESULT_TIMEOUT;

			if (crcValid(buffer, 3)) {
				logData("exception", buffer, 0, expectedBytes);
				modbusClient.setPduSize(2); // function + exception code
				modbusClient.writeToPdu(buffer, 1, modbusClient.getPduSize(), 0);
				return ModbusRTU.RESULT_EXCEPTION;
			} else {
				logData("bad crc (exception)", buffer, 0, expectedBytes);
				return ModbusRTU.RESULT_BAD_RESPONSE;
			}
		} else {
			// NORMAL RESPONSE pdu length - 1 + 2
			if (!this.serialPort.readToBuffer(buffer, 2, modbusClient.getExpectedPduSize() + 1, this.timeout)) // data +
																												// CRC
																												// (without
																												// function)
				return ModbusRTU.RESULT_TIMEOUT;

			// CRC check of (serverId + PDU)
			if (crcValid(buffer, 1 + modbusClient.getExpectedPduSize())) {
				logData("normal", buffer, 0, expectedBytes);
				modbusClient.setPduSize(modbusClient.getExpectedPduSize());
				modbusClient.writeToPdu(buffer, 1, modbusClient.getPduSize(), 0);
				return ModbusRTU.RESULT_OK;
			} else {
				logData("bad crc", buffer, 0, expectedBytes);
				return ModbusRTU.RESULT_BAD_RESPONSE;
			}
		}
	}

	protected void logData(String kind, byte[] buffer, int start, int length) {
		Logger.info("Modbus", "Read: " + ModbusPdu.toHex(buffer, start, length));
	}

	/**
	 * CRC validation
	 *
	 * @param size
	 * @return
	 */
	protected boolean crcValid(byte[] buffer, int size) {
		int crc = ModbusCrc16.calcCRC16(buffer, 0, size);
		int crc2 = ModbusPdu.bytesToInt16(buffer[size], buffer[size + 1], true);
		if (crc == crc2)
			return true;
		else {
			Logger.warning("Modbus",
					"CRC error calc:" + Integer.toHexString(crc) + " in response: " + Integer.toHexString(crc2));
			return false;
		}
	}

}
