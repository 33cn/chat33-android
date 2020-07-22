package com.fzm.push;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.componentservice.app.RouterHelper;
import com.fzm.chat33.core.global.UserInfo;
import com.google.gson.Gson;
import com.umeng.message.UmengNotifyClickActivity;
import com.umeng.message.entity.UMessage;

import org.android.agoo.common.AgooConstants;

/**
 * @author zhengjy
 * @since 2019/08/13
 * Description:厂商渠道通知
 */
public class SystemPushActivity extends UmengNotifyClickActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_system_push);
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.app_color_primary), 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    @Override
    public void onMessage(Intent intent) {
        super.onMessage(intent);
        final String body = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        UMessage message = new Gson().fromJson(body, UMessage.class);
        String targetId = message.extra.get("targetId");
        int channelType = Integer.valueOf(message.extra.get("channelType"));
        Intent i = new Intent();
        if (!UserInfo.getInstance().isLogin()) {
            Uri uri = Uri.parse(RouterHelper.APP_LINK + "?type=chatNotification&channelType=" + channelType + "&targetId=" + targetId);
            i.setComponent(new ComponentName(getPackageName(), "com.fzm.chat33.main.activity.MainActivity"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("route", uri);
        } else {
            i.setComponent(new ComponentName(getPackageName(), "com.fzm.chat33.main.activity.ChatActivity"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("channelType", channelType);
            i.putExtra("targetId", targetId);
        }
        startActivity(i);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
