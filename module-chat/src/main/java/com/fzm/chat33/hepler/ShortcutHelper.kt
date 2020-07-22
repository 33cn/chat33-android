package com.fzm.chat33.hepler

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.fuzamei.componentservice.app.RouterHelper
import com.fzm.chat33.R

/**
 * @author zhengjy
 * @since 2019/11/13
 * Description:
 */
@Deprecated("别用")
object ShortcutHelper {

    private const val SHORTCUT_ID_QR = "SHORTCUT_ID_QR"
    private const val SHORTCUT_ID_SCAN = "SHORTCUT_ID_SCAN"

    private lateinit var context: Context
    private lateinit var manager: ShortcutManager

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun init(context: Context) {
        this.context = context
        manager = context.getSystemService(ShortcutManager::class.java)
        manager.dynamicShortcuts = createShortcutDynamic()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcutDynamic(): List<ShortcutInfo> {
        val shortcutInfoList = ArrayList<ShortcutInfo>()
        val qrCodeShortcut: ShortcutInfo = ShortcutInfo.Builder(context, SHORTCUT_ID_QR)
//                .setShortLabel(context.getString(R.string.lable_shortcut_dynamic_like_short))
//                .setLongLabel(context.getString(R.string.lable_shortcut_dynamic_like_long))
                .setLongLabel("我的二维码")
                .setShortLabel("二维码")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_chat))
                .setIntent(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("${RouterHelper.APP_LINK}?type=myQRCode&channelType=3")))
                .build()
        val scanShortcut: ShortcutInfo = ShortcutInfo.Builder(context, SHORTCUT_ID_SCAN)
//                .setShortLabel(context.getString(R.string.lable_shortcut_dynamic_like_short))
//                .setLongLabel(context.getString(R.string.lable_shortcut_dynamic_like_long))
                .setLongLabel("扫描二维码")
                .setShortLabel("扫一扫")
                .setIcon(Icon.createWithResource(context, R.drawable.ic_wechat_moments))
                .setIntent(Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("${RouterHelper.APP_LINK}?type=scanQRCode")))
                .build()
        shortcutInfoList.add(qrCodeShortcut)
        shortcutInfoList.add(scanShortcut)
        return shortcutInfoList
    }

}