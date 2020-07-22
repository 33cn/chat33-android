package com.fuzamei.componentservice.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fuzamei.common.utils.ImageUtils;
import com.fuzamei.componentservice.R;
import com.fuzamei.componentservice.config.AppConfig;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoFileObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author zhengjy
 * @since 2018/11/12
 * Description:
 */
public enum WeChatHelper {

    /**
     * 单例
     */
    INS;

    // 聊天界面
    public static final int SESSION = 1;
    // 朋友圈
    public static final int TIMELINE = 2;
    // 微信收藏
    public static final int FAVORITE = 3;

    private Context context;
    public IWXAPI api;

    public void init(Context context) {
        init(context, AppConfig.WX_APP_ID);
    }

    public void init(Context context, String appId) {
        this.context = context;
        // 注册appId到微信
        api = WXAPIFactory.createWXAPI(context, appId, true);
        api.registerApp(appId);
    }

    public void shareWeb(String url, String title, String content, int shareType) {
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = url;

        WXMediaMessage msg = new WXMediaMessage(webPage);
        msg.title = title;
        msg.description = content;
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_chat_square);
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("shareWeb");
        req.message = msg;
        if (shareType == SESSION) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        } else  if (shareType == TIMELINE) {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        } else  if (shareType == FAVORITE) {
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }
        api.sendReq(req);
    }

    public void shareText(String text, int shareType) {
        WXTextObject textObject = new WXTextObject();
        textObject.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObject;
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        if (shareType == SESSION) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        } else  if (shareType == TIMELINE) {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        } else  if (shareType == FAVORITE) {
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }
        api.sendReq(req);
    }

    public void shareFile(String fileName, String path, int shareType) {
        WXFileObject fileObject = new WXFileObject();
        fileObject.fileData = getFileBytes(path);
        fileObject.filePath = path;

        WXMediaMessage msg = new WXMediaMessage(fileObject);
        msg.title = fileName;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("file");
        req.message = msg;
        if (shareType == SESSION) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        } else  if (shareType == FAVORITE) {
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }
        api.sendReq(req);
    }

    // 不能使用
    @Deprecated
    public void shareVideoFile(String path, int shareType) {
        WXVideoFileObject videoFileObject = new WXVideoFileObject();
        videoFileObject.filePath = path;

        WXMediaMessage msg = new WXMediaMessage(videoFileObject);
        msg.title = "video";

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        if (shareType == SESSION) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        } else  if (shareType == TIMELINE) {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        } else  if (shareType == FAVORITE) {
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }
        api.sendReq(req);
    }

    public void shareImage(Bitmap bitmap, int shareType) {
        WXImageObject imgObj = new WXImageObject(bitmap);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // 设置缩略图
        float scale = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        int width = (int) (200 * scale) > 200 ? 200 : (int) (200 * scale);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, width, 200, true);
        msg.thumbData = ImageUtils.bmpToByteArray(thumbBmp, true);
        // TODO:thumbData长度不能大于32768,否则微信不能分享

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        if (shareType == SESSION) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
        } else  if (shareType == TIMELINE) {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        } else  if (shareType == FAVORITE) {
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }
        api.sendReq(req);
    }

    private String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private byte[] getFileBytes(String filePath){
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
