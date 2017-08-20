package io.guaong.gesturemusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.guaong.gesturemusic.activity.MusicActivity;
import io.guaong.gesturemusic.component.WaterWaveView;
import io.guaong.gesturemusic.model.Music;
import io.guaong.gesturemusic.util.MusicUtil;

/**
 * Created by 关桐 on 2017/8/2.
 * 用于播放音乐，即向activity反馈信息，且影响WaterWaveView
 */
public class MusicPlayService extends Service {

    private PlayerBinder mPlayerBinder;

    private MediaPlayer mMediaPlayer;

    private ArrayList<Music> mMusicList = new ArrayList<>();
    // 当前音乐位置
    private int mCurrentPosition = 0;
    // 音乐播放完成
    public static final int MUSIC_PLAY_COMPLETE = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPlayerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 取得音乐列表
        mMusicList = intent.getParcelableArrayListExtra("musicList");
        mPlayerBinder = new PlayerBinder();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
    }

    /**
     * 播放绑定
     */
    public class PlayerBinder extends Binder {

        /* 音乐播放顺序 */
        public static final int PLAY_ORDER = 1;
        public static final int PLAY_RANDOM = 2;
        public static final int PLAY_SINGLE = 3;

        private int mPlayOrder;

        private Timer mTimer = new Timer();

        private TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer.isPlaying()){
                    sendCurrentTimeToAnimation(mMediaPlayer.getCurrentPosition());
                }
            }
        };


        PlayerBinder(){
            initMediaPlayer();
            mPlayOrder = PLAY_ORDER;
            sendDurationToAnimation(mMusicList.get(0).getDuration());
            // 定时向WaterView发送消息更新动画
            mTimer.schedule(mTimerTask, 0, 100);
        }

        /**
         * 播放上一曲（activity中onTouch和音乐播放完成监听中使用）
         */
        public void playLast(){
            if (mCurrentPosition == 0) {
                mCurrentPosition = mMusicList.size();
            }
            mCurrentPosition = mCurrentPosition - 1;
            play(mCurrentPosition);
        }

        /**
         * 播放下一曲（activity中onTouch和音乐播放完成监听中使用）
         */
        public void playNext(){
            mCurrentPosition = (mCurrentPosition + 1) % mMusicList.size();
            play(mCurrentPosition);
        }

        /**
         * 播放当前曲目（activity中播放按钮监听使用）
         */
        public void playCurrent(){
            mMediaPlayer.start();
            sendStatusToAnimation(false);
        }

        /**
         * 暂停当前曲目（activity中播放按钮监听使用）
         */
        public void pauseCurrent(){
            mMediaPlayer.pause();
            sendStatusToAnimation(true);
        }

        /**
         * 获取当前音乐信息（activity中修改TextView内容使用）
         */
        public Music getCurrentMusic(){
            return mMusicList.get(mCurrentPosition);
        }

        /**
         * 获取当前音乐位置（activity中音乐列表监听使用）
         */
        public int getCurrentPosition(){
            return mCurrentPosition;
        }

        /**
         * 设置当前音乐位置（activity音乐列表item点击使用）
         */
        public void setCurrentPosition(int position){
            mCurrentPosition = position;
        }

        /**
         * 获取所有音乐信息（音乐列表适配器使用）
         */
        public ArrayList<Music> getMusicList(){
            return mMusicList;
        }

        /**
         * 播放
         * @param current 传入需要播放的下一首的编号
         */
        public void play(int current){
            try {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(MusicPlayService.this, mMusicList.get(current).getUri());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                // 告知WaterWaveView启动动画
                sendStatusToAnimation(false);
                // 告知WaterWaveView当前音乐时间
                sendDurationToAnimation(mMusicList.get(mCurrentPosition).getDuration());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 设置播放顺序
         */
        public void setPlayOrder(int playOrder) {
            mPlayOrder = playOrder;
        }

        /**
         * 初始化音乐播放器
         */
        private void initMediaPlayer(){
            mMediaPlayer = MediaPlayer.create(MusicPlayService.this, mMusicList.get(0).getUri());
            final PlayCompleteListener playCompleteListener = new PlayCompleteListener();
            mMediaPlayer.setOnCompletionListener(playCompleteListener);
        }

        /**
         * 向WaterWaveView发送消息
         * @param musicDuration 时长
         */
        private void sendDurationToAnimation(long musicDuration){
            sendMessageToAnimation(WaterWaveView.RESET_ANIMATION, musicDuration);
        }

        /**
         * 向WaterWaveView发送消息
         * @param b 是否停止动画
         */
        private void sendStatusToAnimation(boolean b){
           sendMessageToAnimation(WaterWaveView.CHANGE_ANIMATION, b);
        }

        /**
         * 向WaterView发送消息
         * @param currentTime 当前音乐时间
         */
        private void sendCurrentTimeToAnimation(int currentTime){
           sendMessageToAnimation(WaterWaveView.UPDATE_ANIMATION, currentTime);
        }

        /**
         * 发送消息到WaterView
         */
        private void sendMessageToAnimation(int type, Object o){
            final Message message = new Message();
            message.arg1 = type;
            message.obj = o;
            WaterWaveView.animationHandler.sendMessage(message);
        }

        /**
         * 向activity发送消息
         */
        private void sendMessageToActivity(){
            final Message message = new Message();
            message.arg1 = MUSIC_PLAY_COMPLETE;
            // 向activity发送消息，改变音乐信息
            MusicActivity.musicChangeInformationHandler.sendMessage(message);
        }

        /**
         * 播放完成监听
         */
        private class PlayCompleteListener implements MediaPlayer.OnCompletionListener{

            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (mPlayOrder){
                    case PLAY_ORDER:
                        mCurrentPosition = mCurrentPosition % mMusicList.size() + 1;
                        break;
                    case PLAY_RANDOM:
                        mCurrentPosition = (int)(Math.random() * mMusicList.size());
                        break;
                    case PLAY_SINGLE:break;
                }
                play(mCurrentPosition);
                // 发送消息给activity，改变TextView改变内容
                sendMessageToActivity();
            }
        }

    }

}
