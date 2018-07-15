package Test.test.URIChatbot;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class MakeResponse {
	public String speechResponse;

	public MakeResponse() {
	}

	@SuppressWarnings("unchecked")
	public String MakeJsonObject(String msg, ArrayList<String> Buttons) {
		JSONObject res = new JSONObject();
		JSONObject keyboard = new JSONObject();
		JSONObject text = new JSONObject();
		//JSONObject buttonJS = new JSONObject();
		JSONArray jsArray = new JSONArray();
		
		Gson gson = new GsonBuilder().create();

		text.put("text", msg);
		res.put("message", text);
		jsArray.addAll(Buttons);
		JsonArray buttonarr = gson.toJsonTree(jsArray).getAsJsonArray();
		keyboard.put("type", "buttons");

		keyboard.put("buttons", buttonarr);
		res.put("keyboard", keyboard);
		return res.toJSONString();

	
	}

	

	public String MakeJsonObject(String input) {
		JsonObject res = new JsonObject();
		JsonObject text = new JsonObject();

		text.addProperty("text", input);
		res.add("message", text);

		return res.toString();

	}

}