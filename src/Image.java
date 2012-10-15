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
import java.net.URL;
import java.net.MalformedURLException;

@WebServlet(name="Image",urlPatterns={"/Image"})
public class Image extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			if(req.getParameter("type")!=null && req.getParameter("type").equals("card") && req.getParameter("multiverseid")!=null)
			{
				//also surves to check for valid parameters
				int id = Integer.parseInt(req.getParameter("multiverseid"));
				File image = new File("images/card-"+id+".jpeg");
				if(!image.exists())
				{
					URL remote = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid="+id+"&type=card");
					image.createNewFile();
					IOUtils.copy(remote.openStream(),new FileOutputStream(image));
				}
				resp.setContentType("image/jpeg");
				IOUtils.copy(new FileInputStream(image),resp.getOutputStream());
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
