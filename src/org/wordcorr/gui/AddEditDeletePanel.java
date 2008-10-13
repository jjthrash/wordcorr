package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.action.IconAction;

/**
 * Panel that has a table and add/edit/delete buttons.
 * @author Keith Hamasaki, Jim Shiba
 **/
class AddEditDeletePanel extends ButtonPanel implements Refreshable {

    /**
     * Constructor.
     **/
    AddEditDeletePanel(Refreshable refresh, boolean showAdd) {
        this(refresh, showAdd, false, false);
    }

    /**
     * Constructor.
     **/
    AddEditDeletePanel(
        Refreshable refresh,
        boolean showAdd,
        boolean showCopy,
        boolean showValidate) {
        this(refresh, showAdd, showCopy, showValidate, false, false);
    }

    /**
     * Constructor.
     **/
    AddEditDeletePanel(
        Refreshable refresh,
        boolean showAdd,
        boolean showCopy,
        boolean showValidate,
        boolean showRefresh,
        boolean sortable) {
        super(new JSplitPane());
        _refresh = refresh;
        _showAdd = showAdd;
        _showCopy = showCopy;
        _showValidate = showValidate;
        _showRefresh = showRefresh;
        _setCurrentEditObjectSetting = true;

        // do the list selection for this table
        final JSplitPane panel = (JSplitPane) getMainComponent();
        panel.setDividerLocation(104);
        ListSelectionModel model = _list.getSelectionModel();
        model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        model.addListSelectionListener(new ListSelectionListener() {
            public synchronized void valueChanged(ListSelectionEvent evt) {
                if (_cancelValueChanged) {
                    _cancelValueChanged = false;
                    return;
                }

                if (evt.getValueIsAdjusting()) {
                    return;
                }

                // we save the location and restore it after because swing
                // likes to reformat everything and it's kind of annoying
                int loc = panel.getDividerLocation();

                int index = _list.getSelectedIndex();
                if (_list.getModel().getSize() > 0 && index != -1) {
                    Persistent persistent = (Persistent) _list.getSelectedValue();

                    // check validation
                    DatabasePane dbPane = MainFrame.getInstance().getDatabasePane();
                    if (!dbPane.validateCurrentEditObject()) {
                        _cancelValueChanged = true;
                        _list.setSelectedIndex(_lastIndex);
                        return;
                    }
                    _lastIndex = index;

                    _del.setEnabled(true);
                    _validate.setEnabled(true);

                    // save current edit object when selecting persistent object
                    if (_setCurrentEditObjectSetting) {
                        dbPane.saveCurrentEditObject();
                    }

                    Component component = (Component) _componentMap.get(persistent);
                    if (component == null) {
                        component = createRightComponent();
                        _componentMap.put(persistent, component);
                    }
                    panel.setRightComponent(component);

                    // set current edit object when selecting persistent object
                    if (_setCurrentEditObjectSetting) {
                        if (component instanceof SavePane) {
                            dbPane.setCurrentEditObject(
                                persistent,
                                ((SavePane) component).getPropertyPane().getInfo());
                        } else {
                            dbPane.setCurrentEditObject(persistent);
                        }
                    }
                } else {
                    panel.setRightComponent(new JLabel(""));
                    _del.setEnabled(false);
                }
                panel.setDividerLocation(loc);
            }
        });

        JScrollPane scroll = new JScrollPane(_list);

        if (sortable) {
            JPanel left = new JPanel(new BorderLayout());
            left.add(new MoveButtonPanel(), BorderLayout.NORTH);
            left.add(scroll, BorderLayout.CENTER);
            panel.setLeftComponent(left);
        } else {
            panel.setLeftComponent(scroll);
        }
        panel.setRightComponent(new JLabel(""));
        addButtons();
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    }

    public final void refresh() throws DatabaseException {
        if (_refresh != null) {
            _refresh.refresh();
        }
        // call the extra refresh method
        refreshExt();

        _del.setEnabled(_list.getSelectedIndex() != -1);

        // clear the component map and force a recreation of the
        // current component, if any.
        _componentMap.clear();
        int index = _list.getSelectedIndex();
        _list.clearSelection();
        if (index >= 0 && index < _list.getModel().getSize()) {
            _list.setSelectedIndex(index);
        }
    }

