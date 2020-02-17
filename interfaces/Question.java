// Question.java

package interfaces;

import java.io.Serializable;

public interface Question extends Serializable
{
	
	// Return the question number
	public int getQuestionNumber();

	// Return the question text
	public String getQuestionDetail();

	// Return the possible answers to select from
	public String[] getAnswerOptions();

}
