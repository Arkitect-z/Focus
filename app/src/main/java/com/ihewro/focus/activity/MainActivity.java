package com.ihewro.focus.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.ALog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.ihewro.focus.GlobalConfig;
import com.ihewro.focus.R;
import com.ihewro.focus.adapter.BaseViewPagerAdapter;
import com.ihewro.focus.bean.EventMessage;
import com.ihewro.focus.bean.Feed;
import com.ihewro.focus.bean.FeedFolder;
import com.ihewro.focus.bean.FeedItem;
import com.ihewro.focus.bean.Help;
import com.ihewro.focus.bean.UserPreference;
import com.ihewro.focus.fragemnt.UserFeedUpdateContentFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedFolderFragment;
import com.ihewro.focus.fragemnt.search.SearchFeedItemListFragment;
import com.ihewro.focus.fragemnt.search.SearchLocalFeedListFragment;
import com.ihewro.focus.task.TimingService;
import com.ihewro.focus.util.StringUtil;
import com.ihewro.focus.view.FeedFolderOperationPopupView;
import com.ihewro.focus.view.FeedListShadowPopupView;
import com.ihewro.focus.view.FeedOperationPopupView;
import com.ihewro.focus.view.FilterPopupView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.XPopupCallback;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;
import skin.support.SkinCompatManager;
import skin.support.utils.SkinPreference;


