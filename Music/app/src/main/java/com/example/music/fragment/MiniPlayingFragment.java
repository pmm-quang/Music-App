package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MiniPlayingFragment} factory method to
 * create an instance of this fragment.
 */
public class MiniPlayingFragment extends Fragment {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View view;

    public MiniPlayingFragment() {
        // Required empty public constructor
    }


        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_mini_playing, container, false);
            artist = view.findViewById(R.id.songArtistBottom);
            songName = view.findViewById(R.id.songNameBottom);
            albumArt = view.findViewById(R.id.bottomAlbumArt);
            nextBtn = view.findViewById(R.id.skipNextBottom);
            playPauseBtn = view.findViewById(R.id.miniPlayPauseBottom);
        return view;
    }
}