    /**
     * Subclasses can override to perform extra refresh behavior.
     **/
    protected void refreshExt() throws DatabaseException {}

    /**
     * Get the JList associated with this panel.
     **/
    protected JList getList() {
        return _list;
    }

    /**
     * Create the right component. The default implementation returns
     * a SavePane initialized with the persistent object.
     **/
    protected Component createRightComponent() {
        Persistent persistent = (Persistent) _list.getSelectedValue();
        return new SavePane(null, persistent, (Refreshable) _list.getModel());
    }

    /**
     * Add a listener class.
     **/
    public final void addAddEditDeleteListener(AddEditDeleteListener l) {
        _listeners.add(l);
    }

    /**
     * Get the buttons
     **/
    private void addButtons() {
        JButton add = new AlignedButton("btnAdd", "accAdd", SwingConstants.CENTER);
        add.setActionCommand("add");
        add.addActionListener(_listener);

        _copy = new AlignedButton("btnCopy", "accCopy", SwingConstants.CENTER);
        _copy.setActionCommand("copy");
        _copy.addActionListener(_listener);

        _del.setActionCommand("delete");
        _del.addActionListener(_listener);
        _del.setEnabled(false);

        _validate =
            new AlignedButton("btnValidate", "accValidate", SwingConstants.CENTER);
        _validate.setActionCommand("validate");
        _validate.addActionListener(_listener);
        _validate.setEnabled(false);

        JButton prev =
            new AlignedButton("btnPrevious", "accPrevious", SwingConstants.CENTER);
        prev.setActionCommand("previous");
        prev.addActionListener(_listener);

        JButton next = new AlignedButton("btnNext", "accNext", SwingConstants.CENTER);
        next.setActionCommand("next");
        next.addActionListener(_listener);

        _find = new AlignedButton("btnFind", "accFind", SwingConstants.CENTER);
        _find.setActionCommand("find");
        _find.addActionListener(_listener);

        if (_showRefresh) {
            JButton refresh = new RefreshButton(SwingConstants.CENTER);
            addButton(refresh);
        }

        if (_showAdd) {
            addButton(add);
            addButton(_del);
        }

        if (_showCopy)
            addButton(_copy);

        if (_showValidate)
            addButton(_validate);

        addSeparator();

        addButton(prev);
        addButton(next);

        addSeparator();

        addButton(_find);
    }

    /**
     * Set delete button label.
     **/
    public void setDeleteButtonLabel(String label) {
        _del.setText(label);
    }

    /**
     * Set find button label.
     **/
    public void setFindButtonLabel(String label) {
        _find.setText(label);
    }

    /**
     * Set current edit object setting.
     **/
    public void setCurrentEditObjectSetting(boolean val) {
        _setCurrentEditObjectSetting = val;
    }

    /**
     * Action Listener for the buttons.
     **/
    private class ButtonListener implements ActionListener {

