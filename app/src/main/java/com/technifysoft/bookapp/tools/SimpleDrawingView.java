package com.technifysoft.bookapp.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimpleDrawingView extends View {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private Paint drawPaint;
    private Paint circlePaint;
    private Path circlePath;
    private boolean eraserOn = false;
    private boolean newAdded = false;
    private boolean allClear = false;
    private Path drawPath;

    private ArrayList<Bitmap> bitmap = new ArrayList<>();
    private ArrayList<Bitmap> undoBitmap = new ArrayList<>();

    public SimpleDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        drawPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);

        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setColor(Color.BLACK);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.w("Paint", "onSizeChanged: " + w + "/" + h);

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(drawPath, drawPaint);
        canvas.drawPath(circlePath, circlePaint);
    }

    /**
     * Toggle erase function
     * @return :
     */
    public boolean onClickEraser() {
        if (eraserOn) {
            eraserOn = false;
            drawPaint.setColor(Color.BLACK);
            drawPaint.setXfermode(null);
            drawPaint.setStrokeWidth(5);
        } else {
            eraserOn = true;
            drawPaint.setColor(getResources().getColor(android.R.color.transparent));
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            drawPaint.setStrokeWidth(25);
        }
        return eraserOn;
    }

    /**
     * Undo if erase
     */
    public void onClickUndo() {
        if (newAdded) {
            bitmap.add(mBitmap.copy(mBitmap.getConfig(), mBitmap.isMutable()));
            newAdded = false;
        }
        if (bitmap.size() > 1) {
            undoBitmap.add(bitmap.remove(bitmap.size() - 1));
            mBitmap = bitmap.get(bitmap.size() - 1).copy(mBitmap.getConfig(), mBitmap.isMutable());
            mCanvas = new Canvas(mBitmap);
            invalidate();
            if (bitmap.size() == 1) {
                allClear = true;
            }
        }
    }

    /**
     * Redo before action
     */
    public void onClickRedo() {
        if (undoBitmap.size() > 0) {
            bitmap.add(undoBitmap.remove(undoBitmap.size() - 1));
            mBitmap = bitmap.get(bitmap.size() - 1).copy(mBitmap.getConfig(), mBitmap.isMutable());
            mCanvas = new Canvas(mBitmap);
            invalidate();
        }
    }

    /**
     * Xóa toàn bộ màn hình
     */
    public void onClickClearAll() {
        try {
            drawPath.reset();
            circlePath.reset();
            bitmap.clear();
            undoBitmap.clear();
            //Erase bitmap
            mBitmap.eraseColor(Color.TRANSPARENT);
            postInvalidate();   // indicate view should be redrawn
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap onClickSearch() {
        return mBitmap;
    }

    public void onClickExit() {
        try {
            drawPath.close();
            circlePath.close();
            bitmap.clear();
            bitmap = null;
            undoBitmap.clear();
            undoBitmap = null;

            //release bitmap
            mBitmap.recycle();
            mBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                newAdded = true;
                if (!allClear)
                    bitmap.add(mBitmap.copy(mBitmap.getConfig(), mBitmap.isMutable()));
                else
                    allClear = false;
                drawPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (eraserOn) {
                    drawPath.lineTo(touchX, touchY);
                    mCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    drawPath.moveTo(touchX, touchY);
                } else {
                    drawPath.lineTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;

            case MotionEvent.ACTION_CANCEL:
                return false;

            default:
                return false;
        }
        invalidate();
        return true;
    }
}
