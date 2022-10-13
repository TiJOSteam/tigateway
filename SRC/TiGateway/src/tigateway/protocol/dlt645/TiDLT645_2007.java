package tigateway.protocol.dlt645;

import java.io.IOException;

import tigateway.TiGW200;
import tigateway.serialport.TiSerialPort;
import tigateway.utils.Helper;
import tijos.framework.platform.TiSettings;
import tijos.framework.util.Formatter;
import tijos.framework.util.LittleBitConverter;
import tijos.framework.util.crc.CheckSum8;
import tijos.framework.util.logging.Logger;

/**
 * Hello world!
 */
public class TiDLT645_2007 {

	// DLT645_TAG_TOTAL_ENERGY_POWER
	/***** data tags table, DI0 DI1 DI2 DI3 *******/
	public static final int DLT645_TAG_TOTAL_ENERGY_POWER = 0x00000000;// 表读数- 组合有功总电能
	public static final int DLT645_TAG_FORWARD_ACTIVE_POWER = 0x00010000; // 表读数-总（正向有功）
	public static final int DLT645_TAG_BACKWARD_ACTIVE_POWER = 0x00020000; // 表读数-总（反向有功）
	public static final int DLT645_TAG_INSTANT_ACTIVE_POWER = 0x02030000; // 瞬时总有功

	public static final int DLT645_TAG_GRID_PHASE_VOLTAGE_A = 0x02010100;
	public static final int DLT645_TAG_GRID_PHASE_VOLTAGE_B = 0x02010200; // 电网相电压[Max. 3路]
	public static final int DLT645_TAG_GRID_PHASE_VOLTAGE_C = 0x02010300;

	public static final int DLT645_TAG_GRID_PHASE_CURRENT_A = 0x02020100;
	public static final int DLT645_TAG_GRID_PHASE_CURRENT_B = 0x02020200; // 电网相电流[Max. 3路]
	public static final int DLT645_TAG_GRID_PHASE_CURRENT_C = 0x02020300;

	public static final int DLT645_TAG_GRID_PHASE_POWER_TOTAL = 0x02030000; // 瞬时总有功功率
	public static final int DLT645_TAG_GRID_PHASE_POWER_A = 0x02030100;
	public static final int DLT645_TAG_GRID_PHASE_POWER_B = 0x02030200; // 电网相功率[Max. 3路]
	public static final int DLT645_TAG_GRID_PHASE_POWER_C = 0x02030300;

	// 零线电流
	public static final int DLT645_TAG_LINEZERO_CURRENT = 0x02800001;

	// 表内温度
	public static final int DLT645_TAG_INTERVAL_TEMPERATURE = 0x02800007;

	// 表读数-电价1/2/3/4（正向有功）
	public static final int DLT645_TAG_FORWARD_ACTIVE_POWER_1 = 0x00010100;
	public static final int DLT645_TAG_FORWARD_ACTIVE_POWER_2 = 0x00010200;
	public static final int DLT645_TAG_FORWARD_ACTIVE_POWER_3 = 0x00010300;
	public static final int DLT645_TAG_FORWARD_ACTIVE_POWER_4 = 0x00010400;

	// 表读数-电价1/2/3/4（反向有功）
	public static final int DLT645_TAG_BACKWARD_ACTIVE_POWER_1 = 0x00020100;
	public static final int DLT645_TAG_BACKWARD_ACTIVE_POWER_2 = 0x00020200;
	public static final int DLT645_TAG_BACKWARD_ACTIVE_POWER_3 = 0x00020300;
	public static final int DLT645_TAG_BACKWARD_ACTIVE_POWER_4 = 0x00020400;

	// 表状态
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_1 = 0x04000501; // 电表运行状态字1
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_3 = 0x04000503; // 电表运行状态字3
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_4 = 0x04000504; // 电表运行状态字4
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_5 = 0x04000505; // 电表运行状态字5
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_6 = 0x04000506; // 电表运行状态字6
	public static final int DLT645_TAG_STATUS_ACTIVE_POWER_7 = 0x04000507; // 电表运行状态字7

	public static final int DLT645_TAG_ADDRESS_ACTIVE_POWER_1 = 0x04000402;

	/***** comm type *******/
	public static final int DLT645_COMM_TYPE_MASK = 0xE0;
	public static final int DLT645_MASTER_QUERY = 0x00;
	public static final int DLT645_SLAVE_REPLY_NORMAL = 0x80;
	public static final int DLT645_SLAVE_REPLY_ERROR = 0xC0;

