package com.fzm.chat33.core.net;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.baidu.crabsdk.CrabSDK;
import com.fuzamei.common.net.rxjava.ApiException;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.config.AppPreference;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.core.logic.MessageDispatcher;
import com.fzm.chat33.core.manager.MessageManager;
import com.fzm.chat33.core.net.socket.ChatSocket;
import com.fzm.chat33.core.net.socket.ChatSocketListener;
import com.fzm.chat33.core.net.socket.SocketState;
import com.fzm.chat33.core.net.socket.SocketStateChangeListener;
import com.fzm.chat33.core.request.SyncMessageRequest;
import com.fzm.chat33.core.response.MsgSocketResponse;
import com.fzm.chat33.core.utils.UserInfoPreference;
import com.fuzamei.componentservice.config.AppConfig;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.fzm.chat33.core.consts.SocketCode.SOCKET_OTHER_LOGIN;
import static com.fzm.chat33.core.consts.SocketCode.SOCKET_OTHER_UPDATE_WORDS;
import static com.fzm.chat33.core.net.socket.SocketState.CONNECTING;
import static com.fzm.chat33.core.net.socket.SocketState.DISCONNECTED;
import static com.fzm.chat33.core.net.socket.SocketState.ESTABLISHED;
import static com.fzm.chat33.core.net.socket.SocketState.INITIAL;

/**
 * 聊天通信的WebSocket实现
 */
public class WebSocketService implements ChatSocket, SocketStateChangeListener {

    private final static int MSG_CALL_CALLBACK = 1;
    private final static int OPEN_CALLBACK = 2;
    private final static int CLOSE_CALLBACK = 3;

    private WebSocket mWebSocket;
    private OkHttpClient client;

    private AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    private AtomicBoolean init = new AtomicBoolean(true);
    @SocketState.State
    private int socketState = DISCONNECTED;
    private int reconnectTimes = 0;

    private List<ChatSocketListener> mChatSocketListener = new LinkedList<>();
    private List<SocketStateChangeListener> mListener = new LinkedList<>();

    @Override
    public void register(@NotNull ChatSocketListener listener) {
        if (!mChatSocketListener.contains(listener)) {
            mChatSocketListener.add(listener);
        }
    }

    @Override
    public void unregister(@NotNull ChatSocketListener listener) {
        mChatSocketListener.remove(listener);
    }

