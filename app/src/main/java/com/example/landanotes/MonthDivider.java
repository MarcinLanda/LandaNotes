package com.example.landanotes;

public class MonthDivider implements ListItem {
    private String month;

    public MonthDivider(String month) {
        this.month = (month.substring(0, 1) + month.substring(1).toLowerCase());
    }

    public String getMonth() {
        return month;
    }

    @Override
    public int getType() {
        return TYPE_DIVIDER;
    }
}