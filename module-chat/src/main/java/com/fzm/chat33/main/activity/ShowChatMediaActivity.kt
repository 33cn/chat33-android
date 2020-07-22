package com.fzm.chat33.main.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.fuzamei.common.utils.*
import com.fuzamei.common.utils.RoomUtils.Companion.run
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.common.widget.BottomPopupWindow.OnItemClickListener
import com.fuzamei.common.widget.photoview.PhotoView
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.LoadableActivity
import com.fuzamei.componentservice.config.AppConfig
import com.fzm.chat33.R
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.utils.FileUtils
import com.google.zxing.Result
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.DownloadTask.Builder
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.StatusUtil.Status
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist.Listener1Model
import com.qmuiteam.qmui.widget.QMUIProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Route(path = AppRoute.CHAT_MEDIA)
class ShowChatMediaActivity : LoadableActivity() {
    private var tv_indicator: TextView? = null
    private var vp_chat_media: ViewPager? = null
    private var adapter: BigImageAdapter? = null
    var currentIndex = 0
    var mediaList: ArrayList<ChatMessage>? = null
    private val visibleMedia: MutableList<ChatMessage> = ArrayList()
    private val options: MutableList<String?> = ArrayList()
    private var codeResult: Result? = null
    private var picSave: String? = null
    private var picForward: String? = null
    private var picScan: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_show_chat_media
    }

    override fun initView() {
        vp_chat_media = findViewById(R.id.vp_chat_media)
        tv_indicator = findViewById(R.id.tv_indicator)
        picSave = getString(R.string.chat_action_large_pic_save)
        picForward = getString(R.string.chat_action_large_pic_forward)
        picScan = getString(R.string.chat_action_large_pic_qr_scan)
    }

    override fun initData() {
        options.add(picSave)
        options.add(picForward)
        mediaList = intent.getSerializableExtra("mediaList") as ArrayList<ChatMessage>
        currentIndex = intent.getIntExtra("currentIndex", 0)
        for (i in mediaList!!.indices) {
            if (TextUtils.isEmpty(mediaList!![i].msg.encryptedMsg)) {
                visibleMedia.add(mediaList!![i])
            } else {
                if (currentIndex > i) {
                    currentIndex--
                }
            }
        }
        if (mediaList!!.size == 1) {
            tv_indicator!!.visibility = View.GONE
        } else {
            tv_indicator!!.visibility = View.VISIBLE
            tv_indicator!!.text = "1/" + visibleMedia.size
        }
    }

    override fun setEvent() {
        adapter = BigImageAdapter(this, visibleMedia)
        vp_chat_media!!.offscreenPageLimit = 2
        vp_chat_media!!.adapter = adapter
        vp_chat_media!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                codeResult = null
                options.clear()
                options.add(picSave)
                options.add(picForward)
                tv_indicator?.text = "${position + 1}/${visibleMedia.size}"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        vp_chat_media!!.currentItem = currentIndex
    }

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, android.R.color.black), 0)
    }

    internal inner class BigImageAdapter(private val context: Context, private val medias: List<ChatMessage>?) : PagerAdapter() {
        private val mImageOptions = RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.DATA)

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val convertView = LayoutInflater.from(context).inflate(R.layout.item_show_chat_media, null)
            val photoView: PhotoView = convertView.findViewById(R.id.pv_chat_media)
            val pb_video: QMUIProgressBar = convertView.findViewById(R.id.pb_video)
            val iv_status = convertView.findViewById<ImageView>(R.id.iv_status)
            photoView.setOnClickListener { finish() }
            if (medias!![position].msgType == ChatMessage.Type.IMAGE) {
                iv_status.visibility = View.GONE
                if (!TextUtils.isEmpty(medias[position].msg.localOrNetUrl)) {
                    if (medias[position].msg.localOrNetUrl.endsWith("gif")) {
                        displayGif(photoView, position)
                    } else {
                        displayImage(photoView, position)
                    }
                }
            } else if (medias[position].msgType == ChatMessage.Type.VIDEO) {
                val hasDownload = medias[position].msg.localPath != null && File(medias[position].msg.localPath).exists()
                iv_status.visibility = View.VISIBLE
                if (hasDownload) {
                    iv_status.setImageResource(R.drawable.icon_video_play)
                } else {
                    iv_status.setImageResource(R.drawable.icon_video_download)
                }
                Glide.with(context).load(medias[position].msg.localOrNetUrl).into(photoView)
                photoView.setOnClickListener {
                    val hasDownload = (medias[position].msg.localPath != null
                            && File(medias[position].msg.localPath).exists())
                    if (hasDownload) {
                        ARouter.getInstance().build(AppRoute.VIDEO_PLAYER)
                                .withString("videoUrl", medias[position].msg.localPath)
                                .navigation()
                    } else {
                        downloadVideoMedia(medias[position], pb_video, iv_status)
                    }
                }
                photoView.setOnLongClickListener(null)
            }
            container.addView(convertView)
            return convertView
        }

        private fun displayGif(photoView: PhotoView, position: Int) {
            Glide.with(context).asGif().load(medias!![position].msg.localOrNetUrl).apply(mImageOptions).into(photoView)
        }

        private fun displayImage(photoView: PhotoView, position: Int) {
            val simpleTarget: CustomViewTarget<PhotoView, Bitmap> = object : CustomViewTarget<PhotoView, Bitmap>(photoView) {
                override fun onResourceLoading(placeholder: Drawable?) {
                    photoView.setImageResource(R.drawable.bg_placeholder)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    photoView.setImageBitmap(bitmap)
                    photoView.setOnClickListener(null)
                    setOnLongClickListener(photoView, bitmap, position)
                }

                override fun onResourceCleared(placeholder: Drawable?) {}
            }
            Glide.with(context).asBitmap().load(medias?.get(position)?.msg?.localOrNetUrl).apply(mImageOptions).into(simpleTarget)
        }

        private fun downloadVideoMedia(message: ChatMessage, pb_video: QMUIProgressBar, iv_status: ImageView) {
            val folder = File(Environment.getExternalStorageDirectory().path + "/" + AppConfig.APP_NAME_EN + "/download/video")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val task = Builder(message.msg.mediaUrl, folder)
                    .setFilename("video_" + message.sendTime + "_" + message.senderId + "." + FileUtils.getExtension(message.msg.mediaUrl))
                    .build()
            val status = StatusUtil.getStatus(task)
            if (status == Status.RUNNING) {
                ShowUtils.showSysToast(instance, getString(R.string.chat_tips_downloading))
                return
            }
            task.enqueue(object : DownloadListener1() {
                override fun taskStart(task: DownloadTask, model: Listener1Model) {
                    pb_video.maxValue = 100
                    if (pb_video.visibility == View.GONE) {
                        pb_video.visibility = View.VISIBLE
                    }
                    if (iv_status.visibility == View.VISIBLE) {
                        iv_status.visibility = View.GONE
                    }
                }

                override fun retry(task: DownloadTask, cause: ResumeFailedCause) {}
                override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {}
                override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                    pb_video.progress = (currentOffset * 1.0f / totalLength * 100).toInt()
                }

                override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Model) {
                    pb_video.visibility = View.GONE
                    iv_status.visibility = View.VISIBLE
                    when (cause) {
                        EndCause.COMPLETED -> if (task.file != null) {
                            iv_status.setImageResource(R.drawable.icon_video_play)
                            message.msg.localPath = task.file!!.absolutePath
                        } else {
                            iv_status.setImageResource(R.drawable.icon_video_download)
                            message.msg.localPath = null
                            ShowUtils.showSysToast(instance, getString(R.string.chat_tips_video_download_fail))
                        }
                        else -> {
                            iv_status.setImageResource(R.drawable.icon_video_download)
                            message.msg.localPath = null
                            ShowUtils.showSysToast(instance, getString(R.string.chat_tips_video_download_fail))
                        }
                    }
                    run(Runnable { ChatDatabase.getInstance().chatMessageDao().insert(message) })
                }
            })
        }

        private fun setOnLongClickListener(photoView: ImageView, bitmap: Bitmap, pos: Int) {
            photoView.setOnLongClickListener {
                lifecycleScope.launch(Dispatchers.Main.immediate) {
                    VibrateUtils.simple(instance, 50)
                    val popupWindow = BottomPopupWindow(instance, options, OnItemClickListener { view, popupWindow, position ->
                        popupWindow.dismiss()
                        if (position == 0) {
                            save(photoView, pos)
                        } else if (position == 1) {
                            val chatFile = medias!![pos].msg
                            ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                                    .withSerializable("chatFile", chatFile)
                                    .navigation()
                        } else {
                            val uri = Uri.parse(codeResult?.text)
                            val groupId = uri.getQueryParameter("gid")
                            val friendId = uri.getQueryParameter("uid")
                            val share = Uri.parse(AppConfig.APP_SHARE_URL)
                            if (share.host == uri.host) {
                                if (!TextUtils.isEmpty(groupId)) {
                                    ARouter.getInstance().build(AppRoute.JOIN_ROOM)
                                            .withString("markId", groupId)
                                            .withInt("sourceType", Chat33Const.FIND_TYPE_QR_CODE)
                                            .navigation()
                                } else if (!TextUtils.isEmpty(friendId)) {
                                    ARouter.getInstance().build(AppRoute.USER_DETAIL)
                                            .withString("userId", friendId)
                                            .withBoolean("fetchInfoById", false)
                                            .withInt("sourceType", Chat33Const.FIND_TYPE_QR_CODE)
                                            .navigation()
                                }
                            } else if (uri.scheme != null && uri.scheme!!.startsWith("http")) {
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            } else {
                                Toast.makeText(instance, getString(R.string.chat_tips_qr_unrecongnize, codeResult?.text), Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    popupWindow.showAtLocation(photoView, Gravity.BOTTOM, 0, 0)
                    if (codeResult == null) {
                        codeResult = withContext(Dispatchers.IO) {
                            QRCodeUtil.decodeFromPicture(bitmap)
                        }
                        if (codeResult != null) {
                            if (!options.contains(picScan)) {
                                options.add(picScan)
                                popupWindow.notifyItemInserted(popupWindow.optionSize() - 1)
                            }
                        }
                    } else {
                        if (!options.contains(picScan)) {
                            options.add(picScan)
                            popupWindow.notifyItemInserted(popupWindow.optionSize() - 1)
                        }
                    }
                }
                false
            }
        }

        fun save(photoView: ImageView?, position: Int) {
            if (!isGrantExternalRW(SAVE_IMAGE_PERMISSION, instance)) {
                tempImg = photoView
                tempPos = position
                return
            }
            if (photoView!!.drawable != null) {
                if (!TextUtils.isEmpty(medias!![position].msg.localPath)) {
                    loading(true)
                    val path = ImageUtils.saveImageToGallery(photoView.drawable)
                    dismiss()
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved))
                    medias[position].msg.localPath = path
                    run(Runnable { ChatDatabase.getInstance().chatMessageDao().insert(medias[position]) })
                }
            }
        }

        override fun getCount(): Int {
            return medias?.size ?: 0
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

    }

    private fun isGrantExternalRW(requestCode: Int, activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), requestCode)
            return false
        }
        return true
    }

    private var tempImg: ImageView? = null
    private var tempPos = 0
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SAVE_IMAGE_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                adapter!!.save(tempImg, tempPos)
                tempImg = null
                tempPos = 0
            } else {
                ShowUtils.showToast(this, getString(R.string.chat_permission_storage))
            }
        }
    }

    companion object {
        const val SAVE_IMAGE_PERMISSION = 0
    }
}