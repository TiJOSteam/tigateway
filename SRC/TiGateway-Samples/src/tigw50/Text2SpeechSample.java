package tigw50;

import java.io.IOException;

import tijos.framework.platform.sound.TiTTS;

/**
 * TTS 文字转音频输出， 在运行前将spk+/- 连接外置音频设备
 * @author Administrator
 *
 */
public class Text2SpeechSample {

	public static void main(String[] args) {

		try {
			//中文需转成UTF-8格式, 可通过TiDevManager中的utf-8转换功能或者从网络下发UTF-8格式字符串
			
			//微信收款 100 元
			byte[] content = { (byte) 0xE5, (byte) 0xBE, (byte) 0xAE, (byte) 0xE4, (byte) 0xBF, (byte) 0xA1,
					(byte) 0xE6, (byte) 0x94, (byte) 0xB6, (byte) 0xE6, (byte) 0xAC, (byte) 0xBE, (byte) 0x20,
					(byte) 0x31, (byte) 0x30, (byte) 0x30, (byte) 0x20, (byte) 0xE5, (byte) 0x85, (byte) 0x83 };
			
			//音量80%
			TiTTS.getInstance().setVolume(80);
			TiTTS.getInstance().play(content);
			
		} catch (IOException ex) {

			ex.printStackTrace();
		}
	}

}
