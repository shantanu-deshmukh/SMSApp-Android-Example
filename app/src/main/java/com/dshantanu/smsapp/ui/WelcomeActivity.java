package com.dshantanu.smsapp.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.dshantanu.smsapp.R;
import com.dshantanu.smsapp.database.SMSSQLiteHelper;
import com.dshantanu.smsapp.messaging.GetAllSMS;
import com.dshantanu.smsapp.database.SMS;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    int currentapiVersion;
    Button btnSetDefaultSmsApp;
    String myPackageName;
    ProgressBar progressBar;
    List<SMS> smsList;
    TextView tvWelcomeMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        currentapiVersion = android.os.Build.VERSION.SDK_INT;
        myPackageName = getPackageName();
        btnSetDefaultSmsApp = (Button) findViewById(R.id.btn_set_default_sms_app);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_welcome_activity);
        tvWelcomeMsg = (TextView) findViewById(R.id.tv_welcome_activity_msg);
        btnSetDefaultSmsApp.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        handlePermissions();
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void handelIsDefaultApp() {
        if (currentapiVersion >= Build.VERSION_CODES.KITKAT) {

            if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
                //this app is not default
                btnSetDefaultSmsApp.setVisibility(View.VISIBLE);
                tvWelcomeMsg.setText(getString(R.string.app_default_request));
            } else {
                //app is already default
                btnSetDefaultSmsApp.setVisibility(View.INVISIBLE);
                handleFirstRun(); //if this is first run it will load all sms which were received before install
            }

        }


    }

    public void handlePermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //don't have permission; ask
            ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, 1);
        } else {
            //already have permission
            handelIsDefaultApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //handelIsDefaultApp();
                } else {
                    handlePermissions();
                }

            }
        }
    }


    public void handleFirstRun() {
        boolean isFirstRun = sharedPref.getBoolean(getString(R.string.sharedpref_is_first_run), true);
        if (isFirstRun) {
            progressBar.setVisibility(View.VISIBLE);
            tvWelcomeMsg.setText(getString(R.string.app_importing_sms));
            btnSetDefaultSmsApp.setVisibility(View.INVISIBLE);
            //get all SMS and save to database
            smsList = GetAllSMS.getAllSMS(getApplicationContext());
            new ImportSMS().execute();

        } else {
            openMainActivity();
        }


    }


    class ImportSMS extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            SMSSQLiteHelper dbHelper = new SMSSQLiteHelper(getApplicationContext());
            //SAVING TO DATABASE
            for (SMS sms : smsList) {
                dbHelper.addSMS(sms);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            openMainActivity();
        }
    }


    private void openMainActivity() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.sharedpref_is_first_run), false);
        editor.commit();
        Intent itMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(itMainActivity);
        finish();
    }


}//end class
