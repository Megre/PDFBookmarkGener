package group.spart.pbg;

import java.util.Arrays;
import java.util.List;

import com.itextpdf.kernel.geom.Rectangle;

import group.spart.pbg.bean.Constants;
import group.spart.pbg.bean.PageColumns;
import group.spart.pbg.bean.RawTextBlock;
import group.spart.pbg.bean.TitleBlock;

/** 
 * Extract titles from raw text block.
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 22, 2020 7:05:28 PM 
 */
public class TitleExtractor {
	
	private TextRender fRender;
	
	// different for each page
	private List<RawTextBlock> fRawTextBlocks;
	private Rectangle fContentRect;
	private int fCurrentPage;
	private int fColumn;
	
	public TitleExtractor(TextRender render) {
		fRender = render;
	}
	
	public PageColumns extract() {
		initialize(fRender);
		
		float[] columnPos = decideColumn();
		return extractTitle(columnPos);
	}
	
	private void initialize(TextRender render) {
		fColumn = render.getColumn();
		fRawTextBlocks = render.getRawTextBlocks();
		fContentRect = render.getContentRect();
		fCurrentPage = render.getCurrentPage();
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
			
			List<TitleBlock> blockList = pageColumns.getBlockList(columnIndex);
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
	
	private float[] decideColumn() {
		float left = fContentRect.getX(), right = fContentRect.getX() + fContentRect.getWidth();
		int[] pointsCount = new int[(int)Math.ceil((right-left)/1.0f)]; // step width: 1.0
		
		for(int idx = 0; idx<fRawTextBlocks.size(); ++idx) {
			
			Rectangle block = fRawTextBlocks.get(idx).getRect();
			int start = (int) Math.floor(block.getX() - left),
					end = (int) Math.floor(block.getX() + block.getWidth() - left);
			for(int j=Math.max(0, start); j<pointsCount.length && j<=end; ++j) {
				pointsCount[j] += 1;
			}
		}
		
		return calcColumnSplitPos(pointsCount);		
	}
	
	private float[] calcColumnSplitPos(int[] pointsCount) {
		if(fColumn >= fContentRect.getWidth()) {
			return new float[] { fContentRect.getX() + fContentRect.getWidth() }; // one column
		}
		
		float[] rst = new float[fColumn];
		Arrays.fill(rst, Float.MAX_VALUE);
		
		// vertically split the content rect to (column + 1) parts, 
		// assume column is 2, then the first split position is within the second part. 
		float splitPosRangeWidth = fContentRect.getWidth() / (fColumn + 1);
		
		int[] leastCountIdx = new int[fColumn];
		Arrays.fill(leastCountIdx, Integer.MAX_VALUE);
		for(int idx=0; idx<pointsCount.length; ++idx) {
			int columnIdx = (int)Math.floor(idx / splitPosRangeWidth) - 1;
			if(columnIdx < 0) continue;
			
			if(leastCountIdx[columnIdx] >= pointsCount.length 
					|| pointsCount[idx] < pointsCount[leastCountIdx[columnIdx]])
				leastCountIdx[columnIdx] = idx;
		}
		
		for(int idx=0; idx<fColumn; ++idx) {
			rst[idx] = fContentRect.getX() + leastCountIdx[idx];
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
