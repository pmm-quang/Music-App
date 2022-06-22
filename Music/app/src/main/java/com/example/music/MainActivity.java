package com.example.music;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.music.fragment.AlbumFragment;
import com.example.music.fragment.SongFragment;
import com.example.music.model.Song;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    public static ArrayList<Song> songs;
    public static boolean shuffle = false;
    public static boolean repeat = false;
    public static ArrayList<Song> albums = new ArrayList<>();
    private String MY_SORT_PREFERENCES = "SortOrder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
    }
    private void initViewPager() {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.addFragment(new SongFragment(), "Songs");
        viewPagerAdapter.addFragment(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(viewPagerAdapter.getTitle(position))).attach();
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                   REQUEST_CODE);
        } else {
//          Toast.makeText(this, "Permission Granted !", Toast.LENGTH_LONG).show();
            songs = getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission Granted !", Toast.LENGTH_LONG).show();
                songs = getAllAudio(this);
                initViewPager();

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        }
    }

    public  ArrayList<Song> getAllAudio(Context context) {
        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREFERENCES, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        ArrayList<String> duplicate = new ArrayList<>();
        albums.clear();
        ArrayList<Song> tempAudioList = new ArrayList<>();
        String orders = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        switch (sortOrder) {
            case "sortByName" :
                orders = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;
            case "sortBySize" :
                orders = MediaStore.MediaColumns.SIZE + " DESC";
                break;
            case "sortByDate" :
                orders = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
        }
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, //for path
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection,null, null, orders);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                Song song = new Song(path, title, artist, album, duration,id);
                Log.e("path " + path, "Album " + album);
                tempAudioList.add(song);
                if (!duplicate.contains(album)) {
                    albums.add(song);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return tempAudioList;
    }

    public static class ViewPagerAdapter extends FragmentStateAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        public String getTitle(int position) {
            return titles.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREFERENCES, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.by_name:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_size:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_date:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}

