package org.wordcorr.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Contains parameters for performing linking of objects in a
 * many-to-one relationship.
 * @author Keith Hamasaki
 **/
interface LinkParameters {

    /**
     * Get the clear link SQL key.
     **/
    String getRemoveSQLKey();

    /**
     * Get the link SQL key.
     **/
    String getCreateSQLKey();

    /**
     * Get the update link SQL key.
     **/
    String getUpdateSQLKey();

    /**
     * Is the item at the given index linked?
     **/
    boolean isLinked(int i);

    /**
     * Set the parameters on the clear link SQL statement.
     **/
    void setRemoveParameters(PreparedStatement stmt, int index)
        throws SQLException;

    /**
     * Set the parameters on the link SQL statement.
     **/
    void setCreateParameters(PreparedStatement stmt, int index)
        throws SQLException;

    /**
     * Set the parameters on the update SQL statement.
     **/
    void setUpdateParameters(PreparedStatement stmt, int index)
        throws SQLException;

    /**
     * Get a count of how many items to link.
     **/
    int getLinkCount();
}
