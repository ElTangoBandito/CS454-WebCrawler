package cs454.webCrawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SearchGui extends Application{
	private static Map<String, Page> linkMap = new HashMap<String, Page>();
	private static Map<String, HashMap<String, Word>> weightMap = new HashMap<String, HashMap<String, Word>>();
	private static Map<String, String> urlMap = new HashMap<String, String>();
	private static Map<Integer, Hyperlink> hyperMap = new HashMap<Integer, Hyperlink>();
	private static Map<String, Double> pageTotalScore = new HashMap<String, Double>();
	private static List<Hyperlink> hyperLinks = new ArrayList<Hyperlink>();
	private static Map<String, String> pageScoreString = new HashMap<String, String>();
	private static Map<String, int[]> positionMap = new HashMap<String, int[]>();
	private static double tmax = 0;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane bp = new BorderPane();
		HBox header = new HBox();
		final HBox centerScreen = new HBox();
		final VBox vp = new VBox();
		ScrollPane scrollPane = new ScrollPane(bp);
		scrollPane.setFitToHeight(true);
		Scene sc = new Scene(scrollPane);
		sc.getStylesheets().add("styles/style.css");
		final TextField text = new TextField();
		Label enterText = new Label("What are you looking for?:");
		Button submit = new Button("Submit");
		Label heading = new Label("Elgoog Enigne Search");
		Label yourText = new Label("");
		final Label response = new Label("");
		final Label response2 = new Label("");
		vp.getChildren().add(yourText);
		centerScreen.getChildren().add(enterText);
		centerScreen.getChildren().add(text);
		centerScreen.getChildren().add(submit);
		header.getChildren().add(heading);
		heading.getStyleClass().add("label2");
		enterText.getStyleClass().add("label3");
		submit.getStyleClass().add("button");//this actually doesn't do anything! Padding doesn't work on buttons?
		yourText.getStyleClass().add("label4");
		response.getStyleClass().add("label5");
		response2.getStyleClass().add("label5");
		header.getStyleClass().add("hb");
		vp.getStyleClass().add("hb");
		bp.setTop(header);
		bp.setCenter(centerScreen);
		bp.setBottom(vp);
		
		for(int i = 0; i < 30; i++){
			final Hyperlink link = new Hyperlink();
			vp.getChildren().add(link);
			hyperMap.put(i, link);
		}
		vp.getChildren().add(response);
		vp.getChildren().add(response2);
		try{
			BufferedReader br = new BufferedReader(new FileReader("scorejson.txt"));
			Gson gson = new GsonBuilder().create();
			ScoreInfo scoreInfo = gson.fromJson(br, ScoreInfo.class);
			linkMap = scoreInfo.getLink();
			tmax = scoreInfo.getTmax();
			
			BufferedReader br2 = new BufferedReader(new FileReader("tfidf.txt"));
			Gson gson2 = new GsonBuilder().create();
			Weight weight = gson2.fromJson(br2, Weight.class);
			weightMap = weight.getWeight();
			
			Properties properties = new Properties();
			properties.load(new FileInputStream("map.properties"));
			for (String key : properties.stringPropertyNames()) {
				   urlMap.put(key, properties.get(key).toString());
				}

		} catch (Exception e){
			e.printStackTrace();
		}
		
		submit.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>(){
			@Override
			public void handle(Event event) {
				for (Hyperlink clearThis: hyperLinks){
					clearThis.setText("");
				}
				hyperLinks.clear();
				pageTotalScore.clear();
				pageScoreString.clear();
				String userText = text.getText().toLowerCase();
			
				
				
				List<String> userWords = new LinkedList<String>(Arrays.asList(userText.split(" ")));
				if (userWords.size() > 1){
					if(userWords.contains("or") && !userWords.get(0).equals("or") && !userWords.get(userWords.size() - 1).equals("or")){
						for (String word: userWords){
							
							if (!word.equals("or")){
								getScore(word);
							}
						}
					}
					else if(userWords.contains("and") && !userWords.get(0).equals("and") && !userWords.get(userWords.size() - 1).equals("and") && userWords.size() > 2){
						
						while (userWords.contains("and")){
							userWords.remove("and");
						}
						andScore(userWords);
					}
					else if(userText.startsWith("\"") && userText.endsWith("\"")){
						String plainText = userText.replace("\"", "");
						List<String> plainWords = new LinkedList<String>(Arrays.asList(plainText.split(" ")));
						absoluteScore(plainWords);
					}
					else{
						while (userWords.contains("or") || userWords.contains("and")){
							userWords.remove("or");
							userWords.remove("and");
						}
						if (userWords.size() == 1){
							for (String word: userWords){
								getScore(word);
							}
						}
						else {
							proxScore(userWords);
						}
					}
				}
				else {
					for (String word: userWords){
						getScore(word);
					}
				}
				
				addPageRank();
				List<String> allUrl = sortMap();
				for (int i = 0; i < allUrl.size(); i++){
					if (i == 5){
						break;
					}
					else{
					final Hyperlink link = hyperMap.get(i);
					hyperLinks.add(link);
						link.setText(allUrl.get(i));
						link.setOnAction(new EventHandler<ActionEvent>(){
							public void handle(ActionEvent t){
								getHostServices().showDocument(link.getText());
							}
						});
					}
					
					
				}
				
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < allUrl.size(); i++){
					if (i == 5){
						break;
					}
					else{

						sb.append(hyperMap.get(i).getText());
						sb.append(": ");
						sb.append(pageScoreString.get(allUrl.get(i)));
						sb.append("\n");
					}
					
					
				}

				response.setText("All Document Scores");
				response2.setText(sb.toString());
				
				
			}
		});
		
		primaryStage.setScene(sc);
		primaryStage.show();
	}
	public static void main(String[] args){
		Application.launch(args);
	}

	public List<String> sortMap(){
		List<String> urlOrder = new ArrayList<String>();
		Set<Entry<String, Double>> set = pageTotalScore.entrySet();
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
				{
					public int compare(Map.Entry<String, Double> value1, Map.Entry<String, Double> value2)
					{
						return (value2.getValue()).compareTo(value1.getValue());
					}
				}
				);
		for(Map.Entry<String, Double> entry:list){
			urlOrder.add(entry.getKey());
		}
		return urlOrder;
	}
	
	public void getScore(String query){
		for (Entry<String, HashMap<String, Word>> entry : weightMap.entrySet()){
			HashMap<String, Word> listOfWords = entry.getValue();
			if (listOfWords.containsKey(query) && listOfWords.get(query) != null){
				Word currentWord = listOfWords.get(query);
				double tfidf = (currentWord.getTfidf()/tmax);
				String currentUrl = urlMap.get(currentWord.getDocument());
				Page currentPage = linkMap.get(currentUrl);
				double pageScore = (currentPage.getScore());
				if (pageTotalScore.get(currentUrl) != null){
					double currentScore = pageTotalScore.get(currentUrl);
					pageTotalScore.put(currentUrl, currentScore + tfidf);
				}
				else {
					pageTotalScore.put(currentUrl, tfidf);
				}
			}
			else{
				continue;
			}
		}
	}
	
	public void andScore(List<String> queryList){
		for (Entry<String, HashMap<String, Word>> entry : weightMap.entrySet()){
			boolean allWordsPresent = true;
			HashMap<String, Word> listOfWords = entry.getValue();
			for (String currentWord: queryList){
				if (!listOfWords.containsKey(currentWord) || listOfWords.get(currentWord) == null){
					allWordsPresent = false;
					break;
				}
			}
			
			if(allWordsPresent){
				for (String currentWord: queryList){
					getScore(currentWord);
				}
			}
		}
	}
	
	public void proxScore (List<String> queryList){
		for (Entry<String, HashMap<String, Word>> entry : weightMap.entrySet()){
			boolean allWordsPresent = true;
			HashMap<String, Word> listOfWords = entry.getValue();
			String currentUrl = urlMap.get(entry.getKey());
			for (String currentWord: queryList){
				if (!listOfWords.containsKey(currentWord) || listOfWords.get(currentWord) == null){
					allWordsPresent = false;
				}
			}
			if(allWordsPresent){
				for (String currentWord: queryList){
					positionMap.put(currentWord, listOfWords.get(currentWord).getPosition());
				}
				for (int i = 1; i < queryList.size(); i++){
					int[] list1 = positionMap.get(queryList.get(i - 1));
					int[] list2 = positionMap.get(queryList.get(i));
					
					double tfidf = entry.getValue().get(queryList.get(i)).getTfidf() + entry.getValue().get(queryList.get(i - 1)).getTfidf();
					tfidf = tfidf/tmax;
					
					boolean prox1 = false;
					boolean prox2 = false;
					boolean prox3 = false;
					boolean prox4 = false;
					
					for (int positionNumber: list1){
						for (int pn: list2){
							if (positionNumber == pn + 1 || positionNumber == pn - 1){
								prox1 = true;
							}
							else if (positionNumber == pn + 2 || positionNumber == pn - 2){
								tfidf = tfidf/2;
								prox2 = true;
							}
							
							else if (positionNumber == pn + 3 || positionNumber == pn - 3){
								tfidf = tfidf/3;
								prox3 = true;
							}
							
							else if (positionNumber == pn + 4 || positionNumber == pn - 4){
								tfidf = tfidf/4;
								prox4 = true;
							}
							else{
							}
							
						}
						
					}
					
					if(!prox1 && !prox2 && !prox3 && !prox4){
						tfidf = 0;
					}
					if (prox4){
						tfidf = tfidf/4;
					}
					if (prox3){
						tfidf = tfidf/3;
					}
					if (prox2){
						tfidf = tfidf/2;
					}
					if (prox1){
						tfidf = tfidf/1;
					}
					if (tfidf != 0){
						if (pageTotalScore.get(currentUrl) != null){
							double currentScore = pageTotalScore.get(currentUrl);
							pageTotalScore.put(currentUrl, currentScore + tfidf);
						}
						else {
							pageTotalScore.put(currentUrl, tfidf);
						}
					}
				}
			}
			positionMap.clear();
		}
	}
	
	public void absoluteScore(List<String> queryList){
		for (Entry<String, HashMap<String, Word>> entry : weightMap.entrySet()){
			boolean allWordsPresent = true;
			HashMap<String, Word> listOfWords = entry.getValue();
			String currentUrl = urlMap.get(entry.getKey());
			for (String currentWord: queryList){
				if (!listOfWords.containsKey(currentWord) || listOfWords.get(currentWord) == null){
					allWordsPresent = false;
				}
			}
			boolean proxFinal = false;
			
			if(allWordsPresent){
				for (String currentWord: queryList){
					positionMap.put(currentWord, listOfWords.get(currentWord).getPosition());
				}
				for (int i = 1; i < queryList.size(); i++){
					int[] list1 = positionMap.get(queryList.get(i - 1));
					int[] list2 = positionMap.get(queryList.get(i));
					
					boolean prox = false;
					
					for (int positionNumber: list1){
						for (int pn: list2){
							if (positionNumber == pn - 1){
								prox = true;
								break;
							}
							
						}
						if(prox){
							break;
						}
					}
					if(!prox){
						break;
					}
					else{
						proxFinal = true;
					}
				}
				
				if(proxFinal){
					double tfidf = 0.0;
					for (String currentWord: queryList){
						tfidf += (entry.getValue().get(currentWord).getTfidf())/tmax;
					}
					pageTotalScore.put(currentUrl, tfidf);
				}
			}
			positionMap.clear();
		}
	}
	
	public void addPageRank(){
		if (pageTotalScore.size() > 1){
			for (Entry<String, Double> entry : pageTotalScore.entrySet()){
				double weightedTfidf = entry.getValue()*0.6;
				double weightedPr = linkMap.get(entry.getKey()).getScore()*0.4;
				double total = weightedTfidf + weightedPr;
				StringBuilder scores = new StringBuilder();
				scores.append("TFIDF: ");
				scores.append(entry.getValue());
				scores.append(" PageRank: ");
				scores.append(linkMap.get(entry.getKey()).getScore());
				scores.append(" Weighted Score: " + total);
				pageScoreString.put(entry.getKey(), scores.toString());
				pageTotalScore.put(entry.getKey(), total);
			}
		}
	}
}
