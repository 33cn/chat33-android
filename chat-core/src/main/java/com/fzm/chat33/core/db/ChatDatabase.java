package com.fzm.chat33.core.db;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.converter.ChatLogToStringConverter;
import com.fzm.chat33.core.converter.ExtRemarkToStringConverter;
import com.fzm.chat33.core.converter.ListToStringConverter;
import com.fzm.chat33.core.converter.RewardToStringConverter;
import com.fzm.chat33.core.db.bean.FriendView;
import com.fzm.chat33.core.db.bean.InfoCacheBean;
import com.fzm.chat33.core.db.bean.RoomInfoBean;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.db.bean.RecentMessage;
import com.fzm.chat33.core.db.bean.RoomKey;
import com.fzm.chat33.core.db.bean.RoomListBean;
import com.fzm.chat33.core.db.bean.RoomUserBean;
import com.fzm.chat33.core.db.bean.RoomView;
import com.fzm.chat33.core.db.bean.SearchHistory;
import com.fzm.chat33.core.db.dao.ChatMessageDao;
import com.fzm.chat33.core.db.dao.FriendsDao;
import com.fzm.chat33.core.db.dao.FtsSearchDao;
import com.fzm.chat33.core.db.dao.InfoCacheDao;
import com.fzm.chat33.core.db.dao.RecentMessageDao;
import com.fzm.chat33.core.db.dao.RoomInfoDao;
import com.fzm.chat33.core.db.dao.RoomKeyDao;
import com.fzm.chat33.core.db.dao.RoomUserDao;
import com.fzm.chat33.core.db.dao.RoomsDao;
import com.fzm.chat33.core.db.dao.SearchHistoryDao;
import com.fzm.chat33.core.db.dao.UserInfoDao;
import com.fzm.chat33.core.db.fts.ChatMessageFts;
import com.fzm.chat33.core.global.UserInfo;
import com.tencent.wcdb.room.db.WCDBOpenHelperFactory;

/**
 * @author zhengjy
 * @since 2018/10/27
 * Description:
 */
@Database(
        entities = {
                ChatMessage.class,
                InfoCacheBean.class,
                RecentMessage.class,
                FriendBean.class,
                RoomListBean.class,
                RoomInfoBean.class,
                RoomUserBean.class,
                RoomKey.class,
                UserInfo.class,
                SearchHistory.class,
                // FTS表
                ChatMessageFts.class
        },
        views = {
                FriendView.class,
                RoomView.class
        },
        version = 21
)
@TypeConverters({
        ChatLogToStringConverter.class,
        ExtRemarkToStringConverter.class,
        ListToStringConverter.class,
        RewardToStringConverter.class
})
public abstract class ChatDatabase extends RoomDatabase {

    private static volatile ChatDatabase INSTANCE;

    public abstract ChatMessageDao chatMessageDao();

    public abstract InfoCacheDao infoCacheDao();

    public abstract RecentMessageDao recentMessageDao();

    public abstract FriendsDao friendsDao();

    public abstract RoomsDao roomsDao();

    public abstract RoomInfoDao roomInfoDao();

    public abstract RoomUserDao roomUserDao();

    public abstract RoomKeyDao roomKeyDao();

    public abstract UserInfoDao userInfoDao();

    public abstract SearchHistoryDao searchHistoryDao();

    public abstract FtsSearchDao ftsSearchDao();

    private static final Object sLock = new Object();

    private static volatile String dbId = null;

    public static ChatDatabase getInstance() {
        if (INSTANCE == null || TextUtils.isEmpty(dbId)) {
            synchronized (sLock) {
                if (INSTANCE == null || TextUtils.isEmpty(dbId)) {
                    INSTANCE = buildDatabase(Chat33.getContext(), UserInfo.getInstance().id);
                }
            }
        }
        return INSTANCE;
    }

    public static ChatDatabase getInstance(String uid) {
        if (INSTANCE == null || TextUtils.isEmpty(dbId)) {
            synchronized (sLock) {
                if (INSTANCE == null) {
                    INSTANCE = buildDatabase(Chat33.getContext(), uid);
                }
            }
        }
        return INSTANCE;
    }

