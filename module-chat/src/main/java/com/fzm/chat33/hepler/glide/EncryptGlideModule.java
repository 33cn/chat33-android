package com.fzm.chat33.hepler.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.fzm.chat33.core.db.bean.ChatMessage;

import java.io.InputStream;

/**
 * @author zhengjy
 * @since 2019/12/18
 * Description:
 */
@GlideModule
public class EncryptGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        glide.getRegistry().append(ChatMessage.class, InputStream.class, new ChatEncryptLoader.LoaderFactory());
        glide.getRegistry().append(SingleKeyEncrypt.class, InputStream.class, new ChatEncryptLoader2.LoaderFactory());
    }
}
