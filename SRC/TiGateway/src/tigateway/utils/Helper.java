package tigateway.utils;

import java.util.Calendar;

public class Helper {

	public static  void reverseBytes(byte[] buff) {
		byte temp = 0;
		for (int i = 0; i < buff.length / 2; i++) {
			temp = buff[i];
			buff[i] = buff[buff.length - i - 1];
			buff[buff.length - i - 1] = temp;
		}

	}
	
	/**
	 * Get current time in BCD encode 
	 * @return BCD code in 7 bytes  - Century, Year, Month, Day, Hour, Minute, Second
	 */
	public static byte [] getTimeBCD(long time)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time * 1000);

		byte [] out = new byte[7];

		 out[0] = BCDEncoder.getByteBCD(cal.get(Calendar.YEAR) / 100);//年份的后两位
		 out[1] = BCDEncoder.getByteBCD(cal.get(Calendar.YEAR) % 100);//年份的后两位
	     out[2] = BCDEncoder.getByteBCD(cal.get(Calendar.MONTH) + 1);//月份加一
	     out[3] = BCDEncoder.getByteBCD(cal.get(Calendar.DAY_OF_MONTH));
	     out[4] = BCDEncoder.getByteBCD(cal.get(Calendar.HOUR_OF_DAY));
	     out[5] = BCDEncoder.getByteBCD(cal.get(Calendar.MINUTE));
	     out[6] = BCDEncoder.getByteBCD(cal.get(Calendar.SECOND));
		
		return out;
	}
	
	/**
	 * Convert BCD to reading
	 * 
	 * @param input
	 * @param decimal
	 * @return
	 */
	public static double BCD2Double(byte[] input, int decimal) {
		return BCD2Double(input, 0, input.length, decimal);
	}

	public static double BCD2Double(byte[] input, int start, int len, int decimal) {
		double reading = 0;
		double coef = 1;

		if (start + len > input.length)
			return Double.NaN;

		/* result is in BCD format XXXXXX.XX, little endian */
		for (int i = 0; i < len; ++i) {
			reading += (input[start + i] & 0x0f) * coef;
			reading += ((input[start + i] >>> 4) & 0x0F) * 10 * coef;
			coef *= 100;
		}

		for (int i = 0; i < decimal; i++) {
			reading /= 10.0;
		}

		return reading;
	}
	
	/**
	 * Memory compare
	 * 
	 * @param b1
	 * @param startB1
	 * @param b2
	 * @param startB2
	 * @param sz
	 * @return
	 */
	public static int memcmp(byte b1[], int startB1, byte b2[], int startB2, int sz) {
		for (int i = 0; i < sz; i++) {
			int v1 = b1[i + startB1];
			int v2 = b2[i + startB2];

			if (v1 != v2) {
				if ((v1 >= 0 && v2 >= 0) || (v1 < 0 && v2 < 0))
					return v1 - v2;
				if (v1 < 0 && v2 >= 0)
					return 1;
				if (v2 < 0 && v1 >= 0)
					return -1;
			}
		}
		return 0;
	}

}
