package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pelotoncycle.communication.pelotoncycle.R;
import com.pelotoncycle.communication.pelotoncycle.utilities.NetworkingChecker;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private final static String NO_NETWORKING_CONNECTION = "No networking connection, please check.";
    private final static String SERVER_UNREACHABLE = "The server can't be reached, please check.";
    protected static final String STREAM_ONE_NAME = "com.pelotoncycle.communication.pelotoncycle.Controllers.MainActivity.streamonename";
    protected static final String STREAM_TWO_NAME = "com.pelotoncycle.communication.pelotoncycle.Controllers.MainActivity.streamtwoname";
    protected final static String PELOTON_API_SERVER = "https://api.pelotoncycle.com/quiz/next/";
    private long mBackPressed;
    private EditText txtStreamOneName;
    private EditText txtStreamTwoName;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        txtStreamOneName = (EditText) findViewById(R.id.streamOneNameEditTextID);
        txtStreamTwoName = (EditText) findViewById(R.id.streamTwoNameEditTextID);
        startButton = (Button) findViewById(R.id.start_btn);

        if (NetworkingChecker.isOnline(this)) {
            Handler handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {

                    if (msg.what != 1) { // code if not connected
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, SERVER_UNREACHABLE, Toast.LENGTH_SHORT).show();
                                startButton.setEnabled(false);
                            }
                        });
                    } else { // code if connected
                        startButton.setEnabled(true);
                    }
                }
            };
            NetworkingChecker.isNetworkAvailable(handler, 2000, PELOTON_API_SERVER + txtStreamOneName.getText().toString());
        } else {
            Toast.makeText(MainActivity.this, NO_NETWORKING_CONNECTION, Toast.LENGTH_SHORT).show();
            startButton.setEnabled(false);
        }
    }


    /**
     * handle the click action of "START" button
     *
     * @param v the button
     */
    public void startStream(final View v) {

        if (validateStreamNames()) {
            final Intent startStreamIntent = new Intent(this, StreamActivity.class);
            startStreamIntent.putExtra(STREAM_ONE_NAME, txtStreamOneName.getText().toString());
            startStreamIntent.putExtra(STREAM_TWO_NAME, txtStreamTwoName.getText().toString());
            startActivity(startStreamIntent);
        }
    }

    /**
     * check whether two streams are input correctly
     *
     * @return true if two streams are input, otherwise false
     */
    public boolean validateStreamNames() {


        String strStreamOneName = txtStreamOneName.getText().toString();


        String strStreamTwoName = txtStreamTwoName.getText().toString();

        if (TextUtils.isEmpty(strStreamOneName)) {
            txtStreamOneName.setError("Stream One's name can't be empty.");
            return false;
        }

        if (TextUtils.isEmpty(strStreamTwoName)) {
            txtStreamTwoName.setError("Stream Two's name can't be empty.");
            return false;
        }
        return true;
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
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press one more time to exit", Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}
