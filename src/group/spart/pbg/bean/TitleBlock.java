package group.spart.pbg.bean;

import java.util.Objects;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;

/** 
 * Calculated text title block in a PDF page
 * 
 * @author megre
 * @email renhao.x@seu.edu.cn
 * @version created on: Dec 15, 2020 10:03:36 AM 
 */
public class TitleBlock {
	private Rectangle rect;
	private String text;
	private float averageHeight;
	private PdfFont font;
	private float fontSize;
	private int locationPage;
	
	public TitleBlock(Rectangle rect, String text, PdfFont font, float fontSize, int page) { 
		this.rect = rect;
		this.text = text; 
		this.font = font;
		this.fontSize = fontSize;
		this.locationPage = page;
		averageHeight = rect.getHeight();
	}
	
	public Rectangle getRect() {
		return rect;
	}

	public String getText() {
		return text;
	}

	public float getAverageHeight() {
		return averageHeight;
	}

	public PdfFont getFont() {
		return font;
	}

	public float getFontSize() {
		return fontSize;
	}
	
	public void appendText(String text) {
		this.text += text;
	}
	
	public void udpateAverageHeight(float height) {
		averageHeight = (averageHeight + height)/2.0f;
	}
	
	@Override
	public String toString() {
		return String.format("\n{%s, page: %d, rect: %.2f %.2f %.2f %.2f, avgH: %.2f}", 
				text, locationPage, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), averageHeight);
	}
	
	@Override
	public boolean equals(Object object) {
		if(! (object instanceof TitleBlock)) return false;
		TitleBlock other = (TitleBlock) object;
		return Objects.equals(rect, other.rect)
				&& Objects.equals(text, other.text)
				&& averageHeight == other.averageHeight
				&& Objects.equals(font, other.font)
				&& fontSize == other.fontSize;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 37 + rect.hashCode();
		result = result * 37 + text.hashCode();
		result = result * 37 + Float.floatToIntBits(averageHeight);
		result = result * 37 + font.hashCode();
		result = result * 37 + Float.floatToIntBits(fontSize);
		result = result * 37 + locationPage;
		return result;
	}
}
