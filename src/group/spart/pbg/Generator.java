package group.spart.pbg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import group.spart.pbg.bean.TitleBlock;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 12:53:40 PM 
 */
public class Generator {
	
	private final String usage = "Command line arguments: [path] [from]-[to] [offset]\n"
			+ "path: the path of input PDF file\n"
			+ "from: the page number where the catalog ends starts\n"
			+ "to: the page number where the catalog page ends\n"
			+ "offset: number of pages ahead the first chapter";
	
	private int pageOffset, pageFrom, pageTo;
	private String inputFilePath;
	
    public static void main(String[] args) throws IOException {
    	Generator gener = new Generator();
    	
    	if(!gener.parseParam(args)) {
    		return;
    	}
    	
    	gener.generate();
    }
 
    private void generate() {
    	File pdfFile = new File(inputFilePath);
    	PdfReader reader = null;
		try {
			reader = new PdfReader(pdfFile);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
    	PdfDocument document = new PdfDocument(reader);
    	
    	// process content page 
    	TextRender listener = new TextRender(document);
    	List<List<TitleBlock>> infoLists = listener.processPages(pageFrom, pageTo);

    	// copy PDF file
    	String shortName = pdfFile.getName();
    	shortName = shortName.substring(0, shortName.lastIndexOf("."));
    	File newFile = new File(pdfFile.getParentFile().getAbsolutePath() + "\\" + shortName + "_PBG.pdf");
    	PdfWriter writer = null;
		try {
			writer = new PdfWriter(newFile);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
    	PdfDocument newDocument = new PdfDocument(writer);
    	document.copyPagesTo(1, document.getNumberOfPages(), newDocument);
    	document.close();
    	
    	// add bookmarks
    	new BookmarkExtractor(infoLists, newDocument).extract(pageFrom, pageTo, pageOffset);
    	newDocument.getCatalog().setPageMode(PdfName.UseOutlines);
    	
    	newDocument.close();
    	
    	System.out.println("Output path: " + newFile.getAbsolutePath());
    }
    
    private boolean parseParam(String[] args) {
    	if(args.length != 3) {
    		printUsage();
    		return false;
    	}
    	
    	int paramIdx = 0;
    	
    	inputFilePath = args[paramIdx++];
    	
    	// from-to
    	String[] fromTo = args[paramIdx++].split("-");
    	if(fromTo.length != 2) {
    		printUsage();
    		return false;
    	}
    	
    	try {
    		pageFrom = Integer.parseInt(fromTo[0]);
    		pageTo = Integer.parseInt(fromTo[1]);
    		pageOffset = Integer.parseInt(args[paramIdx++]);
    	}
    	catch (NumberFormatException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	return true;
    }
    
    private void printUsage() {
    	System.out.println(usage);
    }
}
