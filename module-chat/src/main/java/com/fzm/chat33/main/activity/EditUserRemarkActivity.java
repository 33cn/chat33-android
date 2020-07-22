package com.fzm.chat33.main.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.bus.LiveBus;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.ImageUtils;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.widget.BottomPopupWindow;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.app.BusEvent;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.event.NicknameRefreshEvent;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.RemarkImage;
import com.fzm.chat33.core.bean.RemarkPhone;
import com.fzm.chat33.core.bean.param.EditExtRemarkParam;
import com.fzm.chat33.core.bean.param.EncryptParams;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.manager.CipherManager;
import com.fzm.chat33.core.net.OssModel;
import com.fzm.chat33.hepler.glide.GlideApp;
import com.fzm.chat33.hepler.glide.SingleKeyEncrypt;
import com.fzm.chat33.main.mvvm.EditUserRemarkViewModel;
import com.fzm.chat33.utils.SimpleTextWatcher;
import com.fuzamei.componentservice.widget.CommonTitleBar;
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
 * @since 2019/01/30
 * Description:好友备注信息修改页面
 */
@Route(path = AppRoute.EDIT_USER_INFO)
public class EditUserRemarkActivity extends DILoadableActivity implements View.OnClickListener {

    @Autowired
    public String id;
    @Autowired
    public FriendBean userInfo;

    private final RemarkPhone ADD_PHONE = new RemarkPhone();
    private final RemarkImage ADD_IMAGE = new RemarkImage();
    private final int MAX_PHONE = 5;
    private final int MAX_IMAGE = 3;
    private String[] options;
    private String[] options1;

    private String name;
    private List<RemarkPhone> phones = new ArrayList<>();
    private String desc;
    private List<RemarkImage> images = new ArrayList<>();

    private CommonTitleBar ctb_title;
    private TextView tv_image_count, tv_name_count, tv_desc_count;
    private RecyclerView rv_phone, rv_image;
    private EditText et_name, et_desc;
    private View tv_submit;
    private BottomPopupWindow popupWindow;
    private BottomPopupWindow modifyPopup;

    private CommonAdapter<RemarkPhone> phoneAdapter;
    private CommonAdapter<RemarkImage> imageAdapter;

