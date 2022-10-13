package  tigateway.utils;

/**
 * BCD Decoder
 * 
 * @author TiJOS
 *
 */
public class BCDDecoder {

	/**
	 * BCD to string
	 * 
	 * @param bcd
	 * @return
	 */
	public static String decodeStringBCD(byte[] bcd) {

		StringBuilder temp = new StringBuilder(bcd.length * 2);
		for (int i = 0; i < bcd.length; i++) {
			temp.append((byte) ((bcd[i] & 0xf0) >>> 4));
			temp.append((byte) (bcd[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * Convert BCD to double
	 * 
	 * @param bcd          BCD code
	 * @param decimalPoint decimal point of the double value
	 * @return double result
	 */
	public static double decodeDoubleBCD(byte[] bcd, int start, int len, int decimalPoint) {
		double reading = 0;
		double coef = 1;

		if (start + len > bcd.length)
			return Double.NaN;

		/* result is in BCD format XXXXXX.XX */
		for (int i = 0; i < len; ++i) {
			reading += (bcd[start + i] & 0x0f) * coef;
			reading += ((bcd[start + i] >>> 4)&0x0F) * 10 * coef;
			coef *= 100;
		}

		for (int i = 0; i < decimalPoint; i++) {
			reading /= 10.0;
		}

		return reading;
	}
	
	/**
	 * Convert BCD to byte
	 * 
	 * @param bcd
	 * @return
	 */
	public static int decodeByteBCD(int bcd) {
		return (bcd / 16 * 10 + bcd % 16);
	}

}
