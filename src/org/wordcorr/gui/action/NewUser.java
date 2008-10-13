package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.User;
import org.wordcorr.gui.AddDialog;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to create a new user.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class NewUser extends GenericAction {

    private static final Action _instance = new NewUser();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private NewUser() {
        super("mnuNewUser", "accNewUser");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/toolbarButtonGraphics/development/Application16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscNewUser"));
        MainFrame.getInstance().addDisableOnClose(this);
    }

    /**
     * Add the user.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        Messages messages = AppPrefs.getInstance().getMessages();
        User user = (User) mf.getDatabase().makeObject(User.class);
        AddDialog dialog = new AddDialog("lblAddUser", user);
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
            mf.getDatabasePane().addUser(user);
        }
    }
}

