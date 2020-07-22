package com.fzm.chat33.main.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.widget.BottomPopupWindow;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.RouterHelper;
import com.fuzamei.componentservice.base.LoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.Chat33;

import java.util.Arrays;

/**
 * @author zhengjy
 * @since 2018/10/15
 * Description:网页浏览Activity
 */
@Route(path = AppRoute.WEB_BROWSER)
public class WebBrowserActivity extends LoadableActivity implements View.OnClickListener {

    private View iv_more, rl_title;
    private TextView iv_back, tv_web_title;
    private WebView wv_content;
    private ProgressBar pb_web;
    private BottomPopupWindow popupWindow;

    @Autowired
    public String title;
    @Autowired
    public String url;
    @Autowired
    public int titleColor = DEFAULT_TITLE_COLOR;
    @Autowired
    public int textColor = DEFAULT_TEXT_COLOR;
    @Autowired
    public boolean darkMode = true;
    @Autowired
    public boolean showOptions = true;
    private String[] options = Chat33.getContext().getResources().getStringArray(R.array.chat_browser_options);

    private int oldProgress = 0;
    private boolean redirect = false;
    // 如果页面进行了跳转不是第一个页面了，这个值就是false
    private boolean firstPage = true;

    private static final int DEFAULT_TITLE_COLOR = 0xFFFAFBFC;
    private static final int DEFAULT_TEXT_COLOR = 0xFF32B2F7;

    @Override
    protected int getLayoutId() {
        ARouter.getInstance().inject(this);
        Uri uri = getIntent().getData();
        if (uri != null && RouterHelper.APP_SCHEME.equals(uri.getScheme())) {
            redirect = true;
            ARouter.getInstance().build(uri.getPath()).withString("params", uri.getQuery()).navigation();
            finish();
            return 0;
        } else {
            redirect = false;
            return R.layout.activity_web_browser;
        }
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, titleColor, 0);
        BarUtils.setStatusBarLightMode(this, darkMode);
    }

    @Override
    protected void initView() {
        if (redirect) {
            return;
        }
        rl_title = findViewById(R.id.rl_title);
        iv_back = findViewById(R.id.iv_back);
        iv_more = findViewById(R.id.iv_more);
        tv_web_title = findViewById(R.id.tv_web_title);
        wv_content = findViewById(R.id.wv_content);
        pb_web = findViewById(R.id.pb_web);

        if (textColor != DEFAULT_TEXT_COLOR) {
            iv_back.setBackground(null);
            iv_more.setBackground(null);
            tv_web_title.setTextColor(textColor);
        }
        rl_title.setBackgroundColor(titleColor);
        iv_back.setTextColor(textColor);
    }

    @Override
    protected void initData() {
        if (redirect) {
            return;
        }
        iv_back.setOnClickListener(this);
        iv_more.setOnClickListener(this);
        iv_more.setVisibility(showOptions ? View.VISIBLE : View.INVISIBLE);
        tv_web_title.setText(title);
        pb_web.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void setEvent() {
        if (redirect) {
            return;
        }
        WebSettings settings = wv_content.getSettings();
        //如果访问的页面中要与Javascript交互，则webView必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        //设置适应Html5的一些方法
        settings.setDomStorageEnabled(true);

        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webView的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
//        settings.setUserAgentString(settings.getUserAgentString() + ";chat;");
        //不显示缩放按钮
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        wv_content.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!firstPage || TextUtils.isEmpty(title)) {
                    title = view.getTitle();
                    tv_web_title.setText(title);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) {
                    return false;
                }
                WebBrowserActivity.this.url = url;
                if (url.startsWith("http")) {
                    firstPage = false;
                    wv_content.loadUrl(url);
                    return true;
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return true;
                    }
                }
            }
        });
        wv_content.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    pb_web.setVisibility(View.GONE);
                } else {
                    pb_web.setVisibility(View.VISIBLE);
                }
                if (oldProgress != newProgress) {
                    oldProgress = newProgress;
                    pb_web.setProgress(newProgress);
                }
            }
        });
        wv_content.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        wv_content.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_more) {
            if (popupWindow == null) {
                popupWindow = new BottomPopupWindow(this, Arrays.asList(options), new BottomPopupWindow.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, PopupWindow popupWindow, int position) {
                        popupWindow.dismiss();
                        if (position == 0) {
                            ClipboardManager manager = (ClipboardManager) instance.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData data = ClipData.newPlainText("link", url == null ? "" : url);
                            if (manager != null) {
                                manager.setPrimaryClip(data);
                                ShowUtils.showToastNormal(instance, R.string.chat_link_copyed);
                            }
                        } else if (position == 1) {
                            openWebBrowser(url);
                        }
                    }
                });
            }
            popupWindow.showAtLocation(iv_more, Gravity.BOTTOM, 0, 0);
        }
    }

    private void openWebBrowser(String link) {
        try {
            Uri uri = Uri.parse(link);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ShowUtils.showToastNormal(this, R.string.chat_error_link);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_content != null && wv_content.canGoBack()) {
            wv_content.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wv_content != null) {
            wv_content.destroy();
            wv_content = null;
        }
    }
}
