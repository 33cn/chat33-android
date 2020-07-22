package com.fzm.chat33.main.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.fuzamei.common.utils.*
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.common.widget.BottomPopupWindow.OnItemClickListener
import com.fuzamei.common.widget.photoview.PhotoView
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.LoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.hepler.QRCodeHelper
import com.fzm.chat33.utils.FileUtils
import com.fzm.chat33.utils.StringUtils
import com.google.zxing.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = AppRoute.LARGE_PHOTO)
class LargePhotoActivity : LoadableActivity(), OnClickListener {
    internal var photoView: PhotoView? = null
    private var pb_snap_progress: ProgressBar? = null
    private var tv_count: TextView? = null
    internal var activityLargePhoto: FrameLayout? = null
    private var formatUrl: String? = null
    private var message: ChatMessage? = null
    private var imageUrl: String? = null
    private var channelType = 0
    private var timer: CountDownTimer? = null
    private var codeResult: Result? = null
    private var simpleTarget: CustomViewTarget<PhotoView, Bitmap>? = null
    private var popupWindow: BottomPopupWindow? = null
    private val options: MutableList<String?> = ArrayList()
    private val FIND_TYPE_QR_CODE = 2
    private var picSave: String? = null
    private var picForward: String? = null
    private var picScan: String? = null
    override fun initView() {
        photoView = findViewById(R.id.photo_view)
        photoView?.setOnClickListener(this)
        activityLargePhoto = findViewById(R.id.activity_large_photo)
        activityLargePhoto?.setOnClickListener(this)
        pb_snap_progress = findViewById(R.id.pb_snap_progress)
        tv_count = findViewById(R.id.tv_count)
        picSave = getString(R.string.chat_action_large_pic_save)
        picForward = getString(R.string.chat_action_large_pic_forward)
        picScan = getString(R.string.chat_action_large_pic_qr_scan)
    }

    override fun getLayoutId(): Int {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        return R.layout.activity_large_photo
    }

