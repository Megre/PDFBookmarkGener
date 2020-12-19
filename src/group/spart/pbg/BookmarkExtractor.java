package group.spart.pbg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.navigation.PdfExplicitDestination;

import group.spart.pbg.bean.Bookmark;
import group.spart.pbg.bean.Constants;
import group.spart.pbg.bean.PageColumns;
import group.spart.pbg.bean.TitleBlock;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 14, 2020 11:22:07 AM 
 */
public class BookmarkExtractor {
	private List<PageColumns> fPageColumnsList;
	private PdfDocument fDocument;
	
	// processing status
	private int fPrePage;
	private PdfOutline fLastOutline;
	private Map<PdfOutline, Integer> outlineLevelMap = new HashMap<>(); // outline -> extracted level
	
	private PdfOutline fRootOutline;
	
	public BookmarkExtractor(List<PageColumns> titleInfoLists, PdfDocument document) {
		this.fPageColumnsList = titleInfoLists;
		this.fDocument = document;
	}
	
	public void extract(int pageFrom, int pageTo, int pageOffset) {
		fRootOutline = fDocument.getOutlines(false);
		
		PdfOutline content = addOutline(fRootOutline, 1, "目录", pageFrom);
		
		// the sates used across pages
		updateState(pageFrom, content, 1);
		
		for(PageColumns pageColumns: fPageColumnsList) {
			processOnePage(pageColumns, pageOffset);
		}
	}
	
	private void processOnePage(PageColumns pageColumns, int pageOffset) {
		for(int colIdx = 0; colIdx < pageColumns.columnSize(); ++colIdx) {
			processOneColumn(pageColumns.getBlockList(colIdx), pageOffset);
		}
	}
	
	private void processOneColumn(List<TitleBlock> titleInfoList, int pageOffset) {
		
		// trim title texts
		Map<TitleBlock, Bookmark> titleBookmarMap = calcLevelAligns(titleInfoList);
		
		List<TitleBlock> validTitleInfo = new ArrayList<>();
		for(TitleBlock titleInfo: titleInfoList) {
			if(!titleBookmarMap.containsKey(titleInfo)) continue;
			
	    	validTitleInfo.add(titleInfo);
		}
		
		// add outlines
		for(int idx=0; idx<validTitleInfo.size(); ++idx) {
			TitleBlock titleInfo = validTitleInfo.get(idx);
			
			Bookmark bookmarkInfo = titleBookmarMap.get(titleInfo);
	    	String title = bookmarkInfo.getTitle();
	    	int page = detectPage(bookmarkInfo.getPage(), validTitleInfo, idx, titleBookmarMap);
	    	int level = bookmarkInfo.getLevel();
	    	
	    	System.out.println(titleInfo.getText() + " -> " 
	    			+ "title: " + title + ", " 
	    			+ "level: " + level + ", " 
	    			+ "extracted page: " + bookmarkInfo.getPage() + ", " 
	    			+ "calcuated page: " + page + ", " 
	    			+ "offset page: " + (page + pageOffset));
	    	
	    	// detect parent outline and add current outline to it
	    	PdfOutline curOutline = addOutline(null, level, title, page + pageOffset);
	    	
	    	updateState(page, curOutline, level);
		}
	}
	
	/**
	 * Detect a page if given page is invalid.
	 * @param page
	 * @param validTitleInfo
	 * @param idx
	 * @param titleBookmarMap
	 * @return
	 */
	private int detectPage(int page, List<TitleBlock> validTitleInfo, int idx, Map<TitleBlock, Bookmark> titleBookmarMap) {
		int rst = page;
		if(page < 1 || page > fDocument.getNumberOfPages()) {
	    	// use the page of following titles
	    	int nextPage = -1;
			if(idx + 1 < validTitleInfo.size()) {
				for(int j = idx+1; j<validTitleInfo.size(); ++j) {
					Bookmark nextBookmarkInfo = titleBookmarMap.get(validTitleInfo.get(j));
					nextPage = nextBookmarkInfo.getPage();
					if(nextPage > 0 
							&& nextPage > fPrePage 
							&& nextPage < fDocument.getNumberOfPages()) {
						break;
					}
				}
			}
			
    		rst = nextPage;
    	}
		return rst;
	}
	
