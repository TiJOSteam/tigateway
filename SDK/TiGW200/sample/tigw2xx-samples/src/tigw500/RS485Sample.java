package tigw500;
import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiUART;
import tijos.framework.util.Delay;
import tijos.framework.util.Formatter;

public class RS485Sample {

	public static void main(String[] args) {

		try {
			TiGW500 gw260 = TiGW500.getInstance();

			// 获取第0路RS485 9600 8 1 N
			TiSerialPort rs485 = gw260.getRS485(9600, 8, 1, TiUART.PARITY_NONE);

			byte[] outputHex = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
			String outputString = "this is a text";
			
			while (true) {

				/**
				 * 下发/读取16进制数据
				 */
				//下发输出数据
				rs485.write(outputHex, 0, outputHex.length);

				//读取485返回数据 超时2秒
				byte[] receive = rs485.read(2000);
				
				//未读到数据
				if (receive == null) {
					System.out.println("no data.");
				}
				else {
					//HEX转成字符串打印
					System.out.println("read data: " + Formatter.toHexString(receive));					
				}
				
				/**
				 * 下发/读取字符串
				 */
				byte [] data = outputString.getBytes();
				rs485.write(data, 0, data.length);
				
				//读取485返回数据 超时2秒
				receive = rs485.read(2000);
				//未读到数据
				if (receive == null) {
					System.out.println("no data.");
				}
				else {
					//字符串打印
					System.out.println("read data: " + new String(receive));					
				}
				
				
				//延时3秒
				Delay.msDelay(3000);

			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
