package cs454.webCrawler;

import java.util.HashMap;

public class DocumentPage {
	private HashMap<String, HashMap<String, WordInfo>> completeMap;

	public HashMap<String, HashMap<String, WordInfo>> getCompleteMap() {
		return completeMap;
	}

	public void setCompleteMap(
			HashMap<String, HashMap<String, WordInfo>> completeMap) {
		this.completeMap = completeMap;
	}
}
