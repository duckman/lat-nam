import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import com.mongodb.DBObject; 

@WebServlet(name="ReverseJobs",urlPatterns={"/ReverseJobs"})
public class ReverseJobs extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setCharacterEncoding("UTF-8");
		if(isValid(req.getParameter("key")))
		{
			for(DBObject card:Data.getInstance().fetchRemoteCard(Integer.parseInt(req.getParameter("id"))))
			{
				resp.getWriter().println(card.toString());
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
