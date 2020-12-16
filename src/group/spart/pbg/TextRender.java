package group.spart.pbg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import group.spart.pbg.bean.Constants;
import group.spart.pbg.bean.TitleBlock;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 13, 2020 5:11:04 PM 
 */
public class TextRender extends LocationTextExtractionStrategy {
	
	private List<Map<String, Rectangle>> textRectMapList = new ArrayList<>();
	
	private PdfDocument document;
	private PdfDocumentContentParser parser;
	private List<TitleBlock> infoList = new ArrayList<>();
	private int currentPage = -1;
	
	public TextRender(PdfDocument document) {
		this.document = document;
		parser = new PdfDocumentContentParser(document);
	}
	
	public List<List<TitleBlock>> processPages(int from, int to) {
		List<List<TitleBlock>> pageTitleInfo  = new ArrayList<>();
		for(int idx=from; idx>0 && idx<=to && idx<=document.getNumberOfPages(); ++idx) {
			currentPage = idx;
			parser.processContent(idx, this);
			
			List<TitleBlock> pageInfoList = new ArrayList<>();
			pageInfoList.addAll(infoList);
			pageTitleInfo.add(pageInfoList);
			
			infoList.clear();
		}
		return pageTitleInfo;
	}


	@Override
	public void eventOccurred(IEventData data, EventType type) {
		if(type != EventType.RENDER_TEXT) return;
		
		TextRenderInfo renderInfo = (TextRenderInfo) data;
		
		String text = renderInfo.getText();
		if(text.length() == 0) return;
		
		Rectangle rectBase = renderInfo.getDescentLine().getBoundingRectangle();
		Rectangle rectAscent = renderInfo.getAscentLine().getBoundingRectangle();
		
		Rectangle rect = new Rectangle(rectAscent.getX(), rectAscent.getY(), 
				rectAscent.getWidth(), rectAscent.getY() - rectBase.getY());
		
//		System.out.println(text 
//				+ "  x:" + rect.getX() 
//				+ "  y:" + rect.getY() 
//				+ "  width:" + rect.getWidth() 
//				+ "  height:" + rect.getHeight()
//				+ "  font: " + renderInfo.getFont()
//				+ "  fontSize: " + renderInfo.getFontSize());
		
		TitleBlock titleInfo = null;
		int index = indexNearAxisY(rect);
		if(index >= 0){
			titleInfo = infoList.get(index);
			Rectangle rectAtY = titleInfo.getRect(); 
			
			rectAtY.setX(Math.min(rect.getX(), rectAtY.getX()));
			rectAtY.setY(rect.getY());
			rectAtY.setWidth(rect.getWidth() + rectAtY.getWidth());
			rectAtY.setHeight(rect.getHeight());
			
			titleInfo.appendText(text); 
			titleInfo.udpateAverageHeight(rect.getHeight()); 
		}
		else {
			titleInfo = new TitleBlock(rect, text, renderInfo.getFont(), renderInfo.getFontSize(), currentPage);
			infoList.add(titleInfo);
		}
		
		Map<String,Rectangle> map = new HashMap<>();
		map.put(text, rect);
		textRectMapList.add(map);
		
	}
	
	private int indexNearAxisY(Rectangle rect) {
		final float axisY = rect.getY();
		for(int i=0; i<infoList.size(); ++i) {
			TitleBlock info = infoList.get(i);
			if(Math.abs(axisY - info.getRect().getY()) <= Constants.AlignOffset
					&& Math.abs(rect.getHeight() - info.getAverageHeight()) <= Constants.HeightOffset) {
				return i;
			}
		}
		return -1;
	}
}