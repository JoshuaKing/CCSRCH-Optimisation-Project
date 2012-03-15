package main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Initialise {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Settings settings = Settings.parse_arguments(args);
		
		deleteTempDirectory(settings.temp(), settings.log());
		
		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd MMM yyyy hh:mm:ss (z)");
		String start = formatter.format(new Date());
		settings.log().information("Starting Scan at " + start);
		ArrayList<String> search_list = settings.getSearchList();
		for (int i = 0; i < search_list.size(); i++) {
			File search = null;
			try {
				search = new File(search_list.get(i)).getCanonicalFile();
			} catch (IOException e) {
				settings.log().warning("Sorry, could not open " + search_list.get(i) +": " + e.getLocalizedMessage());
				continue;
			}
			
			HandleDirectory d = new HandleDirectory(settings, search);
			d.handle();
		}

		deleteTempDirectory(settings.temp(), settings.log());
		
		String finish = formatter.format(new Date());
		settings.log().information("Finished Scan at " + finish);
		settings.addSummary("Started scan at " + start + ", finished at " + finish);
		settings.writeSummary();
		settings.log().flush();
	}
	
	private static void deleteTempDirectory(File tmp, Log log) {
		String[] dir = tmp.list();
        for (int i = 0; i < dir.length; i++) {
        	File f = new File(tmp, dir[i]);
        	f.deleteOnExit();
        	if (!deleteDirectory(f)) {
        		log.warning("Could not delete temp file/folder " + dir[i]);
        	}
        }
	}
	
	private static Boolean deleteDirectory(File f) {
		if (f.isDirectory()) {
	        String[] children = f.list();
	        for (int i = 0; i < children.length; i++) {
	            boolean success = deleteDirectory(new File(f, children[i]));
	            if (!success) return false;
	        }
	    }
		return f.delete();
	}

}
