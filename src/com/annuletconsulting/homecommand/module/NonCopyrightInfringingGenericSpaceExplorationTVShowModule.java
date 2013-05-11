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

import java.util.Arrays;
import java.util.List;

public class NonCopyrightInfringingGenericSpaceExplorationTVShowModule extends Module {
	private static final String ALERT = "ALERT";
	private String response;
	private String error;
	
	@Override
	public String[] getCommands() {
		String[] cmds = {ALERT, "DESTRUCT"};
		return cmds;
	}

	@Override
	public int getKeyWordLocation() {
		return 1;
	}

	@Override
	public boolean requiresAccessCode() {
		return true;
	}

	@Override
	public int run(String[] input) {
		log.setLength(0);
		response = "PLEASE REPHRASE YOUR COMMAND CAPTAIN";
		int code = SUCCESS;
		if (requiresAccessCode() && !accessCodeCorrect(input)) {
			error = "";
			return NO_ACCESS_CODE;
		}
		if (input.length > 1 && input[1].equals(ALERT)) {
			HueLightModule hlm = new HueLightModule();
			code = hlm.run(input);
			response = hlm.getSpeechResponse();
			log.append(hlm.getLogText());
		} else {
			HueLightModule hlm = new HueLightModule();
			String[] redAlert = {"RED", "ALERT"};
			code = hlm.run(redAlert);
			response = "SELF DESTRUCT ACTIVATED";
			log.append(response);
		}
		return code;
	}

	private boolean accessCodeCorrect(String[] input) {
		List<String> list = Arrays.asList(input);
		return list.contains("ACCESS") && list.contains("CODE");
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
		// TODO if Red Alert or self destruct, send an audio stream url
		return null;
	}
}