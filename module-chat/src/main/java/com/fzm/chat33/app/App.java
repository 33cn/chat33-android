package com.fzm.chat33.app;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.fuzamei.componentservice.event.NewFriendRequestEvent;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class App extends Application {

    private static App INS;
    public boolean ignoreUpdate = false;
    public HashMap<String, NewFriendRequestEvent> newFriendRequest = new HashMap<>();
    public List<String> snapModeList = new ArrayList<>();

    public static App getInstance() {
        return INS;
    }

    @Override
    protected void attachBaseContext(@Nullable Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INS = this;
    }
}
