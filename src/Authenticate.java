import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;

public class Authenticate
{
	public static boolean isValid(String key)
	{
		DBCollection coll = Data.getMongodb().getDB("mtg").getCollection("keys");
		if(coll.find(new BasicDBObject("key",key)).count()>0)
		{
			return true;
		}
		return false;
	}
}
