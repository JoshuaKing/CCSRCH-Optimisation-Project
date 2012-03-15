package main;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;

import errors.ConfigFileParseError;

public class Settings {
	private File log, config, temp;
	private Log file_log;
	private ArrayList<String> search_paths, ignore_extensions;
	private ArrayList<String> summary;

	// should be constant after initialisation //
	private ArrayList<String> special_extensions;
	private String path_separator = System.getProperty("path.separator");
	private String file_separator = System.getProperty("file.separator");
	private String default_log = "config" + file_separator + "ccsrch.log";
	private String default_temp = "temp/";

	private void setDefault() {
		special_extensions.add("zip");
		special_extensions.add("gz");
		special_extensions.add("pdf");
		String l = System.getProperty("user.dir");
		l += System.getProperty("file.separator") + "ccsrch.log";
		log = new File(l);
		config = null;
	}

	public Settings() {
		this(1, null, null, null, new ArrayList<String>());
	}

	private static String getDefaultConfig() {
		String defaultconf = System.getProperty("user.dir") + File.separator;
		defaultconf += "config" + File.separator + "ccsrch.conf";
		return defaultconf;
	}

	public void setConfigFile(String configfile) {
		if (configfile != null) {
			file_log.debug("Searching for configuration file at " + configfile);
			try {
				config = new File(configfile).getCanonicalFile();
			} catch (IOException e) {
				file_log.warning("Error occured when resolving configuration file path, resorting to default location.");
				config = new File(configfile).getAbsoluteFile();
				;
			}
		}
		if (configfile == null || !config.exists() || !config.isFile()
				|| !config.canRead()) {
			if (configfile != null)
				file_log.warning("Configuration file does not exist/not readable, resorting to default location.");

			String defaultconf = getDefaultConfig();

			file_log.debug("Searching for configuration file at " + defaultconf);
			config = new File(defaultconf);

			if (!config.exists() || !config.isFile() || !config.canRead()) {
				file_log.warning("Default configuration file does not exist/not readable, resorting to default settings.");
				config = null;
				return;
			}
		}
	}
	
	public static void usage() {
		System.out.println("Usage: ");
		System.out.println("");
		System.out.println("	-h --help		Print this usage information.");
		System.out.println("");
		System.out.println("	-c --config-file <path>	Set configuration file location.");
		System.out.println("");
		System.out.println("	-l --log-file <path>	Set log file location.");
		System.out.println("");
		System.out.println("	-t --temp-dir <path>	Set temporary directory location.");
		System.out.println("				Used to extract files (zip, gz, pdf, etc).");
		System.out.println("");
		System.out.println("	-v --verbosity <0 - 4>	Set verbosity of output.");
		System.exit(0);
	}

	public static Settings parse_arguments(String[] args) {
		String config = getDefaultConfig(), temp = null, log = null;
		int verbosity = 1;
		ArrayList<String> search_list = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (i + 1 == args.length || args[i + 1].startsWith("-")) {
				if (a.equals("-h") || a.equals("--help") || a.equals("/h"))
					usage();
				continue;
			}

			i++;
			String a2 = args[i];
			if (a.equals("-c") || a.equals("--config-file"))
				config = a2;
			else if (a.equals("-l") || a.equals("--log-file"))
				log = a2;
			else if (a.equals("-t") || a.equals("--temp-dir"))
				temp = a2;
			else if (a.equals("-v") || a.equals("--verbosity"))
				verbosity = Integer.valueOf(a2);
			else if (!a.startsWith("-") && (i-- != -1))
				search_list.add(a);

		}
		if (temp != null && log != null && search_list.size() > 0 && config == getDefaultConfig())
			config = null;
		
