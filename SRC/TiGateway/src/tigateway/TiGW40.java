package tigateway;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;

public class TiGW40 extends TiGateway {

	private TiSerialPort uart = null;

	private static TiGW40 instance;

	public static TiGW40 getInstance() {
		if (instance == null) {
			instance = new TiGW40();
		}

		return instance;
	}
	
	@Override
	public TiSerialPort getSerialPort(int id, int baudRate, int dataBitNum, int stopBitNum, int parity)
			throws IOException {

		int port = id + 2;
		
		if(uart== null) {
			TiSerialPort serialPort = new TiSerialPort(port);
			serialPort.open(baudRate, dataBitNum, stopBitNum, parity);
			
			this.uart = serialPort;
		}

		return this.uart;
	}
	
	

}
