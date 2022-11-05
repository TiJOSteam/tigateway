package tigw260;

import java.io.IOException;

import tigateway.TiGW260;
import tijos.framework.util.Delay;

/**
 * 可控电源输出例程, 可连接声光报警器 打开电源即可报警
 * 
 * @author TiJOS
 *
 */
public class VoutSample {

	public static void main(String[] args) {

		TiGW260 gw260 = TiGW260.getInstance();

		try {
			//打开电源
			gw260.vout().turnOn();

			Delay.msDelay(1000);

			//关闭电源
			gw260.vout().turnOff();

			Delay.msDelay(2000);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
