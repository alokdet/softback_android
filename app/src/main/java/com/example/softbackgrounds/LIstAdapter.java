package com.example.softbackgrounds;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LIstAdapter extends RecyclerView.Adapter<LIstAdapter.ListItemHolder> {

    private List<Model> list;

    public LIstAdapter(List<Model> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item_layout, parent, false);
        return new ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemHolder holder, int position) {
        holder.titleView.setText(list.get(position).getTitle());
        holder.textView.setText(list.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView titleView;
        private AppCompatTextView textView;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            titleView = (AppCompatTextView) itemView.findViewById(R.id.date_view);
            textView = (AppCompatTextView) itemView.findViewById(R.id.preview);
        }
    }
}
