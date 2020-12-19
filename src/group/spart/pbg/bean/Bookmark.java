package group.spart.pbg.bean;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 15, 2020 10:03:00 AM 
 */
public class Bookmark {
	private String title;
	private int page;
	private int level = 1;
	private TitleBlock titleInfo;
	
	public String getTitle() {
		return title;
	}

	public int getPage() {
		return page;
	}

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public Bookmark(TitleBlock titleInfo) {
		page = extractPage(titleInfo.getText());
		title = trimTitle(titleInfo);
	}
	
	public boolean isValid() {
		return title.length() > 0;
	}
	
	private String trimTitle(TitleBlock titleInfo) {
		String title = titleInfo.getText();
		String[] patterns = new String[] {
			" (?= )",
			"[^" + Constants.AllAscii + Constants.ZhCn + Constants.Punctuation + " ]", // non-ASCII, non-ZH-CN
			"[" + Constants.Punctuation + "]{3,}",
//			"[" + punctuation + "](\\w)(\\1\\1|\\1)+[" + punctuation + "]",
			"(^ *)|( *$)",
			"(?<!^Chapter ?)0*" + page + " *$",
			"[" + Constants.NonEndPunctuation + " ]+$",
			"^ *\\d+ *$",
			"^[" + Constants.Punctuation + " ]*[" + Constants.AllAscii + Constants.ZhCn + "][" + Constants.Punctuation + " ]*$"
		};
		
		for(String pattern: patterns) {
			title = title.replaceAll(pattern, "");
		}
		
		return title.trim();
	}
	
	private int extractPage(String title) {
		final Pattern pattern = Pattern.compile("^.*[^\\d]+(\\d+) *$");
		Matcher matcher = pattern.matcher(title);
		if(matcher.matches()) {
			try{
				int page = Integer.parseInt(matcher.group(1));
				if(page > 0)
					return page;
				}
			catch(NumberFormatException e) {
				
			}
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return "\n{" + title + ", " + page + ", " + level + "}";
	}
	
	@Override
	public boolean equals(Object object) {
		if(! (object instanceof Bookmark)) return false;
		Bookmark other = (Bookmark) object;
		return Objects.equals(title, other.title)
				&& page == other.page
				&& level == other.level
				&& titleInfo == other.titleInfo;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 37 + title.hashCode();
		result = result * 37 + page;
		result = result * 37 + level;
		result = result * 37 + titleInfo.hashCode();
		return result;
	}
}