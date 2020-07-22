package com.fuzamei.componentservice.base;

import androidx.fragment.app.Fragment;

import com.fuzamei.common.net.subscribers.Loadable;
import com.fuzamei.common.widget.LoadingDialog;
import com.fuzamei.componentservice.app.Loading;

/**
 * Created by zhengfan on 16/6/29.
 * fragment基类
 */
public abstract class LoadableFragment extends BaseFragment implements Loadable {

    //自定义加载框
    private LoadingDialog dialog;

    protected void setupLoading(Loading loading) {
        if (loading.getLoading()) {
            loading(loading.getCancelable());
        } else {
            dismiss();
        }
    }

    @Override
    public void loading(boolean cancelable) {
        if (getActivity() instanceof Loadable) {
            ((Loadable) getActivity()).loading(cancelable);
            return;
        }
        if (dialog == null) {
            dialog = new LoadingDialog(activity, cancelable);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void dismiss() {
        if (getActivity() instanceof Loadable) {
            ((Loadable) getActivity()).dismiss();
            return;
        }
        if (dialog != null) {
            if (getActivity() != null && !getActivity().isFinishing() && dialog.isShowing()) {
                dialog.cancel();
            }
        }
    }

    public void open(int id, Fragment fragment) {
        ((LoadableActivity) activity).pushFragment(id, fragment);
    }

    public void replace(int id, Fragment fragment) {
        ((LoadableActivity) activity).replaceFragment(id, fragment);
    }

    public void finish() {
        ((LoadableActivity) activity).popFragment();
    }
}
