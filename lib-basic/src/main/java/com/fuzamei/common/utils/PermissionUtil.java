package com.fuzamei.common.utils;

import android.Manifest;

import com.fuzamei.common.FzmFramework;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2019/03/04
 * Description:
 */
public class PermissionUtil {

    public static boolean hasWriteExternalPermission() {
        return EasyPermissions.hasPermissions(FzmFramework.context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(FzmFramework.context, Manifest.permission.ACCESS_FINE_LOCATION);
    }
}
