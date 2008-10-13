package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.DatabasePane;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Check box menu item for showing/hiding the tree.
 * @author Keith Hamasaki
 **/
public final class ShowTree extends GenericAction {

    private static final ShowTree _instance = new ShowTree();

    public static ShowTree getInstance() {
        return _instance;
    }

    private ShowTree() {
        AppPrefs prefs = AppPrefs.getInstance();
        Messages messages = prefs.getMessages();
        putValue(MNEMONIC_KEY, new Integer(messages.getChar("accShowTree")));
        MainFrame.getInstance().addDisableOnClose(this);
    }

    public void performAction(ActionEvent evt) {
        DatabasePane pane = MainFrame.getInstance().getDatabasePane();
        if (isShown()) {
            AppPrefs.getInstance().setIntProperty(AppPrefs.HIDE_LOCATION, pane.getDividerLocation());
            pane.setDividerLocation(pane.getMinimumDividerLocation());
        } else {
            pane.setDividerLocation(AppPrefs.getInstance().getIntProperty(AppPrefs.HIDE_LOCATION, 150));
        }
    }

    /**
     * Get the shown state of this action.
     **/
    public boolean isShown() {
        int loc = AppPrefs.getInstance().getIntProperty(AppPrefs.DIVIDER_LOCATION, 150);
        DatabasePane pane = MainFrame.getInstance().getDatabasePane();
        return loc != pane.getMinimumDividerLocation();
    }

    /**
     * Set the label for this action based on its current state.
     **/
    public void setLabel() {
        Messages messages = AppPrefs.getInstance().getMessages();
        if (isShown()) {
            putValue(NAME, messages.getString("mnuHideTree"));
        } else {
            putValue(NAME, messages.getString("mnuShowTree"));
        }
    }
}

