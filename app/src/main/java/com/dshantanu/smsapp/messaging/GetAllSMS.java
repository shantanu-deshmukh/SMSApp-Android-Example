package com.dshantanu.smsapp.messaging;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.dshantanu.smsapp.database.SMS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class GetAllSMS {


    public static List<SMS> getAllSMS(Context context) {
        List<SMS> smsList = new ArrayList<>();
        SMS sms;
        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/"), null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                sms = new SMS();
                sms.set_id(cursor.getInt(cursor.getColumnIndex(SMS.COLUMN_ID)));
                sms.set_address(cursor.getString(cursor.getColumnIndex(SMS.COLUMN_ADDRESS)));
                sms.set_msg_body(cursor.getString(cursor.getColumnIndex(SMS.COLUMN_BODY)));
                sms.set_readState(cursor.getInt(cursor.getColumnIndex(SMS.COLUMN_READ_STATE)));
                sms.set_datetime(getDate(cursor.getLong(cursor.getColumnIndex(SMS.COLUMN_TIMESTAMP))));
                sms.set_threadID(cursor.getInt(cursor.getColumnIndex(SMS.COLUMN_THREAD_ID)));
                sms.set_type(cursor.getInt(cursor.getColumnIndex(SMS.COLUMN_TYPE)));
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        return smsList;
    }


    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }


}//end class
