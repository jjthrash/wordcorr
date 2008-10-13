package org.wordcorr.gui;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.action.RevertPersistent;
import org.wordcorr.gui.action.SavePersistent;

/**
 * Dialog for adding a new persistent object.
 *
 * @author Keith Hamasaki
 **/
public class AddDialog extends GenericDialog {

    private static final Class[] CONS_ARGS = { String.class, Class.class };

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     **/
    public AddDialog(String titleKey, Persistent persistent) {
        this (titleKey, persistent, null);
    }

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh on save
     **/
    public AddDialog(String titleKey, Persistent persistent, Refreshable refresh) {
        this (titleKey, persistent, refresh, true);
    }

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh on save
     * @param doSave If true, save the object when the user clicks OK
     **/
    public AddDialog(String titleKey, Persistent persistent, Refreshable refresh,
        boolean doSave)
    {
        this (titleKey, persistent, refresh, doSave, null);
    }

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh on save
     * @param doSave If true, save the object when the user clicks OK
     * @param beanID Overrides of beanID to use with PropertyPane
     **/
    public AddDialog(String titleKey, Persistent persistent, Refreshable refresh,
        boolean doSave, String beanID)
    {
        super(true);
        if (refresh == null) {
            refresh = NullRefreshable.getInstance();
        }

        Messages messages = AppPrefs.getInstance().getMessages();
        setTitle(messages.getString(titleKey));

        _persistent = persistent;
        _refresh = refresh;
        if (beanID == null)
        	_propertyPane = new PropertyPane(null, (Object)persistent, refresh);
        else
        	_propertyPane = new PropertyPane(null, (Object)persistent, refresh, beanID);
        _doSave = doSave;
        init();
        setSize(540, 400);
        setLocationRelativeTo(MainFrame.getInstance());
    }

    /**
     * Get the main panel for this dialog box.
     **/
    protected Component getMainPanel() {
        return _propertyPane;
    }

    /**
     * Get the OK action for this dialog.
     **/
    protected Action getOKAction() {
        return new SavePersistent("btnOK", "accOK", _propertyPane.getInfo(), _persistent, _refresh) {
                protected void performAction(ActionEvent evt) throws Exception {
                    if (_doSave) {
				        if (!_propertyPane.getInfo().validateFields()) {
				            return;
				        }
				        String msg = _persistent.checkValidation();
				        if (msg != null) {
				        	Dialogs.msgbox(msg);
				        	return;
				        }
				        _persistent.save();
				        if (_refresh != null) {
				            _refresh.refresh();
				        }
                    } else {
                        if (!_propertyPane.getInfo().validateFields()) {
                            return;
                        }
                    }
                    setCancelled(false);
                    dispose();
                }
            };
    }

    /**
     * Get the cancel action for this dialog.
     **/
    protected Action getCancelAction() {
        return new RevertPersistent("btnCancel", "accCancel", getPropertyPane().getInfo(), getPersistent(), getRefreshable()) {
                protected void performAction(ActionEvent evt) throws Exception {
                    super.performAction(evt);
                    dispose();
                }
            };
    }

    protected Persistent getPersistent() { return _persistent; }
    protected Refreshable getRefreshable() { return _refresh; }
    protected PropertyPane getPropertyPane() { return _propertyPane; }

    private final Persistent _persistent;
    private final Refreshable _refresh;
    private final PropertyPane _propertyPane;
    private final boolean _doSave;
}