	/***** comm bytes *******/
	private static final int DLT645_LEADING_BYTE = 0xFE;
	private static final int DLT645_START_BYTE = 0x68;

	/***** function code *******/
	private static final int DLT645_FUNC_CODE_MASK = 0x1F;
	private static final int DLT645_PKT_TYPE_TIME_SYNC = 0x08;
	private static final int DLT645_PKT_TYPE_READ_DATA = 0x11;
	private static final int DLT645_PKT_TYPE_READ_DATA_LEFT = 0x12;
	private static final int DLT645_PKT_TYPE_WRITE_DATA = 0x14;
	private static final int DLT645_PKT_TYPE_READ_ADDRESS = 0x13;
	private static final int DLT645_PKT_TYPE_WRITE_ADDRESS = 0x15;
	private static final int DLT645_PKT_TYPE_CHANGE_COMM_SPEED = 0x17;
	private static final int DLT645_PKT_TYPE_TRIPING_CLOSE = 0x1C; // 跳合闸、报警、保电

	private static final int DLT645_PKT_TYPE_WRITE_DATA_RSP = 0x94;
	private static final int DLT645_PKT_TYPE_WRITE_DATA_ERR = 0xD4;

	/***** package length *******/
	private static final int DLT645_HEAD_TAIL_LEN = 16; // 4 leading bytes, 2 start bytes, 6 address, 1 func code, 1
														// data len, 1 cs, 1 end byte
	private static final int DLT645_PRE_LEADING_LEN = 4; // 4 0xEF
	private static final int DLT645_DATA_TAG_LEN = 4; // data identification
	private static final int DLT645_ADDRESS_LEN = 6; // meter address
	private static final int DLT645_MAX_DATA_LEN = 12; // max data 4+8
	private static final int DLT645_MIN_DATA_LEN = 6; // min data 4+2
	private static final int DLT645_POWER_READING_LEN = 4; // power data len
	private static final int DLT645_PASSWORD_LEN = 4; // password len
	private static final int DLT645_OPERATOR_LEN = 4; // operator len
	private static final int DLT645_FIXED_LEN = 10; // 2leading bytes，6 meter address，1func code，1 data len
	private static final int DLT645_gPHASE_VC_LEN = 2; // E V data len
	private static final int DLT645_gPHASE_P_LEN = 3; // three-phase power len
	private static final int DLT645_ERROR_LEN = 1; // error message
	private static final int DLT645_EXTRA_LEN = 4; // read message extra length

	/*
	 * meter address
	 */
	byte[] deviceAddress = new byte[DLT645_ADDRESS_LEN];

	TiSerialPort serialPort;
	
	int preLeadingLen = DLT645_PRE_LEADING_LEN;
	
	byte [] ioBuffer = new byte[256];

	/**
	 * Initialize with serial port
	 * 
	 * @param serialPort
	 * @throws IOException
	 */
	public TiDLT645_2007(TiSerialPort serialPort) {
		this.serialPort = serialPort;

		for (int i = 0; i < deviceAddress.length; i++) {
			deviceAddress[i] = (byte) 0xaa;
		}
	}

	/**
	 * Set meter address 
	 * @param meterAddress
	 * @throws IOException
	 */
	public void setMeterAddress(byte[] meterAddress) throws IOException {
		
		if (meterAddress.length > DLT645_ADDRESS_LEN) {
			throw new IOException("Invalid address length");
		}
		
		for (int i = 0; i < deviceAddress.length; i++) {
			deviceAddress[i] = (byte) 0x00;
		}
		System.arraycopy(meterAddress, 0, this.deviceAddress, DLT645_ADDRESS_LEN - meterAddress.length, meterAddress.length);			

		Helper.reverseBytes(this.deviceAddress);
	}

	/**
	 * Get meter address
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] readMeterAddress() throws IOException {
		byte []address =  queryMeterAddress();
		
		Helper.reverseBytes(address);
		return address;
	}

	/**
	 * query meter reading by data tag
	 * 
	 * @param dataTag
	 * @return
	 * @throws IOException
	 */
	public byte[] readMeterData(int dataTag) throws IOException {
		return queryMeterReading(DLT645_PKT_TYPE_READ_DATA, dataTag);
	}

