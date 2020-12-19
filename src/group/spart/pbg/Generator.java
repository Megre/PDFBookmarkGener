package group.spart.pbg;

import java.io.File;
import java.io.IOException;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 12:53:40 PM 
 */
public class Generator {
	
	private final String usage = "Command line arguments: [path] [from]-[to] [offset] [column]\n"
			+ "path: the path of input PDF file\n"
			+ "from: the page number where the catalog page starts\n"
			+ "to: the page number where the catalog page ends\n"
			+ "offset: number of pages ahead the first chapter\n"
			+ "column (optional, default: 1): number of layout columns of the catalog\n";
	
	private int pageOffset, pageFrom, pageTo, column = 1;
	private String inputFilePath;
	
    public static void main(String[] args) throws IOException {
    	Generator gener = new Generator();
    	
    	if(!gener.parseParam(args)) {
    		return;
    	}
    	
    	gener.generate();
    }
 
    private void generate() {
    	
    	// process content page 
    	TitleExtractor titleExtractor = new TitleExtractor(new File(inputFilePath));
    	titleExtractor.extract(pageFrom, pageTo, pageOffset, column);    	
    	
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
			System.out.println(e.getMessage());
			return false;
		}
    	
    	return true;
    }
    
    private void printUsage() {
    	System.out.println(usage);
    }
}
