package com.dshantanu.smsapp.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.adapter.ConversationAdapter;
import com.dshantanu.smsapp.database.SMS;
import com.dshantanu.smsapp.database.SMSSQLiteHelper;
import com.dshantanu.smsapp.util.ContactUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    Intent extraIntent;
    int conversationID;

    private List<SMS> smsList = new ArrayList<>();
    private RecyclerView recyclerView;
    public ConversationAdapter conversationAdapter;
    private ProgressBar progressBar;
    Button btnSend, btnAddContact;
    LinearLayout llNewConversation;
    SmsManager smsManager;
    EditText etPhoneNumber, etMessage;
    SMSSQLiteHelper smsDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        extraIntent = getIntent();
        conversationID = extraIntent.getIntExtra(getString(R.string.intent_conversation_id), 0);
        setTitle(getString(R.string.new_conversation));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_conversation);
        recyclerView = (RecyclerView) findViewById(R.id.rv_conversation);
        llNewConversation = (LinearLayout) findViewById(R.id.ll_new_conversation);
        etPhoneNumber = (EditText) findViewById(R.id.et_conversation_phone);
        etMessage = (EditText) findViewById(R.id.et_conversation_message);
        btnAddContact = (Button) findViewById(R.id.btn_conversation_add_contact);
        btnSend = (Button) findViewById(R.id.btn_conversation_send);
        btnSend.setOnClickListener(this);
        btnAddContact.setOnClickListener(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        smsManager = SmsManager.getDefault();
        handlePermissions();
        smsDB = new SMSSQLiteHelper(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (conversationID != 0) {
            llNewConversation.setVisibility(View.INVISIBLE);
            new GetConversationFromDB().execute();
        }
        clearNotification();
    }
    private void clearNotification() {
        NotificationManager notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }


    public void handlePermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConversationActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    handlePermissions();
                }

            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_conversation_add_contact:
                //TODO: Adding contact from list
                //TODO: on getting contact set title and make type number layout invisible
                Toast.makeText(ConversationActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_conversation_send:
                boolean isNewConversetation = false;
                String phoneNumber;
                if (conversationID == 0) {
                    isNewConversetation = true;
                    conversationID = smsDB.getConversatationID(etPhoneNumber.getText().toString());
                    phoneNumber = etPhoneNumber.getText().toString();
                }else {
                    phoneNumber = smsDB.getAddress(conversationID);
                }


                if (phoneNumber != null && !phoneNumber.equals("") && phoneNumber.matches("[0-9\\+]+")) {
                    smsManager.sendTextMessage(phoneNumber, null, etMessage.getText().toString(), null, null);

                    SMS sms = new SMS(phoneNumber, etMessage.getText().toString(), 1, getCurrentTimeStamp(), conversationID, 2);
                    smsDB.addSMS(sms);
                    smsList.add(sms);

                    if (isNewConversetation) {
                        conversationAdapter = new ConversationAdapter(smsList);
                        recyclerView.setAdapter(conversationAdapter);
                        conversationAdapter.notifyDataSetChanged();
                        llNewConversation.setVisibility(View.INVISIBLE);
                        setTitle(ContactUtil.getContactName(getApplicationContext(), phoneNumber));
                        etMessage.setText("");
                    } else {
                        conversationAdapter.notifyDataSetChanged();
                        etMessage.setText("");
                    }

                    recyclerView.smoothScrollToPosition(smsList.size());
                    Toast.makeText(ConversationActivity.this, getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ConversationActivity.this, getString(R.string.enter_valid_number), Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }


    public String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(System.currentTimeMillis());
    }


    class GetConversationFromDB extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            smsList.clear();
            smsList = smsDB.getConversation(conversationID);
            smsDB.markSMSRead(conversationID);//marks all sms as read
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            conversationAdapter = new ConversationAdapter(smsList);
            recyclerView.setAdapter(conversationAdapter);
            conversationAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            recyclerView.smoothScrollToPosition(smsList.size());
            setConversationHeading();
        }
    }

    public void setConversationHeading() {
        String name = smsList.get(0).get_address();
        name = name.replace("+", "");
        if (name.matches("[0-9]+")) {
            name = ContactUtil.getContactName(getApplicationContext(), name);
            setTitle(name);
        } else {
            setTitle(name);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_delete_conversation:
                handelDeleteAction();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void handelDeleteAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirm_delete));
        builder.setMessage(getString(R.string.are_you_sure));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                smsDB.deleteConversation(conversationID);
                onBackPressed();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public class SMSBroadcastReceiver extends BroadcastReceiver {

        private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == SMS_RECEIVED) {
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
                        smsList.add(new SMS(messages[i].getOriginatingAddress(), messages[i].getMessageBody(), smsDB.getConversatationID(messages[i].getOriginatingAddress()), getCurrentTimeStamp()));
                        conversationAdapter.notifyDataSetChanged();
                    }

                }
            }
        }
    }


}//end class
