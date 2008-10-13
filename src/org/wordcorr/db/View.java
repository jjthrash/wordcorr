package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;

/**
 * Represents a user's view on a collection.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class View extends AbstractPersistent {

    View(Database db, long id, WordCollection collection) {
        super(db, id);
        _collection = collection;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get the value of threshold.
     **/
    public Integer getThreshold() {
        return _threshold;
    }

    /**
     * Get the numeric value of threshold based on number of varieties.
     **/
    public int getThresholdValue() {
        int threshold =
            new Double(Math.ceil(getThreshold().doubleValue() * _varieties.size() / 100))
                .intValue();
        return threshold;
    }

    /**
     * Set the value of threshold.
     * @param v Value to assign to threshold.
     **/
    public void setThreshold(Integer v) {
        _threshold = v;
        setDirty();
        _thresholdChanged = true;
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
     * Get the members of this view.
     * @return a list of Variety objects that are members of this
     * view, in order.
     **/
    public List getMembers() {
        return new ArrayList(_varieties);
    }

    /**
     * Set the members of this view.
     * @param varieties A list of variety objects to set for this
     * view, in order.
     **/
    public void setMembers(List varieties) {
        _varieties = new ArrayList(varieties);
        setDirty();
        _varietiesChanged = true;
    }

    /**
     * Get this view's collection.
     **/
    public WordCollection getCollection() {
        return _collection;
    }

    /**
     * Reset change flags.
     **/
    private void resetChangeFlags() {
        _thresholdChanged = false;
        _varietiesChanged = false;
    }

    /**
     * Delete this object.
     **/
    public void delete() throws DatabaseException {
        getDatabase().deleteObject(this);
    }

    /**
     * Delete this view's unused groups for the given entry.
     **/
    public void deleteUnusedGroups(final Entry entry) throws DatabaseException {
        getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "DELETE_UNUSED_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return null;
            }
        });
    }

    /**
     * Delete this view's unused protosegments except 'pro' and 'res'.
     **/
    public void deleteUnusedProtosegments() throws DatabaseException {
        getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "DELETE_UNUSED_PROTOSEGMENTS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return null;
            }
        });
    }

    /**
     * Get this view's alignments for the given entry.
     **/
    public List getAlignments(final Entry entry) throws DatabaseException {
        List existing = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_ALIGNMENTS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                try {
                    Datum datum = new Datum(db, rs.getLong(1), entry);
                    datum.revert();
                    Alignment alignment = new Alignment(db, rs.getLong(2), View.this, datum);
                    alignment.updateObject(rs);
                    return alignment;
                } catch (DatabaseException e) {
                    throw new SQLException(e.getRootCause().getMessage());
                }
            }
        });

        List allData = entry.getData();
        List ret = new ArrayList(existing);

        // create new items for each datum not in the result set
        // (unannotated datums)
        // O(n^2) for now
        for (Iterator it = allData.iterator(); it.hasNext();) {
            Datum datum = (Datum) it.next();

            boolean found = false;
            for (Iterator it2 = existing.iterator(); it2.hasNext();) {
                Alignment alignment = (Alignment) it2.next();

                if (alignment.getDatum().equals(datum)) {
                    found = true;
                    break;
                }
            }

            if (!found && getMembers().contains(datum.getVariety())) {
                ret.add(makeAlignment(datum));
            }
        }

        Collections.sort(ret, new Comparator() {
            public int compare(Object o1, Object o2) {
                Alignment a1 = (Alignment) o1;
                Alignment a2 = (Alignment) o2;

                int i1 = _varieties.indexOf(a1.getDatum().getVariety());
                int i2 = _varieties.indexOf(a2.getDatum().getVariety());

                return i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
            }
        });

        return ret;
    }

    /**
     * Get untabulated entries.
     **/
    public List getUntabulatedEntries() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_UNTABULATED_ENTRIES";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), _collection);
                entry.updateObject(rs);
                return entry;
            }
        });
    }

    /**
     * Get grapheme clusters.
     **/
    public List getGraphemeClusters() throws DatabaseException {
        List allData = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_GRAPHEME_CLUSTERS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        });

        // extract grapheme clusters
        List ret = new ArrayList();
        for (Iterator it = allData.iterator(); it.hasNext();) {
            String data = (String) it.next();
            int delim = data.indexOf(":");
            String vector = data.substring(0, delim);
            String rawDatum = data.substring(delim + 1);

            // extract grapheme cluster
            int pos = 0;
            int startpos = 0;
            for (int i = 0; i < vector.length(); i++) {
                switch (vector.charAt(i)) {
                    case Alignment.HOLD_SYMBOL :
                        pos++;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_START :
                        startpos = pos;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_END :
                        String cluster = rawDatum.substring(startpos, pos);
                        if (!ret.contains(cluster))
                            ret.add(cluster);
                        break;
                    default :
                        break;
                }
            }
        }

        Collections.sort(ret, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((String) o1).compareTo((String) o2);
            }
        });

        return ret;
    }

    /**
     * Undefine grapheme clusters.
     **/
    public void undefineGraphemeCluster(String graphemeCluster)
        throws DatabaseException {
        // construct search string
        final StringBuffer search = new StringBuffer(graphemeCluster.length() + 4);
        search.append("%" + Alignment.GRAPHEME_CLUSTER_START);
        for (int i = 0; i < graphemeCluster.length(); i++)
            search.append("_");
        search.append(Alignment.GRAPHEME_CLUSTER_END + "%");

        List allData = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_UNDEFINE_GRAPHEME_CLUSTERS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setString(2, search.toString());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return rs.getString(1);
            }
        });

        // extract grapheme clusters and compare
        for (Iterator it = allData.iterator(); it.hasNext();) {
            String data = (String) it.next();
            int delim1 = data.indexOf(":");
            int delim2 = data.indexOf(":", delim1 + 1);
            int delim3 = data.lastIndexOf(":");
            String vector = data.substring(0, delim1);
            String rawDatum = data.substring(delim1 + 1, delim2);
            long groupID = Long.parseLong(data.substring(delim2 + 1, delim3));
            long alignmentID = Long.parseLong(data.substring(delim3 + 1));

            // extract grapheme cluster
            boolean undefine = false;
            int pos = 0;
            int vectorpos = 0;
            int startpos = 0;
            for (int i = 0; i < vector.length(); i++) {
                switch (vector.charAt(i)) {
                    case Alignment.HOLD_SYMBOL :
                        pos++;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_START :
                        startpos = pos;
                        vectorpos = i;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_END :
                        String cluster = rawDatum.substring(startpos, pos);
                        if (cluster.equals(graphemeCluster)) {
                            // save new vector
                            vector =
                                vector.substring(0, vectorpos)
                                    + vector.substring(vectorpos + 1, i)
                                    + vector.substring(i + 1);

                            // reset
                            i = (i >= 2) ? i - 2 : 0;
                            vectorpos = i;
                            undefine = true;
                        }
                        break;
                    default :
                        break;
                }
            }

            // undefine
            if (undefine) {
                // update alignment vector
                final String updateVector = new String(vector);
                final long updateID = alignmentID;
                getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "UPDATE_ALIGNMENT_VECTOR";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setString(1, updateVector);
                        stmt.setLong(2, updateID);
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        return null;
                    }
                });

                // untabulate group
                Group group = new Group(getDatabase(), groupID, this, null);
                group.revert();
                group.retabulate();

                undefine = false;
            }
        }

    }

    /**
     * Get this view's groups for the given entry.
     **/
    public List getGroups(final Entry entry) throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Group group = new Group(db, rs.getLong(1), View.this, entry);
                group.updateObject(rs);
                return group;
            }
        });
    }

    /**
     * Get the group specified for given entry if it persists
     * or create a new one if it doesn't.
     **/
    public Group getGroup(String name, Entry entry) throws DatabaseException {
        List groups = getGroups(entry);
        for (Iterator it = groups.iterator(); it.hasNext();) {
            Group group = (Group) it.next();
            if (group.getName().equals(name))
                return group;
        }

        // create group
        Group newGroup = makeGroup(entry);
        newGroup.setName(name);
        newGroup.save();
        return newGroup;
    }

    /**
     * Get this view's groups that pass threshold for the given entry.
     **/
    public List getThresholdGroups(final Entry entry) throws DatabaseException {
        // get groups that meet or exceed threshold
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_THRESHOLD_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
                stmt.setInt(3, getThresholdValue());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Group group = new Group(db, rs.getLong(1), View.this, entry);
                group.updateObject(rs);
                return group;
            }
        });
        return groups;
    }

    /**
     * Get this view's groups that are below threshold requirements for the given entry.
     **/
    public List getBelowThresholdGroups(final Entry entry)
        throws DatabaseException {
        // get groups that are below the threshold
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_BELOW_THRESHOLD_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
                stmt.setInt(3, getThresholdValue());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Group group = new Group(db, rs.getLong(1), View.this, entry);
                group.updateObject(rs);
                return group;
            }
        });
        return groups;
    }

    /**
     * Get this view's groups that have been tabulated.
     **/
    public List getTabulatedGroups() throws DatabaseException {
        // get groups that have been tabulated
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_TABULATED_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), getCollection());
                entry.updateObject(rs);

                Group group = new Group(db, rs.getLong(5), View.this, entry);
                group.setName(rs.getString(6));
                group.setDone(rs.getInt(7) != 0);
                return group;
            }
        });
        return groups;
    }

    /**
     * Get this view's tabulated groups that are above threshold.
     **/
    public List getTabulatedThresholdGroups() throws DatabaseException {
        // get groups that have been tabulated
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_TABULATED_THRESHOLD_GROUPS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), getCollection());
                entry.updateObject(rs);

                Group group = new Group(db, rs.getLong(5), View.this, entry);
                group.setName(rs.getString(6));
                group.setDone(rs.getInt(7) != 0);
                return group;
            }
        });
        return groups;
    }

    /**
     * Get this view's Frantz Strength group count.  Defined as tabulated groups
     * that are above threshold and associated with non-residue protosegments.
     **/
    public int getFrantzStrengthGroupCount() throws DatabaseException {
        List count = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_FRANTZ_STRENGTH_GROUP_COUNT";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return ((Integer) count.get(0)).intValue();
    }

    /**
     * Get this view's groups for a cluster used for summary.
     **/
    public List getSummaryClusterGroups(
        final Cluster cluster,
        final double minFrantz,
        final String displayFrantz)
        throws DatabaseException {
        // get groups that have been tabulated
        List groups = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return displayFrantz.equals("Protosegment")
                    ? "GET_SUMMARY_CLUSTER_GROUPS_BY_FRANTZ_PROTO"
                    : "GET_SUMMARY_CLUSTER_GROUPS_BY_FRANTZ_CLUSTER";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, cluster.getID());
                stmt.setDouble(2, minFrantz);
            }
            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), getCollection());
                entry.updateObject(rs);

                Group group = new Group(db, rs.getLong(5), View.this, entry);
                group.setName(rs.getString(6));
                group.setDone(rs.getInt(7) != 0);
                group.setReconstruction(rs.getString(8));
                group.setFrantzCluster(rs.getDouble(9));
                group.setFrantzProtosegment(rs.getDouble(10));
                group.setAllCitationsWithResidue(rs.getInt(11) != 0);
                return group;
            }
        });
        return groups;
    }

    /**
     * Get protosegment.
     * @param protosegment id
     **/
    public Protosegment getProtosegment(final long id) throws DatabaseException {
        List list = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "org.wordcorr.db.Protosegment.RETRIEVE";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, id);
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                try {
                    Zone zone = new Zone(db, rs.getLong(1));
                    zone.revert();
                    Protosegment proto = new Protosegment(db, rs.getLong(2), View.this, zone);
                    proto.updateObject(rs);
                    return proto;
                } catch (DatabaseException e) {
                    throw new SQLException(e.getRootCause().getMessage());
                }
            }
        });
        return (list.isEmpty()) ? null : (Protosegment) list.get(0);
    }

    /**
     * Get this view's protosegments.
     **/
    public List getProtosegments() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_PROTOSEGMENTS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                try {
                    Zone zone = new Zone(db, rs.getLong(1));
                    zone.revert();
                    Protosegment proto = new Protosegment(db, rs.getLong(2), View.this, zone);
                    proto.updateObject(rs);
                    return proto;
                } catch (DatabaseException e) {
                    throw new SQLException(e.getRootCause().getMessage());
                }
            }
        });
    }

    /**
     * Get this view's unused protosegments.
     **/
    public List getUnusedProtosegments() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_UNUSED_PROTOSEGMENTS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                try {
                    Zone zone = new Zone(db, rs.getLong(1));
                    zone.revert();
                    Protosegment proto = new Protosegment(db, rs.getLong(2), View.this, zone);
                    proto.updateObject(rs);
                    return proto;
                } catch (DatabaseException e) {
                    throw new SQLException(e.getRootCause().getMessage());
                }
            }
        });
    }

    /**
     * Get this view's protosegments.
     **/
    public List getRefineTable() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_REFINE_TABLE";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Object[] objs =
                    {
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        new Integer(rs.getInt(4)),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10)};
                return objs;
            }
        });
    }

    /**
     * Get Groups Tabulated.
     **/
    public int getGroupsTabulated() throws DatabaseException {
        List count = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VIEW_GROUPS_TABULATED";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return ((Integer) count.get(0)).intValue();
    }

    /**
     * Get count of groups that are done.
     **/
    public int getGroupDoneCount(final Entry entry) throws DatabaseException {
        List count = getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_GROUP_DONE_COUNT";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, entry.getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                return new Integer(rs.getInt(1));
            }
        });
        return ((Integer) count.get(0)).intValue();
    }

    /**
     * Compute Frantz strenth for tabulated groups in view and update.
     **/
    public int updateFrantzStrength() throws DatabaseException {
        int frantzN = getFrantzStrengthGroupCount();
        List groups = getTabulatedThresholdGroups();

        // N minimum set to 50
        double n =
            Math.rint(
                ((Math.log(((frantzN < 50) ? 50 : frantzN) / 100.) / Math.log(10.)) * 21) + 9);
        for (Iterator it = groups.iterator(); it.hasNext();) {
            Group group = (Group) it.next();

            // init accumulators
            int pcount = 0;
            double acluster = 0.;
            double aproto = 0.;
            StringBuffer reconstruction = new StringBuffer();
            boolean allCitationsWithResidue = true;
            String grp = group.getEntry().getEntryNum() + group.getName();

            // process each citation
            for (Iterator itc = group.getCitations().iterator(); itc.hasNext();) {
                Citation citation = (Citation) itc.next();
                pcount++;
                Cluster cluster = citation.getCorrespondenceSet().getCluster();
                double ccluster = cluster.getCitationCount();
                Protosegment proto = cluster.getProtosegment();
                double cproto = proto.getCitationCount();
                reconstruction.append(proto.getProtosegment());

                acluster = getFrantzAccumulator(ccluster, acluster, n);
                aproto = getFrantzAccumulator(cproto, aproto, n);

                // check residue protosegments
                if (!proto.getZone().getAbbreviation().equals("RES"))
                    allCitationsWithResidue = false;
            }
            if (pcount > 0) {
                double frantzCluster = acluster / pcount;
                double frantzProtosegment = aproto / pcount;
                group.setReconstruction(reconstruction.toString());
                group.setFrantzCluster(frantzCluster);
                group.setFrantzProtosegment(frantzProtosegment);
                group.setAllCitationsWithResidue(allCitationsWithResidue);
                group.save();
            }
        }
        return frantzN;
    }

    /**
     * Compute Frantz strenth accumulator.
     **/
    private double getFrantzAccumulator(double c, double a, double n) {
        double acc = a;
        if (c >= n)
            acc += 1.0;
        if ((c < n) && (c > 2))
            acc += c / n;
        if (c == 2.)
            acc -= 0.5;
        if (c == 1.)
            acc -= 1.0;
        return acc;
    }

    /**
     * Get element representing this view.
     **/
    public Element getElement() {
        Element element = new Element("view");

        // set attributes
        element.setAttribute("view-name", getName());
        Integer threshold = getThreshold();
        element.setAttribute(
            "threshold",
            (threshold == null) ? "" : threshold.intValue() + "");
        // set elements
        element.addContent(createElement("remarks", getRemarks()));

        // view members
        int i = 0;
        for (Iterator it = getMembers().iterator(); it.hasNext();) {
            Variety variety = (Variety) it.next();
            Element memberElem = new Element("view-member");
            element.addContent(memberElem);
            // set attributes
            memberElem.setAttribute("short-name", variety.getShortName());
            memberElem.setAttribute("order-number", ++i + "");
        }

        try {
            // annotations
            Element listElem = new Element("annotations");
            element.addContent(listElem);
            for (Iterator it = getCollection().getEntries().iterator(); it.hasNext();) {
                Entry entry = (Entry) it.next();
                for (Iterator it2 = getAlignments(entry).iterator(); it2.hasNext();) {
                    Alignment alignment = (Alignment) it2.next();
                    // skip records for unannotated datums 
                    if (alignment.getGroup() != null)
                        listElem.addContent(alignment.getElement());
                }
            }

            // results
            listElem = new Element("results");
            element.addContent(listElem);
            for (Iterator it = getProtosegments().iterator(); it.hasNext();) {
                Protosegment protosegment = (Protosegment) it.next();
                listElem.addContent(protosegment.getElement());
            }

            // tabulated groups
            listElem = new Element("tabulated-groups");
            element.addContent(listElem);
            for (Iterator it = getTabulatedGroups().iterator(); it.hasNext();) {
                Group group = (Group) it.next();
                listElem.addContent(group.getElement());
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Get summary element representing this view.
     **/
    public Element getSummaryElement(
        float minFrantz,
        int maxReconstructions,
        String displayFrantz,
        boolean includeResidue,
        String gloss) {
        Element element = new Element("view");

        // set attributes
        element.setAttribute("view-name", getName());
        Integer threshold = getThreshold();
        element.setAttribute(
            "threshold",
            (threshold == null) ? "" : threshold.intValue() + "");
        // set elements
        element.addContent(createElement("remarks", getRemarks()));

        // view members
        int i = 0;
        for (Iterator it = getMembers().iterator(); it.hasNext();) {
            Variety variety = (Variety) it.next();
            Element memberElem = new Element("view-member");
            element.addContent(memberElem);
            // set attributes
            memberElem.setAttribute("name", variety.getName());
            memberElem.setAttribute("short-name", variety.getShortName());
            memberElem.setAttribute("abbreviation", variety.getAbbreviation());
            memberElem.setAttribute("order-number", ++i + "");
        }

        try {
            // protosegments
            Element listElem = new Element("protosegments");
            element.addContent(listElem);
            for (Iterator it = getProtosegments().iterator(); it.hasNext();) {
                Protosegment protosegment = (Protosegment) it.next();
                // check residue protosegment
                if (!includeResidue && protosegment.getZone().getAbbreviation().equals("RES"))
                    continue;

                listElem.addContent(
                    protosegment.getSummaryElement(
                        this,
                        minFrantz,
                        maxReconstructions,
                        displayFrantz,
                        includeResidue,
                        gloss));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Mark this view as imported.
     **/
    public void markImported() {
        _imported = true;
    }

    /**
     * Make an Alignment object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Alignment makeAlignment(Datum datum) {
        return new Alignment(getDatabase(), -1, this, datum);
    }

    /**
     * Make a Group object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Group makeGroup(Entry entry) {
        return new Group(getDatabase(), -1, this, entry);
    }

    /**
     * Make an Protosegment object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Protosegment makeProtosegment(Zone zone) {
        return new Protosegment(getDatabase(), -1, this, zone);
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Override of check validation prior to save.
     * Return null of okay, message if not.
     **/
    public String checkValidation() throws DatabaseException {
        // check for modification
        if (!isDirty())
            return null;

        // check for duplicate name
        if (getID() == UNDEFINED_ID) {
            for (Iterator it = getCollection().getViews().iterator(); it.hasNext();) {
                View vw = (View) it.next();
                if (getName().equalsIgnoreCase(vw.getName()))
                    return AppPrefs.getInstance().getMessages().getCompoundMessage(
                        "msgViewDuplicateName",
                        getName());
            }
        }

        // check for tabulation
        if (getGroupsTabulated() > 0 && (_thresholdChanged || _varietiesChanged)) {
            revert();
            return AppPrefs.getInstance().getMessages().getString("msgViewTabulated");
        } else if (_varieties.size() < 2) {
            return AppPrefs.getInstance().getMessages().getString(
                "msgViewMinimumVarieties");
        }
        return null;
    }

    /**
     * Override of save to also create default protosegments and link members.
     **/
    public synchronized void save() throws DatabaseException {
        boolean newObject = (getID() == UNDEFINED_ID) ? true : false;

        super.save();

        // now do the view linking
        final List all = new ArrayList(_collection.getVarieties());
        getDatabase().linkObjects(new LinkParameters() {
            public String getRemoveSQLKey() {
                return "REMOVE_VIEW_MEMBER";
            }
            public String getCreateSQLKey() {
                return "ADD_VIEW_MEMBER";
            }
            public String getUpdateSQLKey() {
                return "UPDATE_VIEW_MEMBER";
            }

            public void setRemoveParameters(PreparedStatement stmt, int index)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, ((Variety) all.get(index)).getID());
            }

            public void setCreateParameters(PreparedStatement stmt, int index)
                throws SQLException {
                Variety variety = (Variety) all.get(index);
                stmt.setLong(1, getID());
                stmt.setLong(2, variety.getID());
                stmt.setInt(3, _varieties.indexOf(variety));
            }

            public void setUpdateParameters(PreparedStatement stmt, int index)
                throws SQLException {
                Variety variety = (Variety) all.get(index);
                stmt.setInt(1, _varieties.indexOf(variety));
                stmt.setLong(2, getID());
                stmt.setLong(3, variety.getID());
            }

            public int getLinkCount() {
                return all.size();
            }

            public boolean isLinked(int index) {
                return _varieties.contains(all.get(index));
            }
        });

        resetChangeFlags();
    }

    /**
     * Revert this object.
     **/
    public synchronized void revert() throws DatabaseException {
        super.revert();

        resetChangeFlags();
    }

    /**
     * Clear the dirty flag.
     **/
    public void clearDirty() {
        super.clearDirty();

        resetChangeFlags();
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setThreshold(new Integer(rs.getInt(3)));
        if (rs.wasNull()) {
            setThreshold(null);
        }
        setRemarks(rs.getString(4));

        // load the view members
        try {
            _varieties = getDatabase().retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_VIEW_MEMBERS";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException {
                    stmt.setLong(1, getID());
                }

                public Object createObject(Database db, ResultSet rs2) throws SQLException {
                    Variety variety = new Variety(db, rs2.getLong(1), _collection);
                    variety.updateObject(rs2);
                    return variety;
                }
            });
        } catch (DatabaseException e) {
            e.printStackTrace();
            throw new SQLException(e.getRootCause().getMessage());
        }
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        if (getThreshold() == null) {
            stmt.setNull(2, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(2, getThreshold().intValue());
        }
        stmt.setString(3, getRemarks());
        stmt.setLong(4, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        if (getThreshold() == null) {
            stmt.setNull(2, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(2, getThreshold().intValue());
        }
        stmt.setString(3, getRemarks());
        stmt.setLong(4, _collection.getID());
        stmt.setLong(5, _collection.getUser().getID());
    }

    /**
     * Generates object and all children from Element and saves in db.
     **/
    public void generateFromElement(
        Element element,
        Map varieties,
        Map entries,
        Map datumsByEntry)
        throws DatabaseException {
        if (element == null)
            return;

        // set values
        setName(element.getAttributeValue("view-name"));
        String threshold = element.getAttributeValue("threshold");
        if (!threshold.equals(""))
            setThreshold(new Integer(threshold));
        setRemarks(element.getChildText("remarks"));

        // view members
        List members = new ArrayList();
        for (Iterator it = element.getChildren("view-member").iterator();
            it.hasNext();
            ) {
            Element elem = (Element) it.next();
            String[] member =
                { elem.getAttributeValue("order-number"), elem.getAttributeValue("short-name")};
            members.add(member);
        }
        Collections.sort(members, new Comparator() {
            public int compare(Object o1, Object o2) {
                String[] m1 = (String[]) o1;
                String[] m2 = (String[]) o2;

                int i1 = Integer.parseInt(m1[0]);
                int i2 = Integer.parseInt(m2[0]);
                return i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
            }
        });
        // convert to variety
        for (int i = 0; i < members.size(); i++) {
            String[] member = (String[]) members.get(i);
            members.set(i, varieties.get(member[1]));
        }
        setMembers(members);

        markImported();
        save();

        // annotations
        Map groups = new HashMap();
        Element listElem = element.getChild("annotations");
        for (Iterator it = listElem.getChildren("annotated-datum").iterator();
            it.hasNext();
            ) {
            Element elem = (Element) it.next();
            // get or create group if new
            String entryNumber = elem.getAttributeValue("entry-number");
            String tag = elem.getAttributeValue("tag");
            String groupKey = entryNumber + ":" + tag;
            Group group = (Group) groups.get(groupKey);
            if (group == null) {
                Entry ent = (Entry) entries.get(entryNumber);
                group = makeGroup((Entry) entries.get(entryNumber));
                group.setName(tag);
                group.save();
                groups.put(groupKey, group);
            }
            // get datum
            Map datums = (Map) datumsByEntry.get(elem.getAttributeValue("entry-number"));
            Datum datum = (Datum) datums.get(elem.getAttributeValue("datum-number"));
            // create alignment
            Alignment alignment = makeAlignment(datum);
            alignment.setGroup(group);
            alignment.generateFromElement(elem);
        }

        // results
        Set tabulatedGroups = new HashSet();
        listElem = element.getChild("results");
        for (Iterator it = listElem.getChildren("protosegment").iterator();
            it.hasNext();
            ) {
            Element elem = (Element) it.next();
            // get zone
            Zone zone =
                getDatabase().getZone(
                    new Integer(elem.getAttributeValue("zone-row")),
                    new Integer(elem.getAttributeValue("zone-column")));
            // create protosegment
            Protosegment protosegment = makeProtosegment(zone);
            protosegment.generateFromElement(elem, groups, tabulatedGroups);
        }

        // get remaining tabulated groups (below threshold)
        listElem = element.getChild("tabulated-groups");
        if (listElem != null)
            for (Iterator it = listElem.getChildren("group").iterator(); it.hasNext();) {
                Element elem = (Element) it.next();
                // get group
                String entryNumber = elem.getAttributeValue("entry-number");
                String tag = elem.getAttributeValue("tag");
                String groupKey = entryNumber + ":" + tag;
                Group group = (Group) groups.get(groupKey);
                // mark group as tabulated
                tabulatedGroups.add(group);
            }

        // mark tabulated groups
        for (Iterator it = tabulatedGroups.iterator(); it.hasNext();) {
            Group group = (Group) it.next();
            group.setDone(true);
            group.save();
        }
    }

    private boolean _imported = false;
    private boolean _thresholdChanged = false;
    private boolean _varietiesChanged = false;
    private Integer _threshold;
    private String _remarks;
    private List _varieties = Collections.EMPTY_LIST;
    private final WordCollection _collection;
}