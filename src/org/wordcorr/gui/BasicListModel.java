package org.wordcorr.gui;

import java.util.*;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * Generic list mode that contains a list of persistent objects.
 * @author Keith Hamasaki
 **/
public class BasicListModel extends AbstractListModel implements Refreshable, ComboBoxModel {

    /**
     * Constructor.
     **/
    public BasicListModel() {
        this(new ArrayList());
    }

    /**
     * Constructor.
     **/
    public BasicListModel(List list) {
        _messages = AppPrefs.getInstance().getMessages();
        _list = list;
    }

    /**
     * Get the element at the given index.
     **/
    public Object getElementAt(int index) {
        return _list.get(index);
    }

    /**
     * Get the size of the list.
     **/
    public int getSize() {
        return _list.size();
    }

    /**
     * Set the data.
     **/
    public void setData(List list) {
        _list.clear();
        _list.addAll(list);
        refresh();
    }

    /**
     * Notify the JList that we've changed.
     **/
    public void refresh() {
        fireContentsChanged(this, 0, getSize());
    }

    /**
     * Set the selected item. Used only for combo boxes
     **/
    public void setSelectedItem(Object o) {
        _selected = o;
    }

    /**
     * Get the selected item. Used only for combo boxes.
     **/
    public Object getSelectedItem() {
        return _selected;
    }

    private Object _selected;
    private final Messages _messages;
    private final List _list;
}
