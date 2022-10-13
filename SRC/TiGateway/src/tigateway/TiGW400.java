package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;

public class TiGW400 extends TiGateway {

	static final int rs485UartId = 2;
	static final int rs485DuplexGpio = 10;

	private TiSerialPort rs485 = null;

	private static TiGW400 instance;

	public static TiGW400 getInstance() {
		if (instance == null) {
			instance = new TiGW400();
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

		if (rs485 == null) {
			rs485 = new TiSerialPort(rs485UartId);
			rs485.open(baudRate, dataBitNum, stopBitNum, parity);
			rs485.setRS485DuplexLine(0, rs485DuplexGpio);
		}

		return rs485;
	}

	/**
	 * 通过串口获取通道
	 * 
	 * @param id         0 RS485 
	 * @param baudRate
	 * @param dataBitNum
	 * @param stopBitNum
	 * @param parity
	 * @return
	 * @throws IOException
	 */
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)	throws IOException {
		return this.getRS485(baudRate, dataBitNum, stopBitNum, parity);
	}
}
