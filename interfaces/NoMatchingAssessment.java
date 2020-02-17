package interfaces;

public class NoMatchingAssessment extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMatchingAssessment(String reason)
	{
		super(reason);
	}
}

