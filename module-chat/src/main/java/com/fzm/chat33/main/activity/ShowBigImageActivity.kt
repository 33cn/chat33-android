package com.fzm.chat33.main.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders.Builder
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.fuzamei.common.utils.*
import com.fuzamei.common.widget.BottomPopupWindow
import com.fuzamei.common.widget.BottomPopupWindow.OnItemClickListener
import com.fuzamei.common.widget.LoadingDialog
import com.fuzamei.common.widget.photoview.PhotoView
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.LoadableActivity
import com.fzm.chat33.R
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.hepler.QRCodeHelper
import com.google.zxing.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = AppRoute.BIG_IMAGE)
class ShowBigImageActivity : LoadableActivity() {
    private var tv_indicator: TextView? = null
    private var vp_big_image: ViewPager? = null
    //自定义加载框
    private val dialog: LoadingDialog? = null
    private var adapter: BigImageAdapter? = null
    var currentIndex = 0
    var imageList: ArrayList<String>? = null
    private var formatUrl: String? = null
    private val options: MutableList<String?> = ArrayList()
    private var codeResult: Result? = null
    private val FIND_TYPE_QR_CODE = 2
    private var picSave: String? = null
    private var picForward: String? = null
    private var picScan: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_show_big_image
    }

    override fun initView() {
        vp_big_image = findViewById(R.id.vp_big_image)
        tv_indicator = findViewById(R.id.tv_indicator)
        picSave = getString(R.string.chat_action_large_pic_save)
        picForward = getString(R.string.chat_action_large_pic_forward)
        picScan = getString(R.string.chat_action_large_pic_qr_scan)
    }

    override fun initData() {
        formatUrl = "?x-oss-process=image/resize,h_" + ScreenUtils.dp2px(this, 150f) + "/quality,q_70/format,jpg/interlace,1"
        options.add(picSave)
        options.add(picForward)
        imageList = intent.getStringArrayListExtra("image_list")
        currentIndex = intent.getIntExtra("currentIndex", 0)
        if (imageList?.size == 1) {
            tv_indicator!!.visibility = View.GONE
        } else {
            tv_indicator!!.visibility = View.VISIBLE
            tv_indicator!!.text = "1/${imageList?.size?:0}"
        }
    }

    override fun setEvent() {
        adapter = BigImageAdapter(this, imageList)
        vp_big_image!!.offscreenPageLimit = 3
        vp_big_image!!.adapter = adapter
        vp_big_image!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                codeResult = null
                options.clear()
                options.add(picSave)
                options.add(picForward)
                tv_indicator!!.text = (position + 1).toString() + "/" + imageList!!.size
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        vp_big_image!!.currentItem = currentIndex
    }

    override fun setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, android.R.color.black), 0)
    }

    internal inner class BigImageAdapter(private val context: Context, private val images: List<String>?) : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val convertView = LayoutInflater.from(context).inflate(R.layout.item_big_image, null)
            val photoView: PhotoView = convertView.findViewById(R.id.pv_big_image)
            photoView.setOnClickListener { onBackPressed() }
            val options = RequestOptions()
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            if (!TextUtils.isEmpty(images!![position])) {
                if (images[position].endsWith("gif")) {
                    displayGif(photoView, position, options)
                } else {
                    displayImage(photoView, position, options)
                }
            }
            container.addView(convertView)
            return convertView
        }

        private fun displayGif(photoView: PhotoView, position: Int, options: RequestOptions) {
            Glide.with(context).asGif().load(if (images!![position].startsWith("http")) ToolUtils.createCookieUrl(images[position] + formatUrl) else "file://" + images[position]).apply(options).into(photoView)
        }

        private fun displayImage(photoView: PhotoView, position: Int, options: RequestOptions) {
            val simpleTarget: CustomViewTarget<PhotoView, Bitmap> = object : CustomViewTarget<PhotoView, Bitmap>(photoView) {
                override fun onResourceLoading(placeholder: Drawable?) { //                    photoView.setImageDrawable(new ColorDrawable(Color.parseColor("#6C6C6C")));
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    photoView.setImageBitmap(bitmap)
                    setOnLongClickListener(photoView, bitmap, position)
                }

                override fun onResourceCleared(placeholder: Drawable?) {}
            }
            if (!TextUtils.isEmpty(images!![position])) {
                Glide.with(context).asBitmap().apply(options).load(
                        if (images[position].startsWith("http")) createCookieUrl(images[position]) else "file://" + images[position])
                        .into(simpleTarget)
            }
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
                            val chatFile = ChatFile.newImage(imageList!![pos], "", bitmap.height, bitmap.width)
                            ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                                    .withSerializable("chatFile", chatFile)
                                    .navigation()
                        } else {
                            QRCodeHelper.process(this@ShowBigImageActivity, codeResult?.text)
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
                if (!TextUtils.isEmpty(images!![position])) {
                    loading(true)
                    ImageUtils.saveImageToGallery(photoView.drawable)
                    dismiss()
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved))
                }
            }
        }

        private fun createCookieUrl(url: String): GlideUrl {
            return if (TextUtils.isEmpty(url)) {
                GlideUrl("")
            } else GlideUrl(url, Builder().addHeader("Cookie", "chatimage").build())
        }

        override fun getCount(): Int {
            return images?.size ?: 0
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