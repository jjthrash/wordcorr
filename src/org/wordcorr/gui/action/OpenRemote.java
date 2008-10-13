package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.wordcorr.gui.Dialogs;

/**
 * Action to open a remote database.
 * @author Keith Hamasaki
 **/
public final class OpenRemote extends WordCorrAction {

    private static final Action _instance = new OpenRemote();

    public static Action getInstance() {
        return _instance;
    }

    private OpenRemote() {
        super("mnuOpenRemoteDB", "accOpenRemoteDB");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
        Dialogs.msgbox("Not Implemented Yet");
    }
}

