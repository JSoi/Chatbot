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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlQuery {
	private Logger logger = LoggerFactory.getLogger(SparqlQuery.class);
	MakeResponse JsnRespond = new MakeResponse();

	/**
	 * @param newStore
	 *            새로운 상점을 상점명(newStore)으로 가르칠 때 쓰는 메서드
	 */
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
			return "registered "+newStore;
			// System.out.println("이미 등록 된 상점입니다. :teach 명령어를 통해 상점에 대한 정보를 입력해주세요 ");
		}

	}

	public void teachStoreInfo(String subject, String predicate, String Objective) {// objective is RAW!!
		// TODO predicate 추가하기.
		String trimStore = subject.replace(" ", "");
		String StoreSub = matchSubject(trimStore);
		// String StorePre = matchPredicate(predicate);
		logger.info("subject - " + subject + "  predicate - " + predicate + "  Objective - " + Objective);
		final String UPDATE_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>" + "INSERT DATA" + "{ <"
				+ StoreSub + ">    stores:" + predicate + "   \"" + Objective + "\" ." + "}   ";
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
	public String teachStoreInfo_TagCase(String tagLine, String sparql_subject) {
		tagLine.trim();
		tagLine = tagLine.replaceAll(" ", "");
		String Raw_Tags[] = tagLine.split("#");
		List<String> tag_array = new ArrayList<>(Arrays.asList(Raw_Tags));
		tag_array.remove(0);
		String UPDATE_TEMPLATE_first = "PREFIX store: <http://13.209.53.196:3030/stores#> INSERT DATA {";
		String UPDATE_TEMPLATE_middle = "";
		for (String eachtag : tag_array)
			UPDATE_TEMPLATE_middle += "<" + sparql_subject + "> store:태그   \"" + eachtag + "\".  ";
		String UPDATE_TEMPLATE_last = "}";
		final String UPDATE_TEMPLATE = UPDATE_TEMPLATE_first + UPDATE_TEMPLATE_middle + UPDATE_TEMPLATE_last;
		return UPDATE_TEMPLATE;
	}

	public ArrayList<String> SearchDB_obj_StoreName(String input) { // 포함하는 list 리턴
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List		
		for (int i = 0; i < jArray.length(); i++) { //JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			resultArr.add(temp);
			logger.info("temp(array) : " + temp);
	    }
		return resultArr;
	}

	public String SearchDB_SP(String subject, String predicate) { // 상점이름과 사이트를 가지고 사이트 리턴
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		String objectResult = jArray.getJSONObject(0).getJSONObject("object").getString("value");
		logger.info("objectResult : " + objectResult);
		return objectResult;

	}

	/**
	 * 검색 알고리즘은 특정 단어를 포함하는 단어중 공통점이 많은 부분만 ... 가게명이 ABCD일경우 A검색 List B검색 List C검색
	 * List AB 검색 List BC 검색 List ABC 검색 List 검색 후 결과가 있다면 그걸 Store로 결정
	 */

	public String DecideWhichStore(ArrayList<String> candidates) {
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		
		for (int i = 0; i < jArray.length(); i++) { //JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			resultArr.add(temp);
			logger.info("temp(array) : " + temp);
	    }
		return resultArr;
	}

	
	//수정완
	public String matchSubject(String storename) {
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
			logger.info("sb : " + sb.toString());
			String sbToString = sb.toString();
			JSONObject jsonObj = null;
			jsonObj = new JSONObject(sbToString);
			logger.info("jsonObj : " + jsonObj.toString());
			JSONObject results = (JSONObject) jsonObj.get("results");
			logger.info("results : " + results.toString());
			JSONArray jArray = (JSONArray) results.get("bindings");
			logger.info("jArray : " + jArray.toString());
			StoreSub = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			logger.info("StoreSub : " + StoreSub);

		} catch (IOException e) {
			logger.info(e.getMessage());
		}

		return StoreSub;
	}

	public String searchStoreName(String storename) {
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		String storeName = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		logger.info("storeName : " + storeName);
		return storeName;
	}

	public boolean PredicateExist(String predicate) {
		final String SEARCH_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>  " + "ASK { ?s ?p \""
				+ predicate + "\"  .} ";
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
				logger.info("query : " + line);
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

	public boolean PredicateAltExist(String alt_predicate) {
		final String SEARCH_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>  "
				+ "ASK { ?s <http://purl.org/dc/terms/alternative> \"" + alt_predicate + "\".} ";
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("PredicateAltExist& jsonObj : " + jsonObj.toString());
		boolean trueorfalse = jsonObj.getBoolean("boolean");
		logger.info("PredicateAltExist & bool : " + trueorfalse);
		return trueorfalse;
	}

	/**
	 * rdf 구조에 맞춰 matchPredicate 수정 또는 삭제하기
	 */
	public String matchPredicate(String p_word) { // 단어(ex.태그, 분위기, 영업 시간)을 input으로 받아 기존의 틀로 리턴받음
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

	@SuppressWarnings("unused")
	public String searchP(String p_word) {
		String predicateResult = "";
		final String SEARCH_PREDICATE_FORM = "SELECT ?subject ?pre " + "WHERE { " + "?subject " + " ?pre " + " \"" + p_word
				+ "\" . " + "} ";
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("searchP & jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("searchP & results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("searchP & jArray : " + jArray.toString());
		String subjectResult = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		logger.info("searchP & subjectResult : " + subjectResult);
		return subjectResult;
	}
	
	public boolean Whether_Info_Store(String subject_url) {
		final String SEARCH_TEMPLATE = "PREFIX store: <http://localhost:3030/stores#> " + "ASK {" + subject_url
				+ " <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?object "
				+ "  FILTER (?object = store:음식점 || store:일반음식점) .}";
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
				logger.info("query : " + line);
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
		String SEARCH_TEMPLATE = unionConditionTemplate(predicate, arr); // 동적으로 select하는 코드 만들기.
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List		
		for (int i = 0; i < jArray.length(); i++) { //JSONArray 내 json 개수만큼 for문 동작
			String temp = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
			resultArr.add(temp);
			logger.info("temp(array) : " + temp);
	    }
		return resultArr;
	}

	// <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> -타입
	public String unionConditionTemplate(String storetype, ArrayList<String> conditions) {
		String SEARCH_TEMPLATE_first = "PREFIX store: <http://localhost:3030/stores#> SELECT ?subject ?object WHERE {";
		String SEARCH_TEMPLATE_storetype = " ?subject store:음식점분류 <" + Store_type_uri(storetype) + ">. ";
		String SEARCH_TEMPLATE_middle = "";
		for (String onecondition : conditions)
			SEARCH_TEMPLATE_middle += " ?subject ?temp_p ?temp_o FILTER contains(?temp_o, \"" + onecondition + "\") .";
		String SEARCH_TEMPLATE_last = "?subject <http://localhost:3030/stores#이름> ?object. }";
		final String UPDATE_TEMPLATE = SEARCH_TEMPLATE_first + SEARCH_TEMPLATE_storetype + SEARCH_TEMPLATE_middle
				+ SEARCH_TEMPLATE_last;
		return UPDATE_TEMPLATE;
	}
	
	public String Store_type_uri(String store_type) {
		final String SEARCH_TEMPLATE = "SELECT ?subject { ?subject <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?object "
				+ "  FILTER (?object = <http://localhost:3030/stores#음식점> || <http://localhost:3030/stores#일반음식점>). "
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
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		logger.info("sb : " + sb.toString());
		String sbToString = sb.toString();
		JSONObject jsonObj = null;
		jsonObj = new JSONObject(sbToString);
		logger.info("jsonObj : " + jsonObj.toString());
		JSONObject results = (JSONObject) jsonObj.get("results");
		logger.info("results : " + results.toString());
		JSONArray jArray = (JSONArray) results.get("bindings");
		logger.info("jArray : " + jArray.toString());
		String subjectResult = jArray.getJSONObject(0).getJSONObject("subject").getString("value");
		logger.info("subjectResult : " + subjectResult);
		return subjectResult;
	}


}