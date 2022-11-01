package tigw200.dlt645;

import tigateway.TiGW200;
import tigateway.protocol.dlt645.TiDLT645_2007;
import tigateway.serialport.TiSerialPort;
import tigateway.utils.Helper;
import tijos.framework.util.Formatter;

public class DLT645Sample {

	TiSerialPort serialPort;
	TiDLT645_2007 dlt645;

	MeterData meterData = new MeterData();

	public DLT645Sample()  {
		try {
			serialPort = TiGW200.getInstance().getRS485(2400, 8, 1, 2); // 2400 8 1 偶校验
			dlt645 = new TiDLT645_2007(serialPort);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void loadElemeterData() {

		try {
			byte[] sn = dlt645.readMeterAddress();
			meterData.meterSN = Formatter.toHexString(sn);

			System.out.println("sn " + meterData.meterSN);

			dlt645.setMeterAddress(sn);

			System.out.println("DLT645_TAG_TOTAL_ENERGY_POWER");

			byte[] data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_TOTAL_ENERGY_POWER);
			meterData.energy = Helper.BCD2Double(data, 2);

			System.out.println("DLT645_TAG_GRID_PHASE_VOLTAGE");

			// 表读数-电压
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_A);
			meterData.voltage = Helper.BCD2Double(data, 1);

			System.out.println("DLT645_TAG_GRID_PHASE_CURRENT");

			// 表读数-电流
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_A);
			meterData.current = Helper.BCD2Double(data, 3);

			System.out.println("DLT645_TAG_GRID_PHASE_POWER_TOTAL");

			// 表读数-瞬时总有功功率
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_POWER_TOTAL);
			meterData.power = Helper.BCD2Double(data, 4);

			System.out.println("DLT645_TAG_INTERVAL_TEMPERATURE");

			// 温度, 表内温度最高位0表示零上，1表示零下。取值范围：0.0～799.9。
			int sign = 1;
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_INTERVAL_TEMPERATURE);
			if ((data[1] & 0x80) > 0) {
				sign = -1;
				data[1] &= (~0x80);
			}
			meterData.temperature = Helper.BCD2Double(data, 1) * sign;

			System.out.println("DLT645_TAG_STATUS_ACTIVE_POWER_4");

			// 电表运行状态字4（A相故障状态）
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_4);
			meterData.phaseState = data[0];

			// B相故障状态
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_5);
			meterData.phaseState += data[0];

			// C相故障状态
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_6);
			meterData.phaseState += data[0];

			// 合相故障状态
			data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_7);
			meterData.phaseState += data[0];

			// 如果是单相表， 在读取BC电压电流时会出错
			try {
				data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_B);
				meterData.voltageB = Helper.BCD2Double(data, 1);

				data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_C);
				meterData.voltageC = Helper.BCD2Double(data, 1);

				data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_B);
				meterData.currentB = Helper.BCD2Double(data, 3);

				data = this.dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_C);
				meterData.currentC = Helper.BCD2Double(data, 3);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("meterData " + this.meterData);

	}

	public static void main(String[] args) {

		DLT645Sample dlt645sample = new DLT645Sample();
		dlt645sample.loadElemeterData();

	}

}
