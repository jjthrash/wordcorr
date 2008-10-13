package org.wordcorr.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Represents a setting.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Setting extends AbstractPersistent {

    Setting(Database db, long id) {
        super(db, id);
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get most recent user ID.
     **/
    public long getUserID() {
        return _userID;
    }

    /**
     * Set most recent user ID.
     **/
    public void setUserID(long v) {
        _userID = v;
    }

    /**
     * Get this user's most recent collection ID.
     */
    public long getCollectionID() {
        return _colID;
    }

    /**
     * Set this user's most recent collection ID.
     */
    public void setCollectionID(long v) {
        _colID = v;
    }

    /**
     * Get the user's most recent view ID.
     */
    public long getViewID() {
        return _viewID;
    }

    /**
     * Set the user's most recent view ID.
     */
    public void setViewID(long v) {
        _viewID = v;
    }

    /**
     * Get the value of entryID.
     **/
    public long getEntryID() {
        return _entryID;
    }

    /**
     * Set the value of entryID.
     * @param v Value to assign to entryID.
     **/
    public void setEntryID(long v) {
        _entryID = v;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setUserID(getIDValue(rs, 3));
        setCollectionID(getIDValue(rs, 4));
        setViewID(getIDValue(rs, 5));
        setEntryID(getIDValue(rs, 6));
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        setIDValue(stmt, 2, getUserID());
        setIDValue(stmt, 3, getCollectionID());
        setIDValue(stmt, 4, getViewID());
        setIDValue(stmt, 5, getEntryID());
        stmt.setLong(6, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
    }

    private static long getIDValue(ResultSet rs, int index) throws SQLException {
        long ret = rs.getLong(index);
        return rs.wasNull() ? -1 : ret;
    }

    private static void setIDValue(PreparedStatement stmt, int index, long value)
        throws SQLException {
        if (value == -1) {
            stmt.setNull(index, Types.BIGINT);
        } else {
            stmt.setLong(index, value);
        }
    }

    private long _userID = -1;
    private long _colID = -1;
    private long _viewID = -1;
    private long _entryID = -1;
}