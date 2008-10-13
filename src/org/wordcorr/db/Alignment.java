package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an alignment entry in the database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Alignment extends AbstractPersistent {

    public static final char INDEL_SYMBOL   = '/';
    public static final char EXCLUDE_SYMBOL = '.';
    public static final char GRAPHEME_CLUSTER_END = '}';
    public static final char GRAPHEME_CLUSTER_START = '{';
    public static final char HOLD_SYMBOL    = 'x';

    Alignment(Database db, long id, View view, Datum datum) {
        super(db, id);
        _view = view;
        _datum = datum;

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < datum.getName().length(); i++) {
            buf.append(HOLD_SYMBOL);
        }
        _vector = buf.toString();
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the value of group.
     **/
    public Group getGroup() {
        return _group;
    }

    /**
     * Set the value of group.
     * @param v Value to assign to group.
     **/
    public void setGroup(Group v) {
        _group = v;
    }

    /**
     * Get the value of vector.
     **/
    public String getVector() {
        return (_vector == null) ? "" : _vector;
    }

    /**
     * Set the value of vector.
     * @param v Value to assign to vector.
     **/
    public void setVector(String v) {
        _vector = v;
        setDirty();
    }

    /**
     * Get the text representation of metathesis composite.
     **/
    public String getMetathesis() {
    	if ((_metathesis1 == null) ||
    		(_metathesis2 == null) ||
    		(_length1 == null) ||
    		(_length2 == null))
    		return "";
    	
        return _metathesis1.intValue() + ", " +
        	_length1.intValue() + ", " +
        	_metathesis2.intValue() + ", " +
        	_length2.intValue();
    }

    /**
     * Parse text representation of metathesis composite and set components.
     **/
    public void setMetathesis(String metathesis) {
   		Integer zero = new Integer(0);
    	if (metathesis == null || metathesis.equals("")) {
    		setMetathesis1(zero);
    		setLength1(zero);
    		setMetathesis2(zero);
    		setLength2(zero);
    		return;
    	}

		// metathesis1    	
    	int from = 0;
    	int to = metathesis.indexOf(",", from);
    	String val = metathesis.substring(from, to).trim();
    	from = to;
   		setMetathesis1((val.equals("")) ? zero : new Integer(val));

		// length1
    	from = to + 1;
    	to = metathesis.indexOf(",", from);
    	val = metathesis.substring(from, to).trim();
   		setLength1((val.equals("")) ? zero : new Integer(val));

		// metathesis2    	
    	from = to + 1;
    	to = metathesis.indexOf(",", from);
    	val = metathesis.substring(from, to).trim();
    	from = to;
   		setMetathesis2((val.equals("")) ? zero : new Integer(val));

		// length2
    	from = to + 1;
    	val = metathesis.substring(from).trim();
   		setLength2((val.equals("")) ? zero : new Integer(val));
    }

    /**
     * Get the value of metathesis1.
     **/
    public Integer getMetathesis1() {
        return _metathesis1;
    }

    /**
     * Compute and set metathesis and length values
     * using simple metathesis (n, 1, n + 1, 1).
     * @param metathesis1 Value to assign to metathesis1.
     **/
    public void setSimpleMetathesis(int metathesis1) {
    	if (metathesis1 > 0) {
    		Integer one = new Integer(1);
    		_metathesis1 = new Integer(metathesis1);
    		_length1 = one;
    		_metathesis2 = new Integer(metathesis1 + 1);
    		_length2 = one;
    	} else {
    		Integer zero = new Integer(0);
    		_metathesis1 = zero;
    		_length1 = zero;
    		_metathesis2 = zero;
    		_length2 = zero;
    	}
    }

    /**
     * Set the value of metathesis1.
     * @param v Value to assign to metathesis1.
     **/
    public void setMetathesis1(Integer v) {
        _metathesis1 = v;
        setDirty();
    }

    /**
     * Get the value of metathesis2.
     **/
    public Integer getMetathesis2() {
        return _metathesis2;
    }

    /**
     * Set the value of metathesis2.
     * @param v Value to assign to metathesis2.
     **/
    public void setMetathesis2(Integer v) {
        _metathesis2 = v;
        setDirty();
    }

    /**
     * Get the value of observations.
     **/
    public String getObservations() {
        return (_observations == null) ? "" : _observations;
    }

    /**
     * Set the value of observations.
     * @param v Value to assign to observations.
     **/
    public void setObservations(String v) {
        _observations = v;
        setDirty();
    }

    /**
     * Get the value of length1.
     **/
    public Integer getLength1() {
        return _length1;
    }

    /**
     * Set the value of length1.
     * @param v Value to assign to length1.
     **/
    public void setLength1(Integer v) {
        _length1 = v;
        setDirty();
    }

    /**
     * Get the value of length2.
     **/
    public Integer getLength2() {
        return _length2;
    }

    /**
     * Set the value of length2.
     * @param v Value to assign to length2.
     **/
    public void setLength2(Integer v) {
        _length2 = v;
        setDirty();
    }

    /**
     * Get the view.
     **/
    public View getView() {
        return _view;
    }

    /**
     * Get the datum.
     **/
    public Datum getDatum() {
        return _datum;
    }

    /**
     * Get the aligned datum.
     **/
    public String getAlignedDatum() {
        return getDatum().fuseWithAlignment(getVector());
    }

    /**
     * Get the aligned datum in character List to account for grapheme clusters.
     **/
    public List getAlignedDatumList() {
    	String alignedDatum = getAlignedDatum();
    	ArrayList alignedDatumList = new ArrayList();
    	
        boolean gc = false;
        String gcvalue = "";
        int pos = 0;
        int width = 0;
        for (int i = 0; i < alignedDatum.length(); i++) {
        	String ch = alignedDatum.charAt(i) + "";
            switch (alignedDatum.charAt(i)) {
                case Alignment.GRAPHEME_CLUSTER_START :
                	gc = true;
                    break;
                case Alignment.GRAPHEME_CLUSTER_END :
               		alignedDatumList.add(Alignment.GRAPHEME_CLUSTER_START + gcvalue
               			+ Alignment.GRAPHEME_CLUSTER_END);
                    gc = false;
                    gcvalue = "";
                    break;
                default :
                	if (!gc) {
                   		alignedDatumList.add(ch);
                	} else {
                		// grapheme cluster
                		gcvalue += ch;
                	}
                    break;
            }
        }
    	
        return alignedDatumList;
    }

    /**
     * Get the metathetically aligned datum in character List to account for grapheme clusters.
     **/
    public List getMetatheticallyAlignedDatumList() {
    	List datum = getAlignedDatumList();
    	// check properties
    	if (_metathesis1 == null || _metathesis2 == null
    		|| _length1 == null || _length2 == null
    		|| _metathesis1.intValue() < 1 || _metathesis2.intValue() < 1
    		|| _length1.intValue() < 1 || _length1.intValue() < 1)
			return datum;
		
    	// transpose
    	int i1 = _metathesis1.intValue() - 1;
    	int i2 = _metathesis2.intValue() - 1;
    	int l1 = _length1.intValue();
    	int l2 = _length2.intValue();
    	if (i1 > i2) {
    		i1 = i2;
    		l1 = l2;
    		i2 = _metathesis1.intValue() - 1;
    		l2 = _length1.intValue();
    	}
    	Object[] objs1 = datum.toArray();
    	int pos = i1;
    	for (int i = i2; i < i2 + l2; i++)
    		datum.set(pos++, objs1[i]);
    	for (int i = i1 + l1; i < i2; i++)
    		datum.set(pos++, objs1[i]);
    	for (int i = i1; i < i1 + l1; i++)
    		datum.set(pos++, objs1[i]);
        return datum;
    }

    /**
     * Get element representing this alignment.
     **/
    public Element getElement() {
    	Element element = new Element("annotated-datum");
    	
    	// set attributes
    	Datum datum = getDatum();
    	Entry entry = datum.getEntry();
    	element.setAttribute("entry-number", entry.getEntryNum() + "");
    	element.setAttribute("tag", getGroup().getName());
    	element.setAttribute("datum-number", datum.getID() + "");
    	element.setAttribute("vector", getVector());
    	element.setAttribute("metathesis", getMetathesis());
    	// set elements
    	element.addContent(createElement("observations", getObservations()));
    	
    	return element;
    }

    /**
     * Display the value of alignment.
     **/
    public String toString() {
        return getAlignedDatum();
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        _vector = rs.getString(3);
        _metathesis1 = getInt(rs, 4);
        _length1 = getInt(rs, 5);
        _metathesis2 = getInt(rs, 6);
        _length2 = getInt(rs, 7);
        _observations = rs.getString(8);
        long gid = rs.getLong(9);
        if (rs.wasNull()) {
            _group = null;
        } else {
            try {
                _group = new Group(getDatabase(), gid, _view, _datum.getEntry());
                _group.revert();
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
        stmt.setString(1, _vector);
        setInt(stmt, 2, _metathesis1);
        setInt(stmt, 3, _length1);
        setInt(stmt, 4, _metathesis2);
        setInt(stmt, 5, _length2);
        stmt.setString(6, _observations);
        if (_group == null || _group.getID() == -1) {
            stmt.setNull(7, Types.BIGINT);
        } else {
            stmt.setLong(7, _group.getID());
        }
        stmt.setLong(8, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, _vector);
        setInt(stmt, 2, _metathesis1);
        setInt(stmt, 3, _length1);
        setInt(stmt, 4, _metathesis2);
        setInt(stmt, 5, _length2);
        stmt.setString(6, _observations);
        if (_group == null || _group.getID() == -1) {
            stmt.setNull(7, Types.BIGINT);
        } else {
            stmt.setLong(7, _group.getID());
        }
        stmt.setLong(8, _datum.getID());
        stmt.setLong(9, _view.getID());
        stmt.setLong(10, _datum.getVariety().getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(Element element) throws DatabaseException {
    	if (element == null)
    		return;

    	// set values
    	setVector(element.getAttributeValue("vector"));
    	setMetathesis(element.getAttributeValue("metathesis"));
    	setObservations(element.getChildText("observations"));

    	save();
    }

    private Integer _metathesis1;
    private Integer _metathesis2;
    private Integer _length1;
    private Integer _length2;
    private String _vector;
    private String _observations;
    private Group _group;
    private final Datum _datum;
    private final View _view;
}
