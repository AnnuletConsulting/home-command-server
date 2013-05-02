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

package com.annuletconsulting.homecommand.server;

import java.util.Properties;

public class HomeComandProperties {
	private String wolframAlphaApiKey = null;
	private String serverPort = null;
	private String nonJavaUserDir = null;
	private String hueBridgeIp = null;
	private String hueBridgeUser = null;
	private String sharedKey = null;

    private static Properties properties = new Properties();
    private static HomeComandProperties instance = null;
    
    public static HomeComandProperties getInstance() {
        if (instance == null) {
            instance = new HomeComandProperties();
        }
        return instance;
    }    
              
    public HomeComandProperties() {
        try {
    	    properties.load(this.getClass().getResourceAsStream("HomeCommand.properties"));
    	    
    	    serverPort = properties.getProperty("server_port");
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
        try {
    	    wolframAlphaApiKey = properties.getProperty("wolfram_alpha_api_key");
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
        try {
    	    nonJavaUserDir = properties.getProperty("non_java_user_module_directory");
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
        try {
    	    hueBridgeIp = properties.getProperty("hue_bridge_ip");
    	    hueBridgeUser = properties.getProperty("hue_bridge_username");
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
        try {
    	    sharedKey = properties.getProperty("shared_key");
        } catch (Exception exception) {
        	exception.printStackTrace();
        }
    }
    
    /**
     * This allows you to use this class to load properties for your own modules without adding 
     * specific getters for them.
     * 
     * @param propertyName
     * @return
     */
	public String getOtherProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}

	/**
	 * @return the wolframAlphaApiKey
	 */
	public String getWolframAlphaApiKey() {
		return wolframAlphaApiKey;
	}

	/**
	 * @return the serverPort
	 */
	public String getServerPort() {
		return serverPort;
	}

	/**
	 * @return the nonJavaUserDir
	 */
	public String getNonJavaUserDir() {
		return nonJavaUserDir;
	}

	/**
	 * @return the hueBridgeUser
	 */
	public String getHueBridgeUser() {
		return hueBridgeUser;
	}
	
	/**
	 * @return the hueBridgeIp
	 */
	public String getHueBridgeIp() {
		return hueBridgeIp;
	}

	/**
	 * @return the sharedKey
	 */
	public String getSharedKey() {
		return sharedKey;
	}
}