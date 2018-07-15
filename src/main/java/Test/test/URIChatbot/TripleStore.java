package Test.test.URIChatbot;

public class TripleStore {
	String subject;
	String Predicate;
	String Object;
	
	public TripleStore() {
		super();
		this.Predicate = null;
		this.Object = null;
		this.subject = null;
	}
	public TripleStore(String subject, String predicate, String object) {
		super();
		this.subject = subject;
		Predicate = predicate;
		Object = object;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getPredicate() {
		return Predicate;
	}
	public void setPredicate(String predicate) {
		Predicate = predicate;
	}
	public String getObject() {
		return Object;
	}
	public void setObject(String object) {
		Object = object;
	}

}
