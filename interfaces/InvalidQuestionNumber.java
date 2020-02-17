package interfaces;

public class InvalidQuestionNumber extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public InvalidQuestionNumber(String reason)
	{
		super(reason);
	}
}

