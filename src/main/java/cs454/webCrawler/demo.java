package cs454.webCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tika.exception.TikaException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class demo {
	static JSONArray jsonArray = new JSONArray();
	static List<File> allHtmlFiles = new ArrayList<File>();
	static Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();
	static Map<String, Word> wordMap = new HashMap<String, Word>();
	@SuppressWarnings("unchecked")
	public static void main( String[] args ) throws IOException, SAXException, TikaException, ParseException, JSONException
    {			
		File file = new File("content/files");//folder path
		File[] htmlFiles = file.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith("html");
			}
		});
		
		
		//crawl(filePath);
		
		int fileCounter = 0;
		JSONObject finalObj = new JSONObject();
		JSONObject innerObj = new JSONObject();
		for (File f: htmlFiles){
			WordCounter word = new WordCounter(f);
			innerObj.put(f.getName(), word.getJson());
			//jsonMap.put(f.getName(), word.getJson());
			fileCounter++;
		}
		finalObj.put("completeMap", innerObj);
		System.out.println(fileCounter);
		
		
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String pretJson = prettyGson.toJson(finalObj);
		String path = "words.txt";
		try(FileWriter outputJson = new FileWriter(path, false)){
			outputJson.write(pretJson.toString());
			outputJson.write("\n\n");
			outputJson.flush();
			outputJson.close();
		}
		System.out.println("File output successful");
		
		
		WeightCalculator wc = new WeightCalculator(path, fileCounter);
		wc.exportJson();
		
		Map<String, String> urlMap = new HashMap<String, String>();
		Properties properties = new Properties();
		properties.load(new FileInputStream("map.properties"));
		int remover = 0;
		for (String key : properties.stringPropertyNames()) {
			for (File thisFile: htmlFiles){
				if (thisFile.getName().equals(key)){;
					urlMap.put(key, properties.get(key).toString());
				}
			}
		}
		
		Properties properties2 = new Properties();
    	for (Map.Entry<String, String> entry : urlMap.entrySet()){
    		properties.put(entry.getKey(), entry.getValue());
    		System.out.println(entry.getValue());
    	}
    	try {
			properties.store(new FileOutputStream("map.properties"), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(fileCounter + " " + urlMap.size());
    	String directoryPath = "content/files";
    	
		LinkAnalysis la = new LinkAnalysis(directoryPath, (HashMap<String, String>) urlMap);
		JSONObject linkJson = la.getJson();
		JSONObject merged = new JSONObject();
		merged.put("tmax", wc.getMaxScore());
		merged.put("link", linkJson);
		
		Gson prettyGson2 = new GsonBuilder().setPrettyPrinting().create();
		String pretJson2 = prettyGson2.toJson(merged);
		String outPath = "scorejson.txt";
		try(FileWriter outputJson = new FileWriter(outPath, false)){
			outputJson.write(pretJson2.toString());
			outputJson.write("\n\n");
			outputJson.flush();
			outputJson.close();
		}
		
		
		/*
		//in roder to test link analysis, you need to supply the mapping of document name with the document's URL. (WebAdress)
		//program will receive an error if the size of the urlMap doesn't match the number of documents in the directory folder.
		Map<String, String> urlMap = new HashMap<String, String>();
		//urlMap.put("uuid1.html", "http://www.pandora.com");
		//urlMap.put("uuid2.html", "http://www.pandora.com/about");
		//urlMap.put("uuid3.html", "http://www.pandora.com/one/gift");
		//urlMap.put("uuid4.html", "http://www.pandora.com/legal");
		
		Properties properties = new Properties();
		properties.load(new FileInputStream("map.properties"));
		int remover = 0;
		for (String key : properties.stringPropertyNames()) {
			//if (remover == 0){
			//	remover++;
			//	continue;
			//}
			//else{
				urlMap.put(key, properties.get(key).toString());
				//}
			}
		
		JSONObject merged = new JSONObject();
		merged.put("weight", objTemp);
		merged.put("link", linkJson);
		
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String pretJson = prettyGson.toJson(objTemp);
		String path = "C:/Users/Volfurious/Desktop/json/weightScores.txt";
		try(FileWriter outputJson = new FileWriter(path, true)){
			outputJson.write(pretJson.toString());
			outputJson.write("\n\n");
			outputJson.flush();
			outputJson.close();
		}
		
		
		Properties properties = new Properties();

		for (Map.Entry<String,String> entry : urlMap.entrySet()) {
		    properties.put(entry.getKey(), entry.getValue());
		}

		properties.store(new FileOutputStream("map.properties"), null);
		*/
		/*
		for (@SuppressWarnings("rawtypes")
		Iterator iterator = linkJson.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			JSONObject innerObj = (JSONObject) linkJson.get(key);
			String documentName = (String) innerObj.get("document");
			//System.out.println(objTemp.get(documentName));
			//innerObj.put("words", innerObj.get("document"));
			//linkJson.put(linkJson.get(key), innerObj);
		}*/
		
		//System.out.println(la.getInMap().toString());
		//System.out.println(la.getOutMap().toString());
		/*
		 * 
		 * NOTE: BUG
		 * if wc.getNormalizedJson is called after wc.getJson, the json object called from wc.getJson will be normalized!!
		 * not sure why this is happening, unable to fix it.
		 * if regular json file is needed, only call the method wc.getJson, do not call wc.getNormalizedJson before or after under any circumstances.
		System.out.println(objTemp.get("Test3.html"));
		JSONObject objTemp2 = wc.getNormalizedJson();
		System.out.println(objTemp.get("Test3.html"));
		
		//System.out.println(objTemp2.get("Test3.html"));
		//System.out.println(wc.getMax());
		
		Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
		String pretJson = prettyGson.toJson(word.getJson());
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(pretJson);
		JSONObject testObj = (JSONObject) obj;
		*/
		
		//Gradle crawler -Parguments="['http://www.samsung.com/']" -Duser.dir="c:\crawler3"
    }
	
	public static void crawl(String path){
		File location = new File(path);
		File[] allFiles = location.listFiles();
		if (allFiles.length == 0){
			return;
		}
		for (File file : allFiles){
			if (file.isDirectory()){
				crawl(file.getAbsolutePath());
			}
			else {
				allHtmlFiles.add(file);
			}
		}
	}
	public void readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		reader.beginArray();
		reader.endArray();
		reader.close();
		
	}
}
