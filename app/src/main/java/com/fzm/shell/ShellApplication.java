package com.fzm.shell;

import android.content.Context;

import com.baidu.crabsdk.CrabSDK;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.update.XUpdate;
import com.fzm.chat33.app.App;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.di.Injectors;
import com.fzm.chat33.global.CrashHandler;
import com.fzm.chat33.main.activity.MainActivity;
import com.fzm.push.PushManager;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import org.jetbrains.annotations.NotNull;

/**
 * @author zhengjy
 * @since 2018/12/11
 * Description:
 */
public class ShellApplication extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        // 开发调试工具
        DoraemonKit.install(this);

        // 聊天模块初始化
        Chat33.init(this);
        // 推送模块初始化
        PushManager.init(this);
        // 聊天界面模块注入
        Injectors.init();

        // 百度CrabSDK
        CrabSDK.init(this, AppConfig.BAIDU_CRAB_KEY);
        CrabSDK.openNativeCrashHandler();

        // 下拉加载风格
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NotNull
            @Override
            public RefreshHeader createRefreshHeader(@NotNull Context context, @NotNull RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.chat_color_status_bg, R.color.chat_text_grey_light);
                return new ClassicsHeader(context);
            }
        });
        // 上拉加载风格
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NotNull
            @Override
            public RefreshFooter createRefreshFooter(@NotNull Context context, @NotNull RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.chat_color_status_bg, R.color.chat_text_grey_light);
                return new ClassicsFooter(context);
            }
        });
        // App更新库
        XUpdate.get()
                .setIcon(R.mipmap.ic_launcher_chat33)
                .setTitle(getString(com.fzm.chat33.R.string.application_name))
                .setTarget(MainActivity.class)
                .init(this);
        // 崩溃日志记录
        CrashHandler.getInstance().init(this);
    }
}
