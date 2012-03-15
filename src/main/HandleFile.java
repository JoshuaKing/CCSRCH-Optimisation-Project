package main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;

import decompress.GzipExtractor;
import decompress.PdfExtractor;
import decompress.ZipExtractor;
import errors.GzipError;
import errors.PdfError;
import errors.ZipError;

/**
 * Searches a file for credit card information
 * 
 * @author Josh
 * @version 2.0.0
 */
public class HandleFile implements Runnable {
	Settings settings;
	File file;
	String filename, actualfilename;
	ArrayList<Thread> threads;
	Boolean halt;

	/**
	 * Constructor for Handling a file
	 * 
	 * @author Josh
	 * @param s
	 *            the settings class for checking log-file clashes, and logging
	 * @param f
	 *            file which will be processed for credit cards
	 * @param actualfile
	 *            the string detailing the actual location of this file (used
	 *            for extracted ZIP, Gzip, and PDF files)
	 */
	public HandleFile(Settings s, File f, String actualfile) {
		file = f;
		settings = s;
		threads = new ArrayList<Thread>();
		halt = false;
		actualfilename = actualfile;
		
		if (actualfilename.equals(settings.getLogLocation())) {
			settings.log().information("Filepath matches Log file location, skipping.");
			halt = true;
			return;
		} else if (actualfile.startsWith(settings.getTempLocation())) {
			settings.log().information("File is in Temp directory, skipping.");
			halt = true;
			return;
		}
		
		try {
			filename = file.getCanonicalPath();
		} catch (IOException e) {
			filename = file.getAbsolutePath();
		}
		if (actualfilename == null)
			actualfilename = filename;

		Log.dbug("Handling " + actualfilename);

		if (!settings.validExtension(filename)) {
			Log.dbug("Invalid Extension/File Type, skipping.");
			halt = true;
			return;
		}

		String[] split_str = filename.split("[.]");
		String ext = split_str[split_str.length - 1].toLowerCase();

		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String mime = fileNameMap.getContentTypeFor(filename);

		if (mime == null || mime.equals("application/octet-stream")) {
			if (ext.equals("zip") || (!ext.equals("gz") && !ext.equals("pdf")))
				handleZip(ext);

			if (ext.equals("gz") || (!ext.equals("zip") && !ext.equals("pdf")))
				handleGzip(ext);

			if (ext.equals("pdf") || (!ext.equals("zip") && !ext.equals("gz")))
				handlePdf(ext);
		} else if (mime.equals("application/pdf")) {
			handlePdf(ext);
		} else if (mime.equals("application/zip")) {
			handleZip(ext);
		}
	}

	/**
	 * Constructor for Handling a file
	 * 
	 * @author Josh
	 * @param s
	 *            the settings class for checking log-file clashes, and logging
	 * @param f
	 *            file which will be processed for credit cards
	 */
	public HandleFile(Settings s, File f) {
		this(s, f, null);
	}

	private void handlePdf(String extension) {
		Log.dbug("Extracting PDF file.");
		try {
			// Create Pdf Extractor //
			PdfExtractor pe = new PdfExtractor(settings, filename);
			pe.extract();
			HandleFile pdf_file = new HandleFile(settings, pe.getPdfFile(),
					actualfilename);
			pdf_file.scan();
			halt = true;
			return;
		} catch (PdfError e) {
			Log.warn("PDF Extraction Failed.");
			if (extension.equals("pdf")) {
				settings.log().debug("PDF Extraction Error, skipping.");
				halt = true;
				return;
			}
			settings.log().debug(
					"PDF Extraction Error, probably not a PDF file.");
		}
	}

