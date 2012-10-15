import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.management.JMException;
import java.io.PrintWriter;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import com.mongodb.tools.ConnectionPoolStat;

@WebServlet(name="Status",urlPatterns={"/Status"})
public class Status extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		resp.setContentType("application/json; charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		Data data = Data.getInstance();
		JSONObject json = new JSONObject();
		try
		{
			json.put("count",data.getCount());
			json.put("last",data.getLast());
			json.put("cardJobs",data.getCardJobs());
			json.put("langJobs",data.getLangJobs());
			json.put("mongo",new JSONObject(new ConnectionPoolStat().getStats())); 
			out.print(json.toString());
		}
		catch(JSONException ex)
		{
			out.print("{ \"error\" : \"JSON Error\" }");
		}
		catch(JMException ex)
		{
			out.print("{ \"error\" : \"?\" }");
		}
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		doGet(req, resp);
	}
}
