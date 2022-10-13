package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.platform.lte.TiLTE;
import tijos.framework.platform.lte.TiLTECSQ;
import tijos.framework.platform.lte.TiLTECell;
import tijos.framework.platform.peripheral.TiLight;
import tijos.framework.util.Delay;

public class TiGW100 extends TiGateway {

	static final int rs485UartId = 1;
	static final int rs485DuplexGpio = 10;

	static final int rs232UartId = 2;

	private TiSerialPort rs485 = null;
	private TiSerialPort rs232 = null;

	private static TiGW100 instance;

	public static TiGW100 getInstance() {
		if (instance == null) {
			instance = new TiGW100();
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
	 * 获取RS232端口, 用于与其它型号产品兼容
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
			rs232 = new TiSerialPort(rs232UartId);
			rs232.open(baudRate, dataBitNum, stopBitNum, parity);
		}

		return rs232;
	}

	/**
	 * 通过串口获取通道
	 * 
	 * @param id         0 RS485 1 RS232
	 * @param baudRate
	 * @param dataBitNum
	 * @param stopBitNum
	 * @param parity
	 * @return
	 * @throws IOException
	 */
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)	throws IOException {
		if (id == 0) {
			return this.getRS485(baudRate, dataBitNum, stopBitNum, parity);
		} else {
			return this.getRS232(baudRate, dataBitNum, stopBitNum, parity);
		}
	}

	public static void main(String[] args) {
		try {

			TiLTE lte = TiLTE.getInstance();

			lte.startup(20);

			TiLight light = TiLight.getInstance();

			System.out.println("lte status. " + lte.getNetworkStatus());

			light.turnOn(0);

			System.out.println("imei=" + lte.getIMEI());
			System.out.println("imsi=" + lte.getIMSI());
			System.out.println("iccid=" + lte.getICCID());
			System.out.println("ip=" + lte.getPDPIP());
			System.out.println("apn=" + lte.getAPN());
			System.out.println("rssi=" + lte.getRSSI());

			TiLTECSQ csq = lte.getCSQ();
			System.out.println("csq.rssi=" + csq.getRSSI());

			TiLTECell cell = lte.getCellInfo();

			System.out.println("cell.mcc=" + cell.getMCC());
			System.out.println("cell.mnc=" + cell.getMNC());
			System.out.println("cell.lac=" + cell.getLAC());
			System.out.println("cell.ci=" + cell.getCI());

			System.out.println("register status=" + lte.getNetworkStatus());

			TiGW100.getInstance().greenLED().turnOn();
			
			TiSerialPort sp = TiGW100.getInstance().getSerialPort(1, 9600, 8, 1, 0);
			sp.write("this is test".getBytes());
			Delay.msDelay(100);
			String resp = new String(sp.read(1000));
			
			System.out.println("resp " +resp);
			
			Delay.msDelay(1000);

			TiGW100.getInstance().greenLED().turnOff();
			
			

			System.exit(0);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
