package tigw200.dlt645;

public class MeterData {

	// 表号
	public String meterSN = "";

	// 表读数-总电能
	public double energy;

	// 表读数-电压
	public double voltage;

	// 表读数-电流
	public double current;
	
	// 表读数-B相电压
	public double voltageB;

	// 表读数-B相电流
	public double currentB;

	// 表读数-C相电压
	public double voltageC;

	// 表读数-C相电流
	public double currentC;


	// 表读数-瞬时总有功功率
	public double power;

	// 开关闸状态
	public int switchstate;

	//上一次开关阀状态
	public int pre_switchstate;

	// 表内温度
	public double temperature;

	//电表运行状态字（ABC相故障状态）
	public int phaseState;
 
	@Override
	public String toString() {
		return "energy " + this.energy + " voltage " + this.voltage + " current " + this.current + " power "
				+ this.power + " switch " + this.switchstate + " temp " + this.temperature  + " sn " + this.meterSN + " phaseState " + this.phaseState
				+ " voltageB " + this.voltageB + " currentB " + this.currentB 
				+ " voltageC " + this.voltageC + " currentC " + this.currentC  ;
	}
}
