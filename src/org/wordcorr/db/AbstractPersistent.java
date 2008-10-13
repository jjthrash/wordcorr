package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base class for persistent objects.
 * @author Keith Hamasaki, Jim Shiba
 **/
public abstract class AbstractPersistent implements Persistent, DatabaseObject {

    /**
     * Constructor.
     **/
    AbstractPersistent(Database db, long id) {
        _db = db;
        _id = id;
    }

    /**
     * Get the database associated with this object.
     **/
    public Database getDatabase() {
        return _db;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get this object's ID.
     **/
    public long getID() {
        return _id;
    }

    /**
     * Set this object's ID.
     **/
    public void setID(long id) {
        _id = id;
        setDirty();
    }

    /**
     * Get this object's name.
     **/
    public String getName() {
        return _name;
    }

    /**
     * Set this object's name.
     **/
    public void setName(String name) {
        _name = name;
        setDirty();
    }

    /**
     * Create element and set text for this object.
     **/
    public Element createElement(String name, String text) {
    	Element elem = new Element(name);
    	elem.setText(text);
    	return elem;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Check validation prior to save.
     * Return null of okay, message if not.
     **/
    public String checkValidation() throws DatabaseException {
    	return null;
    }

    /**
     * Save this object.
     **/
    public synchronized void save() throws DatabaseException {
        if (_id == UNDEFINED_ID) {
            _id = _db.createObject(this);
        } else {
            _db.saveObject(this);
        }
        _dirty = false;
    }

    /**
     * Delete this object from database.
     **/
    public synchronized void delete() throws DatabaseException {
        _db.deleteObject(this);
        _id = UNDEFINED_ID;
        _dirty = false;
    }

    /**
     * Revert this object.
     **/
    public synchronized void revert() throws DatabaseException {
        _db.revertObject(this);
        _dirty = false;
    }

    /**
     * Is this object dirty?
     **/
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * Is this object new?
     **/
    public boolean isNew() {
	return _id == UNDEFINED_ID;
    }

    /**
     * Clear the dirty flag.
     **/
    public void clearDirty() {
        _dirty = false;
    }

    /**
     * Set the dirty flag.
     **/
    protected void setDirty() {
        _dirty = true;
    }

    /**
     * Set Integer in PreparedStatment.
     **/
    protected void setInt(PreparedStatement stmt, int index, Integer value)
        throws SQLException
    {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value.intValue());
        }
    }

    /**
     * Get Integer from ResultSet.
     **/
    protected Integer getInt(ResultSet rs, int index) throws SQLException {
        int ret = rs.getInt(index);
        return rs.wasNull() ? null : new Integer(ret);
    }

    //---------------------------------------------------------------//
    // Object Method Overrides
    //---------------------------------------------------------------//

    /**
     * Return a string representation of this user.
     **/
    public String toString() {
        return isDirty() ? "+> " + _name : _name;
    }

    /**
     * Are two persistent objects equal?
     **/
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        return _id == ((AbstractPersistent) o)._id;
    }

    /**
     * Get the hash code for this object.
     **/
    public int hashCode() {
        return (getClass().getName() + _id).hashCode();
    }

    public static long UNDEFINED_ID = -1;
    private final Database _db;
    private long _id;
    private String _name;
    private boolean _dirty;
}
