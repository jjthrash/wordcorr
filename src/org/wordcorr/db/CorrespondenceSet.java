package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a correspondence set entry in the database.
 * @author Jim Shiba
 **/
public class CorrespondenceSet extends AbstractPersistent {

    public CorrespondenceSet(Database db, long id, Cluster cluster) {
        super(db, id);
        _cluster = cluster;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the cluster.
     **/
    public Cluster getCluster() {
        return _cluster;
    }

    /**
     * Set the value of cluster.
     * @param v Value to assign to cluster.
     **/
    public void setCluster(Cluster v) {
        _cluster = v;
    }

    /**
     * Get the value of correspondence set.
     **/
    public String getSet() {
        return (_set == null) ? "" : _set;
    }

    /**
     * Set the value of correspondence set.
     * @param v Value to assign to correspondence set.
     **/
    public void setSet(String v) {
        _set = v;
        _setCharacters = stringToCharacters(_set);
        
        setVarietyCount();
    }

    /**
     * Get the value of variety count.
     **/
    public Integer getVarietyCount() {
        return _varietyCount;
    }

    /**
     * Compute and set the value of variety count.
     **/
    protected void setVarietyCount() {
        // count number of ignores
        int cnt = 0;
        for (int i = 0; i < _set.length(); i++) {
            if (_set.charAt(i) == '.')
                cnt++;
        }
        _varietyCount = new Integer(_setCharacters.size() - cnt);
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
    }

    /**
     * Append to the value of remarks.
     * @param v Value to append to remarks.
     **/
    public void appendRemarks(String v) {
        if (v != null && !v.trim().equals(""))
            _remarks += "\n " + v;
    }

    /**
     * Get the value of order.
     **/
    public Integer getOrder() {
        return _order;
    }

    /**
     * Set the value of order.
     * @param v Value to assign to order.
     **/
    public void setOrder(Integer v) {
        _order = v;
    }

    /**
     * Get citations of this correspondence set.
     **/
    public List getCitations() throws DatabaseException {
        // get correspondence set citations
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_CITATIONS";
            }
            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Citation citation =
                    new Citation(db, rs.getLong(1), CorrespondenceSet.this, null);
                citation.updateObject(rs);
                return citation;
            }
        });
    }

    /**
     * Get element representing this correspondence set.
     **/
    public Element getElement() {
        Element element = new Element("correspondence-set");

        // set attributes
        Integer varietyCount = getVarietyCount();
        int glyphCount = _setCharacters.size();
        element.setAttribute(
            "ignore-count",
            (varietyCount == null) ? "" : glyphCount - varietyCount.intValue() + "");
        element.setAttribute("glyph-count", glyphCount + "");
        element.setAttribute(
            "order",
            (getOrder() == null) ? "" : getOrder().intValue() + "");
        // set elements
        element.addContent(createElement("glyph-string", getSet()));
        element.addContent(createElement("remarks", getRemarks()));
        try {
            for (Iterator it = getCitations().iterator(); it.hasNext();) {
                Citation citation = (Citation) it.next();
                element.addContent(citation.getElement());
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Make an Citation object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Citation makeCitation(Group group) {
        return new Citation(
            getDatabase(),
            AbstractPersistent.UNDEFINED_ID,
            this,
            group);
    }

    /**
     * Check to see if correspondence set is conformable (exclude ignores) with input set.
     * @param set Value to compare to.
     **/
    public boolean isConformable(String set) {
        List set1 = _setCharacters;
        List set2 = stringToCharacters(set);

        // check lengths
        if (set1.size() != set2.size())
            return false;

        // check each character
        for (int i = 0; i < set1.size(); i++) {
            String ch1 = (String) set1.get(i);
            String ch2 = (String) set2.get(i);
            // skip ignores
            if (ch1.equals(".") || ch2.equals("."))
                continue;
            // check character
            if (ch1.equals(ch2))
                continue;
            return false;
        }
        return true;
    }

    /**
     * Break set string to List of characters to account for grapheme clusters.
     * @param val Value to convert.
     **/
    private List stringToCharacters(String val) {
        List chars = new ArrayList();
        String gc = "";
        boolean gc_build = false;
        for (int i = 0; i < val.length(); i++) {
            String ch = val.substring(i, i + 1);
            switch (val.charAt(i)) {
                case Alignment.GRAPHEME_CLUSTER_START :
                    gc_build = true;
                    break;
                case Alignment.GRAPHEME_CLUSTER_END :
                    chars.add(gc);
                    gc = "";
                    gc_build = false;
                    break;
                default :
                    if (!gc_build) {
                        chars.add(ch);
                    } else {
                        // build grapheme cluster
                        gc += ch;
                    }
                    break;
            }
        }
        return chars;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        _clusterID = rs.getLong(2);
        _set = rs.getString(3);
        _setCharacters = stringToCharacters(_set);
        _varietyCount = getInt(rs, 4);
        _remarks = rs.getString(5);
        _order = getInt(rs, 6);

        // load cluster if null
        if (_cluster == null) {
            try {
                _cluster = new Cluster(getDatabase(), _clusterID, null);
                _cluster.revert();
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
        stmt.setString(1, _set);
        setInt(stmt, 2, _varietyCount);
        stmt.setString(3, _remarks);
        setInt(stmt, 4, _order);
        stmt.setLong(5, _cluster.getID());
        stmt.setLong(6, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, _set);
        setInt(stmt, 2, _varietyCount);
        stmt.setString(3, _remarks);
        setInt(stmt, 4, _order);
        stmt.setLong(5, _cluster.getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(
        Element element,
        Map groups,
        Set tabulatedGroups)
        throws DatabaseException {
        if (element == null)
            return;

        // get variety count
        /* Note:  Possible future use for data verification.	
        		int glyphCount = Integer.parseInt(element.getAttributeValue("glyph-count"));
        		int ignoreCount = Integer.parseInt(element.getAttributeValue("ignore-count"));
        		int varietyCount = glyphCount - ignoreCount;
        */

        // set values
        setSet(element.getChildText("glyph-string"));
        setOrder(new Integer(element.getAttributeValue("order")));
        setRemarks(element.getChildText("remarks"));

        save();

        // citations
        for (Iterator it = element.getChildren("citation").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            // get group
            String entryNumber = elem.getAttributeValue("entry-number");
            String tag = elem.getAttributeValue("tag");
            String groupKey = entryNumber + ":" + tag;
            Group group = (Group) groups.get(groupKey);
            // create citation
            Citation citation = makeCitation(group);
            citation.generateFromElement(elem);
            // mark group as tabulated
            tabulatedGroups.add(group);
        }
    }

    private long _clusterID;
    private Integer _order;
    private Integer _varietyCount;
    private String _remarks;
    private String _set;
    private List _setCharacters;
    private Cluster _cluster;
}