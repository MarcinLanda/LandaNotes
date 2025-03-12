package com.example.landanotes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Month;

interface ListItem {
    int TYPE_BIRTHDAY = 0;
    int TYPE_DIVIDER = 1;
    int getType();
}

public class Birthday implements ListItem, Parcelable {
    private String name;
    private String date;

    public Birthday(String name, String date){
        this.name = name;
        this.date = date;
    }


    @Override
    public int getType(){
        return TYPE_BIRTHDAY;
    }

    public String getName(){
        return this.name;
    }
    public String getDate(){ return this.date;}
    public void setName(String name){this.name = name;}
    public void setDate(String date){this.date = date;}

    //Return the month of the birthday
    public String getMonth(){
        return(this.date.substring(0, this.date.indexOf(' ')).toUpperCase());
    }

    //Return the day of the birthday
    public String getDay(){
        return(this.date.substring(this.date.indexOf(' ') + 1, this.date.indexOf(' ') + 3));
    }

    //Get the amount of days from today to the birthday
    public int daysLeft(){
        long today = LocalDate.now().toEpochDay();
        long bday = LocalDate.of(LocalDate.now().getYear(), Month.valueOf(getMonth()).getValue(), Integer.parseInt(getDay())).toEpochDay();
        long timeRemaining = bday - today;
        //If birthday already past, calculate it for next year
        if(timeRemaining < 0){
            timeRemaining += 365;
        }
        return Math.toIntExact(timeRemaining);
    }

    //Returns a JSONObject consisting of all Note object features
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try{
            json.put("name", name);
            json.put("date", date);
        } catch (JSONException e){
            Log.println(Log.WARN, "Birthday.java","Uh oh, Stinky: something wrong with JSON (65)");
        }
        return(json);
    }

//Makes Birthday Object Parcelable
    public static final Creator<Birthday> CREATOR = new Creator<Birthday>() {
        @Override
        public Birthday createFromParcel(Parcel in) {
            return new Birthday(in);
        }
        @Override
        public Birthday[] newArray(int size) {
            return new Birthday[size];
        }
    };

    protected Birthday(Parcel in) {
        name = in.readString();
        date = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.date);
    }
//Makes Birthday Object Parcelable
}

