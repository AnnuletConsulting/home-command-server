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

/**
 * Multi-Step modules will be used when a command needs to use a series of requests
 * and responses that are used to choose from options or start doing something that
 * you will need to stop or check on later.
 * 
 * First call start() then call step() 0...N times, then call end() or hit MaxWaitTime.
 * Example would be 
 * Computer, Music Search, Artist Green Day
 * "There are X Albums and Y Songs by Green Day in your library."
 * List Albums
 * "Green Day Albums available are: A, B, C..."
 * Shuffle All
 * *Music plays*
 * Computer stop music
 * *Music stops*
 * 
 * @author Walt Moorhouse
 */
public interface MultiStepModule {
	public String[] getMultiStepStartCommands();
	public String[] getMultiStepLoopCommands();
	public String[] getMultiStepEndCommands();
	public boolean requiresAccessCodeForMultiStep();
	public int start(String input);
	public int step(String input);
	public int end(String input);
	public int getMaxWaitTime();
	public String getErrorMessage();
	public String getShortTextResponse();
	public String getFullTextResponse();
	public String getStreamUrl();
}
