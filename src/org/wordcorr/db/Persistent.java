package org.wordcorr.db;

/**
 * Interface representing a persistent object.
 * @author Keith Hamasaki, Jim Shiba
 **/
public interface Persistent {

    /**
     * Check validation prior to save.
     * Return null if okay, message if not.
     **/
    String checkValidation() throws DatabaseException;

    /**
     * Save this object.
     **/
    void save() throws DatabaseException;

    /**
     * Delete this object.
     **/
    void delete() throws DatabaseException;

    /**
     * Revert this object to its database state.
     **/
    void revert() throws DatabaseException;

    /**
     * Is this object new? (not ever saved before)
     **/
    boolean isNew();

    /**
     * Has this object been modified since load or last save?
     **/
    boolean isDirty();

    /**
     * Clear the dirty flag.
     **/
    void clearDirty();
}
