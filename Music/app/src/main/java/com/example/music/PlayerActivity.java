package com.example.music;

import static com.example.music.MainActivity.songs;
import static com.example.music.MainActivity.repeat;
import static com.example.music.MainActivity.shuffle;
import static com.example.music.adapter.AlbumDetailsAdapter.albumFiles;
import static com.example.music.main.MusicApplication.ACTION_NEXT;
import static com.example.music.main.MusicApplication.ACTION_PLAY;
import static com.example.music.main.MusicApplication.ACTION_PREVIOUS;
import static com.example.music.main.MusicApplication.CHANNEL_ID_2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music.interfaces.ActionPlaying;
import com.example.music.model.Song;
import com.example.music.notification.NotificationReceiver;
import com.example.music.service.MusicService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity
        implements ServiceConnection, ActionPlaying {

    private TextView songName, artistName, durationPlayed, durationTotal;
    private ImageView coverArt, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    private FloatingActionButton playPauseBtn;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    private int position = -1;

    public static ArrayList<Song> listSongs = new ArrayList<>();
    public static Uri uri;
//    public static MediaPlayer mediaPlayer;
    public MusicService musicService;
//    MediaSessionCompat mediaSC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
//        mediaSC = new MediaSessionCompat(getBaseContext(), "My audio");
        initViews();
        getIntentMethod();

//        songName.setText(listSongs.get(position).getTitle());
//        artistName.setText(listSongs.get(position).getArtist());
////       mediaPlayer.setOnCompletionListener(this);
//        musicService.onCompleted();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (musicService != null && b) {
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPos = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPos);
                    durationPlayed.setText(formattedTime(mCurrentPos));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(view -> {
            if (shuffle) {
                shuffle = false;
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
            } else {
                shuffle = true;
                shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
            }
        });
        repeatBtn.setOnClickListener(view -> {
            if (repeat) {
                repeat = false;
                repeatBtn.setImageResource(R.drawable.ic_repeat_off);
            } else {
                repeat = true;
                repeatBtn.setImageResource(R.drawable.ic_repeat_on);
            }
        });
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });

            }
        };
        nextThread.start();
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setImageResource(R.drawable.ic_play);

            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPos);

                    }
                    handler.postDelayed(this,1000);
                }
            });
        } else {
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);

            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPos);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }
    public void nextBtnClicked() {
        boolean isplaying = musicService.isPlaying();

        musicService.stop();
        musicService.release();

        if (shuffle && !repeat) {
            position = getRandomSong(listSongs.size() - 1);
        } else if (!shuffle && !repeat) {
            position = (position + 1) % listSongs.size();
        }

        uri = Uri.parse(listSongs.get(position).getPath());
//        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        musicService.createMediaPlayer(position);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPos = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPos);

                }
                handler.postDelayed(this,1000);
            }
        });
//        mediaPlayer.setOnCompletionListener(this);
        musicService.onCompleted();

        if (isplaying) {
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        } else {
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }
    public void prevBtnClicked() {
        boolean isplaying = musicService.isPlaying();
        musicService.stop();
        musicService.release();
        if (shuffle && !repeat) {
            position = getRandomSong(listSongs.size() - 1);
        } else if (!shuffle && !repeat) {
            position = (position - 1) < 0 ? (listSongs.size() - 1) : (position - 1);
        }
//        position = (position - 1) < 0 ? (listSongs.size() - 1) : (position - 1);
        uri = Uri.parse(listSongs.get(position).getPath());
//        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        musicService.createMediaPlayer(position);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        seekBar.setMax(musicService.getDuration() / 1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPos = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPos);

                }
                handler.postDelayed(this,1000);
            }
        });
