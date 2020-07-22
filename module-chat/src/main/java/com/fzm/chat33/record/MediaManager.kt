package com.fzm.chat33.record

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.text.TextUtils
import com.fuzamei.common.utils.RoomUtils

import com.fuzamei.common.utils.ToolUtils
import com.fzm.chat33.core.Chat33
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.manager.DownloadManager

import java.io.File
import java.io.IOException

import com.fuzamei.componentservice.config.AppConfig
import com.fuzamei.componentservice.config.AppPreference
import com.fzm.chat33.core.bean.param.toDecParams
import com.fzm.chat33.core.db.ChatDatabase
import com.fzm.chat33.core.manager.FileEncryption
import com.fzm.chat33.core.manager.toByteArray
import com.fzm.chat33.core.manager.toCacheFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object MediaManager {

    private var mPlayer: MediaPlayer? = null

    private var isPause: Boolean = false
    //    private static boolean isReset = true;//是否被重置了 如果false 则表示正在播放

    fun playSound(filePathString: String, audioManager: AudioManager, onCompletionListener: MediaPlayer.OnCompletionListener) {
        // TODO Auto-generated method stub
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
            //保险起见，设置报错监听
            mPlayer!!.setOnErrorListener { mp, what, extra ->
                // TODO Auto-generated method stub
                mPlayer!!.reset()
                false
            }
        } else {
            mPlayer!!.reset()//就恢复
        }

        try {
            audioManager.mode = AppPreference.SOUND_PLAY_MODE
            mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mPlayer!!.setOnCompletionListener(onCompletionListener)

            if (filePathString.startsWith("http")) {
                mPlayer!!.setDataSource(Chat33.getContext(), Uri.parse(filePathString), ToolUtils.getCookieHeaderMap())
            } else {
                mPlayer!!.setDataSource(filePathString)
            }

            mPlayer!!.prepareAsync()
            mPlayer!!.setOnPreparedListener {
                mPlayer!!.start()
                //                    isReset = false;
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun playSound(message: ChatMessage?, audioManager: AudioManager,
                  onCompletionListener: MediaPlayer.OnCompletionListener) = GlobalScope.launch(Dispatchers.Main) {
        if (mPlayer == null) {
            mPlayer = MediaPlayer()
            //保险起见，设置报错监听
            mPlayer!!.setOnErrorListener { mp, what, extra ->
                mPlayer!!.reset()
                false
            }
        } else {
            mPlayer!!.reset()//就恢复
        }

        if (message == null) {
            return@launch
        }
        if (TextUtils.isEmpty(message.msg.localPath)) {
            val file = withContext(Dispatchers.IO) {
                DownloadManager.downloadTemp(message.msg.mediaUrl)
            } ?: return@launch
            message.msg.localPath = file.path
            RoomUtils.run { ChatDatabase.getInstance().chatMessageDao().insert(message) }
        }
        val dataFile = if (AppConfig.FILE_ENCRYPT && message.msg.localPath.contains(AppConfig.ENC_PREFIX)) {
            FileEncryption.decrypt(message.toDecParams(),
                    File(message.msg.localPath).toByteArray())?.toCacheFile(message.msg.localPath)
        } else {
            File(message.msg.localPath)
        }

        try {
            audioManager.mode = AppPreference.SOUND_PLAY_MODE
            mPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mPlayer!!.setOnCompletionListener(onCompletionListener)

            mPlayer!!.setDataSource(dataFile?.path ?: "")

            mPlayer!!.prepareAsync()
            mPlayer!!.setOnPreparedListener {
                mPlayer!!.start()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    //停止函数
    fun pause() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.pause()
            isPause = true
        }
    }

    //停止函数
    fun stop() {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.reset()
            //            isReset = true;
        }
    }

    //继续
    fun resume() {
        if (mPlayer != null && isPause) {
            mPlayer!!.start()
            isPause = false
        }
    }

    fun release() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }
}