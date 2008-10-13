package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a citation entry in the database.
 * @author Jim Shiba
 **/
public class Citation extends AbstractPersistent {

    Citation(Database db, long id, CorrespondenceSet correspondenceSet, Group group) {
        super(db, id);
        _correspondenceSet = correspondenceSet;
        _group = group;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the correspondence set.
     **/
    public CorrespondenceSet getCorrespondenceSet() {
        return _correspondenceSet;
    }

    /**
     * Set the value of correspondence set.
     * @param v Value to assign to correspondence set.
     **/
    public void setCorrespondenceSet(CorrespondenceSet v) {
        _correspondenceSet = v;
    }

    /**
     * Get the group.
     **/
    public Group getGroup() {
        return _group;
    }

    /**
     * Get the value of position.
     **/
    public Integer getPosition() {
        return _position;
    }

    /**
     * Set the value of position.
     * @param v Value to assign to position.
     **/
    public void setPosition(Integer v) {
        _position = v;
    }

    /**
     * Get element representing this citation.
     **/
    public Element getElement() {
    	Element element = new Element("citation");

		// get group tag and entry key
		try {
	        GroupData data = getGroupData();
	    	// set attributes
	    	element.setAttribute("entry-number", data.getEntryID() + "");
	    	element.setAttribute("tag", data.getTag());
	    	Integer position = getPosition();
	    	element.setAttribute("glyph-position",
	    		(position == null) ? "" : position.intValue() + "");
		} catch (DatabaseException e) {
			e.printStackTrace();
			return element;
		}
    	
    	return element;
    }

    /**
     * Get group data.
     **/
    public GroupData getGroupData() throws DatabaseException {
    	List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_GROUP_DATA";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException
                {
                    stmt.setLong(1, _groupID);
                }

                public Object createObject(Database db, ResultSet rs)
                    throws SQLException
                {
                    return new GroupData(rs.getString(1), rs.getLong(2));
                }
            });
		return (GroupData)list.get(0);
    }
		        
     /**
     * Group data class.
     **/
    public static class GroupData {
    	
    	public GroupData(String tag, long entryID) {
    		_tag = tag;
    		_entryID = entryID;
    	}
    	
    	public long getEntryID() {
    		return _entryID;
    	}
    	
    	public String getTag() {
    		return _tag;
    	}
    	
    	private long _entryID;
    	private String _tag;
    }
    
    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
    	_correspondenceSetID = rs.getLong(2);
    	_groupID = rs.getLong(3);
        _position = getInt(rs, 4);
        
        // load correspondence set if null
        if (_correspondenceSet == null) {
	        try {
		    	_correspondenceSet = new CorrespondenceSet(getDatabase(), _correspondenceSetID, null);
		    	_correspondenceSet.revert();
	        } catch (DatabaseException e) {
	            e.printStackTrace();
	            throw new SQLException(e.getRootCause().getMessage());
	        }
        }
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        setInt(stmt, 1, _position);
        stmt.setLong(2, _correspondenceSet.getID());
        stmt.setLong(3, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        setInt(stmt, 1, _position);
        stmt.setLong(2, _correspondenceSet.getID());
        stmt.setLong(3, _group.getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(Element element) throws DatabaseException {
    	if (element == null)
    		return;

    	// set values
    	setPosition(new Integer(element.getAttributeValue("glyph-position")));
    	
    	save();
    }

    private long _correspondenceSetID;
    private long _groupID;
	private Integer _position;
    private CorrespondenceSet _correspondenceSet;
    private Group _group;
}
