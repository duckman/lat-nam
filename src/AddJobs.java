import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.net.URL;
import java.net.URLEncoder;
import org.apache.commons.logging.LogFactory;

@WebServlet(name="AddJobs",urlPatterns={"/AddJobs"})
public class AddJobs extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		if(Authenticate.isValid(req.getParameter("key")) && req.getParameter("start") != null && req.getParameter("end") != null)
		{
			Data.getInstance().addJobs(Integer.parseInt(req.getParameter("start")),Integer.parseInt(req.getParameter("end")));
		}
		if(Authenticate.isValid(req.getParameter("key")) && req.getParameter("set") != null)
		{
			int page=0;
			int last=0;
			URL in = new URL("http://gatherer.wizards.com/Pages/Search/Default.aspx?page="+page+"&action=advanced&set=%2B%5B%22"+URLEncoder.encode(req.getParameter("set"),"UTF-8")+"%22%5D");
			Document dom = Jsoup.parse(in.openStream(),null,"http://gatherer.wizards.com/");
			while(dom.select(".cardItem").size()>0 && last!=Integer.parseInt(dom.select(".cardItem").first().select("a").first().attr("href").split("[=&]")[1]))
			{
				last = Integer.parseInt(dom.select(".cardItem").first().select("a").first().attr("href").split("[=&]")[1]);
				for(Element card:dom.select(".cardItem"))
				{
					Data.getInstance().addJob(Integer.parseInt(card.select("a").first().attr("href").split("[=&]")[1]));
				}
				page++;
				in = new URL("http://gatherer.wizards.com/Pages/Search/Default.aspx?page="+page+"&action=advanced&set=%2B%5B%22"+URLEncoder.encode(req.getParameter("set"),"UTF-8")+"%22%5D");
				dom = Jsoup.parse(in.openStream(),null,"http://gatherer.wizards.com/");
			}
		}
		else
		{
			resp.setStatus(404);
		}
	}

	private boolean isValid(String key)
	{
		if(key != null && key.equals("testing"))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		doGet(req, resp);
	}
}
