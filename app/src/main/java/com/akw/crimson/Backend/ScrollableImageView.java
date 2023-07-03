package com.akw.crimson.Backend;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
public class ScrollableImageView extends androidx.appcompat.widget.AppCompatImageView {
    private float startX;
    private float startY;
    private float previousX;
    private float previousY;
    private RectF imageRect;
    private float scaleFactor = 1.0f;
    private float previousScaleFactor = 1.0f;
    private float maxScaleFactor = 3.0f;
    private float minScaleFactor = 0.5f;

    public ScrollableImageView(Context context) {
        super(context);
        init();
    }

    public ScrollableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageRect == null) {
            imageRect = new RectF(0, 0, getImageWidth(), getImageHeight());
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                previousX = startX;
                previousY = startY;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                previousScaleFactor = scaleFactor;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    float currentX0 = event.getX(0);
                    float currentY0 = event.getY(0);
                    float currentX1 = event.getX(1);
                    float currentY1 = event.getY(1);

                    float currentDistance = calculateDistance(currentX0, currentY0, currentX1, currentY1);
                    float previousDistance = calculateDistance(previousX, previousY, currentX0, currentY0);

                    scaleFactor = previousScaleFactor * (currentDistance / previousDistance);
                    scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));

                    float focusX = (currentX0 + currentX1) / 2.0f;
                    float focusY = (currentY0 + currentY1) / 2.0f;

                    Matrix matrix = getImageMatrix();
                    matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                    setImageMatrix(matrix);
                } else {
                    float currentX = event.getX();
                    float currentY = event.getY();

                    float dx = currentX - previousX;
                    float dy = currentY - previousY;

                    int scrollX = getScrollX();
                    int scrollY = getScrollY();

                    boolean shouldScroll = false;

                    if (dx > 0 && scrollX - dx < imageRect.left) {
                        dx = scrollX - imageRect.left;
                        shouldScroll = true;
                    } else if (dx < 0 && scrollX + getWidth() - dx > imageRect.right) {
                        dx = scrollX + getWidth() - imageRect.right;
                        shouldScroll = true;
                    }

                    if (dy > 0 && scrollY - dy < imageRect.top) {
                        dy = scrollY - imageRect.top;
                        shouldScroll = true;
                    } else if (dy < 0 && scrollY + getHeight() - dy > imageRect.bottom) {
                        dy = scrollY + getHeight() - imageRect.bottom;
                        shouldScroll = true;
                    }

                    if (shouldScroll) {
                        scrollBy((int) -dx, (int) -dy);
                    }

                    previousX = currentX;
                    previousY = currentY;
                }
                break;
        }
        return true;
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int getImageWidth() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }

    private int getImageHeight() {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }
}