    @Override
    public void addSocketStateChangeListener(@NotNull SocketStateChangeListener listener) {
        if (!mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    @Override
    public void removeSocketStateChangeListener(@NotNull SocketStateChangeListener listener) {
        mListener.remove(listener);
    }

    @Override
    public void connect() {
        socketConnect();
    }

    @Override
    public void disconnect() {
        mChatSocketListener.clear();
        mListener.clear();
        init.set(true);
        socketDisConnect();
    }

    @Override
    public void send(@NotNull String event) {
        if (!isAlive()) {
            if (mChatSocketListener.size() > 0) {
                retryConnect.run();
            }
        } else {
            sendInternal(event);
        }
    }

    @Override
    public void send(@NotNull byte[] message) {
        throw new UnsupportedOperationException("can't send byte[] data");
    }

    @Override
    public boolean isAlive() {
        return socketState == ESTABLISHED;
    }

    private void sendInternal(String event) {
        if (mWebSocket != null) {
            mWebSocket.send(event);
        }
    }

    private void socketConnect() {
        if (!UserInfo.getInstance().isLogin()) {
            return;
        }
        if (socketState == INITIAL || socketState == CONNECTING || socketState == ESTABLISHED) {
            return;
        }
        if (init.getAndSet(false)) {
            onSocketStateChange(INITIAL);
        } else {
            onSocketStateChange(CONNECTING);
        }
        shouldReconnect.set(true);
        retryConnect.run();
    }

    private void socketDisConnect() {
        shouldReconnect.set(false);
        if (mWebSocket != null) {
            mWebSocket.close(1000, "app close");
        }
        resetClient();
    }

    private final Runnable retryConnect = new Runnable() {
        @Override
        public void run() {
            if (client == null || mWebSocket == null) {
                okHttpInit();
            }
        }
    };

    @Override
    public void onSocketStateChange(@SocketState.State int state) {
        setSocketState(state);
        for (SocketStateChangeListener listener : mListener) {
            getMainHandler().post(() -> listener.onSocketStateChange(socketState));
        }
    }

    private void setSocketState(@SocketState.State int state) {
        this.socketState = state;
    }

    private WebSocketService.MainHandler getMainHandler() {
        if (mMainHandler == null) {
            mMainHandler = new WebSocketService.MainHandler(this);
        }
        return mMainHandler;
    }

    private String uuid;

    private void okHttpInit() {
        if (!UserInfo.getInstance().isLogin()) {
            return;
        }
        String cookie_session = AppPreference.INSTANCE.getSESSION_KEY();
        String token = AppPreference.INSTANCE.getTOKEN();
        if (TextUtils.isEmpty(cookie_session)) {
            cookie_session = AppConfig.CHAT_SESSION;
        }
        if (TextUtils.isEmpty(uuid)) {
            uuid = ToolUtils.getUUID();
        }
        Request request = new Request.Builder()
                .addHeader("cookie", cookie_session)
                .addHeader("FZM-AUTH-TOKEN", token)
                .addHeader("FZM-DEVICE", "Android")
                .addHeader("FZM-DEVICE-NAME", Build.MODEL)
                .addHeader("FZM-APP-ID", AppConfig.APP_ID)
                .addHeader("FZM-UUID", uuid)
                .addHeader("FZM-VERSION", ToolUtils.getVersionName())
                .url(AppConfig.SOCKET_URL)
                .build();
        client = new OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build();
        client.newWebSocket(request, webSocketListener);
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    private void resetClient() {
        onSocketStateChange(DISCONNECTED);
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client = null;
        }
        if (mWebSocket != null) {
            mWebSocket.cancel();
        }
    }

    private void startSyncMessage() {
        UserInfoPreference.getInstance().setLongPref(UserInfoPreference.SYNC_MSG_TIME, System.currentTimeMillis());
        long time = UserInfoPreference.getInstance().getLongPref(UserInfoPreference.LATEST_MSG_TIME, 0);
        SyncMessageRequest request = new SyncMessageRequest(time);
        mWebSocket.send(new Gson().toJson(request));
        // 同步标志位置为true
        MessageDispatcher.setSyncing(true);
        Log.e("WebSocketListener", "last_log_time:" + ToolUtils.formatLogTime(time));
    }

    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, okhttp3.Response response) {
            super.onOpen(webSocket, response);
            Log.e("WebSocketListener", "onOpen");
            mWebSocket = webSocket;
            onSocketStateChange(ESTABLISHED);
            resetReconnectTimes();
            // 发送本地最新消息时间，告知服务端开始同步
            startSyncMessage();
            // 重连成功后发送，短时间内发送失败的消息会重新发送
            MessageManager.resendOneMore();
            getMainHandler().removeCallbacks(retryConnect);
            getMainHandler().obtainMessage(OPEN_CALLBACK, "").sendToTarget();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.e("onMessage", text);
            try {
                MsgSocketResponse msgSocketResponse = new Gson().fromJson(text, MsgSocketResponse.class);
                getMainHandler().obtainMessage(MSG_CALL_CALLBACK, msgSocketResponse).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            Log.e("WebSocketListener", "onMessage");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            Log.e("WebSocketListener", "onClosing:" + code + ", " + reason);
            resetClient();
            getMainHandler().obtainMessage(CLOSE_CALLBACK, new Pair<>(code, reason)).sendToTarget();
            if (code == SOCKET_OTHER_LOGIN
                    || code == SOCKET_OTHER_UPDATE_WORDS) {
                // 在其他终端登录，停止重连
                shouldReconnect.set(false);
            } else {
                if (shouldReconnect.get()) {
                    tryReconnect();
                } else {
                    socketDisConnect();
                }
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.e("WebSocketListener", "onClosed:" + code + ", " + reason);
            resetClient();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
            super.onFailure(webSocket, t, response);
            Log.e("WebSocketListener", "onFailure:" + t.getMessage());
            CrabSDK.uploadException(new RuntimeException("WebSocket Exception", t));
            resetClient();
            if (shouldReconnect.get()) {
                tryReconnect();
            }
        }
    };

    private void tryReconnect() {
        reconnectTimes++;
        if (reconnectTimes < 10) {
            getMainHandler().postDelayed(retryConnect, 1_000);
        } else if (reconnectTimes < 30) {
            getMainHandler().postDelayed(retryConnect, 5_000);
        } else if (reconnectTimes < 50) {
            getMainHandler().postDelayed(retryConnect, 15_000);
        } else {
            getMainHandler().postDelayed(retryConnect, 60_000);
        }
    }

    private void resetReconnectTimes() {
        this.reconnectTimes = 0;
    }

    private MainHandler mMainHandler;

    static class MainHandler extends Handler {
        WebSocketService socket;

        MainHandler(WebSocketService socket) {
            super(Looper.getMainLooper());
            this.socket = socket;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CALL_CALLBACK:
                    MsgSocketResponse message = (MsgSocketResponse) msg.obj;
                    if (socket.mChatSocketListener != null) {
                        for (ChatSocketListener listener : socket.mChatSocketListener) {
                            listener.onCall(message);
                        }
                    }
                    break;
                case OPEN_CALLBACK:
                    if (socket.mChatSocketListener != null) {
                        for (ChatSocketListener listener : socket.mChatSocketListener) {
                            listener.onOpen();
                        }
                    }
                    break;
                case CLOSE_CALLBACK:
                    Pair pair = (Pair) msg.obj;
                    if (socket.mChatSocketListener != null) {
                        for (ChatSocketListener listener : socket.mChatSocketListener) {
                            listener.onClose(new ApiException((int) pair.first, (String) pair.second));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
