import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet(name="IndexWords",urlPatterns={"/IndexWords"})
public class IndexWords extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		if(isValid(req.getParameter("key")))
		{
			long start = System.currentTimeMillis();
			Data.getInstance().reIndexWords();
			long end = System.currentTimeMillis();

			resp.getOutputStream().println("Reindexing the words took "+(end-start)+"ms");
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
