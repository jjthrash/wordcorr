package org.wordcorr.gui.input;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.action.IconAction;

/**
 * Component that allows you to select multiple items from a
 * list. Similar in functionality to a group of checkboxes or a
 * multi-select list, but more user-friendly. The UI consists of two
 * multi-line select boxes, with arrow buttons between them that let
 * you move items back and forth, and buttons to sort selected
 * items. UI events are reported to registered change listeners.
 *
 * @author Keith Hamasaki
 **/
public class MultiChooser extends JPanel {

    //-----------------------------------------------------------------//
    // Construction
    //-----------------------------------------------------------------//

    /**
     * Constructor. Creates an empty MultiChooser.
     **/
    public MultiChooser() {
        this(Collections.EMPTY_LIST);
    }

    /**
     * Constructor.
     **/
    public MultiChooser(Object[] items) {
        this(Arrays.asList(items));
    }

    /**
     * Constructor.
     * @param items The list of items.
     **/
    public MultiChooser(List items) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // source
        JPanel sourcePane = new JPanel();
        sourcePane.setLayout(new BoxLayout(sourcePane, BoxLayout.Y_AXIS));
        MyListModel sourceModel = new MyListModel(items);
        _sourceList = new JList(sourceModel);
        sourceModel.setJList(_sourceList);
        JScrollPane sourceScroll = new JScrollPane(_sourceList);
        sourceScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceScroll.setPreferredSize(new Dimension(800, 100));
        sourcePane.add(sourceScroll);
        JLabel sourceLabel =
            new JLabel(
                AppPrefs.getInstance().getMessages().getString("lblViewMemberSource"));
        sourceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourcePane.add(sourceLabel);

        // destination
        JPanel destPane = new JPanel();
        destPane.setLayout(new BoxLayout(destPane, BoxLayout.Y_AXIS));
        MyListModel destModel = new MyListModel(Collections.EMPTY_LIST);
        _destList = new JList(destModel);
        destModel.setJList(_destList);
        JScrollPane destScroll = new JScrollPane(_destList);
        destScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        destScroll.setPreferredSize(new Dimension(800, 100));
        destPane.add(destScroll);
        JLabel destLabel =
            new JLabel(
                AppPrefs.getInstance().getMessages().getString("lblViewMemberDestination"));
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destPane.add(destLabel);

        Container middlePane =
            new ButtonBox(
                new JButton[] {
                    new JButton(
                        new MoveAction(
                            "/toolbarButtonGraphics/navigation/Forward16.gif",
                            _sourceList,
                            _destList)),
                    new JButton(
                        new MoveAction(
                            "/toolbarButtonGraphics/navigation/Back16.gif",
                            _destList,
                            _sourceList))});

        Container rightPane =
            new ButtonBox(
                new JButton[] {
                    new JButton(
                        new UpDownAction(
                            "/toolbarButtonGraphics/navigation/Up16.gif",
                            _destList,
                            true)),
                    new JButton(
                        new UpDownAction(
                            "/toolbarButtonGraphics/navigation/Down16.gif",
                            _destList,
                            false))});

        add(sourcePane);
        add(Box.createHorizontalStrut(4));
        add(middlePane);
        add(Box.createHorizontalStrut(4));
        add(destPane);
        add(Box.createHorizontalStrut(4));
        add(rightPane);
    }

    //-----------------------------------------------------------------//
    // Public Interface
    //-----------------------------------------------------------------//

    /**
     * Set the selected values on this chooser.
     **/
    public void setSelectedValues(List objects) {
        ((MyListModel) _sourceList.getModel()).moveTo(
            objects,
            (MyListModel) _destList.getModel());

        _sourceList.clearSelection();
        _destList.clearSelection();
        fireChangeEvent();
    }

    /**
     * Get the selected values on this chooser, in order.
     **/
    public List getSelectedValues() {
        return ((MyListModel) _destList.getModel()).getData();
    }

    /**
     * Refresh this object.
     **/
    public void reset(List items) {
        ((MyListModel) _sourceList.getModel()).clear();
        ((MyListModel) _sourceList.getModel()).add(items);
        ((MyListModel) _destList.getModel()).clear();
        fireChangeEvent();
    }

    /**
     * Request focus implementation.
     **/
    public void requestFocus() {
        _sourceList.requestFocus();
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

        void moveTo(List objects, MyListModel listModel) {
            remove(objects);
            listModel.add(objects);
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
     * Move button action.
     **/
    private final class MoveAction extends IconAction {

        MoveAction(String iconname, JList source, JList dest) {
            super(iconname);
            _source = source;
            _dest = dest;
        }

        public void actionPerformed(ActionEvent evt) {
            ((MyListModel) _source.getModel()).moveTo(
                Arrays.asList(_source.getSelectedValues()),
                (MyListModel) _dest.getModel());

            _source.clearSelection();
            _dest.clearSelection();
            fireChangeEvent();
        }

        private final JList _source;
        private final JList _dest;
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
    private final JList _sourceList;
    private final JList _destList;
}