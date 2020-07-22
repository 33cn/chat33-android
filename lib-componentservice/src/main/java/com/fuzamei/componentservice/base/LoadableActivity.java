package com.fuzamei.componentservice.base;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fuzamei.common.net.subscribers.Loadable;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.widget.LoadingDialog;
import com.fuzamei.componentservice.R;
import com.fuzamei.componentservice.app.Loading;

/**
 * @author zhengjy
 * @since 2018/9/14
 * Description: Activity一般通用基类
 */
public abstract class LoadableActivity extends BaseActivity implements Loadable {

    //自定义加载框
    private LoadingDialog dialog;

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.app_color_primary), 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .show(fragment)
                .commit();
    }

    public void hideFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(fragment)
                .commit();
    }

    public void replaceFragment(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public void addFragment(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(id, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public void pushFragment(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.basic_slide_right_in, R.anim.basic_slide_right_out)
                .add(id, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public void popFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    protected void setupLoading(Loading loading) {
        if (loading.getLoading()) {
            loading(loading.getCancelable());
        } else {
            dismiss();
        }
    }

    @Override
    public void loading(boolean cancelable) {
        if (dialog == null) {
            dialog = new LoadingDialog(this, cancelable);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void dismiss() {
        if (dialog != null) {
            if (!this.isFinishing() && dialog.isShowing()) {
                dialog.cancel();
            }
        }
    }

    @Override
    public void onBackPressed() {
        popFragment();
    }
}
