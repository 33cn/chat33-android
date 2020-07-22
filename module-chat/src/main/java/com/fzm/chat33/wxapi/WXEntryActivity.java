package com.fzm.chat33.wxapi;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.R;
import com.fuzamei.componentservice.helper.WeChatHelper;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/**
 * @author zhengjy
 * @since 2018/11/12
 * Description:
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_color_primary), 0);
        BarUtils.setStatusBarLightMode(this, true);

        boolean result = WeChatHelper.INS.api.handleIntent(getIntent(), this);
        if (!result) {
            ShowUtils.showToast(this, R.string.chat_error_share_param);
            finish();
        }
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case 0:
                ShowUtils.showToastNormal(this, R.string.chat_share_success);
                finish();
                break;
            case -2:
                ShowUtils.showToastNormal(this, R.string.chat_share_cancel);
                finish();
                break;
            case -1:
            case -3:
            case -4:
            case -5:
            case -6:
            default:
                ShowUtils.showToast(this, R.string.chat_share_fail);
                finish();
                break;
        }
    }
}
