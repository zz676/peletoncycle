package com.pelotoncycle.communication.pelotoncycle.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Zhisheng on 5/2/2015.
 */
public class DataPiece implements Comparable<DataPiece>{

    private String dName;
    private int last;
    private int current;


    public String getdName() {
        return dName;
    }

    public void setdName(String dName) {
        this.dName = dName;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }


    public static DataPiece jsonToDataPieceObject(final JSONObject jsonObject) {

        try {
            DataPiece result = new DataPiece();
            result.setdName(jsonObject.getString("stream"));
            result.setLast(jsonObject.getInt("last"));
            result.setCurrent(jsonObject.getInt("current"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(DataPiece.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int compareTo(DataPiece obj) {
        if(this.last == obj.last)
            return this.current - obj.current;
        return this.last - obj.last;
    }
}
