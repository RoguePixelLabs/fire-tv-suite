package local.pitv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.FrameLayout;

/** Masks SurfaceView corners without replacing its efficient video surface. */
final class RoundedVideoPanel extends FrameLayout {
    private final Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path corners = new Path();
    private final RectF border = new RectF();
    private final float radius;
    private final float stroke;

    RoundedVideoPanel(Context context) {
        super(context);
        radius = TvStyle.dp(context, 20);
        stroke = TvStyle.dp(context, 2);
        cornerPaint.setColor(TvStyle.BG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(stroke);
        borderPaint.setColor(0xff303e57);
    }

    void setBorderColor(int color) {
        borderPaint.setColor(color);
        invalidate();
    }

    void setMaskColor(int color) { cornerPaint.setColor(color); invalidate(); }

    @Override public boolean onInterceptTouchEvent(android.view.MotionEvent event) {
        // The embedded video surface must leave panel taps to the parent action.
        return isClickable() || super.onInterceptTouchEvent(event);
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        corners.reset();
        corners.setFillType(Path.FillType.EVEN_ODD);
        corners.addRect(0, 0, width, height, Path.Direction.CW);
        corners.addRoundRect(0, 0, width, height, radius, radius, Path.Direction.CW);
        border.set(stroke / 2, stroke / 2, width - stroke / 2, height - stroke / 2);
    }

    @Override protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // VideoView uses a separate surface: a rounded background alone does not mask it.
        canvas.drawPath(corners, cornerPaint);
        canvas.drawRoundRect(border, radius - stroke / 2, radius - stroke / 2, borderPaint);
    }
}
