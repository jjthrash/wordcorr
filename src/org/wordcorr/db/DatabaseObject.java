package org.wordcorr.db;

import java.sql.*;

/**
 * Interface representing objects that can be saved to the
 * database. This is a package only interface that the database can
 * use, unlike the Persistent interface, which is for external use.
 * @author Keith Hamasaki
 **/
interface DatabaseObject {

    /**
     * Get the ID of this object.
     **/
    long getID();

    /**
     * Update this object based on a result set.
     **/
    abstract void updateObject(ResultSet rs) throws SQLException;

    /**
     * Set the parameters on the given save statement.
     **/
    abstract void setCreateParameters(PreparedStatement stmt)
        throws SQLException;

    /**
     * Set the parameters on the given save statement.
     **/
    abstract void setUpdateParameters(PreparedStatement stmt)
        throws SQLException;

}
