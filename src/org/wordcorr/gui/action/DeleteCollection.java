package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.WordCollection;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to delete active collection.
 * @author Jim Shiba
 **/
public final class DeleteCollection extends GenericAction {

    private static final Action _instance = new DeleteCollection();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private DeleteCollection() {
        super("mnuDeleteCollection", "accDeleteCollection");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscDeleteCollection"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Delete collection.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        WordCollection collection = mf.getDatabasePane().getCurrentCollection();
        if (collection == null) {
            return;
        }
        // confirm deletion
        Messages messages = AppPrefs.getInstance().getMessages();
        if (Dialogs.confirm(messages.getCompoundMessage("msgConfirmDeleteCollection",
        	collection.getName()))) {
        	// delete collection
	        if (collection.getID() != -1) {
		    	try {
		    		collection.delete();
		    		mf.updateStatus();
		    		mf.refresh();
		    	} catch (DatabaseException e) {
		    		e.printStackTrace();
		    	}
	        }
        }
    }
}
