package com.fuzamei.update;

import android.content.Context;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.FzmFramework;
import com.fuzamei.update.Interface.IUpdateInfo;
import com.fuzamei.update.Interface.OnCheckUpdateListener;
import com.fuzamei.update.Interface.UpdateChecker;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * @author zhengjy
 * @since 2018/08/03
 * Description:
 */
public class XUpdate {

    private static XUpdate sInstance = null;

    private Context application;
    /**
     * 通知栏显示标题
     */
    private String title;
    /**
     * 通知栏显示图标
     */
    private int icon;
    /**
     * 通知栏点击跳转目标
     */
    private Class<?> target;

    private XUpdate() {
        this.title = FzmFramework.getString(R.string.update_downloading);
        this.icon = 0;
    }

    public static XUpdate get() {
        if (sInstance == null) {
            sInstance = new XUpdate();
        }
        return sInstance;
    }

    public XUpdate setTitle(String title) {
        this.title = title;
        return this;
    }

    public XUpdate setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public XUpdate setTarget(Class<?> target) {
        this.target = target;
        return this;
    }

    public Context getContext() {
        if (application == null) {
            throw new RuntimeException("You should init XUpdate first.");
        }
        return application;
    }

    public String getTitle() {
        return title;
    }

    public int getIcon() {
        return icon;
    }

    public Class<?> getTarget() {
        return target;
    }

    public void init(Context context) {
        if (icon == 0) {
            throw new RuntimeException("field 'icon' must have value");
        }
        this.application = context;
    }

    /**
     * 检查更新，自动匹配实现UpdateChecker接口的类
     *
     * @param listener  检查更新结果监听
     */
    public static UpdateManager.Builder checkUpdate(OnCheckUpdateListener listener) {
        UpdateChecker checker = (UpdateChecker) ARouter.getInstance().build("/data/updateChecker").navigation();
        return checkUpdate(checker, listener);
    }

    /**
     * 检查更新，传入UpdateChecker对象
     *
     * @param checker   更新检查器
     * @param listener  检查更新结果监听
     */
    public static UpdateManager.Builder checkUpdate(UpdateChecker checker, OnCheckUpdateListener listener) {
        return checkUpdate(Observable.just(checker).flatMap(new Function<UpdateChecker, ObservableSource<IUpdateInfo>>() {
            @Override
            public ObservableSource<IUpdateInfo> apply(UpdateChecker checker) throws Exception {
                return Observable.just(checker.checkUpdate());
            }
        }), listener);
    }

    /**
     * 检查更新，传入网络请求的observable
     *
     * @param observable    检查更新的observable
     * @param listener      检查更新结果监听
     */
    public static UpdateManager.Builder checkUpdate(Observable<IUpdateInfo> observable, OnCheckUpdateListener listener) {
        return new UpdateManager.Builder(observable, listener);
    }
}
