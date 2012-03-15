package decompress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import errors.GzipError;

import main.Settings;

public class GzipExtractor {
	private File gzipdir, gzipfile;
	String gzippath;

	public GzipExtractor(Settings settings, File gzip) throws GzipError {
		// Create a (hopefully) unique directory for it //
		gzipfile = gzip;
		String temp_path = null;
		try {
			temp_path = settings.temp().getCanonicalPath();
		} catch (IOException e1) {
			temp_path = settings.temp().getAbsolutePath();
		}
		
		String end = gzip.getName();
		if (end.toLowerCase().endsWith(".gz")) {
			end = end.substring(0, end.length() - 3);
		}

		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd-MMM-yyyy-hh-mm-ss-z");
		String date = formatter.format(new Date());
		gzippath = temp_path + "/ccsrch-gzip-archive-at-" + date + "-of-" + end;

		gzipdir = new File(gzippath);

		

		try {
			if (!gzipdir.createNewFile()) {
				settings.log()
						.warning(
								"Gzip Extractor: Could not create temporary file for unarchival of " + gzippath + ", skipping.");
				throw new GzipError();
			}
			gzippath = gzipdir.getCanonicalPath();
		} catch (IOException e) {
			gzippath = gzipdir.getAbsolutePath();
		}
	}

	public void extract() throws GzipError {
		try {
			// Create input stream //
			GZIPInputStream gzipinstream = new GZIPInputStream(
					new FileInputStream(gzipfile));

			FileOutputStream outstream = new FileOutputStream(gzipdir);
			int n;
			byte[] buf = new byte[1024];
			while ((n = gzipinstream.read(buf, 0, 1024)) > -1)
				outstream.write(buf, 0, n);
			outstream.close();
			gzipinstream.close();
			
		} catch (IOException e) {
			throw new GzipError();
		}
	}

	public File getGzipFile() {
		return gzipdir;
	}
}
