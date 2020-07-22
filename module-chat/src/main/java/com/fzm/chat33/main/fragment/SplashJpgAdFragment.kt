package com.fzm.chat33.main.fragment

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide

import com.fzm.chat33.R
import com.fzm.chat33.bean.AdDisplayBean
import com.fuzamei.componentservice.app.RouterHelper
import kotlinx.android.synthetic.main.fragment_splash_jpg_ad.*
import java.io.File

/**
 * @author zhengjy
 * @since 2019/06/13
 * Description:图片广告显示界面
 */
class SplashJpgAdFragment : BaseSplashAdFragment() {

    companion object {
        @JvmStatic
        fun create(bean: AdDisplayBean): SplashJpgAdFragment {
            val fragment = SplashJpgAdFragment()
            val params = Bundle()
            params.putSerializable("display", bean)
            fragment.arguments = params
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_splash_jpg_ad
    }

    override fun initView(view: View?, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        Glide.with(this).load(File(mData?.path ?: "")).into(iv_ad)
        mTimer?.startWith(iv_skip)
        iv_ad.setOnClickListener {
            mTimer?.cancel()
            mListener?.adDisplayFinish(processLink(mData?.link))
        }
    }

    override fun initData() {

    }

    override fun setEvent() {

    }
}
