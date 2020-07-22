package com.fzm.chat33.ait;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.fzm.chat33.ait.activity.AitSelectorActivity;
import com.fzm.chat33.core.db.bean.RoomUserBean;

import java.util.List;

/**
 * @author zhengjy
 * @since 2019/08/19
 * Description:
 */
public class AitManager implements TextWatcher {

    private String targetId;

    private AitContactsModel aitContactsModel;

    private int curPos;

    private boolean ignoreTextChange = false;

    private AitTextChangeListener listener;
    private OnOpenAitListListener onOpenAitListListener;

    public AitManager(String targetId) {
        this.targetId = targetId;
        aitContactsModel = new AitContactsModel();
    }

    public void setTextChangeListener(AitTextChangeListener listener) {
        this.listener = listener;
    }

    public void setOnOpenAitListListener(OnOpenAitListListener listener) {
        this.onOpenAitListListener = listener;
    }

    public List<String> getAitMembers() {
        return aitContactsModel.getAitMembers();
    }

    public void reset() {
        aitContactsModel.reset();
        ignoreTextChange = false;
        curPos = 0;
    }

    public interface OnOpenAitListListener {

        /**
         * 检测到输入@符号，需要启动@联系人界面
         */
        void onOpenAitList();
    }

    /**
     * ------------------------------ 增加@成员 --------------------------------------
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AitSelectorActivity.getREQUEST_CODE() && resultCode == Activity.RESULT_OK) {
            String account;
            final String name;
            RoomUserBean roomUser = (RoomUserBean) data.getSerializableExtra(AitSelectorActivity.getRESULT_DATA());
            account = roomUser.getId();
            if (!TextUtils.isEmpty(roomUser.getRoomNickname())) {
                name = roomUser.getRoomNickname();
            } else {
                name = roomUser.getNickname();
            }
            insertAitMemberInner(account, name, curPos, false);
        }
    }

    public void insertAitMember(String id, String name, int start) {
        insertAitMemberInner(id, name, start, true);
    }

    private void insertAitMemberInner(String account, String name, int start, boolean needInsertAitInText) {
        // "\u200b"为不可见字符，"\u2004"区别于普通空格
        name = name + "\u2004";
        String content = needInsertAitInText ? "@" + name : name;
        if (listener != null) {
            // 关闭监听
            ignoreTextChange = true;
            // insert 文本到editText
            listener.onTextAdd(content, start, content.length());
            // 开启监听
            ignoreTextChange = false;
        }

        // update 已有的 aitBlock
        aitContactsModel.onInsertText(start, content);

        int index = needInsertAitInText ? start : start - 1;
        // 添加当前到 aitBlock
        aitContactsModel.addAitMember(account, name, index);
    }

    /*
     * ------------------------------ editText 监听 --------------------------------------
     */

    /**
     * 当删除尾部空格时，删除一整个segment,包含界面上也删除
     *
     * @param start 字符串变化起始位置
     * @param count 字符串变化长度
     * @return      是否删除了Segment
     */
    private boolean deleteSegment(int start, int count) {
        if (count != 1) {
            return false;
        }
        boolean result = false;
        AitBlock.AitSegment segment = aitContactsModel.findAitSegmentByEndPos(start);
        if (segment != null) {
            int length = start - segment.start;
            if (listener != null) {
                ignoreTextChange = true;
                listener.onTextDelete(segment.start, length);
                ignoreTextChange = false;
            }
            aitContactsModel.onDeleteText(start, length);
            result = true;
        }
        return result;
    }

    /**
     * @param editable 变化后的Editable
     * @param start    text 变化区块的起始index
     * @param count    text 变化区块的大小
     * @param delete   是否是删除
     */
    private void afterTextChanged(Editable editable, int start, int count, boolean delete) {
        curPos = delete ? start : count + start;
        if (ignoreTextChange) {
            return;
        }
        if (delete) {
            int before = start + count;
            if (deleteSegment(before, count)) {
                return;
            }
            aitContactsModel.onDeleteText(before, count);

        } else {
            if (count <= 0 || editable.length() < start + count) {
                return;
            }
            CharSequence s = editable.subSequence(start, start + count);
            if (s == null) {
                return;
            }
            if (s.toString().equals("@")) {
                // 启动@联系人界面
                if (!TextUtils.isEmpty(targetId)) {
                    if (onOpenAitListListener != null) {
                        onOpenAitListListener.onOpenAitList();
                    }
                }
            }
            aitContactsModel.onInsertText(start, s.toString());
        }
    }

    private int editTextStart;
    private int editTextCount;
    private int editTextBefore;
    private boolean delete;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        delete = count > after;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.editTextStart = start;
        this.editTextCount = count;
        this.editTextBefore = before;
    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTextChanged(s, editTextStart, delete ? editTextBefore : editTextCount, delete);
    }
}

