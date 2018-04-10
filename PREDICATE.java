package ai.api.examples.Chatbot;

import java.util.Iterator;

import com.github.andrewoma.dexx.collection.ArrayList;

public enum PREDICATE {
	
	ADDRESS("주소"),
	MENU("메뉴"),
	SITE("사이트"),
	CATEGORY("음식점 종류"),
	PHONENUM("연락처"),
	OPENINGHOURS("음식점종류");
	
	private String label;
	private ArrayList<PREDICATE> list = new ArrayList<>();
	PREDICATE(String label){this.label = label;}
	public String getlabel() {
		return label;
	}
	public void addList() {
		list.append(ADDRESS);
		list.append(MENU);
		list.append(SITE);
		list.append(CATEGORY);
		list.append(PHONENUM);
		list.append(PHONENUM);
		list.append(OPENINGHOURS);	
		list.asList();
	}
	public boolean IsPredicate(String label) {
		addList();
		Iterator<PREDICATE> it = list.iterator();
		while(it.hasNext()) {
			if(it.next().getlabel().equals(label)) return true;
		}
		return false;
		
	}

}
