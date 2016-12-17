package com.example.avjindersinghsekhon.minimaltodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import fr.ganfra.materialspinner.MaterialSpinner;

public class ReminderActivity extends AppCompatActivity{
    private TextView mtoDoTextTextView;
    private Button mRemoveToDoButton;
    private MaterialSpinner mSnoozeSpinner;
    private String[] snoozeOptionsArray;
    private StoreRetrieveData storeRetrieveData;
    private ArrayList<ToDoItem> mToDoItems;
    private ToDoItem mItem;
    public static final String EXIT = "com.avjindersekhon.exit";
    private TextView mSnoozeTextView;
    String Date = "";
    String Todo = "";
    String Time = "";
    String theme;
    //AnalyticsApplication app;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
       // app = (AnalyticsApplication)getApplication();
       // app.send(this);

        theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
        if(theme.equals(MainActivity.LIGHTTHEME)){
            setTheme(R.style.CustomStyle_LightTheme);
        }
        else{
            setTheme(R.style.CustomStyle_DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);
        storeRetrieveData = new StoreRetrieveData(this, MainActivity.FILENAME);
        mToDoItems = MainActivity.getLocallyStoredData(storeRetrieveData);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));



        Intent i = getIntent();
        UUID id = (UUID)i.getSerializableExtra(TodoNotificationService.TODOUUID);
        mItem = null;
        for(ToDoItem toDoItem : mToDoItems){
            if (toDoItem.getIdentifier().equals(id)){
                mItem = toDoItem;
                break;
            }
        }

        snoozeOptionsArray = getResources().getStringArray(R.array.snooze_options);

        mRemoveToDoButton = (Button)findViewById(R.id.toDoReminderRemoveButton);
        mtoDoTextTextView = (TextView)findViewById(R.id.toDoReminderTextViewBody);
        mSnoozeTextView = (TextView)findViewById(R.id.reminderViewSnoozeTextView);
        mSnoozeSpinner = (MaterialSpinner)findViewById(R.id.todoReminderSnoozeSpinner);

//        mtoDoTextTextView.setBackgroundColor(item.getTodoColor());
        mtoDoTextTextView.setText(mItem.getToDoText());

        if(theme.equals(MainActivity.LIGHTTHEME)){
            mSnoozeTextView.setTextColor(getResources().getColor(R.color.secondary_text));
        }
        else{
            mSnoozeTextView.setTextColor(Color.WHITE);
            mSnoozeTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_snooze_white_24dp,0,0,0
            );
        }

        mRemoveToDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //app.send(this, "Action", "Todo Removed from Reminder Activity");
                Date = mItem.getToDoDate().toString();
                 if(mItem.getToDoDate().getHours() <6){
                     Time = "새벽";
                 }else if(mItem.getToDoDate().getHours() <12){
                     Time = "오전";
                 }else if(mItem.getToDoDate().getHours() <18){
                     Time = "오후";
                 }
                else{
                     Time = "저녁";
                 }
                Todo = mItem.getToDoText();
                Log.d("lololo",Todo);

                Thread thread = new Thread(new Runnable(){
                    volatile boolean running = true;
                    @Override
                    public void run() {
                        try {
                            if(running){
                                HttpClient httpclient = new DefaultHttpClient();
                                // specify the URL you want to post to
                                HttpPost httppost = new HttpPost("http://211.221.128.124:3000/");
                                Log.d("lalalalala", "http://211.221.128.124:3000/");
                                try {
                                    // create a list to store HTTP variables and their values
                                    Log.d("lalalalala", "add");
                                    List nameValuePairs = new ArrayList();
                                    // add an HTTP variable and value pair
                                    nameValuePairs.add(new BasicNameValuePair("time",Time));
                                    nameValuePairs.add(new BasicNameValuePair("date","겨울"));
                                    nameValuePairs.add(new BasicNameValuePair("message",Todo));
                                    nameValuePairs.add(new BasicNameValuePair("token",MainActivity.token));
                                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                                    // send the variable and value, in other words post, to the URL
                                    HttpResponse response = httpclient.execute(httppost);
                                    Log.d(" 현재상태", "뭐지");

                                } catch (ClientProtocolException e) {
                                    // process execption
                                } catch (IOException e) {
                                    // process execption
                                }
                                running = false;
                            } else{

                            }
                        } catch (Exception e) {
                            Log.e("ddd", e.getMessage());
                        }
                    }
                });
                thread.start();

                mToDoItems.remove(mItem);
                changeOccurred();
                saveData();
                closeApp();
                finish();
            }
        });


//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, snoozeOptionsArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_text_view, snoozeOptionsArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        mSnoozeSpinner.setAdapter(adapter);
//        mSnoozeSpinner.setSelection(0);

    }

    private void closeApp(){
        Intent i = new Intent(ReminderActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(EXIT, true);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(EXIT, true);
        editor.apply();
        startActivity(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }
    private void changeOccurred(){
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MainActivity.CHANGE_OCCURED, true);
        editor.commit();
        editor.apply();
    }

    private Date addTimeToDate(int mins){
        //app.send(this, "Action", "Snoozed", "For "+mins+" minutes");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, mins);
        return calendar.getTime();
    }
    private int valueFromSpinner(){
        switch (mSnoozeSpinner.getSelectedItemPosition()){
            case 0:
                return 10;
            case 1:
                return 30;
            case 2:
                return 60;
            default:
                return 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toDoReminderDoneMenuItem:
                Date date = addTimeToDate(valueFromSpinner());
                mItem.setToDoDate(date);
                mItem.setHasReminder(true);
                Log.d("OskarSchindler", "Date Changed to: " + date);
                changeOccurred();
                saveData();
                closeApp();
                //foo
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        try{
//            storeRetrieveData.saveToFile(mToDoItems);
//        }
//        catch (JSONException | IOException e){
//            e.printStackTrace();
//        }
//    }

    private void saveData(){
        try{
            storeRetrieveData.saveToFile(mToDoItems);
        }
        catch (JSONException | IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try{
//            storeRetrieveData.saveToFile(mToDoItems);
//        }
//        catch (JSONException | IOException e){
//            e.printStackTrace();
//        }
    }

}
