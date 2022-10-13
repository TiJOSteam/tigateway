package tigateway.peripheral;

import tijos.framework.platform.peripheral.TiLight;

public class TiLED {

	private int id = 0;

	public TiLED(int id) {
		this.id = id;
	}

	/**
	 * 亮灯
	 */
	public void turnOn() {
		try {
			TiLight.getInstance().turnOn(id);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 灭灯
	 */
	public void turnOff() {
		try {
			TiLight.getInstance().turnOff(id);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
