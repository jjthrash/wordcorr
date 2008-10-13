package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to refresh the screen.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class Refresh extends GenericAction {

    private static final Action _instance = new Refresh();

    public static Action getInstance() {
        return _instance;
    }

    private Refresh() {
        super("mnuRefresh", "accRefresh");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Refresh16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscRefresh"));
        MainFrame.getInstance().addDisableOnClose(this);
    }

    public void performAction(ActionEvent evt) throws Exception {
    	// refresh MainFrame
        MainFrame.getInstance().refresh();
    }
}

