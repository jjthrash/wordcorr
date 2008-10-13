package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a cluster entry in the database.
 * @author Jim Shiba
 **/
public class Cluster extends AbstractPersistent {

    Cluster(Database db, long id, Protosegment protosegment) {
        super(db, id);
        _protosegment = protosegment;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the protosegment.
     **/
    public Protosegment getProtosegment() {
        return _protosegment;
    }

    /**
     * Set the value of protosegment.
     * @param v Value to assign to protosegment.
     **/
    public void setProtosegment(Protosegment v) {
        _protosegment = v;
    }

    /**
     * Get the value of environment.
     **/
    public String getEnvironment() {
        return (_environment == null) ? "" : _environment;
    }

    /**
     * Set the value of environment.
     * @param v Value to assign to environment.
     **/
    public void setEnvironment(String v) {
        _environment = v;
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
     * Display the value of cluster.
     **/
    public String toString() {
        return _environment + " : " + _order;
    }

    /**
     * Get correspondence set.
     * @param correspondence set id
     **/
    public CorrespondenceSet getCorrespondenceSet(final long id)
        throws DatabaseException {
        CorrespondenceSet correspondenceSet =
            new CorrespondenceSet(getDatabase(), id, Cluster.this);
        correspondenceSet.revert();
        return correspondenceSet;
    }

    /**
     * Get correspondence sets of this cluster.
     **/
    public List getCorrespondenceSets() throws DatabaseException {
        // get cluster correspondence sets
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_CORRESPONDENCE_SETS";
            }
            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                CorrespondenceSet set = new CorrespondenceSet(db, rs.getLong(1), Cluster.this);
                set.updateObject(rs);
                return set;
            }
        });
    }

    /**
     * Get maximum correspondence set order.
     **/
    public int getMaxCorrespondenceSetOrder() throws DatabaseException {
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_MAX_CORRESPONDENCE_SET_ORDER";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return ((Integer) list.get(0)).intValue();
    }

    /**
     * Get number of citations.
     **/
    public int getCitationCount() throws DatabaseException {
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_CLUSTER_CITATION_COUNT";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return ((Integer) list.get(0)).intValue();
    }

    /**
     * Get element representing this cluster.
     **/
    public Element getElement() {
        Element element = new Element("cluster");

        // set attributes
        element.setAttribute("environment", getEnvironment());
        Integer order = getOrder();
        element.setAttribute(
            "cluster-order",
            (order == null) ? "" : order.intValue() + "");
        // set elements
        element.addContent(createElement("remarks", getRemarks()));
        try {
            for (Iterator it = getCorrespondenceSets().iterator(); it.hasNext();) {
                CorrespondenceSet set = (CorrespondenceSet) it.next();
                element.addContent(set.getElement());
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Get summary element representing this cluster.
     **/
    public Element getSummaryElement(
        View view,
        float minFrantz,
        int maxReconstructions,
        String displayFrantz,
        boolean includeResidue,
        String gloss) {
        Element element = new Element("cluster");

        // set attributes
        element.setAttribute("environment", getEnvironment());
        Integer order = getOrder();
        element.setAttribute(
            "cluster-order",
            (order == null) ? "" : order.intValue() + "");
        try {
            // groups
            int i = 0;
            for (Iterator it =
                view.getSummaryClusterGroups(this, minFrantz, displayFrantz).iterator();
                it.hasNext();
                ) {
                // check maximum number of reconstructions per cluster
                if (++i > maxReconstructions)
                    break;
                Group group = (Group) it.next();
                element.addContent(group.getSummaryElement(view, displayFrantz, gloss));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Add correspondence set to existing sets if identical or conformable.
     * @param fromSet Correspondence set to add.
     **/
    public boolean addCorrespondenceSet(CorrespondenceSet fromSet)
        throws DatabaseException {
        Cluster fromCluster = fromSet.getCluster();

        // check for identical set to move citations to
        boolean conformable = false;
        List toSets = this.getCorrespondenceSets();
        for (Iterator its = toSets.iterator(); its.hasNext();) {
            CorrespondenceSet toSet = (CorrespondenceSet) its.next();
            // check for identical set (include ignores)
            if (fromSet.getSet().equals(toSet.getSet())) {
                // move citations
                for (Iterator itc = fromSet.getCitations().iterator(); itc.hasNext();) {
                    Citation citation = (Citation) itc.next();
                    citation.setCorrespondenceSet(toSet);
                    citation.save();
                }
                // delete correspondence set after appending remarks
                toSet.appendRemarks(fromSet.getRemarks());
                toSet.save();
                fromSet.delete();
                // delete cluster if empty
                if (fromCluster.getCorrespondenceSets().isEmpty()) {
                    this.appendRemarks(fromCluster.getRemarks());
                    this.save();
                    Protosegment fromProtosegment = fromCluster.getProtosegment();
                    fromCluster.delete();
                    fromProtosegment.reorderClusterOrder();
                }
                return true;
                // check for nonconformable set (exclude ignores)
            } else if (!toSet.isConformable(fromSet.getSet())) {
                conformable = false;
                break;
            }
            conformable = true;
        }

        if (conformable) {
            // move correspondence set
            fromSet.setCluster(this);
            fromSet.save();
            // delete cluster if empty
            if (fromCluster.getCorrespondenceSets().isEmpty()) {
                this.appendRemarks(fromCluster.getRemarks());
                this.save();
                Protosegment fromProtosegment = fromCluster.getProtosegment();
                fromCluster.delete();
                fromProtosegment.reorderClusterOrder();
            }
            return true;
        }
        return false;
    }

    /**
     * Checks to see if idential or conformable correspondence set exists.
     * @param fromSet Correspondence set to add.
     **/
    public boolean hasConformable(CorrespondenceSet fromSet)
        throws DatabaseException {
        List toSets = this.getCorrespondenceSets();
        for (Iterator its = toSets.iterator(); its.hasNext();) {
            CorrespondenceSet toSet = (CorrespondenceSet) its.next();
            // check for identical set (include ignores)
            // or check for nonconformable set (exclude ignores)
            if (fromSet.getSet().equals(toSet.getSet())
                || toSet.isConformable(fromSet.getSet())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a CorrespondenceSet object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public CorrespondenceSet makeCorrespondenceSet() {
        return new CorrespondenceSet(
            getDatabase(),
            AbstractPersistent.UNDEFINED_ID,
            this);
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        _protosegmentID = rs.getLong(2);
        _environment = rs.getString(3);
        _remarks = rs.getString(4);
        _order = getInt(rs, 5);

        // load protosegment if null
        if (_protosegment == null) {
            try {
                _protosegment = new Protosegment(getDatabase(), _protosegmentID, null, null);
                _protosegment.revert();
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
        stmt.setString(1, _environment);
        stmt.setString(2, _remarks);
        setInt(stmt, 3, _order);
        stmt.setLong(4, _protosegment.getID());
        stmt.setLong(5, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, _environment);
        stmt.setString(2, _remarks);
        setInt(stmt, 3, _order);
        stmt.setLong(4, _protosegment.getID());
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

        // set values
        setEnvironment(element.getAttributeValue("environment"));
        setOrder(new Integer(element.getAttributeValue("cluster-order")));
        setRemarks(element.getChildText("remarks"));

        save();

        // correspondence sets
        for (Iterator it = element.getChildren("correspondence-set").iterator();
            it.hasNext();
            ) {
            Element elem = (Element) it.next();
            // create correspondence set
            CorrespondenceSet set = makeCorrespondenceSet();
            set.generateFromElement(elem, groups, tabulatedGroups);
        }
    }

    private long _protosegmentID;
    private Integer _order;
    private String _environment;
    private String _remarks;
    private Protosegment _protosegment;
}