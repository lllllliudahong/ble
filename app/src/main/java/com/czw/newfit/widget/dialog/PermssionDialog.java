package com.czw.newfit.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.czw.newfit.R;


public class PermssionDialog extends Dialog {
    private TextView tvContent;
    private TextView tvConfirm;

    public PermssionDialog(Context context) {
        super(context, R.style.my_dialog);
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_permssion, null);
        tvContent = view.findViewById(R.id.tv_content);
        tvConfirm = view.findViewById(R.id.tvConfirm);
        setContentView(view);
        tvConfirm.setOnClickListener(v -> {
            if (isShowing())
                dismiss();
            if (itemClickListener != null) {
                itemClickListener.onItemClick(-1);
            }
        });




    }

    public void setContent(String content) {
        tvContent.setText(content);
    }

    private onItemClickListener itemClickListener;

    public void setOnItemClickListener(onItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface onItemClickListener {
        void onItemClick(int position);
    }
}
