package Test.test.URIChatbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

public class SparqlQuery {
	private Logger logger = LoggerFactory.getLogger(SparqlQuery.class);
	MakeResponse JsnRespond = new MakeResponse();

	/**
	 * @param newStore
	 *            새로운 상점을 상점명(newStore)으로 가르칠 때 쓰는 메서드
	 */

	public boolean storeExist(String name) {
		logger.info("storeExist");
		logger.info("NAME : " + name);
		final String SEARCH_TEMPLATE = "PREFIX store: <http://13.209.53.196:3030/stores#> " + "ASK { ?s store:이름 \""
				+ name + "\".}";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		boolean trueorfalse = jsonObj.getBoolean("boolean");
		return trueorfalse;
	}

	public String teachNewStore(String newStore) {
		logger.info("getText:newstore");
		String id = UUID.randomUUID().toString();

		final String UPDATE_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#> " + "INSERT DATA"
				+ "{ <http://13.209.53.196:3030/stores#" + id + ">    stores:이름    \"" + newStore.replace(" ", "")
				+ "\" ." + "}   ";

		if (matchSubject(newStore).equals("")) {

			String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-update", "--service",
					"http://13.209.53.196:3030/stores", UPDATE_TEMPLATE };
			Process proc = null;

			try {
				proc = Runtime.getRuntime().exec(args);

				/** StringBuilder 사용하기 */
				StringBuilder sb = new StringBuilder();
				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

				while ((line = reader.readLine()) != null) {
					logger.info("update : " + line);
					sb.append(line);
				}

			} catch (IOException e) {
				logger.info(e.getMessage());
			}
			return newStore;
		} else {
			return "registered " + newStore;
			// System.out.println("이미 등록 된 상점입니다. :teach 명령어를 통해 상점에 대한 정보를 입력해주세요 ");
		}

	}

	public void teachStoreInfo(String subject, String predicate, String Objective) {// objective is RAW!!
		// TODO predicate 추가하기.
		logger.info("teachStoreInfo");
		String trimStore = subject.replace(" ", "");
		String StoreSub = matchSubject(trimStore);
		String StorePre = matchPredicate(Objective); // 카페 -> store:카페로 찾아줌
		if (!predicate.equals("음식점분류"))
			StorePre = "\"" + Objective + "\"";
		logger.info("subject - " + subject + "  predicate - " + predicate + "  Objective - " + Objective);
		logger.info("MATCHPRE : " + StorePre);
		// final String UPDATE_TEMPLATE = "PREFIX stores:
		// <http://13.209.53.196:3030/stores#>" + "INSERT DATA" + "{ <"
		// + StoreSub + "> stores:" + predicate + " \"" + Objective + "\" ." + "} ";
		final String UPDATE_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>" + "INSERT DATA" + "{ <"
				+ StoreSub + ">    stores:" + predicate + "   " + StorePre + " ." + "}   ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-update", "--service",
				"http://13.209.53.196:3030/stores", UPDATE_TEMPLATE };
		Process proc = null;

		try {
			proc = Runtime.getRuntime().exec(args);
			/** StringBuilder 사용하기 */
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				logger.info("update : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
	}

	// 추가 - 더 수정하기
	public void teachStoreInfo_TagCase(String subject, String tagLine) {
		logger.info("teachStoreInfo_TagCase");
		String trimStore = subject.replace(" ", "");
		String StoreSub = matchSubject(trimStore);
		logger.info("StoreSub : " + StoreSub);
		tagLine.trim();
		tagLine = tagLine.replaceAll(" ", "");
		String Raw_Tags[] = tagLine.split("#");
		List<String> tag_array = new ArrayList<>(Arrays.asList(Raw_Tags));
		tag_array.remove(0);
		String UPDATE_TEMPLATE_first = "PREFIX store: <http://13.209.53.196:3030/stores#> INSERT DATA {";
		String UPDATE_TEMPLATE_middle = "";
		for (String eachtag : tag_array)
			UPDATE_TEMPLATE_middle += "<" + StoreSub + "> store:태그   \"" + eachtag + "\".  ";
		String UPDATE_TEMPLATE_last = "}";
		final String UPDATE_TEMPLATE = UPDATE_TEMPLATE_first + UPDATE_TEMPLATE_middle + UPDATE_TEMPLATE_last;
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-update", "--service",
				"http://13.209.53.196:3030/stores", UPDATE_TEMPLATE };
		Process proc = null;

		try {
			proc = Runtime.getRuntime().exec(args);
			/** StringBuilder 사용하기 */
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				logger.info("update : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
	}

	public ArrayList<String> SearchDB_obj_StoreName(String input) { // 포함하는 list 리턴
		logger.info("SearchDB_obj_StoreName");
		final String SEARCH_TEMPLATE = "SELECT ?subject ?object " + "WHERE { ?subject "
				+ " <http://13.209.53.196:3030/stores#이름> " + " ?object filter contains(?object,\"" + input + "\") . "
				+ "} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("SearchDB_obj_StoreName & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("SearchDB_obj_StoreName & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("SearchDB_obj_StoreName & jArray : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		for (int i = 0; i < jArray.length(); i++) { // JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			resultArr.add(temp);
			// logger.info("SearchDB_obj_StoreName & temp(array) : " + temp);
		}
		return resultArr;
	}

	public String SearchDB_SP(String subject, String predicate) { // 상점이름과 사이트를 가지고 사이트 리턴
		logger.info("SearchDB_SP");
		String ExactStore = "";
		String ExactPre = "";
		String returnString = "";
		ExactStore = matchSubject(subject);
		// ExactPre = matchPredicate(predicate);
		ExactPre = matchPredicate(predicate);
		final String SEARCH_OBJ_TEMPLATE = "SELECT ?object " + "WHERE { <" + ExactStore + "> " + ExactPre
				+ "  ?object . " + "} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_OBJ_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("SearchDB_SP & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("SearchDB_SP &results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("SearchDB_SP &jArray : " + jArray.toString());
		if (jArray.length() == 0)
			return "";
		String objectResult = jArray.getJSONObject(0).getJSONObject("object").getString("value");
		// logger.info("SearchDB_SP & objectResult : " + objectResult);
		return objectResult;

	}

	/**
	 * 검색 알고리즘은 특정 단어를 포함하는 단어중 공통점이 많은 부분만 ... 가게명이 ABCD일경우 A검색 List B검색 List C검색
	 * List AB 검색 List BC 검색 List ABC 검색 List 검색 후 결과가 있다면 그걸 Store로 결정
	 */

	public String DecideWhichStore(List<String> storename_arr) {
		logger.info("DecideWhichStore");
		ArrayList<String> searchResults = new ArrayList<String>();
		String returnStoreName = "";
		String simple = "";
		String searchedStringCommon = "";

		for (String s : storename_arr) {
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
		// System.out.println("가장 많이 검색된 가게명 : " + searchedStringCommon);
		// System.out.println("가게명 후보 집합 : " + simple);
		if (matchSubject(simple) != null) { // 정확히 일치하는 게 있다면
			returnStoreName = simple;
		} else {
			returnStoreName = searchedStringCommon;
		}
		return returnStoreName;
	}

	/** A,B,C,D 등 작게 쪼갠 string을 검색해서 포함 string 을 리스트로 반환 */
	public ArrayList<String> DecideStoreBySplit(String input) {
		logger.info("DecideStoreBySplit");
		final String UPDATE_TEMPLATE = "SELECT ?subject ?object " + "WHERE { " + "?subject"
				+ " <http://13.209.53.196:3030/stores#이름> " + " ?object filter contains(?object,\"" + input + "\") . "
				+ "} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", UPDATE_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("DecideStoreBySplit & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("DecideStoreBySplit & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("DecideStoreBySplit & jArray : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List

		for (int i = 0; i < jArray.length(); i++) { // JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			resultArr.add(temp);
			// logger.info("DecideStoreBySplit & temp(array) : " + temp);
		}
		return resultArr;
	}

	// 수정완
	public String matchSubject(String storename) {
		logger.info("storename");
		String StoreSub = "";
		final String SEARCH_TEMPLATE = "SELECT ?subject " + "WHERE { " + "?subject "
				+ " <http://13.209.53.196:3030/stores#이름> " + " \"" + storename + "\" . " + "} ";

		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };

		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(args);
			logger.info("precss info:" + proc.toString());
			/** StringBuilder 사용하기 */
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			while ((line = reader.readLine()) != null) {
				logger.info(line);
				sb.append(line);
			}
			String sbToString = sb.toString();
			JSONObject jsonObj = null;
			jsonObj = new JSONObject(sbToString);
			// logger.info("matchSubject & jsonObj : " + jsonObj.toString());
			JSONObject results = (JSONObject) jsonObj.get("results");
			// logger.info("matchSubject & results : " + results.toString());
			JSONArray jArray = (JSONArray) results.get("bindings");
			// logger.info("matchSubject & jArray : " + jArray.toString());
			if (jArray.length() == 0)
				return "";
			StoreSub = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			// logger.info("matchSubject & StoreSub : " + StoreSub);

		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		return StoreSub;
	}

	public String searchStoreName(String storename) {
		logger.info("<-------------------  searchStoreName  ------------------->");
		final String SEARCH_TEMPLATE = "SELECT ?subject " + "WHERE { " + "?subject "
				+ " <http://13.209.53.196:3030/stores#이름> " + " \"" + storename + "\" . " + "} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("searchStoreName& jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("searchStoreName& results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("searchStoreName& jArray : " + jArray.toString());
		if (jArray.length() == 0)
			return "";
		String storeName = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		logger.info("searchStoreName& storeName : " + storeName);
		return storeName;
	}

	/**
	 * rdf 구조에 맞춰 matchPredicate 수정 또는 삭제하기 1. 별리달리 <위치> 알려줘 2. 대전 <카페> 알려줘
	 */
	public String matchPredicate(String p_word) { // 단어(ex.태그, 분위기, 영업 시간)을 input으로 받아 기존의 틀로 리턴받음
		logger.info("matchPredicate");
		// 수정할사항2) - <질문할 때> predicate 기반으로 틀을 리턴받고, 이 틀을 기반으로 질문해서 결과 받기
		String predicateResult = "";
		if (PredicateExist(p_word)) {
			// predicateResult = "<http://13.209.53.196:3030/stores#"+ p_word +">";
			predicateResult = "<" + searchP(p_word) + ">";
		} else {
			return "";
		}
		return predicateResult;
	}

	public boolean PredicateExist(String predicate) {
		logger.info("<-------------------  PredicateExist  ------------------->");
		final String SEARCH_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>  " + "ASK { ?s ?p \""
				+ predicate
				+ "\" FILTER (?p = <http://purl.org/dc/terms/alternative> || ?p = <http://www.w3.org/2000/01/rdf-schema#label>)  .} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("PredicateExist & jsonObj : " + jsonObj.toString());
		boolean trueorfalse = jsonObj.getBoolean("boolean");
		logger.info("PredicateExist & bool : " + trueorfalse);
		return trueorfalse;
	}

	@SuppressWarnings("unused")
	public String searchP(String p_word) {
		logger.info("searchP");
		String predicateResult = "";
		final String SEARCH_PREDICATE_FORM = "SELECT ?subject ?pre " + "WHERE { " + "?subject "
				+ " <http://purl.org/dc/terms/alternative> | <http://www.w3.org/2000/01/rdf-schema#label> " + " \""
				+ p_word + "\" . " + "} ";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_PREDICATE_FORM };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("searchP & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("searchP & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("searchP & jArray : " + jArray.toString());
		if (jArray.length() == 0)
			return "";
		String subjectResult = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		// logger.info("searchP & subjectResult : " + subjectResult);
		return subjectResult;
	}

	public boolean Whether_Info_Store(String subject_url) {
		logger.info("Whether_Info_Store");
		final String SEARCH_TEMPLATE = "PREFIX store: <http://13.209.53.196:3030/stores#> " + "ASK {" + subject_url
				+ " <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?object "
				+ "  FILTER (?object = store:음식점 ||  ?object = store:일반음식점) .}";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("Whether_Info_Store & jsonObj : " + jsonObj.toString());
		boolean trueorfalse = jsonObj.getBoolean("boolean");
		logger.info("Whether_Info_Store & bool : " + trueorfalse);
		return trueorfalse;
	}

	public ArrayList<String> UnionConditionSparql(String predicate, ArrayList<String> arr) {
		logger.info("UnionConditionSparql");
		String SEARCH_TEMPLATE = unionConditionTemplate(predicate, arr); // 동적으로 select하는 코드 만들기.
		logger.info("SPARQL : " + SEARCH_TEMPLATE);
		/**
		 * 1) 가게 유형 거르기 2) 태그, 메뉴 등(추가적으로 생성가능한 조건)으로 거르기
		 */
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("UnionConditionSparql & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("RESULT ARR : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		for (int i = 0; i < jArray.length(); i++) { // JSONArray 내 json 개수만큼 for문 동작
			String storename = jArray.getJSONObject(i).getJSONObject("object").getString("value");
			String location = "";
			String temp_hap = storename;
			if (jArray.getJSONObject(i).has("loc")) {
				location = jArray.getJSONObject(i).getJSONObject("loc").getString("value");
				temp_hap = temp_hap + "|" + location;
			}
			logger.info("name + " + storename + "\t location : " + location);
			resultArr.add(temp_hap);
			logger.info("UnionConditionSparql & temp(array) : " + temp_hap);
		}
		return resultArr;
	}

	// <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> -타입
	public String unionConditionTemplate(String storetype, ArrayList<String> conditions) {
		logger.info("unionConditionTemplate");
		String SEARCH_TEMPLATE_first = "PREFIX store: <http://13.209.53.196:3030/stores#> SELECT ?subject ?object ?loc WHERE {";
		String SEARCH_TEMPLATE_storetype = " ?subject store:음식점분류 <" + Store_type_uri(storetype) + ">. ";
		String SEARCH_TEMPLATE_middle = "";
		int temp_count = 1;
		for (String onecondition : conditions) {
			temp_count++;
			SEARCH_TEMPLATE_middle += " ?subject ?temp_p" + Integer.toString(temp_count) + " ?temp_o"
					+ Integer.toString(temp_count) + " FILTER contains(?temp_o" + Integer.toString(temp_count) + ", \""
					+ onecondition + "\") .";
		}
		String SEARCH_TEMPLATE_last = "?subject <http://13.209.53.196:3030/stores#이름> ?object. "
				+ " OPTIONAL { ?subject store:주소 ?loc .}}";
		final String UPDATE_TEMPLATE = SEARCH_TEMPLATE_first + SEARCH_TEMPLATE_storetype + SEARCH_TEMPLATE_middle
				+ SEARCH_TEMPLATE_last;
		return UPDATE_TEMPLATE;
	}

	public String Store_type_uri(String store_type) {
		logger.info("Store_type_uri");
		final String SEARCH_TEMPLATE = "SELECT ?subject { ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?object "
				+ "  FILTER (?object = <http://13.209.53.196:3030/stores#음식점> || ?object = <http://13.209.53.196:3030/stores#일반음식점>). "
				+ "?subject ?predicate \"" + store_type + "\"}";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		// logger.info("Store_type_uri & sonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("Store_type_uri & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("Store_type_uri & jArray : " + jArray.toString());
		if (jArray.length() == 0)
			return "";
		String subjectResult = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		// logger.info("Store_type_uri & subjectResult : " + subjectResult);
		return subjectResult;
	}

	public String returnSpecByName(String name) {
		final String SEARCH_TEMPLATE = "PREFIX store: <http://13.209.53.196:3030/stores#>"
				+ "SELECT ?subject ?query ?object WHERE {" + "?subject ?predicate \"" + name + "\"."
				+ "?subject ?query ?object FILTER (?query != store:음식점분류)" + "}";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				// logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("Store_type_uri & sonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		// logger.info("Store_type_uri & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		// logger.info("Store_type_uri & jArray : " + jArray.toString());
		if (jArray.length() == 0)
			return "";
		String subjectResult = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		// logger.info("Store_type_uri & subjectResult : " + subjectResult);
		return subjectResult;
	}

	public String condition_list(String storename, String condition) {
		logger.info("condition_list");
		final String SEARCH_TEMPLATE = "PREFIX store: <http://13.209.53.196:3030/stores#>"
				+ " SELECT ?temp WHERE { ?subject store:이름\"" + storename + "\". ?subject store:" + condition
				+ " ?temp. }";
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_TEMPLATE };
		Process proc = null;
		/** StringBuilder 사용하기 */
		StringBuilder sb = new StringBuilder();
		try {
			proc = Runtime.getRuntime().exec(args);
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		JSONObject results = (JSONObject) jsonObj.get("results");
		JSONArray jArray = (JSONArray) results.get("bindings");
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		for (int i = 0; i < jArray.length(); i++) { // JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("temp").getString("value");
			resultArr.add(temp);
		}
		String resultString = "";
		if (!resultArr.isEmpty()) {
			resultString += condition + " : ";
			for (String i : resultArr) {
				resultString += (i + ",");
			}
			resultString = "\n" + resultString.substring(0, resultString.length() - 1);
		}

		return resultString;
	}
}