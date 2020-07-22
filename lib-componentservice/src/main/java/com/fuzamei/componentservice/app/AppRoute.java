package com.fuzamei.componentservice.app;

/**
 * @author Mark
 * @since 2018/9/26
 * Description:各个组件间跳转的path
 */
public interface AppRoute {

    // App壳
    String SPLASH = "/main/splash";

    // module-chat模块
    String MAIN = "/app/MainActivity";
    String CHAT = "/app/chatActivity";
    String USER_DETAIL = "/app/userDetail";
    String GROUP_INFO = "/app/groupInfo";
    String WEB_BROWSER = "/app/webBrowser";
    String QR_CODE = "/app/QRCode";
    String QR_SCAN = "/app/QRScanner";
    String JOIN_ROOM = "/app/joinRoom";
    String CONTACT_SELECT = "/app/contactSelect";
    String FRIEND_VERIFY = "/app/addVerify";
    String ADMIN_SET = "/app/adminSet";
    String CHAT_FILE = "/app/chatFileList";
    String CREATE_GROUP = "/app/createGroup";
    String EDIT_AVATAR = "/app/editAvatar";
    String EDIT_GROUP_INFO = "/app/editGroupInfo";
    String EDIT_NAME = "/app/editInfo";
    String EDIT_USER_INFO = "/app/editUserRemark";
    String FILE_DETAIL = "/app/fileDetail";
    String FORWARD_MESSAGE = "/app/forwardList";
    String GROUP_MEMBER = "/app/groupMember";
    String SELECT_GROUP_MEMBER = "/app/selectMember";
    String GROUP_NOTICE = "/app/groupNotice";
    String LARGE_PHOTO = "/app/largePhoto";
    String NEW_FRIENDS = "/app/newFriends";
    String PAY_PASSWORD = "/app/payPassword";
    String PROMOTE_DETAIL = "/app/promoteDetail";
    String PUSH_CHECK = "/app/pushCheck";
    String RECOMMEND_GROUPS = "/app/recommendedGroups";
    String SEARCH_CHAT_FILE = "/app/searchChatFile";
    String SEARCH_ONLINE = "/app/searchOnline";
    String SEARCH_LOCAL = "/app/searchLocal";
    String SEARCH_LOCAL_SCOPE = "/app/searchLocalLogs";
    String SECURITY_SETTING = "/app/securitySetting";
    String SERVER_TIPS = "/app/serverTips";
    String SETTING = "/app/setting";
    String CAMERA_SHOOT = "/app/shootActivity";
    String BIG_IMAGE = "/app/showBigImage";
    String CHAT_MEDIA = "/app/showChatMedia";
    String SYSTEM_SHARE = "/app/systemShare";
    String ENCRYPT_PWD = "/app/encryptPassword";
    String GROUP_USER_FILE = "/app/userFile";
    String VERIFY_QUESTION = "/app/verifyQuestion";
    String VIDEO_PLAYER = "/app/videoPlayer";
    String AIT_SELECT = "/app/aitSelector";
    String BLACK_LIST = "/app/blackList";
    String REWARD_PACKET = "/app/rewardPacket";
    String IMPORT_MNEMONIC_WORD = "/app/importMnemonicWord";
    String PRAISE_RANK = "/app/praiseRank";
    String PRAISE_RANK_HISTORY = "/app/praiseRankHistory";

    // module-login模块
    String LOGIN_INJECTOR = "/login/injector";
    String LOGIN = "/login/LoginActivity";

    // module-pwallet模块
    String APPLICATION_PWALLET = "/wallet/pwalletApplication";
    String CHOOSE_VERIFY = "/wallet/chooseVerifyType";
    String DEPOSIT_IN = "/wallet/depositIn";
    String DEPOSIT_OUT = "/wallet/depositOut";
    String DEPOSIT_TX_DETAIL = "/wallet/depositTxDetail";
    String DEPOSIT_RECEIPT = "/wallet/depositReceipt";
    String DEPOSIT_HOME = "/wallet/DepositHomeFragment";

    // module-attendance模块
    String WORK_INJECTOR = "/attendance/injector";
    String ATTENDANCE = "/attendance/checkWork";

    // lib-picverify模块
    String PIC_VERIFY = "/verify/VerifyPopup";

    String CHAT_PRAISE = "/app/chatPraise";
    String MESSAGE_PRAISE = "/app/MessagePraise";
    String RED_PACKET_RECORDS = "/app/redPacketRecords";
}
