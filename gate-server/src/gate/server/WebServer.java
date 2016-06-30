package gate.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class WebServer {
	
	private HttpServer mServer;
	private Properties mConfig;
	
	public WebServer(Properties config, RequestHandler requestHandler) throws IOException {
		
		mConfig = config;
		
		String host = mConfig.getProperty("host", "localhost");
		int port = Integer.parseInt(mConfig.getProperty("port", "8000"));

		try {
			mServer = HttpServer.create(new InetSocketAddress(host, port), 0);
		} catch (IOException e) {
			BufferedInputStream tmp = new BufferedInputStream(new URL("http://localhost:"+port+"/exit").openStream());
			tmp.close();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			mServer = HttpServer.create(new InetSocketAddress(port), 0);
		}
		
		mServer.createContext("/", requestHandler);
		mServer.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
	
	}
	
	public void start() {
		mServer.start();
		Runtime.getRuntime().addShutdownHook(new OnShutdown());
		System.out.println("Server started");
	}
	
	public void stop() {
		mServer.stop(1);
		System.out.println("Server stopped");
	}
	
	/* Responds to a JVM shutdown by stopping the server. */
	class OnShutdown extends Thread {
	    public void run() {
	        WebServer.this.stop();
	    }
	}	
}
