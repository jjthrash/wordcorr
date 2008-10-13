package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.DatabaseReplicator;
import org.wordcorr.db.User;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to replicate local database.
 * @author Jim Shiba
 **/
public final class Replicate extends GenericAction {

    private static final Action _instance = new Replicate();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private Replicate() {
        super("mnuReplicate", "accReplicate");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/development/WebComponent16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscReplicate"));
        MainFrame.getInstance().addDisableOnClose(this);
    }

    /**
     * Replicate local database.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        User user = mf.getDatabasePane().getCurrentUser();
        if (user == null) {
            return;
        }
        DatabaseReplicator replicator = new DatabaseReplicator();
        replicator.setUser(user);
        replicator.run();
    }
}