	private void handleGzip(String extension) {
		Log.dbug("Extracting Gzip file.");
		String actual = actualfilename;
		if (actual.toLowerCase().endsWith(".gz")) {
			actual = actual.substring(0, actual.length() - 3);
		}
		try {
			// Create Gzip Extractor //
			GzipExtractor ge = new GzipExtractor(settings, file);
			ge.extract();
			HandleFile gzip_file = new HandleFile(settings, ge.getGzipFile(),
					actual);
			gzip_file.scan();
			halt = true;
			return;
		} catch (GzipError e) {
			Log.warn("Gzip Extraction Failed.");
			if (extension.equals("gz")) {
				settings.log().debug("Gzip Extraction Error, skipping.");
				halt = true;
				return;
			}
			settings.log().debug(
					"Gzip Extraction Error, probably not a Gzip file.");
		}
	}

	private void handleZip(String extension) {
		Log.dbug("Extracting ZIP file.");
		// create Zip Extractor //
		try {
			ZipExtractor ze = new ZipExtractor(settings, file);
			ze.extract();
			File dir = ze.getZipDirectory();
			HandleDirectory zip_dir = new HandleDirectory(settings, dir,
					actualfilename);
			zip_dir.handle();
			halt = true;
			return;
		} catch (ZipError e) {
			Log.warn("Zip Extraction Failed.");
			if (extension.equals("zip")) {
				settings.log().debug("Zip Extraction Error, skipping.");
				halt = true;
				return;
			}
			settings.log().debug(
					"Zip Extraction Error, probably not a Zip file.");
		}
	}

	/**
	 * Scan file for credit card numbers hidden inside. Creates a thread for
	 * each possible Credit Card, for fast checking
	 * 
	 * @author Josh
	 */
	public void scan() {
		// Next step, scan for credit card details! //
		// This is some fairly simple searching using Regular Expressions //
		if (halt)
			return;
		try {
			RandomAccessFile f = new RandomAccessFile(file, "r");
			long l = f.length();

			int min_jump = CreditCard.getMinLength();
			long pos = min_jump;
			f.seek(pos);

			while ((pos = f.getFilePointer()) < l) {
				char c = (char) f.read();
				if (c != ' ' && (c < '0' || c > '9')) {
					try {
						f.skipBytes(min_jump);
						continue;
					} catch (IOException e) {
						return;
					}
				}
				String sequence = "";
				long found_at = pos, start_at = found_at;
				long min_start = pos - min_jump + 1;
				while (pos != min_start) {
					pos--;
					f.seek(pos);
					c = (char) f.read();
					if (c >= '0' && c <= '9') {
						sequence += c;
						start_at = pos;
					} else if (c == ' ') {
						// do nothing
					} else {
						break;
					}
				}
				sequence = new StringBuffer(sequence).reverse().toString();
				pos = found_at - 1;
				f.seek(pos);
				while (true) {
					pos++;
					f.seek(pos);
					c = (char) f.read();
					if (c >= '0' && c <= '9') {
						sequence += c;
					} else if (c == ' ') {
						// do nothing
					} else {
						break;
					}
				}

				int seq_length = sequence.length();
				if (seq_length < min_jump) {
					// f.skipBytes(min_jump);
					continue;
				}

				ArrayList<Integer> lengths = CreditCard.validLengths();
				for (int i = 0; i < lengths.size(); i++) {
					int cc_len = lengths.get(i);
					for (int off = 0; off <= seq_length - cc_len; off++) {
						String sub_seq = sequence.substring(off, off + cc_len);
						CreditCard cc = new CreditCard(settings, sub_seq,
								actualfilename, start_at + off);
						Thread t = new Thread(cc, "Credit Card Test " + sub_seq);
						t.start();
						threads.add(t);
					}
				}
			}

			for (int i = 0; i < threads.size(); i++) {
				if (!threads.get(i).isAlive()) {
					try {
						threads.get(i).join();
					} catch (InterruptedException e) {
						continue;
					}
					threads.remove(i);
					i = -1;
				}
			}
		} catch (IOException e) {
			settings.log().warning(
					"Could not open " + filename
							+ " for random-access reading, skipping.");
			return;
		}
	}

	@Override
	public void run() {
		scan();
	}
}
