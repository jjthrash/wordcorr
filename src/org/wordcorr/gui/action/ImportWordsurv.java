package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.User;
import org.wordcorr.db.WordsurvFile;
import org.wordcorr.gui.Task;
import org.wordcorr.gui.TaskDialog;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to import Wordsurv file.
 * @author Jim Shiba
 **/
public final class ImportWordsurv extends GenericAction {

    private static final Action _instance = new ImportWordsurv();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private ImportWordsurv() {
        super("mnuImportWordsurv", "accImportWordsurv");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Import16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscImportWordsurv"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Import file.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        User user = mf.getDatabasePane().getCurrentUser();
        if (user == null) {
            return;
        }
        WordsurvFile file = new WordsurvFile();
        file.setUser(user);
        TaskDialog dialog = new TaskDialog("lblImportWordsurv", file);
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
        	// set as current if not deleted due to error.
        	if (file.getCollection() != null) {
	        	mf.getDatabase().getCurrentSetting().setCollectionID(file.getCollection().getID());
	        	mf.getDatabasePane().refresh();
        	}
        }
    }
}
