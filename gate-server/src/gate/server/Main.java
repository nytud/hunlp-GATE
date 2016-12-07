package gate.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Main {

	private RequestHandler handler;
	private WebServer server;

	public Main(String config) {

		try {
			File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			if ("bin".equals(jarFile.getName())) { // workaround for running without jar
				jarFile = jarFile.getParentFile();
			}

			File configFile = new File(config);
			if (!configFile.isAbsolute()) {
				configFile = new File(jarFile.getParentFile(), config);
			}

			FileInputStream is = new FileInputStream(configFile);
			Properties props = new Properties();
			props.load(is);

			handler = new RequestHandler(props);
			server  = new WebServer(props, handler);

			server.start();

		} catch (FileNotFoundException e) {
			System.err.println("Error: configuration file not found - " + config );
		} catch (IOException e) {
			System.err.println("Error: could not parse configuration file");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new Main(args.length > 0 ? args[0] : "gate-server.props");
	}

}
