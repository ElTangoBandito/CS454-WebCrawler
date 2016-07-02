package cs454.webCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class WordCounter {

	private List<String> stopWords = Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hasn't", "hadn't", "has", "have", "haven't", "having", "he", "hed", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they're", "they'll", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves");
	private String text;
	private List<String> wordTextList = new ArrayList<String>();
	private List<String> listOfWords = new ArrayList<String>();
	private Set<String> uniqueWords = new HashSet<String>();
	private HashMap<String, Integer> jsonWords = new HashMap<String, Integer>();
	private JSONObject jsonObject = new JSONObject();

	//the input is the html file, it will be parsed again in this class as a plain text. Note** May change input as string instead of File to
	//prevent the same file from being parsed again by Tika. This will save some run time.
	public WordCounter(File html) throws IOException, SAXException, TikaException{
		InputStream input = new FileInputStream(html);
		text = stripper(getText(input));
		this.uniqueWords.addAll(listOfWords);
		wordCount();
	}

	//Removes all html tags, and return the html as plain text
	public String getText(InputStream input) throws IOException, SAXException, TikaException{
		ContentHandler handler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		new HtmlParser().parse(input, handler, metadata, new ParseContext());
		String htmlText = handler.toString();
		return htmlText;
	}
	
	public String stripper(String input){
		String strippedText = input;
		strippedText = strippedText.replaceAll("[^a-zA-Z0-9 ']", " "); //removes everything but number and characters in the string except '
		strippedText = strippedText.replaceAll("\\d", ""); //removes all numbers from the string. Note** We may want to keep numbers.
		strippedText = strippedText.trim().replaceAll(" +", " "); //gets rid of excessive spaces.
		strippedText = strippedText.toLowerCase();
		
		//save a copy of stripped text into an arraylist. This arraylist will be used to count the position of the words.
		List<String> tempWords = new LinkedList<String>(Arrays.asList(strippedText.split(" ")));
		wordTextList.addAll(tempWords);
		
		//this stores every word into a list, then removes all stopwords.
		List<String> words = new LinkedList<String>(Arrays.asList(strippedText.split(" ")));
		words.removeAll(stopWords);
		listOfWords.addAll(words);
		
		return strippedText;
	}
	//Note** punctuation will be removed as well. Words like she's will become shes
	
	@SuppressWarnings("unchecked")
	public void wordCount(){
		for(String unique: uniqueWords){
			JSONArray positionList = new JSONArray();
			int counter = 0;
			
			for(int i = 0; i < wordTextList.size(); i++){
				String word = wordTextList.get(i);
				if (unique.equals(word)){
					counter++;
					positionList.add(Integer.toString(i + 1));
				}
			}
			 if (counter != 0){
				 JSONObject elements = new JSONObject();
				 elements.put("frequency", counter);
				 elements.put("position", positionList);
				 elements.put("word", unique);
				 jsonObject.put(unique, elements);
			 }
			
		}
	}
	
	public HashMap<String, Integer> getMap(){
		return jsonWords;
	}
	
	public String getText(){
		return text;
	}
	
	public List<String> getWords(){
		return listOfWords;
	}
	
	public Set<String> getUniqueWords(){
		return uniqueWords;
	}
	
	public JSONObject getJson(){
		return jsonObject;
	}
}
