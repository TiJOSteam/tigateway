package tigateway.protocol.hostlink;

import java.util.ArrayList;
import java.util.List;

/**
 * PLC 返回数据
 * 
 * @author Administrator
 *
 */
public class Response {
	/**
	 * 设备单元地址
	 */
	public int addr;
	/**
	 * 状态符
	 */
	public String state;
	/**
	 * 命令符
	 */
	public String code;

	/**
	 * 数据
	 */
	public List<Integer> dataList = new ArrayList<>();

	@Override
	public String toString() {
		return "addr " + addr + " state " + this.state + " code " + code + " data num " + dataList.size();
	}
}
