package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Represents an Entry.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Entry extends AbstractPersistent {

    Entry(Database db, long id, WordCollection collection) {
        super(db, id);
        _collection = collection;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the value of gloss2.
     **/
    public String getGloss2() {
        return (_gloss2 == null) ? "" : _gloss2;
    }

    /**
     * Set the value of gloss2.
     * @param v Value to assign to gloss2.
     **/
    public void setGloss2(String v) {
        _gloss2 = v;
        setDirty();
    }

    /**
     * Get the data for this entry.
     * @return A list of Datum objects for this entry.
     **/
    public List getData() {
        if (!_dataLoaded) {
            loadData();
        }
        return new ArrayList(_data);
    }

    /**
     * Set the data for this entry.
     * @param data A list of Datum objects for this entry.
     **/
    public void setData(List data) {
        _data = new ArrayList(data);
        _dataLoaded = true;
        setDirty();
    }

    /**
     * Get the value of entryNum.
     **/
    public Integer getEntryNum() {
        return _entryNum;
    }

    /**
     * Set the value of entryNum.
     * @param v Value to assign to entryNum.
     **/
    public void setEntryNum(Integer v) {
        _entryNum = v;
    }

    /**
     * Get this view's collection.
     **/
    public WordCollection getCollection() {
        return _collection;
    }

    /**
     * Get this entry's datums used for summary.
     **/
    public List getSummaryDatums(final View view, final Group group) throws DatabaseException {
        // get groups that have been tabulated
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_SUMMARY_DATUMS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, view.getID());
                stmt.setLong(2, group.getID());
                stmt.setLong(3, getID());
            }

            public Object createObject(Database db, ResultSet rs2) throws SQLException {
                Datum datum = new Datum(db, rs2.getLong(1), Entry.this);
                datum.updateObject(rs2);
                return datum;
            }
        });
        return groups;
    }

    /**
     * Get element representing this entry.
     **/
    public Element getElement() {
        Element element = new Element("entry");

        // set attributes
        element.setAttribute("entry-number", getEntryNum() + "");
        element.setAttribute("gloss", getName());
        element.setAttribute("secondary-gloss", getGloss2());

        // datums
        for (Iterator it = getData().iterator(); it.hasNext();) {
            Datum datum = (Datum) it.next();
            element.addContent(datum.getElement());
        }

        return element;
    }

    /**
     * Mark this datum as imported.
     **/
    public void markImported() {
        _imported = true;
    }

    /**
     * Delete this object.
     **/
    public void delete() throws DatabaseException {
        getDatabase().deleteObject(this);
    }

    /**
     * Make a datum object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Datum makeDatum() {
        return new Datum(getDatabase(), -1, this);
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Override of save to also update data.
     **/
    public synchronized void save() throws DatabaseException {
        super.save();

        if (!_imported) {
            // datums are not deleted by replacing _data list
            // but by flagging each datum as deleted to allow access.
            int count = 0;
            for (Iterator it = getData().iterator(); it.hasNext(); count++) {
                Datum datum = (Datum) it.next();
                if (datum.isDeleted()) {
                    datum.delete();
                } else {
                    datum.save();
                }
            }
            // update data _list.
            loadData();
            clearDirty();
        }
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setGloss2(rs.getString(3));
        _entryNum = new Integer(rs.getInt(4));
        if (rs.wasNull()) {
            _entryNum = null;
        }
    }

    /**
     * Load the data for this entry.
     **/
    private void loadData() {
        // load the data
        try {
            setData(getDatabase().retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_DATA";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException {
                    try {
                        stmt.setLong(1, _collection.getOriginalView().getID());
                        stmt.setLong(2, getID());
                    } catch (DatabaseException e) {
                        throw new SQLException(e.getRootCause().getMessage());
                    }
                }

                public Object createObject(Database db, ResultSet rs2) throws SQLException {
                    Datum datum = new Datum(db, rs2.getLong(1), Entry.this);
                    datum.updateObject(rs2);
                    return datum;
                }
            }));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getGloss2());
        if (_entryNum == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            stmt.setInt(3, _entryNum.intValue());
        }
        stmt.setLong(4, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getGloss2());
        if (_entryNum == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            stmt.setInt(3, _entryNum.intValue());
        }
        stmt.setLong(4, _collection.getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public Map generateFromElement(Element element, Map varieties)
        throws DatabaseException {
        if (element == null)
            return null;

        // set values
        setEntryNum(new Integer(element.getAttributeValue("entry-number")));
        setName(element.getAttributeValue("gloss"));
        setGloss2(element.getAttributeValue("secondary-gloss"));

        markImported();
        save();

        // datums
        Map datums = new HashMap();
        for (Iterator it = element.getChildren("datum").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            Datum datum = makeDatum();
            datum.generateFromElement(elem, varieties);
            datums.put(elem.getAttributeValue("datum-number"), datum);
        }

        return datums;
    }

    private boolean _dataLoaded = false;
    private boolean _imported = false;
    private Integer _entryNum;
    private String _gloss2;
    private List _data = Collections.EMPTY_LIST;
    private final WordCollection _collection;
}