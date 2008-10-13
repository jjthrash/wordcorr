package org.wordcorr.gui.input;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.action.IconAction;

/**
 * Component that allows you to sort a list using a multi-select list.
 *
 * @author Jim Shiba
 **/
public class MultiSorter extends JPanel {

    //-----------------------------------------------------------------//
    // Construction
    //-----------------------------------------------------------------//

    /**
     * Constructor. Creates an empty MultiSorter.
     **/
    public MultiSorter() {
        this(Collections.EMPTY_LIST);
    }

    /**
     * Constructor.
     * @param items The list of items.
     **/
    public MultiSorter(List items) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        _model = new MyListModel(items);
        _list = new JList(_model);
        _model.setJList(_list);

        JScrollPane scrollPane = new JScrollPane(_list);
        scrollPane.setPreferredSize(new Dimension(200, 100));

        Container buttonPane =
            new ButtonBox(
                new JButton[] {
                    new JButton(
                        new UpDownAction("/toolbarButtonGraphics/navigation/Up16.gif", _list, true)),
                    new JButton(
                        new UpDownAction(
                            "/toolbarButtonGraphics/navigation/Down16.gif",
                            _list,
                            false))});

        add(scrollPane);
        add(Box.createHorizontalStrut(4));
        add(buttonPane);
    }

    //-----------------------------------------------------------------//
    // Public Interface
    //-----------------------------------------------------------------//

    /**
     * Get list.
     **/
    public List getList() {
        return _model.getData();
    }

    /**
     * Refresh this object.
     * @param items The list of items.
     **/
    public void reset(List items) {
        _model.clear();
        _model.add(items);
        fireChangeEvent();
    }

    /**
     * Add a change listener to this object.
     **/
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Remove a change listener from this object.
     **/
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * Notify all listeners that have registered interest for
     * change events.
     **/
    protected void fireChangeEvent() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(_event);
            }
        }
    }

    //-----------------------------------------------------------------//
    // Inner Classes
    //-----------------------------------------------------------------//

    /**
     * Simple Box subclass that contains buttons arranged vertically.
     **/
    private static final class ButtonBox extends Box {
        ButtonBox(JButton[] buttons) {
            super(BoxLayout.Y_AXIS);
            for (int i = 0; i < buttons.length; i++) {
                add(buttons[i]);
                add(Box.createVerticalStrut(4));
            }
        }
    }

    /**
     * List model class for lists in this object.
     **/
    private static final class MyListModel
        extends AbstractListModel
        implements Refreshable {
        MyListModel(List list) {
            _list = new LinkedList(list);
        }

        void setJList(JList jlist) {
            _sorter = new SortableList(jlist, _list, this);
        }

        SortableList getSortableList() {
            return _sorter;
        }

        void clear() {
            _list.clear();
            refresh();
        }

        void remove(List objects) {
            _list.removeAll(objects);
            refresh();
        }

        void add(List objects) {
            _list.addAll(objects);
            refresh();
        }

        public void refresh() {
            fireContentsChanged(this, 0, _list.size());
        }

        public int getSize() {
            return _list.size();
        }

        public Object getElementAt(int i) {
            if (i < 0 || i >= _list.size()) {
                return null;
            }

            return _list.get(i);
        }

        public List getData() {
            return new ArrayList(_list);
        }

        private final List _list;
        private SortableList _sorter;
    }

    /**
     * Move up/down button action.
     **/
    private final class UpDownAction extends AbstractAction {
        UpDownAction(String iconname, JList list, boolean up) {
            putValue(SMALL_ICON, new ImageIcon(this.getClass().getResource(iconname)));
            _list = list;
            _up = up;
        }

        public void actionPerformed(ActionEvent evt) {
            int[] indices = _list.getSelectedIndices();
            if (_up) {
                ((MyListModel) _list.getModel()).getSortableList().moveUp();
            } else {
                ((MyListModel) _list.getModel()).getSortableList().moveDown();
            }
            fireChangeEvent();

            // keep selected item in view
            _list.ensureIndexIsVisible(_list.getSelectedIndex());
        }

        private final JList _list;
        private final boolean _up;
    }

    private final ChangeEvent _event = new ChangeEvent(this);
    private final JList _list;
    private final MyListModel _model;
}