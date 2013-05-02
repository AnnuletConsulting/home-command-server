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

import java.util.HashMap;
import com.annuletconsulting.homecommand.server.HomeComandProperties;
import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASound;
import com.wolfram.alpha.WASubpod;

/**
 * Uses wolfram_alpha_api_key in the properties file.  This must be a valid key or the module will not load.
 * 
 * @author Walt Moorhouse
 */
public class QuestionModule extends Module {
    private static String appid = HomeComandProperties.getInstance().getWolframAlphaApiKey();
    private static WAEngine engine = new WAEngine();
	private static String errorMessage = null;
	private static String shortTextResponse = null;
	private static String fullTextResponse = null;
	private static String htmlResponse = null;
	private static String urlResponse = null;
	private static HashMap<String, WASound> waSounds = new HashMap<String, WASound>();
	private static HashMap<String, WAImage> waImages = new HashMap<String, WAImage>();
    
    public QuestionModule() {
    	super();
        engine.setAppID(appid);
        engine.addFormat("plaintext");
//        engine.addFormat("html");
//        engine.addFormat("image");
//        engine.addFormat("sound");
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
	public int run(String[] words) {
		WAQuery query = engine.createQuery();
		StringBuffer input = new StringBuffer();
		for (String word : words) {
			input.append(word);
			input.append(" ");
		}
		input.deleteCharAt(input.length()-1); //remove last " "
	    query.setInput(input.toString());
        try {
            urlResponse = engine.toURL(query);
            WAQueryResult queryResult = engine.performQuery(query);
            
            if (queryResult.isError()) {
                errorMessage = "error code: " + queryResult.getErrorCode()
                		 	 + " error message: " + queryResult.getErrorMessage();
                return SERVICE_ERROR;
            } else if (!queryResult.isSuccess()) {
            	errorMessage = "Query was not understood; no results available.";
            	return NO_RESULTS;
            } else {
                for (WAPod pod : queryResult.getPods()) {
                    if (!pod.isError()) {
                    	fullTextResponse = append(fullTextResponse, pod.getTitle());
                        for (WASubpod subpod : pod.getSubpods()) {
                            for (Object element : subpod.getContents()) {
                            	if (element instanceof WAPlainText) {
                            		fullTextResponse = append(fullTextResponse, ((WAPlainText)element).getText());
                                	if (pod.getTitle().equals("Result"))
                                		shortTextResponse = append(shortTextResponse, ((WAPlainText)element).getText());
                                } else if (element instanceof WASound) {
                                	waSounds.put(pod.getTitle(), (WASound)element);
                                } else if (element instanceof WAImage) {
                            		waImages.put(pod.getTitle(), (WAImage)element);
                                }
                            }
                        }
                    }
                }
            }
        } catch (WAException e) {
        	errorMessage = e.getMessage();
        	return SERVICE_ERROR;
        }
		return SUCCESS;
	}
	
	public HashMap<String, WASound> getSounds() {
		return waSounds;
	}

	private String append(String main, String addition) {
		if (main == null)
			main = "";
		return main + " "+addition;
	}

	@Override
	public String getShortTextResponse() {
		return shortTextResponse;
	}

	@Override
	public String getFullTextResponse() {
		return fullTextResponse;
	}

	@Override
	public String getHTMLResponse() {
		return htmlResponse;
	}

	@Override
	public String getURLResponse() {
		return urlResponse;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage ;
	}

	@Override
	public String[] getCommands() {
		String[] commands = {"QUESTION", "HOW", "WHY", "WHEN", "WHO", "WHAT"};
		return commands;
	}

	@Override
	public String getSpeechResponse() {
		return shortTextResponse;
	}
}