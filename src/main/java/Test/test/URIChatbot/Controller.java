package Test.test.URIChatbot;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;

public class Controller {

	public static String FLAG;
	private MorphAnalysis analysis;
	private AIConfiguration configuration;
	@SuppressWarnings("unused")
	private AIDataService dataService;

	@SuppressWarnings("unused")
	private MakeResponse data;
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
		 data = new MakeResponse();
		this.query = new SparqlQuery();
		this.JsnRespond = new MakeResponse();
		
		
	}

	public String flow(String line)
			throws AIServiceException, ParseException, IOException, org.json.simple.parser.ParseException {
		String result = dialogflow.dialog(line);

		if (!result.equals(""))
			return JsnRespond.MakeJsonObject(result);
		else {
			if(line.equals("가르치기")||FLAG.contains("teaching")) {
				return teachingProcess(line);
			}
			else if(line.equals("질문하기"))
				return analysis.analyze(line);
			else
				return JsnRespond.MakeJsonObject(line);

		}
	}

	private String teachingProcess(String line) throws IOException {
		logger.info("FirstFLAG : "+FLAG);
		if(line.equals("가르치기")) {
			FLAG = "teaching Subject";
			return JsnRespond.MakeJsonObject("등록할 상점의 이름을 적어주세요");
		}
		if(line.equals("그만하기")) {
			FLAG = "teaching Stop";
			return JsnRespond.MakeJsonObject("가르치는것을 그만둘까요?");

		}
		
		
		
		if(FLAG.equals("teaching Subject")) {
			FLAG = "teaching Predicates";
			String makeComment = "";
			String subject =  query.teachNewStore(line);
			newStore = new TripleStore();
			if(subject.contains("registered")) {
				String subArr[] = subject.split(" ");
				subject = subArr[1];
				makeComment = "이미 등록한 상점이네요";
				}
			newStore.setSubject(subject);
			return JsnRespond.MakeJsonObject(makeComment+subject +"의 정보를 가르쳐 주세요",makebuttonsArr());
		}
		if(FLAG.equals("teaching Predicates")) {
			String predicate = line;
			newStore.setPredicate(predicate);
			FLAG = "teaching Predicates "+predicate;
			return JsnRespond.MakeJsonObject("음식점 "+newStore.getSubject()+"의 "+predicate+"정보를 입력해주세요");
			
		}
		if(FLAG.equals("teaching Predicates "+newStore.getPredicate())) {
			newStore.setObject(line);
			FLAG ="teaching yes or no";
			return JsnRespond.MakeJsonObject("음식점 "+newStore.getSubject()+"의 "+newStore.getPredicate()+"은 "+line+"입니다. 맞으면 예, 틀리면 아니요를 눌러주세요" );
		}
		if(FLAG.equals("teaching yes or no")) {
			if(IsYes(line)) {
				query.teachStoreInfo(newStore.getSubject(), newStore.getPredicate(), newStore.getObject());
				FLAG = "teaching Predicates";
				return JsnRespond.MakeJsonObject("등록되었습니다");
 				
			}
			else if(line.equals("아니요")) {
				FLAG = "teaching Predicates "+newStore.getPredicate();
				return  JsnRespond.MakeJsonObject("음식점 "+newStore.getSubject()+"의 "+newStore.getPredicate()+"정보를 다시 입력해주세요");
		}
			else {
				return JsnRespond.MakeJsonObject("예 아니요로 답해주세요");
			}
		}
		if(FLAG.equals("teaching Stop")) {
			if(IsYes(line)) {
				FLAG = "default";
				return JsnRespond.MakeJsonObject("그만둘게요");
			}	
		}
		logger.info("FLAG : "+FLAG);

		return line;
		
	}

	private String makebuttons() {
		/* TODO 버튼이 한개씩 없어짐 */
		PREDICATE[] sortOfFood = PREDICATE.values();
		ArrayList<String> buttons = new ArrayList<>();
		for (int i = 0; i < sortOfFood.length; i++) {
			buttons.add(sortOfFood[i].getlabel());
		}
		buttons.add("그만하기");

		FLAG = "teaching Predicates";
		return JsnRespond.MakeJsonObject("버튼을 입력해주세요",buttons);
	}

	private ArrayList<String> makebuttonsArr() {
		/* TODO 버튼이 한개씩 없어짐 */
		PREDICATE[] sortOfFood = PREDICATE.values();
		ArrayList<String> buttons = new ArrayList<>();
		for (int i = 0; i < sortOfFood.length; i++) {
			buttons.add(sortOfFood[i].getlabel());
		}
		buttons.add("그만하기");

		FLAG = "teaching Predicates";
		return buttons;
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