	/**
	 * Detect parent outline and add a child outline to it.
	 * @param parentOutline not-null to specify parent outline or null to detect one
	 * @param level
	 * @param title
	 * @param page
	 * @return added outline
	 */
	private PdfOutline addOutline(PdfOutline parentOutline, int level, String title, int page) {
		if(parentOutline == null) { // detect the parent of current outline
			int lastLevel = outlineLevelMap.get(fLastOutline);
	    	if(level == lastLevel) {
	    		parentOutline = fLastOutline.getParent();
	    	}
	    	else if(level > lastLevel) {
	    		parentOutline = fLastOutline;
	    	}
	    	else {
	    		parentOutline = fLastOutline.getParent(); 

	    		while(parentOutline != null) {
	    			if(outlineLevelMap.containsKey(parentOutline)) {
	    				int thisLevel = outlineLevelMap.get(parentOutline);
	    				if(level == thisLevel) {
	    					parentOutline = parentOutline.getParent();
	    					break;
	    				}
	    				if(level > thisLevel) {
	    					break;
	    				}
	    			}
	    			parentOutline = parentOutline.getParent();
	    		}
	    		if(parentOutline==null)
	    			parentOutline = fRootOutline;
	    	}
		}
		
    	PdfOutline curOutline = parentOutline.addOutline(title);
    	curOutline.addAction(PdfAction.createGoTo(PdfExplicitDestination.createFitH(fDocument.getPage(page-1), 0)));
    	return curOutline;
	}
	
	private void updateState(int prePage, PdfOutline lastOutline, int level) {
		this.fPrePage = prePage;
		this.fLastOutline = lastOutline;
		outlineLevelMap.put(lastOutline, level);
	}
	
	/**
	 * Build bookmarks by aligning title texts.
	 * @param titleInfoList
	 * @return TitleInfo to BookmarInfo map
	 */
	private Map<TitleBlock, Bookmark> calcLevelAligns(List<TitleBlock> titleInfoList) {
		Map<TitleBlock, Bookmark> titleBookmarkMap = new HashMap<>();
		
		// sort title infos by x axis
		List<TitleBlock> aligns = new ArrayList<>();
		for(TitleBlock info: titleInfoList) {
			aligns.add(info);
		}
		aligns.sort(new Comparator<TitleBlock>() {
			@Override
			public int compare(TitleBlock o1, TitleBlock o2) {
				if(o1.getRect().getX() == o2.getRect().getX()) return 0;
				return o1.getRect().getX()>o2.getRect().getX()?1:-1;
			}
		});
		
		// build TitleInfo->BookmarInfo map
		int idx = 0;
		int level = 1;
		Map<TitleBlock, Bookmark> tmpMap = new HashMap<>();
		while(idx < aligns.size()) {
			TitleBlock titleInfo = aligns.get(idx);
			Bookmark bookmarkInfo = new Bookmark(titleInfo); // trim title texts
			
			if(!bookmarkInfo.isValid()) {
				++idx;
				continue;
			}
			
			tmpMap.clear();
			tmpMap.put(titleInfo, bookmarkInfo);
			
			float x = titleInfo.getRect().getX();
			while(++idx < aligns.size()) {
				titleInfo = aligns.get(idx);
				bookmarkInfo = new Bookmark(titleInfo);
				if(!bookmarkInfo.isValid()) continue;
				
				// near x
				if(Math.abs(x - titleInfo.getRect().getX()) > Constants.AlignOffset) {
					break;
				}
				
				x = titleInfo.getRect().getX();
				tmpMap.put(titleInfo, bookmarkInfo);
			}
			
			// must have 1 titles at least locating at x to add a level of bookmarks
			if(tmpMap.size() > 0) {
				for(Entry<TitleBlock, Bookmark> entry: tmpMap.entrySet()) {
					entry.getValue().setLevel(level);
				}
				titleBookmarkMap.putAll(tmpMap);
				++level;
			}
		}
		return titleBookmarkMap;
	}
	
}
