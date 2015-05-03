package com.pelotoncycle.communication.pelotoncycle.Controllers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.pelotoncycle.communication.pelotoncycle.R;
import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;
import com.pelotoncycle.communication.pelotoncycle.utilities.StreamDataTask;

import java.util.PriorityQueue;

public class StreamActivity extends Activity {

    private final static String TAG = "StreamActivity";
    private String streamOneName;
    private String streamTwoName;
    private TextView numberTextview;
    private PriorityQueue<Integer> streamOneQueue = new PriorityQueue<>();
    private PriorityQueue<Integer> streamTwoQueue = new PriorityQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        if (getIntent() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                streamOneName = extras.getString(MainActivity.STREAM_ONE_NAME);
                streamTwoName = extras.getString(MainActivity.STREAM_TWO_NAME);
            }
        }
        numberTextview = (TextView) findViewById(R.id.number_textview);
        getFirstDataPiecesForTwoStreams();
    }

    public void getFirstDataPiecesForTwoStreams() {

        Handler stremOneHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.obj != null) {
                    DataPiece tempData = (DataPiece) msg.obj;
                    streamOneQueue.offer(tempData.getLast());
                    streamOneQueue.offer(tempData.getCurrent());

                    Log.d(TAG, "Stream: " + tempData.getdName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                }

                Handler streamTwoHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.obj != null) {
                            DataPiece tempData = (DataPiece) msg.obj;
                            streamTwoQueue.offer(tempData.getLast());
                            streamTwoQueue.offer(tempData.getCurrent());
                            Log.d(TAG, "Stream: " + tempData.getdName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                        }
                    }
                };
                new StreamDataTask(streamTwoHandler).execute(MainActivity.PELOTON_API_SERVER + streamTwoName);
            }
        };
        new StreamDataTask(stremOneHandler).execute(MainActivity.PELOTON_API_SERVER + streamOneName);
    }


    /**
     * handle the click action of "NEXT NUMBER" button
     *
     * @param v the button
     */

    public void showNextNumber(final View v) {

        if (streamOneQueue.size() != 0 && streamTwoQueue.size() != 0) {
            updateNextNumber();
        } else if (streamOneQueue.size() == 0) {

            Handler asyncHandler = new Handler() {
                public void handleMessage(final Message msg) {
                    super.handleMessage(msg);
                    if (msg.obj != null) {
                        DataPiece tempData = (DataPiece) msg.obj;
                        streamOneQueue.offer(tempData.getCurrent());
                        Log.d(TAG, "Stream: " + tempData.getdName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                        updateNextNumber();
                    }
                }
            };
            new StreamDataTask(asyncHandler).execute(MainActivity.PELOTON_API_SERVER + streamOneName);

        } else if (streamTwoQueue.size() == 0) {

            Handler asyncHandler = new Handler() {
                public void handleMessage(final Message msg) {
                    super.handleMessage(msg);
                    if (msg.obj != null) {
                        DataPiece tempData = (DataPiece) msg.obj;
                        streamTwoQueue.offer(tempData.getCurrent());
                        Log.d(TAG, "Stream: " + tempData.getdName() + " Last:" + tempData.getLast() + " Current:" + tempData.getCurrent());
                        updateNextNumber();
                    }
                }
            };
            new StreamDataTask(asyncHandler).execute(MainActivity.PELOTON_API_SERVER + streamTwoName);
        }
    }

    /**
     *
     */

    public void updateNextNumber() {
        if (streamOneQueue.size() != 0 && streamTwoQueue.size() != 0) {
            int numOne = streamOneQueue.poll();
            int numTwo = streamTwoQueue.poll();

            if (numOne <= numTwo) {
                numberTextview.setText(Integer.toString(numOne));
                streamTwoQueue.offer(numTwo);

            } else {
                numberTextview.setText(Integer.toString(numTwo));
                streamOneQueue.offer(numOne);
            }
        }
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
