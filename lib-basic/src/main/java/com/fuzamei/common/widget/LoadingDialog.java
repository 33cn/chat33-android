package com.fuzamei.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.fuzamei.commonlib.R;

/**
 * Created by xiufeng on 2018/4/23.
 */
public class LoadingDialog extends Dialog {

    protected static final String TAG = "LoadingDialog";

    private Context context;
    private boolean cancelable;

    public LoadingDialog(Context context, boolean cancelable) {
        super(context, R.style.loadingdialog);
        this.context = context;
        this.cancelable = cancelable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(cancelable);
    }
}
