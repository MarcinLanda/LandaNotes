package com.example.landanotes;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> notes;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    public NotesAdapter(Context context, List<Note> notes, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.notes = notes;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleTextView.setText(note.getTitle());

        //Get the text color based on the theme
        TypedValue typedValue = new TypedValue();
        Context context = holder.itemView.getContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        holder.titleTextView.setTextColor(typedValue.data);

        if(!note.isLocked()){
            holder.contentTextView.setText(note.getBody());
        } else {
            holder.contentTextView.setText(randomJoke());
        }
        holder.contentTextView.setTextColor(typedValue.data);

        if(note.isFavorite()){
            Configuration configuration = context.getResources().getConfiguration();
            int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
                holder.bookmark.setImageResource(R.drawable.bookmark_t_dark);
            } else {
                holder.bookmark.setImageResource(R.drawable.bookmark_t);
            }
        } else {
            holder.bookmark.setImageResource(0);
        }
        holder.itemView.setBackgroundColor(note.getColor());
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(position));
        holder.itemView.setOnLongClickListener(v -> {longClickListener.onItemLongClick(position); return true;});
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    //Get a random joke from a large JSON file
    private String randomJoke(){
        InputStream inputStream = context.getResources().openRawResource(R.raw.stupidstuff);
        String json = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
        try {
            JSONArray jokeList = new JSONArray(json);
            return (String) ((JSONObject)jokeList.get((int)(Math.random() * jokeList.length()))).get("body");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return("");
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        ImageView bookmark;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.name);
            contentTextView = itemView.findViewById(R.id.date);
            bookmark = itemView.findViewById(R.id.bookmarked);
        }
    }
}