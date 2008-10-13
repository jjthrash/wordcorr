package org.wordcorr.gui;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Datum;
import org.wordcorr.db.Entry;
import org.wordcorr.db.Setting;
import org.wordcorr.db.Variety;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.gui.input.SortableList;

/**
 * Pane for entering and editing data.
 * @author Keith Hamasaki, Jim Shiba
 **/
class DataPane extends AddEditDeletePanel {

    DataPane(WordCollection collection) {
        super(null, true, false, true);

        // change button labels
        setDeleteButtonLabel(
            AppPrefs.getInstance().getMessages().getString("btnDeleteData"));
        setFindButtonLabel(
            AppPrefs.getInstance().getMessages().getString("btnFindData"));

        List list = new ArrayList();
        getList().setModel(new BasicListModel(list));
        getList().setFont(FontCache.getFont(FontCache.PRIMARY_GLOSS));
        _sorter = new SortableList(getList(), list, (Refreshable) getList().getModel());
        _collection = collection;

        addAddEditDeleteListener(new AddEditDeleteListener() {
            public void doAdd(ActionEvent evt) throws DatabaseException {
                Entry entry = _collection.makeEntry();
                AddDialog dialog = new AddDialog("lblAddEntry", entry);
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    entry.setEntryNum(new Integer(getList().getModel().getSize() + 1));
                    entry.save();
                    refresh();
                    getList().setSelectedValue(entry, true);
                }
            }

            public void doDelete(ActionEvent evt) throws DatabaseException {
                Entry entry = (Entry) getList().getSelectedValue();
                
                // reorder entry numbers after deletion
                int entryNum = entry.getEntryNum().intValue();
                entry.delete();
                _collection.reorderEntries(entryNum, entryNum);
                
                JSplitPane split = (JSplitPane) getMainComponent();
                int loc = split.getDividerLocation();
                split.setRightComponent(new JLabel(""));
                split.setDividerLocation(loc);
            }

            public void doValidate(ActionEvent evt) throws DatabaseException {
                Entry entry = (Entry) getList().getSelectedValue();
		        Messages messages = AppPrefs.getInstance().getMessages();
		        
		        // get missing varieties
		        int missing = 0;
		        String missingVarieties = "";
		        boolean first = true;
		        List datums = entry.getData();
   		        Setting setting = _collection.getDatabase().getCurrentSetting();
   			    View view = _collection.getViewByID(setting.getViewID());
 		        searchVarieties : for (Iterator it = view.getMembers().iterator(); it.hasNext();) {
		        	Variety variety = (Variety)it.next();
		        	
		        	// search for variety
			        for (Iterator it2 = datums.iterator(); it2.hasNext();) {
			        	Datum datum = (Datum)it2.next();
			        	
			        	if (datum.isDeleted()) {
			        		// skip datum marked for deletion since list is not updated.
			        		continue;
			        	}
			        	if (variety.getID() == datum.getVariety().getID()) {
			        		// remove from list
			        		datums.remove(datum);
			        		continue searchVarieties;
			        	}
			        }
					if (first) {
						first = false;
						missingVarieties = variety.getShortName();
					} else {		        
			        	missingVarieties +=  ", " + variety.getShortName();
					}
					++missing;
 		        }
 		        
			    if (missingVarieties.equals("")) {
			    	// none missing
			        Dialogs.customMsgbox(messages.getString("pgtDataValidateEntryNONE"),
			        	messages.getString("pgtDataValidateEntry"));
			    } else {
			    	// generate title
		        	String title = messages.getString("pgtDataValidateEntry") 
		        		+ " - " + missing + " of " + view.getMembers().size();
		        		
			        // break up missing varieties into lines
			        int len = 50;
			        if (missingVarieties.length() <= len) {
			        	// one line
				        Dialogs.customMsgbox(missingVarieties, title);
			        } else {
			        	// multiple lines
			        	StringBuffer lines = new StringBuffer();
			        	while (missingVarieties.length() > len) {
			        		int end = missingVarieties.lastIndexOf(",", len);
			        		if (end != -1) {
				        		lines.append(missingVarieties.substring(0, end) + "\n");
			 		       		missingVarieties = missingVarieties.substring(end + 2);
			        		}
			        	}
			        	lines.append(missingVarieties);
			        	Dialogs.customMsgbox(lines.toString(), title);
			        }
			    }
            }

            public void doMoveUp(ActionEvent evt) {
                Dialogs.showWaitCursor(MainFrame.getInstance());
                try {
                    int index = getList().getSelectedIndex();
                    if (index <= 0)
                        return;
                    ListModel model = getList().getModel();
                    Entry e1 = (Entry) model.getElementAt(index);
                    Entry e2 = (Entry) model.getElementAt(index - 1);
                    e1.setEntryNum(new Integer(index));
                    e1.save();
                    e2.setEntryNum(new Integer(index + 1));
                    e2.save();
                    _sorter.moveUp();
                    refresh();
                } catch (DatabaseException e) {
                    Dialogs.genericError(e);
                } finally {
                    Dialogs.showDefaultCursor(MainFrame.getInstance());
                }
            }

            public void doMoveDown(ActionEvent evt) {
                Dialogs.showWaitCursor(MainFrame.getInstance());
                try {
                    int index = getList().getSelectedIndex();
                    if (index < 0 || index >= getList().getModel().getSize() - 1)
                        return;
                    ListModel model = getList().getModel();
                    Entry e1 = (Entry) model.getElementAt(index);
                    Entry e2 = (Entry) model.getElementAt(index + 1);
                    e1.setEntryNum(new Integer(index + 2));
                    e1.save();
                    e2.setEntryNum(new Integer(index + 1));
                    e2.save();
                    _sorter.moveDown();
                    refresh();
                } catch (DatabaseException e) {
                    Dialogs.genericError(e);
                } finally {
                    Dialogs.showDefaultCursor(MainFrame.getInstance());
                }
            }

            public void doCopy(ActionEvent evt) { }
        });

        getList()
            .getSelectionModel()
            .addListSelectionListener(new ListSelectionListener() {
            public synchronized void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()) {
                    return;
                }

                Entry entry = (Entry) getList().getSelectedValue();
                if (entry != null) {
                    try {
                        Setting setting = _collection.getDatabase().getCurrentSetting();
                        setting.setEntryID(entry.getID());
                        setting.save();
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void refreshExt() throws DatabaseException {
        ((BasicListModel) getList().getModel()).setData(_collection.getEntries());
        Setting setting = _collection.getDatabase().getCurrentSetting();
        for (int i = 0; i < getList().getModel().getSize(); i++) {
            Entry entry = (Entry) getList().getModel().getElementAt(i);
            if (entry.getID() == setting.getEntryID()) {
                getList().setSelectedValue(entry, true);
                break;
            }
        }
    }

    protected FindDialog createFindDialog() {
        return new FindDialog("GlossFindDialog");
    }
    
    private final WordCollection _collection;
    private final SortableList _sorter;
}
