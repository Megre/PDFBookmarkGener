package group.spart.pbg;

import java.io.IOException;
import static java.lang.System.*;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 12:53:40 PM 
 */
public class Main {

	public static final String file = "C:\\Users\\TF\\Documents\\自动同步\\微云同步\\书籍\\职业规划\\你的生命有什么可能[古典].pdf";
	 
    /**
     *   创建一个PDF文件：hello.pdf 
     * @param    args    no arguments needed
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
    	PdfReader reader = new PdfReader(file);
    	PdfDocument document = new PdfDocument(reader);
    	
    	String text = PdfTextExtractor.getTextFromPage(document.getPage(14));
    	out.println(text);
    }
 
    

}
