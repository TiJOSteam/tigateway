package tigw40;


import tigateway.TiGW40;
import tigateway.serialport.TiSerialPort;

/**
 * 串口例程
 * @author lemon
 *
 */
public class UARTSample {

	public static void main(String[] args) {

		try {
			TiGW40 gw40 = TiGW40.getInstance();

			TiSerialPort sp1 = gw40.getSerialPort(0, 9600, 8, 1, 0);

			sp1.write("this is port1".getBytes());
			
			byte [] bt1 = sp1.read(2000);
			if(bt1 != null) {
				System.out.println(new String(bt1));
			}
			else {
				System.out.println("no data received");
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
