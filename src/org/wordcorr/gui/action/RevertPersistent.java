package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.beans.*;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.input.InputTable;

/**
 * Action to revert a persistent object.
 * @author Keith Hamasaki
 **/
public class RevertPersistent extends GenericAction {

    /**
     * Constructor.
     * @param tkey The resource bundle key for the menu title
     * @param mkey The resource bundle key for the menu mnemonic
     * @param info The input table to update with the new data
     * @param persistent The persistent object to revert
     * @param refresh A refreshable instance to refresh after reverting
     **/
    public RevertPersistent(String tkey, String mkey, InputTable info,
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
        _persistent.revert();
        _info.setValues(_persistent);
        if (_refresh != null) {
            _refresh.refresh();
        }
    }

    private final InputTable _info;
    private final Persistent _persistent;
    private final Refreshable _refresh;
}
