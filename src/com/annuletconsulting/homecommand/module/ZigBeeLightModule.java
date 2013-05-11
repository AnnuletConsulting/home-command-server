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

public class ZigBeeLightModule extends Module {
	private String error = null;
	private String response = null;

	@Override
	public String[] getCommands() {
		String[] commands = {"ZIGBEE"};
		return commands;
	}

	@Override
	public boolean requiresAccessCode() {
		return false;
	}

	@Override
	public int run(String[] input) {
		// TODO [Living Room] lights are now {on | off}
		// use input.contains("on") to allow flexibility with commands.
		return 0;
	}

	@Override
	public String getErrorMessage() {
		return error;
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
	public String getHTMLResponse() {
		return null;
	}

	@Override
	public String getURLResponse() {
		return null;
	}

	@Override
	public String getSpeechResponse() {
		return response;
	}

	@Override
	public int getKeyWordLocation() {
		return 0;
	}

	@Override
	public String getAudioStreamUrl() {
		return null;
	}
}