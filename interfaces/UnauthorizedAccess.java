package interfaces;

public class UnauthorizedAccess extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnauthorizedAccess(String reason)
	{
		super(reason);
	}
}

