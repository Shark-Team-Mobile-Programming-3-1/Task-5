package com.example.ugandaconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Adapter that shows a list of rows. It can also filter the rows (for the search icon).
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    // One row of data
    public static class Row {
        public String title;
        public String subtitle;
        public String target; // screen to open when clicked (null = nothing happens)

        public Row(String title, String subtitle, String target) {
            this.title = title;
            this.subtitle = subtitle;
            this.target = target;
        }
    }

    // Used to tell the fragment that a row was clicked
    public interface OnRowClickListener {
        void onRowClick(Row row);
    }

    private final List<Row> allRows;    // every row
    private final List<Row> shownRows;  // rows that match the search
    private final OnRowClickListener listener;

    public ItemAdapter(List<Row> rows, OnRowClickListener listener) {
        this.allRows = rows;
        this.shownRows = new ArrayList<>(rows);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Row row = shownRows.get(position);
        holder.titleText.setText(row.title);
        holder.subtitleText.setText(row.subtitle);
        holder.itemView.setOnClickListener(v -> listener.onRowClick(row));
    }

    @Override
    public int getItemCount() {
        return shownRows.size();
    }

    // Keep only the rows that contain the search text
    public void filter(String text) {
        shownRows.clear();
        if (text == null || text.trim().isEmpty()) {
            shownRows.addAll(allRows);
        } else {
            String search = text.toLowerCase(Locale.getDefault());
            for (Row row : allRows) {
                boolean inTitle = row.title.toLowerCase(Locale.getDefault()).contains(search);
                boolean inSubtitle = row.subtitle.toLowerCase(Locale.getDefault()).contains(search);
                if (inTitle || inSubtitle) {
                    shownRows.add(row);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Holds the views of one row
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.title_text);
            subtitleText = itemView.findViewById(R.id.subtitle_text);
        }
    }
}