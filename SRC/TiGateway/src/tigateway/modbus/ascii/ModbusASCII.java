package tigateway.modbus.ascii;


import tigateway.modbus.Modbus;
import tigateway.serialport.TiSerialPort;

/**
 * MODBUS RTU driver for TiJOS based on https://github.com/sp20/modbus-mini
 *
 * @author TiJOS
 */
public class ModbusASCII extends Modbus {


    /**
     * Initialize with serial port and default time out (2000ms) and pause 5ms after write
     *
     * @param serialPort serial port
     */
    public ModbusASCII(TiSerialPort serialPort) {
        this(serialPort, 2000);
    }

    /**
     * Initialize modbus client with serial port
     *
     * @param serialPort serial port
     * @param timeout    read timeout
     * @param pause      pause after send data
     */
    public ModbusASCII(TiSerialPort serialPort, int timeout) {
        AscIITransportUART rtu = new AscIITransportUART(serialPort, timeout);
        setTransport(rtu);
    }

    
}
