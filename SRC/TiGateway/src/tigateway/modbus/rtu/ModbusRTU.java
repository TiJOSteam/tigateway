package tigateway.modbus.rtu;

import tigateway.modbus.Modbus;
import tigateway.serialport.TiSerialPort;

/**
 * MODBUS RTU driver for TiJOS based on https://github.com/sp20/modbus-mini
 *
 * @author TiJOS
 */
public class ModbusRTU extends Modbus {

	/**
	 * Initialize with serial port and default time out (2000ms) and pause 5ms after
	 * write
	 *
	 * @param serialPort serial port
	 */
	public ModbusRTU(TiSerialPort serialPort) {
		this(serialPort, 2000);
	}

	/**
	 * Initialize modbus client with serial port
	 *
	 * @param serialPort serila port
	 * @param timeout    read timeout
	 * @param pause      pause after send data
	 */
	public ModbusRTU(TiSerialPort serialPort, int timeout) {
		RtuTransportUART rtu = new RtuTransportUART(serialPort, timeout);
		setTransport(rtu);

	}

}
