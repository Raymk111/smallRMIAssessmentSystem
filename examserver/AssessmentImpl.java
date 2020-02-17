package examserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import interfaces.InvalidQuestionNumber;
import interfaces.InvalidOptionNumber;
import interfaces.Assessment;
import interfaces.Question;


public class AssessmentImpl implements Assessment
{
	private static final long serialVersionUID = -2651026414931741771L;
	private String title, courseCode;
    private Date closingDate;
    private List<Question> questions;
    private int[] answers;
    private int sID;
    private boolean startedAssignment;
    
    public AssessmentImpl(String title, String assessmentSourceDir, Date closingDate, int sID, String courseCode)
    {
        this.title = title;
        this.courseCode = courseCode;
        this.closingDate = closingDate;
        this.sID = sID;
        populateQuestions(assessmentSourceDir);
    }
    
	private void populateQuestions(String assessmentSourceDir)
	{
		questions = new ArrayList<Question>(10);
		FileReader fr;
		try
		{
			fr = new FileReader(assessmentSourceDir + ".txt");
			BufferedReader br = new BufferedReader(fr);
			Stream<String> stream = br.lines();
			ArrayList<String> questionParams = new ArrayList<String>(10);
			stream.forEach(s -> questionParams.add(s));
			br.close();
			
			for(String assessment : questionParams)
			{
				String[] assessmentParameters = assessment.split(";");
				questions.add(new QuestionImpl(Integer.valueOf(assessmentParameters[0]), assessmentParameters[1], getAnswers(assessmentParameters[2])));
			}
			
			answers = new int[questions.size()];
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String[] getAnswers(String questions)
	{
		return questions.split(",");
	}

	// Return information about the assessment	
	public String getInformation()
    {
		String ret = "";
		if(startedAssignment)
		{
			ret = String.format("Code:%s \t Title:%s \t Due:%s\n", courseCode, title, closingDate.toString());
			for(Question q : questions)
			{
				ret += String.format("Question: %s\t ->\t %s\n", q.getQuestionDetail(), q.getAnswerOptions()[getSelectedAnswer(q.getQuestionNumber())]);
			}
		}
		else
		{
			ret = String.format("Code:%s \t Title:%s \t Due:%s\n", courseCode, title, closingDate.toString());
		}
		
        return ret;
    }

	// Return the final date / time for submission of completed assessment
	public Date getClosingDate()
    {
        return closingDate;
    }

	// Return a list of all questions and anser options
	public List<Question> getQuestions()
    {
        return questions;
    }

	// Return one question only with answer options
	public Question getQuestion(int questionNumber) throws InvalidQuestionNumber
    {
        if(questions.size() < questionNumber) throw new InvalidQuestionNumber("Question Number Out Of Bounds");
        
        return questions.get(questionNumber);
    }

	// Answer a particular question
	public void selectAnswer(int questionNumber, int optionNumber) throws InvalidQuestionNumber, InvalidOptionNumber
    {
		startedAssignment = true;
		if(questionNumber > answers.length) throw new InvalidQuestionNumber("Invalid Question Number Selected");
		
		if(questionNumber > questions.get(questionNumber).getAnswerOptions().length) throw new InvalidOptionNumber("Invalid Option Selected");
		
		answers[questionNumber] = optionNumber;
		
        return;
    }

	// Return selected answer or zero if none selected yet
	public int getSelectedAnswer(int questionNumber)
    {
        return answers[questionNumber];
    }

	// Return studentid associated with this assessment object
	// This will be preset on the server before object is downloaded
	public int getAssociatedID()
    {
        return sID;
    }

}