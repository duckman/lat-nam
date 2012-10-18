import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.PrintWriter;
import java.io.IOException;
import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

@WebServlet(name="Find",urlPatterns={"/Find"})
public class Find extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("application/json; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		try
		{
			DBObject query = new BasicDBObject();
			DBObject fields = new BasicDBObject();
			String sort = null;
			boolean asc = true;
			int limit = 0;
			int skip = 0;

			if(req.getParameter("query")!=null)
			{
				query = (DBObject)JSON.parse(req.getParameter("query"));
			}
			else
			{
				if(req.getParameter("multiverseid")!=null)
				{
					query.put("multiverseid",Integer.parseInt(req.getParameter("multiverseid")));
				}
				if(req.getParameter("cmc")!=null)
				{
					query.put("cmc",Integer.parseInt(req.getParameter("cmc")));
				}
				if(req.getParameter("expension")!=null)
				{
					query.put("expension",req.getParameter("expension"));
				}
				if(req.getParameter("name")!=null)
				{
					query.put("name",req.getParameter("name"));
				}
				if(req.getParameter("artist")!=null)
				{
					query.put("artist",req.getParameter("artist"));
				}
				if(req.getParameter("rarity")!=null)
				{
					query.put("rarity",req.getParameter("artist"));
				}
			}

			if(req.getParameter("fields")!=null)
			{
				fields = (DBObject)JSON.parse(req.getParameter("fields"));
			}

			if(req.getParameter("sort")!=null)
			{
				sort = req.getParameter("sort");
			}

			if(req.getParameter("asc")!=null)
			{
				asc = Boolean.parseBoolean(req.getParameter("asc"));
			}

			if(req.getParameter("limit")!=null)
			{
				limit = Integer.parseInt(req.getParameter("limit"));
			}

			if(req.getParameter("skip")!=null)
			{
				skip = Integer.parseInt(req.getParameter("skip"));
			}

			JSONArray result = new JSONArray();
			DBCursor cur = find(query,fields,sort,asc,limit,skip);
			for(DBObject card:cur)
			{
				result.put(new JSONObject(card.toString()));
			}
			out.println(result.toString());
		}
		catch(JSONException ex)
		{
			out.println("{\"error\":\"This should never actually happen...\"}");
		}
		catch(JSONParseException ex)
		{
			out.println("{\"error\":\"Invalid Query\"}");
		}
	}

	public DBCursor find(DBObject query,DBObject fields,String sort,boolean asc,int limit,int skip)
	{
		DBCursor cur = Data.getMongodb().getDB("mtg").getCollection("cards").find(query,fields);

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

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		doGet(req, resp);
	}
}
