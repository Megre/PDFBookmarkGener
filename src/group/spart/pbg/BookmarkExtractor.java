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
import group.spart.pbg.bean.TitleBlock;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 14, 2020 11:22:07 AM 
 */
public class BookmarkExtractor {
	private List<List<TitleBlock>> titleInfoLists;
	private PdfDocument document;
	
	private int prePage, lastLevel;
	private PdfOutline lastOutline;
	
	public BookmarkExtractor(List<List<TitleBlock>> titleInfoLists, PdfDocument document) {
		this.titleInfoLists = titleInfoLists;
		this.document = document;
	}
	
	public void extract(int pageFrom, int pageTo, int pageOffset) {
		PdfOutline root = document.getOutlines(false);
		List<PdfOutline> outlineRoots = new ArrayList<>();
		outlineRoots.add(root);
		
		PdfOutline content = addOutline(root, "目录", pageFrom);
		
		// the sates used across pages
		updateState(pageFrom, content, 1);
		
		for(List<TitleBlock> titleInfoList: titleInfoLists) {
			processOnePage(titleInfoList, pageOffset, outlineRoots);
		}
	}
	
	private void processOnePage(List<TitleBlock> titleInfoList, int pageOffset, List<PdfOutline> outlineRoots) {
		
		Map<TitleBlock, Bookmark> titleBookmarMap = calcLevelAligns(titleInfoList);
		
		List<TitleBlock> validTitleInfo = new ArrayList<>();
		for(TitleBlock titleInfo: titleInfoList) {
			if(!titleBookmarMap.containsKey(titleInfo)) continue;
			
	    	validTitleInfo.add(titleInfo);
		}
		
		for(int idx=0; idx<validTitleInfo.size(); ++idx) {
			TitleBlock titleInfo = validTitleInfo.get(idx);
			
			Bookmark bookmarkInfo = titleBookmarMap.get(titleInfo);
	    	String title = bookmarkInfo.getTitle();
	    	int page = bookmarkInfo.getPage();
	    	int level = bookmarkInfo.getLevel();
	    	
	    	if(page < 1 || page > document.getNumberOfPages()) {
		    	// use the page of following titles
		    	int nextPage = -1;
				if(idx + 1 < validTitleInfo.size()) {
					for(int j = idx+1; j<validTitleInfo.size(); ++j) {
						Bookmark nextBookmarkInfo = titleBookmarMap.get(validTitleInfo.get(j));
						nextPage = nextBookmarkInfo.getPage();
						if(nextPage > 0 
								&& nextPage > prePage 
								&& nextPage < document.getNumberOfPages()) {
							break;
						}
					}
				}
				
	    		page = nextPage;
	    	}
	    	
	    	System.out.println(titleInfo.getText() + " -> " 
	    			+ "title: " + title + ", " 
	    			+ "level: " + level + ", " 
	    			+ "extracted page: " + bookmarkInfo.getPage() + ", " 
	    			+ "calcuated page: " + page + ", " 
	    			+ "offset page: " + (page + pageOffset));
	    	
	    	// grow outlineRoots
	    	if(level >= outlineRoots.size()) {
	    		while(level >= outlineRoots.size()) {
	    			outlineRoots.add(lastOutline);
	    		}
	    	}
	    	
	    	// detect the parent of current outline
	    	PdfOutline parentOutline;
	    	if(level == lastLevel) {
	    		parentOutline = lastOutline.getParent();
	    	}
	    	else if(level - lastLevel > 1) {
		    	int calcLevel = level;
		    	while(calcLevel - lastLevel > 1) {
		    		--calcLevel;
		    	}
		    	parentOutline = outlineRoots.get(calcLevel - 1);
	    	}
	    	else {
	    		parentOutline = outlineRoots.get(level - 1);
	    	}
	    	
//	    	PdfOutline curOutline = parentOutline.addOutline(title);
//	    	curOutline.addAction(PdfAction.createGoTo(PdfExplicitDestination.createFitH(document.getPage(page + pageOffset), 0)));
	    	
	    	PdfOutline curOutline = addOutline(parentOutline, title, page + pageOffset);
	    	
	    	// update outline root records
	    	if(level < outlineRoots.size()) {
	    		outlineRoots.set(level, curOutline);
	    	}
	    	else {
	    		outlineRoots.add(curOutline);
	    	}
	    	
	    	updateState(page, curOutline, level);
		}
	}
	
	private PdfOutline addOutline(PdfOutline parentOutline, String title, int page) {
    	PdfOutline curOutline = parentOutline.addOutline(title);
    	curOutline.addAction(PdfAction.createGoTo(PdfExplicitDestination.createFitH(document.getPage(page-1), 0)));
    	return curOutline;
	}
	
	private void updateState(int prePage, PdfOutline lastOutline, int lastLevel) {
		this.prePage = prePage;
		this.lastOutline = lastOutline;
		this.lastLevel = lastLevel;
	}
	
	
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
			Bookmark bookmarkInfo = new Bookmark(titleInfo);
			
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
			
			// must have 2 titles at least locating at x to add a level of bookmarks
			if(tmpMap.size() > 1) {
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
