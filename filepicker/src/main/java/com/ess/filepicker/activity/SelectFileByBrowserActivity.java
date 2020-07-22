package com.ess.filepicker.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.filepicker.R;
import com.ess.filepicker.SelectOptions;
import com.ess.filepicker.adapter.BreadAdapter;
import com.ess.filepicker.adapter.FileListAdapter;
import com.ess.filepicker.adapter.SelectSdcardAdapter;
import com.ess.filepicker.model.BreadModel;
import com.ess.filepicker.model.EssFile;
import com.ess.filepicker.model.EssFileCountCallBack;
import com.ess.filepicker.model.EssFileListCallBack;
import com.ess.filepicker.task.EssFileCountTask;
import com.ess.filepicker.task.EssFileListTask;
import com.ess.filepicker.util.Const;
import com.ess.filepicker.util.FileUtils;
import com.fuzamei.common.utils.BarUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件浏览界面
 */
public class SelectFileByBrowserActivity extends AppCompatActivity
        implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemChildClickListener,
        View.OnClickListener, EssFileListCallBack, EssFileCountCallBack, FileListAdapter.onLoadFileCountListener {

    /*todo 是否可预览文件，默认可预览*/
    private boolean mCanPreview = true;

    /*当前目录，默认是SD卡根目录*/
    private String mCurFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /*当前目录，微信目录*/
    private final String mWeChatFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tencent/MicroMsg/Download/";
    /*所有可访问存储设备列表*/
    private List<String> mSdCardList;

    private View weChatFolder;

    private FileListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView mBreadRecyclerView;
    private ImageView mImbSelectSdCard;
//    private Toolbar mToolBar;
    private BreadAdapter mBreadAdapter;
    private PopupWindow mSelectSdCardWindow;
    private Button btnConfirm;
    private ImageView ivBack;

    /*是否刚才切换了SD卡路径*/
    private boolean mHasChangeSdCard = false;
    /*已选中的文件列表*/
    private ArrayList<EssFile> mSelectedFileList = new ArrayList<>();
    /*当前选中排序方式的位置*/
    private int mSelectSortTypeIndex = 0;

    private EssFileListTask essFileListTask;
    private EssFileCountTask essFileCountTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);

        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.basic_color_bg), 0);
        BarUtils.setStatusBarLightMode(this, true);
        mSdCardList = FileUtils.getAllSdPaths(this);
        if (!mSdCardList.isEmpty()) {
            mCurFolder = mSdCardList.get(0) + File.separator;
        }
        initUi();
        initData();
    }

    private void initUi() {
        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        weChatFolder = findViewById(R.id.tv_wechat);
        weChatFolder.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.rcv_file_list);
        mBreadRecyclerView = findViewById(R.id.breadcrumbs_view);
        mImbSelectSdCard = findViewById(R.id.imb_select_sdcard);
        mImbSelectSdCard.setOnClickListener(this);
        if (!mSdCardList.isEmpty() && mSdCardList.size() > 1) {
            mImbSelectSdCard.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FileListAdapter(new ArrayList<EssFile>());
        mAdapter.setLoadFileCountListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.bindToRecyclerView(mRecyclerView);
        mAdapter.setOnItemClickListener(this);

        mBreadRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mBreadAdapter = new BreadAdapter(new ArrayList<BreadModel>());
        mBreadRecyclerView.setAdapter(mBreadAdapter);
        mBreadAdapter.bindToRecyclerView(mBreadRecyclerView);
        mBreadAdapter.setOnItemChildClickListener(this);

        btnConfirm.setText(String.format(getString(R.string.file_picker_selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(SelectOptions.getInstance().maxCount)));
    }

    private void initData() {
        executeListTask(mSelectedFileList, mCurFolder, SelectOptions.getInstance().getFileTypes(), SelectOptions.getInstance().getSortType());
    }

    private void gotoWechatFolder() {
        executeListTask(mSelectedFileList, mWeChatFolder, SelectOptions.getInstance().getFileTypes(), SelectOptions.getInstance().getSortType());
    }

    private void executeListTask(List<EssFile> essFileList, String queryPath, String[] types, int sortType) {
        essFileListTask = new EssFileListTask(essFileList, queryPath, types, sortType, this);
        essFileListTask.execute();
    }

    /**
     * 查找到文件列表后
     *
     * @param queryPath 查询路径
     * @param fileList  文件列表
     */
    @Override
    public void onFindFileList(String queryPath, List<EssFile> fileList) {
        if (fileList.isEmpty()) {
            mAdapter.setEmptyView(R.layout.empty_file_list);
        }
        mCurFolder = queryPath;
        mAdapter.setNewData(fileList);
        List<BreadModel> breadModelList = FileUtils.getBreadModeListFromPath(mSdCardList, mCurFolder);
        if (mHasChangeSdCard) {
            mBreadAdapter.setNewData(breadModelList);
            mHasChangeSdCard = false;
        } else {
            if (breadModelList.size() > mBreadAdapter.getData().size()) {
                //新增
                List<BreadModel> newList = BreadModel.getNewBreadModel(mBreadAdapter.getData(), breadModelList);
                mBreadAdapter.addData(newList);
            } else {
                //减少
                int removePosition = BreadModel.getRemovedBreadModel(mBreadAdapter.getData(), breadModelList);
                if (removePosition > 0) {
                    mBreadAdapter.setNewData(mBreadAdapter.getData().subList(0, removePosition));
                }
            }
        }

        mBreadRecyclerView.smoothScrollToPosition(mBreadAdapter.getItemCount() - 1);
        //先让其滚动到顶部，然后再scrollBy，滚动到之前保存的位置
        mRecyclerView.scrollToPosition(0);
        int scrollYPosition = mBreadAdapter.getData().get(mBreadAdapter.getData().size() - 1).getPrePosition();
        //恢复之前的滚动位置
        mRecyclerView.scrollBy(0, scrollYPosition);
    }

    /**
     * 显示选择SdCard的PopupWindow
     * 点击其他区域隐藏，阴影
     */
    private void showPopupWindow() {
        if (mSelectSdCardWindow != null) {
            mSelectSdCardWindow.showAsDropDown(mImbSelectSdCard);
            return;
        }
        View popView = LayoutInflater.from(this).inflate(R.layout.pop_select_sdcard, null);
        mSelectSdCardWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSelectSdCardWindow.setFocusable(true);
        mSelectSdCardWindow.setOutsideTouchable(true);
        RecyclerView recyclerView = popView.findViewById(R.id.rcv_pop_select_sdcard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SelectSdcardAdapter adapter = new SelectSdcardAdapter(FileUtils.getAllSdCardList(mSdCardList));
        recyclerView.setAdapter(adapter);
        adapter.bindToRecyclerView(recyclerView);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapterIn, View view, int position) {
                mSelectSdCardWindow.dismiss();
                mHasChangeSdCard = true;
                executeListTask(mSelectedFileList, FileUtils.getChangeSdCard(adapter.getData().get(position), mSdCardList), SelectOptions.getInstance().getFileTypes(), SelectOptions.getInstance().getSortType());
            }
        });
        mSelectSdCardWindow.showAsDropDown(mImbSelectSdCard);
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }


    @Override
    public void onFindChildFileAndFolderCount(int position, String childFileCount, String childFolderCount) {
        try {
            // 第三方文件选择库，有时候会在这里抛出异常
            mAdapter.getData().get(position).setChildCounts(childFileCount, childFolderCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.notifyItemChanged(position, "childCountChanges");
    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (adapter.equals(mAdapter)) {
            EssFile item = mAdapter.getData().get(position);
            if (item.isDirectory()) {
                //点击文件夹
                //保存当前的垂直滚动位置
                mBreadAdapter.getData().get(mBreadAdapter.getData().size() - 1).setPrePosition(mRecyclerView.computeVerticalScrollOffset());
                executeListTask(mSelectedFileList, mCurFolder + item.getName() + File.separator, SelectOptions.getInstance().getFileTypes(), SelectOptions.getInstance().getSortType());
            } else {
                if (mAdapter.getData().get(position).isChecked()) {
                    int index = findFileIndex(item);
                    if (index != -1) {
                        mSelectedFileList.remove(index);
                    }
                } else {
                    if (mSelectedFileList.size() >= SelectOptions.getInstance().maxCount) {
                        //超出最大可选择数量后
                        Snackbar.make(mRecyclerView, getString(R.string.file_picker_error_max_count, SelectOptions.getInstance().maxCount), Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    mSelectedFileList.add(item);
                }
                //选中某文件后，判断是否单选
                if (SelectOptions.getInstance().isSingle) {
                    Intent result = new Intent();
                    result.putParcelableArrayListExtra(Const.EXTRA_RESULT_SELECTION, mSelectedFileList);
                    setResult(RESULT_OK, result);
                    super.onBackPressed();
                    return;
                }
                mAdapter.getData().get(position).setChecked(!mAdapter.getData().get(position).isChecked());
                mAdapter.notifyItemChanged(position, "");
                btnConfirm.setText(String.format(getString(R.string.file_picker_selected_file_count), String.valueOf(mSelectedFileList.size()), String.valueOf(SelectOptions.getInstance().maxCount)));
            }
        }
    }

    /**
     * 查找文件位置
     */
    private int findFileIndex(EssFile item) {
        for (int i = 0; i < mSelectedFileList.size(); i++) {
            if (mSelectedFileList.get(i).getAbsolutePath().equals(item.getAbsolutePath())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBackPressed() {
        if (!FileUtils.canBackParent(mCurFolder, mSdCardList)) {
            super.onBackPressed();
            return;
        }
        executeListTask(mSelectedFileList, new File(mCurFolder).getParentFile().getAbsolutePath() + File.separator, SelectOptions.getInstance().getFileTypes(),SelectOptions.getInstance().getSortType());
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (adapter.equals(mBreadAdapter) && view.getId() == R.id.btn_bread) {
            //点击某个路径时
            String queryPath = FileUtils.getBreadModelListByPosition(mSdCardList, mBreadAdapter.getData(), position);
            if (mCurFolder.equals(queryPath)) {
                return;
            }
            executeListTask(mSelectedFileList, queryPath, SelectOptions.getInstance().getFileTypes(),SelectOptions.getInstance().getSortType());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(essFileListTask!=null){
            essFileListTask.cancel(true);
        }
        if(essFileCountTask!=null){
            essFileCountTask.cancel(true);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imb_select_sdcard) {
            showPopupWindow();
        } else if (id == R.id.btnConfirm) {
            if (mSelectedFileList != null && !mSelectedFileList.isEmpty()) {
                exit();
            } else {
                Toast.makeText(this, R.string.file_picker_error_select_file, Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.ivBack) {
            finish();
        } else if (id == R.id.tv_wechat) {
            if (mCurFolder.equals(mWeChatFolder)) {
                return;
            }
            if (!new File(mWeChatFolder).exists()) {
                Toast.makeText(this, R.string.file_picker_error_wechat_file_no_exits, Toast.LENGTH_SHORT).show();
                return;
            }
            executeListTask(mSelectedFileList, mWeChatFolder, SelectOptions.getInstance().getFileTypes(),SelectOptions.getInstance().getSortType());
        }
    }

    public void exit() {
        Intent result = new Intent();
        result.putParcelableArrayListExtra(Const.EXTRA_RESULT_SELECTION, mSelectedFileList);
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.browser_sort) {
            //排序
            new AlertDialog
                    .Builder(this)
                    .setSingleChoiceItems(R.array.file_picker_sort_list, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectSortTypeIndex = which;
                        }
                    })
                    .setNegativeButton(R.string.file_picker_desc, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (mSelectSortTypeIndex) {
                                case 0:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_NAME_DESC);
                                    break;
                                case 1:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_TIME_ASC);
                                    break;
                                case 2:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_SIZE_DESC);
                                    break;
                                case 3:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_EXTENSION_DESC);
                                    break;
                            }
                            //恢复排序
                            mBreadAdapter.getData().get(mBreadAdapter.getData().size() - 1).setPrePosition(0);
                            executeListTask(mSelectedFileList, mCurFolder, SelectOptions.getInstance().getFileTypes(),SelectOptions.getInstance().getSortType());
                        }
                    })
                    .setPositiveButton(R.string.file_picker_asc, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (mSelectSortTypeIndex) {
                                case 0:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_NAME_ASC);
                                    break;
                                case 1:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_TIME_DESC);
                                    break;
                                case 2:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_SIZE_ASC);
                                    break;
                                case 3:
                                    SelectOptions.getInstance().setSortType(FileUtils.BY_EXTENSION_ASC);
                                    break;
                            }
                            //恢复排序
                            mBreadAdapter.getData().get(mBreadAdapter.getData().size() - 1).setPrePosition(0);
                            executeListTask(mSelectedFileList, mCurFolder, SelectOptions.getInstance().getFileTypes(),SelectOptions.getInstance().getSortType());
                        }
                    })
                    .setTitle(R.string.file_picker_select_title)
                    .show();

        }
        return true;
    }

    @Override
    public void onLoadFileCount(int position) {
        essFileCountTask = new EssFileCountTask(position, mAdapter.getData().get(position).getAbsolutePath(), SelectOptions.getInstance().getFileTypes(), this);
        essFileCountTask.execute();
    }
}