    private static ChatDatabase buildDatabase(Context context, String id) {
        WCDBOpenHelperFactory factory = new WCDBOpenHelperFactory()
                .writeAheadLoggingEnabled(true)       // 打开WAL以及读写并发，可以省略让Room决定是否要打开
                .asyncCheckpointEnabled(true);        // 打开异步Checkpoint优化，不需要可以省略
        dbId = id;
        return Room.databaseBuilder(context, ChatDatabase.class, "chat" + id + ".db")
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                        MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
                        MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18,
                        MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21)
                // 只允许在主线程查询RoomKey
                .allowMainThreadQueries()
                .openHelperFactory(factory)
                .addCallback(DATABASE_CALL_BACK)
                .build();
    }

    public static void reset() {
        synchronized (sLock) {
            INSTANCE = null;
        }
    }

    // 数据库版本迁移
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 修改主键logId的类型
            database.execSQL("CREATE TABLE IF NOT EXISTS chat_message_new (`logId` TEXT NOT NULL, `msgId` TEXT, " +
                    "`channelType` INTEGER NOT NULL, `senderId` TEXT, `fromGId` TEXT, `receiveId` TEXT, " +
                    "`messageState` INTEGER NOT NULL, `msgType` INTEGER NOT NULL, `sendTime` INTEGER NOT NULL, " +
                    "`imageUrl` TEXT, `mediaUrl` TEXT, `isRead` INTEGER, `name` TEXT, `duration` REAL, " +
                    "`height` INTEGER, `width` INTEGER, `type` INTEGER, `delete_logId` TEXT, `content` TEXT," +
                    " `coin` INTEGER, `packetId` TEXT, `packetUrl` TEXT, `packetType` INTEGER, `redBagRemark` TEXT," +
                    " `isOpened` INTEGER, `redPacketStatus` INTEGER, `avatar` TEXT, `nickname` TEXT, `remark` TEXT," +
                    " `uid` TEXT, `userLevel` INTEGER, PRIMARY KEY(`logId`, `channelType`))");
            database.execSQL("INSERT INTO chat_message_new SELECT * FROM chat_message");
            database.execSQL("DROP TABLE chat_message");
            database.execSQL("ALTER TABLE chat_message_new RENAME TO chat_message");
            // 新建room_info表
            database.execSQL("CREATE TABLE IF NOT EXISTS room_info (`id` TEXT NOT NULL, `markId` TEXT, " +
                    "`name` TEXT, `avatar` TEXT, `isMember` INTEGER NOT NULL, `onlineNumber` INTEGER NOT NULL, " +
                    "`memberNumber` INTEGER NOT NULL, `noDisturbing` INTEGER NOT NULL, `onTop` INTEGER NOT NULL, " +
                    "`memberLevel` INTEGER NOT NULL, `canAddFriend` INTEGER NOT NULL, `joinPermission` INTEGER NOT NULL, " +
                    "`roomNickname` TEXT, `managerNumber` INTEGER NOT NULL, `mutedNumber` INTEGER NOT NULL," +
                    " `roomMutedType` INTEGER NOT NULL, `mutedType` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, PRIMARY KEY(`id`))");
            // 新建room_user表
            database.execSQL("CREATE TABLE IF NOT EXISTS room_user (`roomId` TEXT NOT NULL, `id` TEXT NOT NULL, " +
                    "`nickname` TEXT, `roomNickname` TEXT, `avatar` TEXT, `memberLevel` INTEGER NOT NULL, " +
                    "`roomMutedType` INTEGER NOT NULL, `mutedType` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`roomId`, `id`))");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN isSnap INTEGER DEFAULT 2 NOT NULL");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN destroyTime INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN snapVisible INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN snapCounting INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN isSnap INTEGER DEFAULT 2 NOT NULL");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN sourceChannel INTEGER");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN forwardUserName TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN sourceName TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN sourceLog TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN sourceChannel INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN forwardUserName TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN sourceName TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN sourceLog TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN needConfirm INTEGER DEFAULT 1 NOT NULL");
            database.execSQL("ALTER TABLE friends ADD COLUMN needAnswer INTEGER DEFAULT 2 NOT NULL");
            database.execSQL("ALTER TABLE friends ADD COLUMN question TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN source TEXT");
            database.execSQL("ALTER TABLE room_info ADD COLUMN recordPermission INTEGER DEFAULT 1 NOT NULL");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN forwardType INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN forwardType INTEGER");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN ignoreInHistory INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN fileUrl TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN fileName TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN md5 TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN fileSize INTEGER");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN localPath TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN fileUrl TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN fileName TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN md5 TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN fileSize INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN localPath TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN extRemark TEXT");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN coinName TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN coinName TEXT");
        }
    };

    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE room_info ADD COLUMN disableDeadline INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE room_list ADD COLUMN disableDeadline INTEGER DEFAULT 0 NOT NULL");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN disableDeadline INTEGER DEFAULT 0 NOT NULL");
        }
    };

    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN roomName TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN operator TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN target TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN owner TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN recordId TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN amount TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN roomName TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN operator TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN target TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN owner TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN recordId TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN amount TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN depositAddress TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN depositAddress TEXT");
        }
    };

    public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE room_user ADD COLUMN publicKey TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN publicKey TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN encrypt INTEGER NOT NULL DEFAULT 2");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN encrypt INTEGER NOT NULL DEFAULT 2");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN kid TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN encryptedMsg TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN fromKey TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN toKey TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN encrypted INTEGER NOT NULL DEFAULT 2");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN kid TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN encryptedMsg TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN fromKey TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN toKey TEXT");
            database.execSQL("ALTER TABLE room_info ADD COLUMN encrypt INTEGER NOT NULL DEFAULT 2");
            database.execSQL("ALTER TABLE room_list ADD COLUMN encrypt INTEGER NOT NULL DEFAULT 2");
            database.execSQL("CREATE TABLE IF NOT EXISTS `room_key` (`roomId` TEXT NOT NULL, " +
                    "`kid` TEXT NOT NULL, `key` TEXT, `originKey` TEXT, `fromKey` TEXT, PRIMARY KEY(`roomId`, `kid`))");
        }
    };

    public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE info_cache ADD COLUMN identification INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE info_cache ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE friends ADD COLUMN identification INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE friends ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE room_list ADD COLUMN identification INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE room_list ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE room_info ADD COLUMN identification INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE room_info ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE room_user ADD COLUMN identification INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE room_user ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN roomId TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN markId TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN inviterId TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN roomAvatar TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN identificationInfo TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN roomId TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN markId TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN inviterId TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN roomAvatar TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN identificationInfo TEXT");
        }
    };

    public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `user_info` (`account` TEXT, `avatar` TEXT, " +
                    "`id` TEXT NOT NULL, `uid` TEXT, `phone` TEXT, `isSetPayPwd` INTEGER NOT NULL, `user_level` " +
                    "INTEGER NOT NULL, `username` TEXT, `verified` INTEGER NOT NULL, `token` TEXT, " +
                    "`position` TEXT, `firstLogin` INTEGER NOT NULL, `depositAddress` TEXT, " +
                    "`publicKey` TEXT, `identification` INTEGER NOT NULL, `identificationInfo` TEXT, " +
                    "`code` TEXT, PRIMARY KEY(`id`))");
        }
    };

    public static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN aitList TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN names TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN mutedType INTEGER");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN opt INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN aitList TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN names TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN mutedType INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN opt INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN beAit INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN matchOffsets TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN matchOffsets TEXT");
            database.execSQL("DROP TABLE room_user");
            database.execSQL("CREATE TABLE IF NOT EXISTS `room_user` (`roomId` TEXT NOT NULL, `id` TEXT NOT NULL, `nickname` TEXT, `roomNickname` TEXT, `avatar` TEXT, `memberLevel` INTEGER NOT NULL, `roomMutedType` INTEGER NOT NULL, `mutedType` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, `identification` INTEGER NOT NULL, `identificationInfo` TEXT, `publicKey` TEXT, PRIMARY KEY(`roomId`, `id`), FOREIGN KEY(`roomId`) REFERENCES `room_list`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`keywords` TEXT NOT NULL, " +
                    "`searchTime` INTEGER NOT NULL, PRIMARY KEY(`keywords`))");
            database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `message_fts` USING FTS4(`logId` TEXT NOT NULL, `msgId` TEXT, `encrypted` INTEGER NOT NULL, `isSnap` INTEGER NOT NULL, `snapVisible` INTEGER NOT NULL, `snapCounting` INTEGER NOT NULL, `destroyTime` INTEGER NOT NULL, `ignoreInHistory` INTEGER NOT NULL, `channelType` INTEGER NOT NULL, `senderId` TEXT, `fromGId` TEXT, `receiveId` TEXT, `messageState` INTEGER NOT NULL, `msgType` INTEGER NOT NULL, `sendTime` INTEGER NOT NULL, `imageUrl` TEXT, `mediaUrl` TEXT, `isRead` INTEGER, `name` TEXT, `localPath` TEXT, `duration` REAL, `height` INTEGER, `width` INTEGER, `type` INTEGER, `roomName` TEXT, `operator` TEXT, `target` TEXT, `owner` TEXT, `names` TEXT, `mutedType` INTEGER, `opt` INTEGER, `delete_logId` TEXT, `roomId` TEXT, `markId` TEXT, `inviterId` TEXT, `roomAvatar` TEXT, `identificationInfo` TEXT, `matchOffsets` TEXT, `content` TEXT, `kid` TEXT, `encryptedMsg` TEXT, `fromKey` TEXT, `toKey` TEXT, `recordId` TEXT, `amount` TEXT, `coin` INTEGER, `coinName` TEXT, `packetId` TEXT, `packetUrl` TEXT, `packetType` INTEGER, `redBagRemark` TEXT, `isOpened` INTEGER, `redPacketStatus` INTEGER, `aitList` TEXT, `fileUrl` TEXT, `fileName` TEXT, `fileSize` INTEGER, `md5` TEXT, `sourceChannel` INTEGER, `forwardType` INTEGER, `sourceName` TEXT, `forwardUserName` TEXT, `sourceLog` TEXT, `avatar` TEXT, `nickname` TEXT, `remark` TEXT, `uid` TEXT, `userLevel` INTEGER, tokenize=mmicu, content=`chat_message`, notindexed=`snapVisible`, notindexed=`snapCounting`, notindexed=`destroyTime`, notindexed=`imageUrl`, notindexed=`mediaUrl`, notindexed=`localPath`, notindexed=`height`, notindexed=`width`, notindexed=`roomAvatar`, notindexed=`encryptedMsg`, notindexed=`recordId`, notindexed=`coinName`, notindexed=`packetId`, notindexed=`packetUrl`, notindexed=`fileUrl`, notindexed=`fileSize`, notindexed=`md5`, order=DESC)");
            database.execSQL("INSERT INTO `message_fts` (`docid`, `logId`, `msgId`, `encrypted`, `isSnap`, `snapVisible`, `snapCounting`, `destroyTime`, `ignoreInHistory`, `channelType`, `senderId`, `fromGId`, `receiveId`, `messageState`, `msgType`, `sendTime`, `imageUrl`, `mediaUrl`, `isRead`, `name`, `localPath`, `duration`, `height`, `width`, `type`, `roomName`, `operator`, `target`, `owner`, `names`, `mutedType`, `opt`, `delete_logId`, `roomId`, `markId`, `inviterId`, `roomAvatar`, `identificationInfo`, `matchOffsets`, `content`, `kid`, `encryptedMsg`, `fromKey`, `toKey`, `recordId`, `amount`, `coin`, `coinName`, `packetId`, `packetUrl`, `packetType`, `redBagRemark`, `isOpened`, `redPacketStatus`, `aitList`, `fileUrl`, `fileName`, `fileSize`, `md5`, `sourceChannel`, `forwardType`, `sourceName`, `forwardUserName`, `sourceLog`, `avatar`, `nickname`, `remark`, `uid`, `userLevel`) SELECT chat_message.`rowid` AS docid, chat_message.`logId`, chat_message.`msgId`, chat_message.`encrypted`, chat_message.`isSnap`, chat_message.`snapVisible`, chat_message.`snapCounting`, chat_message.`destroyTime`, chat_message.`ignoreInHistory`, chat_message.`channelType`, chat_message.`senderId`, chat_message.`fromGId`, chat_message.`receiveId`, chat_message.`messageState`, chat_message.`msgType`, chat_message.`sendTime`, chat_message.`imageUrl`, chat_message.`mediaUrl`, chat_message.`isRead`, chat_message.`name`, chat_message.`localPath`, chat_message.`duration`, chat_message.`height`, chat_message.`width`, chat_message.`type`, chat_message.`roomName`, chat_message.`operator`, chat_message.`target`, chat_message.`owner`, chat_message.`names`, chat_message.`mutedType`, chat_message.`opt`, chat_message.`delete_logId`, chat_message.`roomId`, chat_message.`markId`, chat_message.`inviterId`, chat_message.`roomAvatar`, chat_message.`identificationInfo`, chat_message.`matchOffsets`, chat_message.`content`, chat_message.`kid`, chat_message.`encryptedMsg`, chat_message.`fromKey`, chat_message.`toKey`, chat_message.`recordId`, chat_message.`amount`, chat_message.`coin`, chat_message.`coinName`, chat_message.`packetId`, chat_message.`packetUrl`, chat_message.`packetType`, chat_message.`redBagRemark`, chat_message.`isOpened`, chat_message.`redPacketStatus`, chat_message.`aitList`, chat_message.`fileUrl`, chat_message.`fileName`, chat_message.`fileSize`, chat_message.`md5`, chat_message.`sourceChannel`, chat_message.`forwardType`, chat_message.`sourceName`, chat_message.`forwardUserName`, chat_message.`sourceLog`, chat_message.`avatar`, chat_message.`nickname`, chat_message.`remark`, chat_message.`uid`, chat_message.`userLevel` FROM chat_message");
            database.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_message_fts_BEFORE_UPDATE " +
                    "BEFORE UPDATE ON `chat_message` BEGIN DELETE FROM `message_fts` " +
                    "WHERE `docid`=OLD.`rowid`; END");
            database.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_message_fts_BEFORE_DELETE " +
                    "BEFORE DELETE ON `chat_message` BEGIN DELETE FROM `message_fts` " +
                    "WHERE `docid`=OLD.`rowid`; END");
            database.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_message_fts_AFTER_UPDATE " +
                    "AFTER UPDATE ON `chat_message` BEGIN INSERT INTO `message_fts`(`docid`, `logId`, `msgId`, `encrypted`, `isSnap`, `snapVisible`, `snapCounting`, `destroyTime`, `ignoreInHistory`, `channelType`, `senderId`, `fromGId`, `receiveId`, `messageState`, `msgType`, `sendTime`, `imageUrl`, `mediaUrl`, `isRead`, `name`, `localPath`, `duration`, `height`, `width`, `type`, `roomName`, `operator`, `target`, `owner`, `names`, `mutedType`, `opt`, `delete_logId`, `roomId`, `markId`, `inviterId`, `roomAvatar`, `identificationInfo`, `matchOffsets`, `content`, `kid`, `encryptedMsg`, `fromKey`, `toKey`, `recordId`, `amount`, `coin`, `coinName`, `packetId`, `packetUrl`, `packetType`, `redBagRemark`, `isOpened`, `redPacketStatus`, `aitList`, `fileUrl`, `fileName`, `fileSize`, `md5`, `sourceChannel`, `forwardType`, `sourceName`, `forwardUserName`, `sourceLog`, `avatar`, `nickname`, `remark`, `uid`, `userLevel`) VALUES (NEW.`rowid`, NEW.`logId`, NEW.`msgId`, NEW.`encrypted`, NEW.`isSnap`, NEW.`snapVisible`, NEW.`snapCounting`, NEW.`destroyTime`, NEW.`ignoreInHistory`, NEW.`channelType`, NEW.`senderId`, NEW.`fromGId`, NEW.`receiveId`, NEW.`messageState`, NEW.`msgType`, NEW.`sendTime`, NEW.`imageUrl`, NEW.`mediaUrl`, NEW.`isRead`, NEW.`name`, NEW.`localPath`, NEW.`duration`, NEW.`height`, NEW.`width`, NEW.`type`, NEW.`roomName`, NEW.`operator`, NEW.`target`, NEW.`owner`, NEW.`names`, NEW.`mutedType`, NEW.`opt`, NEW.`delete_logId`, NEW.`roomId`, NEW.`markId`, NEW.`inviterId`, NEW.`roomAvatar`, NEW.`identificationInfo`, NEW.`matchOffsets`, NEW.`content`, NEW.`kid`, NEW.`encryptedMsg`, NEW.`fromKey`, NEW.`toKey`, NEW.`recordId`, NEW.`amount`, NEW.`coin`, NEW.`coinName`, NEW.`packetId`, NEW.`packetUrl`, NEW.`packetType`, NEW.`redBagRemark`, NEW.`isOpened`, NEW.`redPacketStatus`, NEW.`aitList`, NEW.`fileUrl`, NEW.`fileName`, NEW.`fileSize`, NEW.`md5`, NEW.`sourceChannel`, NEW.`forwardType`, NEW.`sourceName`, NEW.`forwardUserName`, NEW.`sourceLog`, NEW.`avatar`, NEW.`nickname`, NEW.`remark`, NEW.`uid`, NEW.`userLevel`); END");
            database.execSQL("CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_message_fts_AFTER_INSERT " +
                    "AFTER INSERT ON `chat_message` BEGIN INSERT INTO `message_fts`(`docid`, `logId`, `msgId`, `encrypted`, `isSnap`, `snapVisible`, `snapCounting`, `destroyTime`, `ignoreInHistory`, `channelType`, `senderId`, `fromGId`, `receiveId`, `messageState`, `msgType`, `sendTime`, `imageUrl`, `mediaUrl`, `isRead`, `name`, `localPath`, `duration`, `height`, `width`, `type`, `roomName`, `operator`, `target`, `owner`, `names`, `mutedType`, `opt`, `delete_logId`, `roomId`, `markId`, `inviterId`, `roomAvatar`, `identificationInfo`, `matchOffsets`, `content`, `kid`, `encryptedMsg`, `fromKey`, `toKey`, `recordId`, `amount`, `coin`, `coinName`, `packetId`, `packetUrl`, `packetType`, `redBagRemark`, `isOpened`, `redPacketStatus`, `aitList`, `fileUrl`, `fileName`, `fileSize`, `md5`, `sourceChannel`, `forwardType`, `sourceName`, `forwardUserName`, `sourceLog`, `avatar`, `nickname`, `remark`, `uid`, `userLevel`) VALUES (NEW.`rowid`, NEW.`logId`, NEW.`msgId`, NEW.`encrypted`, NEW.`isSnap`, NEW.`snapVisible`, NEW.`snapCounting`, NEW.`destroyTime`, NEW.`ignoreInHistory`, NEW.`channelType`, NEW.`senderId`, NEW.`fromGId`, NEW.`receiveId`, NEW.`messageState`, NEW.`msgType`, NEW.`sendTime`, NEW.`imageUrl`, NEW.`mediaUrl`, NEW.`isRead`, NEW.`name`, NEW.`localPath`, NEW.`duration`, NEW.`height`, NEW.`width`, NEW.`type`, NEW.`roomName`, NEW.`operator`, NEW.`target`, NEW.`owner`, NEW.`names`, NEW.`mutedType`, NEW.`opt`, NEW.`delete_logId`, NEW.`roomId`, NEW.`markId`, NEW.`inviterId`, NEW.`roomAvatar`, NEW.`identificationInfo`, NEW.`matchOffsets`, NEW.`content`, NEW.`kid`, NEW.`encryptedMsg`, NEW.`fromKey`, NEW.`toKey`, NEW.`recordId`, NEW.`amount`, NEW.`coin`, NEW.`coinName`, NEW.`packetId`, NEW.`packetUrl`, NEW.`packetType`, NEW.`redBagRemark`, NEW.`isOpened`, NEW.`redPacketStatus`, NEW.`aitList`, NEW.`fileUrl`, NEW.`fileName`, NEW.`fileSize`, NEW.`md5`, NEW.`sourceChannel`, NEW.`forwardType`, NEW.`sourceName`, NEW.`forwardUserName`, NEW.`sourceLog`, NEW.`avatar`, NEW.`nickname`, NEW.`remark`, NEW.`uid`, NEW.`userLevel`); END");
            database.execSQL("CREATE VIEW `FriendView` AS SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 3 UNION SELECT friends.id, friends.name, friends.remark, friends.avatar, friends.identification, friends.onTop, friends.noDisturbing FROM friends");
            database.execSQL("CREATE VIEW `RoomView` AS SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 2 UNION SELECT room_list.id, room_list.name, room_list.name AS remark, room_list.avatar, room_list.identification, room_list.onTop, room_list.noDisturbing  FROM room_list");
        }
    };

    public static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE friends ADD COLUMN searchKey TEXT");
            database.execSQL("ALTER TABLE room_list ADD COLUMN searchKey TEXT");
            database.execSQL("ALTER TABLE room_user ADD COLUMN searchKey TEXT");
        }
    };

    public static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE friends ADD COLUMN isBlocked INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE TRIGGER trigger_delete_friends_AFTER_DELETE AFTER DELETE ON friends BEGIN INSERT INTO info_cache (channelType, id, nickname, avatar, remark, identification, identificationInfo) VALUES (3, old.id, old.name, old.avatar, old.remark, old.identification, old.identificationInfo) ;END");
            database.execSQL("CREATE TRIGGER trigger_insert_friends_AFTER_INSERT AFTER INSERT ON friends BEGIN DELETE FROM info_cache WHERE channelType = 3 AND id = new.id;END");
        }
    };

    public static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN praise TEXT");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN `like` INTEGER");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN reward INTEGER");
            database.execSQL("ALTER TABLE chat_message ADD COLUMN `action` TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN `like` INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN reward INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN `action` TEXT");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN recent_like INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN recent_reward INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `message_fts_new` USING FTS4(`logId` TEXT NOT NULL, `msgId` TEXT, `encrypted` INTEGER NOT NULL, `isSnap` INTEGER NOT NULL, `snapVisible` INTEGER NOT NULL, `snapCounting` INTEGER NOT NULL, `destroyTime` INTEGER NOT NULL, `ignoreInHistory` INTEGER NOT NULL, `channelType` INTEGER NOT NULL, `senderId` TEXT, `fromGId` TEXT, `receiveId` TEXT, `messageState` INTEGER NOT NULL, `msgType` INTEGER NOT NULL, `sendTime` INTEGER NOT NULL, `imageUrl` TEXT, `mediaUrl` TEXT, `isRead` INTEGER, `name` TEXT, `localPath` TEXT, `duration` REAL, `height` INTEGER, `width` INTEGER, `type` INTEGER, `roomName` TEXT, `operator` TEXT, `target` TEXT, `owner` TEXT, `names` TEXT, `mutedType` INTEGER, `opt` INTEGER, `delete_logId` TEXT, `roomId` TEXT, `markId` TEXT, `inviterId` TEXT, `roomAvatar` TEXT, `identificationInfo` TEXT, `matchOffsets` TEXT, `content` TEXT, `kid` TEXT, `encryptedMsg` TEXT, `fromKey` TEXT, `toKey` TEXT, `recordId` TEXT, `amount` TEXT, `coin` INTEGER, `coinName` TEXT, `packetId` TEXT, `packetUrl` TEXT, `packetType` INTEGER, `redBagRemark` TEXT, `isOpened` INTEGER, `redPacketStatus` INTEGER, `aitList` TEXT, `fileUrl` TEXT, `fileName` TEXT, `fileSize` INTEGER, `md5` TEXT, `sourceChannel` INTEGER, `forwardType` INTEGER, `sourceName` TEXT, `forwardUserName` TEXT, `sourceLog` TEXT, `avatar` TEXT, `nickname` TEXT, `remark` TEXT, `uid` TEXT, `userLevel` INTEGER, `like` INTEGER, `reward` INTEGER, `action` TEXT, tokenize=mmicu, content=`chat_message`, notindexed=`snapVisible`, notindexed=`snapCounting`, notindexed=`destroyTime`, notindexed=`imageUrl`, notindexed=`mediaUrl`, notindexed=`localPath`, notindexed=`height`, notindexed=`width`, notindexed=`roomAvatar`, notindexed=`encryptedMsg`, notindexed=`recordId`, notindexed=`coinName`, notindexed=`packetId`, notindexed=`packetUrl`, notindexed=`fileUrl`, notindexed=`fileSize`, notindexed=`md5`, notindexed=`like`, notindexed=`reward`, notindexed=`action`, order=DESC)");
            database.execSQL("INSERT INTO `message_fts_new` (`docid`, `logId`, `msgId`, `encrypted`, `isSnap`, `snapVisible`, `snapCounting`, `destroyTime`, `ignoreInHistory`, `channelType`, `senderId`, `fromGId`, `receiveId`, `messageState`, `msgType`, `sendTime`, `imageUrl`, `mediaUrl`, `isRead`, `name`, `localPath`, `duration`, `height`, `width`, `type`, `roomName`, `operator`, `target`, `owner`, `names`, `mutedType`, `opt`, `delete_logId`, `roomId`, `markId`, `inviterId`, `roomAvatar`, `identificationInfo`, `matchOffsets`, `content`, `kid`, `encryptedMsg`, `fromKey`, `toKey`, `recordId`, `amount`, `coin`, `coinName`, `packetId`, `packetUrl`, `packetType`, `redBagRemark`, `isOpened`, `redPacketStatus`, `aitList`, `fileUrl`, `fileName`, `fileSize`, `md5`, `sourceChannel`, `forwardType`, `sourceName`, `forwardUserName`, `sourceLog`, `avatar`, `nickname`, `remark`, `uid`, `userLevel`) SELECT message_fts.`rowid` AS docid, message_fts.`logId`, message_fts.`msgId`, message_fts.`encrypted`, message_fts.`isSnap`, message_fts.`snapVisible`, message_fts.`snapCounting`, message_fts.`destroyTime`, message_fts.`ignoreInHistory`, message_fts.`channelType`, message_fts.`senderId`, message_fts.`fromGId`, message_fts.`receiveId`, message_fts.`messageState`, message_fts.`msgType`, message_fts.`sendTime`, message_fts.`imageUrl`, message_fts.`mediaUrl`, message_fts.`isRead`, message_fts.`name`, message_fts.`localPath`, message_fts.`duration`, message_fts.`height`, message_fts.`width`, message_fts.`type`, message_fts.`roomName`, message_fts.`operator`, message_fts.`target`, message_fts.`owner`, message_fts.`names`, message_fts.`mutedType`, message_fts.`opt`, message_fts.`delete_logId`, message_fts.`roomId`, message_fts.`markId`, message_fts.`inviterId`, message_fts.`roomAvatar`, message_fts.`identificationInfo`, message_fts.`matchOffsets`, message_fts.`content`, message_fts.`kid`, message_fts.`encryptedMsg`, message_fts.`fromKey`, message_fts.`toKey`, message_fts.`recordId`, message_fts.`amount`, message_fts.`coin`, message_fts.`coinName`, message_fts.`packetId`, message_fts.`packetUrl`, message_fts.`packetType`, message_fts.`redBagRemark`, message_fts.`isOpened`, message_fts.`redPacketStatus`, message_fts.`aitList`, message_fts.`fileUrl`, message_fts.`fileName`, message_fts.`fileSize`, message_fts.`md5`, message_fts.`sourceChannel`, message_fts.`forwardType`, message_fts.`sourceName`, message_fts.`forwardUserName`, message_fts.`sourceLog`, message_fts.`avatar`, message_fts.`nickname`, message_fts.`remark`, message_fts.`uid`, message_fts.`userLevel` FROM message_fts");
            database.execSQL("DROP TABLE message_fts");
            database.execSQL("ALTER TABLE message_fts_new RENAME TO message_fts");
        }
    };

    public static final Migration MIGRATION_18_19 = new Migration(18, 19) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE chat_message ADD COLUMN packetMode INTEGER");
            database.execSQL("ALTER TABLE recent_message ADD COLUMN packetMode INTEGER");
            database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `message_fts_new` USING FTS4(`logId` TEXT NOT NULL, `msgId` TEXT, `encrypted` INTEGER NOT NULL, `isSnap` INTEGER NOT NULL, `snapVisible` INTEGER NOT NULL, `snapCounting` INTEGER NOT NULL, `destroyTime` INTEGER NOT NULL, `ignoreInHistory` INTEGER NOT NULL, `channelType` INTEGER NOT NULL, `senderId` TEXT, `fromGId` TEXT, `receiveId` TEXT, `messageState` INTEGER NOT NULL, `msgType` INTEGER NOT NULL, `sendTime` INTEGER NOT NULL, `imageUrl` TEXT, `mediaUrl` TEXT, `isRead` INTEGER, `name` TEXT, `localPath` TEXT, `duration` REAL, `height` INTEGER, `width` INTEGER, `type` INTEGER, `roomName` TEXT, `operator` TEXT, `target` TEXT, `owner` TEXT, `names` TEXT, `mutedType` INTEGER, `opt` INTEGER, `delete_logId` TEXT, `roomId` TEXT, `markId` TEXT, `inviterId` TEXT, `roomAvatar` TEXT, `identificationInfo` TEXT, `matchOffsets` TEXT, `content` TEXT, `kid` TEXT, `encryptedMsg` TEXT, `fromKey` TEXT, `toKey` TEXT, `recordId` TEXT, `amount` TEXT, `coin` INTEGER, `coinName` TEXT, `packetId` TEXT, `packetUrl` TEXT, `packetType` INTEGER, `redBagRemark` TEXT, `isOpened` INTEGER, `redPacketStatus` INTEGER, `aitList` TEXT, `fileUrl` TEXT, `fileName` TEXT, `fileSize` INTEGER, `md5` TEXT, `sourceChannel` INTEGER, `forwardType` INTEGER, `sourceName` TEXT, `forwardUserName` TEXT, `sourceLog` TEXT, `avatar` TEXT, `nickname` TEXT, `remark` TEXT, `uid` TEXT, `userLevel` INTEGER, `like` INTEGER, `reward` INTEGER, `action` TEXT, packetMode INTEGER, tokenize=mmicu, content=`chat_message`, notindexed=`snapVisible`, notindexed=`snapCounting`, notindexed=`destroyTime`, notindexed=`imageUrl`, notindexed=`mediaUrl`, notindexed=`localPath`, notindexed=`height`, notindexed=`width`, notindexed=`roomAvatar`, notindexed=`encryptedMsg`, notindexed=`recordId`, notindexed=`coinName`, notindexed=`packetId`, notindexed=`packetUrl`, notindexed=`fileUrl`, notindexed=`fileSize`, notindexed=`md5`, notindexed=`like`, notindexed=`reward`, notindexed=`action`, notindexed=`packetMode`, order=DESC)");
            database.execSQL("INSERT INTO `message_fts_new` (`docid`, `logId`, `msgId`, `encrypted`, `isSnap`, `snapVisible`, `snapCounting`, `destroyTime`, `ignoreInHistory`, `channelType`, `senderId`, `fromGId`, `receiveId`, `messageState`, `msgType`, `sendTime`, `imageUrl`, `mediaUrl`, `isRead`, `name`, `localPath`, `duration`, `height`, `width`, `type`, `roomName`, `operator`, `target`, `owner`, `names`, `mutedType`, `opt`, `delete_logId`, `roomId`, `markId`, `inviterId`, `roomAvatar`, `identificationInfo`, `matchOffsets`, `content`, `kid`, `encryptedMsg`, `fromKey`, `toKey`, `recordId`, `amount`, `coin`, `coinName`, `packetId`, `packetUrl`, `packetType`, `redBagRemark`, `isOpened`, `redPacketStatus`, `aitList`, `fileUrl`, `fileName`, `fileSize`, `md5`, `sourceChannel`, `forwardType`, `sourceName`, `forwardUserName`, `sourceLog`, `avatar`, `nickname`, `remark`, `uid`, `userLevel`) SELECT message_fts.`rowid` AS docid, message_fts.`logId`, message_fts.`msgId`, message_fts.`encrypted`, message_fts.`isSnap`, message_fts.`snapVisible`, message_fts.`snapCounting`, message_fts.`destroyTime`, message_fts.`ignoreInHistory`, message_fts.`channelType`, message_fts.`senderId`, message_fts.`fromGId`, message_fts.`receiveId`, message_fts.`messageState`, message_fts.`msgType`, message_fts.`sendTime`, message_fts.`imageUrl`, message_fts.`mediaUrl`, message_fts.`isRead`, message_fts.`name`, message_fts.`localPath`, message_fts.`duration`, message_fts.`height`, message_fts.`width`, message_fts.`type`, message_fts.`roomName`, message_fts.`operator`, message_fts.`target`, message_fts.`owner`, message_fts.`names`, message_fts.`mutedType`, message_fts.`opt`, message_fts.`delete_logId`, message_fts.`roomId`, message_fts.`markId`, message_fts.`inviterId`, message_fts.`roomAvatar`, message_fts.`identificationInfo`, message_fts.`matchOffsets`, message_fts.`content`, message_fts.`kid`, message_fts.`encryptedMsg`, message_fts.`fromKey`, message_fts.`toKey`, message_fts.`recordId`, message_fts.`amount`, message_fts.`coin`, message_fts.`coinName`, message_fts.`packetId`, message_fts.`packetUrl`, message_fts.`packetType`, message_fts.`redBagRemark`, message_fts.`isOpened`, message_fts.`redPacketStatus`, message_fts.`aitList`, message_fts.`fileUrl`, message_fts.`fileName`, message_fts.`fileSize`, message_fts.`md5`, message_fts.`sourceChannel`, message_fts.`forwardType`, message_fts.`sourceName`, message_fts.`forwardUserName`, message_fts.`sourceLog`, message_fts.`avatar`, message_fts.`nickname`, message_fts.`remark`, message_fts.`uid`, message_fts.`userLevel` FROM message_fts");
            database.execSQL("DROP TABLE message_fts");
            database.execSQL("ALTER TABLE message_fts_new RENAME TO message_fts");
        }
    };

    public static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW `FriendView`");
            database.execSQL("DROP VIEW `RoomView`");
            database.execSQL("CREATE VIEW `FriendView` AS SELECT friends.id, friends.name, friends.remark, friends.avatar, friends.identification, friends.onTop, friends.noDisturbing FROM friends UNION SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 3");
            database.execSQL("CREATE VIEW `RoomView` AS SELECT room_list.id, room_list.name, room_list.name AS remark, room_list.avatar, room_list.identification, room_list.onTop, room_list.noDisturbing  FROM room_list UNION SELECT info_cache.id, info_cache.nickname AS name, info_cache.remark, info_cache.avatar, info_cache.identification, 2 AS onTop, 2 AS noDisturbing FROM info_cache WHERE channelType = 2");
        }
    };

    public static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_info ADD COLUMN `isChain` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("DROP TABLE room_user");
            database.execSQL("CREATE TABLE IF NOT EXISTS `room_user` (`roomId` TEXT NOT NULL, `id` TEXT NOT NULL, " +
                    "`nickname` TEXT, `roomNickname` TEXT, `avatar` TEXT, `memberLevel` INTEGER NOT NULL, " +
                    "`roomMutedType` INTEGER NOT NULL, `mutedType` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, " +
                    "`identification` INTEGER NOT NULL, `identificationInfo` TEXT, `publicKey` TEXT, " +
                    "`searchKey` TEXT, PRIMARY KEY(`roomId`, `id`))");
        }
    };

    private static final Callback DATABASE_CALL_BACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase database) {
            super.onCreate(database);
            database.execSQL("CREATE TRIGGER trigger_delete_friends_AFTER_DELETE AFTER DELETE ON friends BEGIN INSERT INTO info_cache (channelType, id, nickname, avatar, remark, identification, identificationInfo) VALUES (3, old.id, old.name, old.avatar, old.remark, old.identification, old.identificationInfo) ;END");
            database.execSQL("CREATE TRIGGER trigger_insert_friends_AFTER_INSERT AFTER INSERT ON friends BEGIN DELETE FROM info_cache WHERE channelType = 3 AND id = new.id;END");
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase database) {
            super.onOpen(database);
        }
    };
}
