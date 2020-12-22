package group.spart.pbg;

import java.io.File;
import group.spart.pbg.err.ExtractionException;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 12:53:40 PM 
 */
public class Generator {
	
	// input arguments
	private int pageOffset, pageFrom, pageTo, column = 1;
	private String inputFilePath;
	
	private final String usage = "Command line arguments: [path] [from]-[to] [offset] [column]\n"
			+ "path: the path of input PDF file\n"
			+ "from: the page number where the catalog page starts\n"
			+ "to: the page number where the catalog page ends\n"
			+ "offset: number of pages ahead the first chapter\n"
			+ "column (optional, default: 1): number of layout columns of the catalog\n"
			+ "The output file is in the same path of the input file, whose name is appended with \"_PBG\".\n"
			+ "https://github.com/Megre/PDFBookmarkGener";
	
	private final String fDebugFlag = "_debug_PDFBookmarkGener_";
	
    public static void main(String[] args) {
    	Generator gener = new Generator();
    	
    	if(gener.parseParam(args)) {
    		gener.generate();
    	}
    }
    
    private void generate() {
    	
    	try {
	    	CatalogExtractor titleExtractor = new CatalogExtractor(new File(inputFilePath));
	    	titleExtractor.extract(pageFrom, pageTo, pageOffset, column);    
    	}
    	catch (ExtractionException e) {
			printMessage(e);
			printUsage();
		}
    	catch (Exception e) {
    		printMessage(e);
    	}
    	
    }
    
    private boolean parseParam(String[] args) {
    	if(args.length < 3) {
    		printUsage();
    		return false;
    	}
    	
    	int paramIdx = 0;
    	
    	inputFilePath = args[paramIdx++];
    	
    	// from-to, pageOffset, column
    	String[] fromTo = args[paramIdx++].split("-");
    	if(fromTo.length != 2) {
    		printUsage();
    		return false;
    	}
    	
    	try {
    		pageFrom = Integer.parseInt(fromTo[0]);
    		pageTo = Integer.parseInt(fromTo[1]);
    		pageOffset = Integer.parseInt(args[paramIdx++]);
    		
    		if(args.length > 3) {
    			column = Integer.parseInt(args[paramIdx++]);
    		}
    	}
    	catch (NumberFormatException e) {
			printMessage(e);
			return false;
		}
    	
    	return true;
    }
    
    private void printUsage() {
    	System.out.println(usage);
    }
    
    private void printMessage(Exception e) {
    	String debug = System.getenv(fDebugFlag);
    	if("true".equals(debug)) e.printStackTrace();
    	else System.err.println(e.getMessage());
    }
}
