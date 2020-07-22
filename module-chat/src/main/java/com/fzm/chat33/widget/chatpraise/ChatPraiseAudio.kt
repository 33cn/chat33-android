package com.fzm.chat33.widget.chatpraise

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.fuzamei.common.utils.ScreenUtils
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.record.MediaManager
import kotlinx.android.synthetic.main.chat_message_praise_audio.view.*

/**
 * 创建日期：2019/11/28
 * 描述:
 * 作者:yll
 */
class ChatPraiseAudio(activity: Activity) : ChatPraiseBase(activity) {

    override fun getLayoutId(): Int {
        return R.layout.chat_message_praise_audio
    }

    private var isPlaying = false
    private var mMinItemWith: Int = 0// 设置对话框的最大宽度和最小宽度
    private var mMaxItemWith: Int = 0

    override fun initView() {
        contentView.rl_audio.setOnClickListener {
            playOrPauseAudio()
        }
    }

    override fun bindData(message: ChatMessage) {
        super.bindData(message)
        mMaxItemWith = (ScreenUtils.getScreenWidth(activity) * 0.55f).toInt()
        mMinItemWith = (ScreenUtils.getScreenWidth(activity) * 0.20f).toInt()
        val durationTime = message.msg.duration.toInt()
        if (durationTime > 0) {
            val duration = (message.msg.duration + 0.5f).toInt().toString() + "s"
            contentView.tv_duration.text = duration
            contentView.tv_duration.visibility = View.VISIBLE
        } else {
            contentView.tv_duration.visibility = View.GONE
        }
        setViewWidthByDuration(message.msg.duration, contentView.rl_audio)
        contentView.iv_lock.visibility = if (message.isSnap == 1 && (message.isSentType || message.snapVisible == 0)) View.VISIBLE else View.GONE
        contentView.tv_audio_icon.visibility = View.VISIBLE
        //设置动画资源
        val left = intArrayOf(R.string.icon_yuyin_left_vol1, R.string.icon_yuyin_left_vol2, R.string.icon_yuyin_left_vol3)
        val right = intArrayOf(R.string.icon_yuyin_right_vol1, R.string.icon_yuyin_right_vol2, R.string.icon_yuyin_right_vol3)
        contentView.tv_audio_icon.setAnimResource(300, if (message.isSentType) right else left)
        contentView.rl_audio.setBackgroundResource(if(message.isSentType) R.drawable.chat_send_selector else R.drawable.chat_receive_selector)
    }

    //根据播放时长设置语音条长度
    private fun setViewWidthByDuration(duration: Float, view: View) {
        if (duration == 0f) {
            val lParams = view.layoutParams
            lParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            view.layoutParams = lParams
        } else {
            val timePercent = duration / AppConfig.DEFAULT_MAX_RECORD_TIME
            val lParams = view.layoutParams
            lParams.width = (mMinItemWith + (mMaxItemWith - mMinItemWith) * timePercent).toInt()
            view.layoutParams = lParams
        }
    }

    private fun playOrPauseAudio() {
        //todo
        //        MyLog.toastShort("播放或停止:" + chatFile.getName());
        if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(0x11, activity)) {
            return
        }
        val playUrl = message?.msg?.mediaUrl
        if(playUrl.isNullOrEmpty()) return
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        contentView.tv_audio_icon.reset()
        MediaManager.stop()
        if(isPlaying) return
        contentView.tv_audio_icon.play()
        isPlaying = true

        //        playingViewAnim.setTag(chatFile.getNetUrl());
        // 播放音频
        MediaManager.playSound(message, activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager, MediaPlayer.OnCompletionListener {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            isPlaying = false
            contentView.tv_audio_icon.reset()
        })
    }

    override fun onPause() {
        super.onPause()
        MediaManager.stop()
        contentView.tv_audio_icon.stop()
    }
}