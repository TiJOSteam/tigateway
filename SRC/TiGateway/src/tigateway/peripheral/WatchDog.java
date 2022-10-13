package tigateway.peripheral;

import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.util.Delay;

public class WatchDog extends Thread {

	boolean wdtRunning = true;
	int pinId = 5;
	TiGPIO wdtPin;

	@Override
	public void run() {
		this.setDaemon(true);
		while (true) {
			try {
				wdtPin.writePin(pinId, 0);
				Delay.msDelay(1000);
				wdtPin.writePin(pinId, 1);
				Delay.msDelay(1000);				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void init() {
		try {
			
			if(wdtPin != null)
				return;
			
			wdtPin = TiGPIO.open(0, pinId);
			wdtPin.setWorkMode(pinId, TiGPIO.OUTPUT_PP);
			
			this.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
