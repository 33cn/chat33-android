package com.fzm.chat33.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.fzm.chat33.R;

/**
 * @author zhengjy
 * @since 2019/11/28
 * Description:图片引导页面对话框
 */
public class ImageGuidanceDialog extends Dialog {

    public ImageGuidanceDialog(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Window window = getWindow();
        window.setBackgroundDrawableResource(R.color.chat_transparent);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        setCanceledOnTouchOutside(false);
        View container = LayoutInflater.from(context).inflate(R.layout.dialog_image_guidance, null);
        container.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContentView(container);
    }

    @Override
    public void onBackPressed() {

    }
}
