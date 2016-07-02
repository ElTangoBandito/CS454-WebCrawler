package cs454.webCrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.sax.Link;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CrawlerFileOperation {

	private static final int BUFFER_SIZE = 4096;
	static JSONArray jsonArray = new JSONArray();

	@SuppressWarnings("unused")
	public static boolean fileDownloader(String fileURL, String saveDir)
			throws IOException {
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		int responseCode = httpConn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileType = "";
			String fileName = "";
			String saveFilePath = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			String contentType = httpConn.getContentType();
			int contentLength = httpConn.getContentLength();

			if (disposition != null) {
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 10,
							disposition.length() - 1);
				}
			} else {
				saveFilePath = saveDir;
			}

			InputStream inputStream = httpConn.getInputStream();
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");
			httpConn.disconnect();
			return true;
		} else {
			System.out
					.println("HTTP code: "
							+ responseCode);
			httpConn.disconnect();
			return false;
		}

	}

	@SuppressWarnings({ "unused", "unchecked" })
	public static void store(Map<String, Object> metadata, File file1) {

		FileWriter fstream;
		BufferedWriter out;

		try {
			Map<String, Object> metadata1 = new HashMap<String, Object>();

			metadata1.put("title", metadata.get("title"));
			metadata1.put("type", metadata.get("type"));
			metadata1.put("met", metadata.get("metadata"));
			metadata1.put("url", metadata.get("url"));
			metadata1.put("crawled date", metadata.get("crawled date"));
			metadata1.put("localDirLocation", metadata.get("localDirLocation"));

			List<Link> links = (List<Link>) metadata.get("links");
			Map<Integer, Object> linksToPut = new HashMap<Integer, Object>();

			/* ArrayList<String> linksToPut = new ArrayList<String>(); */
			int i = 0;

			for (Link l : links) {
				i++;
				JSONObject linkObj = new JSONObject();

				linkObj.put("Link Text", l.getText());
				linkObj.put("Target URL", l.getUri());

				Gson prettyGson1 = new GsonBuilder().setPrettyPrinting()
						.create();
				Gson uglyJson1 = new Gson();
				String pretJson1 = prettyGson1.toJson(linkObj);

				linksToPut.put(i, pretJson1);
			}

			metadata1.put("links", linksToPut);

			JSONObject obj = new JSONObject();

			for (Map.Entry<String, Object> entry : metadata1.entrySet()) {
				obj.put(entry.getKey(), entry.getValue());
			}
			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
			Gson uglyJson = new Gson();
			String pretJson = prettyGson.toJson(obj);

			jsonArray.add(obj);
		} catch (Exception e) {

		}
	}

	public static void saveContent(File file1) {
		try {
			String path = file1.getAbsolutePath();

			Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

			String pretJson = prettyGson.toJson(jsonArray);

			FileWriter file = new FileWriter(path, true);
			file.write(pretJson.toString());
			file.write("\n\n");
			file.flush();
			file.close();

			System.out.println("Done");
		}

		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean saveHTMLContent(ToHTMLContentHandler toHTMLHandler,
			File file) {

		String path = file.getAbsolutePath();

		try {
			FileWriter file1 = new FileWriter(path);

			file1.write(toHTMLHandler.toString());
			file1.write("\n\n");
			file1.flush();
			file1.close();
			return true;
		} catch (IOException e) {
			System.out.println("saving content failed");
			return false;
		}
	}
}
