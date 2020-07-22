package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View

import com.fzm.chat33.R
import com.fzm.chat33.bean.AdDisplayBean
import android.net.Uri
import android.widget.MediaController
import kotlinx.android.synthetic.main.fragment_splash_gif_ad.*

/**
 * @author zhengjy
 * @since 2019/06/13
 * Description:GIF广告显示界面
 */
class SplashGifAdFragment : BaseSplashAdFragment() {

    companion object {
        @JvmStatic
        fun create(bean: AdDisplayBean): SplashGifAdFragment {
            val fragment = SplashGifAdFragment()
            val params = Bundle()
            params.putSerializable("display", bean)
            fragment.arguments = params
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash_gif_ad
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        try {
            val mc = MediaController(activity)
            mc.visibility = View.INVISIBLE
            vv_ad.setMediaController(mc)
            val uri = Uri.parse("android.resource://${activity.packageName}/")
            vv_ad.setVideoURI(uri)
            vv_ad.start()
            mTimer?.startWith(iv_skip)
            vv_ad.setOnClickListener {
                mTimer?.cancel()
                mListener?.adDisplayFinish(processLink(mData?.link))
            }
        } catch (e: Exception) {
            mListener?.adDisplayFinish(null)
            e.printStackTrace()
        }
    }

    override fun initData() {

    }

    override fun setEvent() {

    }
}
