package com.example.music.service;

import static com.example.music.PlayerActivity.listSongs;
import static com.example.music.main.MusicApplication.ACTION_NEXT;
import static com.example.music.main.MusicApplication.ACTION_PLAY;
import static com.example.music.main.MusicApplication.ACTION_PREVIOUS;
import static com.example.music.main.MusicApplication.CHANNEL_ID_2;
import static com.example.music.notification.NotificationReceiver.NEXT;
import static com.example.music.notification.NotificationReceiver.PLAY_PAUSE;
import static com.example.music.notification.NotificationReceiver.PREVIOUS;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music.PlayerActivity;
import com.example.music.R;
import com.example.music.interfaces.ActionPlaying;
import com.example.music.model.Song;
import com.example.music.notification.NotificationReceiver;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder =  new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<Song> songs = new ArrayList<>();
    ActionPlaying action;
    MediaSessionCompat mediaSC;
    Uri uri;
    int position = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSC = new MediaSessionCompat(getBaseContext(), "My audio");

    }

    private void playMedia(int StartPosition) {
        songs = listSongs;
        position = StartPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (songs != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }

        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }

    }

    public void start() {
        mediaPlayer.start();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (action != null) {
            action.nextBtnClicked();
            if (this.mediaPlayer != null) {
                createMediaPlayer(position);
                this.mediaPlayer.start();
                onCompleted();
            }
        }
    }

    public void createMediaPlayer(int pos) {
        position = pos;
        uri = Uri.parse(songs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }




    public class MyBinder extends Binder{

        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int mPos = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (mPos != -1) {
            playMedia(mPos);
        }
        if (actionName != null) {
            switch (actionName) {
                case PLAY_PAUSE:
                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    if (action != null) {
                        Log.e("Inside", "Action");
                        action.playPauseBtnClicked();
                    }
                    break;
                case NEXT:
                    Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show();
                    if (action != null) {
                        Log.e("Inside", "Action");
                        action.nextBtnClicked();
                    }
                    break;
                case PREVIOUS:
                    Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show();
                    if (action != null) {
                        Log.e("Inside", "Action");
                        action.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    public  void setCallBack(ActionPlaying ac) {
        this.action =ac;
    }

    public void showNotification(int playPauseBtn) {
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent cIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0,
                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(songs.get(position).getPath());
        Bitmap thumb= null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music);
        }
        Notification nof = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(songs.get(position).getTitle())
                .setContentText(songs.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSC.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
//        NotificationManager nofManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        nofManager.notify(0, nof);
       startForeground(2, nof);
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return  art;
    }

}
