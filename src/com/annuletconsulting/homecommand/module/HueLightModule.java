/*
 * Copyright (C) 2013 Annulet Consulting, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.annuletconsulting.homecommand.module;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import com.annuletconsulting.homecommand.server.HomeComandProperties;

/**
 * Uses hue_bridge_ip and hue_bridge_username in the properties file.  
 * 
 * @author Walt Moorhouse
 */
public class HueLightModule extends Module {
	public static final String GET = "GET";
	public static final String PUT = "PUT";
	private static final String DELIM = ":";
	private String response = "";
	private static String[] lightNames = null;
	
	public HueLightModule() {
    	super();
		String[] output = sendJSON(GET, "lights", null);
		StringBuffer lights = new StringBuffer();
		if (output[0].equals("200")) {
	        while (output[1].indexOf("name") != -1) {        
	            int elementIndex = output[1].indexOf("name");
	            int startIndex = output[1].indexOf("\"", elementIndex+6);
	            int endIndex = output[1].indexOf("\"", startIndex+1);
	            lights.append(output[1].substring(startIndex+1, endIndex).toUpperCase());
	            output[1] = output[1].substring(endIndex);
//	            if (output[1].indexOf("name") != -1)
	            lights.append(DELIM);
	        }
	    lightNames = lights.toString().split(DELIM);
		} else {
			System.err.println("HomeCommandServer, HueLightModule: Unable to load light names.");
		}
	}

	@Override
	public int getKeyWordLocation() {
		return 0;
	}

	@Override
	public String[] getCommands() {
		String[] commands = {"LIGHTS", "LIGHT", "ILLUMINATION", "TURN", 
				"LIFE", "LIVE", "LIES", "LICE"}; //Google sometimes hears "living room" and changes light to life or live
		return commands;
	}

	@Override
	public boolean requiresAccessCode() {
		return false;
	}

	@Override
	public int run(String[] input) {
		try {
			response = "";
			int percent = getIndexOf("PERCENT", input);
			int colorIndex = getIndexOf("COLOR", input);
			int moodIndex = getIndexOf("MOOD", input);
			int alertIndex = getIndexOf("ALERT", input);
			int in = getIndexOf("IN", input);
			String lights[] = null;
			if (in >= 0)
				lights = getLightIds(input[in+1]);
			else
				lights = getLightIds("");
			log.delete(0, log.length());
			if (getIndexOf("OFF", input) >= 0)
				log.append(turnOff(lights));
			else {
				if (getIndexOf("ON", input) >= 0)
					log.append(turnOn(lights));
				if (moodIndex >= 0)
					log.append(setMood(lights, input[moodIndex+2]));
				if (alertIndex >= 0)
					log.append(setAlert(lights, input[alertIndex-1]));
				if (percent > 0)
					log.append(setLightPercentage(input[percent-1], lights));
				if (colorIndex >= 0 && colorIndex+2 < input.length)
					log.append(changeColor(input[colorIndex+2], lights));
			}
		} catch (Exception e) {
			log.append(e.getMessage());
			response = "ERROR";
			return ERROR;
		}
		return SUCCESS;
	}

	private String setAlert(String[] lights, String alertColor) {
		StringBuffer respns = new StringBuffer();
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			StringBuffer body = new StringBuffer();
			body.append("{\"hue\":");
			if (alertColor.equalsIgnoreCase("RED"))
				body.append("0, \"sat\":254,\"ct\": 451,\"alert\": \"lselect\",\"effect\": \"none\",\"colormode\": \"ct\",\"xy\":[0.6477, 0.3314]");
			else if (alertColor.equalsIgnoreCase("BLUE"))
				body.append("46920, \"sat\":254,\"ct\": 500,\"alert\": \"lselect\",\"effect\": \"none\",\"colormode\": \"ct\",\"xy\":[0.167, 0.04]");
			else if (alertColor.equalsIgnoreCase("YELLOW"))
				body.append("22450, \"sat\":254, \"alert\": \"lselect\",\"effect\": \"none\"");
			body.append("}");
			String[] result = sendJSON(PUT, cmd.toString(), body.toString());
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" IS NOW ALERTING ");
				respns.append(alertColor);
				respns.append(" ");
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	private Object setMood(String[] lights, String mood) {
		StringBuffer respns = new StringBuffer();
		int count = 0;
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			String[] result = {"999", "NO MOOD MATCHED"};
			if ("PARTY".equals(mood))
				result = sendJSON(PUT, cmd.toString(), "{\"effect\": \"colorloop\"}");
			else if ("POLICE".equals(mood)) {
				if (count++%2 == 1)
					result = sendJSON(PUT, cmd.toString(), "{\"hue\":0, \"sat\":254,\"ct\": 451,\"alert\": \"lselect\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.6477, 0.3314]}");
				else 
					result = sendJSON(PUT, cmd.toString(), "{\"hue\":46920, \"sat\":254,\"ct\": 500,\"alert\": \"lselect\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.167, 0.04]}");
			}
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" MOOD SET TO ");
				respns.append(mood);
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	private String[] getLightIds(String name) {
		StringBuffer lights = new StringBuffer();
		for (int x=0; x<lightNames.length; x++) {
			if (lightNames[x].contains(name)) {
				lights.append(x+1);  // Lights start at 1
				lights.append(DELIM);
			}
		}
		return lights.toString().split(DELIM);
	}

