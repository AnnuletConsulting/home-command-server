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

public class SetUpModule extends Module {
	private String response;
	
	@Override
	public String[] getCommands() {
		String[] cmds = {"SETUP"};
		return cmds;
	}

	@Override
	public int getKeyWordLocation() {
		return 0;
	}

	@Override
	public boolean requiresAccessCode() {
		return false;
	}

	@Override
	public int run(String[] input) {
		response = "You are using Annulet Consulting's Home Command System. The voice setup system is not yet completed.";
		return SUCCESS;
	}

	@Override
	public String getErrorMessage() {
		return "";
	}

	@Override
	public String getShortTextResponse() {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public String getFullTextResponse() {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public String getHTMLResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getURLResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSpeechResponse() {
		return response;
	}

	@Override
	public String getAudioStreamUrl() {
		return null;
	}
}