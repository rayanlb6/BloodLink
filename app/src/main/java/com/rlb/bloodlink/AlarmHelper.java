package com.rlb.bloodlink;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmHelper {

    public static void scheduleRepeatingTask(Context context){
        Intent intent = new Intent(context, MyReceiver.class);
        PendingIntent pendingIntent =PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = 5*60*1000;
        long startTime= System.currentTimeMillis() + interval;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,startTime,interval,pendingIntent);



    }
}