	/**
	 * 时钟同步
	 * 
	 * @param seconds
	 * @throws IOException
	 */
	public void timeSync(int seconds) throws IOException {
		byte[] pkt = this.createTimeSyncRequest(seconds);
		sendPkt(pkt);
	}

	/**
	 * 透传
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public byte[] transmit(byte[] data) throws IOException {

		this.sendPkt(data);
		return this.getReply();

	}

	/**
	 * 跳合闸、报警、保电指令
	 * 
	 * @return
	 * @throws IOException
	 */
	public void meterSwitchCommand(int command, byte[] password, byte[] operator) throws IOException {

		// 如果meterAddres未获取
		if (this.deviceAddress[0] == 0xAA && this.deviceAddress[1] == 0xAA && this.deviceAddress[2] == 0xAA) {
			this.deviceAddress = this.queryMeterAddress();
		}

		int pos = 0;
		byte[] data = new byte[8];
		data[pos++] = (byte) command;
		data[pos++] = 0;

		byte[] timeBCD = Helper.getTimeBCD(TiSettings.getInstance().getDateTime() + 86400);
		data[pos++] = timeBCD[6];
		data[pos++] = timeBCD[5];
		data[pos++] = timeBCD[4];
		data[pos++] = timeBCD[3];
		data[pos++] = timeBCD[2];
		data[pos++] = (byte) 0x99; // timeBCD[1];

		Helper.reverseBytes(password);
		Helper.reverseBytes(operator);

		byte[] pkt = this.createWriteRequest(DLT645_PKT_TYPE_TRIPING_CLOSE, password, operator, data);
		sendPkt(pkt);

		byte[] recvPkt = getReply();

		decodePkt(DLT645_PKT_TYPE_TRIPING_CLOSE, recvPkt, 0);

	}

	/**
	 * write the specified tag data to meter with password and operator
	 * 
	 * @param password
	 * @param operator
	 * @param dataTag
	 * @param data
	 * @throws IOException
	 */
	public void writeMeterData(byte[] password, byte[] operator, int dataTag, byte[] data) throws IOException {
		if (password.length != 4 || operator.length != 4)
			throw new IOException("invalid password or oeprator length");

		byte[] pkt = createWriteRequest(DLT645_PKT_TYPE_WRITE_DATA, password, operator, dataTag, data);
		sendPkt(pkt);

		byte[] recvPkt = getReply();

		if (recvPkt.length == 0) {
			throw new IOException("DLT645 Receive meter reading reply failed!");
		}

		decodePkt(DLT645_PKT_TYPE_WRITE_DATA, recvPkt, dataTag);
	}

	/**
	 * Alarm response
	 * 
	 * @param dataTag
	 * @throws IOException
	 */
	public void writeAlarmDataResponse(int dataTag) throws IOException {
		byte[] pkt = createSendPkt(DLT645_PKT_TYPE_READ_DATA_LEFT, dataTag);

		sendPkt(pkt);
	}

	/**
	 * query meter reading by data tag
	 * 
	 * @param dataTag
	 * @return
	 * @throws IOException
	 */
	private byte[] queryMeterReading(int funCode, int dataTag) throws IOException {

		byte[] pkt = createSendPkt(funCode, dataTag); // format of message

		sendPkt(pkt);

		byte[] recvPkt = getReply();

		if (recvPkt.length == 0) {
			throw new IOException("DLT645 Receive meter reading reply failed!");
		}

		byte[] meterData = decodePkt(funCode, recvPkt, dataTag);

		return meterData;
	}

	private byte[] queryMeterAddress() throws IOException {

		byte[] meterAddress = new byte[DLT645_ADDRESS_LEN];
		for (int i = 0; i < meterAddress.length; i++) {
			meterAddress[i] = (byte) 0xaa;
		}

		int funCode = DLT645_PKT_TYPE_READ_ADDRESS;

		byte[] pkt = createSendPkt(meterAddress, funCode, 0); // format of message

		sendPkt(pkt);

		byte[] recvPkt = getReply();

		if (recvPkt.length == 0) {
			throw new IOException("DLT645 Receive meter reading reply failed!");
		}

		byte[] meterData = decodePkt(funCode, recvPkt, 0);

		return meterData;

	}

