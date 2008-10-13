package org.wordcorr.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Setting;
import org.wordcorr.db.View;

/**
 * Panel for choosing a view. This panel loads the last selected view
 * by the current user, and stores the user's preference when choosing
 * a new view.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class ViewChooser extends JPanel {

    /**
     * Constructor.
     **/
    public ViewChooser(final WordCollection collection) throws DatabaseException {
    	this(collection, null);
    }

    /**
     * Constructor.
     * List excludeViews - list of view names to exclude from list.
     **/
    public ViewChooser(final WordCollection collection, List excludeViews) throws DatabaseException {
        super(new FlowLayout(FlowLayout.LEFT));
        _collection = collection;
        _excludeViews = excludeViews;
        BasicListModel model = new BasicListModel();
        _list.setModel(model);
        
        model.setData(getViews());
        this.add(new JLabel(AppPrefs.getInstance().getMessages().getString("lblChooseView")));
        this.add(_list);

        _list.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        _view = (View) _list.getSelectedItem();
                        if (_view != null) {
	                        Setting setting = collection.getDatabase().getCurrentSetting();
	                        setting.setViewID(_view.getID());
	                        setting.save();
                        }
                    } catch (DatabaseException e) {
                        Dialogs.genericError(e);
                    }

                    fireChangeEvent();
                }
            });
    }

    /**
     * Get views.
     **/
    private List getViews() throws DatabaseException {
        // filter out list
        List views = new ArrayList(_collection.getViews());
        if (_excludeViews != null) {
        	for (Iterator it = _excludeViews.iterator(); it.hasNext();) {
        		String exclude = (String)it.next();
        		for (Iterator itv = views.iterator(); itv.hasNext();) {
	        		View vw = (View)itv.next();
	        		if (vw.getName().equalsIgnoreCase(exclude)) {
// Note:  WordCollection.getViews() needs to retrieve collection's list for this to work,
//        not new list.  Currently will not work but ViewChooser no longer in use.	        			
	        			views.remove(vw);
	        			break;
	        		}
        		}
        	}
        }
        return views;
	}
	
    /**
     * Refresh list.
     **/
    public void refresh() throws DatabaseException {
    	((BasicListModel )_list.getModel()).setData(getViews());
    }

    /**
     * Initialize with the user's choice.
     **/
    public void selectDefault() throws DatabaseException {
        Setting setting = _collection.getDatabase().getCurrentSetting();
        boolean viewSelected = false;
        for (int i = 0; i < _list.getModel().getSize(); i++) {
            View view = (View) _list.getModel().getElementAt(i);
            if (setting.getViewID() == view.getID()) {
                _list.setSelectedItem(view);
                viewSelected = true;
                break;
            }
        }
        if (!viewSelected)
        	_list.setSelectedItem(null);
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
                ((ChangeListener) listeners[i+1]).stateChanged(_event);
            }
        }
    }

    /**
     * Get the currently selected view.
     **/
    public View getSelectedView() {
        return _view;
    }

    private View _view;
    private final WordCollection _collection;
    private final ChangeEvent _event = new ChangeEvent(this);
    private final List _excludeViews;
    private final JComboBox _list = new JComboBox();
}
