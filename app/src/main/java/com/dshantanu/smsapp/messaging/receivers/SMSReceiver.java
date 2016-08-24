package com.dshantanu.smsapp.messaging.receivers;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;

import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.database.SMS;
import com.dshantanu.smsapp.database.SMSSQLiteHelper;
import com.dshantanu.smsapp.ui.MainActivity;
import com.dshantanu.smsapp.util.ContactUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages;

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");

            messages = new SmsMessage[pdus.length];

            SMSSQLiteHelper smsDB = new SMSSQLiteHelper(context);

            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);//deprecated method is handled in if...else
                }

                smsDB.addSMS(new SMS(messages[i].getOriginatingAddress(), messages[i].getMessageBody(), smsDB.getConversatationID(messages[i].getOriginatingAddress()), getCurrentTimeStamp()));
                showNotification(context, messages[i].getOriginatingAddress(), messages[i].getMessageBody());

            }

        }
    }


    void showNotification(Context context, String address, String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notification_sms)
                        .setContentTitle(ContactUtil.getContactName(context, address))
                        .setContentText(message);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.notificationID, mBuilder.build());
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(System.currentTimeMillis());
    }

}//end class
