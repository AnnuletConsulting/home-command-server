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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.annuletconsulting.homecommand.module.*;

public class HomeCommand {
    private static final String ENCODING_FORMAT = "UTF8";
	private static final String SIGNATURE_METHOD = "HmacSHA256";
	
	public static final String LIGHTS = "lights";
	public static final String QUESTIONS = "questions";
	public static final String MATH = "math";
	public static final String TV_SHOW = "tvshow";
	private static final String AES = "AES";
	private static boolean end = false;
	private static int socket = 8888;
	private static String sharedKey = null;
	private static String userModulesPath = "user/modules";
	private static String nonJavaUserModulesPath = "user/nonjavamodules";
	private static HashMap<String, Module> modules = new HashMap<String, Module>();
	
	// This will contain any MultiStepModules that are currently stepping through a loop.
	private static HashMap<String, Module> activeMultiStepModule = new HashMap<String, Module>();
	private static String nodeType;

	/**
	 * This class will accept commands from a node in each room. For it to react to events on the server
	 * computer, it must be also running as a node.  However the server can cause all nodes to react
	 * to an event happening on any node, such as an email or text arriving.  A call on a node device
	 * could pause all music devices, for example.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			socket = Integer.parseInt(HomeComandProperties.getInstance().getServerPort());
			nonJavaUserModulesPath = HomeComandProperties.getInstance().getNonJavaUserDir();
		} catch (Exception e) {
			System.out.println("Error loading from properties file.");
			e.printStackTrace();
		}
		try {
			sharedKey = HomeComandProperties.getInstance().getSharedKey();
			if (sharedKey == null)
				System.out.println("shared_key is null, commands without valid signatures will be processed.");
		} catch (Exception e) {
			System.out.println("shared_key not found in properties file.");
			e.printStackTrace();
		}
		try {
			if (args.length > 0) {
				String arg0 = args[0];
				if (arg0.equals("help") || arg0.equals("?") || arg0.equals("usage") || arg0.equals("-help") || arg0.equals("-?")) {
					System.out.println("The defaults can be changed by editing the HomeCommand.properties file, or you can override them temporarily using command line options.");
					System.out.println("\nHome Command Server command line overrride usage:");
					System.out.println("hcserver [server_port] [java_user_module_directory] [non_java_user_module_directory]"); //TODO make hcserver.sh
					System.out.println("\nDefaults:");
					System.out.println("server_port: "+socket);
					System.out.println("java_user_module_directory: "+userModulesPath);
					System.out.println("non_java_user_module_directory: "+ nonJavaUserModulesPath);
					System.out.println("\n2013 | Annulet, LLC");
				}
				socket = Integer.parseInt(arg0);
			}
			if (args.length > 1)
				userModulesPath = args[1];
			if (args.length > 2)
				nonJavaUserModulesPath = args[2];
			
			modules.put(LIGHTS, new HueLightModule());
			modules.put(QUESTIONS, new QuestionModule());
			modules.put(MATH, new MathModule());
			modules.put(TV_SHOW, new NonCopyrightInfringingGenericSpaceExplorationTVShowModule());
			modules.putAll(NonJavaUserModuleLoader.loadModulesAt(nonJavaUserModulesPath));
	        ServerSocket ss = new ServerSocket(socket);
	        while(!end) {
                Socket s = ss.accept();
                InputStreamReader isr = new InputStreamReader(s.getInputStream());
                PrintWriter output = new PrintWriter(s.getOutputStream(), true);
                int character;
                StringBuffer inputStrBuffer = new StringBuffer();
                while((character = isr.read()) != 13) {
                    inputStrBuffer.append((char)character);
                }
                System.out.println(inputStrBuffer.toString());
                String[] cmd; // = inputStrBuffer.toString().split(" ");
                String result = "YOUR REQUEST WAS NOT VALID JSON";
                if (inputStrBuffer.substring(0, 1).equals("{")) {
                	nodeType = extractElement(inputStrBuffer.toString(), "node_type");
                	if (sharedKey != null) {
                		if (validateSignature(extractElement(inputStrBuffer.toString(), "time_stamp"), extractElement(inputStrBuffer.toString(), "signature"))) {
                			if ("Y".equalsIgnoreCase(extractElement(inputStrBuffer.toString(), "cmd_encoded")))
                				cmd = decryptCommand(extractElement(inputStrBuffer.toString(), "command"));
                			else
                                cmd = extractElement(inputStrBuffer.toString(), "command").split(" ");
                			result = getResult(cmd);
                		} else
                			result = "YOUR SIGNATURE DID NOT MATCH, CHECK SHARED KEY";
                	} else {
                        cmd = extractElement(inputStrBuffer.toString(), "command").split(" ");
                		result = getResult(cmd);
                	}
                }
                System.out.println(result);
                output.print(result);
                output.print((char) 13);
                output.close();
                isr.close();
                s.close();
	        }
	        ss.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}

	/**
	 * TODO write decryption algorithm
	 */
	private static String[] decryptCommand(String encryptedString) {
		return encryptedString.split(" ");
	}

