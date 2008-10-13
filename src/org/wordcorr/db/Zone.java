package org.wordcorr.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a zone entry in the database.
 * @author Jim Shiba
 **/
public class Zone extends AbstractPersistent {

    Zone(Database db, long id) {
        super(db, id);
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the value of row in stylized phonetic chart.
     **/
    public Integer getRow() {
        return _row;
    }

    /**
     * Set the value of row in stylized phonetic chart.
     * @param v Value to assign to row.
     **/
    public void setRow(Integer v) {
        _row = v;
    }

    /**
     * Get the value of column in stylized phonetic chart.
     **/
    public Integer getColumn() {
        return _column;
    }

    /**
     * Set the value of column in stylized phonetic chart.
     * @param v Value to assign to column.
     **/
    public void setColumn(Integer v) {
        _column = v;
    }

    /**
     * Get the value of abbreviation.
     **/
    public String getAbbreviation() {
        return _abbreviation;
    }

    /**
     * Set the value of abbreviation.
     * @param v Value to assign to abbreviation.
     **/
    public void setAbbreviation(String v) {
        _abbreviation = v;
        setDirty();
    }

    /**
     * Get the zone name.
     **/
    public String getName() {
        return _name;
    }
    public String toString() {
    	return _name;
    }

    /**
     * Set the value of name.
     * @param v Value to assign to name.
     **/
    public void setName(String v) {
        _name = v;
        setDirty();
    }

    /**
     * Get the value of type.
     **/
    public String getType() {
        return _type;
    }

    /**
     * Set the value of type.
     * @param v Value to assign to type.
     **/
    public void setType(String v) {
        _type = v;
        setDirty();
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        _row = getInt(rs, 2);
        _column = getInt(rs, 3);
        _abbreviation = rs.getString(4);
        _name = rs.getString(5);
        _type = rs.getString(6);
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        setInt(stmt, 1, _row);
        setInt(stmt, 2, _column);
        stmt.setString(3, _name);
        stmt.setString(4, _abbreviation);
        stmt.setString(5, _type);
        stmt.setLong(6, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        setInt(stmt, 1, _row);
        setInt(stmt, 2, _column);
        stmt.setString(3, _abbreviation);
        stmt.setString(4, _name);
        stmt.setString(5, _type);
    }

    private Integer _column;
    private Integer _row;
    private String _abbreviation;
    private String _name;
    private String _type;
}
