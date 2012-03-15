package errors;

public class ConfigFileParseError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String l, h;
	private int n;
	
	public ConfigFileParseError(int linenumber, String line) {
		this(null, linenumber, line);
	}
	
	public ConfigFileParseError(String help, int linenumber, String line) {
		h = help;
		l = line;
		n = linenumber;
	}
	
	public String getMessage() {
		String s = "Error on Line #" + n + ": " + l;
		if (h != null) s += "\n> " + h;
		return s;
	}
}
