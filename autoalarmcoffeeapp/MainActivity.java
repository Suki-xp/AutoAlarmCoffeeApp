//For this assignment I am going to be writing the MainAcivity file that interacts with the
//simple_ui_design code to create the app we want to see

package com.example.autoalarmcoffeeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.AlarmManager;
import android.content.Context;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.PendingIntent;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    //Need to rewrite the text view so that it can be updated in real time
    //and the alarm variables
    private TextView first_Alarm_Text;
    private TimePicker settingAlarm;
    private TextView storing_time;
    private Button startCoffee;
    private Button endCoffee;
    private Calendar create_calendar;
    private AlarmManager passingAlarm;
    private PendingIntent casted_Intent;
    private ProgressBar track_progress;
    private HashMap<String, String> timeList;
    private Calendar timeTrack;
    private boolean isBrewing = false;
    //To check if the status of the coffee is
    //brewing or not to cancel the tracking progress

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_ui_design);
        //Using HashMap declaration to store the data of the alarms added
        //and Calander.getInstance reference call
        timeList = new HashMap<>();
        timeTrack = Calendar.getInstance();
        storing_time = findViewById(R.id.store_time);

        //We want to set the declaration to access the elements of the simple_Ui_Design xml
        TextView gettingText = findViewById(R.id.create_text);
        TextView updatingText = findViewById(R.id.update_text);
        TextView updatingText2 = findViewById(R.id.update_text_2);

        Button startApp = findViewById(R.id.create_button);
        startCoffee = findViewById(R.id.start_button);
        endCoffee = findViewById(R.id.end_button);

        first_Alarm_Text = findViewById(R.id.alarm_text);
        settingAlarm = findViewById(R.id.create_alarm);

        passingAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        casted_Intent = createPendingIntent(this);
        create_calendar = Calendar.getInstance();
        track_progress = findViewById(R.id.live_updates);

        //Now we need to extract the specific hours and minutes and then parse them
        //through the inputs text to update the time IRL
        //This gets the direct access where the values can be updated and changed
        settingAlarm.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener()
        {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateTime("Alarm 1", hourOfDay, minute, view);
                setFutureTime(hourOfDay, minute, view);
            }
        });

        //Sets the alarm(s) gone from the display to replay after the button click
        //same with the start and stop buttons of the alarm, and other parts of the app
        settingAlarm.setVisibility(View.GONE);
        updatingText.setVisibility(View.GONE);
        updatingText2.setVisibility(View.GONE);
        startCoffee.setVisibility(View.GONE);
        endCoffee.setVisibility(View.GONE);
        track_progress.setVisibility(View.GONE);
        storing_time.setVisibility(View.GONE);

        // Request runtime permission for exact alarms if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.SCHEDULE_EXACT_ALARM}, 1);
            }
        }

        if (gettingText != null && startApp != null)
        {
            // Set a click listener for the button to update the status
            startApp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    gettingText.setText("Instant Warm Cup Magic ☕");
                    startApp.setVisibility(View.GONE);
                    settingAlarm.setVisibility(View.VISIBLE);
                    startCoffee.setVisibility(View.VISIBLE);
                    endCoffee.setVisibility(View.VISIBLE);

                }
            });
        }

        //For the button display when start coffee and end coffee are hit on the screen
        startCoffee.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v)
            {
                updatingText2.setVisibility(View.GONE);
                updatingText.setVisibility(View.VISIBLE);
                if (create_calendar != null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || passingAlarm.canScheduleExactAlarms())
                    {
                        if(passingAlarm.canScheduleExactAlarms())
                        {
                            //Gets a reference to the Pending Intent to create
                            //and coverts the time into miliseconds which can be parsed into log
                            //which links the time to the alarm setup, by first getting the reference parsed through the method
                            setExactAlarm(passingAlarm, create_calendar.getTimeInMillis(), casted_Intent);
                            //Gets the reference to the specific time in hh and minutes to match the format before calling the server
                            int hour = settingAlarm.getHour();
                            int minutes = settingAlarm.getMinute();
                            String alarmBegin = String.format("%02d:%02d", hour, minutes);
                            Log.d("MainActivity", "Sending alarm time: " + alarmBegin);
                            try {
                                connectingToServer(alarmBegin);
                            } catch (MalformedURLException e) {
                                Log.e("MainActivity", "URL error: " + e.getMessage());
                            }

                            long currentTimeStamp = System.currentTimeMillis();
                            //Generate an Hashmap to hold the common set alarms used for easier user functionality
                            //and either saves of gets rid of them depending on the buttons pressed
                            // Unique key with timestamp
                            String timeKey = "Time saved at " + System.currentTimeMillis();
                            timeList.put("Time saved at", timeTrack.get(Calendar.HOUR_OF_DAY) + ":" + timeTrack.get(Calendar.MINUTE));
                            timeList.put("Status" + currentTimeStamp, "Begin");
                            loggingTimes(timeList);
                        }
                    }

                    //Creates the loop to run the tracking of the program
                    isBrewing = true;
                    final int total_time = 300;
                    final int counter = 100 / total_time; //Increments progress every 1%
                    track_progress.setVisibility(View.VISIBLE);

                    new Thread(new Runnable()
                    {
                        private int current_time = 0;
                        @Override
                        public void run() {
                            while (current_time < 100 && isBrewing)
                            {
                                current_time += counter;
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        track_progress.setProgress(current_time);
                                    }
                                    });
                                    try
                                    {
                                        Thread.sleep(1000);
                                    }
                                    catch (InterruptedException e)
                                    {
                                        Thread.currentThread().interrupt();
                                        //This is the exception that will cancel the progress
                                        //tracking when the button is pressed
                                    }
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if(!isBrewing)
                                    {
                                        track_progress.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }).start();
                    //Creates the reference to the notification call class
                    Intent notification_intent = new Intent(MainActivity.this, TrackingProgress.class);
                    notification_intent.putExtra("total_time", total_time);
                    startService(notification_intent); //Starts the call to reference the live display notification
                }
                else {
                    Log.e("MainActivity", "create_calendar is null, cannot set alarm");
                    updatingText.setText("Error restart the app ");
                    updatingText.setVisibility(View.GONE);
                    updatingText2.setVisibility(View.GONE);
                }
            }
        });

        endCoffee.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    Log.d("MainActivity", "endCoffee clicked at " + LocalTime.now());
                }
                updatingText.setVisibility(View.GONE);
                updatingText2.setVisibility(View.VISIBLE);
                cancelingServer();
                cancelingAlarms(passingAlarm, casted_Intent);
                // Unique key with timestamp
                long cancelTimes = System.currentTimeMillis();
                timeList.put("Time unsaved at", + timeTrack.get(Calendar.HOUR_OF_DAY) + ":" + timeTrack.get(Calendar.MINUTE));
                isBrewing = false;
                track_progress.setVisibility(View.GONE);
                loggingTimes(timeList);
            }
           });
    }

    //Method to display the logged times that the user added
    private void loggingTimes(HashMap<String, String> convertList)
    {
        StringBuilder displayString = new StringBuilder();
        if (!convertList.isEmpty())
        {
            for (Map.Entry<String, String> display : convertList.entrySet())
            {
                displayString.append(display.getKey()).append(": ").append(display.getValue()).append("\n");
                //Custom formatting for better readability
            }
            String storage = displayString.toString();
            storing_time.setText(storage);
            storing_time.setVisibility(View.VISIBLE);
        }
        else
        {
            displayString.append("Cant log any events");
            storing_time.setVisibility(View.GONE);
        }
    }

    //This is method that will update the string which in term is the referenced
    //passed back to the alarm alarm which shows the UI update
    //we are turning the view off bc they can already see the time updating
    private void updateTime(String input, int hr, int min, TimePicker view)
    {
        //A check that addresses the correct alarm to change
        //and whether or not the input was changed to a null value
        if (view == settingAlarm && first_Alarm_Text != null)
        {
            first_Alarm_Text.setText(input + ": " + hr + ":" + min);
        }
        first_Alarm_Text.setVisibility(View.GONE);
    }

    //Creates a calendar object for any of the time retrieved from the alarm clock
    //and sets up a scheduling affect
    //Additionally reference to a more exact time as well
    public void setFutureTime(int hrs, int mins, TimePicker view)
    {
        create_calendar = Calendar.getInstance(); //Calender object instance to compare the times
        long current_time = Calendar.getInstance().getTimeInMillis();

        //References an instance of the calendar for conversion
        create_calendar.set(Calendar.HOUR_OF_DAY, hrs);
        create_calendar.set(Calendar.MINUTE, mins);
        create_calendar.set(Calendar.SECOND, 0);

        //A check to make sure the day has or hasn't passed and move the alarm forward or not
        if (current_time > create_calendar.getTimeInMillis())
        {
            create_calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

    }

    //This method creates an instance of the alarm manager class
    //allow for the ability to schedule the alarm and display it on screen
    public void setExactAlarm(AlarmManager passingAlarm, long triggerTimes, PendingIntent casted_Intent)
    {
        passingAlarm.setExact(AlarmManager.RTC_WAKEUP, triggerTimes, casted_Intent);
    }

    //Wraps the intent object(idk ripped this off lol it supposed to be a wrapper for
    //the alarm manager I am pretty sure or something)
    public PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent("com.example.GENERIC_ACTION");
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    //Method to cancel the alarm set uo
    public void cancelingAlarms(AlarmManager passingAlarm, PendingIntent casted_Intent)
    {
        passingAlarm.cancel(casted_Intent);
    }

    //This is method that links the front end of this app to the python back end that
    //we wrote using visual studio code
    private void connectingToServer(String alarmTime) throws MalformedURLException {
        String url = "http://127.0.0.1:5000/trigger";
        String jsonInput = "{\"action\": \"turn_on\", \"alarm_time\": \"" + alarmTime + "\"}";
        //This links the kasa plug to
        //to the server call like how we did when we were parsing the command of the json
        //and loading it through the url setup
        try
        {
            java.net.URL urlCreate = new java.net.URL(url);
            HttpURLConnection httpCreate = (HttpURLConnection) urlCreate.openConnection();
            //Create a new URL object that command will parse through
            //and the http connection request as well
            httpCreate.setRequestMethod("POST");
            httpCreate.setRequestProperty("Content-Type", "application/json; utf-8");
            httpCreate.setDoOutput(true); //Like the load requests from the python its doing the
            //same thing by acessing the parse data from the server so it can run the plug command
            try (java.io.OutputStream outputs = httpCreate.getOutputStream())
            {
                byte[] inputs = jsonInput.getBytes("utf-8");
                outputs.write(inputs, 0, inputs.length);
            }
            int responseVerification = httpCreate.getResponseCode(); //This is like the response code
            //200 or other value that indicates it was passed through
            if (responseVerification == HttpURLConnection.HTTP_OK)
            {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(httpCreate.getInputStream(), "utf-8"));
                StringBuilder vaildate = new StringBuilder();
                String trackingLines;
                while ((trackingLines = reader.readLine()) != null)
                {
                    vaildate.append(trackingLines.trim());
                }
            }
            else
            {
                Log.e("MainActivity", "cant be parsed due to " + responseVerification);
            }
            httpCreate.disconnect(); //Ends the connection
        }
        catch (Exception e)
        {
            Log.e("MainActivity", "Server error due to " + e.getMessage());
        }
    }
    //Exact same method but for canceling the server
    private void cancelingServer()
    {
        String url = "http://127.0.0.1:5000/cancel";
        String jsonInput = "{\"action\": \"cancel\"}";
        try
        {
            java.net.URL urlCreate = new java.net.URL(url);
            HttpURLConnection httpCreate = (HttpURLConnection) urlCreate.openConnection();
            //Create a new URL object that command will parse through
            //and the http connection request as well
            httpCreate.setRequestMethod("POST");
            httpCreate.setRequestProperty("Content-Type", "application/json; utf-8");
            httpCreate.setDoOutput(true);

            try (java.io.OutputStream outputs = httpCreate.getOutputStream())
            {
                byte[] inputs = jsonInput.getBytes("utf-8");
                outputs.write(inputs, 0, inputs.length);
            }
            int responseVerification = httpCreate.getResponseCode(); //This is like the response code
            //200 or other value that indicates it was passed through
            if (responseVerification == HttpURLConnection.HTTP_OK)
            {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(httpCreate.getInputStream(), "utf-8"));
                StringBuilder vaildate = new StringBuilder();
                String trackingLines;
                while ((trackingLines = reader.readLine()) != null)
                {
                    vaildate.append(trackingLines.trim());
                }
            }
            else
            {
                Log.e("MainActivity", "cant be parsed due to " + responseVerification);
            }
            httpCreate.disconnect(); //Ends the connection
        }
        catch (Exception e)
        {
            Log.e("MainActivity", "Server error due to " + e.getMessage());
        }
    }
}
