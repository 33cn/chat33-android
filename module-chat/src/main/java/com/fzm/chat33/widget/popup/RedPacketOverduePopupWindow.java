package com.fzm.chat33.widget.popup;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.fzm.chat33.R;


public class RedPacketOverduePopupWindow extends BasePopupWindow implements OnClickListener {

    public RedPacketOverduePopupWindow(Context context, View popupView) {
        super(context, popupView);
        popupView.findViewById(R.id.root).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
