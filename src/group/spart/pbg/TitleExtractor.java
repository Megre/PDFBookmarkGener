package group.spart.pbg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import group.spart.pbg.bean.PageColumns;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 19, 2020 2:17:16 PM 
 */
public class TitleExtractor {
	
	private PdfDocument fDocument;
	private File fPdfFile, fSavedFile;
	private List<PageColumns> fPageColumnList = new ArrayList<>();
	
	public TitleExtractor(File pdfFile) {
    	PdfReader reader = null;
		try {
			reader = new PdfReader(pdfFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		fDocument = new PdfDocument(reader);
		fPdfFile = pdfFile;
	}
	
	public List<PageColumns> extract(int pageFrom, int pageTo, int pageOffset, int column) {
		TextRender render = new TextRender(fDocument);
		PageColumns pageColumns = null;
		for(int page=pageFrom; page<=pageTo; ++page) {
			pageColumns = render.processPage(page, column);
			fPageColumnList.add(pageColumns);
		}
		
		addBookmarks(pageFrom, pageTo, pageOffset);
		
    	return fPageColumnList;
	}
	
	private void addBookmarks(int pageFrom, int pageTo, int pageOffset) {
		PdfDocument newDocument = copyPdf();
    	new BookmarkExtractor(fPageColumnList, newDocument).extract(pageFrom, pageTo, pageOffset);
    	newDocument.getCatalog().setPageMode(PdfName.UseOutlines);
    	newDocument.close();
    	
    	System.out.println("Output path: " + fSavedFile.getAbsolutePath());
	}
	
	private PdfDocument copyPdf() {
		// copy PDF file
    	String shortName = fPdfFile.getName();
    	shortName = shortName.substring(0, shortName.lastIndexOf("."));
    	fSavedFile = new File(fPdfFile.getParentFile().getAbsolutePath() + "\\" + shortName + "_PBG.pdf");
    	PdfWriter writer = null;
		try {
			writer = new PdfWriter(fSavedFile);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
    	PdfDocument newDocument = new PdfDocument(writer);
    	fDocument.copyPagesTo(1, fDocument.getNumberOfPages(), newDocument);
    	fDocument.close();
    	
    	return newDocument;
	}
}
