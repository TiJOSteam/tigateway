package tigw400;

import java.io.IOException;

import tigateway.TiGW400;
import tijos.framework.platform.lpwan.TiNBIoT;
import tijos.framework.util.Delay;

public class NBNetworkSample {

	public static void main(String[] args) {

		TiGW400 gw400 = TiGW400.getInstance();

		try {
			TiNBIoT.getInstance().startup(30);

			System.out.println("Connected to NBIoT.");
			// 注网成功 蓝灯亮
			gw400.blueLED().turnOn();

			// 4G设备唯一ID
			System.out.println("IMEI " + TiNBIoT.getInstance().getIMEI());

			// 4G信号强度
			System.out.println("RSSI " + TiNBIoT.getInstance().getRSSI());

			// SIM卡IMSI
			System.out.println("IMSI " + TiNBIoT.getInstance().getIMSI());

			// SIM卡ICCID编号
			System.out.println("ICCID " + TiNBIoT.getInstance().getICCID());

			System.out.println("CI " + TiNBIoT.getInstance().getCI());

			System.out.println("RSRP " + TiNBIoT.getInstance().getRSRP());

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		Delay.msDelay(10000);
		gw400.blueLED().turnOff();

		System.out.println("Exiting ...");

	}

}