	private String turnOff(String[] lights) {
		StringBuffer respns = new StringBuffer();
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			String[] result = sendJSON(PUT, cmd.toString(), "{\"on\":false}");
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" IS NOW OFF ");
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	private String turnOn(String[] lights) {
		StringBuffer respns = new StringBuffer();
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			String[] result = sendJSON(PUT, cmd.toString(), "{\"on\":true}");
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" IS NOW ON ");
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	private String setLightPercentage(String percent, String[] lights) {
		StringBuffer respns = new StringBuffer();
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			StringBuffer body = new StringBuffer();
			body.append("{\"bri\":");
			body.append(Integer.parseInt(percent)/100*255);
			body.append("}");
			String[] result = sendJSON(PUT, cmd.toString(), body.toString());
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" IS NOW AT ");
				respns.append(percent);
				respns.append(" PERCENT BRIGHTNESS ");
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	private String changeColor(String color, String[] lights) {
		StringBuffer respns = new StringBuffer();
		for (String light :  lights) {
			StringBuffer cmd = new StringBuffer();
			cmd.append("lights/");
			cmd.append(light);
			cmd.append("/state");
			StringBuffer body = new StringBuffer();
			body.append("{\"hue\":");
			if (color.equalsIgnoreCase("WHITE"))
				body.append("34250, \"sat\":110,\"ct\": 210,\"alert\": \"none\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.3523, 0.3572]");
			else if (color.equalsIgnoreCase("RED"))
				body.append("0, \"sat\":254,\"ct\": 451,\"alert\": \"none\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.6477, 0.3314]");
			else if (color.equalsIgnoreCase("BLUE"))
				body.append("46920, \"sat\":254,\"ct\": 500,\"alert\": \"none\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.167, 0.04]");
			else if (color.equalsIgnoreCase("GREEN"))
				body.append("25500, \"sat\":254,\"ct\": 267,\"alert\": \"none\",\"effect\": \"none\",\"colormode\": \"xy\",\"xy\":[0.3942, 0.4884]");
			else if (color.equalsIgnoreCase("YELLOW"))
				body.append("22450, \"sat\":254, \"alert\": \"none\",\"effect\": \"none\"");
			else if (color.equalsIgnoreCase("PURPLE"))
				body.append("45925, \"sat\":254,\"alert\": \"none\",\"effect\": \"none\"");
			else if (color.equalsIgnoreCase("ORANGE"))
				body.append("12500, \"sat\":254,\"alert\": \"none\",\"effect\": \"none\"");
			else if (color.equalsIgnoreCase("TEAL"))
				body.append("30900, \"sat\":254,\"alert\": \"none\",\"effect\": \"none\"");
			else if (color.equalsIgnoreCase("PINK"))
				body.append("53100, \"sat\":254,\"alert\": \"none\",\"effect\": \"none\"");
			body.append("}");
			String[] result = sendJSON(PUT, cmd.toString(), body.toString());
			if (result[0].equals("200")) {
				respns.append(lightNames[Integer.parseInt(light)-1]);
				respns.append(" IS NOW ");
				respns.append(color);
				respns.append(" ");
			} else {
				respns.append("ERROR ");
				respns.append(result[1]);
			}
		}
		return respns.toString();
	}

	@Override
	public String getErrorMessage() {
		return response;
	}

	@Override
	public String getShortTextResponse() {
		return response;
	}

	@Override
	public String getFullTextResponse() {
		return response;
	}

	@Override
	public String getSpeechResponse() {
		return response;
	}

	@Override
	public String getHTMLResponse() {
		return null;
	}

	@Override
	public String getURLResponse() {
		return null;
	}
    
    private String[] sendJSON(String method, String command, String json) {
    	String[] ret = {"999", "NO CONNECTION"};
    	try {
    		StringBuffer url = new StringBuffer();
    		url.append("http://");
    		url.append(HomeComandProperties.getInstance().getHueBridgeIp());
    		url.append("/api/");
    		url.append(HomeComandProperties.getInstance().getHueBridgeUser());
    		url.append("/");
    		url.append(command);
	    	HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
	    	conn.setDoOutput(true);
			conn.setRequestMethod(method);
	    	conn.addRequestProperty("Content-Type", "application/json");
	    	if (json != null) {
		    	OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		    	out.write(json);
		    	out.close();
	    	}
			ret[0] = String.valueOf(conn.getResponseCode());
			ret[1] = getResponse(conn);
		} catch (Exception e) {
			ret[0] = "500";
			ret[1] = e.getMessage();
			e.printStackTrace();
		}
    	return ret;
    }

    protected static String getResponse(HttpURLConnection connection) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    	InputStream inputStream = null;
    	try {
    		baos = new ByteArrayOutputStream(1024);
    		int length = 0;
    		byte[] buffer = new byte[1024];

    		if ( connection.getResponseCode() == 200) {
    			inputStream  = connection.getInputStream(); 
    		} else {
    			inputStream = connection.getErrorStream();
    		}            
    		while ((length = inputStream.read(buffer)) != -1) {
    			baos.write(buffer, 0, length);
    		}
    		return baos.toString();
    	}
    	catch ( Exception exception ) {
    		return "500";
    	}
    	finally {
    		try {
    			baos.close();
    		}
    		catch ( Exception exception ) {
    		}
    	}
    }
}