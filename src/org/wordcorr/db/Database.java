package org.wordcorr.db;

import java.util.List;

/**
 * Represents a database of WordCorr information.
 * @author Keith Hamasaki, Jim Shiba
 **/
public interface Database {

    // status constants
    int STATUS_CURRENT       = 0;
    int STATUS_UNINITIALIZED = 1;
    int STATUS_OLD           = 2;

    /**
     * Get the name of this database.
     **/
    String getName();

    /**
     * Test this database. Returns one of the status constants defined
     * in this class.
     * @throw SQLException if an error occurs connecting to the
     * database.
     **/
    int test() throws DatabaseException;

    /**
     * Initialize this database.
     **/
    void init() throws DatabaseException;

    /**
     * Migrate this database from an older version.
     **/
    void migrate() throws DatabaseException;

    /**
     * Return a list of all users in the database.
     **/
    List getUsers() throws DatabaseException;

    /**
     * Get the current setting.
     **/
    Setting getCurrentSetting() throws DatabaseException;

    /**
     * Return a list of all zones in the database.
     **/
    List getZones() throws DatabaseException;

    /**
     * Return a zone in the database based on row and column.
     **/
    Zone getZone(final Integer row, final Integer col) throws DatabaseException;

    /**
     * Make an object of the given type, with no data. This does not
     * create an entry in the database, but creates an in-memory
     * object that can later be saved to the database.
     **/
    Persistent makeObject(Class cl) throws DatabaseException;

    /**
     * Retrieve a list of all objects of the given type.
     **/
    List retrieveObjects(RetrieveAllParameters params) throws DatabaseException;

    /**
     * Add a database object.
     **/
    long createObject(DatabaseObject object) throws DatabaseException;

    /**
     * Save a database object.
     **/
    void saveObject(DatabaseObject object) throws DatabaseException;

    /**
     * Delete a database object.
     **/
    void deleteObject(DatabaseObject object) throws DatabaseException;

    /**
     * Revert an object to its database state.
     **/
    void revertObject(DatabaseObject object) throws DatabaseException;

    /**
     * Link an object to other objects with an optional order.
     **/
    void linkObjects(LinkParameters params) throws DatabaseException;

    /**
     * Insert database records and return new record information.
     **/
    List insertRecords(StatementParameters parameters) throws DatabaseException;

    /**
     * Update database records and return new record information.
     **/
    List updateRecords(StatementParameters parameters) throws DatabaseException;
}
