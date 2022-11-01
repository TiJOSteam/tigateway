package tigw500;

import tigateway.TiGW500;

public class CurrentLoopInputSample {

	public static void main(String[] args) {

		TiGW500 gw500 = TiGW500.getInstance();

		try {
			// 从0通道读取电流环电流值 单位uA
			int value = gw500.currentLoopInput(0);
			
			System.out.println("Current(uA) " + value);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
