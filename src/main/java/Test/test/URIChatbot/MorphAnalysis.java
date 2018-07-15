package Test.test.URIChatbot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

public class MorphAnalysis {
	private String openApiURL;
	private String accessKey;
	private String analysisCode;
	private Gson gson;
	private SparqlQuery query;
	private MakeResponse respond;

	public MorphAnalysis() {
		openApiURL = "http://aiopen.etri.re.kr:8000/WiseQAnal";
		accessKey = "d303f91a-0f8f-4f58-acab-5d85944807ff"; // 발급받은 Access Key
		analysisCode = "SRL"; // 언어 분석 코드
		gson = new Gson();
		query = new SparqlQuery();
		respond = new MakeResponse();

	}

	public String analyze(String text) throws ParseException {
		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();

		argument.put("analysis_code", analysisCode);
		argument.put("text", text);

		request.put("access_key", accessKey);
		request.put("argument", argument);

		URL url;
		@SuppressWarnings("unused")
		Integer responseCode = null;
		String responBody = null;
		String predicate = "";
		String realstorename="";
		String finalResult="";
		try {
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
			//int byteRead = is.read(buffer);
			responBody = new String(buffer);
			JSONParser parser = new JSONParser();
			JSONObject root = (JSONObject) parser.parse(responBody);
			JSONObject _object = (JSONObject) root.get("_object");
			JSONObject orgQInfo = (JSONObject) _object.get("orgQInfo");
			JSONObject orgQUnit = (JSONObject) orgQInfo.get("orgQUnit");
			JSONObject nDoc = (JSONObject) orgQUnit.get("ndoc");

			JSONArray sentence = (JSONArray) nDoc.get("sentence");
			JSONObject morp_bf = (JSONObject) sentence.get(0);
			JSONArray MORPArray = (JSONArray) morp_bf.get("morp");
			//JSONArray MORPArray_eval = (JSONArray) morp_bf.get("morp_eval");
			JSONArray vLATs = (JSONArray) orgQUnit.get("vLATs");
			
			if (vLATs.size() != 0) { // 어휘정답유형 존재할 경우 - Predicate로 취급해준다
				JSONObject strLAT_o = (JSONObject) vLATs.get(0);
				String strLAT = (String) strLAT_o.get("strLAT");
				// respond.MakeJsonObject("ASSUMED PREDICATE = " + strLAT);
				predicate = strLAT;
				predicate.trim();

				String predicate_spec = query.matchPredicate(predicate);
				if (predicate_spec.equals("")) {
					return respond.MakeJsonObject("매치되는 정보 분류가 없어요 ㅠㅠ");

				}
			} else {
				return respond.MakeJsonObject("잘 이해하지 못했어요");

			}

			ArrayList<String> NNGList = new ArrayList<String>();
			for (int MCount = 0; MCount < MORPArray.size(); MCount++) {
				JSONObject Mtemp = (JSONObject) MORPArray.get(MCount);
				String type = (String) Mtemp.get("type");
				String NNGString = (String) Mtemp.get("lemma");
				if (!type.contains("J")) {
					NNGList.add(NNGString);
				}
			}

			String linewithReal = "";
			for (String s : NNGList)
				linewithReal += s;
			String storename_array[] = linewithReal.substring(0, linewithReal.lastIndexOf(predicate)).split(" ");
			ArrayList<String> storename_arr = new ArrayList<String>(Arrays.asList(storename_array));

			// for (String s : storename_arr)
			// System.out.print(s + " / ");
			realstorename = query.DecideWhichStore(storename_arr);
			if (query.matchSubject(realstorename).equals("")) { // 해당 가게 정보가 없을 경우
				return respond.MakeJsonObject("\"" + realstorename + "\" 가게가 존재하지 않습니다. :newstore 명령어를 통해 알려주세요!");
			}
			finalResult = query.SearchDB_SP(realstorename, predicate);
			if (finalResult.equals("")) { // 해당 가게 정보가 없을 경우
				return respond.MakeJsonObject(realstorename + "의 " + predicate + " 정보가 없습니다. :teach 명령어를 통해 알려주세요!");
			}
			
			// respond.MakeJsonObject("결과 : " + finalResult);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return respond.MakeJsonObject(Answer(realstorename, predicate, finalResult));

	}

	private String Answer(String subject, String predicate, String objective) {
		// TODO Auto-generated method stub
		return subject + "의 " + predicate + "(은)는 " + objective + "입니다.";
	}

}
