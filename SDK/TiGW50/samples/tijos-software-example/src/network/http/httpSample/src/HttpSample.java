
import tijos.framework.networkcenter.httpclient.HttpConnection;
import tijos.framework.networkcenter.httpclient.HttpResponse;
import tijos.framework.platform.lte.TiLTE;
import tijos.framework.platform.network.NetworkInterfaceManager;

public class HttpSample {

	public static void main(String[] args) {

		try {
			System.out.println("start network.");
            //启动LTE网络
            TiLTE.getInstance().startup(30);
			
			String url = "http://img.tijos.net/img/version.txt";

			HttpConnection httpConnection = new HttpConnection(url);
			
			httpConnection.setContentType(HttpConnection.APPLICATION_TEXT_PLAIN);
			
			HttpResponse resp = httpConnection.get();
			
			System.out.println(new String(resp.payload));
			
			while(resp.hasMoreData()) {
				resp = httpConnection.readMoreHttpResponse();
				System.out.println(new String(resp.payload));				
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
