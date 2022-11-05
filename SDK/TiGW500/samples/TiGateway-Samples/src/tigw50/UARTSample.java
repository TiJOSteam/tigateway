package tigw50;


import tigateway.TiGW50;
import tigateway.serialport.TiSerialPort;

/**
 * 串口例程
 * @author lemon
 *
 */
public class UARTSample {

	public static void main(String[] args) {

		try {
			TiGW50 gw50 = TiGW50.getInstance();

			TiSerialPort sp1 = gw50.getSerialPort(1, 9600, 8, 1, 0);

			sp1.write("this is port1".getBytes());
			byte [] bt1 = sp1.read(2000);
			System.out.println(new String(bt1));

			TiSerialPort sp2 = gw50.getSerialPort(2, 9600, 8, 1, 0);
			sp2.write("this is port2".getBytes());
			byte [] bt2 = sp1.read(2000);
			System.out.println(new String(bt2));

			TiSerialPort sp3 = gw50.getSerialPort(3, 9600, 8, 1, 0);
			sp3.write("this is port3".getBytes());
			byte [] bt3 = sp1.read(2000);
			System.out.println(new String(bt3));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