		return new Settings(verbosity, config, temp, log, search_list);
	}

	public void setTempDirectory(String temp_path) {
		if (temp != null) {
			file_log.debug("Searching for configuration file at " + temp_path);
			try {
				temp = new File(temp_path).getCanonicalFile();
			} catch (IOException e) {
				file_log.warning("Error occured when resolving temporary file path, resorting to default location.");
				temp = new File(temp_path).getAbsoluteFile();
			}

			if (!temp.exists()) {
				if (!temp.mkdirs()) {
					file_log.fatal("Could not open temp folder.", 450);
				}
			}
		}
		if (temp == null || !temp.exists() || !temp.isDirectory()
				|| !temp.canWrite()) {
			if (temp_path != null)
				file_log.warning("Temp directory does not exist/not writable, resorting to default location.");

			String defaulttemp = System.getProperty("user.dir");
			defaulttemp += file_separator + default_temp;

			file_log.debug("Searching for temp directory at " + defaulttemp);
			temp = new File(defaulttemp);

			if (!temp.exists() || !temp.isDirectory() || !temp.canWrite()) {
				file_log.fatal(
						"Default Temporary directory does not exist/not writable, resorting to default settings.",
						450);
				return;
			}
		}
	}

	public void setLogLocation(String location, int verbosity) {
		if (location != null) {
			try {
				log = new File(location).getCanonicalFile();
			} catch (IOException e) {
				log = new File(location).getAbsoluteFile();
			}

			try {
				Boolean newlog = log.createNewFile();
				if (newlog) {
					file_log = new Log(log, verbosity);
					file_log.information("Created new, empty log file.");
				}
			} catch (IOException e) {
				// hopefully file just already exists
			}
		}

		if (location == null || log == null || !log.exists() || !log.isFile()
				|| !log.canWrite()) {
			if (location != null)
				Log.warn("Log file does not exist/not writable, resorting to default location.");

			String defaultlog = System.getProperty("user.dir");
			defaultlog += file_separator + default_log;

			Log.dbug("Searching for log file at " + defaultlog);
			try {
				log = new File(defaultlog).getCanonicalFile();
			} catch (IOException e) {
				log = new File(defaultlog).getAbsoluteFile();
			}

			try {
				Boolean newdeflog = log.createNewFile();
				if (newdeflog)
					Log.info("Created new, empty log file.");
			} catch (IOException e) {
				// hopefully file just already exists
			}

			if (!log.exists() || !log.isFile() || !log.canWrite()) {
				Log.warn("Default log file does not exist/not writable, resorting to default settings.");
				log = null;
				return;
			}
		}
		file_log = new Log(log, verbosity);
	}

	public Settings(int verbosity, String configfile, String tempfile,
			String logfile, ArrayList<String> search) {
		search_paths = search;
		ignore_extensions = new ArrayList<String>();
		special_extensions = new ArrayList<String>();
		summary = new ArrayList<String>();

		setDefault();
		setLogLocation(logfile, verbosity);
		setTempDirectory(tempfile);
		try {
			if (configfile != null)
				setConfigFile(configfile);
			
			if (tempfile == null && logfile == null && search.size() == 0)
				parse_config_file(verbosity);
			
		} catch (ConfigFileParseError e) {
			file_log.fatal(e.getLocalizedMessage(), 501);
		}
	}

	private String next_line(BufferedReader br, Integer line_num)
			throws IOException {
		String line = br.readLine();
		while (line != null && (line.trim().equals("") || line.startsWith("#"))) {
			line = br.readLine();
			line_num++;
		}
		if (line == null)
			throw new EOFException();
		return line;
	}

	private void parse_config_file(int verbosity) throws ConfigFileParseError {
		file_log.debug("Parsing config file.");
		try {
			BufferedReader br = new BufferedReader(new FileReader(config));
			String line = "";
			String help = "";
			Integer line_num = 1;

			// Parse log file location //
			line = next_line(br, line_num);

			help = "Log line must be first, and must start with \"LOG:\"";
			help += " followed by filepath of log, if desired (default otherwise)";

			if (!line.startsWith("LOG:"))
				throw new ConfigFileParseError(help, line_num, line);
			setLogLocation(line.substring(4).trim(), verbosity);

			// Parse Ignore Extensions //
			line = next_line(br, line_num);
			help = "Ignored Extensions line must be second, and must start with \"IGNORE:\"";
			help += " followed by file extensions to ignore (seperated by '";
			help += path_separator + "', if desired (default otherwise)";

			if (!line.startsWith("IGNORE:"))
				throw new ConfigFileParseError(help, line_num, line);
			String[] ignore = line.substring(7).toLowerCase()
					.split(path_separator);
			for (int i = 0; i < ignore.length; i++) {
				file_log.debug("Adding '" + ignore[i].trim()
						+ "' to ignored extensions.");
				ignore_extensions.add(ignore[i].trim());
			}

			// Parse Files/Directories to search //
			line = next_line(br, line_num);
			help = "Directories/Files to search must be third, and must start with \"SEARCH:\"";
			help += " followed by paths of directories/files to search (seperated by '";
			help += path_separator;
			help += "', if none specified, nothing will be searched.)";

			if (!line.startsWith("SEARCH:"))
				throw new ConfigFileParseError(help, line_num, line);
			String[] search = line.substring(7).toLowerCase()
					.split(path_separator);
			for (int i = 0; i < search.length; i++) {
				file_log.debug("Adding '" + search[i].trim()
						+ "' to search list.");
				search_paths.add(search[i].trim());
			}

		} catch (FileNotFoundException e) {
			file_log.warning("Config file not found: "
					+ e.getLocalizedMessage());
			return;
		} catch (EOFException e) {
			file_log.debug("Config file finished earlier than expected.");
			return;
		} catch (IOException e) {
			file_log.warning("Encountered error while reading config file: "
					+ e.getLocalizedMessage());
		}
	}

	public Boolean validExtension(String filename) {
		String[] split_str = filename.split("[.]");
		String ext = split_str[split_str.length - 1].toLowerCase();
		if (ignore_extensions.contains(ext))
			return false;

		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String mime = fileNameMap.getContentTypeFor(filename);
		Log.dbug("Mime Type for " + filename + " = " + mime);

		if (special_extensions.contains(ext))
			return true;
		if (mime == null)
			return true; // unknown mime type
		if (mime.equals("application/octet-stream"))
			return true;
		if (mime.equals("application/zip"))
			return true;
		if (mime.equals("application/pdf"))
			return true;
		if (mime.matches("text/.+"))
			return true; // check file is simple text

		return false;
	}

	public ArrayList<String> getSearchList() {
		ArrayList<String> copy = new ArrayList<String>(search_paths);
		return copy;
	}

	public Log log() {
		return file_log;
	}

	public File temp() {
		return temp;
	}

	public String getLogLocation() {
		try {
			return log.getCanonicalPath();
		} catch (IOException e) {
			return log.getAbsolutePath();
		}
	}

	public String getTempLocation() {
		try {
			return temp.getCanonicalPath();
		} catch (IOException e) {
			return temp.getAbsolutePath();
		}
	}

	public void addSummary(String line) {
		summary.add(line);
	}

	public void writeSummary() {
		file_log.success("============== SUMMARY ==============");
		file_log.success("");
		for (int i = 0; i < summary.size(); i++)
			file_log.success(summary.get(i));
		file_log.success("=====================================");
	}
}
