package examserver;

import interfaces.Question;

public class QuestionImpl implements Question
{
	private static final long serialVersionUID = -5632945502126132851L;
	private int qNumber;
	private String qDetail;
	private String[] answerOptions;
	
	public QuestionImpl(int qNumber, String qDetail, String[] answerOptions)
	{
		this.qNumber = qNumber;
		this.qDetail = qDetail;
		this.answerOptions = answerOptions;
	}
	
	public int getQuestionNumber()
	{
		return qNumber;
	}

	public String getQuestionDetail()
	{
		return qDetail;
	}

	public String[] getAnswerOptions()
	{
		return answerOptions;
	}
	
}