	/**
	 * Create send packet by type
	 * 
	 * @param funCode
	 * @param dataTag
	 * @return
	 */
	private byte[] createSendPkt(int funCode, int dataTag) throws IOException {
		return createSendPkt(this.deviceAddress, funCode, dataTag);
	}

	private byte[] createSendPkt(byte[] meterAddress, int funCode, int dataTag) throws IOException {
		int datalen = 0;
		int pktLen;
		byte[] data = new byte[DLT645_MAX_DATA_LEN];

		switch (funCode) {
		case DLT645_PKT_TYPE_READ_DATA:
		case DLT645_PKT_TYPE_READ_DATA_LEFT:
			datalen = DLT645_DATA_TAG_LEN;

			byte[] tag = LittleBitConverter.GetBytes(dataTag);
			System.arraycopy(tag, 0, data, 0, tag.length);

			break;
		case DLT645_PKT_TYPE_READ_ADDRESS:
			datalen = 0;
			break;

		default:
			throw new IOException("Invalid type");
		}

		pktLen = datalen + DLT645_HEAD_TAIL_LEN;

		byte[] pkt = new byte[pktLen];

		/* add 4 leading bytes */
		pkt[0] = (byte) DLT645_LEADING_BYTE;
		pkt[1] = (byte) DLT645_LEADING_BYTE;
		pkt[2] = (byte) DLT645_LEADING_BYTE;
		pkt[3] = (byte) DLT645_LEADING_BYTE;

		pkt[4] = DLT645_START_BYTE; // start byte

		System.arraycopy(meterAddress, 0, pkt, 5, meterAddress.length);

		pkt[11] = DLT645_START_BYTE;
		pkt[12] = (byte) (DLT645_MASTER_QUERY | funCode); // function code
		pkt[13] = (byte) datalen;

		if (datalen > 0) {
			for (int i = 0; i < datalen; ++i) {
				pkt[14 + i] = (byte) (data[i] + 0x33);
			}
		}

		pkt[pktLen - 2] = (byte) getChecksum(pkt, 4, pktLen - 6); // get the checksum excluding the leading bytes and
																	// end byte
		pkt[pktLen - 1] = 0x16;

		return pkt;
	}

	private byte[] createWriteRequest(int funCode, byte[] password, byte[] operator, int dataTag, byte[] data) {

		int expectRecvLen = DLT645_HEAD_TAIL_LEN + DLT645_DATA_TAG_LEN + DLT645_PASSWORD_LEN + DLT645_OPERATOR_LEN
				+ DLT645_EXTRA_LEN;

		int dataLen = 04 + 04 + 04 + data.length;
		int pktLen = expectRecvLen;
		int pos = 0;

		byte[] pkt = new byte[pktLen];

		/* add 4 leading bytes */
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;

		pkt[pos++] = DLT645_START_BYTE; // start byte

		System.arraycopy(deviceAddress, 0, pkt, pos, deviceAddress.length);
		pos += DLT645_ADDRESS_LEN;

		pkt[pos++] = DLT645_START_BYTE;
		pkt[pos++] = (byte) funCode;
		pkt[pos++] = (byte) dataLen;

		byte[] tag = LittleBitConverter.GetBytes(dataTag);
		System.arraycopy(tag, 0, pkt, pos, tag.length);
		pos += DLT645_DATA_TAG_LEN;

		System.arraycopy(password, 0, pkt, pos, password.length);
		pos += 4;

		System.arraycopy(operator, 0, pkt, pos, operator.length);
		pos += 4;

		System.arraycopy(data, 0, pkt, pos, data.length);
		pos += data.length;

		if (dataLen > 0) {
			for (int i = 0; i < dataLen; ++i) {
				pkt[14 + i] = (byte) (pkt[14 + i] + 0x33);
			}
		}

		pkt[pos++] = (byte) getChecksum(pkt, 4, pktLen - 6); // get the checksum excluding the leading bytes and end
																// byte
		pkt[pos++] = 0x16;

		return pkt;
	}

