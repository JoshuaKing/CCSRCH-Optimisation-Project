package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	File log = null;
	BufferedWriter bw;
	int verbosity;
	String newline;

	public Log(File f) {
		this(f, 0);
	}

	public Log(File f, int verbosity) {
		this.verbosity = verbosity;
		newline = String.format("%n");

		log = f;
		if (log == null || !log.isFile() || !log.canWrite()) {
			log = null;
			return;
		}
		try {
			bw = new BufferedWriter(new FileWriter(log));
		} catch (IOException e) {
			log = null;
		}
	}

	public void flush() {
		try {
			bw.flush();
		} catch (IOException e) {
			return;
		}
	}

	public static void dbug(String msg) {
		System.out.println("DEBUG>   " + msg);
	}

	public static void info(String msg) {
		System.out.println("INFO>    " + msg);
	}

	public static void warn(String msg) {
		System.out.println("WARNING> " + msg);
	}

	public static void succ(String msg) {
		System.out.println("SUCCESS> " + msg);
	}

	public static void fatl(String msg, int code) {
		System.out.println("**** FATAL ERROR: CANNOT CONTINUE ****");
		System.out.println("ERROR>   " + msg);
		System.exit(code);
	}

	public void debug(String msg) {
		if (verbosity > 0)
			return;
		Log.dbug(msg);
		if (log != null && log.isFile() && log.canWrite()) {
			try {
				bw.write(String.format("DEBUG>   " + msg + newline));
				flush();
			} catch (IOException e) {
				return;
			}
		}
	}

	public void information(String msg) {
		if (verbosity > 1)
			return;
		Log.info(msg);
		if (log != null && log.isFile() && log.canWrite()) {
			try {
				bw.write("INFO>    " + msg + newline);
				flush();
			} catch (IOException e) {
				return;
			}
		}
	}

	public void warning(String msg) {
		if (verbosity > 2)
			return;
		Log.warn(msg);
		if (log != null && log.isFile() && log.canWrite()) {
			try {
				bw.write("WARNING> " + msg + newline);
				flush();
			} catch (IOException e) {
				return;
			}
		}
	}

	public void fatal(String msg, int code) {
		if (verbosity > 3)
			return;
		if (log != null && log.isFile() && log.canWrite()) {
			try {
				bw.write("**** FATAL ERROR: CANNOT CONTINUE ****" + newline);
				bw.write("ERROR>   " + msg + newline);
				Log.fatl(msg, code); // Also log error to console
			} catch (IOException e) {
				Log.fatl(msg, code);
			}
		}
	}

	public void success(String msg) {
		if (verbosity > 4)
			return;
		Log.succ(msg);
		if (log != null && log.isFile() && log.canWrite()) {
			try {
				bw.write("SUCCESS> " + msg + newline);
			} catch (IOException e) {
				return;
			}
		}
	}
}
