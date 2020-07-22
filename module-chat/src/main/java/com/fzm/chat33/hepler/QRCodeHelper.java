package com.fzm.chat33.hepler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.event.CaptureEvent;
import com.fzm.chat33.R;
import com.fzm.chat33.core.global.Chat33Const;

import org.greenrobot.eventbus.EventBus;

/**
 * @author zhengjy
 * @since 2019/05/15
 * Description:
 */
public class QRCodeHelper {

    public static void process(Context context, String result) {
        Uri uri = Uri.parse(result);
        try {
            String groupId = uri.getQueryParameter("gid");
            String friendId = uri.getQueryParameter("uid");
            if (uri.toString().contains(AppConfig.APP_SHARE_URL)) {
                if (!TextUtils.isEmpty(groupId)) {
                    ARouter.getInstance().build(AppRoute.JOIN_ROOM)
                            .withString("markId", groupId)
                            .withInt("sourceType", Chat33Const.FIND_TYPE_QR_CODE)
                            .navigation();
                } else if (!TextUtils.isEmpty(friendId)) {
                    ARouter.getInstance().build(AppRoute.USER_DETAIL)
                            .withString("userId", friendId)
                            .withBoolean("fetchInfoById", false)
                            .withInt("sourceType", Chat33Const.FIND_TYPE_QR_CODE)
                            .navigation();
                }
            } else if (uri.getScheme() != null && uri.getScheme().startsWith("http")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            } else {
                EventBus.getDefault().post(new CaptureEvent(1, result));
            }
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.chat_tips_qr_unrecongnize, result), Toast.LENGTH_SHORT).show();
        }
    }
}
