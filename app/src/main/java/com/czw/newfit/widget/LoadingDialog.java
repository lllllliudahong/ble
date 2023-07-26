package com.czw.newfit.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czw.newfit.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.Circle;

/**
 * 加载框
 */
public class LoadingDialog extends Dialog {

    private LinearLayout llLoadgingBg;
    private TextView tvLoading;
    private SpinKitView progressBar;
    private Context context;
    private boolean cancelable = false;
    private boolean cancledOnTouchOutside = false;

    public LoadingDialog(Context context) {
        this(context, R.style.LoadingDialog);
    }

//    protected LoadingDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
//        this(context, R.style.LoadingDialog);
//        setCancelable(false);
//        setOnCancelListener(cancelListener);
//    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initDialog();
    }

    private void initDialog() {
        setContentView(R.layout.dialog_loading);
        getWindow().getAttributes().gravity = Gravity.CENTER;
        progressBar = findViewById(R.id.loading);
        Circle doubleBounce = new Circle();
        progressBar.setIndeterminateDrawable(doubleBounce);
        tvLoading = findViewById(R.id.loadingText);
        llLoadgingBg = findViewById(R.id.llLoadgingBg);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

//        setOnShowListener(new OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//
//                progressBar.setVisibility(View.VISIBLE);
//            }
//        });
//
//        setOnDismissListener(new OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                if (isCancelable()){
//                    return;
//                }
//                progressBar.setVisibility(View.GONE);
//            }
//        });



    }

    /**
     * 设置提示语：小于六个字比较好
     */
    public void setContent(String content) {
        if (tvLoading != null) {
            tvLoading.setText(content);
        }
        tvLoading.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
    }

    /**
     * 设置提示语颜色
     */
    public void setContentColor(int color) {
        if (tvLoading != null) {
            tvLoading.setTextColor(context.getResources().getColor(color));
        }
    }

    /**
     * 设置提示语字体大小
     */
    public void setContentSize(float size) {
        if (tvLoading != null) {
            tvLoading.setTextSize(size);
        }
    }

    /**
     * 设置加载框背景
     */
    public void setLoadingBg(int bg_drawable) {
        if (llLoadgingBg != null) {
            llLoadgingBg.setBackgroundResource(bg_drawable);
        }
    }

    public boolean isCancelable() {
        return cancelable;
    }

    /**
     * 设置能否点击返回键消失
     */
//    @Override
//    public void setCancelable(boolean cancelable) {
//        this.cancelable = false;
//    }

    public boolean isCancledOnTouchOutside() {
        return cancledOnTouchOutside;
    }

    /**
     * 设置能否点击加载框外部消失
     */
//    public void setCancledOnTouchOutside(boolean cancledOnTouchOutside) {
//        this.cancledOnTouchOutside = false;
//    }
}
