package tigw260;

import java.io.IOException;

import java.net.Socket;

import tigateway.TiGW260;
import tigateway.serialport.TiSerialPort;
import tijos.framework.devicecenter.TiUART;
import tijos.framework.platform.lte.TiLTE;
import tijos.framework.util.Delay;

/**
 * TCP Socket 与RS485透传 例程， 在运行时请设置正确的TCP Server IP地址
 *
 * @author TiJOS
 */
public class Rs485toTcpApp extends Thread {

	// TCP服务器IP及PORT
	String host = "";
	int port;

	Socket client = null;
	TiSerialPort rs485 = null;
	byte[] data = new byte[512];

	public Rs485toTcpApp(String host, int port, TiSerialPort rs485) {
		this.host = host;
		this.port = port;
		this.rs485 = rs485;
	}

	/**
	 * 读网络数据线程
	 */
	@Override
	public void run() {

		while (true) {
			if(this.client == null) {
				this.connect();
			}

			tcp2rs485();
			Delay.msDelay(200);
		}
	}

	/**
	 * 接收网络数据下发RS485
	 */
	public void tcp2rs485() {
		if (this.client == null || this.rs485 == null) {
			return;
		}

		try {
			int len = this.client.getInputStream().read(data, 0, data.length);
			if (len > 0) {
				System.out.println("tcp data arrvied " + len);
				this.rs485.write(data, 0, len);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			// reconnect
			reconnect();
		}

	}

	/**
	 * 读取485数据上报网络
	 */
	public void rs485toTcp() {
		try {
			byte[] receive = rs485.read(1000);
			// 未读到数据
			if (receive == null) {
				// System.out.println("no data from rs485.");
			} else {
				// HEX转成字符串打印
				System.out.println("data arrived len : " + receive.length);

				int ret = sendDataToServer(receive);

				// 发送失败重连服务器重试
				if (0 != ret) {
					ret = sendDataToServer(receive);
				}

				if (ret != 0) {
					System.out.println("send error");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 发数据到服务器 如果失败则断开连接 下次发送时自动连接
	 * 
	 * @param data
	 * @return
	 */
	int sendDataToServer(byte[] data) {
		connect();
		if (client == null) {
			return -1; // connect error
		}

		try {
			client.getOutputStream().write(data, 0, data.length);
		} catch (IOException ex) {
			ex.printStackTrace();
			close();
			return -2;
		}

		return 0;
	}

	void connect() {
		if (client == null) {
			// Connect to the server with TCP
			try {
				client = new Socket(host, port);
				System.out.println("connected " + host);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to connect TCP server");
			}
		}

	}

	void close() {
		if (this.client != null) {
			try {
				this.client.close();
			} catch (Exception ex) {
			}

			this.client = null;
		}

	}

	/**
	 * 重新连接网络
	 */
	void reconnect() {
		close();
		connect();
	}

	public static void main(String[] args) {

		// TCP服务器IP及PORT
		String host = "tcp.ticloud.io";
		int port = 9876;

		try {
			// 启动LTE网络
			TiLTE.getInstance().startup(20);
			// 获取TiGW2xx对象并启动看门狗
			TiGW260 gw260 = TiGW260.getInstance();

			// 获取第0路RS485 9600 8 1 N
			TiSerialPort rs485 = gw260.getRS485(9600, 8, 1, TiUART.PARITY_NONE);
			Rs485toTcpApp rs485tcp = new Rs485toTcpApp(host, port, rs485);

			// 连接服务器
			rs485tcp.connect();

			// 启动读网络数据线程
			rs485tcp.start();

			while (true) {
				// 读485上传
				rs485tcp.rs485toTcp();
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		System.exit(0);

	}

}
