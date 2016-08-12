
/**
 * Description: Use this class to flatten wsdls by inlining imported schemas and other wsdls. Needs JRE 8.0 or higher
 * Output: Flattened WSDL, printed to std out.
 * Author: Suraj N. Pillai
 * Date: 04/10/2016
 */

package com.suraj.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class FlattenWSDL{
	public static void main(String[] args){
		if(args.length==0 || args[0].trim().replaceAll("-","").equalsIgnoreCase("h")){
			System.out.println("Usage");
			System.out.println("java com.suraj.utils.FlattenWSDL <wsdlurl> <username if any> <password if any>");
		}
		String wsdlLoc = args[0];
		if(args.length>2){
			final String uname=args[1];
			final String pwrd=args[2];
			Authenticator.setDefault (new Authenticator() {
			    protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication (uname, pwrd.toCharArray());
			    }
			});
		}
		String inp=getResp(wsdlLoc);
		Pattern patt = Pattern.compile("<wsdl:import[^<>]+location=\"(https://[^\\\"]+)\"[^<>]+>",Pattern.DOTALL);
		inp=flatten(inp,patt);
		patt = Pattern.compile("<xs[d]?:import[^<>]+schemaLocation=\"(https://[^\\\"]+)\"[^<>]+>",Pattern.DOTALL);
		inp=flatten(inp,patt);
		
		
		System.out.println(inp);
		
	}
	
	
	private static String flatten(String inp,Pattern patt){
	
		Matcher myMatch = patt.matcher(inp);
		HashMap<String,String> respMaps = new HashMap<String,String>();
		while(myMatch.find()){
            //System.out.println(myMatch.group()+"\n");
			String url = myMatch.group(1);
			if(!respMaps.containsKey(url)){
				respMaps.put(url, getResp(url));
				String replaceWith=respMaps.get(url).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
										.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "")
										.replaceAll("<[/]?wsdl:definitions[^<>]*>","");
				inp=myMatch.replaceFirst(Matcher.quoteReplacement(replaceWith));
			}else{
				inp=myMatch.replaceFirst("");
				//System.out.println("Urls are getting repeated. Quitting");
			//	break;
			}
			
			myMatch = patt.matcher(inp);
        }
		return inp;
	}
	
	private static String getResp(String url){
		
		try{
			URL wsdlurl = new URL(url);
			return new BufferedReader(new InputStreamReader(wsdlurl.openStream())).lines().parallel().collect(Collectors.joining("\n"));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return "";
	}
}