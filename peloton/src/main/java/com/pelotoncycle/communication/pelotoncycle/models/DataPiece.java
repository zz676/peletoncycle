package com.pelotoncycle.communication.pelotoncycle.models;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A instance of this class stands for a piece of data retrieved from a stream at each time
 *
 * @author Zhisheng Zhou
 * @version 1.0
 */
public class DataPiece implements Comparable<DataPiece> {

    private final static String TAG = "DataPiece";

    /**
     * name of the stream
     */
    private String sName;

    /**
     * last number
     */
    private int last;

    /**
     * current number
     */
    private int current;

    /**
     * Getter for sName
     *
     * @return sName
     */
    public String getsName() {
        return sName;
    }

    /**
     * Setter for sName
     *
     * @param sName the value to be set
     */
    public void setsName(String sName) {
        this.sName = sName;
    }

    /**
     * Getter for last
     *
     * @return last
     */
    public int getLast() {
        return last;
    }

    /**
     * Setter for last
     *
     * @param last the value to be set
     */
    public void setLast(int last) {
        this.last = last;
    }

    /**
     * Getter for current
     *
     * @return current
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Setter for current
     *
     * @param current the value to be set
     */
    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public int compareTo(@NonNull final DataPiece obj) {
        if (this.last == obj.last)
            return this.current - obj.current;
        return this.last - obj.last;
    }

    @Override
    public String toString() {
        return "Stream Name:" + this.sName + " last:" + this.last + " current:" + this.current;
    }
}
