package com.pelotoncycle.communication.pelotoncycle.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * A class including methods, which are used to check networking availability
 *
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class NetworkingChecker {

    private final static String TAG = "NetworkingChecker";

    /**
     * check if a network connection is available and the network can make remote connection to the server
     * ask fo message '0' (not connected) or '1' (connected) on 'handler'
     *
     * @param handler   {@link Handler}
     * @param timeout   the time given to the server to response (in milliseconds)
     * @param serverUrl the server to be connected to
     */
    public static void isNetworkAvailable(final Handler handler, final int timeout, final String serverUrl) {
        try {
            new Thread() {
                private boolean responded = false;

                @Override
                public void run() {

                    // set 'responded' to TRUE if is able to connect with the server (responds fast)
                    new Thread() {
                        @Override
                        public void run() {

                            try {
                                HttpGet requestForTest = new HttpGet(serverUrl);
                                new DefaultHttpClient().execute(requestForTest); // can last...
                                responded = true;
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage() + " in NetworkingChecker.isNetworkAvailable");
                            }
                        }
                    }.start();

                    try {
                        int waited = 0;
                        while (!responded && (waited < timeout)) {
                            sleep(100);
                            if (!responded) {
                                waited += 100;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage() +  "in NetworkingChecker.isNetworkAvailable");
                    } finally {
                        if (!responded) {
                            handler.sendEmptyMessage(0);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }
                }
            }.start();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in isNetworkAvailable.");
        }
    }


    /**
     * check whether your android device is online
     *
     * @param mContext the activity where this method is called
     * @return true if there is networking connection available, false otherwise
     */
    public static boolean isOnline(final Context mContext) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage() + " in isOnline.");
        }
        return false;
    }
}
