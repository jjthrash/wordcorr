package org.wordcorr.db;

import org.jdom.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Represents a single raw datum in the database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Datum extends AbstractPersistent {

    Datum(Database db, long id, Entry entry) {
        super(db, id);
        _entry = entry;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get this datum's variety.
     **/
    public Variety getVariety() {
        return _variety;
    }

    /**
     * Set this datum's variety.
     **/
    public void setVariety(Variety v) {
        _variety = v;
    }

    /**
     * Get the value of specialSemantics.
     **/
    public String getSpecialSemantics() {
        return (_specialSemantics == null) ? "" : _specialSemantics;
    }

    /**
     * Set the value of specialSemantics.
     * @param v Value to assign to specialSemantics.
     **/
    public void setSpecialSemantics(String v) {
        _specialSemantics = v;
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
     * Get this datum's entry.
     **/
    public Entry getEntry() {
        return _entry;
    }

    /**
     * Get the value of entryNum.
     **/
    public Integer getEntryNum() {
        return getEntry().getEntryNum();
    }
    
    /**
     * Get the value of datum's entry gloss.
     **/
    public String getEntryGloss() {
    	return getEntry().getName();
    }

    /**
     * Get element representing this datum.
     **/
    public Element getElement() {
    	Element element = new Element("datum");
    	
    	// set attributes
    	element.setAttribute("datum-number", getID() + "");
    	element.setAttribute("short-name", getVariety().getShortName());
    	element.setAttribute("datum", getName());
    	// set elements
    	element.addContent(createElement("special-semantics", getSpecialSemantics()));
    	element.addContent(createElement("remarks", getRemarks()));
    	
    	return element;
    }

    /**
     * Mark this datum as deleted.
     **/
    public void markDeleted() {
        _deleted = true;
    }

    /**
     * Is this datum deleted?
     **/
    public boolean isDeleted() {
        return _deleted;
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
     * Fuse this datum with the given alignment vector.
     **/
    public String fuseWithAlignment(String vector) {
        String raw = getName();
        if (vector == null) {
            return raw;
        }

        StringBuffer ret = new StringBuffer();
        int rawIndex = 0;
        for (int i = 0; i < vector.length(); i++) {
            if (vector.charAt(i) == Alignment.HOLD_SYMBOL) {
                if (rawIndex < raw.length()) {
                    ret.append(raw.charAt(rawIndex++));
                }
            } else {
                ret.append(vector.charAt(i));
            }
        }

        if (rawIndex < raw.length()) {
            ret.append(raw.substring(rawIndex));
        }
        return ret.toString();
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Override of save to also extract out alignment.
     **/
    public synchronized void save() throws DatabaseException {
        if (getID() == UNDEFINED_ID && !_imported) {
	    	// extract alignment
	    	String alignedDatum = getName();
	    	StringBuffer datum = new StringBuffer();
	    	StringBuffer vector = new StringBuffer();
	        for (int i = 0; i < alignedDatum.length(); i++) {
	        	char ch = alignedDatum.charAt(i);
	            if (ch == Alignment.INDEL_SYMBOL || ch == Alignment.EXCLUDE_SYMBOL) {
	            	vector.append(ch);
	            } else {
	            	datum.append(ch);
	            	vector.append(Alignment.HOLD_SYMBOL);
	            }
	        }
	        setName(datum.toString());
	    	
	        super.save();
	        
	        // create alignment
	        View originalView = _entry.getCollection().getOriginalView();
	        Alignment alignment = _entry.getCollection().getOriginalView().makeAlignment(this);
	        alignment.setVector(vector.toString());
	        alignment.setGroup(originalView.getGroup("?", _entry));
	        alignment.save();
        } else {
        	super.save();
        }
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        long vid = rs.getLong(3);
        if (rs.wasNull()) {
            _variety = null;
        } else {
            try {
                _variety = new Variety(getDatabase(), vid, _entry.getCollection());
                _variety.revert();
            } catch (DatabaseException e) {
                e.printStackTrace();
                throw new SQLException(e.getRootCause().getMessage());
            }
        }
        _specialSemantics = rs.getString(4);
        _remarks = rs.getString(5);
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        if (_variety == null) {
            stmt.setNull(2, Types.BIGINT);
        } else {
            stmt.setLong(2, _variety.getID());
        }
        stmt.setString(3, _specialSemantics);
        stmt.setString(4, _remarks);
        stmt.setLong(5, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        if (_variety == null) {
            stmt.setNull(2, Types.BIGINT);
        } else {
            stmt.setLong(2, _variety.getID());
        }
        stmt.setString(3, _specialSemantics);
        stmt.setString(4, _remarks);
        stmt.setLong(5, _entry.getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(Element element, Map varieties) throws DatabaseException {
    	if (element == null)
    		return;

    	// get variety
    	String varietyName = element.getAttributeValue("short-name");
    	Variety variety = (Variety)varieties.get(varietyName);
    	
    	// set values
    	setVariety(variety);
    	setName(element.getAttributeValue("datum"));
    	setSpecialSemantics(element.getChildText("special-semantics"));
    	setRemarks(element.getChildText("remarks"));
    	
    	markImported();
    	save();
    }

    private boolean _deleted = false;
    private boolean _imported = false;
    private String _specialSemantics;
    private String _remarks;
    private Variety _variety;
    private final Entry _entry;
}
