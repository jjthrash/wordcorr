package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.User;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to delete active user.
 * @author Jim Shiba
 **/
public final class DeleteUser extends GenericAction {

    private static final Action _instance = new DeleteUser();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private DeleteUser() {
        super("mnuDeleteUser", "accDeleteUser");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscDeleteUser"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Delete user.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        User user = mf.getDatabasePane().getCurrentUser();
        if (user == null) {
            return;
        }
        // confirm deletion
        Messages messages = AppPrefs.getInstance().getMessages();
        if (Dialogs.confirm(messages.getCompoundMessage("msgConfirmDeleteUser",
            user.getName()))) {
            // delete user
            if (user.getID() != -1) {
                try {
                    user.delete();
                    mf.updateStatus();
                    mf.refresh();
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
