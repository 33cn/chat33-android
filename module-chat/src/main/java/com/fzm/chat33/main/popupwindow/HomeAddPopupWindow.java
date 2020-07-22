package com.fzm.chat33.main.popupwindow;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.widget.OperatePopupWindow;


public class HomeAddPopupWindow extends OperatePopupWindow implements OnClickListener {


    private TextView scan, createGroup, join;

    public HomeAddPopupWindow(Context context, View popupView) {
        super(context, popupView);
        findView();
        initData();
    }


    private void findView() {
        scan = mRootView.findViewById(R.id.scan);
        scan.setOnClickListener(this);
        createGroup = mRootView.findViewById(R.id.create_group);
        createGroup.setOnClickListener(this);
        join = mRootView.findViewById(R.id.join);
        join.setOnClickListener(this);

    }

    //    private void initAnim() {
//        Animation animation = null;
//        animation = AnimationUtils.loadAnimation(context, R.anim.popup_bottom_to_top);
//        windowLayout.startAnimation(animation);
//    }
    public void show(View anchor) {
        int[] location = computeLocation(anchor);

        showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] + ScreenUtils.dp2Px(41));
//        if (message.isSentType()) {
//            int[] location = new int[2];
//            anchor.getLocationOnScreen(location);
//
//            showAtLocation(anchor, Gravity.NO_GRAVITY, location[0], location[1] - getHeight());
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////
////
////                showAtLocation(anchor, 0, 0, Gravity.END | Gravity.CENTER_VERTICAL);
////            } else {
////                showAsDropDown(anchor);
////            }
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                showAsDropDown(anchor, 0, 0, Gravity.START | Gravity.CENTER_VERTICAL);
//            } else {
//                showAsDropDown(anchor);
//            }
//        }
    }

    private int[] computeLocation(View anchor) {

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        location[0] = ScreenUtils.getScreenWidth() - ScreenUtils.dp2Px(155);
//        MyLog.d("positionX: " + location[0] + "positionY: " + location[1]);
//        int screenHeight = MyApp.getInstance().getScreenHeight();
//        if (location[1] > (screenHeight / 2)) {
//
//        } else {
//
//
//        }
        return location;
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        dismiss();
        int i = v.getId();
        if (i == R.id.scan) {
            ARouter.getInstance().build(AppRoute.QR_SCAN).navigation();
        } else if (i == R.id.create_group) {
            ARouter.getInstance().build(AppRoute.CREATE_GROUP).navigation();
        } else if (i == R.id.join) {
            ARouter.getInstance().build(AppRoute.SEARCH_ONLINE).navigation();
        }
    }

}
