package org.wordcorr.gui;

import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Setting;
import org.wordcorr.db.View;
import org.wordcorr.gui.tree.LeafNode;
import java.awt.Component;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;

/**
 * Node that contains a word list collection.
 * @author Keith Hamasaki, Jim Shiba
 **/
final class CollectionNode extends LeafNode implements Refreshable {

    /**
     * Constructor.
     **/
    CollectionNode(WordCollection collection, DefaultTreeModel model) {
        super(collection);
        _collection = collection;
        _model = model;
    }

    /**
     * Get the tree icon for this node.
     **/
    public ImageIcon getIcon() {
        return new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/table/ColumnInsertBefore16.gif"));
    }

    /**
     * Get the right side component for this collection.
     **/
    public synchronized Component getRightComponent() {
        if (_rightPane == null) {
            _rightPane = new RightPane();
        }
        return _rightPane;
    }

    /**
     * Get the collection associated with this node.
     **/
    public WordCollection getCollection() {
        return _collection;
    }

    /**
     * Reset selected index of tabbed pane to default (CollectionPane).
     **/
    public void resetSelectedPane() {
    	RightPane pane = (RightPane)getRightComponent();
    	pane.setSelectedIndex(0);
    }

    /**
     * Update the setting with this object as their current.
     **/
    public void updateSetting(Setting setting) throws DatabaseException {
        setting.setCollectionID(_collection.getID());
        setting.setUserID(_collection.getUser().getID());
        View view = _collection.getViewByID(setting.getViewID());
        if (view == null) {
            view = _collection.getOriginalView();
        }
        setting.setViewID(view.getID());
        setting.save();
        MainFrame.getInstance().updateStatus();

        // refresh panes
        ((RightPane)getRightComponent()).refresh();
    }

    /**
     * Refresh this node.
     **/
    public void refresh() {
        _model.nodeChanged(this);
        
        // set current edit object for collection
        if (_rightPane != null && _rightPane.getSelectedIndex() == 0)
            MainFrame.getInstance().getDatabasePane().setCurrentEditObject(_collection,
                ((SavePane)_rightPane.getComponentAt(0)).getPropertyPane().getInfo());
    }

    /**
     * Does this node equal another?
     **/
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        return ((CollectionNode) o)._collection.equals(_collection);
    }

    /**
     * Right pane class.
     **/
    private final class RightPane extends JTabbedPane {

        RightPane() {
            Messages messages = AppPrefs.getInstance().getMessages();
            add(messages.getString("lblCollection"), new SavePane(null, _collection, CollectionNode.this));
            add(messages.getString("lblVarieties"), new JLabel(""));
            add(messages.getString("lblData"), new JLabel(""));
            add(messages.getString("lblViews"), new JLabel(""));
            add(messages.getString("lblAnnotate"), new JLabel(""));
            add(messages.getString("lblTabulate"), new JLabel(""));
            add(messages.getString("lblRefine"), new JLabel(""));

            // use a change listener for lazy loading
            addChangeListener(new ChangeListener() {
                    public synchronized void stateChanged(ChangeEvent evt) {
                    	if (_cancelStateChanged) {
                    		_cancelStateChanged = false;
                    		return;
                    	}
                    	
                        int index = getSelectedIndex();
                        
                        // check validation
                        DatabasePane dbPane = MainFrame.getInstance().getDatabasePane();
                        if (!dbPane.validateCurrentEditObject()) {
                        	_cancelStateChanged = true;
                        	setSelectedIndex(_lastIndex); 
                        	return;                       
                        }
                        _lastIndex = index;
                        
                        // save current pane id
                        AppPrefs.getInstance().setIntProperty(AppPrefs.LAST_PANE, index);
                        
                        // save edit object before moving to another pane
                        dbPane.saveCurrentEditObject();
                        
                        // set current edit object if collection pane
                        if (index == 0)
                            dbPane.setCurrentEditObject(_collection,
                            	((SavePane)getComponentAt(0)).getPropertyPane().getInfo());

                        // sanity check
                        if (index == 0 || index >= _componentArray.length) {
                            return;
                        }

                        if (_componentArray[index] == null) {
                            Dialogs.showWaitCursor(MainFrame.getInstance());
                            switch (index) {
                                case 1:
                                    _componentArray[index] = new VarietiesPane(_collection);
                                    break;
                                case 2:
                                    _componentArray[index] = new DataPane(_collection);
                                    break;
                                case 3:
                                    _componentArray[index] = new ViewsPane(_collection);
                                    break;
                                case 4:
                                    _componentArray[index] = new AnnotatePane(_collection);
                                    break;
                                case 5:
                                    _componentArray[index] = new TabulatePane(_collection);
                                    break;
                                case 6:
                                    _componentArray[index] = new RefinePane(_collection);
                                    break;
                                default:
                                    _componentArray[index] = new JLabel("");
                                    break;
                            }
                            Dialogs.showDefaultCursor(MainFrame.getInstance());
                        }
                        setComponentAt(index, _componentArray[index]);
                        if (_componentArray[index] instanceof Refreshable) {
                            Dialogs.showWaitCursor(MainFrame.getInstance());
                            try {
                                // do not automatically refresh tabulate but initialize
                                if (_componentArray[index] instanceof Initializable) {
                                    ((Initializable) _componentArray[index]).init();
                                } else {
                                    ((Refreshable) _componentArray[index]).refresh();
                                }
                            } catch (DatabaseException ignored) {
                            } finally {
                                Dialogs.showDefaultCursor(MainFrame.getInstance());
                            }
                        }
                    }
                });
        }

        private void refresh() {
            for (int i = 0; i < _componentArray.length; i++) {
                if (_componentArray[i] != null && _componentArray[i] instanceof Refreshable) {
                    Dialogs.showWaitCursor(MainFrame.getInstance());
                    try {
                        ((Refreshable) _componentArray[i]).refresh();
                    } catch (DatabaseException ignored) {
                    } finally {
                        Dialogs.showDefaultCursor(MainFrame.getInstance());
                    }
                }
            }
        }

        private Component[] _componentArray = new Component[7];
        private int _lastIndex = -1;
        private boolean _cancelStateChanged = false;
    }

    private final WordCollection _collection;
    private final DefaultTreeModel _model;
    private RightPane _rightPane;
}
