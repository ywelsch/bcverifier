package de.unikl.bcverifier.web;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import de.unikl.bcverifier.Configuration;

public class ConfigSession extends WebSession {
	private final Configuration config = new Configuration();
	final List<String> params = new ArrayList<String>();
	final Map<String,ParameterDescription> paramMap = new HashMap<String,ParameterDescription>();
	
	public ConfigSession(Request request) {
		super(request);
		JCommander parser = new JCommander(config);
        List<ParameterDescription> actParams = parser.getParameters();
        for (ParameterDescription param : actParams) {
        	Field f = param.getField(); 
        	if (f.isAnnotationPresent(WebGUI.class)) {
        		Class<?> t = f.getType();
        		if (t.equals(Boolean.TYPE) || t.equals(Integer.TYPE) || t.isEnum() || t.equals(String.class)) {
        			paramMap.put(param.getNames(), param);
        			params.add(param.getNames());
        		}
        	}
        }
        config.setWebDefaults();
	}
	
	public Configuration getConfig() {
		return config;
	}
	
	public static ConfigSession get() {
		return (ConfigSession)WebSession.get();
	}

	public List<String> getParams() {
		return params;
	}

	public ParameterDescription getParam(String param) {
		return paramMap.get(param);
	}

	public void put(String param, Object object) {
		Field f = paramMap.get(param).getField();
		f.setAccessible(true);
		try {
			f.set(ConfigSession.get().getConfig(), object);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Object get(String param) {
		Field f = ConfigSession.get().getParam(param).getField();
		f.setAccessible(true);
		try {
			return f.get(ConfigSession.get().getConfig());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
