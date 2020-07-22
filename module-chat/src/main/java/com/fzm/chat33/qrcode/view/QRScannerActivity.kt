package com.fzm.chat33.qrcode.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.provider.MediaStore
import android.text.TextUtils
import android.view.SurfaceHolder
import android.view.SurfaceHolder.Callback
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.fuzamei.common.utils.BarUtils
import com.fuzamei.common.utils.PicUtils
import com.fuzamei.common.utils.QRCodeUtil
import com.fuzamei.componentservice.app.AppRoute
import com.fzm.chat33.R
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.hepler.QRCodeHelper
import com.fzm.chat33.qrcode.camera.CameraManager
import com.fzm.chat33.qrcode.decoding.CaptureActivityHandler
import com.fzm.chat33.qrcode.decoding.InactivityTimer
import com.fzm.chat33.qrcode.view.ViewfinderView.OnSelectFromAlbumListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

@Route(path = AppRoute.QR_SCAN, extras = AppConst.NEED_LOGIN)
class QRScannerActivity : AppCompatActivity(), Callback, IQRCodeView {
    internal var viewfinderView: ViewfinderView? = null
    private var handler: CaptureActivityHandler? = null
    private var hasSurface = false
    private var decodeFormats: Vector<BarcodeFormat>? = null
    private var characterSet: String? = null
    private var inactivityTimer: InactivityTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playBeep = false
    private var vibrate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_qrscanner)
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0)
        BarUtils.addMarginTopEqualStatusBarHeight(this, findViewById(R.id.rl_title))
        findViewById<View>(R.id.iv_back).setOnClickListener { finish() }
        viewfinderView = findViewById<View>(R.id.viewfinder_view) as ViewfinderView
        viewfinderView!!.setOnSelectFromAlbumListener(OnSelectFromAlbumListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
                return@OnSelectFromAlbumListener
            }
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_PIC)
        })
        init()
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
        }
    }

    private fun init() {
        CameraManager.init(application)
        hasSurface = false
        inactivityTimer = InactivityTimer(this)
    }

    override fun onResume() {
        super.onResume()
        val surfaceView = findViewById<View>(R.id.preview_view) as SurfaceView
        val surfaceHolder = surfaceView.holder
        if (hasSurface) {
            initCamera(surfaceHolder)
        } else {
            surfaceHolder.addCallback(this)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }
        decodeFormats = null
        characterSet = null
        playBeep = true
        val audioService = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioService.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false
        }
        initBeepSound()
        vibrate = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PIC) {
                lifecycleScope.launch(Dispatchers.Main.immediate) {
                    try {
                        val selectedImage = data!!.data //获取系统返回的照片的Uri
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = contentResolver.query(selectedImage!!,
                                filePathColumn, null, null, null) //从系统表中查询指定Uri对应的照片
                        cursor!!.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        val path = cursor.getString(columnIndex) //获取照片路径
                        cursor.close()
                        val result = withContext(Dispatchers.IO) {
                            val bitmap = PicUtils.getBitmapFromPath(path)
                            QRCodeUtil.decodeFromPicture(bitmap)
                        }
                        handleDecode(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2) {
            when (permissions[0]) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, SELECT_PIC)
                } else {
                    Toast.makeText(this, R.string.chat_error_permission_denied, Toast.LENGTH_SHORT).show()
                }
                Manifest.permission.CAMERA -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, R.string.chat_error_permission_denied, Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }
        CameraManager.get().closeDriver()
    }

    override fun onDestroy() {
        inactivityTimer!!.shutdown()
        super.onDestroy()
    }

    private val FIND_TYPE_QR_CODE = 2
    /**
     * 处理扫描结果
     */
    override fun handleDecode(result: Result) {
        inactivityTimer!!.onActivity()
        playBeepSoundAndVibrate()
        if (result == null) {
            Toast.makeText(this@QRScannerActivity, R.string.chat_error_qcode_fail, Toast.LENGTH_SHORT).show()
            return
        }
        val resultString: String = result.getText()
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(this@QRScannerActivity, "Scan failed!", Toast.LENGTH_SHORT).show()
        } else {
            QRCodeHelper.process(this, resultString)
            finish()
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder)
        } catch (ioe: IOException) {
            return
        } catch (e: RuntimeException) {
            return
        }
        if (handler == null) {
            handler = CaptureActivityHandler(this, decodeFormats, characterSet)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int,
                                height: Int) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
    }

    override fun getViewfinderView(): ViewfinderView {
        return viewfinderView!!
    }

    override fun getHandler(): Handler {
        return handler!!
    }

    override fun drawViewfinder() {
        viewfinderView!!.drawViewfinder()
    }

    private fun initBeepSound() {
        if (playBeep && mediaPlayer == null) { // The volume on STREAM_SYSTEM is not adjustable, and users found it
// too loud,
// so we now play on the music stream.
            volumeControlStream = AudioManager.STREAM_MUSIC
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer!!.setOnCompletionListener(beepListener)
            val file = resources.openRawResourceFd(
                    R.raw.beep)
            try {
                mediaPlayer!!.setDataSource(file.fileDescriptor,
                        file.startOffset, file.length)
                file.close()
                mediaPlayer!!.setVolume(BEEP_VOLUME, BEEP_VOLUME)
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                mediaPlayer = null
            }
        }
    }

    private fun playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer!!.start()
        }
        if (vibrate) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATE_DURATION)
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private val beepListener = OnCompletionListener { mediaPlayer -> mediaPlayer.seekTo(0) }

    companion object {
        const val SELECT_PIC = 1
        private const val BEEP_VOLUME = 0.10f
        private const val VIBRATE_DURATION = 200L
    }
}