//        mediaPlayer.setOnCompletionListener(this);
        musicService.onCompleted();
        if (isplaying) {
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
        } else {
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }


    private String formattedTime(int mCurrentPos) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPos % 60);
        String minutes = String.valueOf(mCurrentPos / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        }
        return totalOut;
    }

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        durationTotal.setText(formattedTime(durTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
//            Glide.with(this).asBitmap().load(art).into(coverArt);

            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImgAnimation(this, coverArt, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();

                    if (swatch != null) {
                        ImageView gredient = findViewById(R.id.imgViewGredient);
                        RelativeLayout nContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gDraw = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gredient.setBackground(gDraw);
                        GradientDrawable gDrawBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        nContainer.setBackground(gDrawBg);
                        songName.setTextColor(swatch.getTitleTextColor());
                        artistName.setTextColor(swatch.getBodyTextColor());

                    } else {
                        ImageView gredient = findViewById(R.id.imgViewGredient);
                        RelativeLayout nContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gDraw = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gredient.setBackground(gDraw);
                        GradientDrawable gDrawBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        nContainer.setBackground(gDrawBg);
                        songName.setTextColor(Color.WHITE);
                        artistName.setTextColor(Color.DKGRAY);
                    }
                }
            });

        } else {
            Glide.with(this).asBitmap().load(R.drawable.music).into(coverArt);

            ImageView gredient = findViewById(R.id.imgViewGredient);
            RelativeLayout nContainer = findViewById(R.id.mContainer);
            gredient.setBackgroundResource(R.drawable.main_bg);
            songName.setTextColor(Color.WHITE);
            artistName.setTextColor(Color.DKGRAY);
        }
    }
    public void ImgAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animationOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animationIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animationOut);
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");

        if (sender != null && sender.equals("albumDetails")) {
            listSongs = albumFiles;
        } else {
            listSongs = songs;
        }

        if (listSongs != null) {

            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(this.position).getPath());
        }

//
//        if (mediaPlayer != null) {
//            mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
////            musicService.createMediaPlayer(position);
//            mediaPlayer.start();
//        } else {
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
////            musicService.createMediaPlayer(position);
//            mediaPlayer.start();
//        }
//        musicService.showNotification(R.drawable.ic_pause);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);
//        seekBar.setMax(musicService.getDuration() / 1000);
//        metaData(uri);
    }

    private void initViews() {
        songName = findViewById(R.id.songName);
        artistName = findViewById(R.id.songArtist);
        durationPlayed = findViewById(R.id.durationPlayer);
        durationTotal = findViewById(R.id.durationTotal);
        coverArt = findViewById(R.id.coverArt);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);
        backBtn = findViewById(R.id.backBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        playPauseBtn = findViewById(R.id.playPause);
        seekBar = findViewById(R.id.seekbar);

    }
    private int getRandomSong(int i) {
        Random random = new Random();
        int tmp = random.nextInt(i + 1);
        return tmp;
    }


//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        nextBtnClicked();
//        if (musicService != null) {
////            mediaPlayer.stop();
////            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//            musicService.createMediaPlayer(position);
//            musicService.start();
//            musicService.onCompleted();
//        }
//    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
//        Toast.makeText(this, "Connected" + musicService, Toast.LENGTH_SHORT).show();
        Log.e("connect", "onServiceConnected: ");
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);

        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
//        mediaPlayer.setOnCompletionListener(this);
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }

//    void showNotification(int playPauseBtn) {
//        Intent intent = new Intent(this, PlayerActivity.class);
//        PendingIntent cIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        Intent prevIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_PREVIOUS);
//        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0,
//                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_PLAY);
//        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0,
//                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent nextIntent = new Intent(this, NotificationReceiver.class)
//                .setAction(ACTION_NEXT);
//        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0,
//                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        byte[] picture = null;
//        picture = getAlbumArt(listSongs.get(position).getPath());
//        Bitmap thumb= null;
//        if (picture != null) {
//            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
//        } else {
//            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music);
//        }
//        Notification nof = new NotificationCompat.Builder(this, CHANNEL_ID_2)
//                .setSmallIcon(playPauseBtn)
//                .setLargeIcon(thumb)
//                .setContentTitle(listSongs.get(position).getTitle())
//                .setContentText(listSongs.get(position).getArtist())
//                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
//                .addAction(playPauseBtn, "Pause", pausePending)
//                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
//                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                            .setMediaSession(mediaSC.getSessionToken()))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setOnlyAlertOnce(true)
//                .build();
//        NotificationManager nofManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        nofManager.notify(0, nof);
//    }

//    private byte[] getAlbumArt(String uri) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(uri);
//        byte[] art = retriever.getEmbeddedPicture();
//        retriever.release();
//        return  art;
//    }
}