    private EditExtRemarkParam param = new EditExtRemarkParam();
    @Inject
    public ViewModelProvider.Factory provider;
    private EditUserRemarkViewModel viewModel;

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_friend;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        viewModel = ViewModelProviders.of(this, provider).get(EditUserRemarkViewModel.class);
        ctb_title = findViewById(R.id.ctb_title);
        tv_submit = findViewById(R.id.tv_submit);
        et_name = findViewById(R.id.et_name);
        tv_image_count = findViewById(R.id.tv_image_count);
        rv_phone = findViewById(R.id.rv_phone);
        rv_image = findViewById(R.id.rv_image);
        tv_name_count = findViewById(R.id.tv_name_count);
        tv_desc_count = findViewById(R.id.tv_desc_count);
        et_desc = findViewById(R.id.et_desc);


        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getSetFriendExtRemark().observe(this, it-> {
            ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_edit_success));
            LiveBus.of(BusEvent.class).nicknameRefresh()
                    .setValue(new NicknameRefreshEvent(EditUserRemarkActivity.this.id, param.remark));
            LiveBus.of(BusEvent.class).contactsRefresh().setValue(1);
            RoomUtils.run(() -> ChatDatabase.getInstance().friendsDao().updateRemark(id, param.remark));
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    protected void initData() {
        options = getResources().getStringArray(R.array.chat_choose_friend_remark1);
        options1 = getResources().getStringArray(R.array.chat_choose_friend_remark2);
        ctb_title.setMiddleText(getString(R.string.chat_title_friend_remark));
        ctb_title.setRightVisible(false);
        ctb_title.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupInfo();

        rv_phone.setLayoutManager(new LinearLayoutManager(this));
        rv_image.setLayoutManager(new LinearLayoutManager(this));
        phoneAdapter = new CommonAdapter<RemarkPhone>(this, R.layout.adapter_remark_phone, phones) {
            @Override
            protected void convert(final ViewHolder holder, final RemarkPhone remarkPhone, int position) {
                if (ADD_PHONE.equals(remarkPhone)) {
                    holder.setVisible(R.id.ll_add_phone, true);
                    holder.setVisible(R.id.ll_phone_info, false);
                    holder.setOnClickListener(R.id.ll_add_phone, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (phones.size() < MAX_PHONE) {
                                int index = phones.size() - 1;
                                phones.add(index, new RemarkPhone(getString(R.string.chat_tips_phone_remark), ""));
                                phoneAdapter.notifyItemInserted(index);
                            } else {
                                phones.add(new RemarkPhone(getString(R.string.chat_tips_phone_remark), ""));
                                phoneAdapter.notifyItemInserted(phones.size());
                                phones.remove(ADD_PHONE);
                                phoneAdapter.notifyItemRemoved(phones.size() - 1);
                            }
                        }
                    });
                } else {
                    holder.setVisible(R.id.ll_add_phone, false);
                    holder.setVisible(R.id.ll_phone_info, true);
                    holder.setText(R.id.et_phone_remark, remarkPhone.remark);
                    holder.setText(R.id.et_phone, remarkPhone.phone);
                    holder.setOnClickListener(R.id.iv_delete_phone, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.getView(R.id.et_phone_remark).clearFocus();
                            holder.getView(R.id.et_phone).clearFocus();
                            int index = phones.indexOf(remarkPhone);
                            phones.remove(index);
                            phoneAdapter.notifyItemRemoved(index);
                            if (phones.size() < MAX_PHONE && !phones.contains(ADD_PHONE)) {
                                phones.add(ADD_PHONE);
                                phoneAdapter.notifyItemInserted(phones.size());
                            }
                        }
                    });
                    EditText et_phone_remark = holder.getView(R.id.et_phone_remark);
                    EditText et_phone = holder.getView(R.id.et_phone);
                    et_phone_remark.addTextChangedListener(new SimpleTextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            remarkPhone.remark = s.toString();
                        }
                    });
                    et_phone.addTextChangedListener(new SimpleTextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            remarkPhone.phone = s.toString();
                        }
                    });
                }
            }
        };
        rv_phone.setAdapter(phoneAdapter);

        imageAdapter = new CommonAdapter<RemarkImage>(this, R.layout.adapter_remark_image, images) {
            @Override
            protected void convert(final ViewHolder holder, final RemarkImage remarkImage, final int position) {
                if (ADD_IMAGE.equals(remarkImage)) {
                    holder.setVisible(R.id.ll_add_image, true);
                    holder.setVisible(R.id.rl_image, false);
                    holder.setOnClickListener(R.id.ll_add_image, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPopup(v);
                        }
                    });
                } else {
                    holder.setVisible(R.id.ll_add_image, false);
                    holder.setVisible(R.id.rl_image, true);
                    final ImageView imageView = holder.getView(R.id.iv_remark);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    GlideApp.with(instance).load(new SingleKeyEncrypt(remarkImage.showUrl, CipherManager.getPublicKey()))
                            .apply(new RequestOptions().placeholder(R.drawable.bg_image_placeholder))
                            .into(imageView);
                    holder.setOnClickListener(R.id.iv_delete, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int index = images.indexOf(remarkImage);
                            images.remove(index);
                            imageAdapter.notifyItemRemoved(index);
                            if (images.size() < MAX_IMAGE && !images.contains(ADD_IMAGE)) {
                                images.add(ADD_IMAGE);
                                imageAdapter.notifyItemInserted(images.size());
                            }
                            tv_image_count.setText(images.size() - 1 + "/" + MAX_IMAGE);
                        }
                    });
                    holder.setOnLongClickListener(R.id.rl_image, new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            showModifyPopup((ImageView) holder.getView(R.id.iv_remark), images.indexOf(remarkImage));
                            return true;
                        }
                    });
                    holder.setOnClickListener(R.id.rl_image, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int index = images.indexOf(remarkImage);
                            ArrayList<String> imgList = new ArrayList<>();
                            for (RemarkImage image : images) {
                                if (!ADD_IMAGE.equals(image)) {
                                    imgList.add(image.showUrl);
                                }
                            }
                            Intent intent = new Intent(instance, ShowBigImageActivity.class);
                            intent.putStringArrayListExtra("images", imgList);
                            intent.putExtra("currentIndex", index);
                            instance.startActivity(intent,
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                                            instance, v, "shareImage").toBundle());
                        }
                    });
                }
            }
        };
        rv_image.setNestedScrollingEnabled(false);
        rv_image.setAdapter(imageAdapter);
    }

    private void setupInfo() {
        name = userInfo.getRemark();
        if (!TextUtils.isEmpty(userInfo.getExtRemark().description)) {
            desc = userInfo.getExtRemark().description;
        } else {
            desc = "";
        }
        if (!TextUtils.isEmpty(name)) {
            if (name.length() > 20) {
                name = name.substring(0, 20);
            }
            et_name.setText(name);
            et_name.setSelection(name.length());
            tv_name_count.setText(getString(R.string.chat_tips_num_20, name.length()));
        } else {
            tv_name_count.setText(getString(R.string.chat_tips_num_20, 0));
        }
        if (!TextUtils.isEmpty(desc)) {
            et_desc.setText(desc);
            et_desc.setSelection(desc.length());
            tv_desc_count.setText(getString(R.string.chat_tips_num_400, desc.length()));
        } else {
            tv_desc_count.setText(getString(R.string.chat_tips_num_400, 0));
        }
        if (userInfo.getExtRemark().phones != null) {
            phones.addAll(userInfo.getExtRemark().phones);
        }
        if (userInfo.getExtRemark().images != null) {
            for (String image : userInfo.getExtRemark().images) {
                images.add(new RemarkImage(image, image));
            }
        }
        if (phones.size() < MAX_PHONE) {
            phones.add(ADD_PHONE);
        }
        if (images.size() < MAX_IMAGE) {
            images.add(ADD_IMAGE);
            tv_image_count.setText(images.size() - 1 + "/" + MAX_IMAGE);
        } else {
            tv_image_count.setText(images.size() + "/" + MAX_IMAGE);
        }

        param.id = id;
        param.remark = name;
        param.telephones = phones;
        param.description = desc;
        param.pictures = userInfo.getExtRemark().images;
    }

    private final int REQUEST_LIST_CODE = 2;
    private final int REQUEST_TAKE_CODE = 3;

    private void showPopup(View view) {
        popupWindow = new BottomPopupWindow(this, Arrays.asList(options),
                new BottomPopupWindow.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, PopupWindow popupWindow, int position) {
                        popupWindow.dismiss();
                        if (position == 0) {
                            if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(TAKE_IMAGE_PERMISSION, instance)) {
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
                                    .maxSelectNum(MAX_IMAGE - images.size() + 1)
                                    .imageSpanCount(4)
                                    .previewImage(true)
                                    .isCamera(false)
                                    .forResult(REQUEST_LIST_CODE);
                        }
                    }
                });
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    private void showModifyPopup(final ImageView image, final int adapterPos) {
        modifyPopup = new BottomPopupWindow(this, Arrays.asList(options1),
                new BottomPopupWindow.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, PopupWindow popupWindow, int position) {
                        popupWindow.dismiss();
                        if (position == 0) {
                            ChatFile chatFile = ChatFile.newImage(images.get(adapterPos).ossUrl, "",
                                    image.getDrawable().getIntrinsicHeight(), image.getDrawable().getIntrinsicWidth());
                            ARouter.getInstance().build(AppRoute.CONTACT_SELECT).withSerializable("chatFile", chatFile).navigation();
                        } else if (position == 1) {
                            if (image.getDrawable() != null) {
                                loading(false);
                                ImageUtils.saveImageToGallery(image.getDrawable());
                                dismiss();
                                ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_img_saved));
                                dismiss();
                            }
                        } else if (position == 2) {
                            images.remove(adapterPos);
                            imageAdapter.notifyItemRemoved(adapterPos);
                            if (images.size() < MAX_IMAGE && !images.contains(ADD_IMAGE)) {
                                images.add(ADD_IMAGE);
                                imageAdapter.notifyItemInserted(images.size());
                            }
                            tv_image_count.setText(images.size() - 1 + "/" + MAX_IMAGE);
                        }
                    }
                });
        modifyPopup.showAtLocation(image, Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected void setEvent() {
        tv_submit.setOnClickListener(this);
        et_name.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    tv_name_count.setText(getString(R.string.chat_tips_num_20, 0));
                } else {
                    tv_name_count.setText(getString(R.string.chat_tips_num_20, s.length()));
                }
                param.remark = s.toString();
            }
        });
        if (et_desc != null) {
            et_desc.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s)) {
                        tv_desc_count.setText(getString(R.string.chat_tips_num_400, 0));
                    } else {
                        tv_desc_count.setText(getString(R.string.chat_tips_num_400, s.length()));
                    }
                    param.description = s.toString();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tv_submit) {
            List<RemarkPhone> tempPhones = new ArrayList<>();
            for (RemarkPhone item : phones) {
                if (!TextUtils.isEmpty(item.phone)) {
                    tempPhones.add(item);
                }
            }
            List<String> tempImages = new ArrayList<>();
            for (RemarkImage image : images) {
                if (!ADD_IMAGE.equals(image)) {
                    tempImages.add(image.ossUrl);
                }
            }
            param.telephones = tempPhones;
            param.pictures = tempImages;
            viewModel.setFriendExtRemark(param);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 图片结果回调
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_LIST_CODE) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                for (LocalMedia media : selectList) {
                    compressAndUploadImage(media.getPath());
                }
            } else if (requestCode == REQUEST_TAKE_CODE) {
                String imageUrl = data.getStringExtra("result");
                compressAndUploadImage(imageUrl);
            }
        }
    }

    private void compressAndUploadImage(final String imagePath) {
        if (imagePath.contains(AppConfig.ENC_PREFIX)) {
            ShowUtils.showToast(instance, getString(R.string.chat_tips_img_not_exist));
            return;
        }
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

                        OssModel.getInstance().uploadMedia(new EncryptParams(CipherManager.getPublicKey()), file.getAbsolutePath(), OssModel.PICTURE,
                                new OssModel.UpLoadCallBack() {
                                    @Override
                                    public void onSuccess(@NotNull String url) {
                                        if (!TextUtils.isEmpty(url)) {
                                            if (images.size() < MAX_IMAGE) {
                                                int index = images.size() - 1;
                                                images.add(index, new RemarkImage(imagePath, url));
                                                imageAdapter.notifyItemInserted(index);
                                                tv_image_count.setText(images.size() - 1 + "/" + MAX_IMAGE);
                                            } else {
                                                images.add(new RemarkImage(imagePath, url));
                                                imageAdapter.notifyItemInserted(images.size());
                                                images.remove(ADD_IMAGE);
                                                imageAdapter.notifyItemRemoved(images.size() - 1);
                                                tv_image_count.setText(images.size() + "/" + MAX_IMAGE);
                                            }
                                        }
                                        dismiss();
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