public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.playButton)
    ButtonBarLayout playButton;
    @BindView(R.id.fl_main_body)
    FrameLayout flMainBody;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar_container)
    FrameLayout toolbarContainer;

    private static final int DRAWER_FOLDER_ITEM = 847;
    private static final int DRAWER_FOLDER = 301;
    private static final int SHOW_ALL = 14;
    private static final int SHOW_STAR = 876;
    private static final int SHOW_DISCOVER = 509;
    private static final int FEED_MANAGE = 460;
    private static final int SETTING = 911;
    private static final int PAY_SUPPORT = 71;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.search_view_content)
    LinearLayout searchViewContent;
    @BindView(R.id.subtitle)
    TextView subtitle;
    public static final int RQUEST_STORAGE_READ = 8;


    private UserFeedUpdateContentFragment feedPostsFragment;
    private Fragment currentFragment = null;
    private List<IDrawerItem> subItems = new ArrayList<>();
    private Drawer drawer;
    private FeedListShadowPopupView popupView;//点击顶部标题的弹窗
    private FilterPopupView drawerPopupView;//右侧边栏弹窗
    private List<String> errorFeedIdList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();
    private SearchLocalFeedListFragment searchLocalFeedListFragment;
    private SearchFeedFolderFragment searchFeedFolderFragment;
    private SearchFeedItemListFragment searchFeedItemListFragment;


    public static void activityStart(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            toolbar.inflateMenu(R.menu.main_night);
        } else {
            toolbar.inflateMenu(R.menu.main);
        }

        if (getIntent() != null){
            boolean flag = getIntent().getBooleanExtra(GlobalConfig.is_need_update_main,false);
            if (flag){
                //更新数据
                /*updateDrawer();
                clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章");*/
            }
        }
        setSupportActionBar(toolbar);
        toolbarTitle.setText("全部文章");
        EventBus.getDefault().register(this);


        initEmptyView();

        clickFeedPostsFragment(new ArrayList<String>());

        initListener();

        initTapView();

        createTabLayout();

        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (!EasyPermissions.hasPermissions(this, perms)) {
            //没有权限 1. 申请权限
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RQUEST_STORAGE_READ, perms)
                            .setRationale("需要存储器读写权限以便后续备份和导入导出功能使用")
                            .setPositiveButtonText("确定")
                            .setNegativeButtonText("取消")
                            .build());
        }

        //开启定时任务
        startTimeService();


    }

    private void startTimeService(){
        TimingService.startService(this);
    }


    /**
     * 新手教程，第一次打开app，会自动弹出教程
     */
    private void initTapView() {
        if (UserPreference.queryValueByKey(UserPreference.FIRST_USE_LOCAL_SEARCH_AND_FILTER, "0").equals("0")) {
            if (LitePal.count(FeedItem.class) > 0) {
                new TapTargetSequence(this)
                        .targets(TapTarget.forToolbarMenuItem(toolbar, R.id.action_search, "搜索", "在这里，搜索本地内容的一切。")
                                        .cancelable(false)
                                        .drawShadow(true)
                                        .titleTextColor(R.color.colorAccent)
                                        .descriptionTextColor(R.color.text_secondary_dark)
                                        .tintTarget(true)
                                        .targetCircleColor(android.R.color.black)//内圈的颜色
                                        .id(1),

                                TapTarget.forToolbarMenuItem(toolbar, R.id.action_filter, "过滤设置", "开始按照你想要的方式显示内容吧！")
                                        .cancelable(false)
                                        .drawShadow(true)
                                        .titleTextColor(R.color.colorAccent)
                                        .descriptionTextColor(R.color.text_secondary_dark)
                                        .tintTarget(true)
                                        .targetCircleColor(android.R.color.black)//内圈的颜色
                                        .id(2))
                        .listener(new TapTargetSequence.Listener() {
                            @Override
                            public void onSequenceFinish() {
                                //设置该功能已经使用过了
                                UserPreference.updateOrSaveValueByKey(UserPreference.FIRST_USE_LOCAL_SEARCH_AND_FILTER, "1");
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                                switch (lastTarget.id()) {
                                    case 1:
                                        break;
                                    case 2:
                                        drawerPopupView.toggle();
                                        break;
                                }
                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {
                                // Boo
                            }
                        }).start();
            }

        }


    }


    private void initListener() {

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
//                searchViewContent.setVisibility(View.GONE);
                if (!query.equals("")) {
                    updateTabLayout(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                //开始同步搜索
                if (!newText.equals("")) {
                    updateTabLayout(newText);
                }
                return true;
            }

        });


        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchViewContent.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                searchViewContent.setVisibility(View.GONE);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //显示弹窗
                toggleFeedListPopupView();
            }
        });
    }

    private void createTabLayout() {
        //碎片列表
        fragmentList.clear();
        searchFeedFolderFragment = new SearchFeedFolderFragment(this);
        searchLocalFeedListFragment = new SearchLocalFeedListFragment(this);
        searchFeedItemListFragment = new SearchFeedItemListFragment(this);
        fragmentList.add(searchFeedFolderFragment);
        fragmentList.add(searchLocalFeedListFragment);
        fragmentList.add(searchFeedItemListFragment);

        //标题列表
        List<String> pageTitleList = new ArrayList<>();
        pageTitleList.add("文件夹");
        pageTitleList.add("订阅");
        pageTitleList.add("文章");

        //新建适配器
        BaseViewPagerAdapter adapter = new BaseViewPagerAdapter(getSupportFragmentManager(), fragmentList, pageTitleList);

        //设置ViewPager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        //适配夜间模式
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            tabLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary_night));
        } else {
            tabLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary));
        }

    }

    private void updateTabLayout(final String text) {

        //显示动画
        searchFeedItemListFragment.showLoading();
        searchLocalFeedListFragment.showLoading();
        searchFeedFolderFragment.showLoading();


        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<FeedItem> searchResults;
                String text2 = "%" + text + "%";
                searchResults = LitePal.where("title like ? or summary like ?", text2, text2).find(FeedItem.class);

                final List<Feed> searchResults2;
                text2 = "%" + text + "%";
                searchResults2 = LitePal.where("name like ? or desc like ?", text2, text2).find(Feed.class);

                final List<FeedFolder> searchResult3s;
                text2 = "%" + text + "%";
                searchResult3s = LitePal.where("name like ?", text2).find(FeedFolder.class);


                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchFeedItemListFragment.updateData(searchResults);
                        searchLocalFeedListFragment.updateData(searchResults2);
                        searchFeedFolderFragment.updateData(searchResult3s);
                    }
                });
            }
        }).start();


    }


    /**
     * 全文搜索🔍
     *
     * @param text
     * @return
     */
    public void queryFeedItemByText(String text) {
        List<FeedItem> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("title like ? or summary like ?", text, text).find(FeedItem.class);
        searchFeedItemListFragment.updateData(searchResults);
    }


    public void queryFeedByText(String text) {
        List<Feed> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("name like ? or desc like ? or url like ?", text, text, text).find(Feed.class);
        searchLocalFeedListFragment.updateData(searchResults);
    }

    public void queryFeedFolderByText(String text) {
        List<FeedFolder> searchResults;
        text = "%" + text + "%";
        searchResults = LitePal.where("name like ?", text).find(FeedFolder.class);
        searchFeedFolderFragment.updateData(searchResults);
    }


    private void toggleFeedListPopupView() {
        //显示弹窗
        if (popupView == null) {
            popupView = (FeedListShadowPopupView) new XPopup.Builder(MainActivity.this)
                    .atView(playButton)
                    .hasShadowBg(true)
                    .setPopupCallback(new XPopupCallback() {
                        @Override
                        public void onShow() {
                            popupView.getAdapter().setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                                @Override
                                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                                    if (view.getId() == R.id.item_view) {
                                        int feedFolderId = popupView.getFeedFolders().get(position).getId();
                                        List<Feed> feeds = LitePal.where("feedfolderid = ?", String.valueOf(feedFolderId)).find(Feed.class);
                                        ArrayList<String> list = new ArrayList<>();

                                        for (int i = 0; i < feeds.size(); i++) {
                                            list.add(String.valueOf(feeds.get(i).getId()));
                                        }
                                        //切换到指定文件夹下
                                        clickAndUpdateMainFragmentData(list, popupView.getFeedFolders().get(position).getName());
                                        popupView.dismiss();//关闭弹窗
                                    }
                                }
                            });
                        }

                        @Override
                        public void onDismiss() {
                        }
                    })
                    .asCustom(new FeedListShadowPopupView(MainActivity.this));
        }
        popupView.toggle();
    }


    public void initEmptyView() {
        initDrawer();
    }

    //初始化侧边栏
    public void initDrawer() {

        //构造侧边栏项目
        createDrawer();

        //构造右侧栏目
        createRightDrawer();

    }


    public void createDrawer() {
        //初始化侧边栏
        refreshLeftDrawerFeedList(false);


        //初始化侧边栏
        drawer = new DrawerBuilder().withActivity(this)
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems((IDrawerItem[]) Objects.requireNonNull(subItems.toArray(new IDrawerItem[subItems.size()])))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        drawerItemClick(drawerItem);
                        return false;
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(View view, int position, IDrawerItem drawerItem) {
                        drawerLongClick(drawerItem);
                        return true;
                    }
                })
                .withStickyFooter(R.layout.component_drawer_foooter)
                .withStickyFooterShadow(false)
                .build();

                drawer.setHeader(getLayoutInflater().inflate(R.layout.padding, null), false);


        //初始化顶部的内容包括颜色
        boolean flag = false;
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            flag = true;
            ((TextView)(drawer.getStickyFooter().findViewById(R.id.mode_text))).setText("日间模式");
        }else {
            ((TextView)(drawer.getStickyFooter().findViewById(R.id.mode_text))).setText("夜间模式");
        }

        final boolean finalFlag = flag;
        drawer.getStickyFooter().findViewById(R.id.mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!finalFlag) {//flag true 表示夜间模式
                    SkinCompatManager.getInstance().loadSkin("night", null, SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 200); // 延时1秒
                } else {
                    SkinCompatManager.getInstance().restoreDefaultTheme();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 200); // 延时1秒
                }
            }
        });


        drawer.getStickyFooter().findViewById(R.id.manage).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                FeedManageActivity.activityStart(MainActivity.this);
            }
        });


        drawer.getStickyFooter().findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingActivity.activityStart(MainActivity.this);
            }
        });


    }


    private void updateDrawer() {
        //初始化侧边栏
        refreshLeftDrawerFeedList(true);
        drawer.setItems(subItems);
    }

    private void drawerItemClick(IDrawerItem drawerItem) {
        if (drawerItem.getTag() != null) {
            switch ((int) drawerItem.getTag()) {
                case SHOW_ALL:
                    clickAndUpdateMainFragmentData(new ArrayList<String>(), "全部文章");
                    break;
                case SHOW_STAR:
                    StarActivity.activityStart(MainActivity.this);
                    break;
                case SHOW_DISCOVER:
                    FeedCategoryActivity.activityStart(MainActivity.this);
                    break;
                case DRAWER_FOLDER_ITEM:
                    ALog.d("名称为" + ((SecondaryDrawerItem) drawerItem).getName() + "id为" + drawerItem.getIdentifier());
                    ArrayList<String> list = new ArrayList<>();
                    list.add(String.valueOf(drawerItem.getIdentifier()));
                    clickAndUpdateMainFragmentData(list, ((SecondaryDrawerItem) drawerItem).getName().toString());
                    break;

            }
        }

    }


    private void drawerLongClick(IDrawerItem drawerItem) {

        if (drawerItem.getTag() != null) {
            switch ((int) drawerItem.getTag()) {
                case DRAWER_FOLDER:
                    //获取到这个文件夹的数据
                    new XPopup.Builder(MainActivity.this)
                            .asCustom(new FeedFolderOperationPopupView(MainActivity.this, drawerItem.getIdentifier(), ((ExpandableBadgeDrawerItem) drawerItem).getName().toString(), "", new Help(false)))
                            .show();


                    break;
                case DRAWER_FOLDER_ITEM:
                    //获取到这个feed的数据
                    new XPopup.Builder(MainActivity.this)
                            .asCustom(new FeedOperationPopupView(MainActivity.this, drawerItem.getIdentifier(), ((SecondaryDrawerItem) drawerItem).getName().toString(), "", new Help(false)))
                            .show();
                    break;
            }
        }
    }

    /**
     * 初始化主fragment
     *
     * @param feedIdList
     */
    private void clickFeedPostsFragment(ArrayList<String> feedIdList) {
        if (feedPostsFragment == null) {
            feedPostsFragment = UserFeedUpdateContentFragment.newInstance(feedIdList, toolbarTitle,subtitle);
        }
        toolbar.setTitle("全部文章");
        addOrShowFragment(getSupportFragmentManager().beginTransaction(), feedPostsFragment);
    }

    /**
     * 更新主fragment的内部数据并修改UI
     *
     * @param feedIdList
     * @param title
     */
    private void clickAndUpdateMainFragmentData(ArrayList<String> feedIdList, String title) {
        if (feedPostsFragment == null) {
            ALog.d("出现未知错误");
        } else {
            toolbarTitle.setText(title);
            feedPostsFragment.updateData(feedIdList);
        }

    }


    /**
     * 获取用户的订阅数据，显示在左侧边栏的drawer中
     */
    public void refreshLeftDrawerFeedList(boolean isUpdate) {
        subItems.clear();
        subItems.add(new SecondaryDrawerItem().withName("全部").withIcon(GoogleMaterial.Icon.gmd_home).withSelectable(true).withTag(SHOW_ALL));
        subItems.add(new SecondaryDrawerItem().withName("收藏").withIcon(GoogleMaterial.Icon.gmd_star).withSelectable(false).withTag(SHOW_STAR));
        subItems.add(new SecondaryDrawerItem().withName("发现").withIcon(GoogleMaterial.Icon.gmd_explore).withSelectable(false).withTag(SHOW_DISCOVER));
        subItems.add(new SectionDrawerItem().withName("订阅源").withDivider(false));




        List<FeedFolder> feedFolderList = LitePal.order("ordervalue").find(FeedFolder.class);
        for (int i = 0; i < feedFolderList.size(); i++) {

            int notReadNum = 0;

            List<IDrawerItem> feedItems = new ArrayList<>();
            List<Feed> feedList = LitePal.where("feedfolderid = ?", String.valueOf(feedFolderList.get(i).getId())).order("ordervalue").find(Feed.class);

            boolean haveErrorFeedInCurrentFolder = false;
            for (int j = 0; j < feedList.size(); j++) {
                final Feed temp = feedList.get(j);
                int current_notReadNum = LitePal.where("read = ? and feedid = ?", "0", String.valueOf(temp.getId())).count(FeedItem.class);

                final SecondaryDrawerItem secondaryDrawerItem = new SecondaryDrawerItem().withName(temp.getName()).withSelectable(true).withTag(DRAWER_FOLDER_ITEM).withIdentifier(feedList.get(j).getId());
                if (feedList.get(j).isErrorGet()) {
                    haveErrorFeedInCurrentFolder = true;
                    secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_sync_problem);
                } else {
                    //加载订阅的图标
                    ImageLoader.getInstance().loadImage(StringUtil.getUrlPrefix(temp.getLink()) + "/favicon.ico", new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_rss_feed);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            secondaryDrawerItem.withIcon(GoogleMaterial.Icon.gmd_rss_feed);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                            Drawable ico = new BitmapDrawable(getResources(),loadedImage);
                            secondaryDrawerItem.withIcon(ico);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });

                }

                if (current_notReadNum != 0) {
                    secondaryDrawerItem.withBadge(current_notReadNum + "");
                }
                if (isUpdate) {
                    drawer.updateItem(secondaryDrawerItem);
                }
                feedItems.add(secondaryDrawerItem);

                notReadNum += current_notReadNum;
            }
            ExpandableBadgeDrawerItem one = new ExpandableBadgeDrawerItem().withName(feedFolderList.get(i).getName()).withIdentifier(feedFolderList.get(i).getId()).withTag(DRAWER_FOLDER).withSelectable(false).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)).withSubItems(
                    feedItems
            );

            if (haveErrorFeedInCurrentFolder) {
                one.withTextColorRes(R.color.md_red_700);
            }
            if (notReadNum != 0) {
                one.withBadge(notReadNum + "");
            }
            //添加文件夹
            subItems.add(one);
        }

        //要记得把这个list置空
        errorFeedIdList.clear();
        ;

    }


    /**
     * 添加或者显示 fragment
     *
     * @param transaction
     * @param fragment
     */
    private void addOrShowFragment(FragmentTransaction transaction, Fragment fragment) {
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        //当前的fragment就是点击切换的目标fragment，则不用操作
        if (currentFragment == fragment) {
            return;
        }

        Fragment willCloseFragment = currentFragment;//上一个要切换掉的碎片
        currentFragment = fragment;//当前要显示的碎片

        if (willCloseFragment != null) {
            transaction.hide(willCloseFragment);
        }
        if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
            transaction.add(R.id.fl_main_body, currentFragment).commitAllowingStateLoss();
        } else {
            transaction.show(currentFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SkinPreference.getInstance().getSkinName().equals("night")) {
            getMenuInflater().inflate(R.menu.main_night, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_filter:

                drawerPopupView.toggle();
                break;

            case R.id.action_rsshub:
                //显示弹窗
                //之前选择的位置
                final int select = GlobalConfig.rssHub.indexOf(UserPreference.queryValueByKey(UserPreference.RSS_HUB,GlobalConfig.OfficialRSSHUB));
                ALog.d(UserPreference.getRssHubUrl());
                List<String> list = GlobalConfig.rssHub;
                new MaterialDialog.Builder(this)
                        .title("源管理")
                        .items(list)
                        .itemsCallbackSingleChoice(select, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which>=0 && which<3){
                                    UserPreference.updateOrSaveValueByKey(UserPreference.RSS_HUB,GlobalConfig.rssHub.get(which));
                                    return true;
                                }
                                return false;
                            }
                        })
                        .positiveText("选择")
                        .show();
                break;
        }
        return true;
    }

    long startTime = 0;

    @Override
    public void onBackPressed() {
        //返回键关闭🔍搜索
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                startTime = currentTime;
            } else {
                finish();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void refreshUI(EventMessage eventBusMessage) {
        if (EventMessage.feedAndFeedFolderAndItemOperation.contains(eventBusMessage.getType())) {
//            ALog.d("收到新的订阅添加，更新！" + eventBusMessage);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateDrawer();
                }
            }, 800); // 延迟一下，因为数据异步存储需要时间
        } else if (Objects.equals(eventBusMessage.getType(), EventMessage.FEED_PULL_DATA_ERROR)) {
//            ALog.d("收到错误FeedId List");
//            errorFeedIdList = eventBusMessage.getIds();
        }
    }


    private void createRightDrawer() {
        drawerPopupView = (FilterPopupView) new XPopup.Builder(this)
                .popupPosition(PopupPosition.Right)//右边
                .hasStatusBarShadow(true) //启用状态栏阴影
                .setPopupCallback(new XPopupCallback() {
                    @Override
                    public void onShow() {

                    }

                    @Override
                    public void onDismiss() {
                        //刷新当前页面的数据，因为筛选的规则变了
                        if (drawerPopupView.isNeedUpdate()) {
                            clickAndUpdateMainFragmentData(feedPostsFragment.getFeedIdList(), toolbarTitle.getText().toString());
                            drawerPopupView.setNeedUpdate(false);
                        }
                    }
                })
                .asCustom(new FilterPopupView(MainActivity.this));
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (currentFragment == null && fragment instanceof UserFeedUpdateContentFragment) {
            currentFragment = fragment;
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ALog.d("mainActivity 被销毁");
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
