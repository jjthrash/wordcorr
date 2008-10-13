package org.wordcorr.db;

import java.sql.Timestamp;
import java.util.*;

/**
 * Represents single row data.
 * @author Jim Shiba
 **/
public class RowData {
	/**
	 * Constructor
	 **/
	public RowData() {
	}

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get this object's local client ID.
     **/
    public long getID() {
        return _id;
    }

    /**
     * Set this object's local client ID.
     **/
    public void setID(long id) {
        _id = id;
    }

    /**
     * Get this object's remote server ID.
     **/
    public long getRemoteID() {
        return _remoteID;
    }

    /**
     * Set this object's remote server ID.
     **/
    public void setRemoteID(long id) {
        _remoteID = id;
    }

    /**
     * Get this object's data.
     **/
    public Map getData() {
        return _data;
    }

    /**
     * Set this object's data.
     **/
    public void setData(Map data) {
        _data = data;
    }

    /**
     * Get this object's remote server timestamp.
     **/
    public Timestamp getTimestamp() {
        return _timestamp;
    }

    /**
     * Set this object's remote server timestamp.
     **/
    public void setTimestamp(Timestamp timestamp) {
        _timestamp = timestamp;
    }

	private long _id;
	private long _remoteID;
	private Map _data;
	private Timestamp _timestamp;
}

