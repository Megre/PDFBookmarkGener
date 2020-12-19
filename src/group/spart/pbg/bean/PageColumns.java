package group.spart.pbg.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 19, 2020 7:21:36 PM 
 */
public class PageColumns {
	private Map<Integer, List<TitleBlock>> fBlocks = new HashMap<>();
	
	public List<TitleBlock> getBlockList(int columnIndex) {
		initBlockList(columnIndex);
		
		return fBlocks.get(columnIndex);
	}
	
	public void addTitleBlock(int columnIndex, TitleBlock titleBlock) {
		initBlockList(columnIndex);
		
		fBlocks.get(columnIndex).add(titleBlock);
	}
	
	public int columnSize() {
		return fBlocks.size();
	}
	
	private void initBlockList(int columnIndex) {
		if(!fBlocks.containsKey(columnIndex)) {
			fBlocks.put(columnIndex, new ArrayList<>());
		}
	}
	
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{");
		for(Map.Entry<Integer, List<TitleBlock>> entry: fBlocks.entrySet()) {
			stringBuffer.append(entry.getKey()).append(": ").append(entry.getValue().toString());
		}
		stringBuffer.append("}");
		return stringBuffer.toString();
	}
	
}
