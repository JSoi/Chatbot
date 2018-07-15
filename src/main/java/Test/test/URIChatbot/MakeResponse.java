package Test.test.URIChatbot;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class MakeResponse {
	public String speechResponse;

	public MakeResponse() {
	}

	public String MakeJsonObject(String msg, ArrayList<String> Buttons) {
		JsonObject TotalJson = new JsonObject();
		JsonObject keyboard = new JsonObject();
		JsonObject messageButton = new JsonObject();
		JsonObject text = new JsonObject();
		// JsonObject buttonJS = new JsonObject();
		JsonArray jsArray = new JsonArray();
		Gson gson = new GsonBuilder().create();

		text.addProperty("text", msg);
		TotalJson.add("message", text);

		TotalJson.add("message_button", messageButton);

		keyboard.addProperty("type", "buttons");
		for (int i = 0; i < Buttons.size(); i++) {
			jsArray.add(Buttons.get(i));
		}
		JsonArray buttonarr = gson.toJsonTree(jsArray).getAsJsonArray();
		keyboard.add("buttons", buttonarr);
		TotalJson.add("keyboard", keyboard);

		return TotalJson.toString();

	}

	public String MakeJsonObject(ArrayList<String> Buttons) {
		JsonObject TotalJson = new JsonObject();
		JsonObject keyboard = new JsonObject();
		JsonArray jsArray = new JsonArray();
		Gson gson = new GsonBuilder().create();

		
		keyboard.addProperty("type", "buttons");
		for (int i = 0; i < Buttons.size(); i++) {
			jsArray.add(Buttons.get(i));
		}
		keyboard.add("buttons", jsArray);
		
		TotalJson.add("keyboard", keyboard);
		return TotalJson.toString();

	}

	public String MakeJsonObject(String input) {
		JsonObject res = new JsonObject();
		JsonObject text = new JsonObject();

		text.addProperty("text", input);
		res.add("message", text);

		return res.toString();

	}

}