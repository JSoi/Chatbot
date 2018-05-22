package com.Test.test;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.jena.atlas.json.JsonString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import ai.api.AIServiceException;

/**
 * Handles requests for the application home page.
 */
@RestController
public class HomeController {

	Controller MainControlling = new Controller();
	private Logger logger = LoggerFactory.getLogger(HomeController.class);
	int i = 1;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String hello() {
		JSONObject example = new JSONObject();
		example.put("user_key", "encrypterUserKey");
		example.put("type", "text");
		example.put("user_key", "encrypterUserKey");
		//message(example);
		return example.toJSONString();
		//return "hello";
	}

	@RequestMapping(value = "/keyboard", method = RequestMethod.GET)
	public String keyboard() {
		System.out.println("keyboard");
		DialogFlow df = new DialogFlow();
		String s = df.dialog("안녕");
		JSONObject obj = new JSONObject();
		obj.put("type", s);

		return obj.toJSONString();
	}

	@RequestMapping(value = "/message", method = {RequestMethod.GET,RequestMethod.POST}
	,produces="application/json;charset=UTF-8")
	@ResponseBody
	public String message(@RequestBody String object) throws AIServiceException, java.text.ParseException, IOException, ParseException {

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(object);
		JSONObject parsingJson = (JSONObject)obj;

		String getText = (String) parsingJson.get("content");
		String result;
		
	/*	JSONObject keyboard = new JSONObject();
		JSONObject buttonJS = new JSONObject();
		ArrayList<String> buttons = new ArrayList<String>();
		buttons.add("안녕");
		buttons.add("하이");	
		JSONArray jsArray = new JSONArray();
		
		Gson gson = new GsonBuilder().create();
		JsonArray buttonarr = gson.toJsonTree(jsArray).getAsJsonArray();*/
		logger.info(object);
		logger.info("getText:"+getText);
		MakeResponse r = new MakeResponse();
		logger.info(r.MakeJsonObject(getText));
		result = MainControlling.flow(getText);	

		logger.info(result);
		  

		return result;
	}

}
