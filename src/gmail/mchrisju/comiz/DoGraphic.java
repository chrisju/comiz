package gmail.mchrisju.comiz;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class DoGraphic {

	public static void drawSeperator(Bitmap bm) {
		Canvas c = new Canvas(bm);
		Paint p = new Paint();

		p.setARGB(255, 51, 181, 229);
		p.setStyle(Style.FILL);
		// p.setColor(Color.argb(255, 51, 181, 229));
		c.drawRect(new RectF(0, 0, bm.getWidth(), bm.getHeight()), p);
	}

	public static void drawTip(Bitmap bm, int color) {
		Canvas c = new Canvas(bm);
		Paint p = new Paint();

		RectF rect = new RectF(0, 0, bm.getWidth(), bm.getHeight());

		// 绘制提示符
		p.setColor(color);
		// p.setARGB(55, 188, 188, 188);
		p.setStyle(Style.FILL_AND_STROKE);
		c.drawOval(rect, p);
		// if ((color & 0xff000000) != 0xff000000) {
		float offset = bm.getWidth() / 6;
		RectF rect2 = new RectF(offset, offset, bm.getWidth() - offset,
				bm.getHeight() - offset);
		color = color - 0x555555;
		p.setColor(color);
		// p.setARGB(55, 188, 188, 188);
		p.setStyle(Style.FILL_AND_STROKE);
		c.drawOval(rect2, p);
		// }
		// 绘制提示符边框
		// p.setARGB(88, 99, 99, 99);
		// p.setStyle(Style.STROKE);
		// c.drawOval(rect, p);
	}

}
