package org.wordcorr.db;

import org.jdom.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Represents a collection of word lists for a user.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class WordCollection extends AbstractPersistent {

    WordCollection(Database db, long id, User user) {
        super(db, id);
        _user = user;
    }

    //---------------------------------------------------------------//
    // Attributes
    //---------------------------------------------------------------//

    /**
     * Get this collection's short name.
     **/
    public String getShortName() {
        return (_sname == null) ? "" : _sname;
    }

    /**
     * Set this collection's short name.
     **/
    public void setShortName(String sname) {
        setDirty();
        _sname = sname;
    }

    /**
     * Get this collection's creator role.
     **/
    public String getCreatorRole() {
        return (_creatorRole == null) ? "" : _creatorRole;
    }

    /**
     * Set this collection's creator role.
     **/
    public void setCreatorRole(String creatorRole) {
        setDirty();
        _creatorRole = creatorRole;
    }

    /**
     * Get this collection's creator.
     **/
    public String getCreator() {
        return (_creator == null) ? "" : _creator;
    }

    /**
     * Set this collection's creator.
     **/
    public void setCreator(String creator) {
            setDirty();
            _creator = creator;
    }

    /**
     * Get this collection's publisher.
     **/
    public String getPublisher() {
        return (_publisher == null) ? "" : _publisher;
    }

    /**
     * Set this collection's publisher.
     **/
    public void setPublisher(String publisher) {
            setDirty();
            _publisher = publisher;
    }

    /**
     * Get this collection's contributor.
     **/
    public String getContributor() {
        return (_contributor == null) ? "" : _contributor;
    }

    /**
     * Set this collection's contributor.
     **/
    public void setContributor(String contributor) {
        setDirty();
        _contributor = contributor;
    }

    /**
     * Get this collection's description.
     **/
    public String getDescription() {
        return (_description == null) ? "" : _description;
    }

    /**
     * Set this collection's description.
     **/
    public void setDescription(String description) {
        setDirty();
        _description = description;
    }

    /**
     * Get this collection's remarks.
     **/
    public String getRemarks() {
        return (_remarks == null) ? "" : _remarks;
    }

    /**
     * Set this collection's remarks.
     **/
    public void setRemarks(String remarks) {
        setDirty();
        _remarks = remarks;
    }

    /**
     * Get this collection's gloss language.
     **/
    public String getGloss() {
        return (_gloss == null) ? "" : _gloss;
    }

    /**
     * Set this collection's gloss language.
     **/
    public void setGloss(String gloss) {
        setDirty();
        _gloss = gloss;
    }

    /**
     * Get this collection's gloss language code.
     **/
    public String getGlossCode() {
        return (_glossCode == null) ? "" : _glossCode;
    }

    /**
     * Set this collection's gloss language code.
     **/
    public void setGlossCode(String glossCode) {
        setDirty();
        _glossCode = glossCode;
    }

    /**
     * Get this collection's secondary gloss language.
     **/
    public String getGloss2() {
        return (_gloss2 == null) ? "" : _gloss2;
    }

    /**
     * Set this collection's secondary gloss language.
     **/
    public void setGloss2(String gloss2) {
        setDirty();
        _gloss2 = gloss2;
    }

    /**
     * Get this collection's secondary gloss language code.
     **/
    public String getGlossCode2() {
        return (_glossCode2 == null) ? "" : _glossCode2;
    }

    /**
     * Set this collection's secondary gloss language code.
     **/
    public void setGlossCode2(String glossCode2) {
        setDirty();
        _glossCode2 = glossCode2;
    }

    /**
     * Get this collection's keywords.
     **/
    public String getKeywords() {
        return (_keywords == null) ? "" : _keywords;
    }

    /**
     * Set this collection's keywords.
     **/
    public void setKeywords(String keywords) {
        setDirty();
        _keywords = keywords;
    }

    /**
     * Get this collection's coverage.
     **/
    public String getCoverage() {
        return (_coverage == null) ? "" : _coverage;
    }

    /**
     * Set this collection's coverage.
     **/
    public void setCoverage(String coverage) {
        setDirty();
        _coverage = coverage;
    }

    /**
     * Get this collection's published source.
     **/
    public String getPublishedSource() {
        return (_publishedSource == null) ? "" : _publishedSource;
    }

    /**
     * Set this collection's published source.
     **/
    public void setPublishedSource(String publishedSource) {
        setDirty();
        _publishedSource = publishedSource;
    }

    /**
     * Get this collection's stable location.
     **/
    public String getStableLocation() {
        return (_stableLocation == null) ? "" : _stableLocation;
    }

    /**
     * Set this collection's stable location.
     **/
    public void setStableLocation(String stableLocation) {
        setDirty();
        _stableLocation = stableLocation;
    }

    /**
     * Get this collection's rights management.
     **/
    public String getRightsManagement() {
        return (_rightsManagement == null) ? "" : _rightsManagement;
    }

    /**
     * Set this collection's rights management.
     **/
    public void setRightsManagement(String rightsManagement) {
        setDirty();
        _rightsManagement = rightsManagement;
    }

    /**
     * Get this collection's rights copyright.
     **/
    public Integer getRightsCopyright() {
        return _rightsCopyright;
    }

    /**
     * Set this collection's rights copyright.
     **/
    public void setRightsCopyright(Integer rightsCopyright) {
        setDirty();
        _rightsCopyright = rightsCopyright;
    }

    /**
     * Get this collection's export timestamp.
     **/
    public Timestamp getExportTimestamp() {
        return _exportTimestamp;
    }

    /**
     * Set this collection's export timestamp.
     **/
    public void setExportTimestamp(Timestamp v) {
        setDirty();
        _exportTimestamp = v;
    }

    /**
     * Set this collection's export timestamp to current date.
     **/
    public void setExportTimestamp() throws DatabaseException {
        setExportTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Get this collection's speech varieties.
     **/
    public List getVarieties() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VARIETIES";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Variety variety = new Variety(db, rs.getLong(1), WordCollection.this);
                variety.updateObject(rs);
                return variety;
            }
        });
    }

    /**
     * Get this collection's speech varieties sorted by Original View.
     **/
    public List getOriginalVarieties() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VARIETIES_BY_VIEW";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                try {
                    stmt.setLong(1, getID());
                    stmt.setLong(2, getOriginalView().getID());
                } catch (DatabaseException e) {
                    throw new SQLException(e.getRootCause().getMessage());
                }
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Variety variety = new Variety(db, rs.getLong(1), WordCollection.this);
                variety.updateObject(rs);
                return variety;
            }
        });
    }

    /**
     * Get the current user's views on this collection.
     **/
    public List getViews() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_VIEWS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setLong(2, getUser().getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                View view = new View(db, rs.getLong(1), WordCollection.this);
                view.updateObject(rs);
                return view;
            }
        });
    }

    /**
     * Get the original view for this collection if it persists
     * or create a new one if it doesn't.
     **/
    public View getOriginalView() throws DatabaseException {
        if (_originalView == null) {
            // search for existing view
            _originalView = getView("Original");
            if (_originalView == null) {
                // create new view
                _originalView = makeView();
                _originalView.setName("Original");
                _originalView.setThreshold(new Integer(50));
                _originalView.save();
            }
        }
        return _originalView;
    }

    /**
     * Get view by name.
     **/
    public View getView(String name) throws DatabaseException {
        // search for existing view
        List views = getViews();
        for (Iterator it = views.iterator(); it.hasNext();) {
            View view = (View) it.next();
            if (view.getName().equals(name)) {
                return view;
            }
        }
        return null;
    }

    /**
     * Get view by ID.
     **/
    public View getViewByID(long id) throws DatabaseException {
        // search for existing view
        List views = getViews();
        for (Iterator it = views.iterator(); it.hasNext();) {
            View view = (View) it.next();
            if (view.getID() == id) {
                return view;
            }
        }
        return null;
    }

    /**
     * Get this collection's entries.
     **/
    public List getEntries() throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_ENTRIES";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), WordCollection.this);
                entry.updateObject(rs);
                return entry;
            }
        });
    }

    /**
     * Get this collection's entries starting from given entry number.
     **/
    public List getEntriesFromEntryNum(final int entryNum)
        throws DatabaseException {
        return getDatabase().retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_ENTRIES_FROM_ENTRY_NUM";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setLong(1, getID());
                stmt.setInt(2, entryNum);
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Entry entry = new Entry(db, rs.getLong(1), WordCollection.this);
                entry.updateObject(rs);
                return entry;
            }
        });
    }

    /**
     * Set views included with element.
     **/
    public void setElementViews(List val) {
        _elementViews = val;
    }

    /**
     * Get element representing this collection.
     **/
    public Element getElement(boolean metadataOnly) {
        Element element = new Element("collection");

        // set attributes
        element.setAttribute("name", getName());
        element.setAttribute("short-name", getShortName());
        element.setAttribute("gloss-language", getGloss());
        element.setAttribute("gloss-language-code", getGlossCode());
        element.setAttribute("secondary-gloss-language", getGloss2());
        element.setAttribute("secondary-gloss-language-code", getGlossCode2());
        element.setAttribute("creator-role", getCreatorRole());
        element.setAttribute("creator", getCreator());
        element.setAttribute("publisher", getPublisher());
        element.setAttribute("rights-management", getRightsManagement());
        Integer copyright = getRightsCopyright();
        element.setAttribute(
            "rights-management-year-copyright-asserted",
            (copyright == null) ? "" : copyright.intValue() + "");
        element.setAttribute("export-timestamp", getExportTimestamp().getTime() + "");
        // set elements
        element.addContent(createElement("contributor", getContributor()));
        element.addContent(createElement("description", getDescription()));
        element.addContent(createElement("remarks", getRemarks()));
        element.addContent(createElement("keywords", getKeywords()));
        element.addContent(createElement("coverage", getCoverage()));
        element.addContent(createElement("published-source", getPublishedSource()));
        element.addContent(createElement("stable-copy-location", getStableLocation()));

        try {
            // varieties
            Element listElem = new Element("varieties");
            element.addContent(listElem);
            for (Iterator it = getVarieties().iterator(); it.hasNext();) {
                Variety variety = (Variety) it.next();
                listElem.addContent(variety.getElement());
            }

            if (!metadataOnly) {
                // entries
                listElem = new Element("data");
                element.addContent(listElem);
                for (Iterator it = getEntries().iterator(); it.hasNext();) {
                    Entry entry = (Entry) it.next();
                    listElem.addContent(entry.getElement());
                }

                // views
                listElem = new Element("views");
                element.addContent(listElem);
                List viewList = (_elementViews == null) ? getViews() : _elementViews;
                for (Iterator it = viewList.iterator(); it.hasNext();) {
                    View view = (View) it.next();
                    listElem.addContent(view.getElement());
                }
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Get summary element representing this collection.
     **/
    public Element getSummaryElement(
        View view,
        float minFrantz,
        int maxReconstructions,
        String displayFrantz,
        boolean includeResidue,
        String gloss) {
        Element element = new Element("collection");

        // set attributes
        element.setAttribute("name", getName());
        element.setAttribute("short-name", getShortName());
        element.setAttribute("gloss-language", getGloss());
        element.setAttribute("secondary-gloss-language", getGloss2());
        // set elements
        element.addContent(createElement("remarks", getRemarks()));

        try {
            // varieties
            Element listElem = new Element("varieties");
            element.addContent(listElem);
            for (Iterator it = getVarieties().iterator(); it.hasNext();) {
                Variety variety = (Variety) it.next();
                listElem.addContent(variety.getElement());
            }

            // view
            element.addContent(
                view.getSummaryElement(
                    minFrantz,
                    maxReconstructions,
                    displayFrantz,
                    includeResidue,
                    gloss));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return element;
    }

    /**
     * Mark this collection as imported.
     **/
    public void markImported() {
        _imported = true;
    }

    /**
     * Reorder entry numbers from given entry number using starting number.
     **/
    public void reorderEntries(int entryNum, int startNum)
        throws DatabaseException {
        List list = getEntriesFromEntryNum(entryNum);

        int i = startNum;
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();

            entry.setEntryNum(new Integer(i++));
            entry.save();
        }
    }

    /**
     * Make a speech variety object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Variety makeVariety() {
        return new Variety(getDatabase(), -1, this);
    }

    /**
     * Make a view object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public View makeView() {
        return new View(getDatabase(), -1, this);
    }

    /**
     * Make an entry object. This does not add anything to the
     * database. The returned object must be saved to be made
     * permanent.
     **/
    public Entry makeEntry() {
        return new Entry(getDatabase(), -1, this);
    }

    /**
     * Get this collection's user.
     **/
    public User getUser() {
        return _user;
    }

    //---------------------------------------------------------------//
    // Persistent Methods
    //---------------------------------------------------------------//

    /**
     * Override of save to also create Original view.
     **/
    public synchronized void save() throws DatabaseException {
        boolean newObject = (getID() == UNDEFINED_ID) ? true : false;
        super.save();

        // create Original View
        if (newObject && !_imported) {
            getDatabase().getCurrentSetting().setViewID(getOriginalView().getID());
        }
    }

    /**
     * Update the object based on a result set.
     **/
    public void updateObject(ResultSet rs) throws SQLException {
        setName(rs.getString(2));
        setShortName(rs.getString(3));
        setCreatorRole(rs.getString(4));
        setCreator(rs.getString(5));
        setPublisher(rs.getString(6));
        setContributor(rs.getString(7));
        setDescription(rs.getString(8));
        setRemarks(rs.getString(9));
        setGloss(rs.getString(10));
        setGlossCode(rs.getString(11));
        setGloss2(rs.getString(12));
        setGlossCode2(rs.getString(13));
        setKeywords(rs.getString(14));
        setCoverage(rs.getString(15));
        setPublishedSource(rs.getString(16));
        setStableLocation(rs.getString(17));
        setRightsManagement(rs.getString(18));
        int copyright = rs.getInt(19);
        setRightsCopyright((copyright == 0) ? null : new Integer(copyright));
        setExportTimestamp(rs.getTimestamp(20));
    }

    /**
     * Set parameters on the update statement.
     **/
    public void setUpdateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getShortName());
        stmt.setString(3, getCreatorRole());
        stmt.setString(4, getContributor());
        stmt.setString(5, getDescription());
        stmt.setString(6, getRemarks());
        stmt.setString(7, getGloss());
        stmt.setString(8, getGlossCode());
        stmt.setString(9, getGloss2());
        stmt.setString(10, getGlossCode2());
        stmt.setString(11, getKeywords());
        stmt.setString(12, getCoverage());
        stmt.setString(13, getPublishedSource());
        stmt.setString(14, getStableLocation());
        stmt.setString(15, getRightsManagement());
        if (getRightsCopyright() == null)
            stmt.setNull(16, java.sql.Types.INTEGER);
        else
            stmt.setInt(16, getRightsCopyright().intValue());
        if (getExportTimestamp() == null)
            stmt.setNull(17, java.sql.Types.TIMESTAMP);
        else
            stmt.setTimestamp(17, getExportTimestamp());
        stmt.setLong(18, getID());
    }

    /**
     * Set parameters on the create statement.
     **/
    public void setCreateParameters(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, getName());
        stmt.setString(2, getShortName());
        stmt.setString(3, getCreatorRole());
        stmt.setString(4, getCreator());
        stmt.setString(5, getPublisher());
        stmt.setString(6, getContributor());
        stmt.setString(7, getDescription());
        stmt.setString(8, getRemarks());
        stmt.setString(9, getGloss());
        stmt.setString(10, getGlossCode());
        stmt.setString(11, getGloss2());
        stmt.setString(12, getGlossCode2());
        stmt.setString(13, getKeywords());
        stmt.setString(14, getCoverage());
        stmt.setString(15, getPublishedSource());
        stmt.setString(16, getStableLocation());
        stmt.setString(17, getRightsManagement());
        if (getRightsCopyright() == null)
            stmt.setNull(18, java.sql.Types.INTEGER);
        else
            stmt.setInt(18, getRightsCopyright().intValue());
        if (getExportTimestamp() == null)
            stmt.setNull(19, java.sql.Types.DATE);
        else
            stmt.setTimestamp(19, getExportTimestamp());
        stmt.setLong(20, _user.getID());
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
        setGloss(element.getAttributeValue("gloss-language"));
        setGloss2(element.getAttributeValue("secondary-gloss-language"));
        setRemarks(element.getChildText("remarks"));

        // metadata from version 2
        if (version >= 2) {
            setGlossCode(element.getAttributeValue("gloss-language-code"));
            setGlossCode2(element.getAttributeValue("secondary-gloss-language-code"));
            setCreatorRole(element.getAttributeValue("creator-role"));
            setCreator(element.getAttributeValue("creator"));
            setPublisher(element.getAttributeValue("publisher"));
            setRightsManagement(element.getAttributeValue("rights-management"));
            String rightsCopyright =
                element.getAttributeValue("rights-management-year-copyright-asserted");
            if (!rightsCopyright.equals(""))
                setRightsCopyright(new Integer(rightsCopyright));
            String exportTimestamp = element.getAttributeValue("export-timestamp");
            if (!exportTimestamp.equals(""))
                setExportTimestamp(new Timestamp(Long.parseLong(exportTimestamp)));
            setContributor(element.getChildText("contributor"));
            setDescription(element.getChildText("description"));
            setKeywords(element.getChildText("keywords"));
            setCoverage(element.getChildText("coverage"));
            setPublishedSource(element.getChildText("published-source"));
            setStableLocation(element.getChildText("stable-copy-location"));
        }

        markImported();
        save();

        // varieties
        Map varieties = new HashMap();
        Element listElem = element.getChild("varieties");
        for (Iterator it = listElem.getChildren("variety").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            Variety variety = makeVariety();
            variety.generateFromElement(elem, version);
            varieties.put(variety.getShortName(), variety);
        }

        // entries
        Map entries = new HashMap();
        Map datumsByEntry = new HashMap();
        listElem = element.getChild("data");
        for (Iterator it = listElem.getChildren("entry").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            Entry entry = makeEntry();
            Map datums = entry.generateFromElement(elem, varieties);
            datumsByEntry.put(entry.getEntryNum().toString(), datums);
            entries.put(entry.getEntryNum().toString(), entry);
        }

        // views
        listElem = element.getChild("views");
        for (Iterator it = listElem.getChildren("view").iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            View view = makeView();
            view.generateFromElement(elem, varieties, entries, datumsByEntry);
        }

        // reorder all entry numbers
        reorderEntries(0, 1);
    }

    private boolean _imported = false;
    private String _sname;
    private String _creatorRole;
    private String _creator;
    private String _publisher;
    private String _contributor;
    private String _description;
    private String _remarks;
    private String _gloss;
    private String _glossCode;
    private String _gloss2;
    private String _glossCode2;
    private String _keywords;
    private String _coverage;
    private String _publishedSource;
    private String _stableLocation;
    private String _rightsManagement;
    private Integer _rightsCopyright;
    private Timestamp _exportTimestamp;
    private final User _user;
    private View _originalView = null;
    private List _elementViews = null;
}