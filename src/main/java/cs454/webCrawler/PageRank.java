package cs454.webCrawler;

import java.util.ArrayList;
import java.util.List;

public class PageRank {
	private String currentUrl;
	private List<String> listOfLinks;
	private double tau;
	
	//ideally the input would be the entire Json file, or some sort of similar database that allows
	//the program to call on all websites and the links it contains.
	public PageRank(String currentUrl, List<String> listOfLinks, double tau){
		this.currentUrl = currentUrl;
		this.listOfLinks = listOfLinks;
		this.tau = tau;
	}
	
	public int timesReferenced(String url, List<String> listIn){
		int referenced = 0;
		for (String link: listIn){
			if (url.equals(link)){
				referenced++;
			}
		}
		return referenced;
	}
	
	public int referers(List<String> listIn){
		return listIn.size();
	}
	
	public double rankScore(int referenced, int numberOfRefer){
		double rank = 0;
		rank = referenced/numberOfRefer;
		return rank;
	}
	
	public void converge(){
		List<Double> max = new ArrayList<Double>();
		List<Double> min = new ArrayList<Double>();
		boolean converge = false;
		while (!converge){
			//repeat rankScore
			double tempResult = 0; //result = rankScore()
			
		}
		
	}
	
}
