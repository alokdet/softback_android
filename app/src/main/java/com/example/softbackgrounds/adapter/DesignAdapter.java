package com.example.softbackgrounds.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.softbackgrounds.R;
import com.example.softbackgrounds.model.DesignSummary;

import java.util.ArrayList;
import java.util.List;

public class DesignAdapter extends RecyclerView.Adapter<DesignAdapter.DesignViewHolder> {

    private final List<DesignSummary> designs = new ArrayList<>();
    private final OnDesignClickListener listener;

    public interface OnDesignClickListener {
        void onDesignClick(DesignSummary design);
    }

    public DesignAdapter(OnDesignClickListener listener) {
        this.listener = listener;
    }

    public void setDesigns(List<DesignSummary> newDesigns) {
        designs.clear();

        if (newDesigns != null) {
            designs.addAll(newDesigns);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DesignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_item_layout, parent, false);
        return new DesignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DesignViewHolder holder, int position) {
        DesignSummary design = designs.get(position);
        holder.name.setText(design.getName());
        holder.version.setText("Version " + design.getVersion());
        holder.itemView.setOnClickListener(v -> listener.onDesignClick(design));
    }

    @Override
    public int getItemCount() {
        return designs.size();
    }

    static class DesignViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView version;

        DesignViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.designName);
            version = itemView.findViewById(R.id.designVersion);
        }
    }
}
