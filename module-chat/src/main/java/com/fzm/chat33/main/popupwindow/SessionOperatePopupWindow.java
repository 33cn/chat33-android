package com.fzm.chat33.main.popupwindow;

import android.content.Context;

import androidx.annotation.LayoutRes;

import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.global.Chat33Const;
import com.fuzamei.common.utils.RoomUtils;
import com.fzm.chat33.main.mvvm.MessageViewModel;
import com.fuzamei.componentservice.widget.dialog.DialogInterface;
import com.fuzamei.componentservice.widget.dialog.EasyDialog;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.widget.OperatePopupWindow;


public class SessionOperatePopupWindow extends OperatePopupWindow implements OnClickListener {

    private TextView top, delete, disturb, assistant;

    private MessageViewModel viewModel;
    private int channelType;
    private String id;
    private String name;
    private int sticky;
    private int dnd;
    private boolean isDeleted;

    public SessionOperatePopupWindow(Context context, MessageViewModel viewModel, View popupView, int channelType) {
        super(context, popupView);
        this.viewModel = viewModel;
        this.channelType = channelType;
        findView();
        initData();
    }

    public SessionOperatePopupWindow(Context context, @LayoutRes int resource, int channelType) {
        super(context, resource);
        this.channelType = channelType;
        findView();
        initData();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSticky(int sticky) {
        this.sticky = sticky;
    }

    public void setDnd(int dnd) {
        this.dnd = dnd;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    private void findView() {
        top = mRootView.findViewById(R.id.top);
        delete = mRootView.findViewById(R.id.delete);
        disturb = mRootView.findViewById(R.id.disturb);
        assistant = mRootView.findViewById(R.id.assistant);
        top.setOnClickListener(this);
        delete.setOnClickListener(this);
        disturb.setOnClickListener(this);
        assistant.setOnClickListener(this);

    }

    @Deprecated
    public void show(View anchor) {
        top.setText(context.getString(sticky == 1 ? R.string.chat_top_cancel : R.string.chat_top_operate));
        disturb.setText(context.getString(dnd == 1 ? R.string.chat_disturb_cancel : R.string.chat_disturb_operate));
        int[] location = computeLocation(anchor);
        showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] + ScreenUtils.dp2Px(25));
    }

    public void show(View anchor, int x, int y) {
        if (isDeleted) {
            top.setVisibility(View.GONE);
            delete.setVisibility(View.VISIBLE);
            disturb.setVisibility(View.GONE);
            assistant.setVisibility(View.GONE);
        } else {
            if (channelType == Chat33Const.CHANNEL_GROUP) {

            } else if (channelType == Chat33Const.CHANNEL_ROOM) {
                top.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                disturb.setVisibility(View.VISIBLE);
                assistant.setVisibility(View.GONE);
            } else {
                top.setVisibility(View.VISIBLE);
                delete.setVisibility(View.VISIBLE);
                disturb.setVisibility(View.VISIBLE);
                assistant.setVisibility(View.GONE);
            }
        }
        top.setText(context.getString(sticky == 1 ? R.string.chat_top_cancel : R.string.chat_top_operate));
        disturb.setText(context.getString(dnd == 1 ? R.string.chat_disturb_cancel : R.string.chat_disturb_operate));
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        // 减去的10dp为阴影的宽度
        showAtLocation(anchor, Gravity.NO_GRAVITY, x - ScreenUtils.dp2Px(10), location[1] + y - ScreenUtils.dp2Px(10));
    }

    private int[] computeLocation(View anchor) {

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        location[0] = ScreenUtils.getScreenWidth() - ScreenUtils.dp2Px(149);
        return location;
    }

    private void initData() {

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.delete) {
            String content = context.getString(R.string.chat_dialog_delete_message, AppConfig.APP_ACCENT_COLOR_STR, name);
            EasyDialog dialog = new EasyDialog.Builder()
                    .setHeaderTitle(context.getString(R.string.chat_tips_tips))
                    .setBottomLeftText(context.getString(R.string.chat_action_cancel))
                    .setBottomRightText(context.getString(R.string.chat_action_confirm))
                    .setContent(Html.fromHtml(content))
                    .setBottomLeftClickListener(null)
                    .setBottomRightClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog) {
                            dialog.dismiss();
                            RoomUtils.run(new Runnable() {
                                @Override
                                public void run() {
                                    ChatDatabase.getInstance().recentMessageDao().deleteMessage(channelType, id);
                                }
                            });
                        }
                    }).create(context);
            dialog.show();
        } else if (viewId == R.id.top) {
            viewModel.stickyOnTop(id, channelType, sticky == 1 ? 2 : 1);
        } else if (viewId == R.id.disturb) {
            viewModel.setNoDisturb(id, channelType, dnd == 1 ? 2 : 1);
        }
        dismiss();
    }
}
