package ps.exception;

/**
 * Graph related exception
 */
public class GraphException extends Exception {

	private static final long serialVersionUID = 3494595388317993794L;

	public GraphException(String msg) {
		System.err.println(msg);
		System.err.flush();
	}
	
}