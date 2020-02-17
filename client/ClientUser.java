package client;

import interfaces.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

public class ClientUser
{
	private int sessionToken = -1, sID = 0;
	private ExamServer connection;
	private Hashtable<String, Assessment> assessments;
	
	public ClientUser(int sessionToken, int sID, ExamServer connection)
	{
		this.sessionToken = sessionToken;
		this.sID = sID;
		this.connection = connection;
		assessments = new Hashtable<String, Assessment>();
	}
	
	public void startCLI()
	{
		Scanner s = new Scanner(System.in);
		
		for(String input = s.nextLine().toLowerCase(); true; input = s.nextLine().toLowerCase())
		{
			if(input.contains("exit"))
			{
				System.out.println("Goodbye");
				break;
			}
			
			switch(input)
			{
				case "getavailablesummary":
				case "gas":
					printSummary();
					break;
				case "getassessment":
				case "ga":
					System.out.println("Enter Course Code of Assessment");
					String courseCode = s.nextLine();
					getAssessmentObj(courseCode);
					break;
				case "submitassessment":
				case "sa":
					System.out.println("Enter Course Code of Assessment to submit");
					String assessmentCodeSubmittable = s.nextLine();
					
					submitAssessmentObj(assessmentCodeSubmittable);
					
					break;
				case "completeassessment":
				case "ca":
					System.out.println("Enter Course Code of Assessment");
					String assessmentCode = s.nextLine();
					completeAssessmentQuestions(assessmentCode, s);
					break;
				case "help":
					System.out.println("Welcome options include the following:\n"
							+ "\"getAvailableSummary\" | \"gas\" -> get all assessments due\n"
							+ "\"getAssessment\" | \"ga\" -> start completing an assessment\n"
							+ "\"completeAssessment\" | \"ca\" -> start completing an assessment\n"
							+ "\"submitAssessment\" | \"sa\" -> if finished entering Answers to an assignment select submitAssessment\n");
					break;
				case "settoken":
					System.out.println("Enter token");
					sessionToken = Integer.parseInt(s.nextLine());
					break;
				default:
					System.out.println("\n\nInvalid Input type help for available options\n");
					break;
			}
		}
		
		s.close();
	}

	private void submitAssessmentObj(String assessmentCodeSubmittable) {
		Assessment assess = assessments.get(assessmentCodeSubmittable);
		if(assess == null)
		{
			System.out.println("Assessment not found locally, use get assignment to download");
			return;
		}
		
		try
		{
			connection.submitAssessment(sessionToken, sID, assess, assessmentCodeSubmittable);
			System.out.println("Submission successful");
		}
		catch (RemoteException e)
		{
			System.out.println("An Error Occurred try logging in again\n");
		}
		catch (NoMatchingAssessment | UnauthorizedAccess e)
		{
			System.out.println("An Error occured ->\t" + e.getMessage());
		}
	}

	private void getAssessmentObj(String courseCode) {
		try
		{
			Assessment assess = connection.getAssessment(sessionToken, sID, courseCode);
			assessments.put(courseCode, assess);
			System.out.println("Assessment Acquired the Following Assessments are ready to be completed:\n");
			assessments.forEach((key, assessment) -> {
				System.out.println(assessment.getInformation());
			});
		}
		catch (RemoteException | UnauthorizedAccess e)
		{
			System.out.println("An Error Occurred try logging in again\n -> \t" + e.getMessage());
		}
		catch (NoMatchingAssessment e)
		{
			System.out.println("Invalid Assessment Course Code\n" + e.getMessage());
		}
	}

	private void completeAssessmentQuestions(String courseCode, Scanner s)
	{
		Assessment assess = assessments.get(courseCode);
		if(assess == null)
		{
			System.out.println("Assessment not found locally, use get assignment to download");
			return;
		}
		
		System.out.printf("You've started the following assessment:\n %s \n", assess.getInformation());
		
		List<Question> questions = assess.getQuestions();
		
		String input = "";
		int inputNum = 0;
		int questionNumber = 1;
		
		for(Question q : questions)
		{
			System.out.printf("\n%d:\t%s:\n", questionNumber, q.getQuestionDetail());
			String[] answerOptions = q.getAnswerOptions();
			for(String option : answerOptions)
			{
				System.out.println(option);
			}
			
			do
			{
				System.out.printf("\nEnter Answer Number in range [0] -> [%d] or exit with incomplete Assessment:\n", answerOptions.length-1);
				input = s.nextLine();
				if(input == "exit")
					break;
				
				inputNum = Integer.parseInt(input);
			}while(inputNum < 0 || inputNum > answerOptions.length - 1);
			
			try
			{
				assess.selectAnswer(q.getQuestionNumber(), inputNum);
			}
			catch (InvalidQuestionNumber e)
			{
				e.printStackTrace();
			}
			catch (InvalidOptionNumber e)
			{
				e.printStackTrace();
			}
		}
		
		System.out.printf("Here's you're assessment summary:\n %s \n", assess.getInformation());
	}

	private void printSummary()
	{
		try
		{
			List<String> listAssessmentsDue = connection.getAvailableSummary(sessionToken, sID);
			System.out.println("Assessments Due:");
			for(String assessString : listAssessmentsDue)
			{
				System.out.println(assessString);
			}
		}
		catch (RemoteException | UnauthorizedAccess e)
		{
			System.out.println("An Error Occurred try logging in again\n->" + e.getMessage());
		}
		catch (NoMatchingAssessment e)
		{
			System.out.println("An Error Occurred finding this assessment check summary and ensure the assessment exists\n");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		//login and start cli for client if login is successful
		
		String name  = new String("examserver");
		int sID = Integer.parseInt(args[0]);
		String password = args[1];
		int sessionToken = -1;
        
        int registryport = 42777;
        if (args.length > 2)
           registryport = Integer.parseInt(args[0]);

        System.out.println("RMIRegistry port = " + registryport);
        String policy = "/Users/MichaelNaughton/Documents/CSIT/Fourth Year/Semester II/Dist Systems/Assignments/Assign#1/ct414/client.policy";
        System.setProperty("java.security.policy", policy);

        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
        
        try
        {
            Registry registry = LocateRegistry.getRegistry(registryport);
            System.out.println(name);
            ExamServer examServ = (ExamServer) registry.lookup(name);
            
            sessionToken = examServ.login(sID, password);
            System.out.printf("Successful Login\nWelcome: \t%d\n", sID);
            
            ClientUser cUser = new ClientUser(sessionToken, sID, examServ);
            cUser.startCLI();
        }
        catch(RemoteException | NotBoundException | UnauthorizedAccess e)
		{
            System.out.println(e.getMessage());
		}
	}
}
