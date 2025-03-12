package com.example.landanotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private boolean locked = false;
    private boolean favorite = false;
    private boolean newNote = true;
    private Menu menu;
    private Note note = null;
    private Intent data;
    private Configuration configuration;
    private Toolbar toolbar;
    private List<Integer> colorList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initColors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_menu, menu);
        this.menu = menu;
        updateNote();
        themeBack();
        themeLock();
        themeBookmark();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.lock) {
            lockNote(item);
            return true;
        } else if (id == R.id.bookmark) {
            bookmarkNote(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){ //Deprecated
            saveNote();
    }

    //If note was clicked, get its data and fill in the corresponding views
    private void updateNote(){
        data = getIntent();
        if (data != null) {
            note = data.getParcelableExtra("note");
            if(note != null) {
                newNote = false;
                TextView title = findViewById(R.id.noteTitle);
                title.setText(note.getTitle());
                TextView body = findViewById(R.id.noteBody);
                body.setText(note.getBody());
                locked = note.isLocked();
                favorite = note.isFavorite();
                themeLock();
                themeBookmark();
            }
        }
    }

    private void lockNote(MenuItem item){
        locked = !locked;
        themeLock();
    }

    private void bookmarkNote(MenuItem item){
        favorite = !favorite;
        themeBookmark();
    }

    //Set lock icon to light or dark based on set mode
    private void themeLock(){
        configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
            if(locked){
                menu.findItem(R.id.lock).setIcon(R.drawable.lock_dark);
            } else {
                menu.findItem(R.id.lock).setIcon(R.drawable.lock_open_dark);
            }
        } else {
            if(locked){
                menu.findItem(R.id.lock).setIcon(R.drawable.lock);
            } else {
                menu.findItem(R.id.lock).setIcon(R.drawable.lock_open);
            }
        }
    }

    //Set bookmark icon to light or dark based on set mode
    private void themeBookmark(){
        configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
            if(favorite){
                menu.findItem(R.id.bookmark).setIcon(R.drawable.bookmark_t_dark);
            } else {
                menu.findItem(R.id.bookmark).setIcon(R.drawable.bookmark_f_dark);
            }
        } else {
            if(favorite){
                menu.findItem(R.id.bookmark).setIcon(R.drawable.bookmark_t);
            } else {
                menu.findItem(R.id.bookmark).setIcon(R.drawable.bookmark_f);
            }
        }
    }

    //Set the back icon to light or dark based on set mode
    private void themeBack(){
        configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            toolbar.setNavigationIcon(R.drawable.back_dark);
        } else {
            toolbar.setNavigationIcon(R.drawable.back);
        }
        toolbar.setNavigationOnClickListener(view -> {
            saveNote();
        });
    }

    //5 different colors for notes
    private void initColors(){
        colorList.add(0xFF7E9680);
        colorList.add(0xFF79616F);
        colorList.add(0xFFF8956F);
        colorList.add(0xFFD87F81);
        colorList.add(0xFF758EB7);
    }

    //Save changed note
    private void saveNote(){
        if(newNote){
            if(((TextView) findViewById(R.id.noteTitle)).getText().toString().isEmpty() && ((TextView)findViewById(R.id.noteBody)).getText().toString().isEmpty()){
                super.onBackPressed();
            } else if (((TextView) findViewById(R.id.noteTitle)).getText().toString().isEmpty() || ((TextView)findViewById(R.id.noteBody)).getText().toString().isEmpty()){
                emptyDialogue();
            } else {
                int color = colorList.get((int)(Math.random() * 5));
                String title = ((TextView) findViewById(R.id.noteTitle)).getText().toString();
                String body = ((TextView) findViewById(R.id.noteBody)).getText().toString();
                note = new Note(title, body, locked, favorite, color);
                putIntent();
            }
        } else {
            if (!isChanged() || ((TextView) findViewById(R.id.noteTitle)).getText().toString().isEmpty() || ((TextView)findViewById(R.id.noteBody)).getText().toString().isEmpty()){
                super.onBackPressed();
            } else {
                note.setTitle(((TextView) findViewById(R.id.noteTitle)).getText().toString());
                note.setBody(((TextView) findViewById(R.id.noteBody)).getText().toString());
                note.lock(locked);
                note.favorite(favorite);
                putIntent();
            }
        }
    }

    //Return true if anything has changed from original note
    private boolean isChanged(){
        if(!note.getTitle().equals(((TextView)findViewById(R.id.noteTitle)).getText().toString())){
            return(true);
        } else if(!note.getBody().equals(((TextView)findViewById(R.id.noteBody)).getText().toString())){
            return(true);
        } else if(note.isLocked() != locked){
            return(true);
        } else if(note.isFavorite() != favorite){
            return(true);
        } else{
            return(false);
        }
    }

    //Dialogue when only 1 field is empty while other has text
    private void emptyDialogue(){
        new AlertDialog.Builder(this)
                .setTitle("Empty Fields")
                .setMessage("Something is empty, do you want to exit without saving?")
                .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //Return the Note Object to MainActivity
    private void putIntent(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", note);
        resultIntent.putExtra("position", data.getIntExtra("position", -1));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
