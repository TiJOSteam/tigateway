package tigw500;

import tigateway.TiGW500;
import tijos.framework.util.Delay;

public class CurrentLoopOutputSample {

	public static void main(String[] args) {

		TiGW500 gw500 = TiGW500.getInstance();

		// 通过1号通道电流环输出10mA
		int uA = 20 * 1000; // uA

		try {
			gw500.currentLoopOutput(0, uA);

			while(true) {
				Delay.msDelay(1000);
				System.out.println("running");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
