package Test.test.URIChatbot;

public class ConditionTriple {
	String FLAG = "default";
	String location;
	String menu;
	String extra_info;
	String sparqlQuery;

	public ConditionTriple() {
		super();
		this.location = "";
		this.menu = "";
		this.extra_info = "";
		this.FLAG = "default";
		this.sparqlQuery = "";
		/**
		 * FLAG 1. LOC 2. MENU
		 */
	}

	public String getFLAG() {
		return FLAG;
	}

	public void setFLAG(String flag) {
		FLAG = flag;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public void addSparqlQuery(String addQuery) {
		this.sparqlQuery += addQuery;
	}

	public String getConditionSparql() {
		return "";
	}

	public String getJsonResponse() {
		return "";
	}
}
