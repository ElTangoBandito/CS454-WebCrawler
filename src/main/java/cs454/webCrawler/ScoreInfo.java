package cs454.webCrawler;

import java.util.HashMap;

public class ScoreInfo {
	private double tmax;
	private HashMap<String, Page> link;
	public HashMap<String, Page> getLink() {
		return link;
	}
	public void setLink(HashMap<String, Page> link) {
		this.link = link;
	}
	public double getTmax() {
		return tmax;
	}
	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

}
