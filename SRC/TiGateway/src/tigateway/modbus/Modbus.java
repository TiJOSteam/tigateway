package tigateway.modbus;

import static tigateway.modbus.protocol.ModbusConstants.*;

import tigateway.modbus.protocol.ModbusPdu;
import tigateway.modbus.transport.IMBTransport;
import tijos.framework.util.logging.Logger;

/**
 * MODBUS RTU driver for TiJOS based on https://github.com/sp20/modbus-mini
 *
 * @author TiJOS
 */
public class Modbus extends ModbusPdu {

	/**
	 * Modbus Result Code
	 */
	public static final byte RESULT_OK = 0;
	public static final byte RESULT_TIMEOUT = 1;
	public static final byte RESULT_EXCEPTION = 2; // Modbus exception. Get code by getExceptionCode()
	public static final byte RESULT_BAD_RESPONSE = 3; // CRC mismatch, or invalid format

	private boolean responseReady = false;

	/**
	 * device address
	 */
	private byte deviceId;
	private int expectedPduSize;
	private int expectedAddress = -1;
	private int expectedCount = -1;
	private int result; // RESULT_*

	/**
	 * Transport
	 */
	private IMBTransport transport;
	
	/**
	 * Set transport object
	 *
	 * @param tr
	 */
	protected Modbus() {
	}

	/**
	 * Set transport object
	 *
	 * @param tr
	 */
	public void setTransport(IMBTransport tr) {
		this.transport = tr;
	}

	/**
	 * set read timeout of the rs485
	 * @param timeout
	 */
	public void setCommTimeout(int timeout) {
		this.transport.setCommTimout(timeout);
	}
	/**
	 * Get device id
	 *
	 * @return
	 */
	public byte getDeviceId() {
		return deviceId;
	}

	/**
	 * Expected PDU size
	 *
	 * @return
	 */
	public int getExpectedPduSize() {
		return expectedPduSize;
	}

	/**
	 * Initialize a custom request
	 *
	 * @param deviceId           device address
	 * @param newPduSize         pdu size
	 * @param function           function code
	 * @param newExpectedPduSize expected size
	 */
	protected void initCustomRequest(int deviceId, int newPduSize, byte function, int newExpectedPduSize) {
		setPduSize(newPduSize);
		writeByteToPDU(0, function);
		this.deviceId = (byte) deviceId;
		this.expectedAddress = -1;
		this.expectedCount = -1;
		this.expectedPduSize = newExpectedPduSize;
		this.responseReady = false;
	}

	/**
	 * Initialize request
	 *
	 * @param deviceId device id 
	 * @param newPduSize  request pdu size
	 * @param function  function 
	 * @param param1
	 * @param param2
	 * @param newExpectedAddress  
	 * @param newExpectedCount 
	 * @param newExpectedPduSize  resp pdu size
	 */
	protected void initRequest(int deviceId, int newPduSize, byte function, int param1, int param2,
			int newExpectedAddress, int newExpectedCount, int newExpectedPduSize) {
		setPduSize(newPduSize);
		writeByteToPDU(0, function);
		writeInt16ToPDU(1, param1);
		writeInt16ToPDU(3, param2);
		this.deviceId = (byte) deviceId;
		this.expectedAddress = newExpectedAddress;
		this.expectedCount = newExpectedCount;
		this.expectedPduSize = newExpectedPduSize;
		this.responseReady = false;
	}

	/**
	 * Write request to device
	 * @param deviceId
	 * @param newPduSize request pdu size
	 * @param function  function 
	 * @param param1
	 * @param param2
	 */
	protected void initWriteRequest(int deviceId, int newPduSize, byte function, int param1, int param2) {

		int pduRspSize = 5;
		this.initRequest(deviceId, newPduSize, function, param1, param2, -1, -1, pduRspSize);
	}

	/**
	 * Read request to device
	 * @param deviceId
	 * @param function
	 * @param startAddress
	 * @param count
	 */
	protected void initReadRequest(int deviceId, byte function, int startAddress, int count) {

		int pduRspSize = calculateExpectPduSize(function, count);
		initRequest(deviceId, 5, function, startAddress, count, startAddress, count, pduRspSize);
	}

	/**
	 * Initialize a Read Coils request
	 *
	 * @param deviceId     Device address
	 * @param startAddress start address
	 * @param count        read number
	 */
	public void initReadCoilsRequest(int deviceId, int startAddress, int count) {
		if ((count < 1) || (count > MAX_READ_COILS))
			throw new IllegalArgumentException();

		initReadRequest(deviceId, FN_READ_COILS, startAddress, count);
	}

	/**
	 * Initialize a READ DISCRETE INPUT REGISTERs request
	 *
	 * @param deviceId     device address
	 * @param startAddress start address of the registers
	 * @param count        number to read
	 */
	public void initReadDInputsRequest(int deviceId, int startAddress, int count) {
		if ((count < 1) || (count > MAX_READ_COILS))
			throw new IllegalArgumentException();

		initReadRequest(deviceId, FN_READ_DISCRETE_INPUTS, startAddress, count);
	}

