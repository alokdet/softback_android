
package com.example.softbackgrounds.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softbackgrounds.R;

public class MusicAdapter extends ListAdapter<MusicTrack, MusicAdapter.TrackViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(MusicTrack track, int position);
    }

    private final OnTrackClickListener listener;
    private final ArtworkLoader artworkLoader;

    private static final DiffUtil.ItemCallback<MusicTrack> DIFF_CALLBACK = new DiffUtil.ItemCallback<MusicTrack>() {
        @Override
        public boolean areItemsTheSame(@NonNull MusicTrack oldItem, @NonNull MusicTrack newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull MusicTrack oldItem, @NonNull MusicTrack newItem) {
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getArtist().equals(newItem.getArtist())
                    && oldItem.getAlbum().equals(newItem.getAlbum())
                    && oldItem.getDuration() == newItem.getDuration()
                    && java.util.Objects.equals(oldItem.getArtworkUri(),
                    newItem.getArtworkUri()
            );
        }
    };

    public MusicAdapter(Context context, OnTrackClickListener listener) {
        super(DIFF_CALLBACK);

        this.listener = listener;
        this.artworkLoader = ArtworkLoader.getInstance(context);
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_test_item, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull TrackViewHolder holder) {
        super.onViewRecycled(holder);
        artworkLoader.cancel(holder.artwork);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        MusicTrack track = getItem(position);

        holder.title.setText(track.getTitle());
        holder.artist.setText(track.getArtist());

        artworkLoader.load(track.getArtworkUri(), holder.artwork);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();

            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onTrackClick(
                        getItem(currentPosition),
                        currentPosition
                );
            }
        });
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView artwork;
        AppCompatTextView title;
        AppCompatTextView artist;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);

            artwork = (AppCompatImageView) itemView.findViewById(R.id.album_art);
            title = (AppCompatTextView) itemView.findViewById(R.id.track_name);
            artist = (AppCompatTextView) itemView.findViewById(R.id.track_artist);
        }
    }
}
