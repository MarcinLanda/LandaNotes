package com.example.landanotes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Note implements Parcelable {
    private String title;
    private String body;
    private boolean locked;
    private boolean favorite;
    private int color;

    public Note(String title, String body, boolean locked, boolean favorite, int color){
        this.title = title;
        this.body = body;
        this.locked = locked;
        this.favorite = favorite;
        this.color = color;
    }

    public String getTitle(){
        return this.title;
    }
    public String getBody(){ return this.body;}
    public int getColor(){ return this.color;}
    public boolean isLocked(){ return this.locked;}
    public boolean isFavorite(){
        return this.favorite;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setBody(String body){
        this.body = body;
    }
    public void lock(boolean locked){ this.locked = locked;}
    public void favorite(boolean favorite){ this.favorite = favorite;}
    public void setColor(int color){ this.color = color;}

    //Returns a JSONObject consisting of all Note object features
    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try{
            json.put("title", title);
            json.put("body", body);
            json.put("locked", locked);
            json.put("favorite", favorite);
            json.put("color", color);
        } catch (JSONException e){
            Log.println(Log.WARN, "Note.java","Uh oh, Stinky: something wrong with JSON (65)");
        }
        return(json);
    }

//Makes Note Object Parcelable
    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }
        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    protected Note(Parcel in) {
        title = in.readString();
        body = in.readString();
        locked = in.readBoolean();
        favorite = in.readBoolean();
        color = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.body);
        dest.writeBoolean(this.locked);
        dest.writeBoolean(this.favorite);
        dest.writeInt(color);
    }
//Makes Note Object Parcelable
}
