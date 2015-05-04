package com.pelotoncycle.communication.pelotoncycle.utilities;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;

/**
 * Implements {@link AsyncTask}, and used to retrieve {@link DataPiece} from a stream
 *
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class StreamDataTask extends AsyncTask<String, Void, DataPiece> {

    //Used to send messages back to the UI
    private Handler mainUIHandler;

    public StreamDataTask(final Handler mainUIHandler) {
        this.mainUIHandler = mainUIHandler;
    }

    @Override
    protected void onPostExecute(DataPiece result) {
        super.onPostExecute(result);

        // Send a DataPiece object back to UI
        Message msg = Message.obtain();
        msg.obj = result;
        mainUIHandler.sendMessage(msg);
    }

    @Override
    protected DataPiece doInBackground(String... params) {

        String url = params[0];
        return StreamNumService.getDataPiece(url);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}