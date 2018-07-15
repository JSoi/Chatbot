package Test.test.URIChatbot;


public enum PREDICATE {
	ADDRESS("주소"),
	OPENINGHOURS("영업시간"),
	SORT("음식점종류"),
	MENU("메뉴"),
	PHONENUM("전화번호"),
	SITE("사이트"),
	PRICE("가격");
	
	private String label;
	PREDICATE(String label)
	{this.label = label;}
	public String getlabel() {
		return label;
	}
	
}
