package com.fzm.chat33.main.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.ImageUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.VibrateUtils;
import com.fuzamei.common.widget.BottomPopupWindow;
import com.fuzamei.common.widget.photoview.PhotoView;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.net.OssModel;
import com.fzm.chat33.main.mvvm.SettingViewModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.fzm.chat33.core.global.Chat33Const.TAKE_IMAGE_PERMISSION;
import static com.fzm.chat33.core.global.Chat33Const.UPLOAD_IMAGE_PERMISSION;

/**
 * @author zhengjy
 * @since 2018/10/24
 * Description:用户头像编辑界面
 */
@Route(path = AppRoute.EDIT_AVATAR)
public class EditAvatarActivity extends DILoadableActivity implements View.OnClickListener {

    private View iv_back, iv_more, rl_title;
    private PhotoView iv_big_avatar;
    private BottomPopupWindow popupWindow;
    private String[] options;

    @Autowired
    public String avatar;
    @Autowired
    public String id;
    @Autowired
    public int channelType = Chat33Const.CHANNEL_FRIEND;

    @Inject
    public ViewModelProvider.Factory provider;
    private SettingViewModel viewModel;

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_transparent), 0);
        BarUtils.addMarginTopEqualStatusBarHeight(this, rl_title);
        BarUtils.setStatusBarLightMode(this, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_avatar;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        viewModel = ViewModelProviders.of(this, provider).get(SettingViewModel.class);
        rl_title = findViewById(R.id.rl_title);
        iv_back = findViewById(R.id.iv_back);
        iv_more = findViewById(R.id.iv_more);
        iv_big_avatar = findViewById(R.id.iv_big_avatar);
        viewModel.getEditAvatar().observe(this, it -> {
            dismiss();
            if(it.getResult() != null) {
                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_success));
                finish();
            } else {
                ShowUtils.showToast(instance, it.getException().getMessage());
            }
        });
    }

    @Override
    protected void initData() {
        options = getResources().getStringArray(R.array.chat_choose_edit_avatar);
        RequestOptions options = new RequestOptions()
                .centerInside()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH);
        if (!TextUtils.isEmpty(avatar)) {
            Glide.with(this).load(avatar).apply(options).into(iv_big_avatar);
        } else {
            if (channelType == Chat33Const.CHANNEL_FRIEND) {
                iv_big_avatar.setImageResource(R.mipmap.default_avatar_big);
            } else {
                iv_big_avatar.setImageResource(R.mipmap.default_avatar_room_big);
            }
        }
    }

    @Override
    protected void setEvent() {
        iv_back.setOnClickListener(this);
        iv_more.setOnClickListener(this);
        iv_big_avatar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                VibrateUtils.simple(instance, 50);
                showPopup();
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_more) {
            showPopup();
        }
    }

    private void showPopup() {
        if (popupWindow == null) {
            popupWindow = new BottomPopupWindow(this, Arrays.asList(options),
                    new BottomPopupWindow.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, PopupWindow popupWindow, int position) {
                            popupWindow.dismiss();
                            if (position == 0) {
                                if (!com.fzm.chat33.utils.FileUtils.isGrantCamera(TAKE_IMAGE_PERMISSION, instance)) {
                                    return;
                                }
                                ARouter.getInstance().build(AppRoute.CAMERA_SHOOT).navigation(instance, REQUEST_TAKE_CODE);
                            } else if (position == 1) {
                                if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(UPLOAD_IMAGE_PERMISSION, instance)) {
                                    return;
                                }
                                PictureSelector.create(instance)
                                        .openGallery(PictureMimeType.ofImage())
                                        .theme(R.style.chat_picture_style)
                                        .maxSelectNum(1)
                                        .imageSpanCount(4)
                                        .previewImage(true)
                                        .isCamera(false)
                                        .forResult(REQUEST_LIST_CODE);
                            } else if (position == 2) {
                                saveAvatar();
                            }
                        }
                    });
        }
        popupWindow.showAtLocation(iv_more, Gravity.BOTTOM, 0, 0);
    }

    private void saveAvatar() {
        if (iv_big_avatar.getDrawable() != null) {
            loading(false);
            ImageUtils.saveImageToGallery(iv_big_avatar.getDrawable());
            dismiss();
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved));
            dismiss();
        }
    }

    private final int REQUEST_LIST_CODE = 2;
    private final int REQUEST_TAKE_CODE = 3;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == UPLOAD_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PictureSelector.create(instance)
                        .openGallery(PictureMimeType.ofImage())
                        .theme(R.style.chat_picture_style)
                        .maxSelectNum(1)
                        .imageSpanCount(4)
                        .previewImage(true)
                        .isCamera(false)
                        .forResult(REQUEST_LIST_CODE);
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage));
            }
        } else if (requestCode == TAKE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ARouter.getInstance().build(AppRoute.CAMERA_SHOOT).navigation(instance, REQUEST_TAKE_CODE);
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_universal));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 图片结果回调
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_LIST_CODE) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                List<String> pathList = new ArrayList<>();
                for (LocalMedia media : selectList) {
                    pathList.add(media.getPath());
                }
                if (pathList.size() > 0) {
                    compressAndUploadImage(pathList.get(0));
                } else {
                    ShowUtils.showToast(instance, getString(R.string.chat_tips_img_not_exist));
                }
            } else if (requestCode == REQUEST_TAKE_CODE) {
                String imageUrl = data.getStringExtra("result");
                compressAndUploadImage(imageUrl);
            }
        }
    }

    private void editAvatar(final String avatar) {
        viewModel.editAvatar(channelType, id, avatar);
    }

    private void compressAndUploadImage(String imagePath) {
        Luban.with(this)
                .load(imagePath)
                .setTargetDir(com.fzm.chat33.utils.FileUtils.getImageCachePath(this))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        loading(false);
                    }

                    @Override
                    public void onSuccess(final File file) {
                        if (!file.exists()) {
                            ShowUtils.showToast(instance, getString(R.string.chat_tips_img_zip_fail));
                            return;
                        }

                        OssModel.getInstance().uploadMedia(null, file.getAbsolutePath(), OssModel.PICTURE,
                                new OssModel.UpLoadCallBack() {
                                    @Override
                                    public void onSuccess(@NotNull String url) {
                                        if (!TextUtils.isEmpty(url)) {
                                            editAvatar(url);
                                        }
                                    }

                                    @Override
                                    public void onProgress(long currentSize, long totalSize) {

                                    }

                                    @Override
                                    public void onFailure(@NotNull String path) {
                                        dismiss();
                                        ShowUtils.showToast(instance, getString(R.string.chat_tips_pic_upload_fail));
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismiss();
                        ShowUtils.showToast(instance, getString(R.string.chat_tips_img_zip_fail));
                    }
                })
                .launch();
    }
}
