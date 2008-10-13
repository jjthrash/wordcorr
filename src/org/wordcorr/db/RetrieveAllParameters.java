package org.wordcorr.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contains parameters for retrieving a set of objects from the
 * database.
 * @author Keith Hamasaki
 **/
public interface RetrieveAllParameters {

    /**
     * Get the retrieve all SQL statement key for this object.
     **/
    String getRetrieveAllSQLKey();

    /**
     * Set parameters on the given retrieve all statement.
     **/
    void setRetrieveAllParameters(PreparedStatement stmt)
        throws SQLException;

    /**
     * Create a new object with the given database and result set.
     **/
    Object createObject(Database db, ResultSet rs)
        throws SQLException;
}
