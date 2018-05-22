package com.Test.test;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class MakeResponse {
	public String speechResponse;

	public MakeResponse() {
	}

	public String MakeJsonObject(ArrayList<String> Buttons) {
		JSONObject res = new JSONObject();
		JSONObject keyboard = new JSONObject();
		//JSONObject buttonJS = new JSONObject();
		JSONArray jsArray = new JSONArray();
		Gson gson = new GsonBuilder().create();

		jsArray.addAll(Buttons);
		JsonArray buttonarr = gson.toJsonTree(jsArray).getAsJsonArray();
		keyboard.put("type", "buttons");
		keyboard.put("buttons", buttonarr);
		res.put("keyboard", keyboard);
		return res.toJSONString();

	}

	public String MakeJsonObject(String input) {
		JSONObject res = new JSONObject();
		JSONObject text = new JSONObject();

		text.put("text", input);
		res.put("message", text);

		return res.toJSONString();

	}

}