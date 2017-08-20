package io.guaong.gesturemusic.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.guaong.gesturemusic.config.ColorConfig;
import io.guaong.gesturemusic.model.Music;
import io.guaong.gesturemusic.service.MusicPlayService;
import io.guaong.gesturemusic.R;
import io.guaong.gesturemusic.component.PlayButton;
import io.guaong.gesturemusic.component.CircleButton;
import io.guaong.gesturemusic.component.ListButton;
import io.guaong.gesturemusic.component.MenuButton;
import io.guaong.gesturemusic.component.OneToThreeButtonGroup;
import io.guaong.gesturemusic.component.OrderButton;
import io.guaong.gesturemusic.component.TimingButton;
import io.guaong.gesturemusic.util.MusicUtil;

public class MusicActivity extends AppCompatActivity {

    /* 控件 */
    // 播放按钮
    private PlayButton mPlayBtn;
    // 用于显示音乐名字
    private TextView mTitleText;
    // 用于显示音乐作者
    private TextView mArtistText;
    // 菜单按钮
    private MenuButton mMenuBtn;
    // 音乐列表按钮
    private ListButton mListBtn;
    // 播放顺序按钮
    private OrderButton mOrderBtn;
    // 定时按钮
    private TimingButton mTimingBtn;
    // 菜单布局，在有本地音乐时显示的初始布局，音乐列表不会显示
    private RelativeLayout mMenuLayout;
    // 点击音乐列表按钮后显示的音乐列表，此时菜单布局不会显示
    private RelativeLayout mListLayout;
    // 音乐列表容器
    private RecyclerView mRecycler;
    // 用于获取权限是提示用户
    private AlertDialog mAlertDialog;

    // 意图用于启动播放音乐的服务
    private Intent mIntent;
    // 播放音乐的绑定
    private MusicPlayService.PlayerBinder mPlayerBinder;
    // 连接服务和活动
    private ServiceConnection mConnection;
    // 字体
    private Typeface mTypeface;
    // 音乐列表适配器
    private MusicListAdapter mAdapter;
    // 定时器，用于定时停止音乐播放
    private Timer mTimer = new Timer();

    // 音乐列表
    private ArrayList<Music> mMusicList;
    // 播放状态
    private boolean isStopped = true;
    private boolean haveMusic;
    private boolean isFirstBack = true;

    /* 点击屏幕是按下和抬起时的位置 */
    private float downX = 0, downY = 0;
    private float upX = 0, upY = 0;

