package tigateway;

import java.io.IOException;

import tigateway.peripheral.TiVout;
import tigateway.serialport.TiSerialPort;

/**
 * TiGW260 Cat1 可编程网关 支持 网络： LTE Cat1 端口： 1个RS485 2个LED灯： 蓝色灯 绿色灯 红色电源灯  1路可控电源输出 1路TTS音频输出
 * 
 * @author lemon
 *
 */
public class TiGW260 extends TiGateway{

	private TiSerialPort rs485 = null;
	private TiVout vout = null;

	static final int uartId = 1;
	static final int duplexGpio = 10;
	static final int voutGpio = 11;

	private static TiGW260 instance;

	private TiGW260() {

	}

	public static TiGW260 getInstance() {
		if (instance == null) {
			instance = new TiGW260();
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
			rs485 = new TiSerialPort(uartId);
			rs485.open(baudRate, dataBitNum, stopBitNum, parity);
			rs485.setRS485DuplexLine(0, duplexGpio);
		}

		return rs485;
	}

	/**
	 * 获取串口通道 
	 * @param id  0 - RS485
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

	/**
	 * 可控电源输出
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public TiVout vout() throws IOException {
		if (this.vout == null) {
			this.vout = new TiVout(voutGpio);
		}
		return this.vout;
	}

}
