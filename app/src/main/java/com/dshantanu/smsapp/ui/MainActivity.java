package com.dshantanu.smsapp.ui;


import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.adapter.InboxAdapter;
import com.dshantanu.smsapp.adapter.InboxRVItemDecoration;
import com.dshantanu.smsapp.database.SMS;
import com.dshantanu.smsapp.database.SMSSQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private List<SMS> smsList = new ArrayList<>();
    private RecyclerView recyclerView;
    public InboxAdapter ibAdapter;
    private ProgressBar progressBar;
    private static Bundle mBundleRecyclerViewState;
    private final String KEY_RECYCLER_STATE = "recycler_state";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_inbox);
        recyclerView = (RecyclerView) findViewById(R.id.rv_inbox);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new InboxRVItemDecoration(this, LinearLayoutManager.VERTICAL));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent itNewMessage = new Intent(getApplicationContext(), ConversationActivity.class);
                startActivity(itNewMessage);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetSMSFromDB().execute();
        clearNotification();
    }

    private void clearNotification() {
        NotificationManager notifManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
    }


    @Override
    protected void onPause() {
        super.onPause();
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }


    class GetSMSFromDB extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SMSSQLiteHelper dbHelper = new SMSSQLiteHelper(getApplicationContext());
            smsList.clear();
            smsList = dbHelper.getInboxSMS();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ibAdapter = new InboxAdapter(smsList);
            recyclerView.setAdapter(ibAdapter);
            ibAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.INVISIBLE);
            if (mBundleRecyclerViewState != null) {
                Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                recyclerView.getLayoutManager().onRestoreInstanceState(listState);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_view).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                //TODO: handel search
                Toast.makeText(MainActivity.this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
        return true;
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
                        ibAdapter.notifyDataSetChanged();
                    }

                }
            }
        }
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(System.currentTimeMillis());
    }

}//end class
