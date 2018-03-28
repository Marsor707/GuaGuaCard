package com.github.marsor707.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Marsor
 * Github: https://github.com/Marsor707
 * Email: 369135912@qq.com
 */

public class GuaGuaCard extends View {

    private final ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    private Bitmap mBgBitmap, mFgBitmap;
    private Paint mPaint;
    private Canvas mCanvas;
    private Path mPath;
    private GuaGuaListener mListener;
    private int resourceId;
    private int percent;
    private boolean isFirstDraw = true;
    private boolean isComplete = false;

    public GuaGuaCard(Context context) {
        this(context, null);
    }

    public GuaGuaCard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaGuaCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GuaGuaCard);
        resourceId = a.getResourceId(R.styleable.GuaGuaCard_gg_src, R.drawable.xixi);
        percent = a.getInt(R.styleable.GuaGuaCard_gg_per, 40);
        a.recycle();
        initView();
    }


    private void initView() {
        mPaint = new Paint();
        mPaint.setAlpha(0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(50);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPath = new Path();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.reset();
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (!isComplete) {
                    mExecutors.execute(mRunnable);
                }
                break;
        }
        mCanvas.drawPath(mPath, mPaint);
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFirstDraw) {
            final int width = getWidth();
            final int height = getHeight();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            mBgBitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            mBgBitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);
            mBgBitmap = scale(mBgBitmap, width, height);
            mFgBitmap = Bitmap.createBitmap(mBgBitmap.getWidth(), mBgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mFgBitmap);
            mCanvas.drawColor(Color.GRAY);
            isFirstDraw = false;
        }
        canvas.drawBitmap(mBgBitmap, 0, 0, null);
        if (!isComplete) {
            canvas.drawBitmap(mFgBitmap, 0, 0, null);
        }
    }

    public void setGuaGuaListener(GuaGuaListener listener) {
        this.mListener = listener;
    }

    /**
     * 按宽高缩放Bitmap
     *
     * @param origin 原始Bitmap
     * @param width  需要的的宽度
     * @param height 需要的高度
     * @return 缩放后的Bitmap
     */
    private Bitmap scale(Bitmap origin, int width, int height) {
        final int oldWidth = origin.getWidth();
        final int oldHeight = origin.getHeight();
        final float scaleX = (float) width / oldWidth;
        final float scaleY = (float) height / oldHeight;
        final float finalScale = Math.min(scaleX, scaleY);
        final Matrix matrix = new Matrix();
        matrix.preScale(finalScale, finalScale);
        return Bitmap.createBitmap(origin, 0, 0, origin.getWidth(), origin.getHeight(), matrix, true);
    }

    /**
     * 计算Bitmap加载需要的采样率
     *
     * @param options   Bitmap参数
     * @param reqWidth  需要的宽度
     * @param reqHeight 需要的高度
     * @return 采样率
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //取出图片原始宽高信息
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //根据采样率规则和所需大小计算采样率
        while (height / inSampleSize >= reqHeight || width / inSampleSize >= reqWidth) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /**
     * 异步计算刮去的面积占总面积的比例
     */
    private Runnable mRunnable = new Runnable() {
        private int[] mPixels;

        @Override
        public void run() {
            final int w = mFgBitmap.getWidth();
            final int h = mFgBitmap.getHeight();
            float wipeArea = 0;
            final float totalArea = w * h;
            mPixels = new int[w * h];
            mFgBitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int per = (int) (wipeArea * 100 / totalArea);
                if (per > percent) {
                    if (mListener != null) {
                        mListener.onComplete();
                    }
                    isComplete = true;
                    postInvalidate();
                }
            }
        }
    };

    /**
     * 刮完后的回调
     */
    public interface GuaGuaListener {
        void onComplete();
    }
}