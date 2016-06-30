package gate.server;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.util.SimpleFeatureMapImpl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class RequestHandler implements HttpHandler{
	
	private static class Module {
		String classname; FeatureMap config;
		public Module(String classname, FeatureMap config) { 
			this.classname = classname; this.config = config;
		}
	}
	
	protected Map<String,Module> mModules;  

	public RequestHandler(Properties config) throws Exception {
		Gate.init();

		File jarFile = new File(RequestHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		
		for (int n=1; ; ++n) {
			String plugin = config.getProperty("plugin"+n);
			if (plugin == null) break;
			Gate.getCreoleRegister().registerDirectories(new File(jarFile.getParentFile(),plugin).toURI().toURL());			
			System.out.println("Loaded plugin: "+plugin);
		}
		 
		mModules = new HashMap<>();
		for (int n=1; ; ++n) {
			String name = config.getProperty("module"+n+".name");
			if (name == null) break;

			Map<String,String> p = getParameters(config.getProperty("module"+n+".params"));
			SimpleFeatureMapImpl fm = new SimpleFeatureMapImpl();
			for (Map.Entry<String,String> p1 : p.entrySet()) {
				String v = p1.getValue();
				if ("true".equals(v)) {
					fm.put(p1.getKey(), true);
				} else if ("false".equals(v)) {
					fm.put(p1.getKey(), false);
				} else if (v.matches("^[+-]?[0-9]+$")) {
					fm.put(p1.getKey(), Integer.parseInt(v));
				} else if (v.matches("^[+-]?[0-9]+.[0-9]?$")) {
					fm.put(p1.getKey(), Float.parseFloat(v));
				} else {
					fm.put(p1.getKey(), v);					
				}
			}

			Module m = new Module(config.getProperty("module"+n+".class"), fm);
			if (m.classname == null) break;
			
			mModules.put(name, m);
			System.out.println("Loaded module: "+name+" - "+m.classname);			
		}
	}
		
	@Override
	public void handle(HttpExchange request) throws IOException {
		String path = request.getRequestURI().getPath().replaceAll("\\\\", "/").replaceAll("\\.\\./", "");
				
		try {
			if (process(path, request)) return;
		} catch (Exception e) {
			e.printStackTrace();
			sendError(request, 500, "500 Internal Server Error");
		}
		
		// Return 404
		sendError(request, 404, "404 '"+path+"' Not Found");
	}
	
	protected boolean process(String path, HttpExchange request) throws Exception {
		// Shutdown handling
		if (path.equals("/exit")) {
			sendError(request, 200, "200 OK");
			System.exit(0);
			return true;
		}
		
		if (path.equals("/process")) {
			
			Map<String,String> params = getParameters(request.getRequestURI().getRawQuery());
	        
			String run = params.getOrDefault("run","");
			String text = params.getOrDefault("text","");
			
			if (text.isEmpty()) {
				sendError(request, 400, "400 Missing parameter 'text'");
				return true;
			}
			if (run.isEmpty()) {
				sendError(request, 400, "400 Missing parameter 'run'");
				return true;
			}
			
			Document doc =  Factory.newDocument(text);
			
			for (String r : run.split(",")) {
				Module m = mModules.get(r);
				if (m == null) {
					sendError(request, 400, "400 Invalid module name: "+r);
					return true;
				}
				
				ProcessingResource res = (ProcessingResource) Factory.createResource( m.classname );
				res.setParameterValues(m.config);
				res.setParameterValue("document", doc);
				res.execute();
			}
			
			sendXML(request, doc.toXml());
			return true;
		}		
		
		return false;
	}
	
	protected void sendError(HttpExchange request, int errorCode, String text) {
		Headers h = request.getResponseHeaders();
		h.add("Content-Type", "text/plain;charset=utf-8");
		try {
			request.sendResponseHeaders(errorCode, text.length());
			OutputStream os = request.getResponseBody();
			os.write(text.getBytes());
			os.close();
		} catch (IOException e) {
			System.err.println("Connection closed before finished");
		}
	}

	protected void sendXML(HttpExchange request, String text) {
		Headers h = request.getResponseHeaders();
		h.add("Content-Type", "text/xml;charset=utf-8");
		try {
			request.sendResponseHeaders(200, 0);
			OutputStream os = request.getResponseBody();
			os.write(text.getBytes("UTF-8"));
			os.close();
		} catch (IOException e) {
			System.err.println("Connection closed before finished");
		}
	}
	
	protected Map<String,String> getParameters(String query) {
		Map<String, String> result = new HashMap<String, String>();
	    if (query == null) return result;
		try {
			for (String param : query.split("&")) {
		        String pair[] = param.split("=");
		        result.put(URLDecoder.decode(pair[0],"UTF-8"), pair.length>1 ? URLDecoder.decode(pair[1],"UTF-8") : "");
		    }
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    }
	    return result;
	}
}
