package com.pelotoncycle.communication.pelotoncycle.utilities;

import com.pelotoncycle.communication.pelotoncycle.models.DataPiece;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Zhisheng on 5/2/2015.
 */
public class StreamNumService {

    /**
     *
     * @param urlString
     * @return
     */
    public static DataPiece getDataPiece(final String urlString) {

        try {
            String json = getResponseJSON(urlString);
            JSONObject jsonObject = new JSONObject(json);
            return DataPiece.jsonToDataPieceObject(jsonObject);

        } catch (JSONException ex) {
            Logger.getLogger(StreamNumService.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        return null;
    }

    /**
     *
     * @param urlString
     * @return
     */
    private static String getResponseJSON(final String urlString) {
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
                content.append(line + "\n");
            }
            bufferedReader.close();
            return content.toString();
        } catch (IOException e) {
            Logger.getLogger(StreamNumService.class.getName()).log(Level.SEVERE,
                    null, e);
        } finally {
            urlConnection.disconnect();
        }
        return null;
    }
}
