package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.wordcorr.gui.MainFrame;

/**
 * Action to close the current database.
 * @author Keith Hamasaki
 **/
public final class Close extends WordCorrAction {

    private static final Action _instance = new Close();

    public static Action getInstance() {
        return _instance;
    }

    private Close() {
        super("mnuCloseDB", "accCloseDB");
        MainFrame.getInstance().addDisableOnClose(this);
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            MainFrame.getInstance().setDatabase(null);
        } catch (Exception ignored) { } // setting to null is safe
    }
}

