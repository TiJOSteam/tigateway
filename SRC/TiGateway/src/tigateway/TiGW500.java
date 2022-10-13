package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiADC;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.util.Delay;

public class TiGW500 extends TiGateway {

	private TiSerialPort rs485 = null;
	private TiSerialPort[] ttl = new TiSerialPort[2];

	private TiGPIO relay = null;
	private TiGPIO[] digitalInput = new TiGPIO[2];

	private TiADC[] currentLoopIn = new TiADC[2];

	private TiI2CMaster currentLoopOut = null;

	private static TiGW500 instance;

	public static TiGW500 getInstance() {
		if (instance == null) {
			instance = new TiGW500();
		}

		return instance;
	}

	/**
	 * 获取RS485端口 数据位8 停止位1
	 * 
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getRS485(int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {
		if (rs485 == null) {
			rs485 = new TiSerialPort(1);
			rs485.open(baudRate, dataBitNum, stopBitNum, parity);
			rs485.setRS485DuplexLine(0, 10);
		}
		return rs485;
	}

	/**
	 * 获取TTL端口 支持2个，id分别为0,1
	 * 
	 * @param id
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getTTL(int id, int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {
		if (id < 0 || id > 1) {
			throw new IOException("unsuported id");
		}

		if (ttl[id] == null) {
			ttl[id] = new TiSerialPort(id + 2);
			ttl[id].open(baudRate, dataBitNum, stopBitNum, parity);
		}
		return ttl[id];
	}

	/**
	 * 通过串口获取通道
	 * 
	 * @param id         0 RS485 2/3 TTL
	 * @param baudRate
	 * @param dataBitNum
	 * @param stopBitNum
	 * @param parity
	 * @return
	 * @throws IOException
	 */
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)
			throws IOException {

		switch (id) {
		case 0:
			return this.getRS485(baudRate, dataBitNum, stopBitNum, parity);
		case 2:
			return this.getTTL(0, baudRate, dataBitNum, stopBitNum, parity);
		case 3:
			return this.getTTL(1, baudRate, dataBitNum, stopBitNum, parity);
		case 1: //not used
		default:
			throw new IOException("unsuported id");
		}
	}

	/**
	 * 继电器控制 支持1个， id分别为0
	 * 
	 * @param id  继电器id
	 * @param ctl 0 : OFF 1 : ON
	 * @throws IOException
	 */
	@Override
	public void relayControl(int id, int ctl) throws IOException {
		if (id != 0) {
			throw new IOException("unsuported id");
		}

		int pinId = 4;
		if (this.relay == null) {
			TiGPIO gpio = TiGPIO.open(0, pinId);
			gpio.setWorkMode(pinId, TiGPIO.OUTPUT_PP);
			this.relay = gpio;
		}

		this.relay.writePin(pinId, ctl);
	}

	/**
	 * 数字输入 DI (支持2个, id分别为0、1)
	 * 
	 * @param id id
	 * @return 0:OFF 1:ON
	 * @throws IOException
	 */
	@Override
	public int digitalInput(int id) throws IOException {
		if (id < 0 || id > 1) {
			throw new IOException("unsuported id");
		}

		int pinId = id + 11;
		if (this.digitalInput[id] == null) {
			TiGPIO gpio = TiGPIO.open(0, pinId);
			gpio.setWorkMode(pinId, TiGPIO.INPUT_FLOATING);
			this.digitalInput[id] = gpio;
		}

		// DI取反
		if (this.digitalInput[id].readPin(pinId) > 0)
			return 0;

		return 1;
	}

	/**
	 * 电流环输入 (支持2个, id分别为0, 1)
	 * 
	 * @param id
	 * @return uA
	 * @throws IOException
	 */
	@Override
	public int currentLoopInput(int id) throws IOException {
		
		if(id < 0 || id> 2) {
			throw new IOException("unsupported id");
		}
		
		int index = id + 2;
		if(this.currentLoopIn[id] == null) {
			this.currentLoopIn[id] = TiADC.open(0, index);
		}
		
		int value = this.currentLoopIn[id].getMilliVoltage(index);
		
		
		value = (int) ((value / 150.0f) * 1000);
		
		return value;
	}

	/**
	 * 电流环输出 (支持2个, id分别为0, 1)
	 * 
	 * @param id
	 * @param uA  输出值为uA
	 * @return
	 * @throws IOException
	 */
	@Override
	public void currentLoopOutput(int id, int uA) throws IOException {

		if (this.currentLoopOut == null) {
			currentLoopOut = TiI2CMaster.open(2);
		}

		short value = (short) ((uA /20000.0f) * 4095); 
		
		byte hi = (byte)((value & 0xF) << 4);
		byte lo = (byte)((value >>> 4) & 0xff);
		
		byte[] data = {lo, hi};
				
		
		switch (id) {
		case 0:
			this.currentLoopOut.write(0x59, 0x02, data, 0, data.length);
			break;
		case 1:
			this.currentLoopOut.write(0x5A, 0x02, data, 0, data.length);
			break;
		default:
			break;
		}

	}
 
	public static final void main(String [] args) throws IOException {
		
		TiGW500 gw500 = TiGW500.getInstance();
		

	//	gw500.relayControl(0, 1);
//		
////		for(int i = 0 ; i< 3; i ++) {
////			gw500.blueLED().turnOn();
////			Delay.msDelay(1000);
////			gw500.blueLED().turnOff();
////			Delay.msDelay(1000);
////		
////			gw500.greenLED().turnOn();
////			Delay.msDelay(1000);
////			gw500.greenLED().turnOff();
////			Delay.msDelay(1000);
////		}
		
	//	gw500.relayControl(0, 0);

	//	gw500.currentLoopOutput(0, 20000);
		
	//	Delay.sDelay(1);
		
	//	System.out.println(" input " + gw500.currentLoopInput(0));
 
		
//		for(int i = 0;i <= 20*1000 ; i += 1000) {
//			gw500.currentLoopOutput(0, i);
//			
//			int v = gw500.currentLoopInput(0);
//			
//			System.out.println("output " + i  + " input " + v + " delta  " + ((v - i) / (float)i));
//			
//			//Delay.sDelay(8);
//			
//		}
		
		int i = 0;
		
		while(i++ < 1000) {
			int v = gw500.currentLoopInput(0);
			
			System.out.println("ad0 " + v);
			
			Delay.sDelay(1);
		}
		
		
				
		System.out.println("finished");
		
	}
	
}
