package org.wordcorr.gui.input;

import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.gui.Refreshable;

/**
 * A wrapper around a JList or JTable that allows for moving items around.
 * @author Keith Hamasaki
 **/
public class SortableList {

    /**
     * Constructor.
     **/
    public SortableList(final JList jlist, List data, Refreshable refresh) {
        _data = data;
        _refresh = refresh;
        _select = new Selectable() {
                public int[] getSelectedIndices() {
                    return jlist.getSelectedIndices();
                }
                public void refresh() {
                    try {
                        _refresh.refresh();
                    } catch(DatabaseException ignored) { }
                }
                public void clearSelection() {
                    jlist.clearSelection();
                }
                public void addSelectionInterval(int i1, int i2) {
                    jlist.addSelectionInterval(i1, i2);
                }
            };
    }

    /**
     * Constructor.
     **/
    public SortableList(final JTable jtable, List data, Refreshable refresh) {
        _data = data;
        _refresh = refresh;
        _select = new Selectable() {
                public int[] getSelectedIndices() {
                    return jtable.getSelectedRows();
                }
                public void refresh() {
                    try {
                        _refresh.refresh();
                    } catch(DatabaseException ignored) { }
                }
                public void clearSelection() {
                    jtable.clearSelection();
                }
                public void addSelectionInterval(int i1, int i2) {
                    jtable.addRowSelectionInterval(i1, i2);
                }
            };
    }

    /**
     * Move the selected items in this list up.
     **/
    public void moveUp() {
        int[] indices = _select.getSelectedIndices();
        Arrays.sort(indices);

        // skip over all the stuff at the top
        int test = 0;
        int index = 0;
        while (index < indices.length && indices[index] == test) {
            index++;
            test++;
        }

        for (int i = index; i < indices.length; i++) {
            swap(indices[i], indices[i] - 1);
        }
        _select.refresh();
        _select.clearSelection();

        // all the stuff at the top should still be selected
        test = 0;
        index = 0;
        while (index < indices.length && indices[index] == test) {
            _select.addSelectionInterval(test, test);
            index++;
            test++;
        }

        for (int i = index; i < indices.length; i++) {
            _select.addSelectionInterval(indices[i] - 1, indices[i] - 1);
        }
    }

    /**
     * Move the selected items in this list down.
     **/
    public void moveDown() {
        int[] indices = _select.getSelectedIndices();
        Arrays.sort(indices);

        // skip over all the stuff at the bottom
        int test = _data.size() - 1;
        int index = indices.length - 1;
        while (index >= 0 && indices[index] == test) {
            index--;
            test--;
        }

        for (int i = index; i >= 0; i--) {
            swap(indices[i], indices[i] + 1);
        }
        _select.refresh();
        _select.clearSelection();
        int size = _data.size();

        // skip over all the stuff at the bottom
        test = size - 1;
        index = indices.length - 1;
        while (index >= 0 && indices[index] == test) {
            _select.addSelectionInterval(test, test);
            index--;
            test--;
        }

        for (int i = index; i >= 0; i--) {
            _select.addSelectionInterval(indices[i] + 1, indices[i] + 1);
        }
    }

    /**
     * Swap two elements in the list.
     **/
    private void swap(int i1, int i2) {
        if (i1 < 0 || i1 >= _data.size() ||
            i2 < 0 || i2 >= _data.size())
        {
            return;
        }

        int lower = i1 < i2 ? i1 : i2;
        int upper = i1 < i2 ? i2 : i1;

        Object temp = _data.remove(lower);
        _data.add(upper, temp);
    }

    private interface Selectable {
        int[] getSelectedIndices();
        void refresh();
        void clearSelection();
        void addSelectionInterval(int i1, int i2);
    }

    private final List _data;
    private final Refreshable _refresh;
    private final Selectable _select;
}
