package com.pelotoncycle.communication.pelotoncycle.utilities;

import android.util.Log;

import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Includes methods to communicate with the server, and methods to handle with json objects
 *
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class StreamNumService {

    //a tag for logging
    private final static String TAG = "StreamNumService";

    /**
     * Get the next piece of data on the stream
     *
     * @param urlString the url of the stream
     * @return a {@link DataPiece} object
     */
    public static DataPiece getDataPiece(final String urlString) throws Exception {

        String json = getResponseJSON(urlString);
        JSONObject jsonObject = new JSONObject(json);
        return jsonToDataPieceObject(jsonObject);

    }

    /**
     * Convert a jsonObject into a DataPiece object
     *
     * @param jsonObject the json object to be converted
     * @return a DataPiece object
     */
    public static DataPiece jsonToDataPieceObject(final JSONObject jsonObject) throws Exception {

        DataPiece result = new DataPiece();
        result.setsName(jsonObject.getString("stream"));
        result.setLast(jsonObject.getInt("last"));
        result.setCurrent(jsonObject.getInt("current"));
        return result;
    }

    /**
     * Get json string from the server
     *
     * @param urlString url for the stream
     * @return the json string
     */
    private static String getResponseJSON(final String urlString) throws Exception {
        StringBuilder content = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()), 8);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
                content.append("\n");
            }
            bufferedReader.close();
            return content.toString();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() + " in StreamNumService.getResponseJSON");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
