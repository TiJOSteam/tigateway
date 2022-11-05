package tigw200;

import java.io.IOException;

import tigateway.TiGW100;
import tigateway.TiGateway;
import tigateway.protocol.hostlink.HostLink;
import tigateway.protocol.hostlink.Response;
import tigateway.serialport.TiSerialPort;

public class HostlinkSample {
public static void main(String[] args) {
    	
    	try {
    		
    		TiSerialPort sp = TiGW100.getInstance().getSerialPort(1, 9600, 8, 1, 2);
    		
        	HostLink hostlink = new HostLink(sp);
        	
        	Response resp  = hostlink.readRequest(0, "RD", 0, 1);
			
			System.out.println(resp);

			for(int value : resp.dataList) {
				System.out.println(Integer.toHexString(value));
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    		
    }
}
