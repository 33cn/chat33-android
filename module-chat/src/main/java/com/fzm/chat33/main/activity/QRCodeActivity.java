package com.fzm.chat33.main.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.FinanceUtils;
import com.fuzamei.common.utils.ImageUtils;
import com.fuzamei.common.utils.QRCodeUtil;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.helper.WeChatHelper;
import com.fzm.chat33.BuildConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.PromoteBriefInfo;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.core.global.UserInfo;
import com.fzm.chat33.main.mvvm.PromoteDetailViewModel;
import com.fzm.chat33.widget.ChatAvatarView;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

import static com.fzm.chat33.core.global.Chat33Const.CHANNEL_FRIEND;
import static com.fzm.chat33.core.global.Chat33Const.SAVE_IMAGE_PERMISSION;
import static com.fzm.chat33.core.global.Chat33Const.SHARE_IMAGE_PERMISSION;

/**
 * @author zhengjy
 * @since 2018/10/24
 * Description:我的二维码界面
 */
@Route(path = AppRoute.QR_CODE)
public class QRCodeActivity extends DILoadableActivity implements View.OnClickListener {

    private ChatAvatarView iv_avatar;
    private TextView tv_title, tv_name, tv_uid, tv_invite_code, tv_tips, tv_amount, tv_unit, tv_invite_tips;
    private View iv_back, ll_invite_info, ll_invite_code, rl_share_view, ll_share_chat33, ll_share_moments, ll_share_wechat, ll_save;
    private ImageView iv_my_qr;

    @Autowired
    public String id;
    @Autowired(name = "content")
    public String qr_content;
    @Autowired
    public String avatar;
    @Autowired
    public String name;
    @Autowired
    public int channelType;

    private String uid;
    private PromoteBriefInfo mInfo;


