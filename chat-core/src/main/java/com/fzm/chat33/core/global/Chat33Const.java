package com.fzm.chat33.core.global;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/18
 * Description:
 */
public class Chat33Const {

    // 聊天室
    @Deprecated
    public static final int CHANNEL_GROUP = 1;
    // 群
    public static final int CHANNEL_ROOM = 2;
    // 好友
    public static final int CHANNEL_FRIEND = 3;

    // 权限
    public static final int SHARE_IMAGE_PERMISSION = 3;
    public static final int SAVE_IMAGE_PERMISSION = 4;
    public static final int UPLOAD_IMAGE_PERMISSION = 5;
    public static final int TAKE_IMAGE_PERMISSION = 6;
    public static final int WRITE_EXTERNAL_STORAGE = 7;

    // 好友群聊添加来源类型
    public static final int FIND_TYPE_SEARCH = 1;
    public static final int FIND_TYPE_QR_CODE = 2;
    public static final int FIND_TYPE_SHARE = 3;
    // 好友特有的来源类型
    public static final int FIND_TYPE_GROUP = 4;
    public static final int FIND_TYPE_DEFAULT = 5;
    // 通过邀请入群
    public static final int FIND_TYPE_INVITE = 4;

    public static final int MAX_UPLOAD_FILE_SIZE = 100 * 1024 * 1024;

    private static final String[] VIDEO = new String[]{"mp4", "avi", "rmvb", "flv", "f4v", "mpg", "mkv", "mov"};
    public static final List<String> VIDEO_TYPE = Arrays.asList(VIDEO);

    private static final String[] IMAGE = new String[]{"bmp", "png", "jpeg", "gif", "jpg"};
    public static final List<String> IMAGE_TYPE = Arrays.asList(IMAGE);

    /* 群成员权限等级 */

    // 普通成员
    public static final int LEVEL_USER = 1;
    // 群管理员
    public static final int LEVEL_ADMIN = 2;
    // 群主
    public static final int LEVEL_OWNER = 3;


    /* WebSocket消息eventType */

    // 普通消息
    public static final int MSG_NORMAL_MESSAGE = 0;
    // 在其他终端登录
    public static final int MSG_OTHER_LOGIN = 9;
    // 帐号被封禁
    public static final int MSG_BANNED_USER = 10;
    // 群组被封禁
    public static final int MSG_BANNED_GROUP = 11;
    // 入群通知
    public static final int MSG_ENTER_GROUP = 20;
    // 退群通知
    public static final int MSG_EXIT_GROUP = 21;
    // 解散群通知
    public static final int MSG_DISMISS_GROUP = 22;
    // 入群请求和回复通知
    public static final int MSG_GROUP_REQUEST = 23;
    // 群中被禁言、解禁通知
    public static final int MSG_GROUP_MUTE = 25;
    // 同步会话密钥
    public static final int MSG_SYNC_GROUP_KEY = 28;
    // 会话密钥同步完成
    public static final int MSG_SYNC_GROUP_KEY_END =29;
    // 添加好友消息通知
    public static final int MSG_ADD_FRIEND = 31;
    // 删除好友消息通知
    public static final int MSG_DELETE_FRIEND = 32;
    // 好友公钥更新
    public static final int MSG_UPDATE_FRIEND_KEY = 34;
    // 换设备或卸载重装，批量接收最近消息
    public static final int MSG_NEW_DEVICE_PUSH = 40;
    // 离线未读消息批量推送
    public static final int MSG_OFFLINE_PUSH = 41;
    // 同步成功消息
    public static final int MSG_NORMAL_PUSH_END = 43;
    // 逐条转发批量推送
    public static final int MSG_FORWARD_PUSH = 44;
    // 消息确认，发现丢失后推送
    public static final int MSG_ACK_PUSH = 46;
    // 消息确认完成，取消息确认的end作为下一次确认的开始
    public static final int MSG_ACK_PUSH_END = 47;

    /* 通知类型 */

    // 撤回消息
    public static final int REVOKE_MSG = 1;
    // 创建群聊
    public static final int CREATE_GROUP = 2;
    // 退出群聊
    public static final int EXIT_GROUP = 3;
    // 踢出群聊
    public static final int KICK_OUT = 4;
    // 加入群聊
    public static final int JOIN_GROUP = 5;
    // 解散群聊
    public static final int DISMISS_GROUP = 6;
    // 群内添加好友
    public static final int FRIEND_IN_GROUP = 7;
    // 删除好友
    public static final int DELETE_FRIEND = 8;
    // 群主变更
    public static final int CHANGE_GROUP_OWNER = 9;
    // 管理员变更
    public static final int CHANGE_GROUP_ADMIN = 10;
    // 更改群名
    public static final int CHANGE_GROUP_NAME = 11;
    // 领取红包
    public static final int RECEIVE_RED_PACKET = 12;
    // 添加好友（非群内）
    public static final int BECOME_FRIEND = 13;
    // 群内禁言
    public static final int MUTE_IN_GROUP = 14;
    // 消息焚毁
    public static final int SNAP_DESTROY = 15;
    // 对方截屏
    public static final int SCREEN_SHOT = 16;
    // 邀请入群
    public static final int INVITE_GROUP = 17;
    // 收款成功
    public static final int RECEIPT_SUCCESS = 18;
    // 更新群秘钥
    public static final int UPDATE_GROUP_KEY = 19;
    // 被邀请者拒绝入群
    public static final int GROUP_REJECT_MSG = 20;
    // 消息被好友拒收
    public static final int FRIEND_REJECT_MSG = 21;
    // 赞赏消息通知
    public static final int PRAISE_MESSAGE = 22;

}
