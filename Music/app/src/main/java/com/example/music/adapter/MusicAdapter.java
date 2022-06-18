package com.example.music.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.model.Song;
import com.example.music.PlayerActivity;
import com.example.music.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {

    private Context mContext;
    private ArrayList<Song> mFiles;

    public MusicAdapter(Context context, ArrayList<Song> files) {
        this.mFiles = files;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, int position) {
        holder.fileName.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null) {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.albumArt);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.music)
                    .into(holder.albumArt);
        }
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pMenu = new PopupMenu(mContext, view);
                pMenu.getMenuInflater().inflate(R.menu.menu_of_music_item, pMenu.getMenu());
                pMenu.show();
                pMenu.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {
                        case R.id.delete:
                            Toast.makeText(mContext, "DeleteClicked", Toast.LENGTH_LONG).show();
                            deleteSongFile(position, view);
                        default:
                    }
                    return true;
                });
            }
        });
    }
    private void deleteSongFile(int pos, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(pos).getId()));
        File file = new File(mFiles.get(pos).getPath());
        boolean del = file.delete();
        if (del) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, mFiles.size());
            Snackbar.make(view, "File Delete! ", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, "Can't be Delete: ", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder {

        TextView fileName;
        ImageView albumArt;
        ImageView menuMore;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            albumArt = itemView.findViewById(R.id.musicImg);
            menuMore = itemView.findViewById(R.id.menuMore);
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
