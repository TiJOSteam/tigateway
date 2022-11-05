package tigw200;
import java.io.IOException;

import tigateway.TiGW200;
import tijos.framework.platform.lte.TiLTE;
import tijos.framework.platform.lte.TiLTECell;
import tijos.framework.platform.network.NetworkException;
import tijos.framework.platform.network.NetworkInterface;
import tijos.framework.util.Delay;

/**
 * LTE 4G 网络例程
 * 
 * @author Administrator
 *
 */
public class LTENetworkSample {

	public static void main(String[] args)  {

		// 获取TiGW200对象并启动看门狗
		TiGW200 gw200 = TiGW200.getInstance();

		// 启动4G网络,30秒超时, startup执行完成即连接成功，如果连接失败将通过IOException抛出异常
		// 网络事件通过事件通知
		try {
			TiLTE.getInstance().startup(30);
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

		if (TiLTE.getInstance().getNetworkStatus() != NetworkInterface.NETSTATUS_CONNECTED) {
			System.out.println("Failed to startup LTE network.");
		}
		else
		{
			System.out.println("Connected to LTE.");		
		}

		// 注网成功 蓝灯亮
		gw200.blueLED().turnOn();

		try {
			// 4G设备唯一ID
			System.out.println("IMEI " + TiLTE.getInstance().getIMEI());

			// 4G信号强度
			System.out.println("RSSI " + TiLTE.getInstance().getRSSI());

			// SIM卡IMSI
			System.out.println("IMSI " + TiLTE.getInstance().getIMSI());

			// SIM卡ICCID编号
			System.out.println("ICCID " + TiLTE.getInstance().getICCID());

			// 基站信息 可用于基站定位
			TiLTECell cellInfo = TiLTE.getInstance().getCellInfo();

			System.out.println("CI " + cellInfo.getCI());
			System.out.println("LAC" + cellInfo.getLAC());
			System.out.println("MCC " + cellInfo.getMCC());
			System.out.println("MNC " + cellInfo.getMNC());

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// 注网成功 蓝灯亮
		gw200.blueLED().turnOff();

		Delay.msDelay(10000);

		System.out.println("Exiting ...");

	}

}
