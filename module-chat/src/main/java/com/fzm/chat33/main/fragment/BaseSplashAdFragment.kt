package com.fzm.chat33.main.fragment

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.fuzamei.componentservice.app.RouterHelper
import com.fuzamei.componentservice.base.LoadableFragment
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.bean.AdDisplayBean
import java.net.URLEncoder

/**
 * @author zhengjy
 * @since 2019/06/13
 * Description:
 */
abstract class BaseSplashAdFragment : LoadableFragment() {

    protected var mData: AdDisplayBean? = null
    protected var mListener: OnAdDisplayFinishListener? = null
    protected var mTimer: AdCountDownTimer? = null

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        mData = arguments?.getSerializable("display") as AdDisplayBean?
        mTimer = AdCountDownTimer((mData?.duration ?: 0) * 1000L, 1000L)
    }

    fun setOnAdDisplayFinish(listener: OnAdDisplayFinishListener) {
        this.mListener = listener
    }

    inner class AdCountDownTimer(
            millisInFuture: Long,
            countDownInterval: Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {

        var mView: TextView? = null

        override fun onTick(millisUntilFinished: Long) {
            mView?.text = getString(R.string.chat_time_skip, millisUntilFinished / 1000 + 1)
        }

        override fun onFinish() {
            mListener?.adDisplayFinish(null)
        }

        fun startWith(view: TextView?) {
            this.mView = view
            start()
            mView?.setOnClickListener {
                cancel()
                mListener?.adDisplayFinish(null)
            }
        }
    }

    fun processLink(link: String?): String? {
        try {
            val uri = Uri.parse(link)
            return if (uri.toString().contains(AppConfig.APP_SHARE_URL)) {
                val groupId = uri.getQueryParameter("gid")
                val friendId = uri.getQueryParameter("uid")
                if (!TextUtils.isEmpty(groupId)) {
                    "${RouterHelper.APP_LINK}?type=shareJoinRoom&markId=$groupId&sourceType=3&sourceId=null"
                } else if (!TextUtils.isEmpty(friendId)) {
                    "${RouterHelper.APP_LINK}?type=shareUserDetail&userId=$friendId&fetchInfoById=false&sourceType=3&sourceId=$friendId"
                } else {
                    "${RouterHelper.APP_LINK}?type=appWebBrowser&url=${URLEncoder.encode(mData?.link?:"", "UTF-8")}"
                }
            } else {
                "${RouterHelper.APP_LINK}?type=appWebBrowser&url=${URLEncoder.encode(mData?.link?:"", "UTF-8")}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }

    interface OnAdDisplayFinishListener {

        fun adDisplayFinish(route: String?)
    }
}
