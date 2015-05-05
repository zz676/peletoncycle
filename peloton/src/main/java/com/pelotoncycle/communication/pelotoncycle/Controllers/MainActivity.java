package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pelotoncycle.communication.pelotoncycle.R;
import com.pelotoncycle.communication.pelotoncycle.utilities.NetworkingChecker;

/**
 *
 *  The screen for users to input stream names.
 *  A {@link BroadcastReceiver} is registered to monitor the status of networking connection,
 *  if there is no networking connections, the START button will be disabled.
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    protected final static String PELOTON_API_SERVER = "https://api.pelotoncycle.com/quiz/next/";
    protected static final String STREAM_ONE_NAME = "com.pelotoncycle.communication.pelotoncycle.Controllers.MainActivity.streamonename";
    protected static final String STREAM_TWO_NAME = "com.pelotoncycle.communication.pelotoncycle.Controllers.MainActivity.streamtwoname";
    protected static final String PREFS_NAMES = "com.pelotoncycle.communication.pelotoncycle.Controllers.MainActivity.preferences";
    private long mBackPressed;
    private TextView instructionTxtView;
    private EditText txtStreamOneName;
    private EditText txtStreamTwoName;
    private Button startButton;
    private SharedPreferences sharedPreferences;
    private boolean isInstructionShown = false;

    /**
     * the BroadcastReceiver used to monitor the status of networking connection on the device
     */
    private BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAMES, MODE_PRIVATE);
        initView();
    }

    /**
     * initialize views
     */
    public void initView() {

        txtStreamOneName = (EditText) findViewById(R.id.streamOneNameEditTextID);
        txtStreamOneName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //If name of Stream One is changed, clean up all preferences if there is any.
                cleanPreferences();
            }
        });
        txtStreamTwoName = (EditText) findViewById(R.id.streamTwoNameEditTextID);
        txtStreamTwoName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //If name of Stream One is changed, clean up all preferences if there is any.
                cleanPreferences();
            }
        });

        startButton = (Button) findViewById(R.id.start_btn);
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // check
                handleNetWorkingStatusChange();
            }
        };
        instructionTxtView = (TextView) findViewById(R.id.instruction_text_view);
    }

    /**
     * show/hide instructions
     * @param v the button
     */
    public void showInstruction(final View v){
        if(isInstructionShown){
            instructionTxtView.setText("");
            isInstructionShown = false;
        } else {
            instructionTxtView.setText(getResources().getString(R.string.instruction_string));
            isInstructionShown = true;
        }
    }

    /**
     * handle the status changes of views when network connection status is changed
     */
    public void handleNetWorkingStatusChange() {

        try {
            if (NetworkingChecker.isOnline(this)) {
                startButton.setEnabled(true);
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.no_networking_connection), Toast.LENGTH_SHORT).show();
                startButton.setEnabled(false);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in handleNetWorkingStatusChange.");
        }
    }

    /**
     * clean up preferences
     */
    private void cleanPreferences() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
        }
    }

    /**
     * handle the click action of "START" button
     *
     * @param v the button
     */
    public void startStream(final View v) {

        try {
            if (validateStreamNames()) {
                final Intent startStreamIntent = new Intent(this, StreamActivity.class);
                startStreamIntent.putExtra(STREAM_ONE_NAME, txtStreamOneName.getText().toString());
                startStreamIntent.putExtra(STREAM_TWO_NAME, txtStreamTwoName.getText().toString());
                startActivity(startStreamIntent);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in startStream.");
        }
    }

    /**
     * check whether two streams are input correctly
     *
     * @return true if two streams are input, otherwise false
     */
    public boolean validateStreamNames() {
        try {
            String strStreamOneName = txtStreamOneName.getText().toString();
            String strStreamTwoName = txtStreamTwoName.getText().toString();

            if (TextUtils.isEmpty(strStreamOneName)) {
                txtStreamOneName.setError("This field can't be empty.");
                return false;
            }

            if (TextUtils.isEmpty(strStreamTwoName)) {
                txtStreamTwoName.setError("This field can't be empty.");
                return false;
            }

            if(strStreamOneName.equals(strStreamTwoName)){

                txtStreamTwoName.setError("Two streams couldn't have same names.");
                return false;
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in validateStreamNames.");
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clear up all the preferences while exiting the app
        cleanPreferences();
    }

    @Override
    public void onBackPressed() {

        //If the time between two presses is not longer than 2 seconds (TIME_INTERVAL),
        //the application will exit.
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press one more time to exit", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}