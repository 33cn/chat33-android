/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fuzamei.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {
    private static List<Activity> activityList = new ArrayList<Activity>();
    private static int activityCount = 0;

    public static void registerActivityLifecycleCallbacks(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityUtils.registerActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                ActivityUtils.activityStart();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                ActivityUtils.activityStop();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityUtils.unRegisterActivity(activity);
            }
        });
    }

    public synchronized static void registerActivity(Activity activity) {
        activityList.add(activity);
    }

    public static synchronized void unRegisterActivity(Activity activity) {
        if(activityList.size() > 0) {
            activityList.remove(activity);
            if(!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static boolean isActivityTop(Context context, Class clazz) {
        return isActivityTop(context, clazz.getName());
    }

    /**
     *
     * 判断某activity是否处于栈顶
     * @return  true在栈顶 false不在栈顶
     */
    public static boolean isActivityTop(Context context, String clazzName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(clazzName);
    }

    /**
     * 退出程序
     */
    public synchronized static void exitApp() {
        // 结束activity队列中的所有activity
        for (Activity activity : activityList) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 退出到登录页面
     */
    public synchronized static void exitToLogin() {
        for (Activity activity : activityList) {
            if (!activity.isFinishing()
                    && !activity.getClass().getName().contains("LoginActivity")
                    && !activity.getClass().getName().contains("MainActivity")) {
                activity.finish();
            }
        }
    }

    public static boolean isBackground() {
        return activityCount == 0;
    }

    public static int getActivityCount() {
        return activityList.size();
    }

    public synchronized static void finish(String activityName) {
        for (Activity activity : activityList) {
            if (activity.getClass().getName().equals(activityName)) {
                activity.finish();// 会执行destroy方法，而此方法中有removeActivity(activity)的方法
            }
        }
    }

    public static void activityStart() {
        if (isBackground()) {
            for (OnAppStateChangedListener l : listeners) {
                l.onResumeApp();
            }
        }
        activityCount ++;
    }

    public static void activityStop() {
        activityCount--;
        if (isBackground()) {
            for (OnAppStateChangedListener l : listeners) {
                l.onLeaveApp();
            }
        }
    }

    private static List<OnAppStateChangedListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public static void addOnAppStateChangedListener(OnAppStateChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeOnAppStateChangedListener(OnAppStateChangedListener listener) {
        listeners.remove(listener);
    }

    public interface OnAppStateChangedListener {

        /**
         * app进入前台
         */
        void onResumeApp();

        /**
         * app进入后台
         */
        void onLeaveApp();
    }
}
