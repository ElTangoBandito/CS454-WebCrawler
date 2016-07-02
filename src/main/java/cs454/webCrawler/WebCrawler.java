package cs454.webCrawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class WebCrawler {

	static File file1;
	static Map<String,String> fileTypeMap = new HashMap<String,String>(); 
	static Map<String, String> urlMap = new HashMap<String, String>();
	
	String domain = "";
	String otherDomain = "";
	int crawl_site_indicator=0;
	int other_site_indicator=0;
	int count=0;
	List<String> linkList = new ArrayList<String>();
	int i;

	public void crawl(int depth, String crawlUrl) throws MalformedURLException {
		System.out.println("Crawling the website URL : " + crawlUrl);

		if(crawl_site_indicator==0)
		{
			domain = getDomainName(crawlUrl);
			other_site_indicator=1;
		}

		Tika tika = new Tika();
		tika.setMaxStringLength(10 * 1024 * 1024);
		Metadata met = new Metadata();
		URL url;
		try {
			url = new URL(crawlUrl);
		

		ToHTMLContentHandler toHTMLHandler = new ToHTMLContentHandler();
		ParseContext parseContext = new ParseContext();
		LinkContentHandler linkHandler = new LinkContentHandler();
		ContentHandler textHandler = new BodyContentHandler(10 * 1024 * 1024);
		TeeContentHandler teeHandler = new TeeContentHandler(linkHandler,
				textHandler, toHTMLHandler);

		AutoDetectParser parser = new AutoDetectParser();
		
			parser.parse(url.openStream(), teeHandler, met, parseContext);
		

		parse(crawlUrl);

		List<Link> links = linkHandler.getLinks();
		
		if (i<300) {/*

		if (links != null && links.size() > 0) {
			for (Link l : links) {

				if (l.getUri().startsWith("http")) {
					i++;
					System.out.println("Domain Name =>"
							+ getDomainName(l.getUri()));
					if (getDomainName(l.getUri()).equals(domain)) {
						if (!linkList.contains(l.getUri())) {

							linkList.add(l.getUri());
							crawl(l.getUri());
						}
					}else
						continue;

				}else
					continue;
			}

		} */
			

		    
		    if(links !=null && links.size()>0)
		    {
		    		for(Link l:links)
		    		{
		    			if(l.getUri().startsWith("http"))
		    			{
		    				i++;
		    				if(getDomainName(l.getUri()).equals(domain))
		    				{		    	
		    					if(!linkList.contains(l.getUri()))
		    					{	
		    						
		    						linkList.add(l.getUri());
		    						crawl(depth,l.getUri());
		    					}
		    				}
		    				else
		    				{
		    					if(other_site_indicator==0)
		    					{
		    						otherDomain= getDomainName(l.getUri());
		    						other_site_indicator=1;
		    					}	    					
		    					
		    					if(count<depth)
		    					{
		    						if(getDomainName(l.getUri()).equals(otherDomain))
		    						{
		    							
		    							if(!linkList.contains(l.getUri()))
				    					{		
		    								count++;	    		
		    								linkList.add(l.getUri());
				    						System.out.println("Current Depth: " + count +"\n");
				    						crawl(depth,l.getUri());
				    					}
		    						}
		    					}
		    					else
		    					{
		    						other_site_indicator=0;
		    						count=0;
		    						continue;
		    					}
		    				}
		    			}
		    			else
		    				continue;		    			
		    		}
		    }
		    else
		    	return; 
		    
		
		
		}
		
		} catch (IOException | SAXException | TikaException e) {
			e.printStackTrace();
		}

	}

	private String getDomainName(String URL) {
		String domainName = new String(URL);

		int index = domainName.indexOf("://");

		if (index != -1) {
			domainName = domainName.substring(index + 3);
		}

		index = domainName.indexOf('/');

		if (index != -1) {
			domainName = domainName.substring(0, index);
		}

		domainName = domainName.replaceFirst("^www.*?\\.", "");
		domainName = new StringBuffer(domainName).reverse().toString();
		String[] split = domainName.split("\\.");
		domainName = new StringBuffer(split[0] + "." + split[1]).reverse()
				.toString();
		return domainName;

	}
	
	
	@SuppressWarnings("rawtypes")
	public static Map getFileType()
	{
		fileTypeMap.put("application/vnd.ms-powerpoint",".ppt");
		fileTypeMap.put("application/vnd.xml",".xml");
		fileTypeMap.put("application/vnd.ms-excel",".xlsx");
		fileTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",".doc");
		fileTypeMap.put("image/gif",".gif");
		fileTypeMap.put("image/png",".png");
		fileTypeMap.put("image/jpeg",".jpeg");
		fileTypeMap.put("application/octet-stream",".pdf");
		fileTypeMap.put("application/pdf",".pdf");
		
		return fileTypeMap;
	}
	
	
	@SuppressWarnings({ "deprecation", "unused" })
	public static void parse(String url1)
	{
		
		
		try
		{
			Map<String,Object> metadata = new HashMap<String, Object>();
			
			Tika tika = new Tika();
			tika.setMaxStringLength(10*1024*1024); 
			Metadata met=new Metadata();
			URL url = new URL(url1);
			
			ToHTMLContentHandler toHTMLHandler=new ToHTMLContentHandler();
			ParseContext parseContext=new ParseContext();
			LinkContentHandler linkHandler = new LinkContentHandler();
			ContentHandler textHandler = new BodyContentHandler(10*1024*1024);
			TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, textHandler, toHTMLHandler);		
			
			AutoDetectParser parser=new AutoDetectParser();
		    parser.parse(url.openStream(),teeHandler,met,parseContext);
		    
		    String title = met.get(Metadata.TITLE);
		    String type = met.get(Metadata.CONTENT_TYPE);
		    
		    System.out.println(type);
		    
		    
		    Date date = new Date();
		    
		    UUID uuid = UUID.randomUUID();
		    		    
		    List<Link> links = linkHandler.getLinks();
		    
		    System.out.println("Download URL ==>"+url);
		    
		    metadata.put("title",title);
		    metadata.put("type", type);
		    metadata.put("metadata", met);
		    metadata.put("url", url);
		    metadata.put("crawled date", date);		    
		    metadata.put("links", links);
		    
		    String localpath1 = System.getProperty("user.dir")+"\\content\\json";	
		    File dir1 = new File(localpath1);
		    if(!dir1.exists())
		    {
		    	dir1.mkdirs();
		    }
		    
		    localpath1= localpath1+"\\crawler_result.json";
		    
		    file1 = new File(localpath1);
		    
		    String fileName2 = uuid + ".html";
		    String localpath2 = System.getProperty("user.dir")+"\\content\\files\\"+ fileName2;		    
		    File file2 = new File(localpath2);
		    
		    urlMap.put(fileName2, url1);
		    		    
		    String fileName="";
		    String fileType="";		
		    
		    String localpath = System.getProperty("user.dir")+"\\content\\files\\";
		    File dir2 = new File(localpath);
		    if(!dir2.exists())
		    {
		    	dir2.mkdirs();
		    }
		    
		    if ( getFileType().get(type) != null )
		    {
		    	System.out.println("File Type==>"+getFileType().get(type));
		    	
		    	fileName= uuid + (getFileType().get(type)).toString();
		    	localpath = localpath+ fileName;		    
			    File file = new File(localpath); 
			    
			    metadata.put("localpath", file.getAbsolutePath());
		    	boolean flag = CrawlerFileOperation.fileDownloader(url1, localpath);
		    	if(flag==true)
		    		CrawlerFileOperation.store(metadata,file1);
		    }
		    
		    if(type.contains("text/html") || type.contains("application/xhtml")){
		    	metadata.put("localpath", file2.getAbsolutePath());		    		    	
		    	boolean flag = CrawlerFileOperation.saveHTMLContent(toHTMLHandler,file2);
		    	if(flag==true)
		    		CrawlerFileOperation.store(metadata,file1);	
		    }
		    			
		}
		catch (  MalformedURLException e1) {		    
		    System.out.println("Malformed URl");
		  }
		
		catch (Exception e){
			System.out.println("");
		}
	}

}
