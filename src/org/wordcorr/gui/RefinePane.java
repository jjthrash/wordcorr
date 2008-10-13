package org.wordcorr.gui;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.wordcorr.AppProperties;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Cluster;
import org.wordcorr.db.CorrespondenceSet;
import org.wordcorr.db.Database;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Group;
import org.wordcorr.io.InfinitePipeInputStream;
import org.wordcorr.io.InfinitePipeOutputStream;
import org.wordcorr.db.Protosegment;
import org.wordcorr.db.RetrieveAllParameters;
import org.wordcorr.db.SummaryFile;
import org.wordcorr.db.Setting;
import org.wordcorr.db.User;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.XMLFile;
import org.wordcorr.db.Zone;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.MultiSorter;
/**
 * Pane for doing tabulation refinement.
 * @author Jim Shiba
 **/
class RefinePane extends ButtonPanel implements Refreshable {

    private static final String[] COL_KEYS =
        {
            "lblRefineTableZone",
            "lblRefineTableProtosegment",
            "lblRefineTableEnvironment",
            "lblRefineTableClusterOrder",
            "lblRefineTableCorrespondenceSet",
            "lblRefineTableCitationCount",
            "lblRefineTableCitation",
            "lblBlank" };

    /**
     * Constructor.
     **/
    RefinePane(WordCollection collection) {
        super(new JPanel(new BorderLayout()));

        _collection = collection;

        // do the list selection for this table
        final JPanel panel = (JPanel) getMainComponent();
        addButtons();

        // create interface table
        _tableModel = new RefineTableModel();
        _table = new AlignedTable(_tableModel, 4, new RefineCellAlignedRenderer());

        _table.setRowHeight(
            new Double(
                Math.ceil(
                    new JTextField().getFont().getSize()
                        * Double.parseDouble(AppProperties.getProperty("RowHeightFactor"))
                        + _table.getRowMargin() * 2))
                .intValue());
        _table.setDefaultRenderer(Object.class, new RefineCellRenderer());
        _table.getColumnModel().getColumn(1).setCellRenderer(new RefineCellRenderer(FontCache.getIPA()));
        _table.getColumnModel().getColumn(2).setCellRenderer(new RefineCellRenderer(FontCache.getIPA()));

        // set initial column widths
        _table.setAutoCreateColumnsFromModel(false);
        int[] colWidth = { 200, 25, 75, 15, 250, 10, 75, 5 };
        for (int i = 0; i < 8; i++) {
            TableColumn col = _table.getColumnModel().getColumn(i);
            col.setPreferredWidth(colWidth[i]);
        }

        // double click displays citation info
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    displayCitations();
                }
            }
        };
        _table.addMouseListener(mouseListener);

        panel.add(new JScrollPane(_table), BorderLayout.CENTER);

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
        // set table
        Setting setting = _collection.getDatabase().getCurrentSetting();
        final View view = _collection.getViewByID(setting.getViewID());
        if (view != null) {
            List rows = view.getRefineTable();
            _tableModel.setRows(rows, view);
            _table.setAlignedPositionWidths(rows, new AlignedDataExtractor() {
                public String getColumnData(Object obj) {
                    Object[] objs = (Object[]) obj;
                    RefineData data = new RefineData(objs, view);
                    return data.getCorrespondenceSet();
                }
            });
        } else {
            // clear table
            _tableModel.setRows(Collections.EMPTY_LIST, view);
        }
    }

    /**
     * Add buttons
     **/
    private void addButtons() {
        JButton moveCluster =
            new AlignedButton(
                "btnRefineMoveCluster",
                "accRefineMoveCluster",
                SwingConstants.CENTER);
        moveCluster.setActionCommand("RefineMoveCluster");
        moveCluster.addActionListener(_listener);

        JButton moveCorrespondenceSet =
            new AlignedButton(
                "btnRefineMoveCorrespondenceSet",
                "accRefineMoveCorrespondenceSet",
                SwingConstants.CENTER);
        moveCorrespondenceSet.setActionCommand("RefineMoveCorrespondenceSet");
        moveCorrespondenceSet.addActionListener(_listener);

        JButton moveProtosegment =
            new AlignedButton(
                "btnRefineMoveProtosegment",
                "accRefineMoveProtosegment",
                SwingConstants.CENTER);
        moveProtosegment.setActionCommand("RefineMoveProtosegment");
        moveProtosegment.addActionListener(_listener);

        JButton changeEnvironment =
            new AlignedButton(
                "btnRefineChangeEnvironment",
                "accRefineChangeEnvironment",
                SwingConstants.CENTER);
        changeEnvironment.setActionCommand("RefineChangeEnvironment");
        changeEnvironment.addActionListener(_listener);

        JButton changeProtosegment =
            new AlignedButton(
                "btnRefineChangeProtosegment",
                "accRefineChangeProtosegment",
                SwingConstants.CENTER);
        changeProtosegment.setActionCommand("RefineChangeProtosegment");
        changeProtosegment.addActionListener(_listener);

        JButton mergeClusters =
            new AlignedButton(
                "btnRefineMergeClusters",
                "accRefineMergeClusters",
                SwingConstants.CENTER);
        mergeClusters.setActionCommand("RefineMergeClusters");
        mergeClusters.addActionListener(_listener);

        JButton mergeProtosegments =
            new AlignedButton(
                "btnRefineMergeProtosegments",
                "accRefineMergeProtosegments",
                SwingConstants.CENTER);
        mergeProtosegments.setActionCommand("RefineMergeProtosegments");
        mergeProtosegments.addActionListener(_listener);

        JButton reorderClusters =
            new AlignedButton(
                "btnRefineReorderClusters",
                "accRefineReorderClusters",
                SwingConstants.CENTER);
        reorderClusters.setActionCommand("RefineReorderClusters");
        reorderClusters.addActionListener(_listener);

        JButton retabulateGroup =
            new AlignedButton(
                "btnRefineRetabulateGroup",
                "accRefineRetabulateGroup",
                SwingConstants.CENTER);
        retabulateGroup.setActionCommand("RefineRetabulateGroup");
        retabulateGroup.addActionListener(_listener);

        JButton addProtosegment =
            new WButton(new WordCorrAction(
                "btnRefineAddProtosegment",
                "accRefineAddProtosegment") {
            public void actionPerformed(ActionEvent evt) {
                addProtosegment();
            }
        });

        JButton deleteProtosegment =
            new WButton(new WordCorrAction(
                "btnRefineDeleteProtosegment",
                "accRefineDeleteProtosegment") {
            public void actionPerformed(ActionEvent evt) {
                deleteProtosegment();
            }
        });

        JButton displayCitations =
            new WButton(new WordCorrAction(
                "btnRefineDisplayCitations",
                "accRefineDisplayCitations") {
            public void actionPerformed(ActionEvent evt) {
                displayCitations();
            }
        });

        JButton editRemarks =
            new WButton(new WordCorrAction("btnEditRemarks", "accEditRemarks") {
            public void actionPerformed(ActionEvent evt) {
                editRemarks();
            }
        });

        JButton summarize =
            new WButton(new WordCorrAction("btnRefineSummarize", "accRefineSummarize") {
            public void actionPerformed(ActionEvent evt) {
                summarize();
            }
        });
        
        JButton results =            
            new WButton(new WordCorrAction("btnRefineResults", "accRefineResults") {
            public void actionPerformed(ActionEvent evt) {
                outputResults(evt);
            }
        });

        addButton(addProtosegment);
        addButton(deleteProtosegment);
        addButton(moveProtosegment);
        addButton(mergeProtosegments);
        addButton(changeProtosegment);
        addSeparator();
        addButton(moveCluster);
        addButton(mergeClusters);
        addButton(reorderClusters);
        addButton(changeEnvironment);
        addSeparator();
        addButton(moveCorrespondenceSet);
        addButton(retabulateGroup);
        addSeparator();
        addButton(editRemarks);
        addSeparator();
        addButton(displayCitations);
        addButton(summarize);
        addButton(results);
    }

    /**
     * Action Listener for the buttons.
     **/
    private class ButtonListener implements ActionListener {

        /**
         * Action Listener method.
         **/
        public void actionPerformed(ActionEvent evt) {
            Messages messages = AppPrefs.getInstance().getMessages();
            Dialogs.showWaitCursor(RefinePane.this);
            int[] selectedRows = _table.getSelectedRows();
            if (selectedRows.length == 0)
                Dialogs.msgbox(messages.getString("msgRefineSelectRow"));
            for (int i = 0; i < selectedRows.length; i++) {
                RefineData data =
                    (RefineData) ((RefineTableModel) _table.getModel()).getRow(selectedRows[i]);
                if (data == null)
                    continue;

                String cmd = ((JButton) evt.getSource()).getActionCommand();
                data.setTask(cmd);
                TaskDialog dialog = new TaskDialog("lbl" + cmd, data, cmd);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    if (data.getRefresh()) {
                        try {
                            refresh();
                        } catch (DatabaseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!data.runMultiple())
                    break;
            }
            Dialogs.showDefaultCursor(RefinePane.this);
        }
    }

    /**
     * Table cell renderer class.
     * Background color based on cluster order.
     **/
    private static final class RefineCellRenderer
        extends JLabel
        implements TableCellRenderer {

        public RefineCellRenderer() {
            super();
            init();
        }
        
        private void init() {
            // necessary for background color change
            setOpaque(true);

            // setup colors
            int ncolors =
                Integer.parseInt(AppProperties.getProperty("RefinePaneRowColors"));
            _colors = new Color[ncolors];
            for (int i = 0; i < ncolors; i++) {
                _colors[i] = Color.decode(AppProperties.getProperty("RefinePaneRowColor" + i));
            }
            _selectColor =
                Color.decode(AppProperties.getProperty("RefinePaneRowColorSelect"));
            _repeatColorCount = _colors.length - 1;
        }
        
        public RefineCellRenderer(Font font) {
            super();
            init();
            setFont(font);
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            // get RefineData from table
            RefineData data =
                (RefineData) table.getModel().getValueAt(row, RefineData.GET_DATA);

            // set background color based on cluster order
            // first color constant, others repeat
            int colori = 0;
            if (data.getClusterOrder().intValue() > 1) {
                colori = (data.getClusterOrder().intValue() - 1) % _repeatColorCount;
                if (colori == 0)
                    colori = _repeatColorCount;
            }
            setBackground((isSelected) ? _selectColor : _colors[colori]);

            setText((String) value);

            return this;
        }

        private int _repeatColorCount;
        private Color[] _colors;
        private Color _selectColor;
    }

    /**
     * Table cell renderer class for Aligned characters.
     * Background color based on cluster order.
     **/
    private static final class RefineCellAlignedRenderer
        extends AlignedTableCellRenderer {

        public RefineCellAlignedRenderer() {
            super();

            // setup colors
            int ncolors =
                Integer.parseInt(AppProperties.getProperty("RefinePaneRowColors"));
            _colors = new Color[ncolors];
            for (int i = 0; i < ncolors; i++) {
                _colors[i] = Color.decode(AppProperties.getProperty("RefinePaneRowColor" + i));
            }
            _selectColor =
                Color.decode(AppProperties.getProperty("RefinePaneRowColorSelect"));
            _repeatColorCount = _colors.length - 1;
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

            // get RefineData from table
            RefineData data =
                (RefineData) table.getModel().getValueAt(row, RefineData.GET_DATA);

            // set background color based on cluster order
            // first color constant, others repeat
            int colori = 0;
            if (data.getClusterOrder().intValue() > 1) {
                colori = (data.getClusterOrder().intValue() - 1) % _repeatColorCount;
                if (colori == 0)
                    colori = _repeatColorCount;
            }
            setBackground((isSelected) ? _selectColor : _colors[colori]);

            return this;
        }

        private int _repeatColorCount;
        private Color[] _colors;
        private Color _selectColor;
    }

    /**
     * Table model class for data elements.
     **/
    private static final class RefineTableModel extends AbstractTableModel {

        public Object getValueAt(int row, int col) {
            RefineData data = (RefineData) _rows.get(row);
            switch (col) {
                case 0 :
                    return data.getZone();
                case 1 :
                    return data.getProtosegment();
                case 2 :
                    return data.getEnvironment();
                case 3 :
                    return data.getClusterOrder().toString();
                case 4 :
                    return data.getCorrespondenceSet();
                case 5 :
                    return data.getCitationCount() + "";
                case 6 :
                    return data.getCitation();
                case 7 :
                    String hasRemarks = "";
                    hasRemarks += (data.hasProtosegmentRemarks()) ? "P" : "";
                    hasRemarks += (data.hasClusterRemarks()) ? "C" : "";
                    hasRemarks += (data.hasCorrespondenceSetRemarks()) ? "S" : "";
                    return hasRemarks;
                case RefineData.GET_DATA :
                    return data;
                default :
                    return "";
            }
        }

        public int getColumnCount() {
            return 8;
        }

        public String getColumnName(int i) {
            return AppPrefs.getInstance().getMessages().getString(COL_KEYS[i]);
        }

        public RefineData getRow(int index) {
            return (index < 0 || index >= _rows.size())
                ? null
                : (RefineData) _rows.get(index);
        }

        public int getRowCount() {
            return _rows.size();
        }

        void setRows(List rows, View view) {
            _rows = new ArrayList();
            RefineData previous = null;
            for (Iterator it = rows.iterator(); it.hasNext();) {
                Object[] objs = (Object[]) it.next();
                RefineData data = new RefineData(objs, view);
                if (previous != null) {
                    if (data.getSet().equals(previous.getSet())) {
                        previous.add(data.getCitation(), data.getKeys());
                    } else {
                        _rows.add(data);
                        previous = data;
                    }
                } else {
                    _rows.add(data);
                    previous = data;
                }
            }
            refresh();
        }

        public void refresh() {
            fireTableStructureChanged();
        }

        private List _rows = Collections.EMPTY_LIST;
    }

    /**
     * Refine data class.
     **/
    public static class RefineData implements Task {

        public RefineData() {}

        public RefineData(int type, View view) {
            // type used for custom row display for dialog boxes
            _type = type;
            _view = view;
        }

        public RefineData(Object[] data, View view) {
            _zone = (String) data[0];
            _protosegment = (String) data[1];
            _environment = (String) data[2];
            _clusterOrder = (Integer) data[3];
            _correspondenceSet = (String) data[4];
            _citation = (String) data[5];
            _hasProtosegmentRemarks = !((data[6] == null) || ((String) data[6]).equals(""));
            _hasClusterRemarks = !((data[7] == null) || ((String) data[7]).equals(""));
            _hasCorrespondenceSetRemarks =
                !((data[8] == null) || ((String) data[8]).equals(""));
            addKeys((String) data[9]);
            _view = view;
        }

        public int getType() {
            return _type;
        }

        public String getCitation() {
            return _citation;
        }

        public String getCitation(int idx) {
            String val = _citation + ",";
            int i1 = 0;
            int i2 = -1;
            for (int i = 0; i <= idx; i++) {
                i1 = i2 + 1;
                i2 = val.indexOf(",", i1);
            }
            return val.substring(i1, i2).trim();
        }

        public int getCitationCount() {
            return _keys.size();
        }

        public long getCitationKey(int idx) {
            return getKey(idx, 5);
        }

        public long getClusterKey() {
            return getKey(0, 3);
        }

        public Integer getClusterOrder() {
            return _clusterOrder;
        }

        public String getClusterOrderHierarchy() {
            return _protosegment + " | " + _environment + " | " + _clusterOrder;
        }

        public void setClusterOrder(Integer val) {
            _clusterOrder = val;
        }

        public String getCorrespondenceSet() {
            return _correspondenceSet;
        }

        public String getCorrespondenceSetHierarchy() {
            return _protosegment
                + " | "
                + _environment
                + " | "
                + _clusterOrder
                + " | "
                + _correspondenceSet;
        }

        public long getCorrespondenceSetKey() {
            return getKey(0, 4);
        }

        public String getEnvironment() {
            return _environment;
        }

        public void setEnvironment(String val) {
            _environment = val;
        }

        public long getGroupKey(int idx) {
            return getKey(idx, 6);
        }

        public String getProtosegment() {
            return _protosegment;
        }

        public void setProtosegment(String val) {
            _protosegment = val;
        }

        public String getProtosegmentHierarchy() {
            return _zone + " | " + _protosegment;
        }

        public long getProtosegmentKey() {
            return getKey(0, 2);
        }

        public String getZone() {
            return _zone;
        }

        public void setZone(String val) {
            _zone = val;
        }

        public long getZoneKey() {
            return getKey(0, 1);
        }

        public boolean hasClusterRemarks() {
            return _hasClusterRemarks;
        }

        public boolean hasCorrespondenceSetRemarks() {
            return _hasCorrespondenceSetRemarks;
        }

        public boolean hasProtosegmentRemarks() {
            return _hasProtosegmentRemarks;
        }

        public String getRemarks(String type) {
            return (String) _remarks.get(type);
        }

        public void setRemarks(String type, String val) {
            _remarks.put(type, (val == null) ? "" : val);
        }

        public void setRemarksRow(InputRow row) {
            _remarksRow = row;
            _remarksType = null;
        }

        public void useRemarksRow(String type) {
            if (_remarksType != null)
                setRemarks(_remarksType, (String) _remarksRow.getValue());
            _remarksType = type;
            _remarksRow.setValue(getRemarks(type));
        }

        private long getKey(int idx, int typeIdx) {
            String val = (String) _keys.get(idx) + ":";
            int i1 = 0;
            int i2 = -1;
            for (int i = 0; i < typeIdx; i++) {
                i1 = i2 + 1;
                i2 = val.indexOf(":", i1);
            }
            return Long.parseLong(val.substring(i1, i2));
        }

        public String getKeys() {
            return (String) _keys.get(0);
        }

        public void addKeys(String val) {
            _keys.add(val);
        }

        public String getSet() {
            return _zone
                + ":"
                + _protosegment
                + ":"
                + _environment
                + ":"
                + _clusterOrder
                + ":"
                + _correspondenceSet;
        }

        public void add(String citation, String keys) {
            _citation += ", " + citation;
            addKeys(keys);
        }

        public void setTask(String task) {
            _task = task;
        }

        public Object getValue() {
            return _value;
        }

        public void setValue(Object value) {
            _value = value;
        }

        public Object getValue2() {
            return _value2;
        }

        public void setValue2(Object val) {
            _value2 = val;
        }

        public void setRefresh(boolean v) {
            // data changed if true, refresh refine list
            _refresh = v;
        }

        public boolean getRefresh() {
            return _refresh;
        }

        public String toString() {
            switch (_type) {
                case MERGE_PROTOSEGMENTS :
                case MOVE_CLUSTER :
                case MOVE_CORRESPONDENCE_SET :
                    //return getProtoSegmentHierarchy();
                    return "<html>"+_zone+"<font face=\""+FontCache.getIPA().getFontName()+"\"> | "+_protosegment+"</font></html>";
                case MERGE_CLUSTERS :
                case MOVE_CORRESPONDENCE_SET_TO_CLUSTER :
                    //return getClusterOrderHierarchy();
                    return "<html><font face=\""+FontCache.getIPA().getFontName()+"\">"+_protosegment + " | " + _environment + " | </font>" + _clusterOrder;
                case MOVE_PROTOSEGMENT :
                    return getZone();
            }
            return "";
        }

        /**
         * Run task.
         * Return true to close dialog, false to keep open.
         **/
        public boolean run() {
            // check to see if value has been set
            if (_value == null)
                return true;

            // run task
            if (_task.equals("RefineChangeEnvironment")) {
                return changeEnvironment(this, (String) _value);
            } else if (_task.equals("RefineChangeProtosegment")) {
                return changeProtosegment(this, (String) _value);
            } else if (_task.equals("RefineMergeClusters")) {
                return mergeClusters(this, (RefineData) _value);
            } else if (_task.equals("RefineMergeProtosegments")) {
                return mergeProtosegments(this, (RefineData) _value);
            } else if (_task.equals("RefineMoveCluster")) {
                return moveCluster(this, (RefineData) _value);
            } else if (_task.equals("RefineMoveCorrespondenceSet")) {
                return moveCorrespondenceSet(this, (RefineData) _value);
            } else if (_task.equals("RefineMoveProtosegment")) {
                return moveProtosegment(this, (RefineData) _value);
            } else if (_task.equals("RefineReorderClusters")) {
                return reorderClusters(this, (List) _value);
            } else if (_task.equals("RefineRetabulateGroup")) {
                return retabulateGroup(this, (String) _value);
            }
            return true;
        }

        /**
         * Indicates whether multiple tasks can be run when multiple rows selected.
         **/
        public boolean runMultiple() {
            return (_task.equals("RefineMoveCorrespondenceSet"));
        }

        public Cluster toCluster() throws DatabaseException {
            Protosegment protosegment = toProtosegment();
            return protosegment.getCluster(getClusterKey());
        }

        public CorrespondenceSet toCorrespondenceSet() throws DatabaseException {
            Cluster cluster = toCluster();
            return cluster.getCorrespondenceSet(getCorrespondenceSetKey());
        }

        public Protosegment toProtosegment() throws DatabaseException {
            return _view.getProtosegment(getProtosegmentKey());
        }

        public Zone toZone() throws DatabaseException {
            Zone zone = (Zone) _view.getDatabase().makeObject(Zone.class);
            zone.setID(getZoneKey());
            zone.revert();
            return zone;
        }

        private int _type;
        private ArrayList _keys = new ArrayList();
        private Map _remarks = new HashMap();
        private InputRow _remarksRow;
        private String _remarksType;
        private Integer _clusterOrder;
        private String _citation;
        private String _correspondenceSet;
        private String _environment;
        private String _protosegment;
        private String _zone;
        private boolean _hasClusterRemarks;
        private boolean _hasCorrespondenceSetRemarks;
        private boolean _hasProtosegmentRemarks;

        private String _message;
        private String _task;
        private Object _value;
        private Object _value2;
        private View _view;
        private boolean _refresh = false;
        public static final int MERGE_CLUSTERS = 2;
        public static final int MERGE_PROTOSEGMENTS = 3;
        public static final int MOVE_CLUSTER = 4;
        public static final int MOVE_CORRESPONDENCE_SET = 5;
        public static final int MOVE_CORRESPONDENCE_SET_TO_CLUSTER = 6;
        public static final int MOVE_PROTOSEGMENT = 7;
        public static final int GET_DATA = 1000;
    }

    /**
     * Add new Protosegment.
     * Creation of matching protosegment is prevented through validation.
     **/
    private void addProtosegment() {
        try {
            Dialogs.showWaitCursor(RefinePane.this);
            Database db = _collection.getDatabase();
            View view = _collection.getViewByID(db.getCurrentSetting().getViewID());
            Zone zone = (Zone) db.makeObject(Zone.class);
            Protosegment proto = view.makeProtosegment(zone);
            AddDialog dialog = new AddDialog("pgtAddProtosegment", proto, null, true);
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {}
            Dialogs.showDefaultCursor(RefinePane.this);
        } catch (DatabaseException e) {
            e.getRootCause().printStackTrace();
            return;
        }
    }

    /**
     * Delete unused Protosegment.
     **/
    private void deleteProtosegment() {
        Messages messages = AppPrefs.getInstance().getMessages();
        Dialogs.showWaitCursor(RefinePane.this);
        TaskDialog dialog =
            new TaskDialog("lblRefineDeleteProtosegment", new RefineData() {
            public void setValue(Object obj) {
                _proto = (Protosegment) obj;
            }
            public boolean run() {
                try {
                    _proto.delete();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                    return true;
                }
                return true;
            }
            private Protosegment _proto;
        }, "RefineDeleteProtosegment");
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {}
        Dialogs.showDefaultCursor(RefinePane.this);
    }

    /**
     * Display Citations.
     **/
    private void displayCitations() {
        Messages messages = AppPrefs.getInstance().getMessages();
        int[] selectedRows = _table.getSelectedRows();
        if (selectedRows.length == 0)
            Dialogs.msgbox(messages.getString("msgRefineSelectRow"));
        for (int i = 0; i < selectedRows.length; i++) {
            RefineData data =
                (RefineData) ((RefineTableModel) _table.getModel()).getRow(selectedRows[i]);
            if (data == null)
                continue;

            // break up citations into lines
            int len = 50;
            String citation = data.getCitation();
            if (citation.length() <= len) {
                Dialogs.customMsgbox(
                    citation,
                    messages.getString("pgtRefineDisplayCitations")
                        + ":    "
                        + data.getCorrespondenceSetHierarchy());
            } else {
                StringBuffer lines = new StringBuffer();
                while (citation.length() > len) {
                    int end = citation.lastIndexOf(",", len);
                    if (end != -1) {
                        lines.append(citation.substring(0, end) + "\n");
                        citation = citation.substring(end + 2);
                    }
                }
                lines.append(citation);
                Dialogs.customMsgbox(
                    lines.toString(),
                    messages.getString("pgtRefineDisplayCitations")
                        + ":    "
                        + data.getCorrespondenceSetHierarchy());
            }
        }
    }

    /**
     * Edit Remarks.
     **/
    private void editRemarks() {
        Messages messages = AppPrefs.getInstance().getMessages();
        int[] selectedRows = _table.getSelectedRows();
        if (selectedRows.length == 0)
            Dialogs.msgbox(messages.getString("msgRefineSelectRow"));
        for (int i = 0; i < selectedRows.length; i++) {
            RefineData data =
                (RefineData) ((RefineTableModel) _table.getModel()).getRow(selectedRows[i]);
            if (data == null)
                continue;

            // display dialogs
            String cmd = "RefineEditRemarks";
            data.setTask(cmd);
            TaskDialog dialog = new TaskDialog("lbl" + cmd, data, cmd);
            data.setRemarksRow(dialog.getRow("value"));
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {
                try {
                    // ensure last input is captured
                    data.useRemarksRow("Protosegment");

                    CorrespondenceSet cset = data.toCorrespondenceSet();
                    cset.setRemarks(data.getRemarks("Correspondence Set"));
                    cset.save();

                    Cluster cluster = cset.getCluster();
                    cluster.setRemarks(data.getRemarks("Cluster"));
                    cluster.save();

                    Protosegment protosegment = data.toProtosegment();
                    protosegment.setRemarks(data.getRemarks("Protosegment"));
                    protosegment.save();

                    // refresh
                    refresh();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    private void outputResults(ActionEvent evt) {
        MainFrame mf = MainFrame.getInstance();
        final User user = mf.getDatabasePane().getCurrentUser();
        final WordCollection collection = mf.getDatabasePane().getCurrentCollection();
        if (user == null || collection == null)
            return;
        
        final JFileChooser fc =
                new JFileChooser(
                AppPrefs.getInstance().getProperty(
                AppPrefs.LAST_DIR,
                System.getProperty("user.home")));
        int ret = fc.showSaveDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            Runnable task = new Runnable() {
                public void run() {
                    InfinitePipeInputStream db_source = new InfinitePipeInputStream();
                    InfinitePipeOutputStream db_out = new InfinitePipeOutputStream(db_source);
                    String filename = fc.getSelectedFile().getAbsolutePath();
                    filename = (filename.indexOf(".") > 0) ? filename : filename + ".html";
                    XMLFile file = new XMLFile();
                    file.setToExport(db_out);
                    List viewList;
                    try {
                        viewList = collection.getViews();
                        collection.setElementViews(viewList);
                        file.setUser(user);
                        file.setCollection(collection);
                        file.setFilename(filename);
                        AppPrefs.getInstance().setProperty(AppPrefs.LAST_DIR, file.getFilename());
                        file.run(); //deadlock
                        try {
                            db_out.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        
                        InfinitePipeInputStream collectedXML_source = new InfinitePipeInputStream();
                        InfinitePipeOutputStream collectedXML_out = new InfinitePipeOutputStream(collectedXML_source);
                        
                        //now we have the basic xml file.
                        //transform with Results2XML.xsl
                        transform("/Results2XML.xsl", db_source, collectedXML_out);
                        
                        //option to export results as xml (for further processing) or html by using TaskDialog?
                        //if to html:
                        //transform with ResultsXML2HTML.xsl
                        OutputStream file_out = new BufferedOutputStream(new FileOutputStream(filename));
                        transform("/ResultsXML2HTML.xsl", collectedXML_source, file_out);
                        
                        // update collection export timestamp status -- I don't think we want to update status.
//                MainFrame.getInstance().updateStatus();
                    } catch (DatabaseException ex) {
                        System.err.println("locus1");
                        ex.printStackTrace();
                    }  catch (FileNotFoundException ex) {
                        System.err.println("locus2");
                        ex.printStackTrace();
                    }
                }
            };
            Messages m = AppPrefs.getInstance().getMessages();
            Dialogs.indeterminateProgressDialog(task, m.getString("pgbWaitString"), m.getString("pgbCurrentTask")+m.getString("btnRefineResults"));
        }
    }
    
    private void transform(String xsltFilename, InputStream input, OutputStream output) {
        try {
            StreamSource xsltSource = new StreamSource(getClass().getResourceAsStream(xsltFilename));
            Transformer t = TransformerFactory.newInstance().newTransformer(xsltSource);
            StreamSource xmlSource = new StreamSource(input);
            StreamResult htmlSink = new StreamResult(output);
            t.transform(xmlSource, htmlSink);
            output.close();
        } catch (TransformerConfigurationException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (TransformerException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        } catch (IOException e) {
            Dialogs.msgbox(AppPrefs.getInstance().getMessages().getString("msgErrSummaryFileCreation"));
            e.printStackTrace();
        }
    }

    
    /**
     * Summarize Evidence.
     **/
    private void summarize() {
        Dialogs.showWaitCursor(RefinePane.this);
        try {
            Database db = _collection.getDatabase();
            View view = _collection.getViewByID(db.getCurrentSetting().getViewID());

            // display dialog
            String cmd = "RefineSummarize";
            SummaryFile data = new SummaryFile();
            data.setView(view);
            TaskDialog dialog = new TaskDialog("lbl" + cmd, data, cmd);
            dialog.setVisible(true);
            if (!dialog.isCancelled()) {
                if (data.getTabulatedGroupCount() < 50) {
                    Messages messages = AppPrefs.getInstance().getMessages();
                    Dialogs.msgbox(
                        messages.getCompoundMessage(
                            "msgRefineSummarizeWarning",
                            data.getTabulatedGroupCount() + ""));
                }
            }
        } catch (DatabaseException e) {
            e.getRootCause().printStackTrace();
            return;
        }
        Dialogs.showDefaultCursor(RefinePane.this);
    }

    /**
     * Change Cluster environment.
     **/
    public static final boolean changeEnvironment(
        RefineData fromData,
        String value) {
        /*
                System.out.println("*** fromData="+ fromData.getSet()+" ZoneKey="+fromData.getZoneKey()
                + " ProtoKey="+fromData.getProtosegmentKey()
                + " clusterKey="+fromData.getClusterKey()
                + " corrSetKey="+fromData.getCorrespondenceSetKey()
                + " citationKey="+fromData.getCitationKey(0));
                System.out.println("*** toData="+ toData.getSet()+" ZoneKey="+toData.getZoneKey()
                + " ProtoKey="+toData.getProtosegmentKey()
                + " clusterKey="+toData.getClusterKey());
        */
        try {
            Cluster fromCluster = fromData.toCluster();
            fromCluster.setEnvironment(value);
            fromCluster.save();
            fromCluster.getProtosegment().reorderClusterOrder();
        } catch (DatabaseException e) {
            e.printStackTrace();
            return true;
        }
        fromData.setRefresh(true);
        return true;
    }

    /**
     * Change Protosegment value.
     * Prompt user if matching protosegment exists.
     **/
    public static final boolean changeProtosegment(
        RefineData fromData,
        String value) {
        try {
            Protosegment fromProtosegment = fromData.toProtosegment();
            fromProtosegment.setProtosegment(value);
            String msg = fromProtosegment.checkValidation();
            if (msg == null) {
                fromProtosegment.save();
                fromData.setRefresh(true);
                return true;
            } else {
                Dialogs.msgbox(msg);
                fromData.setRefresh(false);
                return false;
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Merge 2 Clusters by moving conformable correspondence sets.
     * Uncomformable sets remain with originating cluster.
     * Originating cluster is deleted if all correspondence sets are moved.
     **/
    public static final boolean mergeClusters(
        RefineData fromData,
        RefineData toData) {
        try {
            Cluster fromCluster = fromData.toCluster();
            Cluster toCluster = toData.toCluster();

            // check to cluster correspondence sets
            List toSets = toCluster.getCorrespondenceSets();
            if (toSets.isEmpty()) {
                // delete bad cluster
                toCluster.delete();
                Dialogs.msgbox(
                    AppPrefs.getInstance().getMessages().getString("msgRefineEmptyToCluster"));
                fromData.setRefresh(false);
                return true;
            }

            // move all correspondence sets from cluster
            boolean moveSuccess = false;
            boolean moveFailure = false;
            List fromSets = fromCluster.getCorrespondenceSets();
            for (Iterator it = fromSets.iterator(); it.hasNext();) {
                CorrespondenceSet fromSet = (CorrespondenceSet) it.next();
                // move correspondence set if identical or comformable
                if (toCluster.addCorrespondenceSet(fromSet)) {
                    // success
                    moveSuccess = true;
                } else {
                    // failure
                    moveFailure = true;
                }
            }
            // moved all
            if (!moveFailure) {
                // delete from cluster after appending remarks
                toCluster.appendRemarks(fromCluster.getRemarks());
                toCluster.save();
                Protosegment fromProtosegment = fromCluster.getProtosegment();
                fromCluster.delete();
                fromProtosegment.reorderClusterOrder();
            }
            // nothing moved
            if (!moveSuccess)
                Dialogs.msgbox(
                    AppPrefs.getInstance().getMessages().getString(
                        "msgRefineMergeClustersNoChange"));

            // refresh if moved at least one
            fromData.setRefresh(moveSuccess);
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(false);
            return true;
        }
        return true;
    }

    /**
     * Merge 2 Protosegments by moving all clusters to target protosegment.
     * Clusters are not merged because multiple clusters with same environments possible.
     **/
    public static final boolean mergeProtosegments(
        RefineData fromData,
        RefineData toData) {
        try {
            Protosegment fromProtosegment = fromData.toProtosegment();
            Protosegment toProtosegment = toData.toProtosegment();

            // move all clusters from protosegment
            // refresh if moved at least one
            fromData.setRefresh(
                toProtosegment.moveClustersFromProtosegment(fromProtosegment));
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(false);
            return true;
        }
        return true;
    }

    /**
     * Move Cluster to another protosegment.
     * Clusters are not merged because multiple clusters with same environments possible.
     **/
    public static final boolean moveCluster(
        RefineData fromData,
        RefineData toData) {
        try {
            Cluster fromCluster = fromData.toCluster();
            Protosegment fromProtosegment = fromData.toProtosegment();
            Protosegment toProtosegment = toData.toProtosegment();

            // move cluster
            fromCluster.setProtosegment(toProtosegment);
            fromCluster.setOrder(new Integer(toProtosegment.getMaxClusterOrder() + 1));
            fromCluster.save();
            fromProtosegment.reorderClusterOrder();
            toProtosegment.reorderClusterOrder();
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(false);
            return true;
        }
        fromData.setRefresh(true);
        return true;
    }

    /**
     * Move Correspondence Set to another cluster by specifying protosegment and environment.
     * Conformable and identical set moved.
     * Multiple clusters with conformable sets causes prompt to select specific one.
     * Protosegments with unmatched clusters (environment) creates new cluster.
     **/
    public static final boolean moveCorrespondenceSet(
        RefineData fromData,
        RefineData toData) {
        try {
            CorrespondenceSet fromSet = fromData.toCorrespondenceSet();
            Cluster fromCluster = fromSet.getCluster();
            final Protosegment toProtosegment = toData.toProtosegment();
            final String environment = ((String) fromData.getValue2());

            // get list of clusters with conformable sets
            WordCollection collection =
                MainFrame.getInstance().getDatabasePane().getCurrentCollection();
            Setting setting = collection.getDatabase().getCurrentSetting();
            final View view = collection.getViewByID(setting.getViewID());
            List clusters =
                collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_REFINE_MOVE_CORRESPONDENCE_SET_TO_CLUSTER_ROW";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException {
                    stmt.setLong(1, toProtosegment.getID());
                    stmt.setString(2, environment);
                }

                public Object createObject(Database db, ResultSet rs) throws SQLException {
                    RefineData data =
                        new RefineData(RefineData.MOVE_CORRESPONDENCE_SET_TO_CLUSTER, view);
                    data.setProtosegment(rs.getString(1));
                    data.setEnvironment(rs.getString(2));
                    data.setClusterOrder(new Integer(rs.getInt(3)));
                    data.addKeys(rs.getString(4));
                    return data;
                }
            });
            // remove non-conformable clusters and identical cluster
            boolean identicalCluster = false;
            for (int i = 0; i < clusters.size(); i++) {
                RefineData clusterData = (RefineData) clusters.get(i);
                Cluster cluster = clusterData.toCluster();
                
                if (cluster.getID() == fromCluster.getID())
                	identicalCluster = true;

                if (identicalCluster
                    || !cluster.hasConformable(fromSet)) {
                    clusters.remove(i);
                    i--;
                }
            }

            boolean moveToNewCluster = false;
            if (clusters.size() == 0) {
            	// check for removal of identical cluster
            	if (identicalCluster)
            		return true;
            		
                // create and move to new cluster
                moveToNewCluster = true;
            } else if (clusters.size() == 1) {
                // move to cluster
                RefineData clusterData = (RefineData) clusters.get(0);
                Cluster toCluster = clusterData.toCluster();

                // move correspondence set
                if (!toCluster.addCorrespondenceSet(fromSet))
                    // uncomformable create and move to new cluster
                    moveToNewCluster = true;
            } else {
                // query user for cluster to move to
                // cluster selection list is stored in value
                fromData.setValue(clusters);
                String cmd = "RefineMoveCorrespondenceSetToCluster";
                fromData.setTask(cmd);
                TaskDialog dialog = new TaskDialog("lbl" + cmd, fromData, cmd);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    // move to cluster
                    RefineData clusterData = (RefineData) fromData.getValue();
                    Cluster toCluster = clusterData.toCluster();

                    // move correspondence set
                    if (!toCluster.addCorrespondenceSet(fromSet))
                        // uncomformable create and move to new cluster
                        moveToNewCluster = true;
                } else {
                    // cancelled
                    fromData.setRefresh(false);
                    return true;
                }
            }

            // move to new cluster
            if (moveToNewCluster)
                moveCorrespondenceSetToNewCluster(
                    fromSet,
                    fromCluster,
                    toProtosegment,
                    environment);
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(false);
            return true;
        }
        fromData.setRefresh(true);
        return true;
    }

    /**
     * Move Correspondence Set to new Cluster.
     **/
    private static final void moveCorrespondenceSetToNewCluster(
        CorrespondenceSet fromSet,
        Cluster fromCluster,
        Protosegment toProtosegment,
        String environment)
        throws DatabaseException {
        // create new cluster
        Cluster newCluster = toProtosegment.makeCluster();
        newCluster.setEnvironment(environment);
        newCluster.setOrder(new Integer(toProtosegment.getMaxClusterOrder() + 1));
        newCluster.save();
        // move protosegment
        fromSet.setCluster(newCluster);
        fromSet.setOrder(new Integer(1));
        fromSet.save();
        if (fromCluster.getCorrespondenceSets().isEmpty()) {
            // delete empty cluster after adding remarks to new cluster
            newCluster.setRemarks(fromCluster.getRemarks());
            newCluster.save();
            Protosegment fromProtosegment = fromCluster.getProtosegment();
            fromCluster.delete();
            fromProtosegment.reorderClusterOrder();
        }
        toProtosegment.reorderClusterOrder();
    }

    /**
     * Move Protosegment to another zone.
     * Only zones without matching protosegments are selectable.
     **/
    public static final boolean moveProtosegment(
        RefineData fromData,
        RefineData toData) {
        try {
            Protosegment fromProtosegment = fromData.toProtosegment();
            Zone toZone = toData.toZone();
            fromProtosegment.setZone(toZone);
            fromProtosegment.save();
        } catch (DatabaseException e) {
            e.printStackTrace();
            return true;
        }
        fromData.setRefresh(true);
        return true;
    }

    /**
     * Reorder clusters of Protosegment by changing ClusterOrder.
     **/
    public static final boolean reorderClusters(
        RefineData fromData,
        List clusters) {
        try {
            int order = 0;
            for (Iterator it = clusters.iterator(); it.hasNext();) {
                Cluster cluster = (Cluster) it.next();
                cluster.setOrder(new Integer(++order));
                cluster.save();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(true);
            return true;
        }
        fromData.setRefresh(true);
        return true;
    }

    /**
     * Retabulate Group.
     **/
    public static final boolean retabulateGroup(
        RefineData fromData,
        String citation) {
        // get group key
        long key = -1;
        for (int i = 0; i < fromData.getCitationCount(); i++) {
            if (fromData.getCitation(i).equals(citation)) {
                key = fromData.getGroupKey(i);
                break;
            }
        }

        // get group and retabulate
        String tag = "";
        try {
            WordCollection collection =
                MainFrame.getInstance().getDatabasePane().getCurrentCollection();
            Setting setting = collection.getDatabase().getCurrentSetting();
            View view = collection.getViewByID(setting.getViewID());
            Group group = new Group(collection.getDatabase(), key, view, null);
            group.revert();
            tag = group.getName();
            group.retabulate();

            // refresh list
            fromData.setRefresh(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
            Dialogs.error(AppPrefs.getInstance().getMessages().getString("msgErrDatabase"));
            fromData.setRefresh(false);
            return true;
        }

        // display confirmation message
        String[] messageData = { citation.substring(0, citation.indexOf(tag)), tag };
        Dialogs.msgbox(
            AppPrefs.getInstance().getMessages().getCompoundMessage(
                "msgRefineRetabulateGroup",
                messageData));
        return true;
    }

    /**
     * Base Input Row for refine data.
     **/
    protected static class RefineInputRow extends InputRow {
        /**
         * Constructor.
         **/
        public RefineInputRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj);
            _data = (RefineData) obj;
            _refresh = refresh;
            init(_combo, _combo);
            refresh();
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {}

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
                } catch (DatabaseException ignored) {}
            }
        });
        protected final Refreshable _refresh;
        protected final RefineData _data;
    }

    /**
     * Input Row for delete protosegment data.
     **/
    public static final class RefineDeleteProtosegmentRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineDeleteProtosegmentRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                View view = collection.getViewByID(setting.getViewID());
                List protosegments = view.getUnusedProtosegments();
                // display zone with protosegment in selection list
                for (Iterator it = protosegments.iterator(); it.hasNext();) {
                    Protosegment protosegment = (Protosegment) it.next();
                    protosegment.setDisplayZone(true);
                    protosegment.setHTMLtoString(true);
                }
                ((BasicListModel) _combo.getModel()).setData(protosegments);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for edit remarks data.
     **/
    public static final class RefineEditRemarksRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineEditRemarksRow(
            BeanCatalog.Property prop,
            final Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);

            _combo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String type = (String) _combo.getSelectedItem();
                    if (type != null) {
                        ((RefineData) obj).useRemarksRow(type);
                    }
                }
            });
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                List types = new ArrayList();
                String type = "Correspondence Set";
                types.add(type);
                CorrespondenceSet cset = _data.toCorrespondenceSet();
                _data.setRemarks(type, cset.getRemarks());

                type = "Cluster";
                types.add(type);
                Cluster cluster = cset.getCluster();
                _data.setRemarks(type, cluster.getRemarks());

                type = "Protosegment";
                types.add(type);
                Protosegment protosegment = _data.toProtosegment();
                _data.setRemarks(type, protosegment.getRemarks());

                ((BasicListModel) _combo.getModel()).setData(types);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for merge clusters data.
     **/
    public static final class RefineMergeClustersRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMergeClustersRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                final View view = collection.getViewByID(setting.getViewID());
                List list =
                    collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "GET_REFINE_MERGE_CLUSTERS_ROW";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setLong(1, view.getID());
                        stmt.setLong(2, _data.getProtosegmentKey());
                        stmt.setString(3, _data.getEnvironment());
                        stmt.setLong(4, _data.getClusterKey());
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        RefineData data = new RefineData(RefineData.MERGE_CLUSTERS, view);
                        data.setProtosegment(rs.getString(1));
                        data.setEnvironment(rs.getString(2));
                        data.setClusterOrder(new Integer(rs.getInt(3)));
                        data.addKeys(rs.getString(4));
                        return data;
                    }
                });
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for merge protosegments data.
     **/
    public static final class RefineMergeProtosegmentsRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMergeProtosegmentsRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                final View view = collection.getViewByID(setting.getViewID());
                List list =
                    collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "GET_REFINE_MERGE_PROTOSEGMENTS_ROW";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setLong(1, view.getID());
                        stmt.setLong(2, _data.getProtosegmentKey());
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        RefineData data = new RefineData(RefineData.MERGE_PROTOSEGMENTS, view);
                        data.setZone(rs.getString(1));
                        data.setProtosegment(rs.getString(2));
                        data.addKeys(rs.getString(3));
                        return data;
                    }
                });
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for move cluster to another protosegment data.
     **/
    public static final class RefineMoveClusterRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMoveClusterRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                final View view = collection.getViewByID(setting.getViewID());
                List list =
                    collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "GET_REFINE_MOVE_CLUSTER_ROW";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setLong(1, view.getID());
                        stmt.setLong(2, _data.getProtosegmentKey());
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        RefineData data = new RefineData(RefineData.MOVE_CLUSTER, view);
                        data.setZone(rs.getString(1));
                        data.setProtosegment(rs.getString(2));
                        data.addKeys(rs.getString(3));
                        return data;
                    }
                });
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for move correspondence set to another protosegment data.
     * Default environment field is also initialized with current value.
     **/
    public static final class RefineMoveCorrespondenceSetRow
        extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMoveCorrespondenceSetRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                final View view = collection.getViewByID(setting.getViewID());
                List list =
                    collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "GET_REFINE_MOVE_CORRESPONDENCE_SET_ROW";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setLong(1, view.getID());
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        RefineData data = new RefineData(RefineData.MOVE_CORRESPONDENCE_SET, view);
                        data.setZone(rs.getString(1));
                        data.setProtosegment(rs.getString(2));
                        data.addKeys(rs.getString(3));
                        return data;
                    }
                });
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }

            // set environment (set value2 workaround)
            _data.setValue2(_data.getEnvironment());
        }
    }

    /**
     * Input Row for move correspondence set to another cluster data.
     **/
    public static final class RefineMoveCorrespondenceSetToClusterRow
        extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMoveCorrespondenceSetToClusterRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            ((BasicListModel) _combo.getModel()).setData((List) _data.getValue());
        }
    }

    public static final class RefineMoveProtosegmentRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineMoveProtosegmentRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                WordCollection collection =
                    MainFrame.getInstance().getDatabasePane().getCurrentCollection();
                Setting setting = collection.getDatabase().getCurrentSetting();
                final View view = collection.getViewByID(setting.getViewID());
                List list =
                    collection.getDatabase().retrieveObjects(new RetrieveAllParameters() {
                    public String getRetrieveAllSQLKey() {
                        return "GET_REFINE_MOVE_PROTOSEGMENT_ROW";
                    }

                    public void setRetrieveAllParameters(PreparedStatement stmt)
                        throws SQLException {
                        stmt.setLong(1, view.getID());
                        stmt.setString(2, _data.getProtosegment());
                    }

                    public Object createObject(Database db, ResultSet rs) throws SQLException {
                        RefineData data = new RefineData(RefineData.MOVE_PROTOSEGMENT, view);
                        data.setZone(rs.getString(3));
                        data.addKeys(rs.getString(4));
                        return data;
                    }
                });
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for move protosegment to another zone data.
     * Zones with no matching protosegments displayed in list.
     **/
    protected static class RefineReorderClustersRow extends InputRow {
        /**
         * Constructor.
         **/
        public RefineReorderClustersRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj);
            _data = (RefineData) obj;
            _refresh = refresh;

            // create sorter
            init(_sorter, _sorter);
            refresh();

            // set RefineData object with each change
            _sorter.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    _data.setValue(_sorter.getList());
                    try {
                        _refresh.refresh();
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    }
                }
            });
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                List clusters = _data.toProtosegment().getClusters();
                _sorter.reset(clusters);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }

        /**
         * Set the value of this row.
         **/
        public void setValue(Object value) {
            refresh();
        }

        /**
         * Get the value of this row.
         **/
        public Object getValue() {
            return _sorter.getList();
        }

        protected final MultiSorter _sorter = new MultiSorter();
        protected final Refreshable _refresh;
        protected final RefineData _data;
    }

    /**
     * Input Row for retabulate group.
     * Citations displayed in list.
     **/
    public static final class RefineRetabulateGroupRow extends RefineInputRow {
        /**
         * Constructor.
         **/
        public RefineRetabulateGroupRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            // set table
            try {
                ArrayList list = new ArrayList();
                for (int i = 0; i < _data.getCitationCount(); i++) {
                    list.add(_data.getCitation(i));
                }

                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Base Summarize Input Row.
     **/
    protected static class RefineSummarizeInputRow extends InputRow {
        /**
         * Constructor.
         **/
        public RefineSummarizeInputRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj);
            _data = (SummaryFile) obj;
            _refresh = refresh;
            init(_combo, _combo);
            refresh();
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {}

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

        /**
         * Set selection data.
         **/
        public void setSelectionData(Object v) {}

        protected final JComboBox _combo = new JComboBox(new BasicListModel() {
            public void setSelectedItem(Object o) {
                super.setSelectedItem(o);
                setSelectionData(o);
                try {
                    _refresh.refresh();
                } catch (DatabaseException ignored) {}
            }
        });
        protected final Refreshable _refresh;
        protected final SummaryFile _data;
    }

    /**
     * Input Row for summarize display frantz for clusters/protosegments data.
     **/
    public static final class RefineSummarizeDisplayFrantzRow
        extends RefineSummarizeInputRow {
        /**
         * Constructor.
         **/
        public RefineSummarizeDisplayFrantzRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Set selection data.
         **/ //davisnw -- modified to use AppPrefs for localization.
        public void setSelectionData(Object v) {
            Messages messages = AppPrefs.getInstance().getMessages();
            String frantzChoice = (String) v;
            if (v.equals(messages.getString("cmbRefineSummarizeFrantzClusters"))) {
                _data.setDisplayFrantz("Clusters");
            } else if (v.equals(messages.getString("cmbRefineSummarizeFrantzProtosegments"))) {
                _data.setDisplayFrantz("Protosegments");
            } else {
                _data.setDisplayFrantz("Both");
            }           
        }

        /**
         * Refresh this object.
         **/ //davisnw -- modified to use AppPrefs for localization.
        public void refresh() {
            Messages messages = AppPrefs.getInstance().getMessages();
            // set table
            try {
                List list = new ArrayList();
                list.add(messages.getString("cmbRefineSummarizeFrantzClusters"));
                list.add(messages.getString("cmbRefineSummarizeFrantzProtosegments"));
                list.add(messages.getString("cmbRefineSummarizeFrantzBoth"));
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    /**
     * Input Row for summarize gloss data.
     **/
    public static final class RefineSummarizeGlossRow
        extends RefineSummarizeInputRow {
        /**
         * Constructor.
         **/
        public RefineSummarizeGlossRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Set selection data.
         **/ //davisnw -- modified to use AppPrefs for localization.
        public void setSelectionData(Object v) {
            String gloss = (String) v;
            if (v.equals(AppPrefs.getInstance().getMessages().getString("cmbRefineSummarizeGlossPrimary"))) {
                _data.setGloss("Primary");
            }
            else {
                _data.setGloss("Secondary");
            }
        }

        /**
         * Refresh this object.
         **/ //davisnw -- modified to use AppPrefs for localization.
        public void refresh() {
            Messages messages=AppPrefs.getInstance().getMessages();
            // set table
            try {
                List list = new ArrayList();
                list.add(messages.getString("cmbRefineSummarizeGlossPrimary"));
                boolean noSecondaryGloss =
                    _data.getView().getCollection().getGloss2().equals("");
                if (!noSecondaryGloss)
                    list.add(messages.getString("cmbRefineSummarizeGlossSecondary"));
                ((BasicListModel) _combo.getModel()).setData(list);
                if (noSecondaryGloss)
                    _combo.setSelectedIndex(0);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    
     /**
     * Input Row for output format
     **/ //davisnw -added.
    public static final class RefineSummarizeFormatRow
        extends RefineSummarizeInputRow {
        
        
        /**
         * Constructor.
         **/
        public RefineSummarizeFormatRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Set selection data.
         **/
        public void setSelectionData(Object v) {
            Messages messages = AppPrefs.getInstance().getMessages();
            if (v.equals(messages.getString("cmbRefineSummarizeMODE_XML"))) {
                _data.setOutputXSL(null);
            } else  {
                StringTokenizer t = new StringTokenizer(AppProperties.getProperty("RefineSummarizeXSLs"),",");
                while (t.hasMoreElements()) {
                    String xsl=t.nextToken();
                    if (v.equals(messages.getString("cmb" + xsl))) {
                        _data.setOutputXSL(AppProperties.getProperty(xsl,null));
                        _data.setOutputExtension(AppProperties.getProperty(xsl+"_extension",""));
                        return;
                    }
                }
                Dialogs.error(messages.getString("msgErrSummaryFileFormat")+v.toString());
            }
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            Messages messages = AppPrefs.getInstance().getMessages();
            // set table
            try {
                List list = new ArrayList();
                list.add(messages.getString("cmbRefineSummarizeMODE_XML"));
                StringTokenizer t = new StringTokenizer(AppProperties.getProperty("RefineSummarizeXSLs"),",");
                while (t.hasMoreElements()) {
                    list.add(messages.getString("cmb" + t.nextToken()));
                }
                ((BasicListModel) _combo.getModel()).setData(list);
                _combo.setSelectedIndex(0);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }
    
    
    /**
     * Input Row for summarize include residue data.
     **/
    public static final class RefineSummarizeIncludeResidueRow
        extends RefineSummarizeInputRow {
        /**
         * Constructor.
         **/
        public RefineSummarizeIncludeResidueRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj, refresh);
        }

        /**
         * Set selection data.
         **/
        public void setSelectionData(Object v) {
            _data.setIncludeResidue(((String) v).equals(AppPrefs.getInstance().getMessages().getString("cmbYes")));
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            Messages messages = AppPrefs.getInstance().getMessages();
            // set table
            try {
                List list = new ArrayList();
                list.add(messages.getString("cmbYes"));
                list.add(messages.getString("cmbNo"));
                ((BasicListModel) _combo.getModel()).setData(list);
            } catch (Exception e) {
                Dialogs.genericError(e);
            }
        }
    }

    private AlignedTable _table;
    private RefineTableModel _tableModel;
    private final ButtonListener _listener = new ButtonListener();
    private final WordCollection _collection;
}