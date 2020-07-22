package com.fzm.chat33.qrcode.view;

import android.content.Intent;
import android.os.Handler;

import com.google.zxing.Result;

/**
 * @author zhengjy
 * @since 2019/03/28
 * Description:
 */
public interface IQRCodeView {

    Handler getHandler();

    void handleDecode(Result result);

    void finish();

    void setResult(int code, Intent data);

    void drawViewfinder();

    ViewfinderView getViewfinderView();

    void startActivity(Intent intent);
}
