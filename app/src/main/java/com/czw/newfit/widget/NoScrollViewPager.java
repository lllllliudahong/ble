package com.czw.newfit.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * description
 * date         2018/1/15$-11:03$
 * email        cwg-1@163.com
 *
 * @author chenwenguang
 */

public class NoScrollViewPager extends ViewPager {
    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 决定事件是否中断
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 不拦截事件, 让嵌套的子viewpager有机会响应触摸事件
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 重写ViewPager滑动事件, 改为什么都不做
        int currentItem = getCurrentItem();
        if (currentItem == 0) {
            return false;
        }
        return true;

    }
}
