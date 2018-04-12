package ai.api.examples;

import java.util.ArrayList;

public enum PREDICATE {
	ADDRESS("주소"),
	MENU("메뉴"),
	SITE("사이트"),
	CATEGORY("음식점 종류"),
	PHONENUM("연락처"),
	OPENINGHOURS("영업시간"),
	AGE("소비자층"),
	PRICE("가격");
	
	private String label;
	PREDICATE(String label){this.label = label;}
	public String getlabel() {
		return label;
	}
}