        /**
         * Action Listener method.
         **/
        public void actionPerformed(ActionEvent evt) {
            Dialogs.showWaitCursor(AddEditDeletePanel.this);
            Messages messages = AppPrefs.getInstance().getMessages();
            try {
                String cmd = ((JButton) evt.getSource()).getActionCommand();
                String msg;
                if (cmd.equals("add")) {
                    for (Iterator it = _listeners.iterator();
                        it.hasNext();
                        ((AddEditDeleteListener) it.next()).doAdd(evt));
                } else if (cmd.equals("copy")) {
                    for (Iterator it = _listeners.iterator();
                        it.hasNext();
                        ((AddEditDeleteListener) it.next()).doCopy(evt));
                    refresh();
                } else if (cmd.equals("delete")) {
                    if (Dialogs.confirm(messages.getString("msgConfirmDelete"))) {
                        for (Iterator it = _listeners.iterator();
                            it.hasNext();
                            ((AddEditDeleteListener) it.next()).doDelete(evt));
                        refresh();
                    }
                } else if (cmd.equals("validate")) {
                    for (Iterator it = _listeners.iterator();
                        it.hasNext();
                        ((AddEditDeleteListener) it.next()).doValidate(evt));
                } else if (cmd.equals("next")) {
                    int index = _list.getSelectedIndex();
                    if (index != _list.getModel().getSize() - 1) {
                        _list.setSelectedIndex(index + 1);
                    }
                    // keep selected item in view
                    _list.ensureIndexIsVisible(_list.getSelectedIndex());
                } else if (cmd.equals("previous")) {
                    int index = _list.getSelectedIndex();
                    if (index > 0) {
                        _list.setSelectedIndex(index - 1);
                    }
                    // keep selected item in view
                    _list.ensureIndexIsVisible(_list.getSelectedIndex());
                } else if (cmd.equals("find")) {
                    FindDialog dialog = createFindDialog();
                    dialog.addFindListener(new FindDialog.FindListener() {
                        public void find(FindDialog.FindEvent findEvt) {
                            String text = findEvt.getProperties().getFindWhat();
                            if (text == null || text.equals("")) {
                                return;
                            }

                            String match = findEvt.getProperties().getMatch();
                            int start = _list.getSelectedIndex() + 1;
                            if (start == _list.getModel().getSize()) {
                                start = 0;
                            }
                            for (int i = start; i != start - 1; i++) {
                                Object obj = _list.getModel().getElementAt(i);
                                if (match.equals(FindDialog.MATCH_WHOLE) && obj.toString().equals(text)) {
                                    _list.setSelectedValue(obj, true);
                                    return;
                                } else if (
                                    match.equals(FindDialog.MATCH_ANY) && obj.toString().indexOf(text) != -1) {
                                    _list.setSelectedValue(obj, true);
                                    return;
                                } else if (
                                    match.equals(FindDialog.MATCH_START) && obj.toString().startsWith(text)) {
                                    _list.setSelectedValue(obj, true);
                                    return;
                                }
                                if (i == _list.getModel().getSize() - 1) {
                                    if (start == 0) {
                                        break;
                                    }
                                    i = -1;
                                }
                            }
                            // if we got here, it's not found, so beep
                            Toolkit.getDefaultToolkit().beep();
                        }
                    });
                    dialog.setVisible(true);
                }
            } catch (DatabaseException e) {
                Dialogs.genericError(e);
            } finally {
                Dialogs.showDefaultCursor(AddEditDeletePanel.this);
            }
        }
    }

    /**
     * Override to provide custom find dialog
     */
    protected FindDialog createFindDialog() {
        return new FindDialog();
    }
    
    /**
     * Panel containing move buttons.
     **/
    private final class MoveButtonPanel extends Box {
        MoveButtonPanel() {
            super(BoxLayout.X_AXIS);
            this
                .add(new JButton(new IconAction("/toolbarButtonGraphics/navigation/Up16.gif") {
                public void actionPerformed(ActionEvent evt) {
                    Dialogs.showWaitCursor(MainFrame.getInstance());
                    try {
                        for (Iterator it = _listeners.iterator();
                            it.hasNext();
                            ((AddEditDeleteListener) it.next()).doMoveUp(evt));
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    } finally {
                        Dialogs.showDefaultCursor(MainFrame.getInstance());
                    }
                }
            }));

            this
                .add(
                    new JButton(new IconAction("/toolbarButtonGraphics/navigation/Down16.gif") {
                public void actionPerformed(ActionEvent evt) {
                    Dialogs.showWaitCursor(MainFrame.getInstance());
                    try {
                        for (Iterator it = _listeners.iterator();
                            it.hasNext();
                            ((AddEditDeleteListener) it.next()).doMoveDown(evt));
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    } finally {
                        Dialogs.showDefaultCursor(MainFrame.getInstance());
                    }
                }
            }));
        }
    }

    private final Refreshable _refresh;
    private final boolean _showAdd;
    private final boolean _showCopy;
    private final boolean _showRefresh;
    private final boolean _showValidate;
    private final JButton _del =
        new AlignedButton("btnDelete", "accDelete", SwingConstants.CENTER);
    private final List _listeners = new LinkedList();
    private final Map _componentMap = new HashMap();
    private final JList _list = new JList();
    private final ButtonListener _listener = new ButtonListener();
    private boolean _cancelValueChanged = false;
    private boolean _setCurrentEditObjectSetting;
    private int _lastIndex = 0;
    private JButton _copy;
    private JButton _find;
    private JButton _validate;
}