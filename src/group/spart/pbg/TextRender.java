package group.spart.pbg;

import java.util.ArrayList;
import java.util.List;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import group.spart.pbg.bean.RawTextBlock;

/** 
 * Extract raw text blocks from catalog pages.
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 5:11:04 PM 
 */
public class TextRender extends LocationTextExtractionStrategy {
	private PdfDocument fDocument;
	private PdfDocumentContentParser fParser;
	
	// processing state for each page
	private List<RawTextBlock> fRawTextBlocks = new ArrayList<>();
	private int fCurrentPage = -1;
	private int fColumn;
	private Rectangle fContentRect;
	
	public TextRender(PdfDocument document) {
		fDocument = document;
		fParser = new PdfDocumentContentParser(document);
	}
	
	public void processPage(int page, int column) {
		initState(page, column);
		fParser.processContent(page, this);
	}


	@Override
	public void eventOccurred(IEventData data, EventType type) {
		if(type != EventType.RENDER_TEXT) return;
		
		TextRenderInfo renderInfo = (TextRenderInfo) data;
		
		String text = renderInfo.getText();
		if(text.length() == 0) return;
		
		float pageWidth = fDocument.getPage(fCurrentPage).getPageSizeWithRotation().getWidth();
		
		Rectangle rectBase = renderInfo.getDescentLine().getBoundingRectangle();
		Rectangle rectAscent = renderInfo.getAscentLine().getBoundingRectangle();
		
		if(rectAscent.getX() +  rectAscent.getWidth() > pageWidth 
				|| rectBase.getX() + rectBase.getWidth() > pageWidth) {
			System.out.println(text + " (out of page)");
			return;
		}
		
		Rectangle rect = new Rectangle(rectAscent.getX(), rectAscent.getY(), 
				rectAscent.getWidth(), rectAscent.getY() - rectBase.getY());
		
//		System.out.println(text 
//				+ "  x:" + rect.getX() 
//				+ "  y:" + rect.getY() 
//				+ "  width:" + rect.getWidth() 
//				+ "  height:" + rect.getHeight()
//				+ "  font: " + renderInfo.getFont()
//				+ "  fontSize: " + renderInfo.getFontSize());
		
		fRawTextBlocks.add(new RawTextBlock(text, rect, renderInfo.getFont(), renderInfo.getFontSize()));
		
		// calculate column
		fContentRect.setX(Math.min(fContentRect.getX(), rect.getX()));
		fContentRect.setY(Math.max(fContentRect.getY(), rect.getY()));
		fContentRect.setWidth(Math.max(fContentRect.getWidth(), rect.getX() + rect.getWidth() - fContentRect.getX()));
		fContentRect.setHeight(Math.max(fContentRect.getHeight(), fContentRect.getY() - (rect.getY() - rect.getHeight())));
	}
	
	public List<RawTextBlock> getRawTextBlocks() {
		return fRawTextBlocks;
	}
	
	public Rectangle getContentRect() {
		return fContentRect;
	}
	
	public int getCurrentPage() {
		return fCurrentPage;
	}
	
	public int getColumn() {
		return fColumn;
	}
	
	private void initState(int page, int column) {
		fColumn = column;
		fCurrentPage = page;
		fRawTextBlocks.clear();
		fContentRect = new Rectangle(Float.MAX_VALUE, -1, -1, -1); // min x, max y, max with, max height
	}
	

}