	private static boolean validateSignature(String timeStamp, String signature) {
		return signature.equals(getSignature(timeStamp));
	}

	private static String getSignature(String timeStamp) {
    	try {
			byte[] data = timeStamp.getBytes(ENCODING_FORMAT);
			Mac mac = Mac.getInstance(SIGNATURE_METHOD);
			mac.init(new SecretKeySpec(sharedKey.getBytes(ENCODING_FORMAT), SIGNATURE_METHOD));
			char[] signature = Hex.encodeHex(mac.doFinal(data));
			return new String( signature );
		}
		catch ( Exception exception ) {
			return "Error in getSignature()";
		}
	}

	public static void stop() {
		end = true;
	}

	/**
	 * This should process the commands sent from the remote node.  Some 
	 * commands require Access Code, this is sent as part of the command string.
	 * 
	 * $COMMAND $OPTIONS $ACCESS_DELIMITER_PHRASE $ACCESS_CODE
	 * 
	 * $NODE_ACCESS_WORD should not be included.
	 * 
	 * Lights On command that requires an access code would be something like
	 * Lights On Access Code 1 2 3 4 A B C D Red Blue Green
	 * 
	 * @param words
	 * @return
	 */
	private static String getResult(String[] words) {
		// TODO Handle MultiStepModules
		String mscmd = "get multistep command";
		if (activeMultiStepModule.containsKey(mscmd)) {
			
		}
		Iterator<String> keys = modules.keySet().iterator();
		while (keys.hasNext()) {
			Module module = modules.get(keys.next());
			for (String cmd : module.getCommands()) {
				if (cmd.equalsIgnoreCase(words[module.getKeyWordLocation()]))
					return runCommandOnModule(words, module);
			}
			// we didn't match on the specified position, so let's check if the keyword is in a different position
			for (String cmd : module.getCommands()) {
				if (Arrays.asList(words).contains(cmd))
					return runCommandOnModule(words, module);
			}
		}
		StringBuffer out = new StringBuffer();
		out.append("{\"error\":\"");
		out.append("I DO NOT KNOW HOW TO ");
		for (String word : words) {
			out.append(word);
			out.append(" ");
		}
		out.append("\"}");
		return out.toString();
	}
	
	private static String runCommandOnModule(String[] words, Module module) {
		switch (module.run(words)) {
			case Module.SUCCESS:
				return prepareResponseJSON(module);
			case Module.NO_ACCESS_CODE:
				return prepareErrorResponseJSON(module, "ACCESS CODE NOT ACCEPTED");
			case Module.NO_RESULTS:
				return prepareErrorResponseJSON(module, "NO RESULTS WERE RETURNED");
			case Module.INVALID_PARAMS:
				return prepareErrorResponseJSON(module, "THE PARAMETERS PROVIDED WERE INVALID OR DID NOT MATCH THE PATTERN");
			case Module.ERROR:
			case Module.SERVICE_ERROR:
			default:
				return prepareErrorResponseJSON(module, null);
		}
	}

	private static String prepareResponseJSON(Module module) {
		StringBuffer json = new StringBuffer();
		json.append("{ \"speech\":\"");
		json.append(module.getSpeechResponse());
		json.append("\" \"short_text\":\"");
		json.append(module.getShortTextResponse());
		json.append("\" \"full_text\":\"");
		json.append(module.getFullTextResponse());
		json.append("\" \"html\":\"");
		json.append(module.getHTMLResponse());
		json.append("\" \"url\":\"");
		json.append(module.getURLResponse());
		json.append("\" \"log\":\"");
		json.append(module.getLogText());
		json.append("\" }");
		return json.toString();
	}
	
	private static String prepareErrorResponseJSON(Module module, String extraText) {
		StringBuffer json = new StringBuffer();
		json.append("{\"error\":\"");
		json.append(extraText);
		json.append(" ");
		json.append(module.getErrorMessage());
		json.append("\"}");
		return json.toString();
	}

	public static String extractElement(String json, String element) {
        while (json.indexOf(element) != -1) {        
            int startIndex = json.indexOf("\"", json.indexOf(element)+2+element.length());
            int endIndex = json.indexOf("\"", startIndex+1);
            return json.substring(startIndex+1, endIndex);
        }
        return null;            
    }
}