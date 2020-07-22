package com.fzm.chat33.widget.forward;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ForwardListAdapter;

/**
 * @author zhengjy
 * @since 2019/05/28
 * Description:
 */
public class ForwardRowEncrypted extends ForwardRowBase {

    private TextView tvMessage;

    public ForwardRowEncrypted(Context context, View root, ForwardListAdapter adapter) {
        super(context, root, adapter);
    }

    @Override
    protected void onFindViewById() {
        tvMessage = rootView.findViewById(R.id.tv_message);
    }

    @Override
    protected void onSetUpView() {
        tvMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        tvMessage.setText(R.string.chat_encrypt_message);
    }
}
