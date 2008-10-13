package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.beans.*;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.input.InputTable;

/**
 * Action to save a persistent object.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class SavePersistent extends GenericAction {

    /**
     * Constructor.
     * @param tkey The resource bundle key for the menu title
     * @param mkey The resource bundle key for the menu mnemonic
     * @param info The input table containing the values entered
     * @param persistent The persistent object to save
     * @param refresh A refreshable instance to refresh after saving
     **/
    public SavePersistent(String tkey, String mkey, InputTable info,
        Persistent persistent, Refreshable refresh)
    {
        super(tkey, mkey);
        _info = info;
        _persistent = persistent;
        _refresh = refresh;
    }

    /**
     * Save the object.
     **/
    protected void performAction(ActionEvent evt) throws Exception {
        if (!_info.validateFields()) {
        	return;
        }
        if (_persistent.checkValidation() != null) {
        	Dialogs.msgbox(_persistent.checkValidation());
        	return;
        }
		_persistent.save();
        if (_refresh != null) {
            _refresh.refresh();
        }
    }

    private final InputTable _info;
    private final Persistent _persistent;
    private final Refreshable _refresh;
}
