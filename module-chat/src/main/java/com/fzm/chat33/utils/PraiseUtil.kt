package com.fzm.chat33.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.fuzamei.componentservice.ext.dp2px
import com.fzm.chat33.R

/**
 * @author zhengjy
 * @since 2019/11/22
 * Description:
 */
object PraiseUtil {

    private var toast: Toast? = null

    fun showLike(context: Context) {
        showToast(context, "+1", R.color.chat_color_accent, R.drawable.ic_thumb_up_accent)
    }

    fun showCancelLike(context: Context) {
        showToast(context, "-1", R.color.chat_text_grey_light, R.drawable.ic_thumb_up_grey)
    }

    fun showReward(context: Context) {
        showToast(context, "+1", R.color.chat_reward_orange, R.drawable.ic_thumb_up_orange)
    }

    private fun showToast(context: Context, msgString: String, @ColorRes color: Int, @DrawableRes image: Int) {
        val toastView: View = LayoutInflater.from(context).inflate(
                R.layout.chat_toast_praise_view, null)
        val root = toastView.findViewById<LinearLayout>(R.id.root)
        val param = LinearLayout.LayoutParams(context.dp2px(90f), context.dp2px(90f))
        root.layoutParams = param
        toastView.setBackgroundResource(R.drawable.chat_toast_bg_trans_70)
        val tips = toastView.findViewById<TextView>(R.id.toast_tv_title)
        tips.text = msgString
        tips.setTextColor(ContextCompat.getColor(context, color))
        val thumb = toastView.findViewById<ImageView>(R.id.toast_iv_thumb)
        thumb.setImageResource(image)
        toast?.cancel()
        toast = Toast(context)
        toast?.view = toastView
        toast?.duration = Toast.LENGTH_SHORT
        toast?.setGravity(Gravity.CENTER, 0, 0)
        toast?.show()
    }
}