    override fun initData() {}
    override fun setEvent() {}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        formatUrl = "?x-oss-process=image/resize,h_" + ScreenUtils.dp2px(instance, 150f) + "/quality,q_70/format,jpg/interlace,1"
        message = intent.getSerializableExtra(CHAT_MESSAGE) as? ChatMessage
        imageUrl = if (message != null) {
            message!!.msg.imageUrl
        } else {
            intent.getStringExtra(IMAGE_URL)
        }
        channelType = intent.getIntExtra(CHANNEL_TYPE, 0)
        if (message == null || message!!.isSnap == 2 || message!!.isSentType) {
            options.add(picSave)
            if (!TextUtils.isEmpty(imageUrl)) {
                options.add(picForward)
            }
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        val mImageOptions = RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        if (message != null && message!!.destroyTime != 0L && message!!.isSnap == 1 && System.currentTimeMillis() < message!!.destroyTime && !message!!.isSentType) {
            setupCountingView()
        }
        simpleTarget = object : CustomViewTarget<PhotoView, Bitmap>(photoView!!) {
            override fun onLoadFailed(errorDrawable: Drawable?) {}
            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                photoView!!.setImageBitmap(bitmap)
                if (message != null && message!!.destroyTime == 0L && message!!.isSnap == 1 && !message!!.isSentType) {
                    message!!.destroyTime = System.currentTimeMillis() + 30 * 1000L
                    setupCountingView()
                } else {
                    setOnLongClickListener(bitmap)
                }
            }

            override fun onResourceCleared(placeholder: Drawable?) {}
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this).asBitmap().apply(mImageOptions).load(
                    if (imageUrl?.startsWith("http") == true) ToolUtils.createCookieUrl(imageUrl) else "file://$imageUrl")
                    .into(simpleTarget!!)
        } else if (channelType == Chat33Const.CHANNEL_ROOM) {
            photoView!!.setImageResource(R.mipmap.default_avatar_room_big)
        } else if (channelType == Chat33Const.CHANNEL_FRIEND) {
            photoView!!.setImageResource(R.mipmap.default_avatar_big)
        }
    }

    private fun setupCountingView() {
        val params = LayoutParams(pb_snap_progress!!.layoutParams)
        params.setMargins(0, BarUtils.getStatusBarHeight(instance), 0, 0)
        pb_snap_progress!!.layoutParams = params
        val params1 = LayoutParams(tv_count!!.layoutParams)
        params1.setMargins(ScreenUtils.dp2px(instance, 15f),
                ScreenUtils.dp2px(instance, 20f) + BarUtils.getStatusBarHeight(instance), 0, 0)
        tv_count!!.layoutParams = params1
        pb_snap_progress!!.visibility = View.VISIBLE
        tv_count!!.visibility = View.VISIBLE
        timer = object : CountDownTimer(message!!.destroyTime - System.currentTimeMillis(), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                pb_snap_progress!!.progress = (millisUntilFinished / 1000L).toInt()
                tv_count!!.text = StringUtils.formateTime(millisUntilFinished)
            }

            override fun onFinish() {
                onBackPressed()
            }
        }
        timer?.start()
    }

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, 0x262B31, 0)
        BarUtils.setStatusBarLightMode(this, false)
    }

    private fun setOnLongClickListener(bitmap: Bitmap) {
        photoView!!.setOnLongClickListener {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                VibrateUtils.simple(instance, 50)
                if (popupWindow == null) {
                    popupWindow = BottomPopupWindow(instance, options, OnItemClickListener { view, popupWindow, position ->
                        popupWindow.dismiss()
                        if (position == 0) {
                            save()
                        } else if (position == 1) {
                            val chatFile = ChatFile.newImage(imageUrl, "", bitmap.height, bitmap.width)
                            ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                                    .withSerializable("chatFile", chatFile)
                                    .navigation()
                        } else {
                            QRCodeHelper.process(this@LargePhotoActivity, codeResult?.text)
                        }
                    })
                }
                popupWindow!!.showAtLocation(photoView, Gravity.BOTTOM, 0, 0)
                if (codeResult == null) {
                    codeResult = withContext(Dispatchers.IO) {
                        QRCodeUtil.decodeFromPicture(bitmap)
                    }
                    if (codeResult != null) {
                        if (!options.contains(picScan)) {
                            options.add(picScan)
                            popupWindow!!.notifyItemInserted(popupWindow!!.optionSize() - 1)
                        }
                    }
                } else {
                    if (!options.contains(picScan)) {
                        options.add(picScan)
                        popupWindow!!.notifyItemInserted(popupWindow!!.optionSize() - 1)
                    }
                }
            }
            false
        }
    }

    private fun save() {
        if (!FileUtils.isGrantExternalRW(Chat33Const.SAVE_IMAGE_PERMISSION, instance)) {
            return
        }
        if (photoView!!.drawable != null) {
            if (!TextUtils.isEmpty(imageUrl)) {
                loading(true)
                ImageUtils.saveImageToGallery(photoView!!.drawable)
                dismiss()
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved))
            }
        }
    }

    override fun onClick(view: View) {
        val i = view.id
        if (i == R.id.photo_view) {
            onBackPressed()
        } else if (i == R.id.activity_large_photo) {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (message != null) {
            if (timer != null) {
                timer!!.cancel()
            }
            val intent = Intent()
            intent.putExtra("logId", message!!.logId)
            intent.putExtra("destroyTime", message!!.destroyTime)
            setResult(ChatActivity.REQUEST_DEAD_TIME, intent)
        }
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Chat33Const.SAVE_IMAGE_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                save()
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage))
            }
        }
    }

    companion object {
        const val CHAT_MESSAGE = "chatMessage"
        const val CHANNEL_TYPE = "channelType"
        const val IMAGE_URL = "imageUrl"
    }
}