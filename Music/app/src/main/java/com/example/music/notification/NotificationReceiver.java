package com.example.music.notification;

import static com.example.music.main.MusicApplication.ACTION_NEXT;
import static com.example.music.main.MusicApplication.ACTION_PLAY;
import static com.example.music.main.MusicApplication.ACTION_PREVIOUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.example.music.service.MusicService;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String PLAY_PAUSE = "playPause";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIt = new Intent(context, MusicService.class);
        if (actionName != null) {
            switch (actionName) {
                case ACTION_PLAY:
                    serviceIt.putExtra("ActionName", PLAY_PAUSE);
                    context.startService(serviceIt);
                    break;
                case ACTION_NEXT:
                    serviceIt.putExtra("ActionName", NEXT);
                    context.startService(serviceIt);
                    break;
                case ACTION_PREVIOUS:
                    serviceIt.putExtra("ActionName", PREVIOUS);
                    context.startService(serviceIt);
                    break;
            }
        }
    }
}
