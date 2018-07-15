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
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
		final String UPDATE_TEMPLATE = "SELECT ?subject ?object " + "WHERE { ?subject "
				+ " <http://13.209.53.196:3030/stores#이름> " + " ?object filter contains(?object,\"" + input + "\") . "
				+ "} ";
		String queryService = "http://13.209.53.196:3030/stores/sparql";
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, UPDATE_TEMPLATE);
		ResultSet results = q.execSelect();
		ArrayList<String> resultArr = new ArrayList<String>(); // 지식베이스에서 일치하는 거 리턴한 List
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("object");
			if (!resultArr.contains(x.toString()))
				resultArr.add(x.toString()); // object 후보 애들 다 넣어주기. 일단 contains query를 씀 - like 사용 시 수정
		}
		q.close();
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
		//String queryService_o = "http://13.209.53.196:3030/stores/sparql";
		//QueryExecution q_o = QueryExecutionFactory.sparqlService(queryService_o, SEARCH_OBJ_TEMPLATE);
		//ResultSet results_o = q_o.execSelect();
		//while (results_o.hasNext()) {
		//	QuerySolution soln = results_o.nextSolution();
		//	RDFNode x = soln.get("object");
		//	returnString = x.toString();
		//}
		//q_o.close();
		
		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores", SEARCH_OBJ_TEMPLATE };
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(args);
			/** StringBuilder 사용하기 */
			StringBuilder sb = new StringBuilder();
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = reader.readLine()) != null) {
				logger.info("query : " + line);
				sb.append(line);
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		return returnString;

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
		String queryService = "http://13.209.53.196:3030/stores/sparql";
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
		q.close();
		return resultArr;
	}

	public String matchSubject(String storename) {
		String StoreSub = "";
		//final String SEARCH_TEMPLATE = "SELECT ?subject " + "WHERE { " + "?subject "
		//		+ " <http://13.209.53.196:3030/stores#이름> " + " \"" + storename + "\" . " + "} ";

		String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-query", "--service",
				"http://13.209.53.196:3030/stores",
				"SELECT ?subject WHERE {?subject <http://13.209.53.196:3030/stores#이름> \"" + storename + "\".}" };

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
				
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(sb.toString());
			JSONObject parsingJson = (JSONObject)obj;
			String getText = (String) parsingJson.get("results");
			logger.info(getText);

			obj = parser.parse(getText);
			parsingJson = (JSONObject)obj;
			JSONArray getarryText = (JSONArray)parsingJson.get("bindings");
			logger.info(getarryText.toString());

			if(!getarryText.isNull(0)) {
				obj = parser.parse(getarryText.getString(0));
				parsingJson = (JSONObject)obj;
				getText = (String)parsingJson.get("subject");
				logger.info(getText);

				obj = parser.parse(getText);
				parsingJson = (JSONObject)obj;
				getText = (String)parsingJson.get("value");
				StoreSub = getText;
				logger.info(getText);

			}
			
			
			/*JSONObject json = new JSONObject(sb.toString());
			logger.info("json : " + json);
			JSONObject array = (JSONObject) json.get("results");
			logger.info("array : " + array);
			JSONArray realArray = array.getJSONArray("bindings");
			logger.info("realArray : " + realArray);

			if (realArray.toList().size() != 0)
				StoreSub = realArray.toString();*/
			
			logger.info("StoreSub : " + StoreSub);

		} catch (IOException e) {
			logger.info(e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return StoreSub;
	}

	public String searchStoreName(String storename) {
		final String SEARCH_TEMPLATE = "SELECT ?subject " + "WHERE { " + "?subject "
				+ " <http://13.209.53.196:3030/stores#이름> " + " \"" + storename + "\" . " + "} ";
		String queryService = "http://13.209.53.196:3030/stores/sparql"; // 인스턴스 목록에서 search
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_TEMPLATE);
		ResultSet results = q.execSelect();
		String StoreSub = "";
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("subject");
			StoreSub = x.toString();
		}
		q.close();
		return StoreSub;
	}

	public boolean PredicateExist(String predicate) {
		final String SEARCH_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>  " + "ASK { ?s ?p \""
				+ predicate + "\"  .} ";
		String queryService = "http://13.209.53.196:3030/stores/sparql";
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_TEMPLATE);
		boolean results = q.execAsk();
		q.close();
		return results;
	}

	public boolean PredicateAltExist(String alt_predicate) {
		final String SEARCH_TEMPLATE = "PREFIX stores: <http://13.209.53.196:3030/stores#>  "
				+ "ASK { ?s <http://purl.org/dc/terms/alternative> \"" + alt_predicate + "\".} ";
		String queryService = "http://13.209.53.196:3030/stores/sparql";
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_TEMPLATE);
		boolean results = q.execAsk();
		q.close();
		return results;
	}

	/**
	 * rdf 구조에 맞춰 matchPredicate 수정 또는 삭제하기
	 */
	public String matchPredicate(String p_word) { // 단어(ex.태그, 분위기, 영업 시간)을 input으로 받아 기존의 틀로 리턴받음
		// 수정할사항2) - <질문할 때> predicate 기반으로 틀을 리턴받고, 이 틀을 기반으로 질문해서 결과 받기
		String predicateResult = "";
		if (PredicateExist(p_word)) {
			// predicateResult = "<http://13.209.53.196:3030/stores#"+ p_word +">";
			predicateResult = searchP(p_word);
		} else {
			return "";
		}
		return predicateResult;
	}

	@SuppressWarnings("unused")
	public String searchP(String p_word) {
		String predicateResult = "";
		String subjectResult = "";
		final String SEARCH_PREDICATE_FORM = "SELECT ?sub ?pre " + "WHERE { " + "?sub " + " ?pre " + " \"" + p_word
				+ "\" . " + "} ";
		String queryService = "http://13.209.53.196:3030/stores/query"; // 뼈대에서 쿼리
		QueryExecution q = QueryExecutionFactory.sparqlService(queryService, SEARCH_PREDICATE_FORM);
		ResultSet results = q.execSelect();
		ArrayList<String> rdfnode_sub = new ArrayList<String>();
		ArrayList<String> rdfnode_pre = new ArrayList<String>();
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("sub");
			RDFNode y = soln.get("pre");
			subjectResult = x.toString();
			predicateResult = y.toString();
			subjectResult = "<" + subjectResult + ">";
			predicateResult = "<" + predicateResult + ">";
		}
		q.close();
		return subjectResult;
	}

}
