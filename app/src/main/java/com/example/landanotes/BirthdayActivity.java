package com.example.landanotes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BirthdayActivity  extends AppCompatActivity {
    private Birthday birthday = null;
    private Intent data;
    private boolean newBday = true;
    private Configuration configuration;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.birthday_activity);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create the month dropdown
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        AutoCompleteTextView monthListView = this.findViewById(R.id.monthDropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, months);
        monthListView.setAdapter(adapter);

        updateBday();
        themeBack();
    }

    @Override
    public void onBackPressed(){ //Deprecated
        saveBday();
    }

    //If a birthday object was clicked, retrieve its data and update the corresponding views
    private void updateBday(){
        data = getIntent();
        if (data != null) {
            birthday = data.getParcelableExtra("birthday");
            if(birthday != null) {
                newBday = false;

                //Update TextViews
                TextView name = findViewById(R.id.nameEditText);
                TextView day = findViewById(R.id.dayEditText);
                TextView year = findViewById(R.id.yearEditText);
                name.setText(birthday.getName());
                day.setText(birthday.getDate().substring(birthday.getDate().indexOf(' ') + 1, birthday.getDate().indexOf(' ') + 3));
                year.setText(birthday.getDate().substring(birthday.getDate().length() - 4));

                //Update the month dropdown
                String monthName = birthday.getDate().substring(0, birthday.getDate().indexOf(' '));
                AutoCompleteTextView monthDropdown = findViewById(R.id.monthDropdown);
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) monthDropdown.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(monthName);
                    if (position != -1) {
                        monthDropdown.setText(monthName, false);
                    }
                }
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
            saveBday();
        });
    }

    //Save changed note
    private void saveBday(){
        if(newBday){
            if(isEmpty() == 1){
                super.onBackPressed();
            } else if (isEmpty() == 2){
                emptyDialogue();
            } else {
                String name = ((TextView) findViewById(R.id.nameEditText)).getText().toString();
                String date = makeDate();
                birthday = new Birthday(name, date);
                putIntent();
            }
        } else {
            if (!isChanged() || isEmpty() == 2){
                super.onBackPressed();
            } else {
                birthday.setName(((TextView) findViewById(R.id.nameEditText)).getText().toString());
                birthday.setDate(makeDate());
                putIntent();
            }
        }
    }

    private int isEmpty(){
        if(((TextView)findViewById(R.id.monthDropdown)).getText().toString().isEmpty() &&
            ((TextView)findViewById(R.id.dayEditText)).getText().toString().isEmpty() &&
            ((TextView)findViewById(R.id.yearEditText)).getText().toString().isEmpty()){
            return 1; //All are empty
        } else if(((TextView)findViewById(R.id.monthDropdown)).getText().toString().isEmpty() ||
                ((TextView)findViewById(R.id.dayEditText)).getText().toString().isEmpty()){
            return 2; //One of month or day is empty
        } else if(((TextView)findViewById(R.id.yearEditText)).getText().toString().isEmpty()){
            return 3; //If year is empty
        }
        return 0;
    }

    //Create the date string
    private String makeDate(){
        //If date is a single digit, add a 0 to the front
        String zero = " ";
        if(((TextView)findViewById(R.id.dayEditText)).getText().toString().length() == 1){
            zero = " 0";
        }

        //Add the month and day string together
        String date = ((TextView)findViewById(R.id.monthDropdown)).getText().toString() +
                zero + ((TextView)findViewById(R.id.dayEditText)).getText().toString();

        //If the year exists, add it to the date string
        if(isEmpty() != 3){
            date = date + ", " + ((TextView)findViewById(R.id.yearEditText)).getText().toString();
        }

        return date;
    }

    //Returns true if any of the data has been changed from original
    private boolean isChanged(){
        String date = makeDate();
        if(!birthday.getName().equals(((TextView)findViewById(R.id.nameEditText)).getText().toString())){
            return(true);
        } else if(!birthday.getDate().equals(date)){
            return(true);
        } else {
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

    //Send the birthday object back to main activity
    private void putIntent(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("birthday", birthday);
        resultIntent.putExtra("position", data.getIntExtra("position", -1));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}