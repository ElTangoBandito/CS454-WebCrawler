package cs454.webCrawler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class WebCrawlerMain {
	
		public static void main( String[] args )
	    {
	    		try
	    		{
	    			if ( args.length > 1 && args[0] != null && args[1] != null)
	    			{
	    				new WebCrawler().crawl(Integer.valueOf(args[0]), args[1]);
	    			}else
	    			{
	    				System.out.println("Invalid Parameter! Usage: WebCrawler <depth_size> <http://www.xyz.com>");
	    				System.exit(0);
	    			}
	    			
	    			//new Crawler1().crawl(args[3].trim(),Integer.parseInt(args[1]));
	    		}
	    		catch (Exception e)
	    		{
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    	
	    	CrawlerFileOperation.saveContent(WebCrawler.file1);
	    	Properties properties = new Properties();
	    	Map<String, String> urlMap = WebCrawler.urlMap;
	    	for (Map.Entry<String, String> entry : urlMap.entrySet()){
	    		properties.put(entry.getKey(), entry.getValue());
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
	    }
}
