package com.dshantanu.smsapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract;
import android.util.Log;

import com.dshantanu.smsapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class SMSSQLiteHelper extends SQLiteOpenHelper {

    final String TAG = "SQLiteHelper";
    public static final String TABLE_SMS = "SMS";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_READ_STATE = "readState";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_THREAD_ID = "threadID";
    public static final String COLUMN_TYPE = "type";

    private static final String DATABASE_NAME = "sms.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_SMS + "( " + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ADDRESS + " text, "
            + COLUMN_BODY + " text not null, "
            + COLUMN_READ_STATE + " integer not null, "
            + COLUMN_DATETIME + " DATETIME not null, "
            + COLUMN_THREAD_ID + " integer not null, "
            + COLUMN_TYPE + " integer not null);";


    public SMSSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SMSSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }


    public void addSMS(String address, String body, int readState, String timeStamp, int threadID, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ADDRESS, address);
        contentValues.put(COLUMN_BODY, body);
        contentValues.put(COLUMN_READ_STATE, readState);
        contentValues.put(COLUMN_DATETIME, timeStamp);
        contentValues.put(COLUMN_THREAD_ID, threadID);
        contentValues.put(COLUMN_TYPE, type);
        db.insert(TABLE_SMS, null, contentValues);
        db.close();
    }




    public void addSMS(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ADDRESS, sms.get_address());
        contentValues.put(COLUMN_BODY, sms.get_msg_body());
        contentValues.put(COLUMN_READ_STATE, sms.get_readState());
        contentValues.put(COLUMN_DATETIME, sms.get_datetime());
        contentValues.put(COLUMN_THREAD_ID, sms.get_threadID());
        contentValues.put(COLUMN_TYPE, sms.get_type());
        db.insert(TABLE_SMS, null, contentValues);
        db.close();
//        Log.v(TAG, "INSERTED: " + sms.toString());


    }


    public SMS getSMS(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_SMS + " where " + COLUMN_ID + "=" + id + "", null);
        if (cursor != null)
            cursor.moveToFirst();
        SMS sms = new SMS(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), Integer.parseInt(cursor.getString(3)), cursor.getString(4), Integer.parseInt(cursor.getString(5)), Integer.parseInt(cursor.getString(6)));
        cursor.close();
        return sms;
    }


    public String getAddress(int threadID) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SMS, new String[]{"" + COLUMN_ADDRESS + " AS ADDR"}, COLUMN_THREAD_ID + " = ?", new String[]{"" + threadID + ""}, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("ADDR");
        String address;
        try {
            address = cursor.getString(index);
        } catch (Exception e) {
            address = null;
        }
        return address;
    }

    public int getConversatationID(String address) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SMS, new String[]{"" + COLUMN_THREAD_ID + " AS ID"}, COLUMN_ADDRESS + " = ?", new String[]{"" + address + ""}, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("ID");
        int id;
        try {
            id = cursor.getInt(index);
        } catch (Exception e) {
            id = getNewConversatationID();//generated new conversation ID if it doesn't exists
        }
        return id;
    }


    //generated a new thread ID
    private int getNewConversatationID() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SMS, new String[]{"MAX(" + COLUMN_THREAD_ID + ") AS MAX"}, null, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex("MAX");
        int id = cursor.getInt(index);
        return id + 1;
    }


    public List<SMS> getAllSMS() {
        List<SMS> smsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SMS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.set_id(Integer.parseInt(cursor.getString(0)));
                sms.set_address(cursor.getString(1));
                sms.set_msg_body(cursor.getString(2));
                sms.set_readState(Integer.parseInt(cursor.getString(3)));
                sms.set_datetime(cursor.getString(4));
                sms.set_threadID(Integer.parseInt(cursor.getString(5)));
                sms.set_type(Integer.parseInt(cursor.getString(6)));
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return smsList;
    }

    public List<SMS> getInboxSMS() {
        List<SMS> smsList = new ArrayList<>();
        String selectQuery = "SELECT  " + COLUMN_ID + ", " + COLUMN_ADDRESS + ", " + COLUMN_BODY + ", " + COLUMN_READ_STATE + ", MAX(" + COLUMN_DATETIME + "), " + COLUMN_THREAD_ID + ", " + COLUMN_TYPE + " FROM " + TABLE_SMS + " GROUP BY " + COLUMN_THREAD_ID + " ORDER BY " + COLUMN_DATETIME + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.set_id(Integer.parseInt(cursor.getString(0)));
                sms.set_address(cursor.getString(1));
                sms.set_msg_body(cursor.getString(2));
                sms.set_readState(Integer.parseInt(cursor.getString(3)));
                sms.set_datetime(cursor.getString(4));
                sms.set_threadID(Integer.parseInt(cursor.getString(5)));
                sms.set_type(Integer.parseInt(cursor.getString(6)));
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return smsList;
    }

    public List<SMS> getConversation(int threadID) {
        List<SMS> smsList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SMS + " WHERE " + COLUMN_THREAD_ID + " = " + threadID + " ORDER BY " + COLUMN_DATETIME + " ASC ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                SMS sms = new SMS();
                sms.set_id(Integer.parseInt(cursor.getString(0)));
                sms.set_address(cursor.getString(1));
                sms.set_msg_body(cursor.getString(2));
                sms.set_readState(Integer.parseInt(cursor.getString(3)));
                sms.set_datetime(cursor.getString(4));
                sms.set_threadID(Integer.parseInt(cursor.getString(5)));
                sms.set_type(Integer.parseInt(cursor.getString(6)));
                smsList.add(sms);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return smsList;
    }



    public void markSMSRead(int threadID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_READ_STATE, Constants.SMS_READ_STATE_READ);
        db.update(TABLE_SMS, values, COLUMN_THREAD_ID + " = ?",
                new String[]{String.valueOf(threadID)});
    }


    public int updateSMS(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, sms.get_id());
        values.put(COLUMN_ADDRESS, sms.get_address());
        values.put(COLUMN_BODY, sms.get_msg_body());
        values.put(COLUMN_READ_STATE, sms.get_readState());
        values.put(COLUMN_DATETIME, sms.get_datetime());
        values.put(COLUMN_THREAD_ID, sms.get_threadID());
        values.put(COLUMN_TYPE, sms.get_type());
        return db.update(TABLE_SMS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(sms.get_id())});
    }

    public void deleteContact(SMS sms) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(sms.get_id())});
        db.close();
    }

    public void deleteConversation(int threadID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMS, COLUMN_THREAD_ID + " = ?",
                new String[]{String.valueOf(threadID)});
        db.close();
    }


    public int getSmsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

}//end class