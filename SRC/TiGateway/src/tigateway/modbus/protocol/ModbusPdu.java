package tigateway.modbus.protocol;

import static tigateway.modbus.protocol.ModbusConstants.*;

/**
 * MODBUS PDU
 *
 * @author TiJOS
 */
public class ModbusPdu {

	protected final byte[] pdu = new byte[MAX_PDU_SIZE]; // function (1 byte), data (0..252 bytes)
	protected int pduSize;

	public static final String toHex(byte[] data, int offset, int length) {
		if ((data.length == 0) || (offset > data.length) || (length < offset))
			return "";
		length = Math.min(data.length - offset, length);
		StringBuffer buf = new StringBuffer(length * 3);
		for (int i = 0; i < length; i++) {
			int b = data[i + offset] & 0xFF;
			buf.append(Integer.toHexString(b >>> 4));
			buf.append(Integer.toHexString(b & 0xF));
			if (i < length - 1)
				buf.append(" ");
		}
		return buf.toString();
	}

	public static final String byteToHex(byte b) {
		int t = b & 0xFF;
		return Integer.toHexString(t >>> 4) + Integer.toHexString(t & 0xF);
	}

	public static final int bytesToInt16(byte lowByte, byte highByte, boolean unsigned) {
		// returned value is signed
		int i = (((int) highByte) << 8) | (((int) lowByte) & 0xFF);
		if (unsigned)
			return i & 0xFFFF;
		else
			return i;
	}

	public static final int ints16ToInt32(int lowInt16, int highInt16) {
		return (highInt16 << 16) | (lowInt16 & 0xFFFF);
	}

	public static final float ints16ToFloat(int lowInt16, int highInt16) {
		return Float.intBitsToFloat(ints16ToInt32(lowInt16, highInt16));
	}

	public static final byte highByte(int int16) {
		return (byte) (int16 >>> 8);
	}

	public static final byte lowByte(int int16) {
		return (byte) (int16);
	}

	public static final int highInt16(int int32) {
		// returned value is signed
		return int32 >> 16;
	}

	public static final int lowInt16(int int32) {
		// returned value is signed
		return ((int32 & 0xFFFF) << 16) >> 16;
	}

	public static final boolean[] int16ToBits(int int16) {
		boolean[] bits = new boolean[16];
		for (int i = 0; i < 16; i++, int16 >>= 1)
			bits[i] = (int16 & 1) != 0;
		return bits;
	}

	public static final int bytesCount(int bitsCount) {
		int bytes = bitsCount / 8;
		if ((bitsCount % 8) != 0)
			bytes++;
		return bytes;
	}

	public void setPduSize(int size) {
		if ((size < 1) || (size > MAX_PDU_SIZE))
			throw new IllegalArgumentException("Invalid PDU size: " + size);
		pduSize = size;
	}

	public int getPduSize() {
		return pduSize;
	}

	public int getFunction() {
		return readByteFromPDU(0, true);
	}

	public void setFunction(int code) {
		writeByteToPDU(0, (byte) code);
	}

	public void readFromPdu(int pduOffset, int size, byte[] dest, int destOffset) {
		System.arraycopy(pdu, pduOffset, dest, destOffset, size);
	}

	public void writeToPdu(byte[] src, int srcOffset, int size, int pduOffset) {
		System.arraycopy(src, srcOffset, pdu, pduOffset, size);
	}

	public int writeByteToPDU(int offset, byte value) {
		if ((offset < 0) || (offset >= pduSize))
			throw new IndexOutOfBoundsException();
		pdu[offset] = value;

		return 1;
	}

	public int writeInt16ToPDU(int offset, int value) {
		// We can only write words starting from offset 1, because there is function
		// code at offset 0.
		if ((offset < 1) || (offset >= pduSize - 1))
			throw new IndexOutOfBoundsException();
		// Modbus uses a "big-Endian" representation (the most significant byte is sent
		// first).
		pdu[offset] = highByte(value);
		pdu[offset + 1] = lowByte(value);

		return 2;
	}

	public void writeBitToPDU(int firstByte, int bitOffset, boolean value) {
		int offset = firstByte + (bitOffset / 8);
		byte b = readByteFromPDU(offset);
		if (value)
			b = (byte) (b | (1 << (bitOffset % 8)));
		else
			b = (byte) (b & ~(1 << (bitOffset % 8)));
		writeByteToPDU(offset, b);
	}

	public int readByteFromPDU(int offset, boolean unsigned) {
		if (unsigned)
			return ((int) readByteFromPDU(offset)) & 0xFF;
		else
			return readByteFromPDU(offset);
	}

	public byte readByteFromPDU(int offset) {
		if ((offset < 0) || (offset >= pduSize))
			throw new IndexOutOfBoundsException();
		return pdu[offset];
	}

	public int readInt16FromPDU(int offset, boolean unsigned) {
		// Integers can be placed only in DATA section of PDU (starting from offset 1)
		if ((offset < 1) || (offset >= pduSize - 1))
			throw new IndexOutOfBoundsException();
		// Big-endian is standard for MODBUS
		return bytesToInt16(pdu[offset + 1], pdu[offset], unsigned);
	}

	public int readInt16FromPDU(int offset, boolean unsigned, boolean bigEndian) {
		// Integers can be placed only in DATA section of PDU (starting from offset 1)
		if ((offset < 1) || (offset >= pduSize - 1))
			throw new IndexOutOfBoundsException();
		if (bigEndian) {
			// Big-endian is standard for MODBUS
			return bytesToInt16(pdu[offset], pdu[offset + 1], unsigned);
		} else {
			return bytesToInt16(pdu[offset + 1], pdu[offset], unsigned);

		}
	}

	protected int readInt32FromPDU(int offset, boolean bigEndian) {
		if (bigEndian)
			// this is "big-endian" (0x12345678 stored as 0x12, 0x34, 0x56, 0x78)
			return ints16ToInt32(readInt16FromPDU(offset + 2, false), readInt16FromPDU(offset, false));
		else
			// this is "middle-endian" (0x12345678 stored as 0x56, 0x78, 0x12, 0x34)
			return ints16ToInt32(readInt16FromPDU(offset, false), readInt16FromPDU(offset + 2, false));
	}

	protected float readFloatFromPDU(int offset, boolean bigEndian) {
		return Float.intBitsToFloat(readInt32FromPDU(offset, bigEndian));
	}

	public boolean readBitFromPDU(int firstByte, int bitOffset) {
		byte b = readByteFromPDU(firstByte + (bitOffset / 8));
		return (b & (1 << (bitOffset % 8))) != 0;
	}

	public int calculateExpectPduSize(int function, int count) {
		switch (function) {
		case FN_READ_COILS:
		case FN_READ_DISCRETE_INPUTS:
			return 2 + bytesCount(count);
		case FN_READ_HOLDING_REGISTERS:
		case FN_READ_INPUT_REGISTERS:
			return 2 + count * 2;
		case FN_WRITE_SINGLE_COIL:
		case FN_WRITE_SINGLE_REGISTER:
		case FN_WRITE_MULTIPLE_COILS:
		case FN_WRITE_MULTIPLE_REGISTERS:
			return 5;
		default:
			return 5;
		}
	}

}
