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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.atlas.logging.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

public class MorphAnalysis {
	private Logger logger = LoggerFactory.getLogger(MorphAnalysis.class);
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

		logger.info("들어왔소");

		String Answer = "";
		URL url;
		@SuppressWarnings("unused")
		Integer responseCode = null;
		String responBody = null;
		String predicate = "";
		String realstorename = "";
		String finalResult = "";
		String predicate_spec = "";
		try {
			logger.info("try진입");
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
			JSONObject orgQInfo = (JSONObject) return_object.get("orgQInfo");
			JSONObject orgQUnit = (JSONObject) orgQInfo.get("orgQUnit");
			JSONObject nDoc = (JSONObject) orgQUnit.get("ndoc");

			JSONArray sentence = (JSONArray) nDoc.get("sentence");
			JSONObject morp_bf = (JSONObject) sentence.get(0);
			JSONArray MORPArray = (JSONArray) morp_bf.get("morp");
			JSONArray WSDArray = (JSONArray) morp_bf.get("WSD");
			JSONArray vLATs = (JSONArray) orgQUnit.get("vLATs");

			if (vLATs.size() != 0) { // 어휘정답유형 존재할 경우 - Predicate로 취급해준다
				JSONObject strLAT_o = (JSONObject) vLATs.get(0);
				String strLAT = (String) strLAT_o.get("strLAT");
				// System.out.println("ASSUMED PREDICATE = " + strLAT);
				predicate = strLAT;
				predicate.trim();

				predicate_spec = query.matchPredicate(predicate);
				if (predicate_spec.equals("")) {
					return "매치되는 정보 분류가 없어요 ㅠㅠ";

				}
			} else {
				return "잘 이해하지 못했어요";

			}

			ArrayList<String> NNGList = new ArrayList<String>();
			ArrayList<String> NounList = new ArrayList<String>();
			for (int MCount = 0; MCount < MORPArray.size(); MCount++) {
				JSONObject Mtemp = (JSONObject) MORPArray.get(MCount);
				String type = (String) Mtemp.get("type");
				String NNGString = (String) Mtemp.get("lemma");
				if (!type.contains("J")) {
					NNGList.add(NNGString);
				}
			}
			for (int WCount = 0; WCount < WSDArray.size(); WCount++) {
				JSONObject Wtemp = (JSONObject) WSDArray.get(WCount);
				String type = (String) Wtemp.get("type");
				if (type.equals("NNG")) {
					NounList.add((String) Wtemp.get("text"));
				}
			}

			if (query.Whether_Info_Store(predicate_spec)) {
				/** predicate이 가게, 카페, 술집인 경우 */
				int cutLine = NounList.lastIndexOf(predicate);
				ArrayList<String> DependencyList = new ArrayList<String>(NounList.subList(0, cutLine));
				Answer = AnswerSuitableStore(predicate, DependencyList);

			} else {
				/** predicate이 분위기, 위치 등일 경우 */
				Answer = AnswerStoreInfo(predicate, NNGList);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Answer;

	}

	/**
	 * @param predicate
	 * @param NNGList
	 *            형태소 분석이 끝난 문장의 구성요소가 들어 있는
	 */
	public String AnswerStoreInfo(String predicate, ArrayList<String> NNGList) {
		String linewithReal = "";
		for (String s : NNGList)
			linewithReal += s;
		String storename_array[] = linewithReal.substring(0, linewithReal.lastIndexOf(predicate)).split(" ");
		ArrayList<String> storename_arr = new ArrayList<String>(Arrays.asList(storename_array));

		String realstorename = query.DecideWhichStore(storename_arr);
		if (query.searchStoreName(realstorename).equals("")) { // 해당 가게 정보가 없을 경우
			return "\"" + realstorename + "\" 가게가 존재하지 않습니다. 가르치기 명령어를 통해 알려주세요!";
		}

		logger.info("-------------------------------------------------------------------");
		logger.info("realstorename - " + realstorename + " // predicate - " + predicate);
		logger.info("-------------------------------------------------------------------");
		String finalResult = query.SearchDB_SP(realstorename, predicate);
		logger.info("-------------------------------------------------------------------");
		logger.info("finalResult - " + finalResult);
		logger.info("-------------------------------------------------------------------");
		if (finalResult.equals("")) { // 해당 가게 정보가 없을 경우
			return realstorename + "의 " + predicate + " 정보가 없습니다. 가르치기 명령어를 통해 알려주세요!";
		}
		return Answer(realstorename, predicate, finalResult);
	}

	/**
	 * 여기에 들어가는
	 */
	public String AnswerSuitableStore(String predicate, ArrayList<String> arr) {
		// 가게를 한정하기 - 일반 음식점, 카페, 술집 등 분류해서 필터링
		ArrayList<String> suitableStores = new ArrayList<String>();
		suitableStores = query.UnionConditionSparql(predicate, arr);
		if (!suitableStores.isEmpty()) {
			for (String s : suitableStores) {
				return s + " 를 추천드릴게요";
			}
		}
		return "만족하는 가게가 없어요 :(";// CRAWLING!!
	}

	private String Answer(String subject, String predicate, String objective) {
		// TODO Auto-generated method stub
		return subject + "의 " + predicate + "(은)는 " + objective + "입니다.";
	}

}
