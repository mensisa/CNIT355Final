package edu.purdue.zhan3050.cnit355final;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.List;


/**
 * Service for music player
 * 1. Get MusicInfo list and return the selected index
 * 2. Start foreground service
 * 3. Music storage
 * 4. Music buttons onClick handler
 */

public class MusicPlayerService extends Service {

    public static List<MusicInfo> musicInfoList;
    public static int position = -1;
    public static MediaPlayer mediaPlayer = null;
    private final static int NOTIFICATION_ID = 1;
    private final static int NOTIFICATION_NEXT = 2;
    private final static int NOTIFICATION_PRE = 3;
    private final static int NOTIFICATION_PLAY = 4;
    private final static int NOTIFICATION_TOUCH = 5;
    private final static int NOTIFICATION_DELETE = 6;
    public MusicBind musicBind = new MusicBind();
    private int currentTime;
    private int duration;
    private AudioControl audioControl;
    /*
     * Check if the music is playing
     */
    public static Boolean MUSIC_STATE = false;
    // customized notification
    private RemoteViews remoteViews;
    // constructor for notification
    private NotificationCompat.Builder mBuilder;
    // Notification
    private Notification notification2;
    private MusicPlayReceiver musicPlayReceiver;


    public class MusicBind extends Binder {
        /*
         * Get Service
         */
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        remoteViews = new RemoteViews(getPackageName(), R.layout.service_remoteview_layout);
        // Set on click notification results
        Intent intent = new Intent(this, MusicPlay.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_TOUCH, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent delIntent = new Intent(this, MusicPlayerService.class);
        PendingIntent delPendingIntent = PendingIntent.getService(this, NOTIFICATION_DELETE, delIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*
         * Next sound button
         */
        Intent intentNext = new Intent();
        intentNext.setAction("MUSCI_SWITCH");
        intentNext.putExtra("NEXT", "NEXT");
        intentNext.putExtra("PRE", "");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_NEXT, intentNext, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewNextBtn, nextPendingIntent);

        /*
         * Previous song button
         */
        Intent intentPre = new Intent();
        intentPre.setAction("MUSCI_SWITCH");
        intentPre.putExtra("NEXT", "");
        intentPre.putExtra("PRE", "PRE");
        PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PRE, intentPre, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewPreBtn, prePendingIntent);

        /*
         * Play or pause button
         */
        Intent intentPlay = new Intent("MUSCIPLAY_BROADCAST");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PLAY, intentPlay, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.remoteViewPlayBtn, playPendingIntent);

        String channelId = "CNIT355";
        mBuilder = new NotificationCompat.Builder(MusicPlayerService.this);
        mBuilder.setContentTitle("MusicPlay");
        mBuilder.setContentText("Enjoy the music.");
        mBuilder.setAutoCancel(false);
        mBuilder.setContentIntent(contentPendingIntent);
        mBuilder.setDeleteIntent(delPendingIntent);
        notification2 = mBuilder.build();

        //Start foreground service
        startForeground(NOTIFICATION_ID, notification2);

        System.out.println("Service started!");

        /*
         * Register MusicPlayReceiver
         */
        musicPlayReceiver = new MusicPlayReceiver();
        IntentFilter filter = new IntentFilter("MUSCIPLAY_BROADCAST");
        registerReceiver(musicPlayReceiver, filter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification() {
        remoteViews.setTextViewText(R.id.remoteViewTitle, musicInfoList.get(position).getTitle());
        remoteViews.setTextViewText(R.id.remoteViewAuthor, musicInfoList.get(position).getArtist());

        try {
            int imagePath = Integer.parseInt(musicInfoList.get(position).getAlbum_id());
            remoteViews.setImageViewResource(R.id.remoteViewImageId, imagePath);
        } catch (Exception e) {
            Bitmap bt = BitmapFactory.decodeFile(musicInfoList.get(position).getAlbum_id());
            remoteViews.setImageViewBitmap(R.id.remoteViewImageId, bt);

        }
        mBuilder.setContent(remoteViews);
        notification2 = mBuilder.build();
        //update foreground notification
        startForeground(NOTIFICATION_ID, notification2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        unregisterReceiver(musicPlayReceiver);
    }

    /*
     * set the index of the current song
     */
    public void setIndex(int index) {
        /*
        判断用户点击的歌曲是否时当前正在播放的歌曲
         */
        if (index != position) {
            MUSIC_STATE = false;
        } else {
            MUSIC_STATE = true;
        }
        position = index;
    }

    /*
     * set MusicInfo List
     */
    public void setMusicInfoList(List<MusicInfo> musicInfoListt) {
        musicInfoList = musicInfoListt;
    }

    /*
     * set MediaPlayer
     */
    public void setMediaPlayer(MediaPlayer mediaPlayerr) {

        if (mediaPlayer == null) {
            mediaPlayer = mediaPlayerr;
            mediaPlayer.start();
        } else if (mediaPlayer != null && !MUSIC_STATE) {
            mediaPlayer.stop();
            mediaPlayer = mediaPlayerr;
            mediaPlayer.start();
        } else {

            Intent intent1 = new Intent();
            intent1.setAction("MUSCI_BROADCAST");
            intent1.putExtra("currentTime", mediaPlayer.getCurrentPosition());
            mediaPlayer.stop();
            mediaPlayer = mediaPlayerr;
            sendBroadcast(intent1);
        }
        // update foreground notification
        updateNotification();

    }


    private class MusicPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("AudioControl") == null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_play);
                    AudioControl.isPause = true;
                } else {
                    if (currentTime == 0) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.start();
                        mediaPlayer.seekTo(currentTime);
                    }
                    AudioControl.isPause = false;
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_pause);

                }
            } else {
                if (AudioControl.isPause == false) {
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_pause);
                } else {
                    remoteViews.setImageViewResource(R.id.remoteViewPlayBtn, R.mipmap.music_play);
                }
            }
            // update foreground notification
            updateNotification();
        }
    }


}
