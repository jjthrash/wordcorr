package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.wordcorr.gui.AppPrefs;

/**
 * Represents a speech variety in the database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Variety extends AbstractPersistent {

    Variety(Database db, long id, WordCollection collection) {
        super(db, id);
        _collection = collection;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the value of shortName.
     **/
    public String getShortName() {
        return (_shortName == null) ? "" : _shortName;
    }

    /**
     * Set the value of shortName.
     * @param v Value to assign to shortName.
     **/
    public void setShortName(String v) {
        _shortName = v;
        setDirty();
    }

    /**
     * Get the value of abbreviation.
     **/
    public String getAbbreviation() {
        return (_abbreviation == null) ? "" : _abbreviation;
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
     * Get the value of alternateName.
     **/
    public String getAlternateName() {
        return (_alternateName == null) ? "" : _alternateName;
    }

    /**
     * Set the value of alternateName.
     * @param v Value to assign to alternateName.
     **/
    public void setAlternateName(String v) {
        _alternateName = v;
        setDirty();
    }

    /**
     * Get the value of remarks.
     **/
    public String getRemarks() {
        return (_remarks == null) ? "" : _remarks;
    }

    /**
     * Set the value of remarks.
     * @param v Value to assign to remarks.
     **/
    public void setRemarks(String v) {
        _remarks = v;
        setDirty();
    }

    /**
     * Get the value of ethnologueCode.
     **/
    public String getEthnologueCode() {
        return (_ethnologueCode == null) ? "" : _ethnologueCode;
    }

    /**
     * Set the value of ethnologueCode.
     * @param v Value to assign to ethnologueCode.
     **/
    public void setEthnologueCode(String v) {
        _ethnologueCode = v;
        setDirty();
    }

    /**
     * Get the value of classification.
     **/
    public String getClassification() {
        return (_classification == null) ? "" : _classification;
    }

    /**
     * Set the value of classification.
     * @param v Value to assign to classification.
     **/
    public void setClassification(String v) {
        _classification = v;
        setDirty();
    }

    /**
     * Get the value of quality.
     **/
    public String getQuality() {
        return (_quality == null) ? "" : _quality;
    }

    /**
     * Set the value of quality.
     * @param v Value to assign to quality.
     **/
    public void setQuality(String v) {
        _quality = v;
        setDirty();
    }

    /**
     * Get the value of locale.
     **/
    public String getLocale() {
        return (_locale == null) ? "" : _locale;
    }

    /**
     * Set the value of locale.
     * @param v Value to assign to locale.
     **/
    public void setLocale(String v) {
        _locale = v;
        setDirty();
    }

    /**
     * Get the value of source.
     **/
    public String getSource() {
        return (_source == null) ? "" : _source;
    }

    /**
     * Set the value of source.
     * @param v Value to assign to source.
     **/
    public void setSource(String v) {
        _source = v;
        setDirty();
    }

    /**
     * Get the value of unpublishedSource.
     **/
    public String getUnpublishedSource() {
        return (_unpublishedSource == null) ? "" : _unpublishedSource;
    }

    /**
     * Set the value of unpublishedSource.
     * @param v Value to assign to unpublishedSource.
     **/
    public void setUnpublishedSource(String v) {
        _unpublishedSource = v;
        setDirty();
    }

    /**
     * Get the value of country.
     **/
    public String getCountry() {
        return (_country == null) ? "" : _country;
    }

    /**
     * Set the value of country.
     * @param v Value to assign to country.
     **/
    public void setCountry(String v) {
        _country = v;
        setDirty();
    }

    /**
     * Get this view's collection.
     **/
    public WordCollection getCollection() {
        return _collection;
    }

    /**
     * Get element representing this variety.
     **/
    public Element getElement() {
        Element element = new Element("variety");

        // set attributes
        element.setAttribute("name", getName());
        element.setAttribute("short-name", getShortName());
        element.setAttribute("abbreviation", getAbbreviation());
        element.setAttribute("ethnologue-code", getEthnologueCode());
        // set elements
        element.addContent(createElement("alternate-name-list", getAlternateName()));
        element.addContent(createElement("classification", getClassification()));
        element.addContent(createElement("locale", getLocale()));
        element.addContent(createElement("quality", getQuality()));
        element.addContent(createElement("source", getSource()));
        element.addContent(createElement("unpublished-source", getUnpublishedSource()));
        element.addContent(createElement("country-where-collected", getCountry()));
        element.addContent(createElement("remarks", getRemarks()));

        return element;
    }

    /**
     * Mark this collection as imported.
     **/
    public void markImported() {
        _imported = true;
    }

    /**
     * Reset attributes.
     **/
    public void reset() {
        setID(-1);
        setName("");
        _abbreviation = "";
        _shortName = "";
        _ethnologueCode = "";
        _locale = "";
        _quality = "";
        _source = "";
    }

    /**
     * Delete this object.
     **/
    public void delete() throws DatabaseException {
        getDatabase().deleteObject(this);

        // remove from views
        List views = _collection.getViews();
        for (Iterator it = views.iterator(); it.hasNext();) {
            View view = (View) it.next();
            List members = view.getMembers();
            members.remove(this);
            view.setMembers(members);
            view.save();
        }
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Check validation prior to save.
     * Return null if okay, message if not.
     **/
    public String checkValidation() throws DatabaseException {
        // check for duplicate
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VARIETY_COUNT_NAME";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setString(2, getName());
                stmt.setLong(3, _collection.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        if (((Integer) list.get(0)).intValue() > 0)
            return AppPrefs.getInstance().getMessages().getString(
                "msgVarietyValidationName");

        list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VARIETY_COUNT_SHORTNAME";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setString(2, _shortName);
                stmt.setLong(3, _collection.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        if (((Integer) list.get(0)).intValue() > 0)
            return AppPrefs.getInstance().getMessages().getString(
                "msgVarietyValidationShortName");

        if (getAbbreviation().equals(""))
            return null;

        list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VARIETY_COUNT_ABBR";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setString(2, getAbbreviation());
                stmt.setLong(3, _collection.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return (((Integer) list.get(0)).intValue() > 0)
            ? AppPrefs.getInstance().getMessages().getString(
                "msgVarietyValidationAbbreviation")
            : null;
    }

    /**
     * Override of save to also add to Original view.
     **/
    public synchronized void save() throws DatabaseException {
        boolean newObject = (getID() == UNDEFINED_ID) ? true : false;
        super.save();

        // add to Original view
        if (newObject && !_imported) {
            View original = _collection.getOriginalView();
            List members = original.getMembers();
            members.add(this);
            original.setMembers(members);
            original.save();
        }
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setShortName(rs.getString(3));
        setAbbreviation(rs.getString(4));
        setAlternateName(rs.getString(5));
        setRemarks(rs.getString(6));
        setEthnologueCode(rs.getString(7));
        setClassification(rs.getString(8));
        setQuality(rs.getString(9));
        setLocale(rs.getString(10));
        setSource(rs.getString(11));
        setUnpublishedSource(rs.getString(12));
        setCountry(rs.getString(13));
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getShortName());
        stmt.setString(3, getAbbreviation());
        stmt.setString(4, getAlternateName());
        stmt.setString(5, getRemarks());
        stmt.setString(6, getEthnologueCode());
        stmt.setString(7, getClassification());
        stmt.setString(8, getQuality());
        stmt.setString(9, getLocale());
        stmt.setString(10, getSource());
        stmt.setString(11, getUnpublishedSource());
        stmt.setString(12, getCountry());
        stmt.setLong(13, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getShortName());
        stmt.setString(3, getAbbreviation());
        stmt.setString(4, getAlternateName());
        stmt.setString(5, getRemarks());
        stmt.setString(6, getEthnologueCode());
        stmt.setString(7, getClassification());
        stmt.setString(8, getQuality());
        stmt.setString(9, getLocale());
        stmt.setString(10, getSource());
        stmt.setString(11, getUnpublishedSource());
        stmt.setString(12, getCountry());
        stmt.setLong(13, _collection.getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(Element element, double version)
        throws DatabaseException {
        if (element == null)
            return;

        // set values
        setName(element.getAttributeValue("name"));
        setShortName(element.getAttributeValue("short-name"));
        setAbbreviation(element.getAttributeValue("abbreviation"));
        setEthnologueCode(element.getAttributeValue("ethnologue-code"));
        setLocale(element.getChildText("locale"));
        setQuality(element.getChildText("quality"));

        // move source to remarks for non-metadata version
        if (version < 2) {
            setRemarks(element.getChildText("source"));
        }

        // metadata from version 2
        if (version >= 2) {
            setSource(element.getChildText("source"));
            setAlternateName(element.getChildText("alternate-name-list"));
            setClassification(element.getChildText("classification"));
            setUnpublishedSource(element.getChildText("unpublished-source"));
            setCountry(element.getChildText("country-where-collected"));
            setRemarks(element.getChildText("remarks"));
        }

        markImported();
        save();
    }

    private boolean _imported = false;
    private String _shortName;
    private String _abbreviation;
    private String _alternateName;
    private String _remarks;
    private String _ethnologueCode;
    private String _classification;
    private String _quality;
    private String _locale;
    private String _source;
    private String _unpublishedSource;
    private String _country;
    private final WordCollection _collection;
}