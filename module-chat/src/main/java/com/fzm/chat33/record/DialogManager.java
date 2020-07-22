package com.fzm.chat33.record;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fzm.chat33.R;
import com.fuzamei.common.widget.IconView;

/**
 * 生成Dialog
 */
public class DialogManager {
	/**
	 * 以下为dialog的初始化控件，包括其中的布局文件
	 */
	private Dialog mDialog;
	private TextView mLable;
	private Context mContext;
	private IconView mIconVoice;
	private IconView mIcon2;
	public DialogManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void showRecordingDialog() {
		// TODO Auto-generated method stub

		mDialog = new Dialog(mContext, R.style.Theme_audioDialog);
		// 用layoutinflater来引用布局
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.dialog_audio_record, null);
		mDialog.setContentView(view);


		mIconVoice = (IconView) mDialog.findViewById(R.id.dialog_icon);
		mIcon2 = (IconView) mDialog.findViewById(R.id.dialog_cancel);
		mLable = (TextView) mDialog.findViewById(R.id.recorder_dialogtext);
		mDialog.show();

	}

	/**
	 * 设置正在录音时的dialog界面
	 */
	public void recording() {
		if (mDialog != null && mDialog.isShowing()) {
			mIconVoice.setVisibility(View.VISIBLE);
			mIcon2.setVisibility(View.GONE);
			mLable.setVisibility(View.VISIBLE);

			mIconVoice.setText(R.string.icon_luyin_vol1);
			mLable.setText(R.string.shouzhishanghua);
			mLable.setTextColor(ContextCompat.getColor(mContext, R.color.chat_white));
		}
	}
	/**
	 * 设置倒数录音时的dialog界面
	 */
	public void countdown() {
		if (mDialog != null && mDialog.isShowing()) {
			mIconVoice.setVisibility(View.VISIBLE);
			mIcon2.setVisibility(View.GONE);
			mLable.setVisibility(View.VISIBLE);

			mLable.setText(R.string.shouzhishanghua);
			mLable.setTextColor(ContextCompat.getColor(mContext, R.color.chat_white));
		}
	}

	/**
	 * 取消界面
	 */
	public void wantToCancel() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
			mIconVoice.setVisibility(View.GONE);
			mIcon2.setVisibility(View.VISIBLE);
			mLable.setVisibility(View.VISIBLE);

			mIcon2.setIconText(R.string.icon_yuyin_chehui);
			mLable.setText(R.string.want_to_cancle);
			mLable.setTextColor(ContextCompat.getColor(mContext, R.color.chat_chat_tips));
		}

	}

	// 时间过短
	public void tooShort() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
			mIconVoice.setVisibility(View.GONE);
			mIcon2.setVisibility(View.VISIBLE);
			mLable.setVisibility(View.VISIBLE);

			mIcon2.setIconBackground(R.drawable.voice_to_short);
			mLable.setText(R.string.tooshort);
			mLable.setTextColor(ContextCompat.getColor(mContext, R.color.chat_chat_tips));
		}
	}
	// 时间过长
	public void tooLong() {
		// TODO Auto-generated method stub
		if (mDialog != null && mDialog.isShowing()) {
//			mIconVoice.setVisibility(View.GONE);
//			mIcon2.setVisibility(View.VISIBLE);
//			mLable.setVisibility(View.VISIBLE);
//
//			mIcon2.setIconBackground(R.drawable.voice_to_short);
//			mLable.setText(R.string.toolong);
//			mLable.setTextColor(ContextCompat.getColor(mContext, R.color.chat_room_red2));
		}
	}

	// 隐藏dialog
	public void dismissDialog() {

		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}

	}

	public void updateVoiceLevel(int level) {
		// TODO Auto-generated method stub

		if (mDialog != null && mDialog.isShowing()) {

			//先不改变它的默认状态
//			mIcon.setVisibility(View.VISIBLE);
//			mVoice.setVisibility(View.VISIBLE);
//			mLable.setVisibility(View.VISIBLE);

			//通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
			int resId = mContext.getResources().getIdentifier("icon_luyin_vol" + level,
					"string", mContext.getPackageName());
			mIconVoice.setIconText(resId);
		}

	}

	/**
	 * 更新数字
	 * @param second
	 */
	public void updateVoiceSecond(int second) {
		if (mDialog != null && mDialog.isShowing()) {
			//通过level来找到图片的id，也可以用switch来寻址，但是代码可能会比较长
//			int resId = mContext.getResources().getIdentifier("s_" + second,
//					"drawable", mContext.getPackageName());
			String showLastSeconds = mContext.getString(R.string.chat_speak_time_remain, second);
			mLable.setText(showLastSeconds);
		}
	}

}