package cs454.webCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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

public class ScoreMerger {

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
	int fileCounter = 0;
	JSONObject finalObj = new JSONObject();
	JSONObject innerObj = new JSONObject();
	for (File f: htmlFiles){
		WordCounter word = new WordCounter(f);
		innerObj.put(f.getName(), word.getJson());
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
	for (String key : properties.stringPropertyNames()) {
		for (File thisFile: htmlFiles){
			if (thisFile.getName().equals(key)){;
				urlMap.put(key, properties.get(key).toString());
			}
		}
	}
	
	Properties properties2 = new Properties();
	for (Map.Entry<String, String> entry : urlMap.entrySet()){
		properties2.put(entry.getKey(), entry.getValue());
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
}}
