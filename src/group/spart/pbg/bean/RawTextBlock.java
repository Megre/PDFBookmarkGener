package group.spart.pbg.bean;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;

/** 
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 19, 2020 2:10:07 PM 
 */
public class RawTextBlock {
	private Rectangle rect;
	private String text;
	private PdfFont font;
	private float fontSize;
	
	public RawTextBlock(String text, Rectangle rect, PdfFont font, float fontSize) {
		this.rect = rect;
		this.text = text;
		this.font = font;
		this.fontSize = fontSize;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the font
	 */
	public PdfFont getFont() {
		return font;
	}

	/**
	 * @return the fontSize
	 */
	public float getFontSize() {
		return fontSize;
	}

	/**
	 * @return the rect
	 */
	public Rectangle getRect() {
		return rect;
	}
	
}
