package com.example.landanotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Rect;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NotesAdapter adapter;
    private SharedPreferences sharedPreferences;
    private List<Note> notes = new ArrayList<>();
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    //RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotesAdapter(this, notes, this::openNote, this::deleteNote);
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration decoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 10;
                outRect.top = 10;
            }
        };
        recyclerView.addItemDecoration(decoration);
    //RecyclerView

    //DrawerLayout+
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //DrawerLayout menu buttons
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.note_view){
                    drawerLayout = findViewById(R.id.drawer_layout);
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (item.getItemId() == R.id.bday_view){
                    Intent intent = new Intent(MainActivity.this, MainBirthdayActivity.class);
                    noteActivity.launch(intent);
                }
                return true;
            }
        });
    //DrawerLayout

    //SharedPreferences
        //Get all saved notes and display them
        sharedPreferences = this.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        String retrievedNotes = sharedPreferences.getString("LandaNotes", null);
        if(retrievedNotes != null){
            try {
                Log.d("SharedPreferences", "Retrieved Notes: " + retrievedNotes);
                JSONArray noteArray = new JSONArray(retrievedNotes);
                for(int i = 0; i < noteArray.length(); i++){
                    notes.add(JSONToNote((JSONObject)noteArray.get(i)));
                }
                sortNotes();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    //SharedPreferences
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Set icon to white if dark mode
        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
                menu.findItem(R.id.add_note).setIcon(R.drawable.add_dark);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_note) {
            Intent intent = new Intent(this, NoteActivity.class);
            noteActivity.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Turn the notes list into a string representation of a JSONArray to be stored in sharedPrefs
    private String notesToString(){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < notes.size(); i++){
            jsonArray.put(notes.get(i).toJSON());
        }
        return(jsonArray.toString());
    }

    //Save notes to sharedPrefs
    private void saveNotes(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("LandaNotes", notesToString());
        editor.apply();
    }

    //Put all favorite notes infront
    private void sortNotes(){
        List<Note> favorites = new ArrayList<>();
        List<Note> nonFavorites = new ArrayList<>();
        for(Note note : notes){
            if (note.isFavorite()) {
                favorites.add(note);
            } else {
                nonFavorites.add(note);
            }
        }
        favorites.addAll(nonFavorites);
        notes.clear();
        notes.addAll(favorites);
        adapter.notifyItemRangeChanged(0, notes.size() - 1);
    }

    //Open the clicked Note object
    private void openNote(int pos){
        Note clickedItem = notes.get(pos);
        if(clickedItem.isLocked()){
            biometricSetup(clickedItem, pos);
        } else {
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtra("note", clickedItem);
            intent.putExtra("position", pos);
            noteActivity.launch(intent);
        }
    }

    //Ask to delete Note object when held
    private void deleteNote(int pos){
        if(pos >= 0 && pos < notes.size()){
            TypedValue typedValue = new TypedValue();
            this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Note")
                    .setMessage("Do you want to delete this note?")
                    .setPositiveButton("Yes", (d, which) -> {
                        notes.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        adapter.notifyItemRangeChanged(pos, notes.size());
                        saveNotes(); //Save notes with deleted note
                    })
                    .setNegativeButton("No", (d, which) -> {d.dismiss();})
                    .show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(typedValue.data);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(typedValue.data);
        }
    }

    //Take in a JSON object and turn it into a Note object
    private Note JSONToNote(JSONObject jsonObject) throws JSONException {
        return(new Note(jsonObject.getString("title"),jsonObject.getString("body"),jsonObject.getBoolean("locked"),jsonObject.getBoolean("favorite"), jsonObject.getInt("color")));
    }

    //Delete SharedPrefs in case it contains old, differently formated data
    private void deleteSharedPrefs(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //Display faceid/fingerprint login
    private void biometricSetup(Note clickedItem, int pos){
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("note", clickedItem);
                intent.putExtra("position", pos);
                noteActivity.launch(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using FaceID or fingerprint")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    //Launch note_activity and prepare for returned Note object
    private ActivityResultLauncher<Intent> noteActivity = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getParcelableExtra("note") != null) {
                int pos = result.getData().getIntExtra("position", -2);
                if(pos == -1){
                    notes.add(result.getData().getParcelableExtra("note"));
                    sortNotes();
                    adapter.notifyItemInserted(notes.size() - 1);
                    saveNotes();
                } else if(pos >= 0){
                    notes.set(pos, result.getData().getParcelableExtra("note"));
                    sortNotes();
                    adapter.notifyItemChanged(pos);
                    saveNotes();
                } else if(pos == -2){
                    Toast.makeText(this, "Uh oh, something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        }
    );
}