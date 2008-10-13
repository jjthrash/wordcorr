package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * A generic action that traps and reports errors.
 * @author Keith Hamasaki
 **/
public abstract class GenericAction extends WordCorrAction {

    /**
     * Constructor.
     * @param tkey The resource bundle key for the menu title
     * @param mkey The resource bundle key for the menu mnemonic
     **/
    protected GenericAction(String tkey, String mkey) {
        super(tkey, mkey);
    }

    /**
     * Constructor.
     **/
    protected GenericAction() { }

    /**
     * Add the user.
     **/
    public final void actionPerformed(ActionEvent evt) {
        Dialogs.showWaitCursor(MainFrame.getInstance());
        try {
            performAction(evt);
        } catch (Exception e) {
            e.printStackTrace();
            Dialogs.genericError(e);
        } finally {
            Dialogs.showDefaultCursor(MainFrame.getInstance());
        }
    }

    /**
     * Perform the wrapped action.
     **/
    protected abstract void performAction(ActionEvent evt) throws Exception;
}