    // 用于接收来自服务中音乐播放改变的消息
    public static MusicInformationChangeHandler musicChangeInformationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setTitle("读取存储权限不可用")
                    .setMessage("请开启读写存储权限，否则无法使用该应用")
                    .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        stopService(mIntent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (haveMusic) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    upX = event.getRawX();
                    upY = event.getRawY();
                    // 夹角小于45度，判定为水平手势
                    boolean isHorizontal = Math.abs((upY - downY) / (upX - downX)) <= 0.5f;
                    // 滑动距离大于100，判定为有滑动手势
                    boolean isMoved = Math.abs(upX - downX) >= 100;
                    boolean isToLeft = (upX - downX) < 0;
                    if (isHorizontal && isMoved) {
                        if (isToLeft) {
                            mPlayerBinder.playLast();
                        } else {
                            mPlayerBinder.playNext();
                        }
                        insertMusicInformation();
                        // 一个不想解决的bug，当滑动后会在执行一遍遍从暂停到播放的动画
                        // 然而发现效果挺好看，于是决定不做修改
                        sendMessageToAnimationButton(PlayButton.PLAY);
                        isStopped = false;
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    downX = event.getRawX();
                    break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (haveMusic) {
            if (mMenuLayout.getVisibility() == View.INVISIBLE) {
                mListLayout.setVisibility(View.INVISIBLE);
                mMenuLayout.setVisibility(View.VISIBLE);
            } else {
                exitApp();
            }
        } else {
            exitApp();
        }
    }

    /**
     * 初始化activity
     */
    private void buildActivity(){
        // 全屏
        setFullWindow();
        // 取消actionBar
        cancelActionBar();
        // 判断是否有读写存储权限
        if (havePermission()){
           showNoPermissionView();
        }else {
            // 判断是否有音乐
            if (addMusicRes()) {
                showGestureMusicView();
            } else {
                showNoMusicView();
            }
        }
    }

    /**
     * 设置全屏
     */
    private void setFullWindow() {
        //设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 取消ActionBar
     */
    private void cancelActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * 是否有所需权限
     */
    private boolean havePermission(){
        return PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * 在没有权限时显示
     * 显示并提示用户没有权限
     */
    private void showNoPermissionView(){
        setContentView(R.layout.activity_no_permission);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
    }

    /**
     * 从媒体库获得音乐资源，添加入list
     * @return 是否有资源添加进list
     */
    private boolean addMusicRes(){
        mMusicList = MusicUtil.getMusicList(this);
        haveMusic = mMusicList.size() != 0;
        return haveMusic;
    }

    /**
     * 在没有音乐时显示
     */
    private void showNoMusicView(){
        setContentView(R.layout.activity_error);
    }

    /**
     * 有音乐时显示
     */
    private void showGestureMusicView(){
        setContentView(R.layout.activity_music);
        initAllObjects();
    }

    /**
     * 初始化所有用到的对象
     */
    private void initAllObjects(){
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPlayerBinder = (MusicPlayService.PlayerBinder) service;
                // 只有等到服务启动完成才可以执行，否则有可能会优先执行，造成空指针异常
                insertMusicInformation();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        findAllView();
        setTextViewFontStyle();
        initService();
        musicChangeInformationHandler = new MusicInformationChangeHandler(this);
        setAllButtonListener();
    }

    /**
     * 获得所有控件
     */
    private void findAllView() {
        mPlayBtn = (PlayButton) findViewById(R.id.a_music_btn);
        mTitleText = (TextView) findViewById(R.id.a_music_title);
        mArtistText = (TextView) findViewById(R.id.a_music_artist);
        mListBtn = (ListButton) findViewById(R.id.a_music_list);
        mOrderBtn = (OrderButton) findViewById(R.id.a_music_order);
        mMenuBtn = (MenuButton) findViewById(R.id.a_music_menu);
        mTimingBtn = (TimingButton) findViewById(R.id.a_music_timing);
        mMenuLayout = (RelativeLayout) findViewById(R.id.a_music_menu_layout);
        mListLayout = (RelativeLayout) findViewById(R.id.a_music_list_layout);
        mRecycler = (RecyclerView) findViewById(R.id.a_music_recycler);
    }

    /**
     * 设置字体
     */
    private void setTextViewFontStyle() {
        mTypeface = Typeface.createFromAsset(getAssets(), "font/nunito.ttf");
        mTitleText.setTypeface(mTypeface);
        mArtistText.setTypeface(mTypeface);
    }

    /**
     * 初始化recycler
     */
    private void initRecycler() {
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MusicListAdapter(mPlayerBinder.getMusicList());
        mRecycler.setAdapter(mAdapter);
    }

    /**
     * 初始化服务
     */
    private void initService() {
        mIntent = new Intent(this, MusicPlayService.class);
        mIntent.putParcelableArrayListExtra("musicList", mMusicList);
        startService(mIntent);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    /**
     * 设置所有按钮的监听
     */
    private void setAllButtonListener() {
        mPlayBtn.setOnClickListener(new PlayClickListener());
        mMenuBtn.setOnClickListener(new MenuClickListener());
        mOrderBtn.setOnClickListener(new OrderClickListener());
        mTimingBtn.setOnClickListener(new TimingClickListener());
        mListBtn.setOnClickListener(new ListClickListener());
    }

    /**
     * 设置音乐信息
     */
    private void insertMusicInformation() {
        mTitleText.setText(mPlayerBinder.getCurrentMusic().getTitle());
        mArtistText.setText(mPlayerBinder.getCurrentMusic().getArtist());
    }

    /**
     * 发送消息给播放按钮，改变样式
     */
    private void sendMessageToAnimationButton(int status) {
        final Message message = new Message();
        message.arg1 = status;
        PlayButton.animationHandler.sendMessage(message);
    }

    /**
     * 按钮的点击动画效果（定时按钮，播放顺序按钮使用）
     */
    private void clickAnimation(CircleButton btn) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -20);
        animation.setDuration(100);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        btn.startAnimation(animation);
    }

    /**
     * 退出app
     */
    private void exitApp(){
        final Timer timer = new Timer();
        if (isFirstBack){
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            isFirstBack = false;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    isFirstBack = true;
                }
            };
            timer.schedule(timerTask, 2000);
        }else {
            finish();
            System.exit(0);
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 播放按钮监听
     */
    class PlayClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (isStopped) {
                mPlayerBinder.playCurrent();
                isStopped = false;
                sendMessageToAnimationButton(PlayButton.PLAY);
            } else {
                mPlayerBinder.pauseCurrent();
                isStopped = true;
                sendMessageToAnimationButton(PlayButton.PAUSE);
            }
        }

    }

