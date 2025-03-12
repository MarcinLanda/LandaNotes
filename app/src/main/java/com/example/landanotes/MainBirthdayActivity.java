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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainBirthdayActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private BirthdayAdapter adapter;
    SharedPreferences sharedPreferences;
    private List<String> uniqueMonths = new ArrayList<>();
    private List<Birthday> birthdays = new ArrayList<>();
    private List<ListItem> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Toolbar

        //RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BirthdayAdapter(listItems, this::openBday, this::deleteBday);
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration decoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 10;
                outRect.top = 10;
            }
        };
        recyclerView.addItemDecoration(decoration);
        //RecyclerView

        //DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

            //Menu Buttons
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener
                (new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.note_view){
                    Intent intent = new Intent(MainBirthdayActivity.this,
                                                MainActivity.class);
                    startActivity(intent);
                } else if (item.getItemId() == R.id.bday_view){
                    drawerLayout = findViewById(R.id.drawer_layout);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
        });
        //DrawerLayout

        //SharedPreferences
            //Get all saved birthdays and display them
        sharedPreferences = this.getSharedPreferences("Notes", Context.MODE_PRIVATE);
        String retrievedBdays = sharedPreferences.getString("Birthdays", null);
        if(retrievedBdays != null){
            try {
                Log.d("SharedPreferences", "Retrieved Birthdays: " + retrievedBdays);
                JSONArray bdayArray = new JSONArray(retrievedBdays);
                for(int i = 0; i < bdayArray.length(); i++){
                    birthdays.add(JSONToBday((JSONObject)bdayArray.get(i)));
                }
                sortBirthdays();
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
            Intent intent = new Intent(this, BirthdayActivity.class);
            bdayActivity.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Save birthdays to sharedPrefs
    private void saveBdays(){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < birthdays.size(); i++){
            jsonArray.put(birthdays.get(i).toJSON());
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Birthdays", jsonArray.toString());
        editor.apply();
    }

    //Sort birthdays and add month dividers
    private void sortBirthdays(){
        //Sort birthdays by days left
        Collections.sort(birthdays, Comparator.comparing(Birthday::daysLeft));

        //Clear previous listItems and add headers followed by each birthday for that month
        listItems.clear();
        uniqueMonths.clear();
        String currentMonth = "";
        for (Birthday birthday : birthdays) {
            String month = birthday.getMonth();
            if (!month.equals(currentMonth)) {
                listItems.add(new MonthDivider(month));
                uniqueMonths.add(month);
                currentMonth = month;
            }
            listItems.add(birthday);
            adapter.notifyDataSetChanged();
        }
    }

    //Open the clicked Birthday object
    private void openBday(int pos){
        Birthday clickedItem = (Birthday) listItems.get(pos);
        Intent intent = new Intent(MainBirthdayActivity.this, BirthdayActivity.class);
        intent.putExtra("birthday", clickedItem);
        intent.putExtra("position", pos);
        bdayActivity.launch(intent);
    }

    //Return how many Month Dividers exist before the specified birthday
    private int getUniqueMonths(String month, int day){
        //Check if there is 2 of the specified month, if there is, find out which one is desired
        String temp = uniqueMonths.get(0);
        if(month.equals(uniqueMonths.get(0)) && month.equals(uniqueMonths.get(uniqueMonths.size() - 1))){
            //If the day of the birthday is before the current day,
            //temporarily remove the first month from uniqueMonths
            if(day < LocalDate.now().getDayOfMonth()){
                uniqueMonths.set(0, "temp");
            }
        }
        //Iterate through each month until the right month is found
        int n = 0;
        for (String m:uniqueMonths) {
            n++;
            if(m.equals(month)){
                uniqueMonths.set(0, temp);
                return n;
            }
        }
        uniqueMonths.set(0, temp);
        return -1;
    }

    //Ask to delete Note object when held
    private void deleteBday(int pos){
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        if(pos >= 0 && pos < listItems.size()){
            AlertDialog dialog = new AlertDialog.Builder(MainBirthdayActivity.this)
                    .setTitle("Delete Birthday")
                    .setMessage("Do you want to delete this birthday?")
                    .setPositiveButton("Yes", (d, which) -> {
                        int unique = getUniqueMonths(((Birthday)listItems.get(pos)).getMonth(),
                                Integer.parseInt(((Birthday)listItems.get(pos)).getDay()));
                        birthdays.remove(pos - unique);
                        sortBirthdays();
                        saveBdays();
                    })
                    .setNegativeButton("No", (d, which) -> {d.dismiss();})
                    .show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(typedValue.data);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(typedValue.data);
        }
    }

    //Take in a JSON object and turn it into a Note object
    private Birthday JSONToBday(JSONObject jsonObject) throws JSONException {
        return(new Birthday(jsonObject.getString("name"), jsonObject.getString("date")));
    }

    //Launch birthday_activity and prepare for returned Birthday object
    private ActivityResultLauncher<Intent> bdayActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getParcelableExtra("birthday") != null) {
                    int pos = result.getData().getIntExtra("position", -2);
                    if(pos == -1){
                        birthdays.add(result.getData().getParcelableExtra("birthday"));
                        sortBirthdays();
                        saveBdays();
                    } else if(pos >= 0){
                        //Get the pos within birthdays of the cur birthday and update it
                        int unique =  getUniqueMonths(((Birthday)listItems.get(pos)).getMonth(), Integer.parseInt(((Birthday)listItems.get(pos)).getDay()));
                        if(unique != -1){
                            String tempDate = birthdays.get(pos - unique).getDate();
                            birthdays.set(pos - unique, result.getData().getParcelableExtra("birthday"));
                            if(!birthdays.get(pos - unique).getDate().equals(tempDate)){
                                sortBirthdays();
                            } else {
                                listItems.set(pos,  result.getData().getParcelableExtra("birthday"));
                            }
                            saveBdays();
                        }
                    } else if(pos == -2){
                        Toast.makeText(this, "Uh oh, something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
}