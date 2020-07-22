package com.fzm.chat33.main.activity

import android.text.TextUtils

import androidx.core.content.ContextCompat

import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.MediaUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.LoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.FileEncryption

import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.manager.toByteArray
import com.fzm.chat33.core.manager.toCacheFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author zhengjy
 * @since 2019/01/25
 * Description:视频播放界面
 */
@Route(path = AppRoute.VIDEO_PLAYER)
class VideoPlayerActivity : LoadableActivity() {

    private var videoPlayer: JzvdStd? = null

    @JvmField
    @Autowired
    var message: ChatMessage? = null
    @JvmField
    @Autowired
    var videoUrl: String? = null
    @JvmField
    @Autowired
    var imageUrl: String? = null

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.transparent), 0)
        BarUtils.setStatusBarLightMode(this, false)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_player
    }

    override fun initView() {
        videoPlayer = findViewById(R.id.videoplayer)
    }

    override fun initData() {
        ARouter.getInstance().inject(this)
        GlobalScope.launch(Dispatchers.Main) {
            val encPath = if (message!!.briefPos != 0) {
                message!!.msg.sourceLog[message!!.briefPos - 1].msg.localPath
            } else {
                message!!.msg.localPath
            }
            videoPlayer!!.setUp(encPath, "", Jzvd.SCREEN_WINDOW_NORMAL)
            val file = if (AppConfig.FILE_ENCRYPT && encPath.contains(AppConfig.ENC_PREFIX)) {
                FileEncryption.decrypt(message!!.toDecParams(),
                        File(encPath).toByteArray())?.toCacheFile(encPath)
            } else {
                File(encPath)
            } ?: return@launch

            videoUrl = file.path
            videoPlayer!!.setUp(videoUrl, "", Jzvd.SCREEN_WINDOW_NORMAL)
            if (isDestroyed || isFinishing) {
                return@launch
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(instance).load(imageUrl).into(videoPlayer!!.thumbImageView)
            } else {
                Glide.with(instance).load(videoUrl)
                        .apply(RequestOptions().placeholder(R.drawable.bg_image_placeholder))
                        .into(videoPlayer!!.thumbImageView)
            }
            videoPlayer!!.startButton.performClick()
        }
    }

    override fun setEvent() {

    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
        /**恢复系统其它媒体的状态 */
        MediaUtils.muteAudioFocus(this, true)
    }

    override fun onResume() {
        super.onResume()
        /**暂停系统其它媒体的状态 */
        MediaUtils.muteAudioFocus(this, false)
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }
}
