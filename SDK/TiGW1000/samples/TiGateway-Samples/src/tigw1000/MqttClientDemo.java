package tigw1000;

import tijos.framework.platform.TiPower;
import tijos.framework.platform.lan.TiLAN;
import java.io.IOException;

import tijos.framework.networkcenter.mqtt.IMqttMessageListener;
import tijos.framework.networkcenter.mqtt.MqttClient;
import tijos.framework.networkcenter.mqtt.MqttConnectOptions;
import tijos.framework.util.Delay;
import tijos.framework.util.logging.Logger;

/**
 * MQTT Client 例程, 在运行此例程时请确保MQTT Server地址及用户名密码正确
 *
 * @author TiJOS
 */

/**
 * MQTT 事件监听
 *
 */

class MqttClientMessage implements  IMqttMessageListener
{
	int connectError = 0;
	
	@Override
	public void onNetworkConnected(boolean isReconnect) {
		// TODO Auto-generated method stub
		System.out.println("onNetworkConnected " + isReconnect);
	}
	
	@Override
	public void onNetworkDisconnected(int err) {
		System.out.println("onNetworkDisconnected " + err);
		
		connectError++;
		
		//连续3次网络连接失败自动重启设备
		if(connectError > 2) {
			TiPower.getInstance().reboot(0);
		}
	}

	@Override
	public void onMqttConnected() {
		System.out.println("onMqttConnected");
		
		connectError = 0;
	}

	@Override
	public void publishMessageArrived(String topic, byte[] payload) {
		System.out.println("publishMessageArrived " + topic + " " + new String(payload));
		
		
	}

	@Override
	public void publishCompleted(int msgId, String topic, int result) {
		System.out.println("publishCompleted " + " mid " + msgId + " " + topic + " " + " result " + result );
		
	}

	@Override
	public void subscribeCompleted(int msgId, String topic, int result) {
		System.out.println("subscribeCompleted " + " mid " + msgId + " " + topic + " " + " result " + result );
		
	}

	@Override
	public void unsubscribeCompleted(int msgId, String topic, int result) {
		System.out.println("unsubscribeCompleted " + " mid " + msgId + " " + topic + " " + " result " + result );
		
	}


}	   

public class MqttClientDemo {

    public static void main(String args[]) {

        // 启动LTE网络
        try {
            TiLAN.getInstance().startup(20);
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        // MQTT Server 地址,用户名, 密码
        final String broker = "mqtt://test.mosquitto.org:1883";
        final String username = "123";
        final String password = "123";

        // ClientID
        final String clientId = "mqtt_test_java_tijos";

        // MQTT连接设置
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(username);
        connOpts.setPassword(password);
  
        MqttClient mqttClient = MqttClient.getInstance();

        int qos = 1;

        try {
            // 连接MQTT服务器 10秒超时
            mqttClient.connect(clientId, broker, 10, connOpts, new MqttClientMessage());
            
            // 订阅topic
            String topic = "topic2";
            String head = "Message from TiJOS NO. ";

            int msgId = mqttClient.subscribe(topic, qos);
            Logger.info("MQTTClientDemo", "Subscribe to topic: " + topic + " msgid = " + msgId);

            // 发布topic
            int counter = 0;
            while (true) {
                String content = head + counter;
                msgId = mqttClient.publish(topic, content.getBytes(), qos, false);
                Logger.info("MQTTClientDemo", "Topic " + topic + "Publish message: " + content + " msgid = " + msgId);

                counter++;
                Delay.msDelay(1000);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
           mqttClient.disconnect();// release resource
        }
    }
}
