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
	
	public String MakeStoreRecommend(String pic_url, String storename, String spec_url) {
		JsonObject photo = new JsonObject();
		JsonObject button = new JsonObject();
		JsonObject message = new JsonObject();
		JsonObject res = new JsonObject();

		message.addProperty("text", storename);
		if (!pic_url.equals("")) {
			photo.addProperty("url", pic_url);
			photo.addProperty("width", 640);
			photo.addProperty("height", 480);
			message.add("photo", photo);
		}
		button.addProperty("label", "더 찾아보기");
		button.addProperty("url", spec_url);
		message.add("message_button", button);

		res.add("message", message);
		return res.toString();
	}
	

}