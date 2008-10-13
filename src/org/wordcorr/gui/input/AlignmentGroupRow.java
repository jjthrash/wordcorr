package org.wordcorr.gui.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Alignment;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Group;
import org.wordcorr.gui.Refreshable;

/**
 * Row for selecting a group for an alignment.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class AlignmentGroupRow extends InputRow {

    /**
     * Constructor.
     **/
    public AlignmentGroupRow(
        BeanCatalog.Property prop,
        Object obj,
        Refreshable refresh) {
        super(prop, obj);
        _alignment = (Alignment) obj;
        _refresh = refresh;
        List groups;
        try {
            groups = _alignment.getView().getGroups(_alignment.getDatum().getEntry());
        } catch (DatabaseException e) {
            groups = new java.util.ArrayList();
        }
        groups.add(0, "");
        final List finalGroups = groups;

        _combo = new JComboBox();
        _combo.setEditable(true);
        java.awt.Component editor = _combo.getEditor().getEditorComponent();
        if (editor instanceof JTextComponent) {
            ((JTextComponent) editor).setDocument(
                new LimitDocument(prop.getMaxLength(), prop.getType()));
        }
        _combo.setModel(new DefaultComboBoxModel(new Vector(groups)));
        _combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Object item = ((JComboBox) evt.getSource()).getSelectedItem();

                // if it's a group, then just set it and return
                if (item instanceof Group) {
                    _alignment.setGroup((Group) item);
                    return;
                }

                // look for the tag in the list, if it's not
                // there, then make a new one
                String tag = item.toString().trim();
                if (tag.equals("")) {
                    // clear the selection
                    _alignment.setGroup(null);
                } else {
                    boolean found = false;
                    for (Iterator it = finalGroups.listIterator(1); it.hasNext();) {
                        Group group = (Group) it.next();
                        if (group.getName().equals(tag)) {
                            _alignment.setGroup(group);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Group group = _alignment.getView().makeGroup(_alignment.getDatum().getEntry());
                        group.setName(tag);
                        _alignment.setGroup(group);
                    }
                }
            }
        });
        init(_combo, _combo);
    }

    /**
     * Set the value of this row.
     **/
    public void setValue(Object value) {
        _combo.setSelectedItem(value);
        _alignment.setGroup((Group) value);
    }

    /**
     * Get the value of this row.
     **/
    public Object getValue() {
        return _combo.getSelectedItem();
    }

    private final JComboBox _combo;
    private final Refreshable _refresh;
    private final Alignment _alignment;
}