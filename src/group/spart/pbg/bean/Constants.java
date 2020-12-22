package group.spart.pbg.bean;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 15, 2020 11:23:25 AM 
 */
public class Constants {
	public static final float AlignOffset = 2.0f,
			HeightOffset = 3.0f,
			SubLevelOffset = 10.0f;
	
	public static final String AllAscii = "\\x21-\\x7e",
			AsciiPunctuation = "\\x21-\\x2f\\x3a-\\x40\\x5b-\\x60\\x7b-\\x7e",
			ZhCn = "\\u4e00-\\u9fa5\\u3400-\\u4db5",
			ZhCnPunctuation = "\\u3002\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\uff1f\\u300a\\u300b",
			Punctuation = AsciiPunctuation + ZhCnPunctuation,
			EndPunctuation = "\\x2e\\x3f\\u3002\\uff1f", // .?。？
			NonEndPunctuation = "\\x21-\\x2d\\x2f\\x3a-\\x3e\\x40\\x5b-\\x60\\x7b-\\x7e" 
					+ "\\uff1b\\uff0c\\uff1a\\u201c\\u201d\\uff08\\uff09\\u3001\\u300a\\u300b";
}
