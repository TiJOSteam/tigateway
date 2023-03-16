package tigw50;

import tigateway.TiGW50;
import tijos.framework.util.Delay;

public class IOSample {

	public static void main(String[] args) {
		
		TiGW50 gw50 = TiGW50.getInstance();
		
		try {		
			//GPIO10 推挽输出
			gw50.digitalOpen(10, false); //
			
			//GPIO10 输出通道0高电平
			gw50.digitalOutput(10, 1);
			Delay.msDelay(1000);
			
			//GPIO10 输出通道0低电平
			gw50.digitalOutput(0, 0);
			Delay.msDelay(1000);
			
			//GPIO11 浮空输入模式
			gw50.digitalOpen(11, true); //
			
			//GPIO11 输入值
			System.out.println("gpio11 " + gw50.digitalInput(11));
			Delay.msDelay(1000);
			
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
