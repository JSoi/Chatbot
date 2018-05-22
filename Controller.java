package com.Test.test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class Controller {

	public static String FLAG;
	private MorphAnalysis analysis;
	private AIConfiguration configuration;
	private AIDataService dataService;
	private AIRequest request;
	private AIResponse response;
	private MakeResponse data = new MakeResponse();
	private SparqlQuery query;
	private TripleStore newStore;
	private MakeResponse JsnRespond;
	private Logger logger = LoggerFactory.getLogger(Controller.class);
	private DialogFlow dialogflow = new DialogFlow();

	public Controller() {
		String key = "017c5fd9b64a4f2ca68debd74d10bbac";
		this.analysis = new MorphAnalysis();
		configuration = new AIConfiguration(key);
		dataService = new AIDataService(configuration);
		this.query = new SparqlQuery();
		this.JsnRespond = new MakeResponse();
	}

	public String flow(String line)
			throws AIServiceException, ParseException, IOException, org.json.simple.parser.ParseException {
		String result = dialogflow.dialog(line);

		if (!result.equals(""))
			return JsnRespond.MakeJsonObject(result);
		else {
			if (line.contains(":newstore") || line.contains(":teach") || FLAG.contains("teaching")) {
				FLAG = "teaching Process";
				return teachingProcess(line);
			} else
				return analysis.analyze(line);
		}
	}

	private String teachingProcess(String line) throws IOException {

		if (FLAG.equals("teaching Predicates") && OneOfThePredicate(line)) {
			newStore.setPredicate(line);
			return JsnRespond.MakeJsonObject(line + "이 등록되었습니다");
		}
		if (line.contains(":newstore")) {// 새로운 상점 만들기
			String newStore = line.replace(":newstore", "").trim();
			query.teachNewStore(newStore);
			FLAG = "default";
			return JsnRespond.MakeJsonObject(newStore + "가 등록되었습니다. :teach를 통해서 가게 정보를 입력해주세요!");
		}
		if (line.contains(":teach")) {// 가르치기
			String storeName = line.replace(":teach", "").trim();
			if (storeName.isEmpty()) {
				return JsnRespond.MakeJsonObject("가르칠 음식점이름이 없습니다. 음식점 이름을 가르쳐주세요");
			}
			newStore = new TripleStore();
			newStore.setSubject(storeName);
			return JsnRespond.MakeJsonObject(storeName + "에대한 세부내용을 입력해주세요");
		}

		else {
			if (line.equals("그만하기")) {
				return JsnRespond.MakeJsonObject("가르치는것을 그만둘까요?");
			}
			if (IsYes(line)) {
				FLAG = "default";
				return JsnRespond.MakeJsonObject("그만둘께요");
			}
			if (FLAG.equals("teaching Predicates")) {
				newStore.setObject(line);
				query.teachStoreInfo(newStore.getSubject(), newStore.getPredicate(), newStore.getObject());
			}
			PREDICATE[] sortOfFood = PREDICATE.values();
			/* TODO 버튼이 한개씩 없어짐 */
			ArrayList<String> buttons = new ArrayList<>();
			for (int i = 0; i < sortOfFood.length; i++) {
				buttons.add(sortOfFood[i].getlabel());
				buttons.add("그만하기");
			}
			FLAG = "teaching Predicates";
			return JsnRespond.MakeJsonObject(buttons);

		}

	}

	private boolean IsYes(String line) {
		switch (line) {
		case "응":
		case "네":
		case "ㅇ":
		case "ㅇㅇ":
		case "yes":
		case "y":
		case "어":
		case "예":
			return true;
		default:
			return false;
		}
	}

	private boolean OneOfThePredicate(String line) {
		PREDICATE[] sortOfFood = PREDICATE.values();
		for (int i = 0; i < sortOfFood.length; i++) {
			if (line.equals(sortOfFood[i].getlabel()))
				return true;
		}
		return false;
	}

}
