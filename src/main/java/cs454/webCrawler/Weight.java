package cs454.webCrawler;

import java.util.HashMap;

public class Weight {
	private HashMap<String, HashMap<String, Word>> weight;

	public HashMap<String, HashMap<String, Word>> getWeight() {
		return weight;
	}

	public void setWeight(HashMap<String, HashMap<String, Word>> weight) {
		this.weight = weight;
	}
}
