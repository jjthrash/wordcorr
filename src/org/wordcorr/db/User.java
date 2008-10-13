package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a user in the database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class User extends AbstractPersistent {

    User(Database db, long id) {
        super(db, id);
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Set this object's name.
     * Capitalize
     **/
    public void setName(String name) {
        super.setName(name.toUpperCase());
    }

    /**
     * Get this user's affiliations.
     **/
    public String getAffiliations() {
        return _affiliations;
    }

    /**
     * Set this user's affiliations.
     **/
    public void setAffiliations(String affiliations) {
        setDirty();
        _affiliations = affiliations;
    }

    /**
     * Get this user's email address.
     **/
    public String getEmail() {
        return _email;
    }

    /**
     * Set this user's email address.
     **/
    public void setEmail(String email) {
        setDirty();
        _email = email;
    }

    /**
     * Get this user's family (last) name.
     **/
    public String getFamilyName() {
        return _familyName;
    }

    /**
     * Set this user's family (last) name.
     **/
    public void setFamilyName(String familyName) {
        setDirty();
        _familyName = familyName;
    }

    /**
     * Get this user's given (first) name.
     **/
    public String getGivenName() {
        return _givenName;
    }

    /**
     * Set this user's given (first) name.
     **/
    public void setGivenName(String givenName) {
        setDirty();
        _givenName = givenName;
    }

    /**
     * Get this user's collections.
     **/
    public List getCollections() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_COLLECTIONS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                WordCollection col = new WordCollection(db, rs.getLong(1), User.this);
                col.updateObject(rs);
                return col;
            }
        });
    }

    /**
     * Make a collection object. This does not add anything to the
     * database. The returned object must be saved to be made permanent.
     **/
    public WordCollection makeCollection() {
        return new WordCollection(getDatabase(), -1, this);
    }

    /**
     * Get element representing this user.
     **/
    public Element getElement() {
        Element element = new Element("user");

        // set attributes
        element.setAttribute("user-id", getName());
        element.setAttribute("family-name", getFamilyName());
        element.setAttribute("given-name", getGivenName());
        element.setAttribute("email", getEmail());
        // set elements
        element.addContent(createElement("affiliations", getAffiliations()));

        return element;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Override of save to also set user view to null.
     **/
    public synchronized void save() throws DatabaseException {
        boolean newObject = (getID() == UNDEFINED_ID) ? true : false;
        super.save();

        // set user view
        if (newObject) {
            getDatabase().getCurrentSetting().setViewID(UNDEFINED_ID);
        }
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setFamilyName(rs.getString(3));
        setGivenName(rs.getString(4));
        setEmail(rs.getString(5));
        setAffiliations(rs.getString(6));
    }

    /**
     * Set the parameters on the given save statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getFamilyName());
        stmt.setString(3, getGivenName());
        stmt.setString(4, getEmail());
        stmt.setString(5, getAffiliations());
    }

    /**
     * Set the parameters on the given save statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getFamilyName());
        stmt.setString(3, getGivenName());
        stmt.setString(4, getEmail());
        stmt.setString(5, getAffiliations());
        stmt.setLong(6, getID());
    }

    private String _affiliations;
    private String _email;
    private String _familyName;
    private String _givenName;
}