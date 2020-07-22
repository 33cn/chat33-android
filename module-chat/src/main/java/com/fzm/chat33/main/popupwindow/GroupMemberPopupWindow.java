package com.fzm.chat33.main.popupwindow;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.widget.OperatePopupWindow;

import java.util.ArrayList;


public class GroupMemberPopupWindow extends OperatePopupWindow implements OnClickListener {


    private TextView add;
    private TextView set;
    private TextView delete;
    private RoomInfoBean roomInfo;
    private ArrayList<String> users = new ArrayList<>();

    public GroupMemberPopupWindow(Context context, View popupView) {
        super(context, popupView);
        findView();
        initData();
    }

    private void findView() {
        add = mRootView.findViewById(R.id.add);
        set = mRootView.findViewById(R.id.set);
        delete = mRootView.findViewById(R.id.delete);
    }

    public void setRoomInfo(RoomInfoBean roomInfo) {
        this.roomInfo = roomInfo;
        if (roomInfo.getMemberLevel() > 1) {
            set.setVisibility(View.VISIBLE);
            set.setOnClickListener(this);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(this);
        } else {
            set.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
        if (roomInfo.getJoinPermission() == 1 || roomInfo.getJoinPermission() == 2) {
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(this);
        } else if (roomInfo.getJoinPermission() == 3 && roomInfo.getMemberLevel() == 3) {
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(this);
        } else {
            add.setVisibility(View.GONE);
        }
        for (RoomUserBean userBean : roomInfo.getUsers()) {
            users.add(userBean.getId());
        }
    }

    public void show(View anchor) {
        int[] location = computeLocation(anchor);
        showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] + ScreenUtils.dp2Px(41));
    }

    private int[] computeLocation(View anchor) {
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        location[0] = ScreenUtils.getScreenWidth() - ScreenUtils.dp2Px(125);
        return location;
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.add) {
            ARouter.getInstance().build(AppRoute.CREATE_GROUP)
                    .withString("roomId", roomInfo.getId())
                    .withStringArrayList("users", users)
                    .navigation();
            dismiss();

        } else if (i == R.id.set) {
            ARouter.getInstance().build(AppRoute.ADMIN_SET)
                    .withSerializable("roomInfo", roomInfo)
                    .navigation();
            dismiss();

        } else if (i == R.id.delete) {
            ARouter.getInstance().build(AppRoute.SELECT_GROUP_MEMBER)
                    .withSerializable("roomInfo", roomInfo)
                    .withBoolean("selectable", true)
                    .withString("action", "remove")
                    .withInt("memberLevel", 1)
                    .navigation();
            dismiss();

        }
    }
}
