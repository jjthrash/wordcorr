package org.wordcorr.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.wordcorr.db.*;
import org.wordcorr.gui.action.ShowTree;
import org.wordcorr.gui.input.InputTable;
import org.wordcorr.gui.tree.*;

/**
 * Pane that holds information about a database.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class DatabasePane extends JSplitPane implements Refreshable {

    private static final JLabel EMPTY_LABEL = new JLabel("");

    /**
     * Constructor.
     **/
    DatabasePane(Database db) throws DatabaseException {
        _db = db;

        setRightComponent(EMPTY_LABEL);
        dbTree = new DatabaseTree();
        JScrollPane scrollPane = new JScrollPane(dbTree);
        scrollPane.setMinimumSize(new Dimension(0,0));
        setLeftComponent(scrollPane);
        addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    AppPrefs.getInstance().setProperty(AppPrefs.DIVIDER_LOCATION, String.valueOf(getDividerLocation()));
                    ShowTree.getInstance().setLabel();
                }
            });
    }

    /**
     * Refresh this pane.
     **/
    public void refresh() throws DatabaseException {
    	// Refreshing the LeftComponent (DatabaseTree) rebuilds all tree nodes
    	// (User, Collection), triggering a reset of the selected pane.
    	// The current pane is saved and later restored after the refresh to fix this.
    	int lastPane = AppPrefs.getInstance().getIntProperty(AppPrefs.LAST_PANE, 0);

        dbTree.refresh();
        Object o = getRightComponent();
        if (o instanceof Refreshable) {
            ((Refreshable) o).refresh();
        }

        // set current pane
        if (o instanceof JTabbedPane)
            ((JTabbedPane) o).setSelectedIndex(lastPane);
    }

    /**
     * Insert a new user.
     **/
    public void addUser(User user) throws DatabaseException {
        dbTree.addUser(user);
    }

    /**
     * Insert a new collection.
     **/
    public void addCollection(WordCollection collection) throws DatabaseException {
        dbTree.addCollection(collection);
    }

    /**
     * Get the current user.
     **/
    public User getCurrentUser() {
        UserNode node = dbTree.getCurrentUser();
        return node == null ? null : node.getUser();
    }

    /**
     * Get the current collection.
     **/
    public WordCollection getCurrentCollection() {
        CollectionNode node = dbTree.getCurrentCollection();
        return node == null ? null : node.getCollection();
    }

    /**
     * Get the current CollectionNode.
     **/
    public CollectionNode getCurrentCollectionNode() {
        return dbTree.getCurrentCollection();
    }

    /**
     * Set the current object being edited.
     **/
    public void setCurrentEditObject(Persistent obj) {
    	setCurrentEditObject(obj, null);
    }

    /**
     * Set the current object being edited and InputTable for validation.
     **/
    public void setCurrentEditObject(Persistent obj, InputTable tab) {
        _currentEditObject = obj;
        _currentEditInputTable = tab;
    }

    /**
     * Save the current object being edited.
     **/
    public boolean saveCurrentEditObject() {
	    // save
     	if (_currentEditObject != null) {
            if (_currentEditObject.isDirty()) {
                try {
                    _currentEditObject.save();
                } catch (DatabaseException e) {
                    _currentEditObject = null;
                    return true;
                }
     		}
        }
        return true;
    }   

    /**
     * Validate the current object being edited.
     **/
    public boolean validateCurrentEditObject() {
        if (_currentEditInputTable == null)
	        return true;

    	// validate
       	if (!_currentEditInputTable.validateFields())
       		return false;
       	
   		try {
       		String msg = _currentEditObject.checkValidation();
	        if (msg != null) {
	        	_currentEditInputTable.setValues(_currentEditObject);
	        	Dialogs.msgbox(msg);
				    return false;
	        }
   		} catch (DatabaseException e) { }
	    return true;
    }   

    /**
     * Left hand pane, which contains a tree of users and
     * collections in this database.
     **/
    private final class DatabaseTree extends JTree implements Refreshable {

        DatabaseTree() throws DatabaseException {
            super(new BranchNode(AppPrefs.getInstance().getMessages().getString("lblUsers")));
            MainFrame mf = MainFrame.getInstance();
            setShowsRootHandles(true);
            getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent evt) {
		        	if (_cancelValueChanged) {
		        		_cancelValueChanged = false;
		        		return;
		        	}
		                    	
		            Object obj = evt.getPath().getLastPathComponent();
		
		            // we save the location and restore it after because swing
		            // likes to reformat everything and it's kind of annoying
		            int loc = getDividerLocation();
		            MainFrame.getInstance().setUserOnlyEnabled(false);
		            if (obj instanceof WNode) {
		                // check validation
		                if (!validateCurrentEditObject()) {
		                	_cancelValueChanged = true;
		                	setSelectionPath(_lastPath);
		                	return;                       
		                }
		                _lastPath = getSelectionPath();
		                        
		                WNode node = (WNode) obj;
		                Component comp = node.getRightComponent();
		                if (comp != null) {
		                    // save/set current edit object when changing user/collection 
		                    saveCurrentEditObject();
		                                    
		                    // UserNode, CollectionNode
		                    setRightComponent(comp);
		                    
		                    // set current edit object when changing user/collection 
		                    if (node instanceof UserNode) {
		                    	// user
			                    setCurrentEditObject((Persistent)node.getUserObject(),
				                    ((SavePane)comp).getPropertyPane().getInfo());
		                    } else {
			                    // set to default pane
			                    if (node instanceof CollectionNode) {
			                        ((CollectionNode)node).resetSelectedPane();
			                    }
			                    if (comp instanceof Refreshable) {
			                    	try {
			                    		((Refreshable)comp).refresh();
			                    	} catch (DatabaseException ignored) { }
			                    }
		                    }
		                    
		                    try {
		                        node.updateSetting(_db.getCurrentSetting());
		                    } catch (DatabaseException ignored) { }
		                    MainFrame.getInstance().setUserOnlyEnabled(true);
		                } else {
		                    // Users label BranchNode
		                    setRightComponent(EMPTY_LABEL);

		                    // set current edit object to null
		                    setCurrentEditObject(null);
		                }
		            } else {
		                // should never happen, top root is a BranchNode
		                setRightComponent(EMPTY_LABEL);
		            }
		            setDividerLocation(loc);
		            MainFrame.getInstance().updateStatus();
		        }
        });            
            refresh();
            setExpandedState(getPathForRow(0), true);
            setCellRenderer(new DatabaseTreeCellRenderer());
        }

        void addUser(User user) throws DatabaseException {
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            UserNode newNode = new UserNode(user, model);
            model.insertNodeInto(newNode, root, root.getChildCount());

            // check for the placeholder
            if (root.getChildAt(0) instanceof HolderNode) {
                model.removeNodeFromParent((HolderNode) root.getChildAt(0));
            }

            setSelectionPath(new TreePath(newNode.getPath()));
        }

        UserNode getCurrentUser() {
            // find the selected user
            TreePath path = getSelectionPath();
            if (path == null)
                return null;
            UserNode userNode = null;
            for (int i = 0; i < path.getPathCount(); i++) {
                Object o = path.getPathComponent(i);
                if (o instanceof UserNode) {
                    userNode = ((UserNode) o);
                    break;
                }
            }

            return userNode;
        }

        CollectionNode getCurrentCollection() {
            // find the selected collection
            TreePath path = getSelectionPath();
            if (path == null)
                return null;
            CollectionNode node = null;
            for (int i = 0; i < path.getPathCount(); i++) {
                Object o = path.getPathComponent(i);
                if (o instanceof CollectionNode) {
                    node = ((CollectionNode) o);
                    break;
                }
            }

            return node;
        }

        void addCollection(WordCollection collection) throws DatabaseException {
            UserNode userNode = getCurrentUser();
            DefaultTreeModel model = (DefaultTreeModel) getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
            CollectionNode newNode = new CollectionNode(collection, model);
            model.insertNodeInto(newNode, userNode, userNode.getChildCount());
            setSelectionPath(new TreePath(newNode.getPath()));
        }

        public void refresh() throws DatabaseException {
            // store expanded paths
            List expands = new ArrayList();
            List newExpands = new ArrayList();
            for (Enumeration _enum = getExpandedDescendants(getPathForRow(0));
                 _enum.hasMoreElements(); )
            {
                expands.add(_enum.nextElement());
            }

            DefaultTreeModel model = (DefaultTreeModel) getModel();
            MutableTreeNode root = (MutableTreeNode) model.getRoot();

            // remove everything first
            for (int i = root.getChildCount() - 1; i >= 0; i--) {
                root.remove(i);
            }

            // now add new stuff
            TreePath newPath = null;
            Setting setting = _db.getCurrentSetting();
            for (Iterator it = _db.getUsers().iterator(); it.hasNext(); ) {
                User user = (User) it.next();
                UserNode node = new UserNode(user, model);
                root.insert(node, root.getChildCount());
                TreePath nodePath = new TreePath(node.getPath());
                if (setting.getUserID() == user.getID()) {
                    newPath = nodePath;
                }
                if (containsPath(expands, nodePath)) {
                    newExpands.add(nodePath);
                }

                for (Iterator it2 = user.getCollections().iterator(); it2.hasNext(); ) {
                    WordCollection col = (WordCollection) it2.next();
                    CollectionNode cnode = new CollectionNode(col, model);
                    node.insert(cnode, node.getChildCount());
                    if (setting.getCollectionID() == col.getID()) {
                        newPath = new TreePath(cnode.getPath());
                    }
                }
            }

            if (root.getChildCount() == 0) {
                root.insert(new HolderNode(AppPrefs.getInstance().getMessages().getString("msgNoUsers")), 0);
            }
            model.nodeStructureChanged(root);

            // reapply selected and expanded paths
            if (newPath != null) {
                setSelectionPath(newPath);
            }
            for (Iterator it = newExpands.iterator(); it.hasNext(); ) {
                TreePath path = (TreePath) it.next();
                setExpandedState(path, true);
            }
        }

        private boolean containsPath(List list, TreePath path) {
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                TreePath test = (TreePath) it.next();
                if (path.getLastPathComponent().equals(test.getLastPathComponent())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Tree cell renderer for database tree.
     **/
    private final class DatabaseTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row, hasFocus);

            if (value instanceof HolderNode) {
                setIcon(null);
            } else if (value instanceof WNode) {
                ImageIcon icon = ((WNode) value).getIcon();
                if (icon != null) {
                    setIcon(icon);
                }
            }
            return this;
        }
    }

    private final Database _db;
    private Persistent _currentEditObject = null;
    private InputTable _currentEditInputTable = null;
    private TreePath _lastPath = null;
    private boolean _cancelValueChanged = false;
    private DatabaseTree dbTree;
}
