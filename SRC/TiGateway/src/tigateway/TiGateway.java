package tigateway;

import java.io.IOException;

import tigateway.peripheral.TiLED;
import tigateway.peripheral.TiVout;
import tigateway.serialport.TiSerialPort;
import tijos.framework.util.logging.Logger;

public abstract class TiGateway {

	private TiLED blueLED = new TiLED(0);
	private TiLED greenLED = new TiLED(1);

	/**
	 * 自动根据型号获取网关对象
	 * 
	 * @return
	 */
	public static TiGateway getInstance() {
		String model = System.getProperty("hardware.oem");
		Logger.info("TiGateway", model);
		switch (model) {
		case "tigw100":
			return TiGW100.getInstance();
		case "tigw1000":
			return TiGW1000.getInstance();
		case "tigw260":
			return TiGW260.getInstance();
		case "tigw400":
			return TiGW400.getInstance();
		case "tigw50":
			return TiGW50.getInstance(); 
		case "tigw500":
			return TiGW500.getInstance();
		default:
			return TiGW200.getInstance();
		}

	}

	/**
	 * 获取串口通道
	 * 
	 * @param id         串口通道ID
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位
	 * @return
	 * @throws IOException
	 */
	public abstract TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)
			throws IOException;

	/**
	 * 继电器控制
	 * 
	 * @param id  继电器id
	 * @param ctl 0 : OFF 1 : ON
	 * @throws IOException
	 */
	public void relayControl(int id, int ctl) throws IOException {
		throw new IOException("not supported");
	}

	public void digitalOpen(int id, boolean in) throws IOException {
		throw new IOException("not supported");		
	}
	
	/**
	 * 数字输出控制 DO
	 * 
	 * @param id  id
	 * @param ctl 0:OFF 1:ON
	 * @throws IOException
	 */
	public void digitalOutput(int id, int ctl) throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * 数字输入 DI
	 * 
	 * @param id id
	 * @return 0:OFF 1:ON
	 * @throws IOException
	 */
	public int digitalInput(int id) throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * 电池环输入
	 * @param id  id
	 * @return  电流值 mA
	 * @throws IOException
	 */
	public int currentLoopInput(int id) throws IOException {
		throw new IOException("not supported");		
	}

	/**
	 * 电流环输出
	 * @param id
	 * @param value
	 * @throws IOException
	 */
	public void currentLoopOutput(int id, int value) throws IOException {
		throw new IOException("not suported");
	}
	
	/**
	 * 可控电源输出
	 * 
	 * @return
	 * @throws IOException
	 */
	public TiVout vout() throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * 蓝色灯
	 * 
	 * @return
	 */
	public TiLED blueLED() {
		return blueLED;
	}

	/**
	 * 绿色灯
	 * 
	 * @return
	 */
	public TiLED greenLED() {
		return greenLED;
	}

}
