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

import com.annuletconsulting.homecommand.server.MP3Streamer;

public class MusicModule extends Module {
	private String response;
	private String audioStreamUrl;
	private String error;
	
	@Override
	public String[] getCommands() {
		String[] cmds = {"PLAY", "MUSIC"};
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
		//TODO process voice command and search available music files to find a match.
		String path = "/home/walt/Music/Johnny_Cash-Hurt.mp3";
		audioStreamUrl = "192.168.1.3:8889";
		response = "BUFFERING"; //"You are using Annulet Consulting's Home Command System. The music module is not yet completed.";Runnable task = new MyRunnable(10000000L + i);
	    Thread worker = new Thread(new MP3Streamer(path, 8889));
	    worker.setName(path);
	    worker.start();
		return SUCCESS;
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
		return audioStreamUrl;
	}

	@Override
	public String getSpeechResponse() {
		return response;
	}

	@Override
	public String getAudioStreamUrl() {
		return audioStreamUrl;
	}
}