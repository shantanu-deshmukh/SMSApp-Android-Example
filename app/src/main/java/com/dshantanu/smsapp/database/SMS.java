package com.dshantanu.smsapp.database;

import com.dshantanu.smsapp.util.Constants;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class SMS {

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ADDRESS = "address"; //number/Name of other person
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_READ_STATE = "seen";  //0 if read, 1 if unread
    public static final String COLUMN_TIMESTAMP = "date"; //timestamp in SQL format
    public static final String COLUMN_THREAD_ID = "thread_id"; //conversation ID
    public static final String COLUMN_TYPE = "type"; //1 if msg received, 2 if msg sent


    private int _id;
    private String _address;
    private String _msg_body;
    private int _readState;
    private String _datetime;
    private int _threadID;
    private int _type;

    //constructor for recieved sms
    public SMS(String _address, String _msg_body, int _threadID, String _datetime) {
        this._readState = Constants.SMS_READ_STATE_UNREAD;
        this._datetime = _datetime;
        this._threadID = _threadID;
        this._type = Constants.SMS_TYPE_RECIEVED;
        this._address = _address;
        this._msg_body = _msg_body;
    }


    public SMS() {

    }

    public SMS(String _address, String _msg_body, int _readState, String _datetime, int _threadID, int _type) {
        this._address = _address;
        this._msg_body = _msg_body;
        this._readState = _readState;
        this._datetime = _datetime;
        this._threadID = _threadID;
        this._type = _type;
    }


    public SMS(int _id, String _address, String _msg_body, int _readState, String _datetime, int _threadID, int _type) {
        this._id = _id;
        this._address = _address;
        this._msg_body = _msg_body;
        this._readState = _readState;
        this._datetime = _datetime;
        this._threadID = _threadID;
        this._type = _type;
    }

    public String get_datetime() {
        return _datetime;
    }

    public void set_datetime(String _datetime) {
        this._datetime = _datetime;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public String get_msg_body() {
        return _msg_body;
    }

    public void set_msg_body(String _msg_body) {
        this._msg_body = _msg_body;
    }

    public int get_readState() {
        return _readState;
    }

    public void set_readState(int _readState) {
        this._readState = _readState;
    }


    public int get_threadID() {
        return _threadID;
    }

    public void set_threadID(int _threadID) {
        this._threadID = _threadID;
    }


    public int get_type() {
        return _type;
    }

    public void set_type(int _type) {
        this._type = _type;
    }


    @Override
    public String toString() {
        return "Sms{" +
                "_id='" + _id + '\'' +
                ", threadId='" + _threadID + '\'' +
                ", address='" + _address + '\'' +
                ", body='" + _msg_body + '\'' +
                ", timestamp='" + _datetime + '\'' +
                ", read='" + _readState + '\'' +
                ", type='" + _type + '\'' +
                '}';
    }

}//end class
