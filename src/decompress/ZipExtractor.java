package decompress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import errors.ZipError;

import main.Log;
import main.Settings;

public class ZipExtractor {
	private File zipdir, zipfile;
	String zippath;

	public ZipExtractor(Settings settings, File zip) throws ZipError {
		// Create a (hopefully) unique directory for it //
		zipfile = zip;
		String temp_path = null;
		try {
			temp_path = settings.temp().getCanonicalPath();
		} catch (IOException e1) {
			temp_path = settings.temp().getAbsolutePath();
		}

		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd-MMM-yyyy-hh-mm-ss-z");
		String date = formatter.format(new Date());
		zippath = temp_path + "/ccsrch-zip-archive-of-" + zip.getName()
				+ "-at-" + date + File.separator;

		zipdir = new File(zippath);

		if (!zipdir.mkdir()) {
			settings.log()
					.warning(
							"Zip Extractor: Could not create temporary file for unarchival of " + zippath + ", skipping.");
			throw new ZipError();
		}

		try {
			zippath = zipdir.getCanonicalPath() + File.separator;
		} catch (IOException e) {
			zippath = zipdir.getAbsolutePath() + File.separator;
		}
	}

	public void extract() throws ZipError {
		try {
			// Create input stream //
			ZipInputStream zipinstream = new ZipInputStream(
					new FileInputStream(zipfile));
			ZipEntry zipentry = zipinstream.getNextEntry();
			if (zipentry == null) throw new ZipError();
			
			while (zipentry != null) {

				if (zipentry.isDirectory()) {
					Log.info("Zip Extractor: Directory " + zipentry.getName()
							+ " Found.");
					File d = new File(zippath + zipentry.getName());
					d.mkdirs();

				} else {
					Log.info("Zip Extractor: File " + zipentry.getName()
							+ " Found.");
					File entry = new File(zippath + zipentry.getName());

					FileOutputStream outstream = new FileOutputStream(entry);
					int n;
					byte[] buf = new byte[1024];
					while ((n = zipinstream.read(buf, 0, 1024)) > -1)
						outstream.write(buf, 0, n);
					outstream.close();
				}
				zipentry = zipinstream.getNextEntry();
			}
			zipinstream.close();
			
		} catch (IOException e) {
			throw new ZipError();
		}
	}

	public File getZipDirectory() {
		return zipdir;
	}
}
