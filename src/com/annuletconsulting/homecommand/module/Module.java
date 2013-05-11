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

public abstract class Module {
	public static final int SUCCESS 	  	= 0;
	public static final int ERROR   	  	= 1;
	public static final int INVALID_PARAMS 	= 2;
	public static final int SERVICE_ERROR 	= 3;
	public static final int NO_RESULTS	 	= 4;
	public static final int NO_ACCESS_CODE 	= 5;
	private static Module instance;
	protected StringBuffer log = new StringBuffer();

	public Module() {
		instance = this;
	}
	
	public static Module getInstance() {
		return instance;
	}
	
	public abstract String[] getCommands();
	public abstract boolean requiresAccessCode();
	public abstract int run(String[] input);
	public abstract String getErrorMessage();
	public abstract String getShortTextResponse();
	public abstract String getFullTextResponse();
	public abstract String getHTMLResponse();
	public abstract String getURLResponse();
	public abstract String getAudioStreamUrl();
	public abstract String getSpeechResponse();
	public abstract int getKeyWordLocation();
	
	public String getLogText() {
		return log.toString();
	}
	
	public int getIndexOf(String item, String[] list) {
		for (int x=0; x<list.length; x++) {
			if (item.equalsIgnoreCase(list[x]))
				return x;
		}
		return -1;
	}
}
