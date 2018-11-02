package Test.test.URIChatbot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

public class MorphAnalysis {
	private Logger logger = LoggerFactory.getLogger(MorphAnalysis.class);
	private String openApiURL;
	private String accessKey;
	private String analysisCode;
	private Gson gson;
	private SparqlQuery query;
	private MakeResponse respond;
	public String resultStoreSparql;

	public String getresultStoreSparql() {
		return resultStoreSparql;
	}

	public void setresultStoreSparql(String resultStoreSparql) {
		this.resultStoreSparql = resultStoreSparql;
	}

	public MorphAnalysis() {
		openApiURL = "http://aiopen.etri.re.kr:8000/WiseQAnal";
		accessKey = "d303f91a-0f8f-4f58-acab-5d85944807ff"; // 발급받은 Access Key
		analysisCode = "SRL"; // 언어 분석 코드
		gson = new Gson();
		query = new SparqlQuery();
		respond = new MakeResponse();
		resultStoreSparql = null;
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	public String analyze(String text) throws ParseException, IOException {
		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();

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

		// return respond.MakeStoreRecommend(geoloc("대전 중구 태평로"), "테스트",
		// "www.google.com");

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

			logger.info("--------return_object----------");
			JSONObject return_object = (JSONObject) root.get("return_object");
			logger.info("--------orgQInfo----------");
			JSONObject orgQInfo = (JSONObject) return_object.get("orgQInfo");
			logger.info("--------orgQUnit----------");
			JSONObject orgQUnit = (JSONObject) orgQInfo.get("orgQUnit");
			logger.info("--------nDoc----------");
			JSONObject nDoc = (JSONObject) orgQUnit.get("ndoc");
			logger.info("--------sentence----------");

			JSONArray sentence = (JSONArray) nDoc.get("sentence");

			JSONObject morp_bf = (JSONObject) sentence.get(0);
			JSONArray MORPEVALArray = (JSONArray) morp_bf.get("morp_eval");
			JSONArray MORPArray = (JSONArray) morp_bf.get("morp");
			JSONArray WSDArray = (JSONArray) morp_bf.get("WSD");
			JSONArray vLATs = (JSONArray) orgQUnit.get("vLATs");
			logger.info("////////////	  vLATs 이전");
			if (vLATs.size() != 0) { // 어휘정답유형 존재할 경우 - Predicate로 취급해준다
				JSONObject strLAT_o = (JSONObject) vLATs.get(0);
				String strLAT = (String) strLAT_o.get("strLAT");
				predicate = strLAT;
				predicate.trim();
				logger.info("질문분석 끝난 PREDICATE : " + predicate);
				/** Predicate의 종류 : 카페, 음식점 or 메뉴, 영업시간 */
				predicate_spec = query.matchPredicate(predicate);
				if (predicate_spec.equals("")) {
					return respond.MakeJsonObject_plaintext("매치되는 정보 분류가 없어요 ㅠㅠ");
				}
				logger.info("///////////매치되는 정보 존재");
			} else {
				return respond.MakeJsonObject_plaintext("잘 이해하지 못했어요");

			}

			// ArrayList<String> NNGList = new ArrayList<String>();
			ArrayList<String> VANNGList = new ArrayList<String>();
			List<String> WSDList = new ArrayList<String>();
			/** 조사만 제외한 것 - 상점명을 온전히 가져오기 위함 */
			/**
			 * 마지막 조사만 제외하자! for (int MCount = 0; MCount < MORPArray.size(); MCount++) {
			 * JSONObject Mtemp = (JSONObject) MORPArray.get(MCount); String type = (String)
			 * Mtemp.get("type"); String NNGString = (String) Mtemp.get("lemma");
			 * NNGList.add(NNGString); //logger.info("MCountNNG : " + NNGString); if
			 * (type.contains("J")) { NNGList_remove.add(NNGString); } }
			 */

			/** 형용사, 명사만을 포함한 것 - 태그 정보 온전히 가져오기 위함 */
			for (int MACount = 0; MACount < MORPEVALArray.size(); MACount++) {
				JSONObject MAtemp = (JSONObject) MORPEVALArray.get(MACount);
				String type = (String) MAtemp.get("result");
				String NNGVAString = (String) MAtemp.get("target");
				if (type.contains("NNG") || type.contains("VA")) {
					String[] splitbyplus = type.split("\\+");
					List<String> part = Arrays.asList(splitbyplus);
					for (String temp : part) { // 한 단어에서 이루어짐
						String context = temp.split("/")[0];
						String spec_type = temp.split("/")[1];
						if (spec_type.equals("NNG")) {
							VANNGList.add(context);
						}
						if (spec_type.equals("VA")) {
							VANNGList.add(NNGVAString);
						}
					}
				}
			}
			/** 조사만 제외한 것 - 상점명을 온전히 가져오기 위함 */
			/** 마지막 조사만 제외하자! */
			List<String> WSDList_text = new ArrayList<String>();
			List<String> WSDList_type = new ArrayList<String>();
			for (int WCount = 0; WCount < WSDArray.size(); WCount++) {
				JSONObject Wtemp = (JSONObject) WSDArray.get(WCount);
				String type = (String) Wtemp.get("type");
				String WSD_text = (String) Wtemp.get("text");
				WSDList_type.add(type);
				WSDList_text.add(WSD_text);
				logger.info("WCountNNG : " + WSD_text);

			}
			int WSDList_cutline = WSDList_text.lastIndexOf(predicate);
			WSDList_text = WSDList_text.subList(0, WSDList_cutline);
			WSDList_type = WSDList_type.subList(0, WSDList_cutline);

			int removePosition = -1;
			for (int i = 0; i < WSDList_type.size(); i++) {
				if (WSDList_type.get(i).contains("J")) {
					removePosition = i;
				}
			}

			if (removePosition != -1 && removePosition != WSDList_type.size()) {
				WSDList_text = new ArrayList<String>(WSDList_text.subList(0, removePosition));
				WSDList_type = new ArrayList<String>(WSDList_type.subList(0, removePosition));
			}
			WSDList = WSDList_text;
			logger.info("PREDICATE : " + predicate);
			logger.info("PREDICATE_SPEC : " + predicate_spec);
			if (query.Whether_Info_Store(predicate_spec)) {
				logger.info("-------------------가게, 카페, 술집 메서드 진입 ----------------------------");
				/** predicate이 가게, 카페, 술집인 경우 */

				int cutLine = VANNGList.lastIndexOf(predicate);
				String conditionstoLine = "";
				for (String a : VANNGList) {
					conditionstoLine += a + "\t";
				}
				logger.info("NounList + " + conditionstoLine);
				ArrayList<String> DependencyList = new ArrayList<String>(VANNGList.subList(0, cutLine));
				if (DependencyList.isEmpty())
					return respond.MakeJsonObject_plaintext("다시 검색해 주세요");
				/// dddddddddd
				conditionstoLine = "";
				for (String a : DependencyList) {
					conditionstoLine += a + "\t";
				}
				logger.info("변한 dependencylist + " + conditionstoLine);
				logger.info("predicate : " + predicate);
				return AnswerSuitableStore(predicate, DependencyList);

			} else {
				/** predicate이 분위기, 위치 등일 경우 */
				Answer = AnswerStoreInfo(predicate, WSDList);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return respond.MakeJsonObject_plaintext(Answer);

	}

	/**
	 * @param predicate
	 * @param NNGList
	 *            형태소 분석이 끝난 문장의 구성요소가 들어 있는 리스트
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public String AnswerStoreInfo(String predicate, List<String> WSDList) throws IOException {
		logger.info("----------------------answerstoreinfo진입--------------------------");
		List<String> storename_arr = WSDList;

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
			ArrayList<String> crawl = new ArrayList<String>();
			if (crawl.isEmpty())
				return realstorename + "의 " + predicate + " 정보가 없습니다. 가르치기 명령어를 통해 알려주세요!";
			/**
			 * String testline = ""; ArrayList<String> cResult = Crawling(); for(String a :
			 * cResult) { testline += a; } return testline;
			 */
		}
		return Answer(realstorename, predicate, finalResult);
	}

