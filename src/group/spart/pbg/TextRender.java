package group.spart.pbg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;

import group.spart.pbg.bean.Constants;
import group.spart.pbg.bean.PageColumns;
import group.spart.pbg.bean.RawTextBlock;
import group.spart.pbg.bean.TitleBlock;

/** 
 * 
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
	private Rectangle contentRect;
	
	public TextRender(PdfDocument document) {
		fDocument = document;
		fParser = new PdfDocumentContentParser(document);
	}
	
	public PageColumns processPage(int page, int column) {
		initState(page);
		fParser.processContent(page, this);

		float[] columnPos = decideColumn(column);
		PageColumns pageColumns = extractTitle(columnPos);
		
		return pageColumns;
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
		contentRect.setX(Math.min(contentRect.getX(), rect.getX()));
		contentRect.setY(Math.max(contentRect.getY(), rect.getY()));
		contentRect.setWidth(Math.max(contentRect.getWidth(), rect.getX() + rect.getWidth() - contentRect.getX()));
		contentRect.setHeight(Math.max(contentRect.getHeight(), contentRect.getY() - (rect.getY() - rect.getHeight())));
	}
	
	private void initState(int page) {
		fCurrentPage = page;
		fRawTextBlocks.clear();
		contentRect = new Rectangle(Float.MAX_VALUE, -1, -1, -1); // minX, maxX, minY, maxY
	}
	
	private PageColumns extractTitle(float[] columnPos) {
		PageColumns pageColumns = new PageColumns();
		
		for(int idx=0; idx<fRawTextBlocks.size(); ++idx) {
			RawTextBlock textBlock = fRawTextBlocks.get(idx);
			
			Rectangle rect = textBlock.getRect();
			String text = textBlock.getText();
			
			int columnIndex = 0;
			for(int col=0; col<columnPos.length; ++col) {
				if(rect.getX() < columnPos[col]) {
					columnIndex = col;
					break;
				}
			}
			
			List<TitleBlock> blockList = pageColumns.getBlockList(columnIndex); // columnTitleList.get(columnIndex);
			TitleBlock titleInfo = null;
			int index = indexNearAxisY(rect, blockList);
			if(index < 0) {
				titleInfo = new TitleBlock(rect.clone(), text, textBlock.getFont(), textBlock.getFontSize(), fCurrentPage);
				blockList.add(titleInfo);
				continue;
			}
			
			titleInfo = blockList.get(index);
			Rectangle rectAtY = titleInfo.getRect(); 
			
			rectAtY.setX(Math.min(rect.getX(), rectAtY.getX()));
			rectAtY.setY(rect.getY());
			rectAtY.setWidth(rect.getWidth() + rectAtY.getWidth());
			rectAtY.setHeight(rect.getHeight());
			
			titleInfo.appendText(text); 
			titleInfo.udpateAverageHeight(rect.getHeight()); 
		}
		
		return pageColumns;
	}
	
	public float[] decideColumn(int column) {
		float left = contentRect.getX(), right = contentRect.getX() + contentRect.getWidth();
		int[] pointsCount = new int[(int)Math.ceil((right-left)/1.0f)]; // step width: 1.0
		
		for(int idx = 0; idx<fRawTextBlocks.size(); ++idx) {
			
			Rectangle block = fRawTextBlocks.get(idx).getRect();
			int start = (int) Math.floor(block.getX() - left),
					end = (int) Math.floor(block.getX() + block.getWidth() - left);
			for(int j=Math.max(0, start); j<pointsCount.length && j<=end; ++j) {
				pointsCount[j] += 1;
			}
		}
		
		return calcColumnSplitPos(pointsCount, column);		
	}
	
	private float[] calcColumnSplitPos(int[] pointsCount, int column) {
		float[] rst = new float[column];
		Arrays.fill(rst, Float.MAX_VALUE);
		
		float splitPosRangeWidth = contentRect.getWidth() / (column + 1);
		
		int[] leastCountIdx = new int[column];
		Arrays.fill(leastCountIdx, Integer.MAX_VALUE);
		for(int idx=0; idx<pointsCount.length; ++idx) {
			int columnIdx = (int)Math.floor(idx / splitPosRangeWidth) - 1;
			if(columnIdx < 0) continue;
			
			if(leastCountIdx[columnIdx] >= pointsCount.length 
					|| pointsCount[idx] < pointsCount[leastCountIdx[columnIdx]])
				leastCountIdx[columnIdx] = idx;
		}
		
		for(int idx=0; idx<column; ++idx) {
			rst[idx] = contentRect.getX() + leastCountIdx[idx];
			System.out.println(String.format("page %d: column split position %d: %.2f", fCurrentPage, idx + 1, rst[idx]));
		}
		
		return rst;
	}
	
	private int indexNearAxisY(Rectangle rect, List<TitleBlock> blocks) {
		final float axisY = rect.getY();
		for(int i=0; i<blocks.size(); ++i) {
			TitleBlock info = blocks.get(i);
			if(Math.abs(axisY - info.getRect().getY()) <= Constants.AlignOffset
					&& Math.abs(rect.getHeight() - info.getAverageHeight()) <= Constants.HeightOffset) {
				return i;
			}
		}
		return -1;
	}
}