package com.google.drawingtool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class Painter extends View {

    public ViewGroup.LayoutParams params;
    private Path path = new Path();
    private Paint brush = new Paint();

    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    public Canvas myCanvas;

    public boolean somethingDrawn = false;
    private Bitmap bitmap;

    private float minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
    private float maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

    public Painter(Context context, float brushSize) {
        super(context);
        this.setDrawingCacheEnabled(true);

        brush.setAntiAlias(true);
        brush.setColor(Color.BLACK);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeWidth(brushSize);

        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(bitmap);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float positionX = event.getX();
        float positionY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                path.moveTo(positionX, positionY);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                somethingDrawn = true;
                updateExtremeBounds(positionX, positionY);
                path.lineTo(positionX, positionY);
                break;
            }
            default:
                return false;
        }
        postInvalidate();
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(path, brush);
    }


    public Bitmap getBitmap() {
        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);

        return bmp;
    }

    public Bitmap getOnlyDrawingBitmap() {
        Bitmap b = getBitmap();
        int x = (int) minX;
        int y = (int) minY;
        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);

        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;

        if (height > b.getHeight() - y)
            height = b.getHeight() - y;

        if (width > b.getWidth() - x)
            width = b.getWidth() - x;

        Bitmap newBitmap = Bitmap.createBitmap(b, x, y, width, height);
        return newBitmap;
    }

    public Bitmap normalizeImage(Bitmap b) {
        int height = b.getHeight(), width = b.getWidth();
        if (height > width)
            width = height;
        else
            height = width;

        Bitmap normalized = Bitmap.createScaledBitmap(b, width, height, true);
        return normalized;
    }

    public Bitmap resizeImage(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);

    }

    public void clear() {
        path.reset();
        dispatchTouchEvent(getSmallMotionEvent());
        maxY = maxX = Integer.MIN_VALUE;
        minY = minX = Integer.MAX_VALUE;
        somethingDrawn = false;
    }

    private MotionEvent getSmallMotionEvent() {
        long downTime = SystemClock.uptimeMillis() - 100;
        long eventTime = SystemClock.uptimeMillis();
        float x = 0.0f;
        float y = 0.0f;

        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN,
                x, y, metaState);

        return motionEvent;
    }

    private void updateExtremeBounds(float x, float y) {
        if (x > maxX)
            maxX = x;
        if (x < minX)
            minX = x;
        if (y > maxY)
            maxY = y;
        if (y < minY)
            minY = y;
    }
}
