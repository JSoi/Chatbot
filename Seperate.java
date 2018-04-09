package ai.api.examples;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;

public class Seperate {

	public void apiai(String text) throws ParseException {
		String[] sSPO = new String[3];
		try {

			InputStreamReader streamReader = new InputStreamReader(System.in);
			BufferedReader bufferedReader = new BufferedReader(streamReader);

			if (text.contains(":newstore")) { // 새로운 상점
				String newStore = text.replace(text.trim().split(" ")[0], "").trim();
				System.out.println(newStore);
				teachNewStore(newStore);
			}

			else if (text.contains(":teach")) { // 가르치기
				System.out.print("가르칠 음식점을 입력해주세요 : ");
				String textStore = text.replace(text.trim().split(" ")[0], "").trim();
				sSPO[0] = textStore.trim();
				System.out.print("1.주소, 2.영업 시간, 3.메뉴, 4.사이트 > ");
				String Predicate = bufferedReader.readLine();
				sSPO[1] = Predicate.trim();
				System.out.print(Predicate + "을(를) 입력해주세요");
				String Objective = bufferedReader.readLine();
				sSPO[2] = Objective.trim();
				System.out.println("SPO->" + sSPO[0] + "/" + sSPO[1] + "/" + sSPO[2]);
				teachStoreInfo(sSPO[0], sSPO[1], sSPO[2]);
			} else { // 질문하기
				Question(text.trim());
				System.out.println("++++++++++++++++");
				QuestionEval(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 가르치고자 한ㄴ 음식점 : 별리달리 "별리달리는 : "
	 */

	public static int position(String input) {
		int position = -1;
		switch (input) {
		case "ARG0":
			position = 0;
			break;
		case "ARG1":
		case "ARG2":
		case "ARG3":
		case "ARG4":
			position = 2;
			break;
		default:
			position = -1;

		}
		return position;
	}

	public static void Question(String text) throws ParseException, org.json.simple.parser.ParseException {
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseQAnal";
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

			ArrayList<String> NNGList = new ArrayList<String>();
			for (int MCount = 0; MCount < MORPArray.size(); MCount++) {
				JSONObject Mtemp = (JSONObject) MORPArray.get(MCount);
				String type = (String) Mtemp.get("type");
				String NNGString = (String) Mtemp.get("lemma");

				if (type.contains("NN")) {
					NNGList.add(NNGString);
				}
			}

			for (int nngcount = 0; nngcount < NNGList.size(); nngcount++) {
				System.out.println(NNGList.get(nngcount));
			}
			System.out.println(text.split(NNGList.get(1))[0]);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void QuestionEval(String text) throws ParseException, org.json.simple.parser.ParseException {
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseQAnal";
		String accessKey = "d303f91a-0f8f-4f58-acab-5d85944807ff"; // 발급받은 Access Key
		String analysisCode = "SRL"; // 언어 분석 코드
		Gson gson = new Gson();

		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();

		argument.put("analysis_code", analysisCode);
		argument.put("text", text);

		request.put("access_key", accessKey);
		request.put("argument", argument);

		URL url2;
		Integer responseCode2 = null;
		String responBody2 = null;
		String predicate = "";

		try {
			url2 = new URL(openApiURL);
			HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
			con2.setRequestProperty("content-type", "application/json; charset=utf-8");
			con2.setRequestMethod("POST");
			con2.setDoOutput(true);

			DataOutputStream wr2 = new DataOutputStream(con2.getOutputStream());
			wr2.write(gson.toJson(request).getBytes("UTF-8"));
			wr2.flush();
			wr2.close();

			responseCode2 = con2.getResponseCode();
			InputStream is = con2.getInputStream();
			byte[] buffer = new byte[is.available()];
			int byteRead = is.read(buffer);
			responBody2 = new String(buffer);
			JSONParser parser = new JSONParser();
			JSONObject root = (JSONObject) parser.parse(responBody2);
			JSONObject return_object = (JSONObject) root.get("return_object");
			JSONObject orgQInfo = (JSONObject) return_object.get("orgQInfo");
			JSONObject orgQUnit = (JSONObject) orgQInfo.get("orgQUnit");
			JSONObject nDoc = (JSONObject) orgQUnit.get("ndoc");
			JSONArray sentence = (JSONArray) nDoc.get("sentence");
			JSONObject morp_bf = (JSONObject) sentence.get(0);
			JSONArray MORPArray = (JSONArray) morp_bf.get("morp_eval");
			JSONArray vLATs = (JSONArray) orgQUnit.get("vLATs");

			if (vLATs.size() != 0) { // 어휘정답유형 존재할 경우 - Predicate로 취급해준다
				JSONObject strLAT_o = (JSONObject) vLATs.get(0);
				String strLAT = (String) strLAT_o.get("strLAT");
				System.out.println("ASSUMED PREDICATE = " + strLAT);
				predicate = strLAT;
			}

			ArrayList<String> NNGList = new ArrayList<String>(); // 명사 리스트

			int real_NNG = 0;

			for (int MCount = 0; MCount < MORPArray.size(); MCount++) {
				JSONObject Mtemp = (JSONObject) MORPArray.get(MCount);
				String type = (String) Mtemp.get("result");
				String NNGString = (String) Mtemp.get("target");
				if (type.contains("/NNG") && !type.contains("/V")) { // 진짜 명사인 애들
					real_NNG++;
					NNGList.add(NNGString);
				} else {
					if (real_NNG <= 1) {
						NNGList.add(NNGString);
					}
				}

			}

			predicate = returnStoreCandidate(NNGList);
			for (int nngcount = 0; nngcount < NNGList.size(); nngcount++) {
				System.out.println(NNGList.get(nngcount));
			}
			///////////////// 테스트중
			String realstorename = DecideWhichStore(NNGList);
			System.out.println("추측 가게 (DB 뒤짐) + " + realstorename);
			System.out.println("Predicate -> " + predicate);

			///////////////// SP가지고 O 추측하기

			String finalResult = SearchDB_SP(realstorename, predicate);
			System.out.println("##############결과 : " + finalResult + " #################");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String returnStoreCandidate(ArrayList<String> arr) {
		String deleteTarget_predicate = arr.get(arr.size() - 1);
		arr.remove(arr.get(arr.size() - 1));
		return deleteTarget_predicate;
	}

	public static void searchByWord(ArrayList<String> arr) {
		String findTarget = "";
		ArrayList<String> resultTempArr = new ArrayList<String>();
		for (int accum_count = 0; accum_count < arr.size() - 1; accum_count++) {
			findTarget += arr.get(accum_count);
			System.out.println(SearchDB_obj_StoreName(findTarget).get(0));
		}
	}

	/**
	 * @param newStore
	 *            새로운 상점을 상점명(newStore)으로 가르칠 때 쓰는 메서드
	 */
	public void teachNewStore(String newStore) throws IOException {
		final String UPDATE_TEMPLATE = "PREFIX store: <http://localhost:3030/stores#> " + "INSERT DATA"
				+ "{ <http://localhost:3030/store#%s>    store:이름    \"" + newStore + "\" ." + "}   ";

		String id = UUID.randomUUID().toString();

		UpdateProcessor upp = UpdateExecutionFactory.createRemote(
				UpdateFactory.create(String.format(UPDATE_TEMPLATE, id)), "http://localhost:3030/store/update");
		upp.execute();
	}

	public void teachStoreInfo(String subject, String predicate, String Objective) {// objective is RAW!!
		// ################### 가게명 추가하기
		// ######################## 일단은 site라고 정해놓음
		String trimStore = subject.replace(" ", "");
		String StoreSub = matchSubject(trimStore);
		//String StorePre = matchPredicate(predicate);
		final String UPDATE_TEMPLATE = "PREFIX store: <http://localhost:3030/stores#>" + "INSERT DATA" + "{ <"
				+ StoreSub + ">    store:" + predicate + "   \"" + Objective + "\" ." + "}   ";
		String id = UUID.randomUUID().toString();
		UpdateProcessor upp = UpdateExecutionFactory.createRemote(
				UpdateFactory.create(String.format(UPDATE_TEMPLATE, id)), "http://localhost:3030/store/update");
		upp.execute();
	}

	public static ArrayList<String> SearchDB_obj_StoreName(String input) { // 포함하는 list 리턴
		final String UPDATE_TEMPLATE = "SELECT ?subject ?object " + "WHERE { ?subject "
				+ " <http://localhost:3030/stores#이름> " + " ?object filter contains(?object,\"" + input + "\") . "
				+ "} ";
		String queryService = "http://localhost:3030/store/sparql";
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, UPDATE_TEMPLATE);
		ResultSet results = q.execSelect();
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("object");
			if (!resultArr.contains(x.toString()))
				resultArr.add(x.toString()); // object 후보 애들 다 넣어주기. 일단 contains query를 씀 - like 사용 시 수정
		}
		return resultArr;
	}
	
	public static String SearchDB_SP(String subject, String predicate) { // 상점이름과 사이트를 가지고 사이트 리턴
		String ExactStore = "";
		String ExactPre = "";
		String returnString = "";
		ExactStore = matchSubject(subject);
		//ExactPre = matchPredicate(predicate);
		ExactPre = "http://localhost:3030/stores#" + predicate;
		final String SEARCH_OBJ_TEMPLATE = "SELECT ?object " + "WHERE { <" + ExactStore + ">" + " <" + ExactPre + "> "
				+ " ?object . " + "} ";
		String queryService_o = "http://localhost:3030/store/sparql";
		QueryExecution q_o = QueryExecutionFactory.sparqlService(queryService_o, SEARCH_OBJ_TEMPLATE);
		ResultSet results_o = q_o.execSelect();
		while (results_o.hasNext()) {
			QuerySolution soln = results_o.nextSolution();
			RDFNode x = soln.get("object");
			returnString = x.toString();
		}
		return returnString;

	}

	/**
	 * 검색 알고리즘은 특정 단어를 포함하는 단어중 공통점이 많은 부분만 ... 가게명이 ABCD일경우 A검색 List B검색 List C검색
	 * List AB 검색 List BC 검색 List ABC 검색 List 검색 후 결과가 있다면 그걸 Store로 결정
	 */

	@SuppressWarnings({ "unchecked", "rawtypes", "unlikely-arg-type" }) //// predicate 제거하기 추가해야됨!!!
	public static String DecideWhichStore(ArrayList<String> candidates) {
		ArrayList<String> searchResults = new ArrayList<String>();
		String returnStoreName = "";
		String simple = "";
		String searchedStringCommon = "";
		for (String s : candidates) {
			simple += s;
			searchResults.addAll(DecideStoreBySplit(simple));
		}

		Map<String, Integer> stringsCount = new HashMap<>();
		for (String s : searchResults) {
			Integer c = stringsCount.get(s);
			if (c == null)
				c = new Integer(0);
			c++;
			stringsCount.put(s, c);
		}
		Map.Entry<String, Integer> mostRepeated = null;
		for (Map.Entry<String, Integer> e : stringsCount.entrySet()) {
			if (mostRepeated == null || mostRepeated.getValue() < e.getValue())
				mostRepeated = e;
		}
		if (mostRepeated != null)
			searchedStringCommon = mostRepeated.getKey();
		// System.out.println("Most common string: " + mostRepeated.getKey());
		System.out.println("가장 많이 검색된 가게명 : " + searchedStringCommon);
		if (matchSubject(simple) != null) { // 정확히 일치하는 게 있다면
			returnStoreName = simple;
		} else {
			returnStoreName = searchedStringCommon;
		}
		return returnStoreName;
	}

	/** A,B,C,D 등 작게 쪼갠 string을 검색해서 포함 string 을 리스트로 반환 */
	public static ArrayList<String> DecideStoreBySplit(String input) {
		final String UPDATE_TEMPLATE = "SELECT ?subject ?object " + "WHERE { " + "?subject"
				+ " <http://localhost:3030/stores#이름> " + " ?object filter contains(?object,\"" + input + "\") . "
				+ "} ";
		String queryService = "http://localhost:3030/store/sparql";
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, UPDATE_TEMPLATE);
		ResultSet results = q.execSelect();
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			// assumes that you have an "?x" in your query
			RDFNode x = soln.get("object");
			if (!resultArr.contains(x.toString()))
				resultArr.add(x.toString()); // object 후보 애들 다 넣어주기. 일단 contains query를 씀 - like 사용 시 수정
		}
		return resultArr;
	}

	public static String matchSubject(String storename) {
		final String SEARCH_TEMPLATE = "SELECT ?subject " + "WHERE { " + "?subject "
				+ " <http://localhost:3030/stores#이름> " + " \"" + storename + "\" . " + "} ";
		String queryService = "http://localhost:3030/store/sparql"; // 인스턴스 목록에서 search
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_TEMPLATE);
		ResultSet results = q.execSelect();
		String StoreSub = "";
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("subject");
			StoreSub = x.toString();
		}
		return StoreSub;
	}

	/**
	 * rdf 구조에 맞춰 matchPredicate 수정 또는 삭제하기 
	 * */
	public static String matchPredicate(String p_word) { // 단어(ex.태그, 분위기, 영업 시간)을 input으로 받아 기존의 틀로 리턴받음
		// 수정할사항1) - <가르칠 때> 틀을 리턴받고, 이 틀을 기반으로 쿼리 전송하기 - teachstore에서 코드 수정함
		// 수정할사항2) - <질문할 때> predicate 기반으로 틀을 리턴받고, 이 틀을 기반으로 질문해서 결과 받기
		String predicateResult = "";
		final String SEARCH_PREDICATE_FORM = "SELECT ?pre " + "WHERE { " + "?pre "
				+ " <http://www.w3.org/2000/01/rdf-schema#label> " + " \"" + p_word + "\" . " + "} ";
		String queryService = "http://localhost:3030/stores/query"; // 뼈대에서 쿼리
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_PREDICATE_FORM);
		ResultSet results = q.execSelect();
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("pre");
			predicateResult = x.toString();
		}
		return predicateResult;
	}
}