	public byte[] createWriteRequest(int funCode, byte[] password, byte[] operator, byte[] data) {

		int expectRecvLen = DLT645_HEAD_TAIL_LEN + DLT645_PASSWORD_LEN + DLT645_OPERATOR_LEN + data.length;

		int dataLen = 04 + 04 + data.length;
		int pktLen = expectRecvLen;
		int pos = 0;

		byte[] pkt = new byte[pktLen];

		/* add 4 leading bytes */
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;
		pkt[pos++] = (byte) DLT645_LEADING_BYTE;

		pkt[pos++] = DLT645_START_BYTE; // start byte

		System.arraycopy(deviceAddress, 0, pkt, pos, deviceAddress.length);
		pos += DLT645_ADDRESS_LEN;

		pkt[pos++] = DLT645_START_BYTE;
		pkt[pos++] = (byte) funCode;
		pkt[pos++] = (byte) dataLen;

		System.arraycopy(password, 0, pkt, pos, password.length);
		pos += 4;

		System.arraycopy(operator, 0, pkt, pos, operator.length);
		pos += 4;

		System.arraycopy(data, 0, pkt, pos, data.length);
		pos += data.length;

		if (dataLen > 0) {
			for (int i = 0; i < dataLen; ++i) {
				pkt[14 + i] = (byte) (pkt[14 + i] + 0x33);
			}
		}

		pkt[pos++] = (byte) getChecksum(pkt, 4, pktLen - 6); // get the checksum excluding the leading bytes and end
																// byte
		pkt[pos++] = 0x16;

		return pkt;
	}

	private byte[] createTimeSyncRequest(long seconds) {
		// DLT645_PKT_TYPE_TIME_SYNC

		byte[] bcdTime = Helper.getTimeBCD(seconds);
		byte[] data = new byte[6];

		data[0] = bcdTime[6];
		data[1] = bcdTime[5];
		data[2] = bcdTime[4];
		data[3] = bcdTime[3];
		data[4] = bcdTime[2];
		data[5] = bcdTime[1];

		int pktLen = data.length + DLT645_HEAD_TAIL_LEN;

		byte[] pkt = new byte[pktLen];

		/* add 4 leading bytes */
		pkt[0] = (byte) DLT645_LEADING_BYTE;
		pkt[1] = (byte) DLT645_LEADING_BYTE;
		pkt[2] = (byte) DLT645_LEADING_BYTE;
		pkt[3] = (byte) DLT645_LEADING_BYTE;

		pkt[4] = DLT645_START_BYTE; // start byte

		pkt[5] = (byte) 0x99;
		pkt[6] = (byte) 0x99;
		pkt[7] = (byte) 0x99;
		pkt[8] = (byte) 0x99;
		pkt[9] = (byte) 0x99;
		pkt[10] = (byte) 0x99;

		pkt[11] = DLT645_START_BYTE;
		pkt[12] = DLT645_PKT_TYPE_TIME_SYNC;// function code
		pkt[13] = (byte) data.length;

		if (data.length > 0) {
			for (int i = 0; i < data.length; ++i) {
				pkt[14 + i] = (byte) (data[i] + 0x33);
			}
		}

		pkt[pktLen - 2] = (byte) getChecksum(pkt, 4, pktLen - 6); // get the checksum excluding the leading bytes and
																	// end byte
		pkt[pktLen - 1] = 0x16;

		return pkt;
	}

