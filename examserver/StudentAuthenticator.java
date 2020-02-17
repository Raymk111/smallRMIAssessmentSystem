package examserver;

import java.util.ArrayList;
import java.util.Hashtable;

import javafx.util.Pair;

public class StudentAuthenticator
{
	private ArrayList<Pair<Integer, String>> accounts;
	private Hashtable<Integer, Integer> sessions;
	public StudentAuthenticator()
	{
		accounts = new ArrayList<Pair<Integer, String>>(100);
		sessions = new Hashtable<Integer, Integer>(100);
		populateAccounts();
	}
	
	public void populateAccounts()
	{
		accounts.add(new Pair<Integer, String>(16461226, "MickKnock"));
	}
	
	public int authenticateUser(int sID, String password)
	{
		int index = accounts.indexOf(new Pair<Integer, String>(sID, password));
		if(index > -1)
		{
			System.out.printf("User: %d Authenticated", sID);
			sessions.put(sID, (int)(Math.random() * Integer.MAX_VALUE));
			return sessions.get(sID);
		}
		else
		{
			return -1;
		}
	}
	
	public boolean authenticateUserToken(int sID, int token)
	{
		int tokenSession = sessions.get(sID);
		if(tokenSession == token)
		{
			System.out.printf("User: %d approved:\t", sID);
			return true;
		}
		
		
		System.out.printf("User: %d Failed Auth:\t", sID);
		return false;
	}
}