	/**
	 * Initialize a READ HOLDING REGISTERs request
	 *
	 * @param deviceId     device address
	 * @param startAddress start address of the registers
	 * @param count        number to read
	 */
	public void initReadHoldingsRequest(int deviceId, int startAddress, int count) {
		if ((count < 1) || (count > MAX_READ_REGS))
			throw new IllegalArgumentException();

		// resp: func, bytecount, register values
		initReadRequest(deviceId, FN_READ_HOLDING_REGISTERS, startAddress, count);

	}

	/**
	 * Initialize a READ INPUT REGISTERs request
	 *
	 * @param deviceId     device address
	 * @param startAddress register start address
	 * @param count        register number
	 */
	public void initReadAInputsRequest(int deviceId, int startAddress, int count) {
		if ((count < 1) || (count > MAX_READ_REGS))
			throw new IllegalArgumentException();

		initReadRequest(deviceId, FN_READ_INPUT_REGISTERS, startAddress, count);
	}

	/**
	 * Initialize a WRITE COIL register request - one register operation
	 *
	 * @param deviceId    device address
	 * @param coilAddress coil address
	 * @param value       value
	 */
	public void initWriteCoilRequest(int deviceId, int coilAddress, boolean value) {
		initWriteRequest(deviceId, 5, FN_WRITE_SINGLE_COIL, coilAddress, value ? 0xFF00 : 0);
	}

	/**
	 * Initialize a WRITE SINGLE REGISTER request
	 *
	 * @param deviceId   device address
	 * @param regAddress register address
	 * @param value      value to write
	 */
	public void initWriteRegisterRequest(int deviceId, int regAddress, int value) {
		initWriteRequest(deviceId, 5, FN_WRITE_SINGLE_REGISTER, regAddress, value);
	}

	/**
	 * Initialize WRITE MULTIPLE COILS registers
	 *
	 * @param deviceId     device address
	 * @param startAddress start register address
	 * @param values       multiple values to write coil
	 */
	public void initWriteCoilsRequest(int deviceId, int startAddress, boolean[] values) {
		if (values.length > MAX_WRITE_COILS)
			throw new IllegalArgumentException();

		int bytes = bytesCount(values.length);
		initWriteRequest(deviceId, 6 + bytes, FN_WRITE_MULTIPLE_COILS, startAddress, values.length);

		writeByteToPDU(5, (byte) bytes);
		for (int i = 0; i < bytes; i++) {
			byte b = 0;
			for (int j = 0; j < 8; j++) {
				int k = i * 8 + j;
				if ((k < values.length) && values[k])
					b = (byte) (b | (1 << j));
			}
			writeByteToPDU(6 + i, b);
		}
	}

	/**
	 * Initialize WRITE MULTIPLE registers
	 *
	 * @param deviceId     device address
	 * @param startAddress start register address
	 * @param values       multiple values to write
	 */
	public void initWriteRegistersRequest(int deviceId, int startAddress, int[] values) {
		if (values.length > MAX_WRITE_REGS)
			throw new IllegalArgumentException();
		int bytes = values.length * 2;

		initWriteRequest(deviceId, 6 + bytes, FN_WRITE_MULTIPLE_REGISTERS, startAddress, values.length);

		writeByteToPDU(5, (byte) bytes);
		for (int i = 0; i < values.length; i++) {
			writeInt16ToPDU(6 + i * 2, values[i]);
		}
	}

	/**
	 * Send request to the device and wait for the response
	 *
	 * @return result modbus result
	 * @throws Exception
	 */
	public int execRequest() throws Exception {
		transport.sendRequest(this);

		result = transport.waitResponse(this);
		responseReady = (result == RESULT_OK);
		if (!responseReady) {
			if (result == RESULT_EXCEPTION)
				Logger.warning("Modbus",
						"Exception 0x " + byteToHex((byte) getExceptionCode()) + " from " + getDeviceId());
			else
				Logger.warning("Modbus", getResultAsString() + " from " + getDeviceId());
		}

		return result;

	}

	/**
	 * Response result
	 *
	 * @return
	 */
	public int getResult() {
		return result;
	}

	public String getResultAsString() {
		switch (result) {
		case RESULT_OK:
			return "OK";
		case RESULT_BAD_RESPONSE:
			return "Bad response";
		case RESULT_EXCEPTION:
			return "Exception " + getExceptionCode();
		case RESULT_TIMEOUT:
			return "Timeout";
		default:
			return null;
		}
	}

	/**
	 * Get modbus exception code
	 *
	 * @return
	 */
	public int getExceptionCode() {
		if (((getFunction() & 0x80) == 0) || (getPduSize() < 2))
			return 0;
		else
			return readByteFromPDU(1, true);
	}

	/**
	 * get response start address
	 *
	 * @return
	 */
	public int getResponseAddress() {
		if (responseReady && (expectedAddress >= 0))
			return (expectedAddress);
		else
			throw new IllegalStateException();
	}

	/**
	 * get response register count
	 *
	 * @return
	 */
	public int getResponseCount() {
		if (responseReady && (expectedCount >= 0))
			return (expectedCount);
		else
			throw new IllegalStateException();
	}

