package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.action.SavePersistent;
import org.wordcorr.gui.action.RevertPersistent;

/**
 * Pane that allows you to save a persistent object. The persistent
 * object must be registered in the beans.xml file.
 *
 * @author Keith Hamasaki, Jim Shiba
 **/
class SavePane extends JPanel {

    private static final Class[] CONS_ARGS = { String.class, Class.class };

    /**
     * Constructor.
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh PropertyPane and on save
     **/
    SavePane(String labelKey, Persistent persistent, Refreshable refresh) {
    	this(labelKey, persistent, refresh, false);
    }

    /**
     * Constructor.
     * @param persistent The persistent object to edit and save
     * @param refresh A Refreshable object to refresh PropertyPane and on save
     **/
    SavePane(String labelKey, Persistent persistent, Refreshable refresh, boolean showButtons) {
        super(new BorderLayout());
        _persistent = persistent;
        _refresh = refresh;
        _propertyPane = new PropertyPane(labelKey, (Object)persistent, refresh);
        add(_propertyPane, BorderLayout.CENTER);
        if (showButtons)
        	add(new ButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Initialize.
     * @param persistent The persistent object to edit and save
     * @param propertyRefresh A Refreshable object to refresh PropertyPane
     * @param refresh A Refreshable object to refresh on save
     **/
    private void init(String labelKey, Persistent persistent, Refreshable propertyRefresh, Refreshable refresh) {
    }

    private final class ButtonPanel extends JPanel {
        ButtonPanel() {
            super(new FlowLayout(FlowLayout.CENTER));
            final Messages messages = AppPrefs.getInstance().getMessages();
            JButton saveButton = new WButton(new SavePersistent("btnSave", "accSave", _propertyPane.getInfo(), _persistent, _refresh));
            this.add(saveButton);
// Remove Revert button            
//            JButton revertButton = new WButton(new RevertPersistent("btnRevert", "accRevert", _propertyPane.getInfo(), _persistent, _refresh));
//            this.add(revertButton);
        }
    }
    
    /**
     * Get Property Pane.
     **/
    public PropertyPane getPropertyPane() {
    	return _propertyPane;
    }

    private final Persistent _persistent;
    private final Refreshable _refresh;
    private final PropertyPane _propertyPane;
}