	/**
	 * Decode the tag data from received packet
	 * 
	 * @param funCode    function code
	 * @param Pkt
	 * @param match_data
	 * @return
	 * @throws IOException
	 */
	private byte[] decodePkt(int funCode, byte[] Pkt, int match_data) throws IOException {

		Logger.info("TiDLT645", "decodePkt funCode " + funCode + " " + Formatter.toHexString(Pkt));

		int check_len;

		/* delete all leading bytes */
		int startPos = 0;
		while (Pkt[startPos] != DLT645_START_BYTE && startPos < Pkt.length - 1) {
			startPos++;
		}

		if (startPos >= Pkt.length || Pkt[startPos] != DLT645_START_BYTE) {
			throw new IOException("DLT645 Decode: receive all FE packet or the start byte is not 68!");
		}

		// L
		int data_len = Pkt[startPos + 9];

		check_len = DLT645_FIXED_LEN + data_len;
		int expCS = Pkt[startPos + check_len] & 0xFF;
		int CS = getChecksum(Pkt, startPos, check_len);// excluding cs and end byte
		if (expCS != CS) {
			throw new IOException("DLT645 Decode: Checksum mismatch! CS " + CS + " exp CS " + expCS);
		}

		/* check if the receive pkt and the send pkt types match */
		int controlCode = Pkt[startPos + 8] & 0xff;
		if ((funCode & DLT645_FUNC_CODE_MASK) != (controlCode & DLT645_FUNC_CODE_MASK)) {
			throw new IOException("DLT645 Decode: Send and receive package types mismatch!");
		}

		for (int i = 0; i < data_len; ++i) // data area should subtract 0x33 to get the real values
		{
			Pkt[DLT645_FIXED_LEN + i + startPos] -= 0x33;
		}

		byte[] Type_Match = LittleBitConverter.GetBytes(match_data);

		switch (funCode) {
		case DLT645_PKT_TYPE_READ_ADDRESS:
			if (data_len < DLT645_ADDRESS_LEN) {
				throw new IOException("DLT645 Decode: receive read address data len mismatch!");
			}

			if (Helper.memcmp(Pkt, startPos + 1, Pkt, startPos + 10, DLT645_ADDRESS_LEN) != 0) {
				throw new IOException("DLT645 receive read address data  mismatch!");
			}

			byte[] address = new byte[DLT645_ADDRESS_LEN];

			System.arraycopy(Pkt, startPos + 10, address, 0, DLT645_ADDRESS_LEN);

			return address;

		case DLT645_PKT_TYPE_READ_DATA:

			if ((controlCode & DLT645_COMM_TYPE_MASK) == DLT645_SLAVE_REPLY_ERROR) {
				throw new IOException("DLT645 Decode: receive reply reading control code is D1!");
			}

			if (Helper.memcmp(Type_Match, 0, Pkt, startPos + 10, 4) != 0) {
				throw new IOException("DLT645 Decode: receive reply reading data identification mismatch!");
			}

			if (!((controlCode & DLT645_COMM_TYPE_MASK) == DLT645_SLAVE_REPLY_NORMAL)) {
				throw new IOException("DLT645 Decode: receive reply reading control code is not read function!");
			}

			if (data_len < DLT645_MIN_DATA_LEN || data_len > DLT645_MAX_DATA_LEN) {
				throw new IOException("DLT645 Decode: receive reply reading data length mismatch!");
			}

			// 4 bytes reading follows N bytes data tag in the data area
			byte[] meterData = new byte[data_len - DLT645_DATA_TAG_LEN];
			System.arraycopy(Pkt, startPos + DLT645_FIXED_LEN + DLT645_DATA_TAG_LEN, meterData, 0, meterData.length);

			return meterData;

		case DLT645_PKT_TYPE_WRITE_DATA:
			if (controlCode != 0x94) {
				throw new IOException("DLT645 Decode: receive reply reading control code is not 0x94!");
			}

			return null;

		case DLT645_PKT_TYPE_TRIPING_CLOSE:
			if (controlCode != 0x9C) {
				throw new IOException("DLT645 Decode: receive reply reading control code is not 0x9C! " + controlCode);
			}

			return null;

		default:
			throw new IOException("DLT645 Decode: control code type unknown!");
		}
	}

	/**
	 * Generate checksum from data buffer
	 * 
	 * @param data  data buffer
	 * @param start start pos
	 * @param len   length
	 * @return checksum value
	 */
	private static int getChecksum(byte[] data, int start, int len) {
		CheckSum8 checksum = new CheckSum8();
		checksum.update(data, start, len);

		return checksum.getValue();
	}

	/**
	 * Send packet to uart
	 * 
	 * @param pkt
	 * @throws IOException
	 */
	private void sendPkt(byte[] pkt) throws IOException {

		Logger.info("TiDLT645", "sendPkt: " + Formatter.toHexString(pkt));

		this.serialPort.clearInput();
		this.serialPort.write(pkt);
	}

