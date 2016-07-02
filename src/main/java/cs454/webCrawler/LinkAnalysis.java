package cs454.webCrawler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LinkAnalysis {
	
	private File[] htmlFiles;
	private Map<String, String> urlMap = new HashMap<String, String>();
	private Map<String, HashSet<String>> outMap = new HashMap<String, HashSet<String>>();
	private Map<String, HashSet<String>> inMap = new HashMap<String, HashSet<String>>();
	private Map<String, HashSet<String>> translatedMap = new HashMap<String, HashSet<String>>();
	private Map<String, Double> previousScores = new HashMap<String, Double>();
	private Map<String, Double> pageRankScores = new HashMap<String, Double>();
	private Map<String, Double> normalizedPageRankScores = new HashMap<String, Double>();
	private boolean firstRun = true;
	
	@SuppressWarnings("rawtypes")
	public LinkAnalysis(String directory, HashMap<String, String> urlMap){
		File file = new File(directory);
		this.urlMap.putAll(urlMap);
		htmlFiles = file.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith("html");
			}
		});
		for (File f: htmlFiles){
			parse(f);
		}
		findInLinks();
		
		Map<String, Double> initialScores = new HashMap<String, Double>();
		Iterator iterator = urlMap.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry entry = (Map.Entry)iterator.next();
			initialScores.put((String) entry.getValue(), 1.0/urlMap.size());
			previousScores.put((String) entry.getValue(), 0.0);
		}
		translateMap();
		pageRankScores = findScores((HashMap<String, Double>) initialScores);
	}
	
	public void translateMap(){
		for(Map.Entry<String, String> entry: urlMap.entrySet()){
			translatedMap.put(entry.getValue(), outMap.get(entry.getKey()));
		}
	}
	
	public void parse(File fileIn){
		Document doc = null;
		try {
			doc = Jsoup.parse(fileIn, "UTF-8");
		} catch (IOException e) {
			System.out.println("Unable to parse the file.");
			e.printStackTrace();
		}
		String title = fileIn.getName();
		Elements links = doc.select("a[href]");
		Set<String> uniqueLinks = new HashSet<String>();
		for (Element link : links){
			String linkString = link.attr("href");
			uniqueLinks.add(linkString);
		}
		outMap.put(title, (HashSet<String>) uniqueLinks);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void findInLinks(){
		Iterator iterator = urlMap.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry entry = (Map.Entry)iterator.next();
			Iterator outIt = outMap.entrySet().iterator();
			Set<String> ref = new HashSet<String>();
			while (outIt.hasNext()){
				Map.Entry outEntry = (Map.Entry)outIt.next();
				
				String currentUrl = (String) entry.getValue();
				Set<String> listOfLinks = (Set<String>) outEntry.getValue();
				for (String link: listOfLinks){
					//prevents counting a html page linking to itself
					if (entry.getKey().equals(outEntry.getKey())){
						continue;
					}
					else if (link.equals(currentUrl)){
						String keyString = (String) outEntry.getKey();
						ref.add((String) urlMap.get(keyString));
					}
					else if (link.length() > 3){
						String linkString = link;
						if (linkString.charAt(linkString.length() - 1) == '/'){
							linkString = linkString.substring(0, linkString.length() - 1);
							if (linkString.equals(currentUrl)){
								String keyString = (String) outEntry.getKey();
								ref.add((String) urlMap.get(keyString));
							}
						}
						else if (linkString.charAt(linkString.length() - 1) == '#' && linkString.length() > 1){
							String tempString = linkString.substring(0, linkString.length() - 1);
							if (tempString.equals(currentUrl)){
								String keyString = (String) outEntry.getKey();
								ref.add((String) urlMap.get(keyString));
							}
							if (tempString.charAt(tempString.length() - 1) == '/'){
								linkString = tempString.substring(0, tempString.length() - 1);
								if (linkString.equals(currentUrl)){
									String keyString = (String) outEntry.getKey();
									ref.add((String) urlMap.get(keyString));
								}
							}
						}
					}
					else{
						
					}
				}				
		}
			inMap.put(entry.getValue().toString(), (HashSet<String>) ref);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public HashMap<String, Double> findScores(HashMap<String, Double> currentScoresArray){
		boolean converged = true;
		for (Map.Entry<String, Double> entry: currentScoresArray.entrySet()){
			double converge = Math.abs(currentScoresArray.get(entry.getKey()) - previousScores.get(entry.getKey()));
			if (converge > 0.01){//0.1 or 0.01, anything higher or lower is not recommended
				converged = false;
			}
		}
		if (firstRun){
			converged = false;
			firstRun = false;
			
		}
		if (converged){
			return currentScoresArray;
		}
		Map<String, Double> updatedScores = new HashMap<String, Double>();
		Iterator iterator = currentScoresArray.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry entry = (Map.Entry)iterator.next();
			HashSet<String> referers = inMap.get(entry.getKey());
			Double newScore = 0.0;
			for (String referPage: referers){
				newScore += currentScoresArray.get(referPage)/translatedMap.get(referPage).size();
			}
			updatedScores.put((String) entry.getKey(), newScore);
		}
		previousScores.clear();
		previousScores.putAll(currentScoresArray);
		return findScores((HashMap<String, Double>) updatedScores);
	}
	
	public void normalizeScores(){
		double max = 0.0;
		for(Map.Entry<String, Double> entry: pageRankScores.entrySet()){
			if(entry.getValue() > max){
				max = entry.getValue();
			}
		}
		for(Map.Entry<String, Double> entry: pageRankScores.entrySet()){
			normalizedPageRankScores.put(entry.getKey(), entry.getValue()/max);
		}
	}
	
	public Map<String, HashSet<String>> getInMap(){
		return inMap;
	}
	public Map<String, HashSet<String>> getOutMap(){
		return outMap;
	}
	public HashMap<String, Double> getScores(){
		return (HashMap<String, Double>) pageRankScores;
	}
	public HashMap<String, Double> getNormalizedScores(){
		normalizeScores();
		return (HashMap<String, Double>) normalizedPageRankScores;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getJson(){
		normalizeScores();
		JSONObject finalJsonScore = new JSONObject();
		for(Map.Entry<String, Double> entry: normalizedPageRankScores.entrySet()){
			JSONObject scoreJson = new JSONObject();
			scoreJson.put("score", entry.getValue().toString());
			String documentName = "";
			for(Map.Entry<String, String> entry2: urlMap.entrySet()){
				if (entry.getKey().equals(entry2.getValue())){
					documentName = entry2.getKey();
				}
			}
			scoreJson.put("document", documentName);
			scoreJson.put("url", entry.getKey());
			finalJsonScore.put(entry.getKey(), scoreJson);
		}
		return finalJsonScore;
	}
}