package com.czw.newfit.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.czw.newfit.R;
import com.czw.newfit.utils.DisplayUtil;


public class CircleProgress extends View {

    private RectF rectF = new RectF();
    private Rect measureRect = new Rect();
    private float strokeWidth;
    private Paint unFinishPaint;
    private Paint finishPaint;
    private Paint stepPaint;
    private Paint titlePaint;
    private Paint innerPaint;
    private int unFinishPaintColor = getResources().getColor(R.color.deep_1);
    private int finishPaintColor = getResources().getColor(R.color.deep);
    private int titlePaintColor = getResources().getColor(R.color.grey);
    private int innerPaintColor = Color.rgb(222, 222, 222);
    private int currentSteps = 0;
    private int targetSteps = 0;
    private String titleString = "步数";
    private String bottomString = "目标";
    private float finishSwap = 0;

    /**
     * the color of center Text
     */
    private int mCenterTextColor = getResources().getColor(R.color.deep);

    public void setBottomString(String bottomString) {
        this.bottomString = bottomString;
    }

    public int getCurrentSteps() {
        return currentSteps;
    }

    public void setCurrentSteps(int currentSteps) {
        this.currentSteps = currentSteps;
        if (targetSteps == 0) {
            finishSwap = 270;
            return;
        }
        if (targetSteps <= currentSteps) {
            finishSwap = 270;
        } else {
            finishSwap = currentSteps / (targetSteps * 1.0f) * 270;
        }

        invalidate();
    }

    public int getTargetSteps() {
        return targetSteps;
    }

    public void setTargetSteps(int targetSteps) {
        this.targetSteps = targetSteps;
        if (targetSteps <= currentSteps) {
            finishSwap = 270;
        } else {
            finishSwap = currentSteps / (targetSteps * 1.0f) * 270;
        }

        invalidate();
    }

    private float default_within_padding = DisplayUtil.dip2px(getContext(), 40);

    public CircleProgress(Context context) {
        super(context);
        obtainAttrs(context, null);
        initPaint(context);
    }

    private void obtainAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress);
        mCenterTextColor = typedArray.getColor(R.styleable.CircleProgress_centerTextColor, getResources().getColor(R.color.deep));
        unFinishPaintColor = typedArray.getColor(R.styleable.CircleProgress_unFinishColor, getResources().getColor(R.color.deep_1));
        finishPaintColor = typedArray.getColor(R.styleable.CircleProgress_finishColor, getResources().getColor(R.color.deep));
        currentSteps = typedArray.getColor(R.styleable.CircleProgress_steps, 0);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        obtainAttrs(context, attrs);
        initPaint(context);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainAttrs(context, attrs);
        initPaint(context);
    }

    private void initPaint(Context context) {
        strokeWidth = DisplayUtil.dp2px(context, 10);
        unFinishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unFinishPaint.setColor(unFinishPaintColor);
        unFinishPaint.setStrokeWidth(strokeWidth);
        unFinishPaint.setStyle(Paint.Style.STROKE);
        unFinishPaint.setStrokeCap(Paint.Cap.ROUND);


        finishPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        finishPaint.setColor(finishPaintColor);
        finishPaint.setStrokeWidth(strokeWidth);
        finishPaint.setStyle(Paint.Style.STROKE);
        finishPaint.setStrokeCap(Paint.Cap.ROUND);

        stepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stepPaint.setColor(mCenterTextColor);
        stepPaint.setTextSize(DisplayUtil.dp2px(context, 40));

        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setColor(titlePaintColor);
        titlePaint.setTextSize(DisplayUtil.dp2px(context, 12));

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setColor(innerPaintColor);

        innerPaint.setStrokeWidth(DisplayUtil.dip2px(context, 2));
        innerPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        rectF.set(strokeWidth / 2f, strokeWidth / 2f, width - strokeWidth / 2f, MeasureSpec.getSize(heightMeasureSpec) - strokeWidth / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackgroundCircle(canvas);
        drawFinish(canvas, finishSwap);
        drawCenterText(canvas);

    }


    private void drawCenterText(Canvas canvas) {
        String steps = String.valueOf(currentSteps);
        stepPaint.getTextBounds(steps, 0, steps.length(), measureRect);
        int width = measureRect.width();
        int height = measureRect.height();

        float center = rectF.right / 2;

        canvas.drawText(steps, center - width / 2, center + height / 2, stepPaint);


        titlePaint.setTextSize(DisplayUtil.dp2px(getContext(), 11));
        titlePaint.getTextBounds(titleString, 0, titleString.length(), measureRect);

        int bottomVertical = (int) (rectF.centerY() + (rectF.centerY() - default_within_padding) / 2);
        int topVertical = (int) (rectF.centerY() - (rectF.centerY() - default_within_padding) / 2);

        canvas.drawText(titleString, center - measureRect.width() / 2, topVertical, titlePaint);

        if (targetSteps != 0) {
            bottomString = "目标" + ":" + targetSteps;
        }

        titlePaint.setTextSize(DisplayUtil.dp2px(getContext(), 14));
        titlePaint.getTextBounds(bottomString, 0, bottomString.length(), measureRect);


        canvas.drawText(bottomString, center - measureRect.width() / 2, bottomVertical + measureRect.height() / 2, titlePaint);

        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.centerX() - default_within_padding, innerPaint);
    }

    private void drawFinish(Canvas canvas, float finishSwap) {
        canvas.drawArc(rectF, 135f, finishSwap, false, finishPaint);
    }

    private void drawBackgroundCircle(Canvas canvas) {
        canvas.drawArc(rectF, 135f, 270f, false, unFinishPaint);
    }

}
