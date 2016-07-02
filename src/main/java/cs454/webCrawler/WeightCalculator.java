package cs454.webCrawler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WeightCalculator {

	private int numberOfDocuments = 0;
	private HashMap<String, HashMap<String, WordInfo>> documents;
	private Set<String> setList = new HashSet<String>();
	private List<String> listOfUniqueWords = new ArrayList<String>();
	private JSONObject JsonScores = new JSONObject();
	private List<JSONObject> jsonList = new ArrayList<JSONObject>();
	private List<JSONObject> normalizedJsonList = new ArrayList<JSONObject>();
	private double maxScore;
	
	public WeightCalculator(String path, int numberOfDocuments){
		this.numberOfDocuments = numberOfDocuments;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			Gson gson = new GsonBuilder().create();
			DocumentPage scoreInfo = gson.fromJson(br, DocumentPage.class);
			if(scoreInfo.getCompleteMap() == null){
				System.out.println("extract failed");
			}
			else{
				System.out.println("Extraction successful.");
			}
			System.out.println("Now loading data...");
			documents = scoreInfo.getCompleteMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
		listUniqueWords();
		
		for (String keyword: listOfUniqueWords){
			calcScores(keyword);
		}
		
		
		
	}
	
	public void listUniqueWords(){
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = documents.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			HashMap<String, WordInfo> documentWords = documents.get(key);
			for (@SuppressWarnings("rawtypes")
			Iterator iterator2 = documentWords.keySet().iterator(); iterator2.hasNext();){
				String key2 = (String) iterator2.next();	
				setList.add(key2);
			}
		}
		listOfUniqueWords.addAll(setList);
		java.util.Collections.sort(listOfUniqueWords);
	}
	
	@SuppressWarnings({ "unchecked" })
	public void calcScores(String keyword){
		double occurencesInDocuments = 0;
		List<JSONObject> documentsContainWord = new ArrayList<JSONObject>();
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = documents.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			HashMap<String, WordInfo> document = documents.get(key);
			if(document.containsKey(keyword)){
				try{
				occurencesInDocuments++;
				JSONObject outer = new JSONObject();
				JSONObject documentWordsJson = new JSONObject();
				documentWordsJson.put("document", key.toString());
				documentWordsJson.put("position", document.get(keyword).getPosition());
				documentWordsJson.put("frequency", document.get(keyword).getFrequency());
				outer.put(keyword, documentWordsJson);
				documentsContainWord.add(outer);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		for (JSONObject currentObj: documentsContainWord){
			JSONObject wordInfo = new JSONObject();
			JSONObject inner = (JSONObject) currentObj.get(keyword);
			wordInfo.put("word", keyword);
			wordInfo.put("tfidf", calculateWeight(currentObj, keyword, occurencesInDocuments));
			wordInfo.put("document", inner.get("document"));
			wordInfo.put("position", inner.get("position"));
			jsonList.add(wordInfo);
			//System.out.println(currentObj.get("document name") + " " + keyword + ": " + calculateWeight(currentObj, keyword, occurencesInDocuments));
		}
	}
	
	public double calculateWeight(JSONObject currentDoc, String word, double df){
		double idf = Math.log10(numberOfDocuments/df);
		JSONObject keywordJsonObj = (JSONObject) currentDoc.get(word);
		int frequency = (int) keywordJsonObj.get("frequency");
		double tf = Math.log(1 + frequency);
		double weight = tf * idf;
		return weight;
	}
	
	public void getMax(){
		double max = 0;
		for (JSONObject obj: jsonList){
			double value = (double) obj.get("tfidf");
			if (value > max){
				max = value;
			}
		}
		maxScore = max;
	}
	
	//this stores all the tf-idf scores of words in the document they belong to. The final object JsonScores will be the main output which we will later use.
	//depending on user's preference, the values can be normalized or not.
	@SuppressWarnings("unchecked")
	public void jsonCreator(boolean normalize){
		int countdown = documents.size() - 1;
		List<JSONObject> listOfScores = new ArrayList<JSONObject>();
		if (normalize){
			getMax();
			listOfScores.addAll(jsonList);
		}
		else{
			listOfScores.addAll(jsonList);
		}
		
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = documents.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			JSONObject currentWordInfo = new JSONObject();
			for (JSONObject objectWord: listOfScores){
				if (key.equals(objectWord.get("document"))){
					currentWordInfo.put(objectWord.get("word"), objectWord);
				}
			}
			JsonScores.put(key, currentWordInfo);
			System.out.println(countdown + " files remaining. " + "Writing " + key + ".");
			countdown--;
		}
	}
	
	public List<String> getWords(){
		return listOfUniqueWords;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getNormalizedJson(){
		for (JSONObject objectWord: jsonList){
			double tfidf = (double) objectWord.get("tfidf")/maxScore;
			//JSONObject tempObj = new JSONObject();
			String word = (String) objectWord.get("word");
			objectWord.put("word", word);
			objectWord.put("tfidf", tfidf);
			objectWord.put("document", objectWord.get("document"));
			objectWord.put("position", objectWord.get("position"));
			normalizedJsonList.add(objectWord);
		}
		jsonCreator(true);
		return JsonScores;
	}
	
	@SuppressWarnings("unchecked")
	public void exportJson(){
		/*
		double countdown = jsonList.size();
		System.out.println("Creating Json file, please wait...");
		for (JSONObject objectWord: jsonList){
			String word = (String) objectWord.get("word");
			System.out.println(countdown + " words left to normalize.");
			countdown--;
			System.out.println(word + objectWord.get("tfidf"));
			double tfidf = (double) objectWord.get("tfidf")/getMax();
			objectWord.put("word", objectWord.get("word"));
			objectWord.put("tfidf", tfidf);
			objectWord.put("document", objectWord.get("document"));
			objectWord.put("position", objectWord.get("position"));
			normalizedJsonList.add(objectWord);
		}
		*/
		jsonCreator(true);
		JSONObject layerJson = new JSONObject();
		layerJson.put("weight", JsonScores);
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String pretJson = prettyGson.toJson(layerJson);
		String path = "tfidf.txt";
		try(FileWriter outputJson = new FileWriter(path, false)){
			outputJson.write(pretJson.toString());
			outputJson.write("\n\n");
			outputJson.flush();
			outputJson.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("File exported.");
	}
	
	public JSONObject getJson(){
		jsonCreator(false);
		return JsonScores;
	}
	
	public List<JSONObject> getList(){
		return jsonList;
	}
	
	public double getMaxScore(){
		return maxScore;
	}
	
}
