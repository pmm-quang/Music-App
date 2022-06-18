package com.example.music;

import static com.example.music.MainActivity.songs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.music.adapter.AlbumDetailsAdapter;
import com.example.music.model.Song;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<Song> albumSongs;
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albumdetails);
        albumSongs = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        albumName = getIntent().getStringExtra("albumName");
        int j = 0;
        for (int i = 0; i < songs.size(); i++) {
            if (albumName.equals(songs.get(i).getAlbum())) {
                albumSongs.add(j, songs.get(i));
                j++;
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (albumSongs.size() >= 1) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return  art;
    }
}