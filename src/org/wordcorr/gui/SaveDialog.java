package org.wordcorr.gui;

import org.wordcorr.db.Persistent;
import org.wordcorr.gui.action.RevertPersistent;
import org.wordcorr.gui.action.SavePersistent;
import java.awt.event.ActionEvent;
import javax.swing.Action;

/**
 * Save/close dialog box.
 * @author Keith Hamasaki
 **/
public class SaveDialog extends AddDialog {

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     **/
    public SaveDialog(String titleKey, Persistent persistent) {
        super(titleKey, persistent);
    }

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh on save
     **/
    public SaveDialog(String titleKey, Persistent persistent, Refreshable refresh) {
        super(titleKey, persistent, refresh);
    }

    /**
     * Get the OK action for this dialog.
     **/
    protected Action getOKAction() {
        return new SavePersistent("btnSave", "accSave", getPropertyPane().getInfo(), getPersistent(), getRefreshable()) {
                protected void performAction(ActionEvent evt) throws Exception {
                    super.performAction(evt);
                }
            };
    }

    /**
     * Get the cancel action for this dialog.
     **/
    protected Action getCancelAction() {
        return new RevertPersistent("btnClose", "accClose", getPropertyPane().getInfo(), getPersistent(), getRefreshable()) {
                protected void performAction(ActionEvent evt) throws Exception {
                    if (!getPersistent().isDirty() || Dialogs.confirm(AppPrefs.getInstance().getMessages().getString("msgConfirmClose"))) {
                        super.performAction(evt);
                        dispose();
                    }
                }
            };
    }
}
