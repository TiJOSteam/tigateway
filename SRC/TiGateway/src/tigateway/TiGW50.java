package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.util.Delay;

public class TiGW50 extends TiGateway {

	private TiSerialPort[] uarts = new TiSerialPort[3];
	private TiGPIO [] gpios = new TiGPIO[4];

	private static TiGW50 instance;

	public static TiGW50 getInstance() {
		if (instance == null) {
			instance = new TiGW50();
		}

		return instance;
	}
	
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)
			throws IOException {
		return getTTL(id, baudRate, dataBitNum, stopBitNum, parity);
	}

	/**
	 * 获取ttl端口
	 * 
	 * @param id         id
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getTTL(int port, int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {

		if (port < 1 || port > 4) {
			throw new IOException("invalid uart port.");
		}
		
		int id = port - 1;

		if (uarts[id] == null) {
			TiSerialPort uart = new TiSerialPort(port);
			uart.open(baudRate, dataBitNum, stopBitNum, parity);

			uarts[id] = uart;
		}

		return uarts[id];
	}

	/**
	 * 初始化GPIO  
	 * @param id  GPIO id 5 10 11 12
	 * @param in  true: GPIO浮空输入方式  false: GPIO推挽输出 
	 */
	@Override
	public void digitalOpen(int id, boolean in) throws IOException {
		int pin = this.getGPIOPin(id);
		TiGPIO gpio = TiGPIO.open(0, id);
		if(in) {
			gpio.setWorkMode(id, TiGPIO.INPUT_FLOATING);
		}else {
			gpio.setWorkMode(id, TiGPIO.OUTPUT_PP);			
		}
		
		this.gpios[pin] = gpio;		
	}

	
	/**
	 * GPIO输出控制 DO
	 * 
	 * @param id  id  GPIO id 5 10 11 12
	 * @param ctl 0:OFF 1:ON
	 * @throws IOException
	 */
	@Override
	public void digitalOutput(int id, int ctl) throws IOException {
		int pin = this.getGPIOPin(id);
		
		TiGPIO gpio =this.gpios[pin];
		
		if(gpio == null) {
			throw new IOException("invalid id or not initialized.");
		}
		
		gpio.writePin(id, ctl);
	}

	/**
	 * GPIO输入 DI
	 * 
	 * @param id  GPIO id 5 10 11 12
	 * @return 0:OFF 1:ON
	 * @throws IOException
	 */
	@Override
	public int digitalInput(int id) throws IOException {
		int pin = this.getGPIOPin(id);
		
		TiGPIO gpio =this.gpios[pin];
		
		if(gpio == null) {
			throw new IOException("invalid id or not initialized.");
		}
		
		return gpio.readPin(id);
	}

	/**
	 * 根据ID打开GPIO
	 * @param id
	 * @return
	 * @throws IOException
	 */
	private int  getGPIOPin(int id) throws IOException {
		
		int pin = 0;
		switch(id ) {
		case 5:
			pin = 0;
			break;
		case 10:
			pin = 1;
			break;
		case 11:
			pin =2;
			break;
		case 12:
			pin = 3;
			break;
		default:
			throw new IOException("unsupported gpio id");
		}
		
		return pin;
	}
	
//	public static void main(String [] argv) throws IOException {
//		
//		TiGateway gw50 = TiGateway.getInstance();
//		
//		gw50.blueLED().turnOn();
//		Delay.sDelay(2);
//		
//		
//		gw50.blueLED().turnOff();
//		Delay.sDelay(2);
//		
//		gw50.greenLED().turnOn();
//		Delay.sDelay(2);
//		
//		gw50.greenLED().turnOff();
//		Delay.sDelay(2);
//
//		
//		gw50.digitalOpen(5, false);
//		gw50.digitalOpen(10, false);
//		gw50.digitalOpen(11, false);
//		gw50.digitalOpen(12, false);
//
//		for(int i = 0;i < 10; i ++) {
//			gw50.digitalOutput(5, 1);
//			gw50.digitalOutput(10, 1);
//			gw50.digitalOutput(11, 1);
//			gw50.digitalOutput(12, 1);
//			Delay.msDelay(2000);
//			gw50.digitalOutput(5, 0);
//			gw50.digitalOutput(10, 0);
//			gw50.digitalOutput(11, 0);
//			gw50.digitalOutput(12, 0);
//			
//			Delay.msDelay(2000);
//		}
//			
//		
//		TiSerialPort sp1 = gw50.getSerialPort(1, 9600, 8, 1 ,0);
//		
//		sp1.write("this is port1".getBytes());
//		System.out.println(new String(sp1.read(3000)));
//		
//		TiSerialPort sp2 = gw50.getSerialPort(2, 9600, 8, 1 ,0);
//		sp2.write("this is port2".getBytes());
//		System.out.println(new String(sp2.read(3000)));
//		
//		
//		TiSerialPort sp3 = gw50.getSerialPort(3, 9600, 8, 1 ,0);
//		sp3.write("this is port3".getBytes());
//		System.out.println(new String(sp3.read(3000)));
//				
//		
//	}

}