	/**
	 * Get discrete value from response to request initiated by
	 * {@link #InitReadCoilsRequest()} or {@link #InitReadDInputsRequest()}. Call
	 * this method ONLY after successful execution of {@link #execRequest()}.<br>
	 *
	 * @param address - Address of bit. It must be in the range specified in
	 *                request. You can use {@link #getResponseAddress()} and
	 *                {@link #getResponseCount()}.
	 * @return Value of bit at given address.
	 */
	public boolean getResponseBit(int address) {
		if ((getFunction() == FN_READ_COILS) || (getFunction() == FN_READ_DISCRETE_INPUTS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			byte b = readByteFromPDU(2 + offset / 8);
			return (b & (1 << (offset % 8))) != 0;
		} else
			throw new IllegalStateException();
	}

	/**
	 * Get register value from response to request initiated by
	 * {@link #InitReadHoldingsRequest()} or {@link #InitReadAInputsRequest()}. Call
	 * this method ONLY after successful execution of {@link #execRequest()}.<br>
	 * There are various utility methods in {@link ModbusPdu} to manipulate int16
	 * values.
	 *
	 * @param address  - Address of register. It must be in the range specified in
	 *                 request. You can use {@link #getResponseAddress()} and
	 *                 {@link #getResponseCount()}.
	 * @param unsigned - Should value stored in PDU be interpreted as signed or
	 *                 unsigned.
	 * @return Value of register at given address. This value is 16 bit signed
	 *         (-32768..+32767) or 16 bit unsigned (0..65535) depending on
	 *         <b>unsigned</b> parameter.
	 */
	public int getResponseInt16(int address, boolean unsigned) {
		if ((getFunction() == FN_READ_HOLDING_REGISTERS) || (getFunction() == FN_READ_INPUT_REGISTERS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			return readInt16FromPDU(2 + offset * 2, unsigned);
		} else
			throw new IllegalStateException();
	}

	/**
	 * Get register value from response to request initiated by
	 * {@link #InitReadHoldingsRequest()} or {@link #InitReadAInputsRequest()}. Call
	 * this method ONLY after successful execution of {@link #execRequest()}.<br>
	 * There are various utility methods in {@link ModbusPdu} to manipulate int16
	 * values.
	 *
	 * @param address   - Address of register. It must be in the range specified in
	 *                  request. You can use {@link #getResponseAddress()} and
	 *                  {@link #getResponseCount()}.
	 * @param bigendian - Should value stored in PDU be interpreted as signed or
	 *                  unsigned.
	 * @return Value of register at given address. This value is 16 bit signed
	 *         (-32768..+32767)
	 */
	public int getResponseSInt16(int address, boolean bigEndian) {

		boolean unsigned = false;

		if ((getFunction() == FN_READ_HOLDING_REGISTERS) || (getFunction() == FN_READ_INPUT_REGISTERS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			return readInt16FromPDU(2 + offset * 2, unsigned, bigEndian);
		} else
			throw new IllegalStateException();

	}

	/**
	 * Get register value from response to request initiated by
	 * {@link #InitReadHoldingsRequest()} or {@link #InitReadAInputsRequest()}. Call
	 * this method ONLY after successful execution of {@link #execRequest()}.<br>
	 * There are various utility methods in {@link ModbusPdu} to manipulate int16
	 * values.
	 *
	 * @param address   - Address of register. It must be in the range specified in
	 *                  request. You can use {@link #getResponseAddress()} and
	 *                  {@link #getResponseCount()}.
	 * @param bigEndian - bigEndian or not
	 * @return Value of register at given address. This value is 16 bit unsigned
	 *         (0..65535) depending on <b>unsigned</b> parameter.
	 */
	public int getResponseUInt16(int address, boolean bigEndian) {

		boolean unsigned = true;
		if ((getFunction() == FN_READ_HOLDING_REGISTERS) || (getFunction() == FN_READ_INPUT_REGISTERS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			return readInt16FromPDU(2 + offset * 2, unsigned, bigEndian);
		} else
			throw new IllegalStateException();
	}

	/**
	 * Get register value from response and convert to a int32 value (4 bytes)
	 *
	 * @param address
	 * @param bigEndian
	 * @return
	 */
	public int getResponseInt32(int address, boolean bigEndian) {
		if ((getFunction() == FN_READ_HOLDING_REGISTERS) || (getFunction() == FN_READ_INPUT_REGISTERS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			return readInt32FromPDU(2 + offset * 2, bigEndian);
		} else
			throw new IllegalStateException();
	}

	/**
	 * Get register value from response and convert to a float value (4bytes)
	 *
	 * @param address
	 * @param bigEndian big or little endian
	 * @return float value from the response
	 */
	public float getResponseFloat(int address, boolean bigEndian) {
		if ((getFunction() == FN_READ_HOLDING_REGISTERS) || (getFunction() == FN_READ_INPUT_REGISTERS)) {
			int offset = address - getResponseAddress();
			if ((offset < 0) || (offset >= getResponseCount()))
				throw new IndexOutOfBoundsException();
			return readFloatFromPDU(2 + offset * 2, bigEndian);
		} else
			throw new IllegalStateException();
	}

}
