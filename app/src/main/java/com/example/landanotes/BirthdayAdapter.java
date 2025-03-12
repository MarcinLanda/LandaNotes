package com.example.landanotes;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BirthdayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ListItem> listItems;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    public BirthdayAdapter(List<ListItem> listItems, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.listItems = listItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ListItem.TYPE_DIVIDER){ //If its a divider, inflate the month_divider layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_divider, parent, false);
            return new MonthDividerViewHolder(view);
        } else { //Otherwise inflate the birthday_view layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.birthday_view, parent, false);
            return new BdayViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Get the text color based on the theme
        TypedValue typedValue = new TypedValue();
        Context context = holder.itemView.getContext();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

        ListItem listItem = listItems.get(position);
        if(listItem.getType() == ListItem.TYPE_DIVIDER){ //Set the month divider to the cur month
            MonthDividerViewHolder monthHolder = (MonthDividerViewHolder) holder;
            MonthDivider monthDivider = (MonthDivider) listItems.get(position);
            monthHolder.monthTextView.setText(monthDivider.getMonth());
            monthHolder.divider.setBackgroundColor(typedValue.data);
        } else { //Update all birthday values
            BdayViewHolder bdayHolder = (BdayViewHolder) holder;
            Birthday birthday = (Birthday) listItems.get(position);
            bdayHolder.nameTextView.setText(birthday.getName());
            bdayHolder.nameTextView.setTextColor(typedValue.data);

            bdayHolder.dateTextView.setText(birthday.getDate());
            bdayHolder.dateTextView.setTextColor(typedValue.data);

            bdayHolder.remainingTextView.setText(String.valueOf(birthday.daysLeft()));
            bdayHolder.remainingTextView.setTextColor(typedValue.data);

            bdayHolder.itemView.setBackgroundColor(0xFF758EB7);
            bdayHolder.itemView.setOnClickListener(v -> clickListener.onItemClick(position));
            bdayHolder.itemView.setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getType();
    }

    static class BdayViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        TextView remainingTextView;
        public BdayViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name);
            dateTextView = itemView.findViewById(R.id.date);
            remainingTextView = itemView.findViewById(R.id.remainingDays);
        }
    }

    static class MonthDividerViewHolder extends RecyclerView.ViewHolder {
        TextView monthTextView;
        View divider;
        public MonthDividerViewHolder(View view){
            super(view);
            monthTextView = view.findViewById(R.id.month_divider);
            divider = view.findViewById(R.id.divider);
        }
    }
}