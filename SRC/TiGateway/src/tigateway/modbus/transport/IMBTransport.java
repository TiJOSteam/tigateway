package tigateway.modbus.transport;

import tigateway.modbus.Modbus;

public interface IMBTransport {

	/**
	 * Send MODBUS request
	 */
	void sendRequest(Modbus mb) throws Exception;

	/**
	 * Waiting for response
	 */
	int waitResponse(Modbus mb) throws Exception;
	
	
	void setCommTimout(int timeout);

}
