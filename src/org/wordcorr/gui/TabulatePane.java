package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.wordcorr.AppProperties;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Alignment;
import org.wordcorr.db.Citation;
import org.wordcorr.db.Cluster;
import org.wordcorr.db.CorrespondenceSet;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Entry;
import org.wordcorr.db.Group;
import org.wordcorr.db.Persistent;
import org.wordcorr.db.Protosegment;
import org.wordcorr.db.Setting;
import org.wordcorr.db.Variety;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Zone;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Task;
import org.wordcorr.gui.TaskDialog;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.IPAKeyListener;
import org.wordcorr.gui.input.Row;

/**
 * Pane for doing tabulation.
 * @author Keith Hamasaki, Jim Shiba
 **/
class TabulatePane extends JPanel implements Refreshable, Initializable {

    private static final String[] COL_KEYS =
        { "lblTag", "lblAbbreviation", "lblShortName", "lblAligned" };
    private static final String[] TAB_COL_KEYS =
        { "lblPosition", "lblCorrespondenceSet", "lblProtosegment", "lblEnvironment" };

    /**
     * Constructor.
     **/
    TabulatePane(WordCollection collection) {
        super(new BorderLayout());
        _collection = collection;
        _mainPanel = new ViewEntryPanel();
        add(_mainPanel, BorderLayout.CENTER);
        
        // initially refresh list
        try {
            refresh();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh this pane.
     **/
    public void refresh() throws DatabaseException {
        // set active view
        _mainPanel.refresh();
        _mainPanel.setVisible(true);
        _mainPanel.selectDefault();
    }

    /**
     * Initialize this pane.
     **/
    public void init() throws DatabaseException {
        // set selection
        _mainPanel.selectDefault();
    }

    /**
     * AddEditDelete panel for view entries.
     **/
    private final class ViewEntryPanel extends AddEditDeletePanel {

        ViewEntryPanel() {
            super(null, false, false, false, true, false);

            // change find label
            setFindButtonLabel(
                AppPrefs.getInstance().getMessages().getString("btnFindTabulate"));
                
            // prevent setting current edit object when selecting entry
            setCurrentEditObjectSetting(false);

            this.setBorder(BorderFactory.createEtchedBorder());
            getList().setModel(new BasicListModel());
            getList().setFont(FontCache.getFont(FontCache.PRIMARY_GLOSS));
            this.setVisible(false);

            getList()
                .getSelectionModel()
                .addListSelectionListener(new ListSelectionListener() {
                public synchronized void valueChanged(ListSelectionEvent evt) {
                    if (evt.getValueIsAdjusting()) {
                        return;
                    }

                    EntryWrapper entry = (EntryWrapper) getList().getSelectedValue();
                    if (entry != null) {
                        entry.setDataSets(false);
                        try {
                            Setting setting = _collection.getDatabase().getCurrentSetting();
                            setting.setEntryID(entry.getID());
                            setting.save();
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        /**
         * Create the right component. Override with
         * SavePane initialized with the persistent object and showing buttons.
         **/
        protected Component createRightComponent() {
            Persistent persistent = (Persistent) getList().getSelectedValue();
            return new SavePane(null, persistent, (Refreshable) getList().getModel(), true);
        }
    
        /**
         * Select the default entry.
         **/
        void selectDefault() throws DatabaseException {
            Setting setting = _collection.getDatabase().getCurrentSetting();
            for (int i = 0; i < getList().getModel().getSize(); i++) {
                EntryWrapper entry = (EntryWrapper) getList().getModel().getElementAt(i);
                if (entry.getID() == setting.getEntryID()) {
                    getList().setSelectedValue(entry, true);
                    break;
                }
            }
        }

        /**
         * Additional refresh behavior for this component.
         **/
        public void refreshExt() throws DatabaseException {
            // populate list
            List entries = Collections.EMPTY_LIST;
            Setting setting = _collection.getDatabase().getCurrentSetting();
            View view = _collection.getViewByID(setting.getViewID());
            if (view != null) {
                view.revert();
                entries = view.getUntabulatedEntries();
            }
            _protosegments = new ProtosegmentList(view);
            List wrappers = new ArrayList(entries.size());
            for (Iterator it = entries.iterator(); it.hasNext();) {
                EntryWrapper entry =
                    new EntryWrapper((Entry) it.next(), view, _protosegments, this);
                wrappers.add(entry);
            }
            ((BasicListModel) getList().getModel()).setData(wrappers);
            
            // set selection
            selectDefault();
        }
        
        protected FindDialog createFindDialog() {
            return new FindDialog("GlossFindDialog");
        }

        private ProtosegmentList _protosegments;
    }

    /**
     * Wrapper bean class for an entry.
     **/
    public static final class EntryWrapper implements Persistent {

        EntryWrapper(
            Entry entry,
            View view,
            ProtosegmentList protosegments,
            AddEditDeletePanel panel)
            throws DatabaseException {
            _entry = entry;
            _view = view;
            _protosegments = protosegments;
            _panel = panel;
        }

        // Attributes
        public long getID() {
            return _entry.getID();
        }
        public Integer getEntry() {
            return _entry.getEntryNum();
        }
        public String getGloss() {
            return _entry.getName();
        }
        public String getGloss2() {
            return _entry.getGloss2();
        }
        public List getData() {
            return _alignments;
        }
        public void setData(List data) {
            _alignments = data;
            setDirty();
        }
        public List getTabulate() {
            return _dataSets;
        }
        public void setTabulate(List data) {
            _dataSets = data;
            setDirty();
        }
        public Group getGroup() {
            return _group;
        }
        public ProtosegmentList getProtosegments() {
            return _protosegments;
        }
        public View getView() {
            return _view;
        }
        private Alignment getCorrespondenceSetAlignment(Group group, Variety variety) {
            // check alignments
            ArrayList alignments = new ArrayList();
            for (Iterator it = _alignments.iterator(); it.hasNext();) {
                Alignment alignment = (Alignment)it.next();
                
                // skip alignment with no group
                if (alignment.getGroup() == null)
                    continue;
                
                // match group and variety
                if (group.getID() == alignment.getGroup().getID()
                    && variety.getID() == alignment.getDatum().getVariety().getID()) {
                    alignments.add(alignment);
                }
            }
            // return if empty or one
            if (alignments.isEmpty()) {
                return null;
            } else if (alignments.size() == 1) {
                return (Alignment)alignments.get(0);
            }
            
            // prompt user for choice if more than 1 found
            SelectAlignmentTask task = new SelectAlignmentTask(alignments);
            String[] messageData = {group.getName(), variety.getShortName()};
            task.setMessage(AppPrefs.getInstance().getMessages().getCompoundMessage(
                "msgTabulateSelectAlignment", messageData));
            String cmd = "TabulateSelectAlignment";
            TaskDialog dialog = new TaskDialog("lbl" + cmd, task, cmd);
            dialog.isCancelVisible(false);
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {
                return (Alignment)task.getValue();
            } else {
                // use first as default
                return (Alignment)alignments.get(0);
            }
        }
            
        public boolean isDataComplete() {
            if (_dataSets.isEmpty())
                return false;
            for (Iterator it = _dataSets.iterator(); it.hasNext();) {
                DataSet set = (DataSet) it.next();
                if (!set.isComplete())
                    return false;
            }
            return true;
        }
        public boolean isValid() {
            return !_dataSets.isEmpty();
        }
        public void setDataSets(boolean reset) {
            if (_isDataSetsSet && !reset) {
                if (!_message.equals(""))
                    Dialogs.msgbox(_message);
                return;
            }
            _message = "";
            _isDataSetsSet = true;

            try {
                if (_view != null) {
                    _alignments = _view.getAlignments(_entry);
                }

                // compute correspondence sets
                if (_alignments.isEmpty() || _alignments.size() == 1) {
                    _dataSets = Collections.EMPTY_LIST;
                } else {
                    List groups = _view.getThresholdGroups(_entry);
                    if (groups.isEmpty()) {
                        _dataSets = Collections.EMPTY_LIST;
                        List belowGroups = _view.getBelowThresholdGroups(_entry);
                        if (!belowGroups.isEmpty()) {
                            // get names of groups that are below threshold
                            String names = "";
                            for (int i = 0; i < belowGroups.size(); i++) {
                                Group grp = (Group) belowGroups.get(i);
                                if (i > 0)
                                    names += ", ";
                                names += grp.getName();
                            }
                            boolean markGroupsDone = false;
                            if (_view.getGroupDoneCount(_entry) > 0) {
                                // mark as done
                                Dialogs.msgbox(
                                    AppPrefs.getInstance().getMessages().getCompoundMessage(
                                        "cmpTabulateBelowThresholdMarkDone",
                                        new Object[] { _entry.getName(), names, _view.getThreshold() }));
                                markGroupsDone = true;
                            } else {
                                _message =
                                    AppPrefs.getInstance().getMessages().getCompoundMessage(
                                        "cmpTabulateBelowThreshold",
                                        new Object[] { _entry.getName(), names, _view.getThreshold() });
                                if (Dialogs.confirm(_message)) {
                                    markGroupsDone = true;
                                } else {
                                    // cause reconfirmation
                                    _message = "";
                                    _isDataSetsSet = false;
                                }
                            }
                            // Mark all groups below threshold done.
                            if (markGroupsDone) {
                                for (Iterator it = belowGroups.iterator(); it.hasNext();) {
                                    Group grp = (Group) it.next();
                                    grp.setDone(true);
                                    grp.save();
                                }
                                _panel.refresh();
                            }
                        }
                    } else {
                        // build correspondence sets
                        _dataSets = new ArrayList();

                        // get current group to process
                        assignGroup : for (int i = 0; i < groups.size(); i++) {
                            _group = (Group) groups.get(i);
                            boolean first = true;
                            int firstGroupsSkipped = 0;
                            int len = 0;
                            for (Iterator it = _view.getMembers().iterator(); it.hasNext();) {
                                Variety variety = (Variety) it.next();
                                
                                Alignment alignment = getCorrespondenceSetAlignment(
                                    _group, variety);

                                if (first && alignment == null) {
                                    ++firstGroupsSkipped;
                                } else if (first) {
                                    first = false;
                                    List alignedDatum =
                                        alignment.getMetatheticallyAlignedDatumList();
                                    len = alignedDatum.size();
                                    for (int j = 0; j < len; j++) {
                                        DataSet set = new DataSet();
                                        for (int k = 0; k < firstGroupsSkipped; k++) {
                                            set.appendCorrespondenceSet(".");
                                        }
                                        set.appendCorrespondenceSet((String)alignedDatum.get(j));
                                        _dataSets.add(j, set);
                                    }
                                } else {
                                    // no alignment for variety, use ignores
                                    if (alignment == null) {
                                        for (int j = 0; j < len; j++) {
                                            DataSet set = (DataSet) _dataSets.get(j);
                                            set.appendCorrespondenceSet(".");
                                        }
                                    } else {
                                        // add alignment
                                        List alignedDatum =
                                            alignment.getMetatheticallyAlignedDatumList();
    
                                        // check length
                                        if (len != alignedDatum.size()) {
                                            _message
                                                += AppPrefs.getInstance().getMessages().getCompoundMessage(
                                                    "cmpTabulateGroupLength",
                                                    new Object[] { _entry.getName(), _group.getName()})
                                                + "\n";
                                            _dataSets.clear();
                                            _group = null;
                                            continue assignGroup;
                                        }
                                        // add alignment character to set
                                        for (int j = 0; j < len; j++) {
                                            DataSet set = (DataSet) _dataSets.get(j);
                                            set.appendCorrespondenceSet((String)alignedDatum.get(j));
                                        }
                                    }
                                }
                            }
                            // check threshold of each correspondence set
                            for (int j = _dataSets.size() - 1; j >= 0; j--) {
                                DataSet set = (DataSet)_dataSets.get(j);
                                // remove if below threshold
                                if (set.isBelowThreshold(_view.getThresholdValue()))
                                    _dataSets.remove(j);
                            }
                            // get next group if no correspondence sets left
                            if (_dataSets.isEmpty()) {
                                _dataSets.clear();
                                _group = null;
                            } else {
                                // group to tabulate ready
                                i = groups.size();
                            }
                        }
                        if (!_message.equals(""))
                            Dialogs.msgbox(_message);
                    }
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
                _dataSets = Collections.EMPTY_LIST;
            }
        }

        // Persistent methods
        public String checkValidation() throws DatabaseException {
            return null;
        }

        public void save() throws DatabaseException {
            // check data
            if (!isDataComplete())
                return;
                
            // process each correspondence set
            int i = 1;
            dataSetLoop : for (Iterator it = _dataSets.iterator(); it.hasNext();) {
                DataSet set = (DataSet) it.next();
                Integer pos = new Integer(i++);

                // process clusters with same environment
                List clusters =
                    set.getProtosegment().getEnvironmentClusters(set.getEnvironment());
                if (!clusters.isEmpty()) {
                    clusterLoop : for (Iterator itc = clusters.iterator(); itc.hasNext();) {
                        Cluster cluster = (Cluster) itc.next();
                        List correspondenceSets = cluster.getCorrespondenceSets();
                        if (correspondenceSets.isEmpty()) {
                            // delete bad cluster
                            cluster.delete();
                            continue clusterLoop;
                        } else {
                            // process correspondence sets
                            boolean conformable = false;
                            for (Iterator its = correspondenceSets.iterator(); its.hasNext();) {
                                CorrespondenceSet cset = (CorrespondenceSet) its.next();
                                // check for identical set (include ignores)
                                if (set.getCorrespondenceSet().equals(cset.getSet())) {
                                    try {
                                        // append remarks
                                        cset.appendRemarks(set.getCorrespondenceSetRemarks());
                                        cset.save();
                                        // add citation
                                        Citation citation = cset.makeCitation(_group);
                                        citation.setPosition(pos);
                                        citation.save();
                                    } catch (DatabaseException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                    continue dataSetLoop;
                                    // check for nonconformable set (exclude ignores)
                                } else if (!cset.isConformable(set.getCorrespondenceSet())) {
                                	conformable = false;
                                	break;
                                }
                                conformable = true;
                            }
                            // process conformable if identical not found
                            if (conformable) {
                                try {
                                    addCorrespondenceSetAndCitationToCluster(cluster, set, pos);
                                } catch (DatabaseException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                continue dataSetLoop;
                            }
                        }
                    }
                }

                // create new cluster if no matches found
                Cluster cluster = set.getProtosegment().makeCluster();
                cluster.setEnvironment(set.getEnvironment());
                cluster.setOrder(
                    new Integer(set.getProtosegment().getMaxClusterOrder() + 1));
                try {
                    cluster.save();
                    set.getProtosegment().reorderClusterOrder();
                    addCorrespondenceSetAndCitationToCluster(cluster, set, pos);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // set group as done
            try {
                _group.setDone(true);
                _group.save();
            } catch (DatabaseException e) {
                e.printStackTrace();
                return;
            }
// Note: Removed Protosegment Cleanup since Protosegments will be deleted in Refine.            
//            _view.deleteUnusedProtosegments();

            // setup next group
            _panel.refresh();
        }

        private void addCorrespondenceSetAndCitationToCluster(
            Cluster cluster,
            DataSet set,
            Integer pos)
            throws DatabaseException {
            // create correspondence set
            CorrespondenceSet correspondenceSet = cluster.makeCorrespondenceSet();
            correspondenceSet.setOrder(pos);
            correspondenceSet.setSet(set.getCorrespondenceSet());
            correspondenceSet.setRemarks(set.getCorrespondenceSetRemarks());
            correspondenceSet.save();

            // create citation
            Citation citation = correspondenceSet.makeCitation(_group);
            citation.setPosition(pos);
            citation.save();
        }

        public void delete() throws DatabaseException {
            clearDirty();
        }

        public void revert() throws DatabaseException {
            clearDirty();
            setDataSets(true);
        }

        public boolean isDirty() {
            return _dirty;
        }
        public void setDirty() {
            _dirty = true;
        }
        public void clearDirty() {
            _dirty = false;
        }
        public boolean isNew() {
            return false;
        }
        public String toString() {
            return (_dirty ? "+> " : "") + _entry.getName();
        }

        private boolean _dirty = false;
        private boolean _isDataSetsSet = false;
        private final Entry _entry;
        private final View _view;
        private AddEditDeletePanel _panel;
        private Group _group;
        private List _alignments;
        private List _dataSets;
        private ProtosegmentList _protosegments;
        private String _message = "";
    }

    /**
     * Input Row for alignment data.
     **/
    public static final class AlignmentRow extends InputRow {

        public AlignmentRow(
            BeanCatalog.Property prop,
            Object obj,
            final Refreshable refresh) {
            super(prop, obj);
            _entry = (EntryWrapper) obj;

            final AlignmentTableModel model = new AlignmentTableModel();
            _table = new AlignedTable(model, 3);
	        _table.setRowHeight(new Double(Math.ceil(
	            new JTextField().getFont().getSize() * 
	            Double.parseDouble(AppProperties.getProperty("RowHeightFactor")) +
	    		_table.getRowMargin() * 2)).intValue());

            // set initial column widths
            _table.setAutoCreateColumnsFromModel(false);
            int[] colWidth = { 10, 25, 125, 200 };
            for (int i = 0; i < 4; i++) {
                TableColumn col = _table.getColumnModel().getColumn(i);
                col.setPreferredWidth(colWidth[i]);
            }
            	
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(_table), BorderLayout.CENTER);
            init(panel, _table);
        }

        public void setValue(Object value) {
            AlignmentTableModel model = (AlignmentTableModel)_table.getModel();
            model.setRows((List)value);
            _table.setAlignedPositionWidths((List)value,
            	new AlignedDataExtractor() {
            		public String getColumnData(Object obj) {
            			Alignment data = (Alignment)obj;
            			return data.getAlignedDatum();
            		}
            	});
        }

        public Object getValue() {
            return null;
        }

        public double getRowWeight() {
            return 20.0;
        }

        private final EntryWrapper _entry;
        private final AlignedTable _table;
    }

    /**
     * Table model class for data elements.
     **/
    private static final class AlignmentTableModel extends AbstractTableModel {

        public Object getValueAt(int row, int col) {
            Alignment alignment = (Alignment) _rows.get(row);
            Variety variety = alignment.getDatum().getVariety();
            Group group = alignment.getGroup();
            switch (col) {
                case 0 :
                    return group == null ? null : group.getName();
                case 1 :
                    return variety.getAbbreviation();
                case 2 :
                    return variety.getShortName();
                case 3 :
                    return alignment.getAlignedDatum();
                default :
                    return "";
            }
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int i) {
            return AppPrefs.getInstance().getMessages().getString(COL_KEYS[i]);
        }

        public int getRowCount() {
            return _rows.size();
        }

        Alignment getRow(int index) {
            return (index < 0 || index >= _rows.size())
                ? null
                : (Alignment) _rows.get(index);
        }

        List getRows() {
            return new ArrayList(_rows);
        }

        void setRows(List rows) {
            _rows.clear();
            _rows.addAll(rows);
            refresh();
        }

        public void refresh() {
            fireTableStructureChanged();
        }

        private final List _rows = new ArrayList();
    }

    /**
     * Input Row for tabulate data.
     **/
    public static final class TabulateRow extends InputRow {

        public TabulateRow(
            BeanCatalog.Property prop,
            Object obj,
            final Refreshable refresh) {
            super(prop, obj);
            _entry = (EntryWrapper) obj;

            final TableModel model = new TabulateTableModel();
            model.addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent evt) {
                    _entry.setTabulate(((TabulateTableModel)model).getRows());
                    try {
                        refresh.refresh();
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    }
                }
            });
            _table = new AlignedTable(model, 1);
	        _table.setRowHeight(new Double(Math.ceil(
	            new JTextField().getFont().getSize() * 
	            Double.parseDouble(AppProperties.getProperty("RowHeightFactor")) +
	    		_table.getRowMargin() * 2)).intValue());

            // flag turned off so that refresh() which calls fireTableStructureChanged()
            // will not remove combobox.
            _table.setAutoCreateColumnsFromModel(false);

            // set initial column widths
            int[] colWidth = { 20, 200, 100, 100 };
            for (int i = 0; i < 4; i++) {
                TableColumn col = _table.getColumnModel().getColumn(i);
                col.setPreferredWidth(colWidth[i]);
            }

            // edit Correspondence Set notes
            _table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2
                        && ((evt.getModifiers() & evt.BUTTON1_MASK) > 0)) {
                        doEditRemarks();
                    }
                }
            });
            _table.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        doEditRemarks();
                        evt.consume();
                    }
                }
            });

            // Correspondence Set function panel
            JPanel fnPanel = new JPanel(new BorderLayout());
            // Group Label
            JLabel lbl = new JLabel();
            lbl.setText(
                (_entry.getGroup() != null)
                    ? AppPrefs.getInstance().getMessages().getCompoundMessage(
                        "lblTabulateGroup",
                        _entry.getGroup().getName())
                    : AppPrefs.getInstance().getMessages().getString("lblTabulateNoGroup"));
            fnPanel.add(lbl, BorderLayout.WEST);
            // Edit Remarks Button
            JPanel bnPanel = new JPanel(new FlowLayout());
            bnPanel
                .add(new WButton(new WordCorrAction("btnEditRemarks", "accEditRemarks") {
                public void actionPerformed(ActionEvent evt) {
                    doEditRemarks();
                }
            }), BorderLayout.WEST);
            // Add Protosegment Button
            bnPanel
                .add(new WButton(new WordCorrAction(
                    "btnAddProtosegment",
                    "accAddProtosegment") {
                public void actionPerformed(ActionEvent evt) {
                    doAddProtosegment();
                }
            }), BorderLayout.EAST);
            fnPanel.add(bnPanel, BorderLayout.EAST);

            // assemble panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(fnPanel, BorderLayout.NORTH);
            panel.add(new JScrollPane(_table), BorderLayout.CENTER);
            setupProtosegmentComboBox(_table.getColumnModel().getColumn(2));

            JTextField textField = new JTextField();
            textField.addKeyListener(new IPAKeyListener(textField));
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent evt) {
                    docUpdate(evt);
                }
                public void removeUpdate(DocumentEvent evt) {
                    docUpdate(evt);
                }
                public void changedUpdate(DocumentEvent evt) {
                    docUpdate(evt);
                }

                private void docUpdate(DocumentEvent evt) {
                    try {
                        javax.swing.text.Document doc = evt.getDocument();
                        _table.getModel().setValueAt(
                            doc.getText(0, doc.getLength()),
                            _table.getSelectedRow(),
                            3);
                    } catch (javax.swing.text.BadLocationException e) {
                        // this should not happen
                        e.printStackTrace();
                    }
                }
            });
            _table.getColumnModel().getColumn(3).setCellEditor(
                new DefaultCellEditor(textField));
            init(panel, _table);
        }

        public void setupProtosegmentComboBox(TableColumn col) {
            //Set up the editor for the protosegment cells.
            DefaultCellEditor editor = (DefaultCellEditor) col.getCellEditor();
            JComboBox comboBox;
            if (editor == null) {
                comboBox = new JComboBox();
                comboBox.setFont(FontCache.getIPA());
                col.setCellEditor(new DefaultCellEditor(comboBox));
                col.setCellRenderer(new FontTableCellRenderer(FontCache.IPA));
            } else {
                comboBox = (JComboBox) editor.getComponent();
            }
            comboBox.removeAllItems();
            List protos = _entry.getProtosegments().getProtosegments();
            if (protos != null) {
                for (int i = 0; i < protos.size(); i++) {
                    Protosegment newproto = (Protosegment) protos.get(i);
                    comboBox.addItem(newproto);
                }
            }
        }

        public void setValue(Object value) {
        	TabulateTableModel model = (TabulateTableModel)_table.getModel();
            model.setRows((List)value);
            _table.setAlignedPositionWidths((List)value,
            	new AlignedDataExtractor() {
            		public String getColumnData(Object obj) {
            			DataSet data = (DataSet)obj;
            			return data.getCorrespondenceSet();
            		}
            	});
        }

        public Object getValue() {
            return (_entry.isDataComplete()) ? "ok" : null;
        }

        public double getRowWeight() {
            return 20.0;
        }

        private void doEditRemarks() {
            DataSet set =
                (DataSet) ((TabulateTableModel) _table.getModel()).getRow(
                    _table.getSelectedRow());
            if (set == null)
                return;
            CorrespondenceSet tempSet =
                new CorrespondenceSet(_entry.getView().getDatabase(), -1, null);
            tempSet.setRemarks(set.getCorrespondenceSetRemarks());
            AddDialog dialog =
                new AddDialog("pgtEditCorrespondenceSetRemarks", tempSet, null, false);
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {
            	String remarks = tempSet.getRemarks();
            	if (remarks != null)
                    set.setCorrespondenceSetRemarks(remarks);
            }
        }

        private void doAddProtosegment() {
            if (_entry.getGroup() == null)
                return;
            try {
                Zone zone = (Zone) _entry.getGroup().getDatabase().makeObject(Zone.class);
                Protosegment proto = _entry.getView().makeProtosegment(zone);
                AddDialog dialog = new AddDialog("pgtAddProtosegment", proto, null, true);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    _entry.getProtosegments().refresh();
                    setupProtosegmentComboBox(_table.getColumnModel().getColumn(2));
                    TabulateTableModel model = (TabulateTableModel) _table.getModel();
                    DataSet dataset = model.getRow(_table.getSelectedRow());
                    if (dataset != null) {
                        dataset.setProtosegment(proto);
                    }
                    model.refresh();
                }
            } catch (DatabaseException e) {
                e.getRootCause().printStackTrace();
                return;
            }
        }

        private final EntryWrapper _entry;
        private final AlignedTable _table;
    }

    /**
     * Table model class for tabulate elements.
     **/
    private static final class TabulateTableModel extends AbstractTableModel {

        public Object getValueAt(int row, int col) {
            DataSet set = (DataSet) _rows.get(row);
            switch (col) {
                case 0 :
                    return "" + (row + 1);
                case 1 :
                    return set.getCorrespondenceSet();
                case 2 :
                    return set.getProtosegment();
                case 3 :
                    return set.getEnvironment();
                default :
                    return "";
            }
        }

        /*
         * Set values for editable table cells.
         */
        public void setValueAt(Object value, int row, int col) {
            DataSet set = (DataSet) _rows.get(row);
            if (col < 2)
                return;
            if (col == 2) {
                set.setProtosegment((Protosegment) value);
            } else if (col == 3) {
                set.setEnvironment((String) value);
            }
            // indicate change in entry
            fireTableCellUpdated(row, col);
        }

        /*
         * Indicate editable table cells.
         */
        public boolean isCellEditable(int row, int col) {
            return col > 1;
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int i) {
            return AppPrefs.getInstance().getMessages().getString(TAB_COL_KEYS[i]);
        }

        public int getRowCount() {
            return _rows.size();
        }

        DataSet getRow(int index) {
            return (index < 0 || index >= _rows.size()) ? null : (DataSet) _rows.get(index);
        }

        List getRows() {
            return new ArrayList(_rows);
        }

        void setRows(List rows) {
            _rows.clear();
            _rows.addAll(rows);
            refresh();
        }

        public void refresh() {
            fireTableDataChanged();
        }

        private final List _rows = new ArrayList();
    }

    /**
     * Protosegment List Bean.
     **/
    private static final class ProtosegmentList implements Refreshable {

        ProtosegmentList(View view) {
            _view = view;
            refresh();
        }

        public void refresh() {
            if (_view == null)
                return;
            try {
                _protosegments = _view.getProtosegments();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }

        public List getProtosegments() {
            return _protosegments;
        }

        private List _protosegments;
        private View _view;
    }

    /**
    * Data Set Bean.
    **/
    private static final class DataSet {

        public boolean isBelowThreshold(int threshold) {
            // count number of ignores
            int cnt = 0;
            for (int i = 0; i < _correspondenceSet.length(); i++) {
                if (_correspondenceSet.charAt(i) == '.')
                    cnt++;
            }
            return (_correspondenceSet.length() - cnt) < threshold;
        }
        public boolean isComplete() {
            return !(
                _environment == null
                    || _environment.trim().equals("")
                    || _protosegment == null
                    || _protosegment.getProtosegment().trim().equals(""));
        }
        public String getCorrespondenceSet() {
            return _correspondenceSet;
        }
        public void appendCorrespondenceSet(String v) {
            _correspondenceSet += v;
        }

        public String getCorrespondenceSetRemarks() {
            return _correspondenceSetRemarks;
        }
        public void setCorrespondenceSetRemarks(String v) {
            _correspondenceSetRemarks = v;
        }

        public String getEnvironment() {
            return _environment;
        }
        public void setEnvironment(String v) {
            _environment = v;
        }

        public Protosegment getProtosegment() {
            return _protosegment;
        }
        public void setProtosegment(Protosegment v) {
            _protosegment = v;
        }

        private String _correspondenceSet = "";
        private String _correspondenceSetRemarks;
        private String _environment;
        private Protosegment _protosegment;
    }

    /**
     * Task to select alignment class.
     **/
    public static class SelectAlignmentTask implements Task {
        
        public SelectAlignmentTask(List alignments) {
            _alignments = alignments;
        }
   
        public List getAlignments() {
            return _alignments;
        }
                
        public String getMessage() {
            return _message;
        }

        public void setMessage(String value) {
            _message = value;
        }
                
        public void setValue(Object value) {
            _value = value;
        }
                
        public Object getValue() {
            return _value;
        }
                
        /**
         * Run task.
         * Return true to close dialog, false to keep open.
         **/
        public boolean run() {
            return true;
        }

        private String _message;
        private Object _value;
        private List _alignments;
    }

    /**
     * Select Alignment Input Row for refine data.
     **/
    protected static class TabulateSelectAlignmentRow extends InputRow {
        /**
         * Constructor.
         **/
        public TabulateSelectAlignmentRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj);
            _data = (SelectAlignmentTask) obj;
            _refresh = refresh;
            init(_combo, _combo);
            refresh();
        }
    
        /**
         * Refresh this object.
         **/
        public void refresh() {
            ((BasicListModel) _combo.getModel()).setData(_data.getAlignments());
        }
    
        /**
         * Set the value of this row.
         **/
        public void setValue(Object value) {
            refresh();
            _combo.setSelectedItem(value);
        }
    
        /**
         * Get the value of this row.
         **/
        public Object getValue() {
            return _combo.getSelectedItem();
        }
    
        protected final JComboBox _combo = new JComboBox(new BasicListModel() {
            public void setSelectedItem(Object o) {
                super.setSelectedItem(o);
                _data.setValue(o);
                try {
                    _refresh.refresh();
                } catch (DatabaseException ignored) {
                }
            }
        });
        protected final Refreshable _refresh;
        protected final SelectAlignmentTask _data;
    }
    
    //    private ViewChooser _chooser;
    private final ViewEntryPanel _mainPanel;
    private final WordCollection _collection;
}