	/**
	 * 여기에 들어가는
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public String AnswerSuitableStore(String predicate, ArrayList<String> arr) throws IOException, ParseException {
		// 가게를 한정하기 - 일반 음식점, 카페, 술집 등 분류해서 필터링
		ArrayList<String> suitableStores = new ArrayList<String>();
		String searchtemplate = "";
		for (String a : arr) {
			searchtemplate += a;
		}
		searchtemplate += predicate;
		logger.info("---------------ANSWERSUITABLESTORE----------------");
		suitableStores = query.UnionConditionSparql(predicate, arr);
		this.resultStoreSparql = query.getFirst_middle_sparql();
		if (!suitableStores.isEmpty()) {
			for (String s : suitableStores) {
				logger.info("S!!!!!!!!!" + s);
				String name = s;
				String location = "";
				String locSearchUrl = "";
				if (s.contains("|")) {
					name = s.split("\\|")[0];
					location = s.split("\\|")[1];
					locSearchUrl = geoloc(location);
				}

				logger.info("NAME!!!!!!!!!" + name);
				logger.info("loc!!!!!!!!!" + location);
				return makeCard(name);
			}
		}

		String c_result = Crawling(searchtemplate);
		if (c_result.equals("")) {
			logger.info("만족하는 가게가 없다");
			return respond.MakeJsonObject_plaintext("검색을 거쳤지만 만족하는 가게가 없었어요 :(");// CRAWLING fail!!
		} else {
			logger.info("카드 만드는중");
			return makeCard(c_result);
		}
		/**
		 * test String testline = ""; ArrayList<String> cResult = Crawling(); for(String
		 * a : cResult) { testline += a; } return testline;
		 */
	}
	
	public String makeCard(String storename) throws UnsupportedEncodingException, ParseException {
		String test = query.condition_list_only(storename, "주소");
		logger.info("test : " + test);
		String picUrl = geoloc(test);
		String exp = "말씀하신 조건으로 탐색해보았습니다!\n"+ storeInfoAll(storename);
		logger.info("picUrl : " + picUrl);
		logger.info("exp : " + exp);
		return respond.MakeStoreRecommend(picUrl, exp,
				"https://www.diningcode.com/isearch.php?query=" + storename);
	}

	private String Answer(String subject, String predicate, String objective) {
		// TODO Auto-generated method stub
		return subject + "의 " + predicate + "(은)는 " + objective + "입니다.";
	}

	public String storeInfoAll(String name) {
		return "가게명 : " + name + query.condition_list(name, "주소") + query.condition_list(name, "사이트")
				+ query.condition_list(name, "연락처") + query.condition_list(name, "영업시간")
				+ query.condition_list(name, "가격") + query.condition_list(name, "메뉴")
				+ query.condition_list(name, "태그");
	}

	public String Crawling(String input) throws IOException {
		logger.info("Crawling INPUT : " + input);
		String result = "";
		Connection.Response response = Jsoup.connect("https://www.diningcode.com/isearch.php?query=" + input)
				.method(Connection.Method.GET).execute();
		Document url = response.parse();
		Elements resultList = url.select("div#div_normal .list li");
		if (resultList.size() == 0) {
			resultList = url.select("div#div_rn .list li");
		}
		int crawlSize = Math.min(1, resultList.size());
		logger.info("crawlsize : " + crawlSize);
		for (int count = 0; count < crawlSize; count++) {
			String href = resultList.get(count).select("a").first().attr("href");
			System.out.println(href);
			result = CrawlingSpec("https://www.diningcode.com" + href);
		}
		return result;
	}

	/** 새로 생성한 이름 return */
	public String CrawlingSpec(String link) throws IOException {
		logger.info("CrawlingSpec");
		Connection.Response response = Jsoup.connect(link).method(Connection.Method.GET).execute();
		String name = "";
		try {
			Document url = response.parse();
			Elements resultList = url.select("ul.list").select("li");
			Elements tagList = resultList.select("li.tag").select("span");
			name = url.select("div.s-list.pic-grade .tit-point .tit").text();
			String tagString = "";
			for (int count = 0; count < tagList.size(); count++) {
				String tag = tagList.get(count).text();
				tagString += "#" + tag;
			}
			String locat = resultList.select("li.locat").text();
			String tel = resultList.select("li.tel").text();
			Elements timeList = url.select("div.busi-hours.short").select("ul.list").select("li");
			String worktime = "";
			for (int i = 0; i < timeList.size(); i++) {
				String r = timeList.get(i).select(".r-txt").text();
				worktime += r;
			}
			Elements menuList = url.select("div.menu-info.short").select("ul.list").select("li");
			String menuString = "";
			for (int i = 0; i < menuList.size(); i++) {
				String r = menuList.get(i).select(".l-txt").text();
				menuString += r + "+";
			}
			if (menuList.size() != 0) {
				menuString = menuString.substring(0, menuString.length() - 1);
			}
			String[] menuSplit = menuString.split("\\+");
			logger.info("NAME : " + name);
			logger.info("WORKTIME : " + worktime);
			logger.info("TEL : " + tel);
			logger.info("LOCAT : " + locat);
			logger.info("TAGSTRING : " + tagString);
			logger.info("MENU : " + menuString);
			if (!query.storeExist(name)) {
				String INSERT_TEMPLATE_FIRST = "PREFIX store: <http://13.209.53.196:3030/stores#> INSERT DATA { ";
				String INSERT_TEMPLATE_MIDDLE = "";
				String INSERT_TEMPLATE_LAST = "}";
				String trimStore = name.replace(" ", "");

				query.teachNewStore(trimStore);
				String StoreSub = "<" + query.matchSubject(trimStore) + ">";
				INSERT_TEMPLATE_MIDDLE += StoreSub + " store:음식점분류   store:음식점 . ";
				if (!locat.equals("")) {
					INSERT_TEMPLATE_MIDDLE += StoreSub + " store:주소 " + "\"" + locat + "\". ";
				}
				if (!tel.equals("")) {
					INSERT_TEMPLATE_MIDDLE += StoreSub + " store:전화번호 " + "\"" + tel + "\". ";
				}
				if (!worktime.equals("")) {
					INSERT_TEMPLATE_MIDDLE += StoreSub + " store:영업시간 " + "\"" + worktime + "\". ";
				}
				if (!menuString.equals("")) {
					for (int i = 0; i < menuSplit.length - 1; i++) {
						INSERT_TEMPLATE_MIDDLE += StoreSub + " store:메뉴 " + "\"" + menuSplit[i] + "\". ";
					}
				}
				if (!tagString.equals("")) {
					query.teachStoreInfo_TagCase(name, tagString);
				}
				final String INSERT_TEMPLATE = INSERT_TEMPLATE_FIRST + INSERT_TEMPLATE_MIDDLE + INSERT_TEMPLATE_LAST;
				logger.info("INSERT TEMPLATE : " + INSERT_TEMPLATE);

				String[] args = new String[] { "/home/ubuntu/apache-jena-fuseki-3.7.0/bin/s-update", "--service",
						"http://13.209.53.196:3030/stores", INSERT_TEMPLATE };
				Process proc = null;
				logger.info("UPDATE TEMPLATE LAST : ", INSERT_TEMPLATE);
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
		} catch (Exception e) {
			logger.info(e.getMessage());
			return "";
		}
		return name;
	}

	public String geoloc(String loc) throws ParseException, UnsupportedEncodingException {
		String lnglat = "";
		if (loc.equals("")) {
			return "";
		}

		InputStream inputStream = null;
		loc = URLEncoder.encode(loc, "UTF-8");
		String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + loc
				+ "&key=AIzaSyCGmk4qA39DMJMoznd8JqnKuzy2U2puF6A";
		String json = "";

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(url);
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			inputStream = entity.getContent();
		} catch (Exception e) {
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
			StringBuilder sbuild = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sbuild.append(line);
			}
			inputStream.close();
			json = sbuild.toString();
		} catch (Exception e) {
		}

		// now parse
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json);
		JSONObject jb = (JSONObject) obj;

		// now read
		JSONArray jsonObject1 = (JSONArray) jb.get("results");
		JSONObject jsonObject2 = (JSONObject) jsonObject1.get(0);
		JSONObject jsonObject3 = (JSONObject) jsonObject2.get("geometry");
		JSONObject location = (JSONObject) jsonObject3.get("location");

		System.out.println("Lat = " + location.get("lat"));
		System.out.println("Lng = " + location.get("lng"));
		lnglat = location.get("lng") + "," + location.get("lat");
		return "https://openapi.naver.com/v1/map/staticmap.bin?clientId=Jf5OTPFkoCHjr1ZVg4Ol&url=http://13.209.53.196&crs=EPSG:4326&center="
				+ lnglat + "&level=10&w=320&h=320&baselayer=default&markers=" + lnglat;
	}

}
