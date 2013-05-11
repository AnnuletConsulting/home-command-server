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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class MathModule extends MultiStepModule {
	private String streamUrl;
	private String urlResponse;
	private String htmlResponse;
	private String fullTextResponse;
	private String shortTextResponse;
	private String errorMsg;
	private static boolean multiStepStarted = false;
	
	private static final String CALCULATE = "CALCULATE";
	private static final String MULTIPLY = "MULTIPLY";
	private static final String DIVIDE = "DIVIDE";
	private static final String ADD = "ADD";
	private static final String SUM = "SUM";
	private static final String SUBTRACT = "SUBTRACT";
	private static double total = 0;
	private static final String REMAINDER = "REMAINDER";
	private static final String POWER = "POWER";
	private static final String ROOT = "ROOT";
	private static final String SQUARED = "SQUARED";
	private static final String CUBED = "CUBED";
	private static final String TOTAL = "TOTAL";
	private static final String SUBTOTAL = "SUBTOTAL";
	private static final String SQUARE = "SQUARE";
	private static final String CUBE = "CUBE";
	private static final String TIMES = "TIMES";
	private static final HashMap<String, String> numberWords = new HashMap<String, String>();
	private static final String ANSWER = "ANSWER";
	
	public MathModule() {
    	super();
    	numberWords.put("ONE", "1");
    	numberWords.put("WON", "1");
    	numberWords.put("TWO", "2");
    	numberWords.put("TO", "2");
    	numberWords.put("TOO", "2");
    	numberWords.put("THREE", "3");
    	numberWords.put("FOUR", "4");
    	numberWords.put("FIVE", "5");
    	numberWords.put("SIX", "6");
    	numberWords.put("SEVEN", "7");
    	numberWords.put("EIGHT", "8");
    	numberWords.put("ATE", "8");
    	numberWords.put("NINE", "9");
    	numberWords.put("TEN", "10");
	}

	@Override
	public int getKeyWordLocation() {
		return 0;
	}

	@Override
	public String[] getCommands() {
		String[] cmds = {CALCULATE, MULTIPLY, DIVIDE, ADD, SUBTRACT};
		return cmds;
	}

	@Override
	public String[] getMultiStepLoopCommands() {
		String[] loopcmds = {REMAINDER, MULTIPLY, DIVIDE, ADD, SUM, SUBTRACT, SUBTOTAL};
		return loopcmds;
	}

	@Override
	public String[] getMultiStepEndCommands() {
		String[] msse = {"STOP", TOTAL };
		return msse;
	}

	@Override
	public int run(String[] words) {
		multiStepStarted = false;
		if (words[0].equals(CALCULATE) || words[0].equals("START")) {
			if (words[0].equals("START"))
				multiStepStarted = true;
			// Remove the word calculate or Start
			String[] tmpWords = new String[words.length-1];
			for (int w=0; w<tmpWords.length; w++)
				tmpWords[w] = words[w+1];
			words = tmpWords;
		}
		words = convertAnyTextWordsIntoNumbers(words);
		words = combineAdjacentNumbers(words);
		return parseWords(words);
	}
	
	private int parseWords(String[] words) {
		if (contains(words, ADD) || contains(words, SUM))
			return add(words);
		if (contains(words, "PLUS"))
			return add(words.length==3?addBlankInFront(words):words);
		if (contains(words, SUBTRACT))
			return subtract(words);
		if (contains(words, MULTIPLY) || contains(words, TIMES))
			return multiply(words);
		if (contains(words, DIVIDE))
			return divide(words);
		if (contains(words, REMAINDER))
			return remainder(words);
		if (contains(words, ROOT))
			return root(words);
		if (contains(words, POWER) || contains(words, SQUARED) || contains(words, SQUARE) || 
				contains(words, CUBED) || contains(words, CUBE))
			return exponent(words);
		return INVALID_PARAMS;
	}

	private String[] addBlankInFront(String[] words) {
		String[] out = new String[words.length+1];
		for (int i=0; i<words.length; i++)
			out[i+1] = words[i];
		return out;
	}

	private String[] combineAdjacentNumbers(String[] words) {
		ArrayList<String> combinedList = new ArrayList<String>();
		StringBuffer tmpNumber = new StringBuffer();
		for (int count = 0; count<words.length; count++) {
			if (isNumeric(words[count])) {
				tmpNumber.append(words[count]);
			} else {
				if (tmpNumber.length() > 0) {
					combinedList.add(tmpNumber.toString());
					tmpNumber.setLength(0);
				}
				combinedList.add(words[count]);
			}
		}
		// If the end of the string was a number it won't have been added.
		if (tmpNumber.length() > 0) {
			combinedList.add(tmpNumber.toString());
			tmpNumber.setLength(0);
		}
		return arrayListToStringArray(combinedList);
	}

	private String[] arrayListToStringArray(ArrayList<String> list) {
		String[] out = new String[list.size()];
		Iterator<String> it = list.iterator();
		int counter = 0;
		while (it.hasNext()) {
			out[counter++] = it.next();
		}
		return out;
	}

	private boolean isNumeric(String string) {
		try {
			Integer.valueOf(string);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private String[] convertAnyTextWordsIntoNumbers(String[] words) {
		for (int i=0; i<words.length; i++) {
			if (numberWords.containsKey(words[i]))
				words[i] = numberWords.get(words[i]);
		}
		return words;
	}

	private boolean contains(String[] words, String word) {
		return Arrays.asList(words).contains(word);
	}

	private int indexOf(String[] words, String word) {
		return Arrays.asList(words).indexOf(word);
	}

	private int root(String[] words) {
		if (words.length == 4) {
			try {
				if (SQUARE.equals(words[0]))
					total = Math.sqrt(Double.valueOf(words[3]));
				else if (CUBE.equals(words[0]))
					total = Math.pow(Double.valueOf(words[3]), -3);
				else 
					throw new Exception("Format Unknown");
				StringBuffer sb = new StringBuffer();
				sb.append("THE ANSWER IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR, DID YOU SAY IN THE FORMAT OF, SQUARE (OR CUBE) ROOT OF 456";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex math than SQUARE (OR CUBE OR NTH) ROOT OF 456?
		return INVALID_PARAMS;
	}

	private int exponent(String[] words) {
		if (words.length == 2) {
			try {
				total = Math.pow(Integer.parseInt(words[0]), (words[1].equals(SQUARED)?2:words[1].equals(CUBED)?3:0));
				StringBuffer sb = new StringBuffer();
				sb.append("THE ANSWER IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR, DID YOU SAY IN THE FORMAT OF, 10 SQUARED OR 10 CUBED?";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		}else if (words.length == 6) {
			try {
				total = Math.pow(Integer.parseInt(words[0]), Integer.parseInt(words[5]));
				StringBuffer sb = new StringBuffer();
				sb.append("THE ANSWER IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR, DID YOU SAY IN THE FORMAT OF, 10 TO THE POWER OF 3";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex math than 10 TO THE POWER OF 3
		return INVALID_PARAMS;
	}

	private int remainder(String[] words) {
		if (words.length == 6) {
			try {
				total = Integer.parseInt(words[2]) % Integer.parseInt(words[5]);
				StringBuffer sb = new StringBuffer();
				sb.append("THE REMAINDER IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR, DID YOU SAY IN THE FORMAT OF, REMAINDER OF 10 DIVIDED BY 3";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex math than REMAINDER OF 10 DIVIDED BY 3
		return INVALID_PARAMS;
	}

	private int divide(String[] words) {
		if (words.length == 4) {
			try {
				total = Integer.parseInt(words[1]) / Integer.parseInt(words[3]);
				StringBuffer sb = new StringBuffer();
				sb.append("THE RESULT IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex math than DIVIDE 241 BY 13
		return INVALID_PARAMS;
	}

	private int multiply(String[] words) {
		if (words.length == 4) {
			try {
				total = Integer.parseInt(words[1]) * Integer.parseInt(words[3]);
				StringBuffer sb = new StringBuffer();
				sb.append("THE RESULT IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex math than MULTIPLY 241 AND 1341324
		return INVALID_PARAMS;
	}

	private int subtract(String[] words) {
		if (words.length == 4) {
			try {
				total = Integer.parseInt(words[3]) - Integer.parseInt(words[1]);
				StringBuffer sb = new StringBuffer();
				sb.append("THE TOTAL IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex subtraction than SUBTRACT 241 FROM 1341324
		return INVALID_PARAMS;
	}

	private int add(String[] words) {
		if (words.length == 4) {
			try {
				total = Integer.parseInt(words[1]) + Integer.parseInt(words[3]);
				StringBuffer sb = new StringBuffer();
				sb.append("THE TOTAL IS ");
				sb.append(total);
				shortTextResponse = sb.toString();
				return SUCCESS;
			} catch (Exception e) {
				shortTextResponse = "THERE WAS AN ERROR";
				errorMsg = e.getMessage();
				e.printStackTrace();
				return ERROR;
			}
		} //TODO add code to do more complex addition than ADD 241 AND 1341324
		return INVALID_PARAMS;
	}

	@Override
	public int getMaxWaitTime() {
		return -1; //infinite wait
	}

	@Override
	public String getAudioStreamUrl() {
		return streamUrl;
	}

	@Override
	public String getURLResponse() {
		return urlResponse;
	}

	@Override
	public String getErrorMessage() {
		return errorMsg;
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
	public String getSpeechResponse() {
		return shortTextResponse;
	}

	@Override
	public boolean requiresAccessCode() {
		return false;
	}

	@Override
	public boolean requiresAccessCodeForMultiStep() {
		return false;
	}

	@Override
	public int step(String[] words) {
		words = convertAnyTextWordsIntoNumbers(words);
		words = combineAdjacentNumbers(words);
		// If we used the ANSWER placeholder, put the total there.
		if (contains(words, ANSWER)) {
			words[indexOf(words, ANSWER)] = String.valueOf(total);
			return run(words);
		}
		// Otherwise, we have to guess where to put the total. 
		if (contains(words, ADD) || contains(words, SUM))
			return add(appendTotalAfterSpace(words));
		if (contains(words, SUBTRACT))
			return subtract(appendTotalAfterSpace(words));
		if (contains(words, MULTIPLY) || contains(words, TIMES))
			return multiply(appendTotalAfterSpace(words));
		if (contains(words, DIVIDE)) {
			if (contains(words, "BY"))
				return divide(appendTotalAfterSpace(words));
			else
				return divide(appendTotalBeforeSpace(words));
		}
//		if (contains(words, REMAINDER))
//			return remainder(words);
//		if (contains(words, ROOT))
//			return root(words);
//		if (contains(words, POWER) || contains(words, SQUARED) || contains(words, SQUARE) || 
//				contains(words, CUBED) || contains(words, CUBE))
//			return exponent(words);
		return INVALID_PARAMS;
	}

	private String[] appendTotalBeforeSpace(String[] words) {
		String[] newWords = new String[words.length+2];
		for (int i=0; i<words.length-1; i++)
			newWords[i] = words[i];
		newWords[words.length-1] = String.valueOf(total);
		newWords[words.length+1] = words[words.length-1];
		return newWords;
	}

	private String[] appendTotalAfterSpace(String[] words) {
		String[] newWords = new String[words.length+2];
		for (int i=0; i<words.length; i++)
			newWords[i] = words[i];
		newWords[words.length+1] = String.valueOf(total);
		return newWords;
	}

	@Override
	public int end(String[] input) {
		// We already have the total so shouldn't need to do anything.
		return 0;
	}

	@Override
	public int getMultiStepKeyWordLocation() {
		return 0;
	}

	@Override
	public boolean isMultiStepStarted() {
		return multiStepStarted ;
	}
}
