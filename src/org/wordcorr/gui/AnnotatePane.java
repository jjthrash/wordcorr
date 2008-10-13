package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.wordcorr.AppProperties;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Alignment;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Entry;
import org.wordcorr.db.Group;
import org.wordcorr.db.Persistent;
import org.wordcorr.db.Variety;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Setting;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.AlignmentVectorTextField;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.IPAKeyListener;


/**
 * Pane for entering and editing data.
 * @author Keith Hamasaki, Jim Shiba
 **/
class AnnotatePane extends JPanel implements Refreshable {

    private static final String[] COL_KEYS =
        { "lblTag", "lblAbbreviation", "lblShortName", "lblAligned", "lblBlank" };

    /**
     * Constructor.
     **/
    AnnotatePane(WordCollection collection) {
        super(new BorderLayout());
        _collection = collection;
        _mainPanel = new ViewEntryPanel();
        add(_mainPanel, BorderLayout.CENTER);
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
     * AddEditDelete panel for view entries.
     **/
    private final class ViewEntryPanel extends AddEditDeletePanel {

        ViewEntryPanel() {
            super(null, false);

            // change find label
            setFindButtonLabel(
                AppPrefs.getInstance().getMessages().getString("btnFindAnnotate"));

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
            Setting setting = _collection.getDatabase().getCurrentSetting();
            View view = _collection.getViewByID(setting.getViewID());
            if (view != null) {
                view.revert();
            }
            List entries = _collection.getEntries();
            List wrappers = new ArrayList(entries.size());
            for (Iterator it = entries.iterator(); it.hasNext();) {
                wrappers.add(new EntryWrapper((Entry) it.next(), view));
            }
            ((BasicListModel) getList().getModel()).setData(wrappers);

            // set selection
            selectDefault();
        }
        
        protected FindDialog createFindDialog() {
            return new FindDialog("GlossFindDialog");
        }
    }

    /**
     * Wrapper bean class for an entry.
     **/
    public static final class EntryWrapper implements Persistent {
        EntryWrapper(Entry entry, View view) throws DatabaseException {
            _entry = entry;
            _view = view;
        }

        // Attributes
        public long getID() {
            return _entry.getID();
        }
        public Integer getEntry() {
            return _entry.getEntryNum();
        }
        public Entry getEntryObject() {
            return _entry;
        }
        public String getGloss() {
            return _entry.getName();
        }
        public String getGloss2() {
            return _entry.getGloss2();
        }
        public List getData() {
            try {
                if (_alignments == null && _view != null) {
                    _alignments = _view.getAlignments(_entry);
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
            return _alignments;
        }
        public void setData(List data) {
            _alignments = data;
            setDirty();
        }

        // Persistent methods
        public String checkValidation() throws DatabaseException {
            return null;
        }

        public void save() throws DatabaseException {
            clearDirty();
            for (Iterator it = getData().iterator(); it.hasNext();) {
                ((Alignment) it.next()).save();
            }
            _view.deleteUnusedGroups(_entry);
        }

        public void delete() throws DatabaseException {
            clearDirty();
        }

        public void revert() throws DatabaseException {
            clearDirty();
            if (_view != null) {
                _alignments = _view.getAlignments(_entry);
            }
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

        View getView() {
            return _view;
        }
        public String toString() {
            return (_dirty ? "+> " : "") + _entry.getName();
        }

        private boolean _dirty = false;
        private final Entry _entry;
        private final View _view;
        private List _alignments;
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
            model.addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent evt) {
                    _entry.setData(model.getRows());
                    try {
                        refresh.refresh();
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    }
                }
            });
            // set varieties for sorting
            model.setVarieties(_entry.getView().getMembers());

            _table = new AlignedTable(model, 3);
            _table.setRowHeight(
                new Double(
                    Math.ceil(
                        new JTextField().getFont().getSize()
                            * Double.parseDouble(AppProperties.getProperty("RowHeightFactor"))
                            + _table.getRowMargin() * 2))
                    .intValue());

            // set initial column widths
            _table.setAutoCreateColumnsFromModel(false);
            int[] colWidth = { 10, 10, 100, 300, 10 };
            for (int i = 0; i < 5; i++) {
                TableColumn col = _table.getColumnModel().getColumn(i);
                col.setPreferredWidth(colWidth[i]);
            }

            _table.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2
                        && ((evt.getModifiers() & evt.BUTTON1_MASK) > 0)) {
                        doEdit();
                    }
                }
            });

            _table.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        doEdit();
                        evt.consume();
                    }
                }
            });

            // setup buttons
            JPanel btnpanel = new JPanel(new WrapFlowLayout(FlowLayout.LEFT));
            btnpanel.add(new WButton(new WordCorrAction("btnEditDatum", "accEditDatum") {
                public void actionPerformed(ActionEvent evt) {
                    doEdit();
                }
            }), BorderLayout.NORTH);
            btnpanel
                .add(new WButton(new WordCorrAction(
                    "btnAnnotateCopyVector",
                    "accAnnotateCopyVector") {
                public void actionPerformed(ActionEvent evt) {
                    doCopyVector();
                }
            }), BorderLayout.NORTH);
            _replaceVectorButton =
                new WButton(new WordCorrAction(
                    "btnAnnotateReplaceVector",
                    "accAnnotateReplaceVector") {
                public void actionPerformed(ActionEvent evt) {
                    doReplaceVector();
                }
            });
            _replaceVectorButton.setEnabled(false);
            btnpanel.add(_replaceVectorButton, BorderLayout.NORTH);
            btnpanel
                .add(new WButton(new WordCorrAction(
                    "btnGraphemeClusterDefine",
                    "accGraphemeClusterDefine") {
                public void actionPerformed(ActionEvent evt) {
                    _alignmentVectorTextField.defineGraphemeCluster();
                }
            }), BorderLayout.NORTH);
            btnpanel
                .add(new WButton(new WordCorrAction(
                    "btnGraphemeClusterUncluster",
                    "accGraphemeClusterUncluster") {
                public void actionPerformed(ActionEvent evt) {
                    _alignmentVectorTextField.unclusterGraphemeCluster();
                }
            }), BorderLayout.NORTH);

            // assemble panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(btnpanel, BorderLayout.NORTH);
            panel.add(new JScrollPane(_table), BorderLayout.CENTER);
            setupVectorText(_table.getColumnModel().getColumn(3));

            // monitor group tag change
            JTextField textField = new JTextField();
            textField.addKeyListener(new IPAKeyListener(textField));
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent evt) {
                    docUpdate(evt);
                }
                public void removeUpdate(DocumentEvent evt) {
                    // note:  if all characters deleted, unwanted edit message appears.
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
                            0);
                    } catch (javax.swing.text.BadLocationException e) {
                        // this should not happen
                        e.printStackTrace();
                    }
                }
            });
            _table.getColumnModel().getColumn(0).setCellEditor(
                new DefaultCellEditor(textField));
            init(panel, _table);
        }

        public void setupVectorText(TableColumn col) {
            //Set up the editor for the vector cells.
            DefaultCellEditor editor = (DefaultCellEditor) col.getCellEditor();
            if (editor == null) {
                _alignmentVectorTextField = new AlignmentVectorTextField();
                _alignmentVectorTextField.setFont(FontCache.getIPA());
                col.setCellEditor(new DefaultCellEditor(_alignmentVectorTextField) {
                    public Component getTableCellEditorComponent(
                        JTable table,
                        Object value,
                        boolean isSelected,
                        int row,
                        int column) {
                        Alignment alignment = (Alignment) value;
                        AlignmentVectorTextField comp = (AlignmentVectorTextField) getComponent();
                        comp.setAlignment(alignment);
                        comp.setValue(alignment.getVector());

                        // Note: Need to bypass AlignmentDocument.insertString because
                        // getTableCellEditorComponent makes call to setup AlignedDatum value
                        // which causes illegal character message to be invoked.
                        (
                            (AlignmentVectorTextField.AlignmentDocument) comp
                                .getDocument())
                                .setDisableInsertString(
                            true);
                        Component editor =
                            super.getTableCellEditorComponent(table, value, isSelected, row, column);
                        (
                            (AlignmentVectorTextField.AlignmentDocument) comp
                                .getDocument())
                                .setDisableInsertString(
                            false);
                        return editor;
                    }
                });
            } else {
                _alignmentVectorTextField = (AlignmentVectorTextField) editor.getComponent();
            }
        }

        public void setValue(Object value) {
            AlignmentTableModel model = (AlignmentTableModel) _table.getModel();
            model.setRows((List) value);
            _table.setAlignedPositionWidths((List) value, new AlignedDataExtractor() {
                public String getColumnData(Object obj) {
                    Alignment data = (Alignment) obj;
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

        private void doEdit() {
            Alignment alignment =
                ((AlignmentTableModel) _table.getModel()).getRow(_table.getSelectedRow());
            if (alignment == null)
                return;

            // recognize and propose grapheme clusters
            recognizeGraphemeCluster(alignment);

            AddDialog dialog = new AddDialog("pgtEditAlignment", alignment, null, false);
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {
                ((AlignmentTableModel) _table.getModel()).refresh();
                Group group = alignment.getGroup();
                if (group != null && group.isDirty()) {
                    try {
                        group.save();
                    } catch (DatabaseException ignored) {}
                }

                // save changes
                try {
                    _entry.save();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // revert if windows closed without cancelling
                    alignment.revert();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Recognize and propose Grapheme Cluster Definition in Alignment from View list.
         **/
        private final void recognizeGraphemeCluster(Alignment alignment) {
            Messages messages = AppPrefs.getInstance().getMessages();
            try {
                List clusters = alignment.getView().getGraphemeClusters();
                String vector = alignment.getVector();
                String alignedDatum = alignment.getAlignedDatum();
                String datum = alignment.getDatum().getName();

                int datumpos = 0;
                boolean skip = false;
                for (int i = 0; i < alignedDatum.length(); i++) {
                    switch (alignedDatum.charAt(i)) {
                        case Alignment.GRAPHEME_CLUSTER_START :
                            skip = true;
                            break;
                        case Alignment.GRAPHEME_CLUSTER_END :
                            skip = false;
                            break;
                        case Alignment.INDEL_SYMBOL :
                        case Alignment.EXCLUDE_SYMBOL :
                            break;
                        default :
                            if (!skip) {
                                // check in grapheme cluster list
                                boolean done = false;
                                for (Iterator it = clusters.iterator(); it.hasNext() && !done;) {
                                    String graphemeCluster = (String) it.next();
                                    if (alignedDatum.substring(i).startsWith(graphemeCluster)) {
                                        // propose match
                                        String proposal =
                                            datum.substring(0, datumpos)
                                                + " {"
                                                + graphemeCluster
                                                + "} "
                                                + datum.substring(datumpos + graphemeCluster.length());

                                        if (Dialogs
                                            .confirm(
                                                messages.getCompoundMessage(
                                                    "msgRecognizeGraphemeCluster",
                                                    new Object[] { graphemeCluster, proposal }))) {
                                            // define grapheme cluster
                                            String newvector = vector.substring(0, i) + Alignment.GRAPHEME_CLUSTER_START;
                                            if (i + graphemeCluster.length() < alignedDatum.length()) {
                                                newvector += vector.substring(i, i + graphemeCluster.length())
                                                    + Alignment.GRAPHEME_CLUSTER_END
                                                    + vector.substring(i + graphemeCluster.length());
                                            } else {
                                                // last cluster
                                                newvector += vector.substring(i) + Alignment.GRAPHEME_CLUSTER_END;
                                            }
                                            alignment.setVector(newvector);
                                            alignment.save();

                                            vector = newvector;
                                            alignedDatum = alignment.getAlignedDatum();
                                            i += graphemeCluster.length() + 1;
                                            datumpos += graphemeCluster.length() - 1;
                                            done = true;
                                        }
                                    }
                                }

                            }
                            ++datumpos;
                            break;
                    }
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }

        private void doCopyVector() {
            Alignment alignment =
                ((AlignmentTableModel) _table.getModel()).getRow(_table.getSelectedRow());
            if (alignment == null)
                return;

            // copy vector
            Messages messages = AppPrefs.getInstance().getMessages();
            if (Dialogs
                .confirm(
                    messages.getCompoundMessage(
                        "msgAnnotateConfirmCopyVector",
                        alignment.getVector()))) {
                _vector = alignment.getVector();
                _replaceVectorButton.setEnabled(true);
            }
        }

        private void doReplaceVector() {
            int[] selectedRows = _table.getSelectedRows();
            if (selectedRows.length == 0)
                return;

            Messages messages = AppPrefs.getInstance().getMessages();
            if (Dialogs
                .confirm(
                    messages.getCompoundMessage("msgAnnotateConfirmReplaceVector", _vector))) {

                // get hold count in vector
                int vectorHoldCount = 0;
                for (int i = 0; i < _vector.length(); i++) {
                    char ch = _vector.charAt(i);
                    if (ch == Alignment.HOLD_SYMBOL) {
                        ++vectorHoldCount;
                    }
                }

                // process each alignment
                ArrayList alignmentExceptions = new ArrayList();
                for (int i = 0; i < selectedRows.length; i++) {
                    Alignment alignment =
                        ((AlignmentTableModel) _table.getModel()).getRow(selectedRows[i]);
                    if (alignment == null)
                        continue;

                    // check vector hold count
                    if (alignment.getDatum().getName().length() == vectorHoldCount) {
                        // replace vector
                        alignment.setVector(_vector);
                    } else {
                        alignmentExceptions.add(alignment);
                    }
                }
                // display exception list
                if (!alignmentExceptions.isEmpty()) {
                    StringBuffer list = new StringBuffer();
                    for (Iterator it = alignmentExceptions.iterator(); it.hasNext();) {
                        Alignment exception = (Alignment) it.next();

                        list.append(
                            (list.length() == 0)
                                ? exception.getDatum().getName()
                                : ", " + exception.getDatum().getName());
                    }
                    Dialogs.msgbox(
                        messages.getCompoundMessage(
                            "msgAnnotateReplaceVectorException",
                            list.toString()));
                }
                ((AlignmentTableModel) _table.getModel()).refresh();

                // save changes
                try {
                    _entry.save();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }

        private final EntryWrapper _entry;
        private final AlignedTable _table;
        private WButton _replaceVectorButton;
        private String _vector = "";
    }

    /**
     * Table model class for data elements.
     **/
    private static final class AlignmentTableModel
        extends AbstractTableModel
        implements Refreshable {

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
                    // alignment for alignment vector
                    return alignment;
                case 4 :
                    // determine Metathesis and Remarks flags
                    String flags = "";
                    if (!(alignment.getMetathesis().equals("0, 0, 0, 0")
                        || alignment.getMetathesis().equals("")))
                        flags += "M";
                    if (!alignment.getObservations().equals(""))
                        flags += "R";
                    return flags;
                default :
                    return "";
            }
        }

        /*
         * Set values for editable table cells.
         */
        public void setValueAt(Object value, int row, int col) {
            // group tag
            if (col == 0) {
                String tag = ((String) value).trim();
                Alignment alignment = (Alignment) _rows.get(row);

                // set default to ?
                if (tag.equals("")) {
                    Dialogs.msgbox(
                        AppPrefs.getInstance().getMessages().getCompoundMessage(
                            "cmpRequiredCell",
                            "Tag"));
                    tag = "?";
                }

                // set Group Tag
                try {
                    View view = alignment.getView();
                    Entry entry = alignment.getDatum().getEntry();
                    Group group = view.getGroup(tag, entry);
                    alignment.setGroup(group);
                    alignment.save();
                    view.deleteUnusedGroups(entry);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            } else if (col == 3) {
                Alignment alignment = (Alignment) _rows.get(row);

                // set Vector
                try {
                    // get vector from text field
                    alignment.setVector((String) _alignmentVectorTextField.getValue());
                    alignment.save();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
            // indicate change
            fireTableCellUpdated(row, col);
        }

        /*
         * Indicate editable table cells.
         */
        public boolean isCellEditable(int row, int col) {
            return col == 0 || col == 3;
        }

        public int getColumnCount() {
            return 5;
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

        void setVarieties(List varieties) {
            _varieties.clear();
            _varieties.addAll(varieties);
        }

        void sortRows() {
            Collections.sort(_rows, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Alignment a1 = (Alignment) o1;
                    Alignment a2 = (Alignment) o2;

                    Group g1 = a1.getGroup();
                    Group g2 = a2.getGroup();

                    // sort by group first
                    if (g1 != null && g2 != null) {
                        String t1 = a1.getGroup().getName();
                        String t2 = a2.getGroup().getName();

                        if (t1 != null && t2 != null) {
                            int comp = t1.compareTo(t2);
                            if (comp != 0)
                                return comp;
                        }
                    }

                    // sort by variety
                    int i1 = _varieties.indexOf(a1.getDatum().getVariety());
                    int i2 = _varieties.indexOf(a2.getDatum().getVariety());

                    return i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
                }
            });

        }

        public void refresh() {
            // sort order of datums
            sortRows();
            fireTableStructureChanged();
        }

        private final List _rows = new ArrayList();
        private final List _varieties = new ArrayList();
    }

    private final ViewEntryPanel _mainPanel;
    private final WordCollection _collection;
    private static AlignmentVectorTextField _alignmentVectorTextField;
}