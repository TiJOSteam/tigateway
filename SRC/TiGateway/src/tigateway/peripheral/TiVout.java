package tigateway.peripheral;

import java.io.IOException;

import tijos.framework.devicecenter.TiGPIO;

/**
 * 可控电源输出
 * 
 * @author Administrator
 *
 */
public class TiVout {

	TiGPIO voutGpio = null;
	int gpioPin;

	public TiVout(int gpioPin) throws IOException {
		this.gpioPin = gpioPin;
		voutGpio = TiGPIO.open(0, gpioPin);
		voutGpio.setWorkMode(gpioPin, TiGPIO.OUTPUT_PP);
	}

	/**
	 * 打开电源
	 * 
	 * @throws IOException
	 */
	public void turnOn() throws IOException {
		voutGpio.writePin(this.gpioPin, 1);
	}

	/**
	 * 关闭电源
	 * 
	 * @throws IOException
	 */
	public void turnOff() throws IOException {
		voutGpio.writePin(this.gpioPin, 0);

	}
}
