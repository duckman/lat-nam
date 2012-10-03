import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import org.jsoup.Jsoup;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import org.apache.commons.io.IOUtils;
import javax.servlet.annotation.WebListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

@WebListener
public class Data implements ServletContextListener,Runnable
{
	private static Data theData;
	private static Thread worker;
	private static boolean go;
	private static Queue<Integer> jobs;
	private static Mongo mongo;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		theData = this;
		jobs = new LinkedBlockingQueue<Integer>();
		jobs.add(1);
		jobs.add(2);
		jobs.add(3);
		jobs.add(4);
		jobs.add(5);
		try
		{
			mongo = new Mongo();
			worker = new Thread(this);
			worker.start();
		}
		catch(UnknownHostException ex)
		{
			ex.printStackTrace(System.err);
		}
	}

	@Override
	public void run()
	{
		DB db = mongo.getDB("mtg");
		DBCollection coll = db.getCollection("cards");
		go = true;
		while(go)
		{
			if(jobs.size()>0)
			{
				try
				{
					int multiverseid = jobs.poll();
					DBObject card = coll.findOne(new BasicDBObject("multiverseid",multiverseid));
					if(card == null)
					{
						coll.insert(fetchRemoteCard(multiverseid));
					}
				}
				catch(IOException ex)
				{
					ex.printStackTrace(System.err);
				}
			}

			try
			{
				Thread.sleep(5000);
			}
			catch(InterruptedException ex)
			{
				ex.printStackTrace(System.err);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		go = false;
		worker = null;
	}

	public static Data getInstance()
	{
		return theData;
	}

	private static BasicDBObject fetchRemoteCard(int multiverseID) throws IOException
	{
		URL in = new URL("http://gatherer.wizards.com/Pages/Card/Details.aspx?printed=true&multiverseid="+multiverseID);
		StringWriter writer = new StringWriter();
		IOUtils.copy(in.openStream(), writer);
		Document dom = Jsoup.parse(writer.toString());
		BasicDBObject doc = null;

		if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_nameRow").size()>0)
		{
			doc = new BasicDBObject();

			doc.put("multiverseid",multiverseID);
			doc.put("name",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_nameRow > .value").first().text().trim());
			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_manaRow").size()>0)
			{
				doc.put("cost",getManaCost(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_manaRow > .value > img")));
			}
			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cmcRow").size()>0)
			{
				doc.put("cmc",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cmcRow > .value").first().text().trim());
			}
			doc.put("type",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_typeRow > .value").first().text().trim());
			replaceImages(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_textRow > .value > .cardtextbox > img"));
			BasicDBList text = new BasicDBList();
			for(Element para:dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_textRow > .value > .cardtextbox"))
			{
				text.add(para.text().trim());
			}
			doc.put("text",text);
			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_flavorRow").size()>0)
			{
				text = new BasicDBList();
				for(Element para:dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_flavorRow > .value > .cardtextbox"))
				{
					text.add(para.text().trim());
				}
				doc.put("flavor",text);
			}
			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ptRow").size()>0)
			{
				doc.put("pt",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ptRow > .value").first().text().trim());
			}
			doc.put("expansion",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_setRow > .value").first().text().trim());
			doc.put("rarity",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rarityRow > .value").first().text().trim());
			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_numberRow").size()>0)
			{
				doc.put("number",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_numberRow > .value").first().text().trim());
			}
			doc.put("artist",dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_artistRow > .value").first().text().trim());
		}

		return doc;
	}

	private static String getManaCost(Elements colors)
	{
		HashMap<String,Integer> table = new HashMap<String,Integer>();
		for(Element color:colors)
		{
			if(Pattern.matches("\\d",color.attr("alt")))
			{
				table.put("Colorless",new Integer(color.attr("alt")));
			}
			else
			{
				if(table.containsKey(color.attr("alt")))
				{
					table.put(color.attr("alt"),table.get(color.attr("alt"))+1);
				}
				else
				{
					table.put(color.attr("alt"),1);
				}
			}
		}
		String result = "";
		for(String key:table.keySet())
		{
			result += ""+key.charAt(0)+""+table.get(key);
		}
		return result;
	}

	private static void replaceImages(Elements images)
	{
		for(Element img:images)
		{
			TextNode text = new TextNode("***"+img.attr("alt")+"***","");
			img.replaceWith(text);
		}
	}
}
