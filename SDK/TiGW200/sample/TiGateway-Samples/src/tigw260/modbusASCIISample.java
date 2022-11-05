package tigw260;
import java.io.IOException;

import tigateway.TiGW260;
import tigateway.modbus.ascii.ModbusASCII;
import tigateway.modbus.rtu.ModbusRTU;
import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiUART;
import tijos.framework.util.Delay;

/**
 * MOBDBUS RTU 例程
 * @author TiJOS
 *
 */
public class modbusASCIISample {
	public static void main(String[] args) {

		try {
			System.out.println("This is a modbus rtu sample.");

			// 获取TiGW200对象并启动看门狗
			TiGW260 gw260 = TiGW260.getInstance();

			//如果MODBUS设备使用vout电源输出需要写打开电源
			gw260.vout().turnOn();

			// 获取第0路RS485 9600 8 1 N
			TiSerialPort rs485 = gw260.getRS485(9600,8,1,TiUART.PARITY_NONE);

			// MODBUS ASCII
			// 通讯超时500 ms
			ModbusASCII modbusAsc = new ModbusASCII(rs485, 500);

			// MODBUS 数据处理
			// 每5秒进行一次数据处理同时绿灯亮一次
			while (true) {
				gw260.greenLED().turnOn();
				MonitorProcess(modbusAsc);
				gw260.greenLED().turnOff();
				Delay.msDelay(2000);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 通过RS485基于MODBUS协议读取设备数据
	 *
	 * @param modbusRtu
	 */
	public static void MonitorProcess(ModbusASCII modbusAsc) {
		try {
			// MODBUS device id 设备地址
			int deviceId = 1;
			// Input Register 开始地址
			int startAddr = 0;

			// Read 2 registers from start address 读取个数
			int count = 2;

			// 读取Holding Register
			modbusAsc.initReadHoldingsRequest(deviceId, startAddr, count);

			// 下发并获取响应
			int result = modbusAsc.execRequest();

			// 成功
			if (result == ModbusRTU.RESULT_OK) {

				int humdity = modbusAsc.getResponseInt16(startAddr, false);
				int temperature = modbusAsc.getResponseInt16(startAddr + 1, false);

				System.out.println("temp = " + temperature + " humdity = " + humdity);
			} else {
				System.out.println("Modbus error " + modbusAsc.getResultAsString());
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
