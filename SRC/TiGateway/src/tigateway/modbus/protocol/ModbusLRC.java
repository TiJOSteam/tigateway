package tigateway.modbus.protocol;

public class ModbusLRC {

	public static int calculateLRC(byte[] data, int offset, int length) {
		int lrc = 0;
		for (int i = 0; i < length; i++) {
			lrc -= data[i + offset];
		}

		return (int) (lrc & 0xff);
	}
}
