package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Alignment;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Entry;
import org.wordcorr.db.Group;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Setting;
import org.wordcorr.db.View;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.AliasTextRow;

/**
 * Pane that holds all of the views in the current collection.
 * @author Keith Hamasaki, Jim Shiba
 **/
class ViewsPane extends AddEditDeletePanel {

    private static interface AddRunnable extends Runnable {
        public DatabaseException getException();
    }
    
    ViewsPane(WordCollection collection) {
        super(null, true, true, false);
        final Messages messages = AppPrefs.getInstance().getMessages();

        // change find label
        setFindButtonLabel(messages.getString("btnFindViews"));

        _collection = collection;
        getList().setModel(new BasicListModel());

        final MainFrame mf = MainFrame.getInstance();
        ListSelectionModel model = getList().getSelectionModel();
        model.addListSelectionListener(new ListSelectionListener() {
            public synchronized void valueChanged(ListSelectionEvent evt) {
                try {
                    View view = (View) getList().getSelectedValue();
                    if (view != null) {
                        Setting setting = _collection.getDatabase().getCurrentSetting();
                        setting.setViewID(view.getID());
                        setting.save();
                        mf.updateStatus();
                    }
                } catch (DatabaseException e) {
                    Dialogs.genericError(e);
                }
            }
        });

        addAddEditDeleteListener(new AddEditDeleteListener() {
            public void doAdd(ActionEvent evt) throws DatabaseException {
                final View view = _collection.makeView();
                AddDialog dialog = new AddDialog("lblAddView", view);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    // add default groups and alignments
                    AddRunnable task = new AddRunnable() {
                        public void run() {
                            try {
                                ViewDuplicator.makeGroupsAndAlignments(view, view, _collection, false);
                                
                                // update collection view list
                                List views = _collection.getViews();
                                views.add(view);
                                
                                _collection.getDatabase().getCurrentSetting().setViewID(view.getID());
                                refresh();
                            } catch (DatabaseException ex) {
                                exception = ex;
                            }
                        }
                        DatabaseException exception = null;
                        public DatabaseException getException() {return exception;}
                    };
                    Messages m = AppPrefs.getInstance().getMessages();
                    Dialogs.indeterminateProgressDialog(task, m.getString("pgbWaitString"), m.getString("pgbCurrentTask") + m.getString("lblAddView"));
                    if (task.getException() != null) { //so we don't swallow the exception from the other thread.
                        throw task.getException();
                    }
                }
            }

            public void doCopy(ActionEvent evt) throws DatabaseException {
                ViewDuplicator duplicator =
                    new ViewDuplicator((View) getList().getSelectedValue(), _collection);
                TaskDialog dialog = new TaskDialog("lblViewCopy", duplicator);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    // update collection view list
                    List views = _collection.getViews();
                    View view = duplicator.getDuplicate();
                    if (view != null) { //view will be null if attempted duplicate view name.
                        views.add(view);
                        _collection.getDatabase().getCurrentSetting().setViewID(view.getID());
                        refresh();
                    }
                }
            }

            public void doDelete(ActionEvent evt) throws DatabaseException {
                View view = (View) getList().getSelectedValue();

                // check for Original view
                View original = _collection.getOriginalView();
                if (view.equals(original)) {
                    Dialogs.error(messages.getString("msgErrDeletingOriginalView"));
                } else {
                    view.delete();

                    // update collection view list
                    List views = _collection.getViews();
                    views.remove(view);
                    changeView(original);

                    JSplitPane split = (JSplitPane) getMainComponent();
                    int loc = split.getDividerLocation();
                    split.setRightComponent(new JLabel(""));
                    split.setDividerLocation(loc);
                    refresh();
                }
            }

            public void doValidate(ActionEvent evt) {}
            public void doMoveUp(ActionEvent evt) {}
            public void doMoveDown(ActionEvent evt) {}
        });
        
        getList().setCellRenderer(new ViewNameListCellRenderer());
    }

    private static class ViewNameListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (String.valueOf(value).equals("Original")) {
                setText(AppPrefs.getInstance().getMessages().getString("lblViewNameOriginal"));
            }
            //TODO, also remember about adding views, shouldn't allow localized original value
            return this;
        }
    }

    protected Component createRightComponent() {
        SavePane pane = (SavePane) super.createRightComponent();
        AliasTextRow row = (AliasTextRow) pane.getPropertyPane().getInfo().getRow("name");
        row.setAliasing("Original", AppPrefs.getInstance().getMessages().getString("lblViewNameOriginal"));
        return pane;
    }    
    
    /**
     * Change current view selection.
     **/
    public void changeView(View view) throws DatabaseException {
        _collection.getDatabase().getCurrentSetting().setViewID(view.getID());
        getList().setSelectedValue(view, true);
    }

    /**
     * Refresh.
     **/
    public void refreshExt() throws DatabaseException {
        ((BasicListModel) getList().getModel()).setData(_collection.getViews());
        setSelect();
    }

    /**
     * Set view selection to user's choice.
     **/
    public void setSelect() throws DatabaseException {
        JList list = getList();
        View original = null;
        View view = null;
        long id = _collection.getDatabase().getCurrentSetting().getViewID();
        if (id != -1) {
            for (int i = 0; i < list.getModel().getSize(); i++) {
                View vw = (View) list.getModel().getElementAt(i);
                if (id == vw.getID()) {
                    view = vw;
                    break;
                } else if (vw.getName().equals("Original")) {
                    original = view;
                }
            }
        }
        // default Original
        if (view == null)
            view = original;

        list.setSelectedValue(view, true);
        MainFrame.getInstance().updateStatus();
    }

    /**
     * Input Row for grapheme clusters.
     **/
    public static final class GraphemeClustersRow extends InputRow {

        public GraphemeClustersRow(
            BeanCatalog.Property prop,
            Object obj,
            final Refreshable refresh) {
            super(prop, obj);
            _view = (View) obj;

            JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            _undefineButton =
                new WButton(new WordCorrAction(
                    "btnGraphemeClusterUndefine",
                    "accGraphemeClusterUndefine") {
                public void actionPerformed(ActionEvent evt) {
                    if (doUndefineGraphemeCluster()) {
                        try {
                            // update list
                            setValue(_view.getGraphemeClusters());
                        } catch (DatabaseException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            btnpanel.add(_undefineButton, BorderLayout.NORTH);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(_text, BorderLayout.NORTH);
            panel.add(btnpanel, BorderLayout.CENTER);
            init(panel, _text);
            _text.setFont(FontCache.getIPA());
        }

        public void setValue(Object value) {
            String list = "";
            if (((List) value).isEmpty()) {
                list = " ";
                _undefineButton.setEnabled(false);
            } else {
                for (Iterator it = ((List) value).iterator(); it.hasNext();) {
                    String gc = (String) it.next();
                    list += (list.equals("")) ? gc : ", " + gc;
                }
                _undefineButton.setEnabled(true);
            }
            _text.setText(list);
        }

        public Object getValue() {
            return null;
        }

        private boolean doUndefineGraphemeCluster() {
            Messages messages = AppPrefs.getInstance().getMessages();
            TaskDialog dialog =
                new TaskDialog(
                    "lblViewsUndefineGraphemeCluster",
                    new UndefineGraphemeClusterTask(_view),
                    "ViewsUndefineGraphemeCluster");
            dialog.setVisible(true);

            return !dialog.isCancelled();
        }

        private WButton _undefineButton;
        private final JLabel _text = new JLabel();
        private final View _view;
    }

    /**
     * Input Row to undefine grapheme cluster.
     **/
    public static final class ViewsUndefineGraphemeClusterRow extends InputRow {
        /**
         * Constructor.
         **/
        public ViewsUndefineGraphemeClusterRow(
            BeanCatalog.Property prop,
            Object obj,
            Refreshable refresh) {
            super(prop, obj);
            _data = (UndefineGraphemeClusterTask) obj;
            _refresh = refresh;
            init(_combo, _combo);
            _combo.setFont(FontCache.getIPA());
            refresh();
        }

        /**
         * Refresh this object.
         **/
        public void refresh() {
            ((BasicListModel) _combo.getModel()).setData(_data.getGraphemeClusters());
        }

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
        protected final UndefineGraphemeClusterTask _data;
    }

    /**
     * Task to undefine grapheme cluster class.
     **/
    public static class UndefineGraphemeClusterTask implements Task {

        public UndefineGraphemeClusterTask(View view) {
            _view = view;
        }

        public List getGraphemeClusters() {
            List graphemeClusters = Collections.EMPTY_LIST;
            try {
                graphemeClusters = _view.getGraphemeClusters();
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            }
            return graphemeClusters;
        }

        public void setValue(Object value) {
            _value = value;
        }

        public Object getValue() {
            return _value;
        }

        /**
         * Run task.
         * Return true to close dialog, false to keep open.
         **/
        public boolean run() {
            try {
                _view.undefineGraphemeCluster((String) _value);
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            }
            return true;
        }

        private Object _value;
        private View _view;
    }

    /**
     * View copy bean class.
     **/
    public static final class ViewDuplicator implements Task {
        ViewDuplicator(View original, WordCollection collection) {
            super();
            _original = original;
            _collection = collection;
        }

        /**
         * Run task.
         * Return true to close dialog, false to keep open.
         **/
        public boolean run() {
            if (_original == null)
                return true;

            // create based on type
            try {
                switch (Integer.parseInt(_type)) {
                    case 0 :
                        _duplicate = makeVarietyType();
                        break;
                    case 1 :
                        _duplicate = makeAnnotateType();
                        break;
                }
                if (_duplicate == null) {
                    return false; //attempted to create copy with same View name as an existing view.
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
                _duplicate = null;
            }
            return true;
        }

        // Attributes
        public View getDuplicate() {
            return _duplicate;
        }
        public String getName() {
            return _name;
        }
        public void setName(String v) {
            _name = v;
        }
        public String getType() {
            return _type;
        }
        public void setType(String v) {
            _type = v;
        }

        // Persistent methods
        private View makeVarietyType() throws DatabaseException {
            View dup = makeView();
            if (dup != null) {
                makeGroupsAndAlignments(dup, _original, _collection, false);
            }
            return dup;
        }

        private View makeAnnotateType() throws DatabaseException {
            View dup = makeView();
            if (dup != null) {
                makeGroupsAndAlignments(dup, _original, _collection, true);
            }
            return dup;
        }
        private View makeView() throws DatabaseException {
            View dup = _collection.makeView();

            // copy properties
            dup.setName(getName());
            dup.setThreshold(_original.getThreshold());
            dup.setMembers(_original.getMembers());
            dup.setRemarks(_original.getRemarks());

            // save new duplicate, but first make sure duplicate doesn't have
            // a duplicate view name.
            String msg = dup.checkValidation();
            if (msg != null) {
                Dialogs.msgbox(msg);
                return null;
            }
            dup.save();
            return dup;
        }
        public static void makeGroupsAndAlignments(
            View dup,
            View original,
            WordCollection collection,
            boolean copy)
            throws DatabaseException {
            // loop through entries
            Map groups = new HashMap();
            List entries = collection.getEntries();
            for (Iterator it = entries.iterator(); it.hasNext();) {
                // groups are unique to each entry
                groups.clear();
                Entry entry = (Entry) it.next();

                // loop through alignments
                List alignments = original.getAlignments(entry);
                for (Iterator it2 = alignments.iterator(); it2.hasNext();) {
                    Alignment alignment = (Alignment) it2.next();
                    Group group = (copy) ? alignment.getGroup() : null;

                    // get or create duplicate group
                    String name = (group == null) ? "?" : group.getName();
                    Group dupGroup = (Group) groups.get(name);
                    if (dupGroup == null) {
                        dupGroup = dup.makeGroup(entry);
                        dupGroup.setName(name);
                        dupGroup.setDone(false);
                        dupGroup.save();
                        groups.put(dupGroup.getName(), dupGroup);
                    }

                    // create duplicate alignment
                    Alignment dupAlignment = dup.makeAlignment(alignment.getDatum());
                    dupAlignment.setName(alignment.getName());
                    dupAlignment.setGroup(dupGroup);
                    if (copy) {
                        dupAlignment.setVector(alignment.getVector());
                        dupAlignment.setMetathesis1(alignment.getMetathesis1());
                        dupAlignment.setLength1(alignment.getLength1());
                        dupAlignment.setMetathesis2(alignment.getMetathesis2());
                        dupAlignment.setLength2(alignment.getLength2());
                        dupAlignment.setObservations(alignment.getObservations());
                    }
                    dupAlignment.save();
                }
            }
        }

        private String _name;
        private String _type;
        private View _duplicate = null;
        private View _original;
        private WordCollection _collection;
    }
    private final WordCollection _collection;
}