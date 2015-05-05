package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pelotoncycle.communication.pelotoncycle.R;
import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;
import com.pelotoncycle.communication.pelotoncycle.utilities.NetworkingChecker;
import com.pelotoncycle.communication.pelotoncycle.utilities.StreamDataTask;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;


/**
 * Activity for users to stream numbers
 * Thoughts about streaming numbers:
 * <li> Get the first {@link DataPiece} from two streams, and add them into two queues separately. This will be done sequentially.</li>
 * <li> Pop up the first number in both queue to compare, and show the smaller one on the screen. Remember to add the larger one back to its queue.</li>
 * In this algorithm, only one of the two queues will become empty at each time. Before popping up numbers from two queues,
 * the sizes of them will be checked, if one of them is empty, {@link StreamDataTask} will be started to get the next {@link DataPiece} from the right stream.
 * <p/>
 * The reason I choose {@link android.os.AsyncTask} over {@link android.os.HandlerThread} is  that there are at most two tasks and they are short-lived.</li>
 * <p/>
 * The status of StreamActivity will be kept before it is destroyed, like the screen is rotated, or the user press the BACK button.
 * But there is one exception: Whenever either of two streams's names is changed, data in {@link SharedPreferences} will be cleared.
 *
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class StreamActivity extends Activity {

    private final static String TAG = "StreamActivity";
    protected final static String CURRENT_NUMBER = "com.pelotoncycle.communication.pelotoncycle.Controllers.StreamActivity.currentNumber";
    protected final static String ALL_NUMBERS = "com.pelotoncycle.communication.pelotoncycle.Controllers.StreamActivity.allNumbers";
    protected final static String IS_FIRST_RUN = "com.pelotoncycle.communication.pelotoncycle.Controllers.StreamActivity.isFirstRun";
    protected final static String STREAM_ONE_SET = "com.pelotoncycle.communication.pelotoncycle.Controllers.StreamActivity.streamOneSet";
    protected final static String STREAM_TWO_SET = "com.pelotoncycle.communication.pelotoncycle.Controllers.StreamActivity.streamTwoSet";

    private String streamOneName;
    private String streamTwoName;
    private TextView currentNumberTextView;
    private TextView allNumsTextView;
    private Button nextNumberBtn;
    private ScrollView previousNumsScrollView;
    private SharedPreferences sharedPreferences;
    private boolean isFirstRun = true;

    // Two sets used to store numbers in the queues.
    // In order to store all the numbers of two queues in sharedPreferences,
    // two sets are needed since sets can be put into sharedPreferences, but Queues can't be.
    private Set<String> streamOneSet = new HashSet<>();
    private Set<String> streamTwoSet = new HashSet<>();

    /**
     * the BroadcastReceiver used to monitor the status of networking connection on the device
     */
    private BroadcastReceiver networkReceiver;
    /**
     * a priority queue used to store numbers from stream one
     * there will be two numbers at most each time (last and current from the same {@link DataPiece})
     */
    private PriorityQueue<Integer> streamOneQueue = new PriorityQueue<>();
    /**
     * a priority queue to store numbers from stream two
     * there will be two numbers at most each time (last and current from the same {@link DataPiece})
     */
    private PriorityQueue<Integer> streamTwoQueue = new PriorityQueue<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        // Get two stream names
        if (getIntent() != null) {
            final Bundle extras = getIntent().getExtras();
            if (extras != null) {
                streamOneName = extras.getString(MainActivity.STREAM_ONE_NAME);
                streamTwoName = extras.getString(MainActivity.STREAM_TWO_NAME);
            }
        }
        init();
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //check whether two streams could be remotely access
                checkServerAvailability(MainActivity.PELOTON_API_SERVER + streamOneName);
                checkServerAvailability(MainActivity.PELOTON_API_SERVER + streamTwoName);
            }
        };
    }

    /**
     * Initilize views status or restore the previous status
     */
    private void init() {

        currentNumberTextView = (TextView) findViewById(R.id.number_textview);
        allNumsTextView = (TextView) findViewById(R.id.previous_numbers_textview);
        nextNumberBtn = (Button) findViewById(R.id.next_num_btn);
        previousNumsScrollView = (ScrollView) findViewById(R.id.previous_numbers_scrollview);


        //Retrieve the status if the user rotate the screen or come back after pressing BACK button
        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAMES, MODE_PRIVATE);
        if (sharedPreferences != null && sharedPreferences.getAll().size() != 0) {
            isFirstRun = sharedPreferences.getBoolean(IS_FIRST_RUN, false);
            currentNumberTextView.setText(sharedPreferences.getString(CURRENT_NUMBER, ""));
            allNumsTextView.setText(sharedPreferences.getString(ALL_NUMBERS, ""));
            streamOneSet = sharedPreferences.getStringSet(STREAM_ONE_SET, streamOneSet);
            streamTwoSet = sharedPreferences.getStringSet(STREAM_TWO_SET, streamTwoSet);
            for (String numOne : streamOneSet) {
                streamOneQueue.add(Integer.valueOf(numOne));
            }
            for (String numTw0 : streamTwoSet) {
                streamTwoQueue.add(Integer.valueOf(numTw0));
            }
            //clear two sets after all the numbers are added into queues
            streamOneSet.clear();
            streamTwoSet.clear();
            previousNumsScrollView.fullScroll(View.FOCUS_DOWN);
        }

        //If either of the streams' name is reset or the app is just started,
        //the getFirstDataPiecesForTwoStreams will called to initialize the status.
        if (isFirstRun) {
            getFirstDataPiecesForTwoStreams();
            isFirstRun = false;
            nextNumberBtn.setEnabled(false);
        }
    }

    /**
     * Check whether the server could be reached in a reasonable time period. here the default value is 4 seconds)
     * If the server has no response in 4 seconds, the NEXT NUMBER will be disabled until the networking connection comes to be normal.
     */
    private void checkServerAvailability(final String url) {
        if (NetworkingChecker.isOnline(this)) {
            Handler handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {

                    if (msg.what != 1) { // code if not connected
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(StreamActivity.this, getResources().getString(R.string.no_networking_connection) + " URL:" + url, Toast.LENGTH_SHORT).show();
                                nextNumberBtn.setEnabled(false);
                            }
                        });
                    } else { // code if connected
                        nextNumberBtn.setEnabled(true);
                    }
                }
            };
            NetworkingChecker.isNetworkAvailable(handler, 2000, url);
        } else {
            Toast.makeText(StreamActivity.this, getResources().getString(R.string.no_networking_connection), Toast.LENGTH_SHORT).show();
            nextNumberBtn.setEnabled(false);
        }
    }

    /**
     * Get first datapiece from each stream sequentially.
     * The reason why I don't run those two tasks concurrently are :
     * <li>This method
     */
    public void getFirstDataPiecesForTwoStreams() {
        try {
            Handler stremOneHandler = new Handler() {
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (msg.obj != null) {
                        DataPiece tempData = (DataPiece) msg.obj;
                        streamOneQueue.offer(tempData.getLast());
                        streamOneQueue.offer(tempData.getCurrent());
                        Log.d(TAG, "Stream: " + tempData.getsName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                    }

                    Handler streamTwoHandler = new Handler() {
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.obj != null) {
                                DataPiece tempData = (DataPiece) msg.obj;
                                streamTwoQueue.offer(tempData.getLast());
                                streamTwoQueue.offer(tempData.getCurrent());
                                Log.d(TAG, "Stream: " + tempData.getsName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                            }
                        }
                    };
                    new StreamDataTask(streamTwoHandler).execute(MainActivity.PELOTON_API_SERVER + streamTwoName);
                }
            };
            new StreamDataTask(stremOneHandler).execute(MainActivity.PELOTON_API_SERVER + streamOneName);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in getFirstDataPiecesForTwoStreams.");
        }
        Log.e(TAG, "calling getFirstDataPiecesForTwoStreams.");
    }


    /**
     * Handle the click action of "NEXT NUMBER" button
     *
     * @param v the button
     */
    public void showNextNumber(final View v) {

        if (streamOneQueue.size() != 0 && streamTwoQueue.size() != 0) {
            updateViews();
        } else if (streamOneQueue.size() == 0) {

            getNextDataPiece(streamOneName);

        } else if (streamTwoQueue.size() == 0) {

            getNextDataPiece(streamTwoName);
        }
    }

    /**
     * Get the next datapiece from a stream
     *
     * @param streamName name of the stream
     */
    private void getNextDataPiece(final String streamName) {
        try {
            Handler asyncHandler = new Handler() {
                public void handleMessage(final Message msg) {
                    super.handleMessage(msg);
                    if (msg.obj != null) {
                        DataPiece tempData = (DataPiece) msg.obj;
                        if (streamName.equals(streamOneName)) {
                            streamOneQueue.offer(tempData.getCurrent());
                        } else {
                            streamTwoQueue.offer(tempData.getCurrent());
                        }
                        Log.d(TAG, "Stream: " + streamName + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                        updateViews();
                    }
                }
            };
            new StreamDataTask(asyncHandler).execute(MainActivity.PELOTON_API_SERVER + streamName);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in getNextDataPiece.");
        }
    }

    /**
     * Show next number and append it to allNumsTextView
     */
    private void updateViews() {

        if (streamOneQueue.size() != 0 && streamTwoQueue.size() != 0) {
            int numOne = streamOneQueue.poll();
            int numTwo = streamTwoQueue.poll();

            if (numOne <= numTwo) {
                currentNumberTextView.setText(Integer.toString(numOne));
                allNumsTextView.append(Integer.toString(numOne) + " ");

                //add numTwo back to the queue two
                streamTwoQueue.offer(numTwo);

            } else {
                currentNumberTextView.setText(Integer.toString(numTwo));
                allNumsTextView.append(Integer.toString(numTwo) + " ");

                //add numOne back to the queue one
                streamOneQueue.offer(numOne);
            }
            previousNumsScrollView.fullScroll(View.FOCUS_DOWN);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

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
    public void onDestroy() {

        super.onDestroy();

        savePreferences();
    }


    /**
     * Save the current status data to preferences when the user presses the Back button,
     * or user rotates the screen
     */
    private void savePreferences() {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CURRENT_NUMBER, currentNumberTextView.getText().toString());
        editor.putString(ALL_NUMBERS, allNumsTextView.getText().toString());
        editor.putBoolean(IS_FIRST_RUN, false);

        while (streamOneQueue.size() != 0) {
            streamOneSet.add(streamOneQueue.poll().toString());
        }
        while (streamTwoQueue.size() != 0) {
            streamTwoSet.add(streamTwoQueue.poll().toString());
        }
        editor.putStringSet(STREAM_ONE_SET, streamOneSet);
        editor.putStringSet(STREAM_TWO_SET, streamTwoSet);
        editor.apply();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stream, menu);
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
}
