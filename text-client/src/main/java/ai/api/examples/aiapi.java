package ai.api.examples;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

public class aiapi {

	static public void main(String[] args) throws ParseException {

		// String text = "나는 충남대학교에 다닌다"; // 분석할 텍스트 데이터
		String text = "";
		String[] sSPO = new String[3];
		while (true) {
			try {
				System.out.print("> ");
				InputStreamReader streamReader = new InputStreamReader(System.in);
				BufferedReader bufferedReader = new BufferedReader(streamReader);
				text = bufferedReader.readLine();
				if (text.contains(":newstore")) { // 새로운 상점
					String newStore = text.replace(text.trim().split(" ")[0], "").trim();
					System.out.println(newStore);
					teachNewStore(newStore);

				} else if (text.contains(":teach")) { // 가르치기
					System.out.print("가르칠 음식점을 입력해주세요 : ");
					String textStore = text.replace(text.trim().split(" ")[0], "").trim();
					sSPO[0] = textStore.trim();
					System.out.print("1.주소, 2.영업 시간, 3.메뉴, 4.사이트 > ");
					String Predicate = bufferedReader.readLine();
					// sSPO[1] = Integer.parseInt(Predicate);
					sSPO[1] = Predicate.trim();
					System.out.print(Predicate + "을(를) 입력해주세요");
					String Objective = bufferedReader.readLine();
					sSPO[2] = Objective.trim();
					System.out.println("SPO->" + sSPO[0] + "/" + sSPO[1] + "/" + sSPO[2]);

					// fuseki server
					final String UPDATE_TEMPLATE = "PREFIX my: <http://localhost:3030/" + sSPO[0] + ">" + "INSERT DATA"
							+ "{ <http://example/%s>    my:title    \"A new book\" ." + "}   ";

					String id = UUID.randomUUID().toString();
					System.out.println(String.format("Adding %s", id));
					UpdateProcessor upp = UpdateExecutionFactory.createRemote(
							UpdateFactory.create(String.format(UPDATE_TEMPLATE, id)),
							"http://localhost:3030/store/update");
					upp.execute();

				} else { // 질문하기
					SRL(text);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int position(String input) {
		int position = -1;
		switch (input) {
		case "ARG0":
			position = 0;
			break;
		case "ARG1":
			position = 2;
			break;
		case "ARG2":
			position = 2;
			break;
		case "ARG3":
			position = 2;
			break;
		case "ARG4":
			position = 2;
			break;
		default:
			position = -1;

		}
		return position;
	}

	public static void SRL(String text) throws ParseException {
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
		String accessKey = "d303f91a-0f8f-4f58-acab-5d85944807ff"; // 발급받은 Access Key
		String analysisCode = "SRL"; // 언어 분석 코드
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
			String SPO[] = new String[3];
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
			JSONObject SRL_bf = (JSONObject) sentence.get(0);
			JSONArray SRLArray = (JSONArray) SRL_bf.get("SRL");
			for (int SRLcount = 0; SRLcount < SRLArray.size(); SRLcount++) {
				JSONObject SRLtemp = (JSONObject) SRLArray.get(SRLcount);
				String verb = (String) SRLtemp.get("verb");
				SPO[1] = verb;
				JSONArray verbArg = (JSONArray) SRLtemp.get("argument");
				ArrayList<String> argumentList = new ArrayList<String>();
				for (int verbArgCount = 0; verbArgCount < verbArg.size(); verbArgCount++) {
					JSONObject arg = (JSONObject) verbArg.get(verbArgCount);
					String type = (String) arg.get("type");
					SPO[position(type)] = (String) arg.get("text");
				}
			}
			if (SPO.length == 0) {
				System.out.println("NO SPO");
			} else {
				System.out.println("S -> " + SPO[0]);
				System.out.println("P -> " + SPO[1]);
				System.out.println("O -> " + SPO[2]);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void teachNewStore(String newStore) throws UnsupportedEncodingException {
		final String UPDATE_TEMPLATE = "PREFIX store: <http://localhost:3030/store>" + "INSERT DATA"
				+ "{ <http://localhost:3030/store>    store:name    \""+newStore+"\" ." + "}   ";

		String id = UUID.randomUUID().toString();
		System.out.println(String.format("Adding %s", id));
		UpdateProcessor upp = UpdateExecutionFactory.createRemote(
				UpdateFactory.create(String.format(UPDATE_TEMPLATE, id)), "http://localhost:3030/store/update");
		upp.execute();
		// String URL = "http://localhost:3030/";
		// String subURL = URLEncoder.encode(newStore, "UTF-8");
		// System.out.println(URL + subURL);
		// TDBFactory.createDataset("http://localhost:3030/");
	}
}