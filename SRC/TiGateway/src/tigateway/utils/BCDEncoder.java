package  tigateway.utils;

import java.util.Calendar;

/**
 * Compressed 8421 BCD
 * 
 * @author TiJOS
 *
 */
public class BCDEncoder {

	/**
	 * String to BCD , like getStringBCD("823258967310960") output 0x823258967310960
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] getStringBCD(String input) {

		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) < '0' || (input.charAt(i) > '9' && input.charAt(i) > 'A')
					|| (input.charAt(i) > 'Z' && input.charAt(i) > 'a')) {
				throw new IllegalArgumentException("input should be sequence of decimal‎ character.");
			}
		}

		byte[] temp = (input.length() % 2 != 0 ? "0" + input : input).getBytes();

		byte[] output = new byte[temp.length / 2];
		for (int i = 0, j = 0, k = 0; i < output.length; i++) {

			if (temp[2 * i] >= '0' && temp[2 * i] <= '9') {
				j = temp[2 * i] - '0';
			} else if (temp[2 * i] >= 'a' && temp[2 * i] <= 'z') {
				j = temp[2 * i] - 'a' + 0x0a;
			} else {
				j = temp[2 * i] - 'A' + 0x0a;
			}
			if (temp[2 * i + 1] >= '0' && temp[2 * i + 1] <= '9') {
				k = temp[2 * i + 1] - '0';
			} else if (temp[2 * i + 1] >= 'a' && temp[2 * i + 1] <= 'z') {
				k = temp[2 * i + 1] - 'a' + 0x0a;
			} else {
				k = temp[2 * i + 1] - 'A' + 0x0a;
			}
			output[i] = (byte) ((j << 4) | k);
		}
		return output;
	}

	/**
	 * Convert a byte to BCD
	 * 
	 * @param a
	 * @return BCD code
	 */
	public static byte getByteBCD(int a) {
		return (byte) ((a / 10) * 16 + (a % 10));
	}

	/**
	 * Convert Double to BCD
	 * 
	 * @param value
	 * @param byteNum      expected output byte number
	 * @param decimalPoint decimal point number
	 * @return BCD code
	 */
	public static byte[] getDoubleBCD(double value, int byteNum, int decimalPoint) {
		for (int i = 0; i < decimalPoint; i++) {
			value *= 10;
		}

		int tmp = (int) value;

		return getDecimalBCD(tmp, byteNum);
	}

	/**
	 * Convert decimal to BCD
	 * 
	 * @param Dec     decimal value
	 * @param byteNum expected byte number
	 * @return BCD code
	 */
	public static byte[] getDecimalBCD(int Dec, int byteNum) {
		int i;
		int temp;

		byte[] Bcd = new byte[byteNum];
		for (i = byteNum - 1; i >= 0; i--) {
			temp = Dec % 100;
			Bcd[i] = (byte) (((temp / 10) << 4) + ((temp % 10) & 0x0F));
			Dec /= 100;
		}

		return Bcd;
	}

	/**
	 * Get current time in BCD encode
	 * 
	 * @return BCD code in 7 bytes - Century, Year, Month, Day, Hour, Minute, Second
	 */
	public static byte[] getCurrentTimeBCD() {
		byte[] out = new byte[7];

		Calendar cal = Calendar.getInstance();

		out[0] = getByteBCD(cal.get(Calendar.YEAR) / 100);// 年份的前两位
		out[1] = getByteBCD(cal.get(Calendar.YEAR) % 100);// 年份的后两位
		out[2] = getByteBCD(cal.get(Calendar.MONTH) + 1);// 月份加一
		out[3] = getByteBCD(cal.get(Calendar.DAY_OF_MONTH));
		out[4] = getByteBCD(cal.get(Calendar.HOUR_OF_DAY));
		out[5] = getByteBCD(cal.get(Calendar.MINUTE));
		out[6] = getByteBCD(cal.get(Calendar.SECOND));

		return out;
	}

}
