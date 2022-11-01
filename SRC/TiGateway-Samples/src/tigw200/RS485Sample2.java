package tigw200;
import java.io.IOException;

import tigateway.TiGW200;
import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiUART;
import tijos.framework.util.Delay;
import tijos.framework.util.Formatter;

public class RS485Sample2 {

	public static void main(String[] args) {

		try {
			// 获取TiGW200对象并启动看门狗
			TiGW200 gw200 = TiGW200.getInstance();

			// 获取第0路RS485 9600 8 1 N
			TiSerialPort rs485 = gw200.getRS485(9600, 8, 1, TiUART.PARITY_NONE);

//		// 获取第1路RS485 9600 8 1 N
//		TiSerialPort rs485 = gw200.getRS485ById(1,9600,8,1,TiUART.PARITY_NONE);

			byte[] on = Formatter.hexStringToByte("483A01570101010100000000DE4544");
			byte[] off = Formatter.hexStringToByte("483A01570000000000000000DA4544");
			
	
			int count = 0;
			while (count ++ < 20) {

				/**
				 * 下发/读取16进制数据
				 */
				//下发输出数据				
				if(count % 2== 0) {
					rs485.write(on);
				}else {
					rs485.write(off);
				}
				

				//读取485返回数据 超时2秒
				byte[] receive = rs485.read(1000);
				
				//未读到数据
				if (receive == null) {
					System.out.println("no data.");
				}
				else {
					//HEX转成字符串打印
					System.out.println("read data: " + Formatter.toHexString(receive));					
				}

			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
