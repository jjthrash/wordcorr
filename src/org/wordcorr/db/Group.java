package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a grouping of tagged data in the database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class Group extends AbstractPersistent {

    public Group(Database db, long id, View view, Entry entry) {
        super(db, id);
        _view = view;
        _entry = entry;
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
     * Get the entry.
     **/
    public Entry getEntry() {
        return _entry;
    }

    /**
     * Get the reconstruction.
     **/
    public String getReconstruction() {
        return _reconstruction;
    }

    /**
     * Set the value of reconstruction.
     * @param v Value to assign to reconstruction.
     **/
    public void setReconstruction(String v) {
        _reconstruction = v;
        setDirty();
    }

    /**
     * Get the Frantz cluster.
     **/
    public double getFrantzCluster() {
        return _frantzCluster;
    }

    /**
     * Set the value of Frantz cluster.
     * @param v Value to assign to Frantz cluster.
     **/
    public void setFrantzCluster(double v) {
        _frantzCluster = v;
        setDirty();
    }

    /**
     * Get the Frantz protosegment.
     **/
    public double getFrantzProtosegment() {
        return _frantzProtosegment;
    }

    /**
     * Set the value of Frantz protosegment.
     * @param v Value to assign to Frantz protosegment.
     **/
    public void setFrantzProtosegment(double v) {
        _frantzProtosegment = v;
        setDirty();
    }

    /**
     * Get the value of all citations associated with residue protosegment.
     **/
    public boolean allCitationsWithResidue() {
        return _allCitationsWithResidue;
    }

    /**
     * Set the value of all citations associated with residue protosegment.
     * @param v Value to assign to all citations associated with residue protosegment.
     **/
    public void setAllCitationsWithResidue(boolean v) {
        _allCitationsWithResidue = v;
    }

    /**
     * Get the value of done.
     **/
    public boolean isDone() {
        return _done;
    }

    /**
     * Set the value of done.
     * @param v Value to assign to done.
     **/
    public void setDone(boolean v) {
        _done = v;
    }

    /**
     * Get citations of this group.
     **/
    public List getCitations() throws DatabaseException {
        // get correspondence set citations
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_CITATIONS_BY_GROUP";
            }
            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Citation citation = new Citation(db, rs.getLong(1), null, Group.this);
                citation.updateObject(rs);
                return citation;
            }
        });
    }

    /**
     * Retabulate Group by removing all Citations and setting done to false.
     * Parent Correspondence Set is removed if not referenced by other Citations.
     * Parent Cluster (of deleted Correspondence Set) is removed if not
     * referenced by other Correspondence Sets.
     **/
    public void retabulate() throws DatabaseException {
        // loop through citations
        List citations = getCitations();
        for (Iterator it = getCitations().iterator(); it.hasNext();) {
            Citation citation = (Citation) it.next();

            // remove unreferenced correspondence set
            CorrespondenceSet cset = citation.getCorrespondenceSet();
            if (cset.getCitations().size() <= 1) {
                // remove unreferenced cluster
                Cluster cluster = cset.getCluster();
                if (cluster.getCorrespondenceSets().size() <= 1) {
                    Protosegment protosegment = cluster.getProtosegment();
                    cluster.delete();
                    protosegment.reorderClusterOrder();
                }
                cset.delete();
            }
            // remove citation
            citation.delete();
        }

        // Set done to false.
        setDone(false);
        save();
    }

    /**
     * Get element representing this group.
     **/
    public Element getElement() {
        Element element = new Element("group");

        // set attributes
        element.setAttribute("entry-number", getEntry().getEntryNum() + "");
        element.setAttribute("tag", getName());
        element.setAttribute("tabulated", isDone() ? "true" : "false");

        return element;
    }

    /**
     * Get summary element representing this group.
     **/
    public Element getSummaryElement(
        View view,
        String displayFrantz,
        String gloss) {
        Element element = new Element("group");

        // set attributes
        element.setAttribute("reconstruction", getReconstruction());
        String frantzStrength =
            displayFrantz.equals("Clusters")
                ? getFrantzCluster() + ""
                : displayFrantz.equals("Protosegments")
                ? getFrantzProtosegment() + ""
                : getFrantzCluster() + ", " + getFrantzProtosegment();
        element.setAttribute("frantz-strength", frantzStrength);
        Entry entry = getEntry();
        element.setAttribute(
            "gloss",
            gloss.equals("Primary")
                || entry.getGloss2().equals("") ? entry.getName() : entry.getGloss2());

        try {
            // datums
            String prevDatum = "";
            List uniqueDatums = new ArrayList();
            DatumVarieties datumVarieties = null;
            // get list of unique datums
            for (Iterator it = entry.getSummaryDatums(view, this).iterator(); it.hasNext();) {
                Datum datum = (Datum) it.next();
                if (!prevDatum.equals(datum.getName())) {
                    prevDatum = datum.getName();
                    datumVarieties = new DatumVarieties(datum.getName());
                    uniqueDatums.add(datumVarieties);
                }
                datumVarieties.addVariety(
                    datum.getVariety().getShortName()
                        + (datum.getSpecialSemantics().equals("")
                            ? ""
                            : " " + '"' + datum.getSpecialSemantics() + '"'));
            }
            // sort by number of varieties
            Collections.sort(uniqueDatums, new Comparator() {
                public int compare(Object o1, Object o2) {
                    int i1 = ((DatumVarieties) o1).getSize();
                    int i2 = ((DatumVarieties) o2).getSize();
                    return i1 > i2 ? -1 : i1 < i2 ? 1 : 0;
                }
            });
            // output
            for (Iterator it = uniqueDatums.iterator(); it.hasNext();) {
                datumVarieties = (DatumVarieties) it.next();
                // set elements
                Element elem = createElement("datum-varieties", datumVarieties.getVarieties());
                elem.setAttribute("datum", datumVarieties.getDatum());
                element.addContent(elem);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
    * Datum varieties.
    **/
    private class DatumVarieties {
        public DatumVarieties(String datum) {
            _datum = datum;
        }

        /*
        * Add to variety list.
        **/
        public void addVariety(String val) {
            _size++;
            if (_varieties == null)
                _varieties = val;
            else
                _varieties += " " + val;
        }

        /**
        * Get datum.
        **/
        public String getDatum() {
            return _datum;
        }

        /**
        * Get variety list.
        **/
        public String getVarieties() {
            return _varieties;
        }

        /**
        * Get size.
        **/
        public int getSize() {
            return _size;
        }

        private int _size = 0;
        private String _datum = "";
        private String _varieties = null;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//
    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        _done = rs.getInt(3) != 0;
    } /**
    * Set parameters on the update statement.
    **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getReconstruction());
        stmt.setDouble(3, getFrantzCluster());
        stmt.setDouble(4, getFrantzProtosegment());
        stmt.setInt(5, _allCitationsWithResidue ? 1 : 0);
        stmt.setInt(6, _done ? 1 : 0);
        stmt.setLong(7, getID());
    } /**
    * Set parameters on the create statement.
    **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setInt(2, _done ? 1 : 0);
        stmt.setLong(3, _view.getID());
        stmt.setLong(4, _entry.getID());
    }

    private boolean _allCitationsWithResidue = false;
    private boolean _done = false;
    private final View _view;
    private final Entry _entry;
    private String _reconstruction;
    private double _frantzCluster;
    private double _frantzProtosegment;
}