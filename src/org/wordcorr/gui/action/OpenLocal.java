package org.wordcorr.gui.action;

import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.db.*;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;

/**
 * Action to open a local database.
 * @author Keith Hamasaki
 **/
public final class OpenLocal extends WordCorrAction {

    private static final Action _instance = new OpenLocal();

    public static Action getInstance() {
        return _instance;
    }

    private OpenLocal() {
        super("mnuOpenLocalDB", "accOpenLocalDB");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
        MainFrame mf = MainFrame.getInstance();
        JFileChooser fc = new DBFileChooser(AppPrefs.getInstance().getProperty(AppPrefs.LAST_DIR, "."));
        int ret = fc.showOpenDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            mf.openDatabase(fc.getSelectedFile());
        }
    }
}