    @Inject
    public ViewModelProvider.Factory provider;
    private PromoteDetailViewModel viewModel;

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_color_blue), 0);
        BarUtils.setStatusBarLightMode(this, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_qrcode;
    }

    @Override
    protected void initView() {
        ARouter.getInstance().inject(this);
        uid = qr_content;
        viewModel = ViewModelProviders.of(this, provider).get(PromoteDetailViewModel.class);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_title = findViewById(R.id.tv_title);
        tv_name = findViewById(R.id.tv_name);
        iv_back = findViewById(R.id.iv_back);
        tv_uid = findViewById(R.id.tv_uid);
        iv_my_qr = findViewById(R.id.iv_my_qr);
        tv_tips = findViewById(R.id.tv_tips);
        tv_amount = findViewById(R.id.tv_amount);
        tv_unit = findViewById(R.id.tv_unit);
        tv_invite_tips = findViewById(R.id.tv_invite_tips);
        rl_share_view = findViewById(R.id.rl_share_view);
        ll_share_chat33 = findViewById(R.id.ll_share_chat33);
        ll_share_moments = findViewById(R.id.ll_share_moments);
        ll_share_wechat = findViewById(R.id.ll_share_wechat);
        ll_save = findViewById(R.id.ll_save);
        ll_invite_code = findViewById(R.id.ll_invite_code);
        tv_invite_code = findViewById(R.id.tv_invite_code);
        ll_invite_info = findViewById(R.id.ll_invite_info);
        viewModel.getPromoteDetail().observe(this, it -> {
            mInfo = it;
            if (it.getPrimary() != null) {
                tv_amount.setText(FinanceUtils.stripZero(it.getPrimary().getTotal()));
                tv_unit.setText(it.getPrimary().getCurrency());
            }
            tv_invite_tips.setText(getString(R.string.chat_invite_count, it.getInviteNum()));
        });
    }

    @Override
    protected void initData() {
        String shareId;
        if (channelType == Chat33Const.CHANNEL_FRIEND) {
            tv_uid.setText(getString(R.string.chat_tips_user_uid, qr_content));
            shareId = "?uid=" + qr_content;
            tv_title.setText(R.string.chat_title_my_qr_code);
            tv_tips.setText(getString(R.string.qr_code_friend_tips, getString(R.string.application_name)));
            if (!TextUtils.isEmpty(avatar)) {
                Glide.with(this).load(avatar)
                        .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round))
                        .into(iv_avatar);
            } else {
                iv_avatar.setImageResource(R.mipmap.default_avatar_round);
            }
            iv_avatar.setIconRes(UserInfo.getInstance().isIdentified() ? R.drawable.ic_user_identified : -1);
        } else {
            tv_uid.setText(getString(R.string.chat_tips_group_uid2, qr_content));
            shareId = "?gid=" + qr_content + "&uid=" + UserInfo.getInstance().uid;
            tv_title.setText(R.string.chat_title_group_qr_code);
            tv_tips.setText(getString(R.string.qr_code_group_tips, getString(R.string.application_name)));
            if (!TextUtils.isEmpty(avatar)) {
                Glide.with(this).load(avatar)
                        .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_room))
                        .into(iv_avatar);
            } else {
                iv_avatar.setImageResource(R.mipmap.default_avatar_room);
            }
            RoomUtils.subscribe(ChatDatabase.getInstance().roomsDao().mayGetRoomById(id), new Consumer<RoomListBean>() {
                @Override
                public void accept(RoomListBean roomListBean) throws Exception {
                    iv_avatar.setIconRes(roomListBean.isIdentified() ? R.drawable.ic_group_identified : -1);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {

                }
            });
        }
        qr_content = UserInfo.getInstance().appendCode(AppConfig.APP_SHARE_URL + shareId);
        tv_name.setText(name);
        final Bitmap bitmap = ((BitmapDrawable) ContextCompat.getDrawable(this, R.mipmap.ic_launcher_chat33)).getBitmap();

        iv_my_qr.setImageBitmap(QRCodeUtil.createQRCodeBitmapWithLogo(qr_content, 350, 350, bitmap));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void setEvent() {
        iv_back.setOnClickListener(this);
        tv_uid.setOnClickListener(this);
        iv_my_qr.setOnClickListener(this);
        ll_share_chat33.setOnClickListener(this);
        ll_share_moments.setOnClickListener(this);
        ll_share_wechat.setOnClickListener(this);
        ll_save.setOnClickListener(this);
        if (ll_invite_info != null) {
            ll_invite_info.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.ll_invite_info) {
            ARouter.getInstance().build(AppRoute.PROMOTE_DETAIL).withSerializable("info", mInfo).navigation();
        } else if (id == R.id.tv_uid) {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("uid", uid);
            if (cm != null) {
                cm.setPrimaryClip(mClipData);
                if (channelType == CHANNEL_FRIEND) {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_code_copyed));
                } else {
                    ShowUtils.showToastNormal(instance, getString(R.string.chat_tips_group_code_copyed));
                }
            }
        } else if (id == R.id.iv_my_qr) {

        } else if (id == R.id.ll_share_chat33) {
            shareQRCode();
        } else if (id == R.id.ll_share_moments) {
            WeChatHelper.INS.shareWeb(qr_content, getString(R.string.chat_tips_share_title),
                    getString(R.string.chat_tips_share_content), WeChatHelper.TIMELINE);
        } else if (id == R.id.ll_share_wechat) {
            WeChatHelper.INS.shareWeb(qr_content, getString(R.string.chat_tips_share_title),
                    getString(R.string.chat_tips_share_content), WeChatHelper.SESSION);
        } else if (id == R.id.ll_save) {
            if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(SAVE_IMAGE_PERMISSION, this)) {
                return;
            }
            String path = saveQRCodeImage();
            if (TextUtils.isEmpty(path)) {
                ShowUtils.showToast(instance, getString(R.string.chat_tips_img_not_exist));
            } else {
                ShowUtils.showToastNormal(this, getString(R.string.chat_tips_code_saved));
            }
        }
    }

    private void shareQRCode() {
        if (!com.fzm.chat33.utils.FileUtils.isGrantExternalRW(SHARE_IMAGE_PERMISSION, this)) {
            return;
        }
        String path = saveQRCodeImage();
        if (TextUtils.isEmpty(path)) {
            ShowUtils.showToast(instance, getString(R.string.chat_tips_img_not_exist));
            return;
        }
        ChatFile chatFile = ChatFile.newImage("", path, rl_share_view.getHeight(), rl_share_view.getWidth());
        ARouter.getInstance().build(AppRoute.CONTACT_SELECT)
                .withSerializable("chatFile", chatFile)
                .navigation();
    }

    private String saveQRCodeImage() {
        if (iv_my_qr.getDrawable() != null) {
            loading(true);
            String path = ImageUtils.saveBitmapToGallery(getShareBitmap());
            dismiss();
            return path;
        }
        return null;
    }

    public Bitmap getShareBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(rl_share_view.getWidth(), rl_share_view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        rl_share_view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SHARE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareQRCode();
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage));
            }
        } else if (requestCode == SAVE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String path = saveQRCodeImage();
                if (TextUtils.isEmpty(path)) {
                    ShowUtils.showToast(instance, getString(R.string.chat_tips_img_not_exist));
                } else {
                    ShowUtils.showToastNormal(this, getString(R.string.chat_tips_code_saved));
                }
            } else {
                ShowUtils.showToast(instance, getString(R.string.chat_permission_storage));
            }
        }
    }
}
