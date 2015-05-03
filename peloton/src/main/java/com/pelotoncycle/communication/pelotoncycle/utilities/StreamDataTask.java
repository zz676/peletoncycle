package com.pelotoncycle.communication.pelotoncycle.utilities;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;

/**
 * Created by Zhisheng on 5/2/2015.
 */
public class StreamDataTask extends AsyncTask<String, Void, DataPiece> {

    private final static String TAG = "StreamDataTask";

    //Used to send messages back to the mainUI
    private Handler mainUIHandler;

    public StreamDataTask(final Handler mainUIHandler) {
        this.mainUIHandler = mainUIHandler;
    }

    @Override
    protected void onPostExecute(DataPiece result) {
        super.onPostExecute(result);

        Message msg = Message.obtain();
        msg.obj = result;
        mainUIHandler.sendMessage(msg);
    }

    @Override
    protected DataPiece doInBackground(String... params) {

        //android.os.Debug.waitForDebugger();
        String url = params[0];
        return StreamNumService.getDataPiece(url) ;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}