package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.FontCache;

/**
 * Represents a protosegment entry in the database.
 * @author Jim Shiba
 **/
public class Protosegment extends AbstractPersistent {

    Protosegment(Database db, long id, View view, Zone zone) {
        super(db, id);
        _view = view;
        _zone = zone;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the view.
     **/
    public View getView() {
        return _view;
    }

    /**
     * Get the zone.
     **/
    public Zone getZone() {
        return _zone;
    }

    /**
     * Set the value of zone.
     * @param v Value to assign to zone.
     **/
    public void setZone(Zone v) {
        _zone = v;
    }

    /**
     * Get the value of protosegment.
     **/
    public String getProtosegment() {
        return (_protosegment == null) ? "" : _protosegment;
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
     * Set flag to display zone.
     * @param v Set display of zone with protosegment.
     **/
    public void setDisplayZone(boolean v) {
        _displayZone = v;
    }

    /**
     * Display the value of protosegment.
     **/
    public String toString() {
        if (HTMLout) {
            String font = "<font face=\""+FontCache.getIPA().getFontName()+"\">";
            return "<html>"+((_displayZone) ? _zone + font +" | " + _protosegment +"</font></html>": "html"+font+_protosegment+"</font></html>");
        }
        return (_displayZone) ? _zone + " | " + _protosegment : _protosegment;
    }
    
    /**
     * When set to true, toString will return a string with html formatting placing
     * ipa data inside an ipa font tag (for example, for displaying in a label).
     */
    public void setHTMLtoString(boolean _HTMLout) {
        HTMLout = _HTMLout;
    }

    /**
     * Set the value of protosegment.
     * @param v Value to assign to protosegment.
     **/
    public void setProtosegment(String v) {
        _protosegment = v;
    }

    /**
     * Get cluster.
     * @param cluster id
     **/
    public Cluster getCluster(final long id) throws DatabaseException {
        Cluster cluster = new Cluster(getDatabase(), id, Protosegment.this);
        cluster.revert();
        return cluster;
    }

    /**
     * Get clusters with identical environment values.
     * @param environment Value of environment.
     **/
    public List getEnvironmentClusters(final String environment)
        throws DatabaseException {
        // get clusters that have identical environments
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_ENVIRONMENT_CLUSTERS";
            }
            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setString(2, environment);
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Cluster cluster = new Cluster(db, rs.getLong(1), Protosegment.this);
                cluster.updateObject(rs);
                return cluster;
            }
        });
    }

    /**
     * Get clusters.
     **/
    public List getClusters() throws DatabaseException {
        // get clusters
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_CLUSTERS";
            }
            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Cluster cluster = new Cluster(db, rs.getLong(1), Protosegment.this);
                cluster.updateObject(rs);
                return cluster;
            }
        });
    }

    /**
     * Move clusters from another protosegment.
     * @param fromProtosegment Protosegment to move clusters from.
     **/
    public boolean moveClustersFromProtosegment(Protosegment fromProtosegment)
        throws DatabaseException {
        // move all clusters from protosegment
        boolean moveSuccess = false;
        List fromClusters = fromProtosegment.getClusters();
        for (Iterator it = fromClusters.iterator(); it.hasNext();) {
            Cluster fromCluster = (Cluster) it.next();
            // move cluster
            fromCluster.setProtosegment(this);
            fromCluster.setOrder(new Integer(this.getMaxClusterOrder() + 1));
            fromCluster.save();
            moveSuccess = true;
        }
        // reorder if moved at least one
        if (moveSuccess) {
            this.reorderClusterOrder();
        }
        return moveSuccess;
    }

    /**
     * Get maximum cluster order.
     **/
    public int getMaxClusterOrder() throws DatabaseException {
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_MAX_CLUSTER_ORDER";
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
     * Reorder cluster order.
     **/
    public void reorderClusterOrder() throws DatabaseException {
        List list = getClusters();
        int id = 0;
        for (Iterator it = list.iterator(); it.hasNext();) {
            Cluster cluster = (Cluster) it.next();
            if (cluster.getOrder().intValue() != ++id) {
                cluster.setOrder(new Integer(id));
                cluster.save();
            }
        }
    }

    /**
     * Get number of citations.
     **/
    public int getCitationCount() throws DatabaseException {
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_PROTOSEGMENT_CITATION_COUNT";
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
     * Get element representing this protosegment.
     **/
    public Element getElement() {
        Element element = new Element("protosegment");

        // set attributes
        element.setAttribute("symbol", getProtosegment());
        Zone zone = getZone();
        Integer row = zone.getRow();
        element.setAttribute("zone-row", (row == null) ? "" : row.intValue() + "");
        Integer column = zone.getColumn();
        element.setAttribute(
            "zone-column",
            (column == null) ? "" : column.intValue() + "");
        // set elements
        element.addContent(createElement("remarks", getRemarks()));
        try {
            List clusters = getClusters();
            if (!clusters.isEmpty()) {
                for (Iterator it = clusters.iterator(); it.hasNext();) {
                    Cluster cluster = (Cluster) it.next();
                    element.addContent(cluster.getElement());
                }
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
    * Get summary element representing this protosegment.
    **/
    public Element getSummaryElement(
        View view,
        float minFrantz,
        int maxReconstructions,
        String displayFrantz,
        boolean includeResidue,
        String gloss) {
        Element element = new Element("protosegment");

        // set attributes
        element.setAttribute("symbol", getProtosegment());
        Zone zone = getZone();
        Integer row = zone.getRow();
        element.setAttribute("zone-place-manner", zone.getName());
        try {
        	// clusters
            List clusters = getClusters();
            if (!clusters.isEmpty()) {
                for (Iterator it = clusters.iterator(); it.hasNext();) {
                    Cluster cluster = (Cluster) it.next();
                    element.addContent(
                        cluster.getSummaryElement(
                            view,
                            minFrantz,
                            maxReconstructions,
                            displayFrantz,
                            includeResidue,
                            gloss));
                }
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
      * Make a Cluster object. This does not add anything to the
      * database. The returned object must be saved to be made
      * permanent.
      **/
    public Cluster makeCluster() {
        return new Cluster(getDatabase(), AbstractPersistent.UNDEFINED_ID, this);
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Check validation prior to save.
     * Return null of okay, message if not.
     **/
    public String checkValidation() throws DatabaseException {
        // check for duplicate
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_PROTOSEGMENT_COUNT";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, _view.getID());
                stmt.setLong(2, _zone.getID());
                stmt.setString(3, _protosegment);
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return (((Integer) list.get(0)).intValue() > 0)
            ? AppPrefs.getInstance().getMessages().getString("msgProtosegmentValidation")
            : null;
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        _protosegment = rs.getString(3);
        _remarks = rs.getString(4);

        // load zone if null
        if (_zone == null) {
            try {
                _zone = new Zone(getDatabase(), rs.getLong(1));
                _zone.revert();
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
        stmt.setString(1, _protosegment);
        stmt.setString(2, _remarks);
        stmt.setLong(3, _zone.getID());
        stmt.setLong(4, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, _protosegment);
        stmt.setString(2, _remarks);
        stmt.setLong(3, _view.getID());
        stmt.setLong(4, _zone.getID());
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
        setProtosegment(element.getAttributeValue("symbol"));
        setRemarks(element.getChildText("remarks"));

        save();

        // clusters
        for (Iterator it = element.getChildren("cluster").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            // create cluster
            Cluster cluster = makeCluster();
            cluster.generateFromElement(elem, groups, tabulatedGroups);
        }
    }

    private boolean _displayZone = false;
    private String _protosegment;
    private String _remarks;
    private final View _view;
    private Zone _zone;
    private boolean HTMLout = false;
}