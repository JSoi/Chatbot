package ai.api.examples.Chatbot;

import java.util.Iterator;

import com.github.andrewoma.dexx.collection.ArrayList;

public enum PREDICATE {
	
	ADDRESS("주소"),
	MENU("메뉴"),
	SITE("사이트"),
	CATEGORY("음식점 종류"),
	PHONENUM("연락처"),
	OPENINGHOURS("영업시간");
	
	private String label;
	private ArrayList<PREDICATE> list = new ArrayList<>();
	PREDICATE(String label){this.label = label;}
	public String getlabel() {
		return label;
	}


}
