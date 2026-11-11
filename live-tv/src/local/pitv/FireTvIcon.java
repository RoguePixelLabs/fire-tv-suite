package local.pitv;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Compact orange TV with a flame, drawn at screen resolution. */
final class FireTvIcon extends Drawable {
 final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
 public void draw(Canvas canvas){Rect b=getBounds();canvas.save();canvas.translate(b.left,b.top);canvas.scale(b.width()/32f,b.height()/32f);
 paint.setColor(0xffff9234);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);canvas.drawRoundRect(2,5,30,25,4,4,paint);canvas.drawLine(11,29,21,29,paint);canvas.drawLine(16,25,16,29,paint);
 paint.setStyle(Paint.Style.FILL);Path flame=new Path();flame.moveTo(17,8);flame.cubicTo(18,13,23,13,22,18);flame.cubicTo(21,24,10,24,10,17);flame.cubicTo(10,14,13,13,13,11);flame.cubicTo(14,12,14,14,15,14);flame.cubicTo(17,13,15,11,17,8);flame.close();canvas.drawPath(flame,paint);
 paint.setColor(0xffffdc80);canvas.drawOval(14,16,19,22,paint);canvas.restore();}
 public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
 public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
 public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
