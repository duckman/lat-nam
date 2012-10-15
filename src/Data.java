import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.net.UnknownHostException;
import org.jsoup.Jsoup;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.concurrent.LinkedBlockingQueue;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import javax.servlet.annotation.WebListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@WebListener
public class Data implements ServletContextListener,Runnable
{
	private static Data theData;
	private static Thread worker;
	private static boolean go;
	private static LinkedBlockingQueue<Integer> cardJobs;
	private static LinkedBlockingQueue<Integer> langJobs;
	private static Mongo mongo;
	private static DB db;
	private static DBCollection coll;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		theData = this;
		try
		{
			File file = new File("cardJobs.object");
			if(file.exists())
			{
				try
				{
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
					cardJobs = (LinkedBlockingQueue<Integer>)in.readObject();
					in.close();
					file.delete();
				}
				catch(IOException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
					cardJobs = new LinkedBlockingQueue<Integer>();
				}
				catch(ClassNotFoundException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
					cardJobs = new LinkedBlockingQueue<Integer>();
				}
			}
			else
			{
				cardJobs = new LinkedBlockingQueue<Integer>();
			}
			file = new File("langJobs.object");
			if(file.exists())
			{
				try
				{
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
					langJobs = (LinkedBlockingQueue<Integer>)in.readObject();
					in.close();
					file.delete();
				}
				catch(IOException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
					langJobs = new LinkedBlockingQueue<Integer>();
				}
				catch(ClassNotFoundException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
					langJobs = new LinkedBlockingQueue<Integer>();
				}
			}
			else
			{
				langJobs = new LinkedBlockingQueue<Integer>();
			}

			mongo = new Mongo();
			db = mongo.getDB("mtg");
			coll = db.getCollection("cards");
			worker = new Thread(this);
			worker.start();
		}
		catch(UnknownHostException ex)
		{
			LogFactory.getLog(Data.class).error(null,ex);
		}
	}

	private void allLanguages()
	{
		try
		{
			for(DBObject card:coll.find())
			{
				//LogFactory.getLog(Data.class).info(card+":"+card.get("multiverseid"));
				langJobs.offer(Integer.parseInt(card.get("multiverseid").toString()));
			}
		}
		catch(Exception ex)
		{
			LogFactory.getLog(Data.class).error(null,ex);
		}
	}

	public void reIndexWords()
	{
		com.mongodb.DBCursor curs = coll.find();

		DBObject current;
		while(curs.hasNext())
		{
			current = curs.next();
			current = insertWords(current);
			coll.save(current);
		}
	}

	private DBObject insertWords(DBObject card)
	{
		String[] words = null;
		words = ArrayUtils.addAll(words,getWords(card.get("name")));
		words = ArrayUtils.addAll(words,getWords(card.get("text")));
		words = ArrayUtils.addAll(words,getWords(card.get("flavor")));
		words = ArrayUtils.addAll(words,getWords(card.get("type")));
		words = ArrayUtils.addAll(words,getWords(card.get("expansion")));
		words = ArrayUtils.addAll(words,getWords(card.get("cost")));
		ArrayList trimmed = new ArrayList();
		for(int x=0;x<words.length;x++)
		{
			words[x] = words[x].replaceAll("^\\W+|\\W+$","");
			if(words[x].length()>0 && !trimmed.contains(words[x].toLowerCase()))
			{
				trimmed.add(words[x].toLowerCase());
			}
		}
		card.put("_words",trimmed);
		return card;
	}

	private String[] getWords(Object sentance)
	{
		if(sentance!=null)
		{
			return sentance.toString().split("\\s|><");
		}
		return null;
	}

	@Override
	public void run()
	{
		//allLanguages();
		go = true;
		while(go)
		{
			if(cardJobs.size()>0)
			{
				try
				{
					int multiverseid = cardJobs.poll();
					DBObject card = coll.findOne(new BasicDBObject("multiverseid",multiverseid));
					if(card == null)
					{
						for(DBObject newCard:fetchRemoteCard(multiverseid))
						{
							newCard = insertWords(newCard);
							coll.insert(newCard);
							langJobs.offer(Integer.parseInt(newCard.get("multiverseid").toString()));
						}
						try
						{
							Thread.sleep(1000);
						}
						catch(InterruptedException ex)
						{
							LogFactory.getLog(Data.class).error(null,ex);
						}
					}
				}
				catch(IOException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
				}
			}
			else if(langJobs.size()>0)
			{
				findLanguages(langJobs.poll());
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
				}
			}
			else
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException ex)
				{
					LogFactory.getLog(Data.class).error(null,ex);
				}
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		File file;
		if(cardJobs.size()>0)
		{
			try
			{
				file = new File("cardJobs.object");
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(cardJobs);
			}
			catch(IOException ex)
			{
				LogFactory.getLog(Data.class).error(null,ex);
			}
		}
		if(langJobs.size()>0)
		{
			try
			{
				file = new File("langJobs.object");
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(langJobs);
			}
			catch(IOException ex)
			{
				LogFactory.getLog(Data.class).error(null,ex);
			}
		}
		go = false;
		worker = null;
		mongo.close();
		mongo = null;
	}

	public static Data getInstance()
	{
		return theData;
	}

	public static ArrayList<DBObject> fetchRemoteCard(int multiverseID) throws IOException
	{
		URL in = new URL("http://gatherer.wizards.com/Pages/Card/Details.aspx?printed=true&multiverseid="+multiverseID);
		Document dom = Jsoup.parse(in.openStream(),null,"http://gatherer.wizards.com/");
		ArrayList<DBObject> docs = new ArrayList<DBObject>();

		if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_nameRow").size()>0)
		{
			DBObject doc = parseCard(dom, "ctl00_ctl00_ctl00_MainContent_SubContent_SubContent");
			doc.put("multiverseid",multiverseID);
			docs.add(doc);
		}
		if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl05_nameRow").size()>0)
		{
			DBObject doc1 = parseCard(dom, "ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl05");
			DBObject doc2 = null;

			doc1.put("multiverseid",Integer.parseInt(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl05_cardImage").first().attr("src").split("[=&]")[1]));

			if(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl06_nameRow").size()>0)
			{
				doc2 = parseCard(dom, "ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl06");
				doc2.put("multiverseid",Integer.parseInt(dom.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl06_cardImage").first().attr("src").split("[=&]")[1]));
				doc2.put("other",doc1.get("multiverseid"));
				doc1.put("other",doc2.get("multiverseid"));
				docs.add(doc2);
			}

			docs.add(doc1);
		}

		return docs;
	}

	private static DBObject parseCard(Document dom, String prefix)
	{
		BasicDBObject doc = new BasicDBObject();
		
		// name
		doc.put("name",dom.select("#"+prefix+"_nameRow > .value").first().text().trim());
		// mana
		if(dom.select("#"+prefix+"_manaRow").size()>0)
		{
			replaceImages(dom.select("#"+prefix+"_manaRow > .value > img"));
			doc.put("cost",dom.select("#"+prefix+"_manaRow > .value").first().text().trim().replaceAll("\\+\\*\\*","<").replaceAll("\\*\\*\\+",">"));
		}
		// cmc
		if(dom.select("#"+prefix+"_cmcRow").size()>0)
		{
			doc.put("cmc",Integer.parseInt(dom.select("#"+prefix+"_cmcRow > .value").first().text().trim()));
		}
		// type
		doc.put("type",dom.select("#"+prefix+"_typeRow > .value").first().text().trim());
		// text
		replaceImages(dom.select("#"+prefix+"_textRow > .value > .cardtextbox > img"));
		String text = "";
		for(Element para:dom.select("#"+prefix+"_textRow > .value > .cardtextbox"))
		{
			if(text.length()>0)
			{
				text+="\n";
			}
			text+=para.text().trim().replaceAll("\\+\\*\\*","<").replaceAll("\\*\\*\\+",">");
		}
		doc.put("text",text);
		// flavor text
		if(dom.select("#"+prefix+"_flavorRow").size()>0)
		{
			text = "";
			for(Element para:dom.select("#"+prefix+"_flavorRow > .value > .cardtextbox"))
			{
				if(text.length()>0)
				{
					text+="\n";
				}
				text+=para.text().trim();
			}
			doc.put("flavor",text);
		}
		// power and toughness
		if(dom.select("#"+prefix+"_ptRow").size()>0)
		{
			doc.put("pt",dom.select("#"+prefix+"_ptRow > .value").first().text().trim());
		}
		// expansion
		doc.put("expansion",dom.select("#"+prefix+"_setRow > .value").first().text().trim());
		// rarity
		doc.put("rarity",dom.select("#"+prefix+"_rarityRow > .value").first().text().trim());
		// number
		if(dom.select("#"+prefix+"_numberRow").size()>0)
		{
			doc.put("number",dom.select("#"+prefix+"_numberRow > .value").first().text().trim());
		}
		// artist
		doc.put("artist",dom.select("#"+prefix+"_artistRow > .value").first().text().trim());

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
			TextNode text = new TextNode("+**"+img.attr("alt")+"**+","");
			img.replaceWith(text);
		}
	}

	public long getCount()
	{
		return coll.count();
	}

	public BasicDBObject getLast()
	{
		BasicDBObject result = new BasicDBObject(coll.find().sort(new BasicDBObject("_id",-1)).limit(1).next().toMap());
		result.remove("_id");
		result.remove("_words");
		return result;
	}

	public void addJob(int id)
	{
		cardJobs.offer(id);
	}

	public void addJobs(int start, int end)
	{
		if(start > end)
		{
			int temp = start;
			start = end;
			end = temp;
		}

		for(int x=start;x<=end;x++)
		{
			cardJobs.offer(x);
		}
	}

	public int getCardJobs()
	{
		return cardJobs.size();
	}

	public int getLangJobs()
	{
		return langJobs.size();
	}

	public DBCursor find(DBObject query,String sort,boolean asc,int limit,int skip)
	{
		BasicDBObject fields = new BasicDBObject("_id",0);
		fields.put("_words",0);

		DBCursor cur = coll.find(query,fields);

		if(sort!=null && sort.length()>0)
		{
			if(asc)
			{
				cur = cur.sort(new BasicDBObject(sort,1));
			}
			else
			{
				cur = cur.sort(new BasicDBObject(sort,-1));
			}
		}

		if(limit>0)
		{
			cur = cur.limit(limit);
		}

		if(skip>0)
		{
			cur = cur.skip(skip);
		}

		return cur;
	}

	public void reverseJobs()
	{
		ArrayList<Integer> temp = new ArrayList<Integer>(cardJobs);
		Collections.reverse(temp);
		cardJobs = new LinkedBlockingQueue<Integer>(temp);
	}

	private void findLanguages(int multiverseID)
	{
		try
		{
			URL in = new URL("http://gatherer.wizards.com/Pages/Card/Languages.aspx?multiverseid="+multiverseID);
			Document dom = Jsoup.parse(in.openStream(),null,"http://gatherer.wizards.com/");
			for(Element card:dom.select(".cardItem"))
			{
				int editID = Integer.parseInt(new URL(card.select("a").first().attr("abs:href")).getQuery().split("=")[1]);
				DBObject edit = coll.findOne(new BasicDBObject("multiverseid",editID));
				if(edit==null)
				{
					for(DBObject newCard:fetchRemoteCard(editID))
					{
						newCard.put("lang",card.child(1).text());
						coll.insert(newCard);
						langJobs.offer(Integer.parseInt(newCard.get("multiverseid").toString()));
					}
				}
				else
				{
					edit.put("lang",card.child(1).text());
					coll.save(edit);
				}
			}
		}
		catch(Exception ex)
		{
			LogFactory.getLog(Data.class).error(null,ex);
		}
	}
}
