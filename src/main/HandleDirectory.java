package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Scans through a directory (and its children) for files containing credit
 * cards.
 * 
 * @author Josh
 * @version 1.0.0
 */
public class HandleDirectory {
	File root;
	File[] items;
	ArrayList<File> directories;
	Settings settings;
	String path;

	/**
	 * Constructor for Handling a directory (although, it will handle a file
	 * being given as well)
	 * 
	 * @author Josh
	 * @param s
	 *            the settings class for checking log-file clashes, and logging
	 * @param f
	 *            file (normally directory) which will be processed for files
	 *            containing credit cards
	 */
	public HandleDirectory(Settings s, File f) {
		this(s, f, null);
	}

	/**
	 * Constructor for Handling a directory (although, it will handle a file
	 * being given as well)
	 * 
	 * @author Josh
	 * @param s
	 *            the settings class for checking log-file clashes, and logging
	 * @param f
	 *            file (normally directory) which will be processed for files
	 *            containing credit cards
	 * @param p
	 *            path of the actual directory (used for extracted zip and gzip
	 *            files)
	 */
	public HandleDirectory(Settings s, File f, String p) {
		settings = s;
		root = f;
		directories = new ArrayList<File>();
		if (f.isFile()) {
			File[] items = { f };
			this.items = items;
			return;
		}

		items = root.listFiles();
		path = p;
		if (path != null)
			return;

		try {
			path = root.getCanonicalPath();
		} catch (IOException e) {
			path = root.getAbsolutePath();
		}
	}

	/**
	 * Searches all directories below, parsing files first, then 1 directory at
	 * a time
	 * 
	 * @author Josh
	 */
	public void handle() {
		settings.log().debug("Handling " + path);

		if (items == null) {
			settings.log().warning("Handling Directory " + path + " Failed");
			return;
		}

		for (int i = 0; i < items.length; i++) {
			if (items[i].isDirectory()) {
				if (items[i].getName().equals("."))
					continue;
				if (items[i].getName().equals(".."))
					continue;
				directories.add(items[i]);
				continue;
			}
			if (items[i].isFile() && items[i].canRead()) {
				String actual = path + File.separator + items[i].getName();

				HandleFile f = new HandleFile(settings, items[i], actual);
				f.scan();
				// Thread t = new Thread(f, "Handle File Test ");
				// t.start();
			}
		}

		for (int i = 0; i < directories.size(); i++) {
			HandleDirectory d = new HandleDirectory(settings,
					directories.get(i), path + File.separator
							+ directories.get(i).getName());
			d.handle();
		}

	}
}
