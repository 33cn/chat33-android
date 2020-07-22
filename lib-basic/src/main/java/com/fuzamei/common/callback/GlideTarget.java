package com.fuzamei.common.callback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * @author zhengjy
 * @since 2018/11/23
 * Description:
 */
public class GlideTarget extends CustomViewTarget<ImageView, Drawable> {

    protected Object tag;
    protected int key;
    private Callback callback;

    public GlideTarget(@NonNull ImageView view, int key, @NonNull Object tag) {
        this(view, key, tag, null);
    }

    public GlideTarget(@NonNull ImageView view, int key, @NonNull Object tag, Callback callback) {
        super(view);
        this.tag = tag;
        this.key = key;
        this.callback = callback;
        this.view.setTag(key, tag);
    }

    @Override
    protected void onResourceCleared(@Nullable Drawable placeholder) {

    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {

    }

    @Override
    protected void onResourceLoading(@Nullable Drawable placeholder) {
        if (view.getContext() != null && view.getContext() instanceof Activity && !((Activity) view.getContext()).isFinishing()) {
            if (tag.equals(view.getTag(key))) {
                if (callback != null) {
                    callback.onResourceLoading(view, placeholder);
                }
            }
        }
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        if (view.getContext() != null && view.getContext() instanceof Activity && !((Activity) view.getContext()).isFinishing()) {
            if (tag.equals(view.getTag(key))) {
                if (callback != null) {
                    callback.onResourceReady(view, resource, transition);
                } else {
                    view.setImageDrawable(resource);
                }
            }
        }
    }

    public interface Callback {

        void onResourceLoading(ImageView view, @Nullable Drawable placeholder);

        void onResourceReady(ImageView view, @NonNull Drawable resource, @Nullable Transition<? super Drawable> transition);
    }
}
