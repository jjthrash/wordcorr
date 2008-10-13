package org.wordcorr.gui.input;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.wordcorr.AppProperties;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Datum;
import org.wordcorr.db.Entry;
import org.wordcorr.db.Variety;
import org.wordcorr.gui.AddDialog;
import org.wordcorr.gui.AlignedTableCellRenderer;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.FontCache;
import org.wordcorr.gui.Messages;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.WButton;
import org.wordcorr.gui.WTable;
import org.wordcorr.gui.action.WordCorrAction;


/**
 * Input row for entering data into an entry.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class EntryDataRow extends InputRow implements Refreshable {

    private static final String[] COL_KEYS =
        { "lblAbbreviation", "lblShortName", "lblDatum", "lblBlank" };

    /**
     * Constructor.
     **/
    public EntryDataRow(
        BeanCatalog.Property prop,
        Object persistent,
        Refreshable refresh) {
        super(prop, persistent);
        _entry = (Entry) persistent;
        _refresh = refresh;
        init(_comp, _comp);
        refresh();

        final DataTableModel model = (DataTableModel) _comp._table.getModel();
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent evt) {
                _entry.setData(model.getRows());
                try {
                    _refresh.refresh();
                } catch (DatabaseException e) {
                    Dialogs.genericError(e);
                }
            }
        });

        _comp._table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2
                    && ((evt.getModifiers() & evt.BUTTON1_MASK) > 0)) {
                    doEdit();
                }
            }
        });

        _comp._table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    doEdit();
                    evt.consume();
                }
            }
        });
    }

    /**
     * Refresh this object.
     **/
    public void refresh() {}

    /**
     * Get the weight of this row in the table. Default implementation
     * returns 0.
     **/
    public double getRowWeight() {
        return 20.0;
    }

    /**
     * Set the value of this row.
     **/
    public void setValue(Object value) {
        refresh();
        ((DataTableModel) _comp._table.getModel()).setRows((List) value);
    }

    /**
     * Get the value of this row.
     **/
    public Object getValue() {
        return null;
    }

    /**
     * Perform an edit action.
     **/
    private void doEdit() {
        Datum datum =
            ((DataTableModel) _comp._table.getModel()).getRow(
                _comp._table.getSelectedRow());
        if (datum == null)
            return;
        AddDialog dialog = new AddDialog("btnEditDatum", datum, null, false);
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
            ((DataTableModel) _comp._table.getModel()).refresh();

            // save changes
            try {
                _entry.save();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                // revert if windows closed without cancelling
                datum.revert();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Component for managing data.
     **/
    private final class DataComponent extends JPanel {

        DataComponent() {
            setLayout(new BorderLayout());

            DataTableModel model = new DataTableModel();
            _table = new WTable(model);
            _table.setRowHeight(
                new Double(
                    Math.ceil(
                        new JTextField().getFont().getSize()
                            * Double.parseDouble(AppProperties.getProperty("RowHeightFactor"))
                            + _table.getRowMargin() * 2))
                    .intValue());
            model.setTable(_table);
            initTable();

            JScrollPane scroll = new JScrollPane(_table);
            add(new ButtonPanel(), BorderLayout.NORTH);

            java.awt.Container main = new Box(BoxLayout.X_AXIS);
            main.add(scroll);
            main.add(Box.createHorizontalStrut(10));
            add(main, BorderLayout.CENTER);
        }

        void initTable() {
            for (int i = 0; i < _table.getModel().getColumnCount(); i++) {
                int width = 50;
                switch (i) {
                    case 0 :
                        width = 10;
                        break;
                    case 1 :
                        width = 50;
                        break;
                    case 2 :
                        width = 300;
                        break;
                    case 3 :
                        width = 10;
                        break;
                }
                _table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            JTextField editor = new JTextField(); editor.setFont(FontCache.getIPA()); //davisnw - set font for editing datums on DataPane
            editor.addKeyListener(new IPAKeyListener(editor));
            _table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(editor)); //davisnw
            _table.getColumnModel().getColumn(2).setCellRenderer(new AlignedTableCellRenderer()); //davisnw
        }

        private final JTable _table;
    }

    /**
     * Button panel.
     **/
    private final class ButtonPanel extends JPanel {
        ButtonPanel() {
            super(new FlowLayout(FlowLayout.LEFT));
            add(new WButton(new WordCorrAction("btnAddDatum", "accAddDatum") {
                public void actionPerformed(ActionEvent evt) {
                    Datum datum = _entry.makeDatum();
                    AddDialog dialog = new AddDialog("btnAddDatum", datum, null, false);
                    dialog.setVisible(true);
                    if (!dialog.isCancelled()) {
                        ((DataTableModel) _comp._table.getModel()).addRow(datum);
                    }
                }
            }));

            add(new WButton(new WordCorrAction("btnEditDatum", "accEditDatum") {
                public void actionPerformed(ActionEvent evt) {
                    doEdit();
                }
            }));

            add(new WButton(new WordCorrAction("btnDeleteDatum", "accDeleteDatum") {
                public void actionPerformed(ActionEvent evt) {
                    if (Dialogs
                        .confirm(AppPrefs.getInstance().getMessages().getString("msgConfirmDelete")))
                        ((DataTableModel) _comp._table.getModel()).removeRows(
                            _comp._table.getSelectedRows());
                }
            }));
        }
    }

    /**
     * Table model class for data elements.
     **/
    private final class DataTableModel
        extends AbstractTableModel
        implements Refreshable {

        public Object getValueAt(int row, int col) {
            Datum datum = (Datum) _rows.get(row);
            if (col < 2) {
                Variety variety = datum.getVariety();
                if (variety == null) {
                    return null;
                }

                switch (col) {
                    case 0 :
                        return variety.getAbbreviation();
                    case 1 :
                        return variety.getShortName();
                }
            }
            if (col == 2)
                return datum.getName();

            // determine Special Semantics and Remarks flags
            String flags = "";
            if (!datum.getSpecialSemantics().equals(""))
                flags += "S";
            if (!datum.getRemarks().equals(""))
                flags += "R";
            return flags;
        }

        /*
         * Set values for editable table cells.
         */
        public void setValueAt(Object value, int row, int col) {
            Datum datum = (Datum) _rows.get(row);
            if (col == 2) {
                String val = (String) value;
                // limit 70
                if (val.length() > 70) {
                    Dialogs.msgbox(
                        AppPrefs.getInstance().getMessages().getCompoundMessage("cmpMaxLength", "70"));
                    val = val.substring(0, 70);
                }
                datum.setName(val);
                // save changes
                try {
                    datum.save();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }

        /*
         * Indicate editable table cells.
         */
        public boolean isCellEditable(int row, int col) {
            return col == 2;
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int i) {
            return _messages.getString(COL_KEYS[i]);
        }

        public int getRowCount() {
            return _rows.size();
        }

        void setTable(JTable table) {
            _sorter = new SortableList(table, _rows, this);
        }

        void addRow(Datum datum) {
            _rows.add(datum);
            refresh();
        }

        void removeRows(int[] indices) {
            if (indices.length == 0) {
                return;
            }

            Arrays.sort(indices);

            for (int i = indices.length - 1; i >= 0; i--) {
                int index = indices[i];
                if (index < 0 || index >= _rows.size()) {
                    continue;
                }
                Datum datum = (Datum) _rows.remove(index);
                datum.markDeleted();
                _deleted.add(datum);
            }
            refresh();
        }

        Datum getRow(int index) {
            return (index < 0 || index >= _rows.size()) ? null : (Datum) _rows.get(index);
        }

        List getRows() {
            List ret = new ArrayList(_rows);
            ret.addAll(_deleted);
            return ret;
        }

        void setRows(List rows) {
            _rows.clear();
            _rows.addAll(rows);
            refresh();
        }

        SortableList getSortableList() {
            return _sorter;
        }

        public void refresh() {
            fireTableStructureChanged();
            _comp.initTable();
        }

        private final List _rows = new ArrayList();
        private final List _deleted = new ArrayList();
        private SortableList _sorter;
    }

    private final Messages _messages = AppPrefs.getInstance().getMessages();
    private final DataComponent _comp = new DataComponent();
    private final Entry _entry;
    private final Refreshable _refresh;
}