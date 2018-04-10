package ai.api.examples.Chatbot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

public class aiapi {

	static public void main(String[] args) throws ParseException {
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
		String accessKey = "d303f91a-0f8f-4f58-acab-5d85944807ff"; // 발급받은 Access Key
		String analysisCode = "SRL"; // 언어 분석 코드
		//String text = "나는 충남대학교에 다닌다"; // 분석할 텍스트 데이터
		String text = "나는 예쁜 꽃을 좋아한다.";
		Gson gson = new Gson();

		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();

		argument.put("analysis_code", analysisCode);
		argument.put("text", text);

		request.put("access_key", accessKey);
		request.put("argument", argument);

		URL url;
		Integer responseCode = null;
		String responBody = null;
		try {
			String s, p, o;
			url = new URL(openApiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("content-type", "application/json; charset=utf-8");
			con.setRequestMethod("POST");
			con.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(gson.toJson(request).getBytes("UTF-8"));
			wr.flush();
			wr.close();

			responseCode = con.getResponseCode();
			InputStream is = con.getInputStream();
			byte[] buffer = new byte[is.available()];
			int byteRead = is.read(buffer);
			responBody = new String(buffer);
			JSONParser parser = new JSONParser();
			JSONObject root = (JSONObject) parser.parse(responBody);
			JSONObject return_object = (JSONObject) root.get("return_object");
			JSONArray sentence = (JSONArray) return_object.get("sentence");
			JSONObject SRL_bf = (JSONObject)sentence.get(0);
			JSONArray SRLArray = (JSONArray)SRL_bf.get("SRL");
			for(int SRLcount = 0; SRLcount<SRLArray.size();SRLcount++) {
				JSONObject SRLtemp = (JSONObject) SRLArray.get(SRLcount);
				String verb = (String) SRLtemp.get("verb");
				JSONArray verbArg = (JSONArray) SRLtemp.get("argument");
				for(int verbArgCount = 0; verbArgCount < verbArg.size(); verbArgCount++) {
					JSONObject arg = (JSONObject)verbArg.get(verbArgCount);
					
				}
				System.out.println(verb);
				//System.out.println(SRLtemp.toString());
			}

			//System.out.println("[responseCode] " + responseCode);
			//System.out.println("[responBody]");
			//System.out.println(responBody);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}