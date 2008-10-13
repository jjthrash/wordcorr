package org.wordcorr.gui;

import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.*;

/**
 * JTable subclass to fix an annoying bug with combobox editing
 * components. Basically, after an edit is started, the combobox
 * should get the focus so that you can change the contents via the
 * keyboard.
 *
 * @author Keith Hamasaki
 **/
public class WTable extends JTable {

    /**
     * Constructor.
     **/
    public WTable(TableModel model) {
	super(model);
    }

    /**
     * Override to give focus to the editing component.
     **/
    public boolean editCellAt(int row, int column, EventObject e) {
        if (cellEditor != null && !cellEditor.stopCellEditing()) {
            return false;
        }

        if (row < 0 || row >= getRowCount() ||
            column < 0 || column >= getColumnCount()) {
            return false;
        }

        if (!isCellEditable(row, column))
            return false;

        TableCellEditor editor = getCellEditor(row, column);
        if (editor != null && editor.isCellEditable(e)) {
            editorComp = prepareEditor(editor, row, column);
            if (editorComp == null) {
                removeEditor();
                return false;
            }
            editorComp.setBounds(getCellRect(row, column, false));
            add(editorComp);
            editorComp.validate();

            setCellEditor(editor);
            setEditingRow(row);
            setEditingColumn(column);
            editor.addCellEditorListener(this);
            editorComp.requestFocus();
            return true;
        }
        return false;
    }
}