    /**
     * 菜单按钮监听
     */
    class MenuClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mMenuBtn.getMenuStatus() == MenuButton.MENU_BUTTON) {
                final Message message = new Message();
                message.arg1 = OneToThreeButtonGroup.ONE_TO_THREE;
                OneToThreeButtonGroup.mButtonTypeHandler.sendMessage(message);
            } else {
                final Message message = new Message();
                message.arg1 = OneToThreeButtonGroup.THREE_TO_ONE;
                OneToThreeButtonGroup.mButtonTypeHandler.sendMessage(message);
            }
        }
    }

    /**
     * 播放顺序按钮状态监听
     */
    class OrderClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            clickAnimation(mOrderBtn);
            mOrderBtn.changeStatus(mOrderBtn.getNextStatus());
            switch (mOrderBtn.getOrderStatus()) {
                case OrderButton.PLAY_ORDER:
                    mPlayerBinder.setPlayOrder(MusicPlayService.PlayerBinder.PLAY_ORDER);
                    break;
                case OrderButton.PLAY_RANDOM:
                    mPlayerBinder.setPlayOrder(MusicPlayService.PlayerBinder.PLAY_RANDOM);
                    break;
                case OrderButton.PLAY_SINGLE:
                    mPlayerBinder.setPlayOrder(MusicPlayService.PlayerBinder.PLAY_SINGLE);
                    break;
            }
        }
    }

    /**
     * 定时按钮监听
     */
    class TimingClickListener implements View.OnClickListener {

        private TimingTask mTimingTask;

        private final int TIMING_HALF_HOUR = 1800000;
        private final int TIMING_ONE_HOUR = 3600000;
        private final int TIMING_HALF_AND_AN_HOUR = 5400000;
        private final int TIMING_TWO_HOURS = 7200000;

        @Override
        public void onClick(View v) {
            clickAnimation(mTimingBtn);
            mTimingBtn.changeStatus(mTimingBtn.getNextStatus());
            switch (mTimingBtn.getTimingStatus()) {
                case TimingButton.ZERO:
                    mTimingTask.cancel();
                    break;
                case TimingButton.HALF_HOUR:
                    mTimingTask = new TimingTask();
                    mTimer.schedule(mTimingTask, TIMING_HALF_HOUR);
                    break;
                case TimingButton.AN_HOUR:
                    mTimingTask.cancel();
                    mTimingTask = new TimingTask();
                    mTimer.schedule(mTimingTask, TIMING_ONE_HOUR);
                    break;
                case TimingButton.ONE_AND_HALF_AN_HOUR:
                    mTimingTask.cancel();
                    mTimingTask = new TimingTask();
                    mTimer.schedule(mTimingTask, TIMING_HALF_AND_AN_HOUR);
                    break;
                case TimingButton.TWO_HOURS:
                    mTimingTask.cancel();
                    mTimingTask = new TimingTask();
                    mTimer.schedule(mTimingTask, TIMING_TWO_HOURS);
                    break;
            }
        }

    }

    /**
     * 音乐列表按钮监听
     */
    class ListClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mListLayout.setVisibility(View.VISIBLE);
            mMenuLayout.setVisibility(View.INVISIBLE);
            initRecycler();
            mRecycler.scrollToPosition(mPlayerBinder.getCurrentPosition());
        }
    }

    /**
     * 音乐列表设配器
     */
    class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicListHolder> {

        private List<Music> mMusicList;

        MusicListAdapter(List<Music> list) {
            mMusicList = list;
        }


        @Override
        public MusicListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list, parent, false);
            return new MusicListHolder(view);
        }

        @Override
        public void onBindViewHolder(MusicListHolder holder, int position) {
            holder.mArtist.setText(mMusicList.get(position).getArtist());
            holder.mTitle.setText(mMusicList.get(position).getTitle());
            holder.mTime.setText(mMusicList.get(position).getTime());
            holder.mLayout.setOnClickListener(new ItemClickListener(position));
            if (position == mPlayerBinder.getCurrentPosition()) {
                holder.mArtist.setTextColor(ColorConfig.BACKGROUND_CUPCAKE_COLOR);
                holder.mTitle.setTextColor(ColorConfig.BACKGROUND_CUPCAKE_COLOR);
                holder.mTime.setTextColor(ColorConfig.BACKGROUND_CUPCAKE_COLOR);
            } else {
                holder.mArtist.setTextColor(ColorConfig.BUTTON_CUPCAKE_COLOR);
                holder.mTitle.setTextColor(ColorConfig.BUTTON_CUPCAKE_COLOR);
                holder.mTime.setTextColor(ColorConfig.BUTTON_CUPCAKE_COLOR);
            }
        }

        @Override
        public int getItemCount() {
            return mMusicList.size();
        }

        class MusicListHolder extends RecyclerView.ViewHolder {

            private TextView mTitle;
            private TextView mArtist;
            private TextView mTime;
            private RelativeLayout mLayout;

            MusicListHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.i_list_title);
                mArtist = (TextView) itemView.findViewById(R.id.i_list_artist);
                mTime = (TextView) itemView.findViewById(R.id.i_list_time);
                mLayout = (RelativeLayout) itemView.findViewById(R.id.i_list_layout);
                mTitle.setTypeface(mTypeface);
                mArtist.setTypeface(mTypeface);
                mTime.setTypeface(mTypeface);
            }
        }

    }

    /**
     * recycler的item点击
     */
    class ItemClickListener implements View.OnClickListener {

        int mPosition;

        ItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            mPlayerBinder.setCurrentPosition(mPosition);
            mPlayerBinder.play(mPosition);
            insertMusicInformation();
            // 这真是个大bug，返回menu时才有动画效果
            sendMessageToAnimationButton(PlayButton.PLAY);
            isStopped = false;
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 定时
     */
    class TimingTask extends TimerTask {

        @Override
        public void run() {
            // 告知server停止播放音乐
            mPlayerBinder.pauseCurrent();
            isStopped = true;
            // 告知播放按钮音乐停止
            sendMessageToAnimationButton(PlayButton.PAUSE);
            // 告知定时按钮时间到，改变状态
            mTimingBtn.changeStatus(TimingButton.ZERO);
        }
    }

    /**
     * 接收音乐播放下一曲的消息
     * 消息来自MusicPlayService
     * 用于告诉MusicActivity需要改变TextView中所显示的音乐信息
     */
    public static class MusicInformationChangeHandler extends Handler {

        private WeakReference<MusicActivity> mWeakReference;

        MusicInformationChangeHandler(MusicActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == MusicPlayService.MUSIC_PLAY_COMPLETE) {
                mWeakReference.get().insertMusicInformation();
            }
        }
    }

}

