package tigateway;

import java.io.IOException;

import tigateway.peripheral.WatchDog;
import tigateway.serialport.TiSerialPort;

/**
 * TiGW200 Cat1 可编程网关 支持 网络： LTE Cat1 端口： 双RS485 3个LED灯： 蓝色灯 绿色灯 红色电源灯
 * 
 * @author lemon
 *
 */
public class TiGW200 extends TiGateway{

	private TiSerialPort[] rs485chn = new TiSerialPort[2];

	private boolean isGW210 = false;
	private static TiGW200 instance;

	private TiGW200() {

		try {
			isGW210 = false;
			String oemInfo = System.getProperty("hardware.oem");
			if (oemInfo.equals("tigw210")) {
				isGW210 = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// WDT is built in for gw210
		if (!isGW210) {
			WatchDog wdt = new WatchDog();
			wdt.init();
		}
	}

	public static TiGW200 getInstance() {
		if (instance == null) {
			instance = new TiGW200();
		}

		return instance;
	}

	/**
	 * 获取第1个通道RS485端口, 用于与其它型号产品兼容
	 * 
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getRS485(int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {
		return this.getRS485ById(0, baudRate, dataBitNum, stopBitNum, parity);
	}

	/**
	 * 获取指定通道RS485端口, 多通道485时请使用此接口
	 * 
	 * @param id         通道 0或通道 1
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getRS485ById(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)
			throws IOException {
		if (id < 0 || id > 1) {
			return null;
		}

		if (rs485chn[id] == null) {
			int uartId = getUartId(id);

			TiSerialPort rs485 = new TiSerialPort(uartId);
			rs485.open(baudRate, dataBitNum, stopBitNum, parity);
			
			if(this.isGW210) {
				if(id == 0) {
					rs485.setRS485DuplexLine(0, 10);
				}else 
				{
					rs485.setRS485DuplexLine(0, 12);
				}
			}
			
			rs485chn[id] = rs485;
		}

		return rs485chn[id];
	}
	
	/**
	 * 获取串口通道
	 * @param id  0 RS485-1  1 RS485-2
	 * @param baudRate
	 * @param dataBitNum
	 * @param stopBitNum
	 * @param parity
	 * @return
	 * @throws IOException
	 */
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)	throws IOException {
		return getRS485ById(id, baudRate, dataBitNum, stopBitNum, parity);
	}


	/**
	 * uart id: tigw200 : 4 and 5 , tigw210 : 1 and 3
	 * @param chn
	 * @return
	 */
	private int getUartId(int chn) {
		int uartId = 0;
		if (isGW210) {
			uartId = 1;
			if (chn == 1) {
				uartId = 3;
			}

		} else {
			uartId = 4;
			if (chn == 1) {
				uartId = 5;
			}

		}
		return uartId;
	}
}
