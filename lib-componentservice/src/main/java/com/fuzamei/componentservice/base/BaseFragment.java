package com.fuzamei.componentservice.base;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fuzamei.common.utils.LogUtils;
import com.fuzamei.commonlib.R;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by zhengfan on 16/6/29.
 * fragment基类
 */
public abstract class BaseFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    protected String TAG = getClass().getSimpleName();
    /**
     * view是否初始化完成
     */
    protected boolean isViewInitiated;

    /**
     * 页面是否对用户可见
     */
    protected boolean isVisibleToUser;

    /**
     * 数据是否初始化
     */
    protected boolean isDataInitiated;

    protected View rootView;

    /**
     * 是否为初次加载，初次加载时显示加载框
     */
    protected boolean initialLoad = true;

    protected FragmentActivity activity = null;

    //获取fragment布局文件ID
    protected abstract int getLayoutId();

    protected abstract void initView(View view, @Nullable Bundle savedInstanceState);

    //initdata是第一次new的时候会执行，fetch是以后每次显示的时候执行，所以，initdata做一些初始化工作，每次请求数据的放在fetch中。
    abstract public void initData();

    abstract public void setEvent();

    // 生命周期 onCreateView ---> onActivityCreated
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        isViewInitiated = true;
        prepareFetchData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        activity = getActivity();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view, savedInstanceState);
        initData();
        setEvent();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    public void fetchData(){

    }

    public void destroyData(){

    }

    public boolean prepareFetchData() {
        if (isVisibleToUser && isViewInitiated) {
            fetchData();
            isDataInitiated = true;
            return true;
        } else if (!isVisibleToUser) {
            destroyData();
        }
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtils.e("Fragment onRequestPermissionsResult ");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        LogUtils.e("requestCode = " + requestCode + " perms = " + perms);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.basic_permission_denied_title))
                    .setRationale(getString(R.string.basic_permission_denied_rationale))
                    .setRequestCode(requestCode)//用于onActivityResult回调做其它对应相关的操作
                    .build()
                    .show();
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        LogUtils.e("requestCode = " + requestCode + " perms = " + perms);

    }
}
