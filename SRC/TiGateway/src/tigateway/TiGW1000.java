package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiGPIO;

/**
 * TiGW1000 可编程网关
 * 
 * @author lemon
 *
 */
public class TiGW1000 extends TiGateway {

	private TiSerialPort rs485 = null;
	private TiSerialPort rs232 = null;
	private TiSerialPort[] ttl = new TiSerialPort[2];

	private TiGPIO[] relay = new TiGPIO[2];
	private TiGPIO[] digitalOut = new TiGPIO[2];
	private TiGPIO[] digitalInput = new TiGPIO[4];

	private static TiGW1000 instance;

	private TiGW1000() {

	}

	public static TiGW1000 getInstance() {
		if (instance == null) {
			instance = new TiGW1000();
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
			rs485 = new TiSerialPort(4);
			rs485.open(baudRate, dataBitNum, stopBitNum, parity);
			rs485.setRS485DuplexLine(1, 6);
		}
		return rs485;
	}

	/**
	 * 获取RS232端口
	 * 
	 * @param baudRate   波特率
	 * @param dataBitNum 数据位
	 * @param stopBitNum 停止位
	 * @param parity     校验位 0 - 无校验 1 - 奇校验 2 - 偶校验
	 * @return
	 * @throws IOException
	 */
	public TiSerialPort getRS232(int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {
		if (rs232 == null) {
			rs232 = new TiSerialPort(5);
			rs232.open(baudRate, dataBitNum, stopBitNum, parity);
		}
		return rs232;
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
	 * @param id         0 RS485 1 RS232 2/3 TTL
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
		case 1:
			return this.getRS232(baudRate, dataBitNum, stopBitNum, parity);
		case 2:
			return this.getTTL(0, baudRate, dataBitNum, stopBitNum, parity);
		case 3:
			return this.getTTL(1, baudRate, dataBitNum, stopBitNum, parity);
		default:
			return null;
		}
	}

	/**
	 * 继电器控制 支持2个， id分别为0和1
	 * 
	 * @param id  继电器id
	 * @param ctl 0 : OFF 1 : ON
	 * @throws IOException
	 */
	@Override
	public void relayControl(int id, int ctl) throws IOException {
		if (id < 0 || id > 1) {
			throw new IOException("unsuported id");
		}

		int pinId = id + 6;
		if (this.relay[id] == null) {
			TiGPIO gpio = TiGPIO.open(2, pinId);
			gpio.setWorkMode(pinId, TiGPIO.OUTPUT_PP);
			this.relay[id] = gpio;
		}

		this.relay[id].writePin(pinId, ctl);
	}

	/**
	 * 数字输出控制 DO (支持2个，id分别0和1)
	 * 
	 * @param id  id
	 * @param ctl 0:OFF 1:ON
	 * @throws IOException
	 */
	@Override
	public void digitalOutput(int id, int ctl) throws IOException {

		if (id < 0 || id > 1) {
			throw new IOException("unsuported id");
		}

		int pinId = id + 4;
		if (this.digitalOut[id] == null) {
			TiGPIO gpio = TiGPIO.open(2, pinId);
			gpio.setWorkMode(pinId, TiGPIO.OUTPUT_PP);
			this.digitalOut[id] = gpio;
		}

		this.digitalOut[id].writePin(pinId, ctl);
	}

	/**
	 * 数字输入 DI (支持4个, id分别为0、1、2、3)
	 * 
	 * @param id id
	 * @return 0:OFF 1:ON
	 * @throws IOException
	 */
	@Override
	public int digitalInput(int id) throws IOException {
		if (id < 0 || id > 4) {
			throw new IOException("unsuported id");
		}

		int pinId = id;
		if (this.digitalInput[id] == null) {
			TiGPIO gpio = TiGPIO.open(2, pinId);
			gpio.setWorkMode(pinId, TiGPIO.INPUT_FLOATING);
			this.digitalInput[id] = gpio;
		}

		// DI取反
		if (this.digitalInput[id].readPin(pinId) > 0)
			return 0;

		return 1;
	}
}
