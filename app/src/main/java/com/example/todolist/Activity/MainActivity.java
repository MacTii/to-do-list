package com.example.todolist.Activity;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.todolist.Adapter.ToDoAdapter;
import com.example.todolist.TaskOperations.AddNewTask;
import com.example.todolist.TaskOperations.DialogCloseListener;
import com.example.todolist.Model.ToDoModel;
import com.example.todolist.Notification.Notification;
import com.example.todolist.R;
import com.example.todolist.ItemTouchHelper.RecyclerItemTouchHelper;
import com.example.todolist.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DialogCloseListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private DatabaseHandler db;

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private SearchView searchView;

    private List<ToDoModel> taskList;

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceSettingsActivity.registerPref(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this);
        db.openDatabase();

        createNotificationChannel();

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        fab = findViewById(R.id.fab);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterListSearch(newText);
                return true;
            }
        });

        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(v -> {
            AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("task_option_preference")) {
            if(sharedPreferences.getBoolean("task_option_preference", false)) {
                filterListHideItems(taskList);
            }
            else {
                tasksAdapter.setTasks(taskList);
            }
        } if(key.equals("category_preference")) {
            String category = sharedPreferences.getString("category_preference", "");
            if(!category.equals("Other")) {
                filterListCategory(taskList, category);
            } else {
                tasksAdapter.setTasks(taskList);
            }
        }
    }

    public void createNotificationChannel() {
        CharSequence name = "testChannel";
        //String description = "des";

        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel("channelID", name, importance);
        //channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void setAlarm(ToDoModel task) {
        Intent intent = new Intent(MainActivity.this, Notification.class);

        // Set title and message of notification
        intent.putExtra("titleExtra", task.getTaskTitle());
        intent.putExtra("messageExtra", task.getTask());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        String date = task.getDate();
        String[] dateSplit = date.split("\\.");
        int yearEnd = Integer.parseInt(dateSplit[2]);
        int monthEnd = Integer.parseInt(dateSplit[1]);
        int dayEnd = Integer.parseInt(dateSplit[0]);

        System.out.println(yearEnd + " " + monthEnd + " " + dayEnd);

        String time = task.getTime();
        String[] timeSplit = time.split(":");
        int hourEnd = Integer.parseInt(timeSplit[0]);
        int minuteEnd = Integer.parseInt(timeSplit[1]);

        LocalDateTime dateTimeEnd = LocalDateTime.of(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd);
        long dateEnd = toLong(dateTimeEnd);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPreference = sharedPref.getString("notification_preference", "5 min");
        String[] notificationPreferenceSplit = notificationPreference.split("\\s");
        int notificationPreferenceSplitTime = Integer.parseInt(notificationPreferenceSplit[0]);

        long triggerAtMillis = dateEnd * 1000L - notificationPreferenceSplitTime * 60L * 1000L;
        System.out.println(triggerAtMillis);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    public static long toLong(LocalDateTime date) {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        return zdt.toInstant().getEpochSecond();
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    private void filterListHideItems(List<ToDoModel> taskList) {
        List<ToDoModel> filteredList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (!toBoolean(taskList.get(i).getStatus())) {
                System.out.println("index = " + i);
                filteredList.add(taskList.get(i));
            }
        }
        if(!filteredList.isEmpty()) {
            tasksAdapter.setFilteredList(filteredList);
        }
    }

    private void filterListCategory(List<ToDoModel> taskList, String category) {
        List<ToDoModel> filteredList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getCategory().equals(category)) {
                filteredList.add(taskList.get(i));
            }
        }
        if(!filteredList.isEmpty()) {
            tasksAdapter.setFilteredList(filteredList);
        } else {
            Toast.makeText(this, "No specific category", Toast.LENGTH_SHORT).show();
            tasksAdapter.setFilteredList(taskList);
        }
    }

    private void filterListSearch(String text) {
        List<ToDoModel> filteredList = new ArrayList<>();
        for (ToDoModel item : taskList) {
            if (item.getTaskTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
        } else {
            tasksAdapter.setFilteredList(filteredList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, PreferenceSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}