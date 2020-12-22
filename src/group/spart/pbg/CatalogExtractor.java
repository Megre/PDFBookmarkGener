package group.spart.pbg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import group.spart.pbg.bean.PageColumns;
import group.spart.pbg.err.ExtractionException;

/** 
 * Copy input PDF file and extract bookmarks.
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 19, 2020 2:17:16 PM 
 */
public class CatalogExtractor {
	
	private File fPdfFile;
	private File fOutputFile;
	
	private PdfDocument fDocument;
	private List<PageColumns> fPageColumnList = new ArrayList<>();
	
	public CatalogExtractor(File file) {
    	fPdfFile = file;
		fDocument = readDocument();
	}
	
    private PdfDocument readDocument() {
    	PdfReader reader = null;
		try {
			reader = new PdfReader(fPdfFile);
		} 
		catch(Exception e) {
			throw new ExtractionException(e.getMessage());
		}
		return new PdfDocument(reader);
    }
    
	/**
	 * Extract catalog pages.
	 * @param pageFrom the page number that the catalog starts
	 * @param pageTo the page number that the catalog ends
	 * @param pageOffset number of pages ahead the first chapter
	 * @param column number of columns of the catalog
	 * @return extracted columns of all catalog pages
	 */
	public void extract(int pageFrom, int pageTo, int pageOffset, int column) {
		checkInputParam(pageFrom, pageTo, pageOffset, column);
		
		TextRender render = new TextRender(fDocument);
		TitleExtractor titleExtractor = new TitleExtractor(render);
		PageColumns pageColumns = null;
		for(int page=pageFrom; page<=pageTo; ++page) {
			render.processPage(page, column);
			pageColumns = titleExtractor.extract();
			fPageColumnList.add(pageColumns);
		}
		
		addBookmarks(fPageColumnList, pageFrom, pageTo, pageOffset);
	}
	
	private void addBookmarks(List<PageColumns> pageColumnList, int pageFrom, int pageTo, int pageOffset) {
		PdfDocument newDocument = copyPdf(); 
		if(newDocument == null) return;
		
    	new BookmarkExtractor(pageColumnList, newDocument).extract(pageFrom, pageTo, pageOffset);
    	newDocument.getCatalog().setPageMode(PdfName.UseOutlines);
    	newDocument.close();
    	
    	System.out.println("Output path: " + fOutputFile.getAbsolutePath());
	}
	
	private PdfDocument copyPdf() {
		
    	PdfWriter writer = null;
		try {
			fOutputFile = createOutputFile();
			writer = new PdfWriter(fOutputFile);
		} catch (Exception e) {
			throw new ExtractionException(e.getMessage());
		}
    	PdfDocument newDocument = new PdfDocument(writer);
    	fDocument.copyPagesTo(1, fDocument.getNumberOfPages(), newDocument);
    	fDocument.close();
    	
    	return newDocument;
	}
	
	
	private File createOutputFile() {
    	String shortName = fPdfFile.getName();
    	shortName = shortName.substring(0, shortName.lastIndexOf("."));
    	return new File(fPdfFile.getParentFile().getAbsolutePath() + "\\" + shortName + "_PBG.pdf");
	}
	
	private void checkInputParam(int pageFrom, int pageTo, int pageOffset, int column) {
		if(pageFrom <= 0 || pageFrom > fDocument.getNumberOfPages()
				|| pageTo <= 0 || pageTo > fDocument.getNumberOfPages()
				|| pageFrom > pageTo
				|| pageOffset < 0
				|| pageOffset + pageTo > fDocument.getNumberOfPages()
				|| column <= 0) {
			throw new ExtractionException("Error input arguments.");
		}
			
	}
	
}
