package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View

import com.fzm.chat33.R
import com.fzm.chat33.bean.AdDisplayBean
import com.fuzamei.componentservice.app.RouterHelper
import kotlinx.android.synthetic.main.fragment_splash_video_ad.*

/**
 * @author zhengjy
 * @since 2019/06/13
 * Description:视频广告显示界面
 */
class SplashVideoAdFragment : BaseSplashAdFragment() {

    companion object {
        @JvmStatic
        fun create(bean: AdDisplayBean): SplashVideoAdFragment {
            val fragment = SplashVideoAdFragment()
            val params = Bundle()
            params.putSerializable("display", bean)
            fragment.arguments = params
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash_video_ad
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        try {
            svv_ad.setDataSource(mData?.path ?: "")
            svv_ad.setVolume(0f, 0f)
            svv_ad.isLooping = true
            svv_ad.prepare {
                svv_ad.start()
                mTimer?.startWith(iv_skip)
                svv_ad.setOnClickListener {
                    mTimer?.cancel()
                    mListener?.adDisplayFinish(processLink(mData?.link))
                }
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