	/**
	 * Receive packet from serial port within timeout
	 * 
	 * @param expLen expected length
	 * @return data reply
	 * @throws IOException
	 */
	private byte[] getReply() throws IOException {

		int pos = 0;
		
		this.ioBuffer[0] = 0;
				
		while(this.ioBuffer[0] != DLT645_START_BYTE) {
			if(0 == this.serialPort.read(this.ioBuffer, 0, 1, 3000))
			{
				throw new IOException("No start byte received.");
			}
		}

		pos ++;

		int expLen = DLT645_FIXED_LEN - 1;
		
		//DLT645_FIXED_LEN 10 bytes, 
		int length = this.serialPort.read(this.ioBuffer, pos, expLen, 3000);
		if(length != expLen) {
			throw new IOException("lack data.");
		}
		pos += expLen;

		expLen = this.ioBuffer[DLT645_FIXED_LEN -1]  + 2; //data len + cs tail
		length = this.serialPort.read(this.ioBuffer, pos, expLen, 3000);
		if(length != expLen) {
			throw new IOException("lack data.");
		}
		
		pos += expLen;
		
		byte [] reply = new byte[pos];
		System.arraycopy(this.ioBuffer, 0, reply, 0,  pos);
		
		return reply;
	}

//	public static void main(String[] args) throws IOException {
//		System.out.println("Hello World!");
//	
//		int[] All_Meter_Data = new int[] { TiDLT645_2007.DLT645_TAG_FORWARD_ACTIVE_POWER,
//				TiDLT645_2007.DLT645_TAG_BACKWARD_ACTIVE_POWER, TiDLT645_2007.DLT645_TAG_INSTANT_ACTIVE_POWER,
//				TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_A, TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_B,
//				TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_C, TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_A,
//				TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_B, TiDLT645_2007.DLT645_TAG_GRID_PHASE_CURRENT_C,
//				TiDLT645_2007.DLT645_TAG_GRID_PHASE_POWER_A, TiDLT645_2007.DLT645_TAG_GRID_PHASE_POWER_B,
//				TiDLT645_2007.DLT645_TAG_GRID_PHASE_POWER_C, TiDLT645_2007.DLT645_TAG_FORWARD_ACTIVE_POWER_1,
//				TiDLT645_2007.DLT645_TAG_FORWARD_ACTIVE_POWER_2, TiDLT645_2007.DLT645_TAG_FORWARD_ACTIVE_POWER_3,
//				TiDLT645_2007.DLT645_TAG_FORWARD_ACTIVE_POWER_4, TiDLT645_2007.DLT645_TAG_BACKWARD_ACTIVE_POWER_1,
//				TiDLT645_2007.DLT645_TAG_BACKWARD_ACTIVE_POWER_2, TiDLT645_2007.DLT645_TAG_BACKWARD_ACTIVE_POWER_3,
//				TiDLT645_2007.DLT645_TAG_BACKWARD_ACTIVE_POWER_4 };
//
//		TiSerialPort serialPort = TiGW200.getInstance().getRS485(2400, 8, 1, 2);
//		TiDLT645_2007 dlt645 = new TiDLT645_2007(serialPort);
//
////		String factory = "68aaaaaaaaaaaa681F03428832EA16";
////		byte[] result = dlt645.transmit(Formatter.hexStringToByte(factory));
////
////		System.out.println("result " + Formatter.toHexString(result));
//
//		byte[] address = dlt645.readMeterAddress();
//		System.out.println("address " + Formatter.toHexString(address));
//		
//		dlt645.setMeterAddress(address);
////		dlt645.setMeterAddress(Formatter.hexStringToByte("00510002"));
//	
//		System.out.println("dev " + Formatter.toHexString(dlt645.deviceAddress));
//		
//
////		byte[] password = new byte[] { 0, 0, 0, 2 };
////		byte[] operator = new byte[] { (byte) 0x00, (byte) 00, (byte) 00, (byte) 0x12 };
////
////		dlt645.meterSwitchCommand(0x1c, password, operator);
//
//		int tag = 0;
//
//		tag = TiDLT645_2007.DLT645_TAG_GRID_PHASE_VOLTAGE_A;
//		double reading = MeterReading_Get(dlt645, tag);
//		System.out.println("tag: " + 0 + " reading: " + reading);
////
//		byte[] meterData = dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_1); // read meter data
//		System.out.println(Formatter.toHexString(meterData));
//
//		meterData = dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_3); // read meter data
//		System.out.println(Formatter.toHexString(meterData));
//
//		meterData = dlt645.readMeterData(TiDLT645_2007.DLT645_TAG_STATUS_ACTIVE_POWER_4); // read meter data
//		System.out.println(Formatter.toHexString(meterData));
//
//		for (int rtag : All_Meter_Data) {
//			reading = MeterReading_Get(dlt645, rtag);
//			System.out.println("tag: " + rtag + " reading: " + reading);
//		}
//	}
//
//	static double MeterReading_Get(TiDLT645_2007 dlt645, int meterdata) throws IOException {
//		byte[] meterData = dlt645.readMeterData(meterdata); // read meter data
//
//		double reading = Helper.BCD2Double(meterData, 2);
//		return reading;
//	}

}
