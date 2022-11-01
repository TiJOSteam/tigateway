package tigw1000;

import tigateway.TiGW1000;
import tijos.framework.util.Delay;

public class IOSample {

	public static void main(String[] args) {
		
		TiGW1000 gw1000 = TiGW1000.getInstance();
		
		try {
			//打开继电器	
			gw1000.relayControl(0, 1);
			Delay.msDelay(1000);
			
			//关闭继电器
			gw1000.relayControl(0, 0);
			Delay.msDelay(1000);
			
		
			//数字量输出通道0高电平
			gw1000.digitalOutput(0, 1);
			Delay.msDelay(1000);
			
			//数字量输出通道0低电平
			gw1000.digitalOutput(0, 0);
			Delay.msDelay(1000);
			
			//数字量输入通道0
			int level = gw1000.digitalInput(0);
			System.out.println("level " + level);
			Delay.msDelay(1000);
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
