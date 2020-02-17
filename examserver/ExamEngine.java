package examserver;

import interfaces.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.server.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;

import examserver.StudentAuthenticator;

public class ExamEngine implements ExamServer
{
	private StudentAuthenticator sAuth;
	private Hashtable<String, Assessment> assessments;
    // Constructor is required
    public ExamEngine()
    {
        super();
        sAuth = new StudentAuthenticator();
        assessments = new Hashtable<String, Assessment>(10);
    }

    // Implement the methods defined in the ExamServer interface...
    // Return an access token that allows access to the server for some time period
    public int login(int studentid, String password) throws UnauthorizedAccess, RemoteException
    {
    	int token = sAuth.authenticateUser(studentid, password);
    	if (token == -1 || !sAuth.authenticateUserToken(studentid, token))
    	{
    		throw new UnauthorizedAccess("Incorrect Student ID or Password");
    	}
    	else
    	{
    		populateSummaryAssessments(studentid);
    		return token;
    	}
    }

    private void populateSummaryAssessments(int studentid)
    {
    	FileReader fr;
		try
		{
			fr = new FileReader("./assessments.txt");
			BufferedReader br = new BufferedReader(fr);
			Stream<String> stream = br.lines();
			Stream<String> assessmentsApplicable = stream.filter(str -> str.contains(Integer.toString(studentid)));
			ArrayList<String> assessmentNames = new ArrayList<String>(10);
			assessmentsApplicable.forEach(s -> assessmentNames.add(s));
			br.close();
			
			for(String assessment : assessmentNames)
			{
				String[] assessmentParameters = assessment.split(";");
				Date closeDate;
				closeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(assessmentParameters[3] + " 23:59:00");
				
				assessments.put(assessmentParameters[4], new AssessmentImpl(assessmentParameters[1], assessmentParameters[2], closeDate, studentid, assessmentParameters[4]));
			}
		}
		catch (IOException | ParseException e)
		{
			e.printStackTrace();
		}
	}

	// Return a summary list of Assessments currently available for this studentid
    public List<String> getAvailableSummary(int token, int studentid) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException
    {
    	if(!sAuth.authenticateUserToken(studentid, token)) throw new UnauthorizedAccess("Invalid User Token");
    	
    	ArrayList<String> summaryList = new ArrayList<String>(assessments.size());
    	
    	//this was satisfying :) nice little lambda
    	assessments.forEach((key, assess) -> {
    		summaryList.add(assess.getInformation());
    	});
        return summaryList;
    }

    // Return an Assessment object associated with a particular course code
    public Assessment getAssessment(int token, int studentid, String courseCode) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException
    {
    	if(!sAuth.authenticateUserToken(studentid, token)) throw new UnauthorizedAccess("Invalid User Token");
    	
    	Assessment assess = assessments.get(courseCode);
    	if(assess == null) throw new NoMatchingAssessment("Assessment " + courseCode + " not found");
    	
        return assess;
    }

    // Submit a completed assessment
    public void submitAssessment(int token, int studentid, Assessment completed, String courseCode) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException
    {
    	if(!sAuth.authenticateUserToken(studentid, token)) throw new UnauthorizedAccess("Invalid User Token");
    	
    	Date now = new Date();
    	if(assessments.contains(courseCode)) throw new NoMatchingAssessment("Invalid Course Code");
    	
    	if(!assessments.get(courseCode).getClosingDate().after(now)) throw new UnauthorizedAccess("Late Submission Rejected for " + courseCode);
    	
    	assessments.put(courseCode, completed);
    }

    public static void main(String[] args)
    {
        String policy="/Users/MichaelNaughton/Documents/CSIT/Fourth Year/Semester II/Dist Systems/Assignments/Assign#1/ct414/server.policy";
        System.setProperty("java.security.policy", policy);
        
        if (System.getSecurityManager() == null)
        {
            System.out.println("noSecurityManager");
            System.setSecurityManager(new SecurityManager());
        }
        
        try
        {
            String name = "examserver";
            ExamServer engine = new ExamEngine();
            ExamServer stub = (ExamServer) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry(42777);
            registry.rebind(name, stub);
            
            System.out.println("ExamEngine bound");
        }
        catch (Exception e)
        {
            System.err.println("ExamEngine exception:");
            e.printStackTrace();
        }
    }
}
