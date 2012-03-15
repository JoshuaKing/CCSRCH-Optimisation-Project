package decompress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import errors.PdfError;

import main.Settings;

public class PdfExtractor {
	private File pdfdir, pdffile;
	private PDDocument pdfdoc;
	private String pdfpath;

	public PdfExtractor(Settings settings, String fileloc) throws PdfError {
		// Create a (hopefully) unique directory for it //
		pdffile = new File(fileloc);
		try {
			pdfdoc = PDDocument.load(pdffile);
		} catch (IOException e) {
			throw new PdfError();
		}
		String temp_path = null;
		try {
			temp_path = settings.temp().getCanonicalPath();
		} catch (IOException e1) {
			temp_path = settings.temp().getAbsolutePath();
		}

		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd-MMM-yyyy-hh-mm-ss-z");
		String date = formatter.format(new Date());
		pdfpath = temp_path + "/ccsrch-pdf-file-at-" + date + "-of-" + pdffile.getName() + ".txt";

		pdfdir = new File(pdfpath);

		

		try {
			if (!pdfdir.createNewFile()) {
				settings.log()
						.warning(
								"Pdf Extractor: Could not create temporary file for unarchival of " + pdfpath + ", skipping.");
				throw new PdfError();
			}
			pdfpath = pdfdir.getCanonicalPath();
		} catch (IOException e) {
			pdfpath = pdfdir.getAbsolutePath();
		}
	}

	public void extract() throws PdfError {
		try {
			// Read from PDF reader //
			PDFTextStripper reader = new PDFTextStripper();
			//String filecontents = reader.getText(pdfdoc);
			FileOutputStream outstream = new FileOutputStream(pdfdir);
			for (int i = reader.getStartPage(); i < reader.getEndPage(); i++) {
				reader.setStartPage(i);
				reader.setEndPage(i);
				String page = reader.getText(pdfdoc);
				try {
					outstream.write(page.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					outstream.write(page.getBytes());
				}
			}
			outstream.close();
			pdfdoc.close();
		} catch (IOException e) {
			throw new PdfError();
		}
	}

	public File getPdfFile() {
		return pdfdir;
	}
}
