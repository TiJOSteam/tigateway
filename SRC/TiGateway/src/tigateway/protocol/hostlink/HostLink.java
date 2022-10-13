package tigateway.protocol.hostlink;

import java.io.IOException;

import tigateway.serialport.TiSerialPort;
import tijos.framework.util.Formatter;

public class HostLink {

	public static String HOSTlINK_PREFIX = "@";
	public static String HOSTlINK_SUFFIX = "*\r";

	TiSerialPort serialPort;

	String sendCode = "";

	public HostLink(TiSerialPort serialPort) {
		this.serialPort = serialPort;
	}
	
	/**
	 * 读操作
	 * @param addr  PLC单元地址
	 * @param cmd   方法码
	 * @param startAddr 开始地址
	 * @param length 长度
	 * @return
	 * @throws IOException
	 */
	public Response readRequest(int addr, String cmd, int startAddr, int length) throws IOException
	{
		this.sendRead(addr, cmd, startAddr, length);
		return this.receive();
	}
	
	/**
	 * 写操作
	 * @param addr PLC单元地址
	 * @param cmd  方法码
	 * @param startAddr 开始地址
	 * @param value 值 
	 * @return
	 * @throws IOException
	 */
	public Response writeRequest(int addr, String cmd, int startAddr, int value[]) throws IOException {
		this.sendWrite(addr, cmd, startAddr, value);
		return this.receive();
	}


	/**
	 * @param addr        PLC单元地址
	 * @param cmd        方法码
	 * @param startOffset 起始寻址地址
	 * @param length      长度
	 */
	private void sendRead(int addr, String cmd, int startAddr, int length) throws IOException  {
		StringBuffer dataBuffer = new StringBuffer();
		dataBuffer.append(HOSTlINK_PREFIX);
		dataBuffer.append(getByteBCD(addr));
		dataBuffer.append(cmd);
		dataBuffer.append(HexFormat.formatHex(Integer.toHexString(startAddr)));
		dataBuffer.append(HexFormat.formatHex(Integer.toHexString(length)));
		
		dataBuffer.append(FCS.getFCS(dataBuffer.toString()));
		dataBuffer.append(HOSTlINK_SUFFIX);
		
		System.out.println(dataBuffer.toString());
		
		byte[] bys = dataBuffer.toString().getBytes();

		this.sendCode = cmd;
		
		if(this.serialPort != null) {
			this.serialPort.write(bys);
		}		
	}

	/**
	 * 
	 * @param addr	PLC单元地址的BCD码
	 * @param cmd	方法码
	 * @param startAddr 起始寻址地址
	 * @param value 值
	 * @throws IOException
	 */
	private void sendWrite(int addr, String cmd, int startAddr, int value[]) throws IOException {
		StringBuffer dataBuffer = new StringBuffer();
		dataBuffer.append(HOSTlINK_PREFIX);
		dataBuffer.append(getByteBCD(addr));
		dataBuffer.append(cmd);
		dataBuffer.append(HexFormat.formatHex(Integer.toHexString(startAddr)));
		if(value != null) {
			for (int i = 0; i < value.length; i++) {
				String hex = Integer.toHexString(value[i]);
				dataBuffer.append(HexFormat.formatHex(hex));
			}
		}
		
		dataBuffer.append(FCS.getFCS(dataBuffer.toString()));
		dataBuffer.append(HOSTlINK_SUFFIX);
		
		System.out.println("send " + dataBuffer.toString());
		
		byte[] bys = dataBuffer.toString().getBytes();

		this.sendCode = cmd;
		
		if(this.serialPort != null) {
			this.serialPort.write(bys);
		}

	}

	
	/**
	 * 读取PLC数据回复并解析
	 * 
	 * @return
	 * @throws IOException
	 */
	public Response receive() throws IOException {
		String line = this.readLine(2000);
		if (line.length() < 11) {
			return null;
		}
		System.out.println("recv " + line);
		return this.analyzeData(line);
	}

	/**
	 * 数据解析
	 * 
	 * @param s
	 * @return
	 */
	private Response analyzeData(String s) {
		Response resp = new Response();
		try {
			String fcs1 = s.substring(s.length() - 4, s.length() - 2);
			String fcs2 = FCS.getFCS(s.substring(0, s.length() - 4));

			if (!fcs1.equals(fcs2)) {
				resp.state = "-1";
				return resp;
			}

			resp.addr = Integer.parseInt(s.substring(1, 3), 16);
			resp.code = s.substring(3, 5);
			resp.state = s.substring(5, 7);

			int len = (s.length() - 11) / 4;
			for (int i = 0; i < len; i++) {
				int beginIndex = 7 + i * 4;
				String item = s.substring(beginIndex, beginIndex + 4);
				resp.dataList.add(Integer.parseInt(item, 16));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resp;
	}

	/**
	 * 读一行
	 * @param timeout
	 * @return
	 * @throws IOException
	 * @throws  
	 */
	private String readLine(int timeout) throws IOException {
		StringBuilder sb = new StringBuilder(200);

		byte[] onebyte = new byte[1];
		while (timeout  > 0) {
			int len = serialPort.readToBuffer(onebyte, 0, 1);
			if (len == 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				timeout -= 50;
				continue;
			}

			//System.out.println(Formatter.toHexString(onebyte));
			if (onebyte[0] == 0x0D) {
				sb.append((char) onebyte[0]);
				break;
			}

			sb.append((char) onebyte[0]);
		}
		return sb.toString();
	}

	private String getByteBCD(int a) {
		byte value = (byte) ((a / 10) * 16 + (a % 10));		
		return Formatter.toHexString(value).toUpperCase();		
	}
    
}
