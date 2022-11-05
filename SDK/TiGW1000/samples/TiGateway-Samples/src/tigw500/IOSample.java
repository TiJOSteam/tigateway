package tigw500;

import tigateway.TiGW500;
import tijos.framework.util.Delay;

public class IOSample {

	public static void main(String[] args) {
		
		TiGW500 gw500 = TiGW500.getInstance();
		
		try {
			//打开继电器	
			gw500.relayControl(0, 1);
			Delay.msDelay(1000);
			
			//关闭继电器
			gw500.relayControl(0, 0);
			Delay.msDelay(1000);
			
			
			//数字量输入通道0
			int level = gw500.digitalInput(0);
			System.out.println("level " + level);
			Delay.msDelay(1000);

			//数字量输入通道1
			level = gw500.digitalInput(1);
			System.out.println("level " + level);
			Delay.msDelay(1000);

		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
