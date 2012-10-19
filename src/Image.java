import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collections;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import org.apache.commons.codec.digest.DigestUtils;
import java.security.DigestInputStream;
import java.security.MessageDigest;

@WebServlet(name="Image",urlPatterns={"/Image"})
public class Image extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			String query = req.getQueryString();
			if(query == null)
			{
				query = "";
				for(String param:Collections.list(req.getParameterNames()))
				{
					if(query.length()>0)
					{
						query+="&";
					}
					query+=param+"="+req.getParameter(param);
				}
			}

			if(query.length()>0)
			{
				DBCollection coll = Data.getMongodb().getDB("mtg").getCollection("images");
				DBObject image = coll.findOne(new BasicDBObject("query",query));
				if(image == null)
				{
					image = new BasicDBObject("query",query);
					URL remote = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?"+query);
					//LogFactory.getLog(Image.class).info(remote.toString());
					image.put("hash",DigestUtils.md5Hex(remote.openStream()));
					coll.insert(image);
					File source = new File("images/"+image.get("hash")+".jpeg");
					if(!source.exists())
					{
						IOUtils.copy(remote.openStream(),new FileOutputStream(source));
					}
				}
				resp.setContentType("image/jpeg");
				IOUtils.copy(new FileInputStream(new File("images/"+image.get("hash")+".jpeg")),resp.getOutputStream());
			}
			else
			{
				resp.setStatus(404);
			}
		}
		catch(Exception ex)
		{
			resp.setStatus(404);
			LogFactory.getLog(Image.class).error(null,ex);
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		doGet(req, resp);
	}
}
