package tigateway.serialport;

import java.io.IOException;

import tijos.framework.devicecenter.TiUART;
import tijos.framework.util.Delay;
import tijos.framework.util.Formatter;
import tijos.framework.util.logging.Logger;

/**
 * Serial Port based on UART for TiJOS, support RS232 and RS485
 *
 * @author TiJOS
 */
public class TiSerialPort {

	private TiUART uart;

	/**
	 * Initialize TiSerialPort with UART and GPIO
	 *
	 * @param uartPort UART port id
	 * @param gpioPort GPIO port id, GPIO should be specified for RS485
	 * @param gpioPin  GPIO pin id
	 * @throws IOException
	 */
	public TiSerialPort(int uartPort) throws IOException {
		uart = TiUART.open(uartPort);
	}

	/**
	 * Open with communication parameters
	 *
	 * @param baudRate
	 * @param dataBitNum
	 * @param stopBitNum
	 * @param parity
	 * @throws IOException
	 */
	public void open(int baudRate, int dataBitNum, int stopBitNum, int parity) throws IOException {

		// UART通讯参数
		uart.setWorkParameters(dataBitNum, stopBitNum, parity, baudRate);
	}

	/**
	 * Set RS485 duplex line for TX/RX switch , should be called after open
	 * 
	 * @param gpioPort
	 * @param pin
	 * @throws IOException
	 */
	public void setRS485DuplexLine(int gpioPort, int pin) throws IOException {
		uart.setHalfDuplexLine(gpioPort, pin);
	}

	/**
	 * Close
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.uart.close();
	}

	public TiUART getUart() {
		return this.uart;
	}

	/**
	 * Clear UART buffer
	 *
	 * @throws IOException
	 */
	public void clearInput() throws IOException {

		this.uart.clear(TiUART.BUFF_READ);
	}

	/**
	 * Available
	 * 
	 * @return
	 * @throws IOException
	 */
	public int available() throws IOException {
		return this.uart.available();
	}

	/**
	 * Write data to the uart
	 *
	 * @param buffer
	 * @param start
	 * @param length
	 * @throws IOException
	 */
	public void write(byte[] buffer, int start, int length) throws IOException {
		this.uart.write(buffer, start, length);
	}

	public void write(byte[] buffer) throws IOException {
		this.uart.write(buffer, 0, buffer.length);
	}

	/**
	 * Read data from uart
	 *
	 * @return data or null
	 * @throws IOException
	 */
	public byte[] read() throws IOException {
		int avail = this.uart.available();
		if (avail <= 0)
			return null;

		byte[] buffer = new byte[avail];
		this.uart.read(buffer, 0, avail);

		return buffer;
	}

	/**
	 * read expected data within timeout
	 * 
	 * @param buffer  buffer to store data
	 * @param timeOut timeout in milliseconds
	 * @return actual received data length
	 * @throws IOException
	 */
	public int read(byte[] buffer, int timeOut) throws IOException {
		return this.read(buffer, 0, buffer.length, timeOut);
	}

	/**
	 * read expected data within timeout
	 * 
	 * @param buffer  buffer to store data
	 * @param start   start offset of the buffer
	 * @param length  expected read data length
	 * @param timeOut timeout in milliseconds
	 * @return actual received data length
	 * @throws IOException
	 */
	public int read(byte[] buffer, int start, int length, int timeOut) throws IOException {

		if (start + length > buffer.length) {
			throw new IOException("invalid parameters");
		}

		long now = System.currentTimeMillis();
		long deadline = now + timeOut;
		int offset = start;
		int bytesToRead = length;
		int res = 0;
		while ((now < deadline) && (bytesToRead > 0)) {
			res = this.uart.read(buffer, offset, bytesToRead);
			if (res <= 0) {
				Delay.msDelay(10);
				now = System.currentTimeMillis();
				continue;
			}

			offset += res;
			bytesToRead -= res;
			if (bytesToRead > 0) // only to avoid redundant call of System.currentTimeMillis()
				now = System.currentTimeMillis();
		}
		res = length - bytesToRead; // total bytes read
		if (res < length) {
			Logger.info("TiSerialPort", "Read timeout(incomplete): " + Formatter.toHexString(buffer, start, res, ""));
		}

		return res;
	}

	/**
	 * Read all data within the time
	 *
	 * @param msec read all data within the time interval if there are data
	 * @return
	 * @throws IOException
	 */
	public byte[] read(int msec) throws IOException {

		byte[] buffer = new byte[512];

		int num = 0;
		int left = buffer.length;

		// read ms to get all data
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < msec) {
			int len = this.uart.read(buffer, num, left);
			if (len <= 0) {
				Delay.msDelay(50);
				continue;
			}

			num += len;
			left -= len;

			if (left == 0)
				break;
		}

		if (num == 0) {
			return null;
		}

		byte[] newBuff = new byte[num];
		System.arraycopy(buffer, 0, newBuff, 0, num);

		return newBuff;
	}

	/**
	 * Read data into buffer from the UART
	 *
	 * @param start
	 * @param length
	 * @param modbusClient
	 * @return
	 * @throws IOException
	 */
	public boolean readToBuffer(byte[] buffer, int start, int length, int timeOut) throws IOException {

		long now = System.currentTimeMillis();
		long deadline = now + timeOut;
		int offset = start;
		int bytesToRead = length;
		int res;
		while ((now < deadline) && (bytesToRead > 0)) {
			res = this.uart.read(buffer, offset, bytesToRead);
			if (res <= 0) {
				Delay.msDelay(10);
				now = System.currentTimeMillis();
				continue;
			}

			offset += res;
			bytesToRead -= res;
			if (bytesToRead > 0) // only to avoid redundant call of System.currentTimeMillis()
				now = System.currentTimeMillis();
		}
		res = length - bytesToRead; // total bytes read
		if (res < length) {
			Logger.info("TiSerialPort",
					"Read timeout(incomplete): " + Formatter.toHexString(buffer, start, start + res, ""));

			return false;
		} else
			return true;
	}

	/**
	 * read data into buffer immediately from serial port  
	 * @param buffer  buffer to store data 
	 * @param start start offset of buffer 
	 * @param length  length to read 
	 * @return  data length from serial port 
	 * @throws IOException
	 */
	public int readToBuffer(byte[] buffer, int start, int length) throws IOException {
		return this.uart.read(buffer, start, length);
	}

}
