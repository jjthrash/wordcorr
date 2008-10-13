package org.wordcorr.gui.action;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import org.wordcorr.db.*;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to create a new local database.
 * @author Keith Hamasaki
 **/
public final class NewLocal extends GenericAction {

    private static final NewLocal _instance = new NewLocal();

    public static NewLocal getInstance() {
        return _instance;
    }

    private NewLocal() {
        super("mnuNewLocalDB", "accNewLocalDB");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    }

    /**
     * Ask the user for the filename, and create the new database.
     **/
    protected void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        AppPrefs prefs = AppPrefs.getInstance();
        JFileChooser fc = new JFileChooser(prefs.getProperty(AppPrefs.LAST_DIR, "."));
        int ret = fc.showSaveDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            createNewDatabase(file);
        }
    }

    /**
     * Create the new database.
     **/
    public void createNewDatabase(File file) throws DatabaseException {
        MainFrame mf = MainFrame.getInstance();
        AppPrefs prefs = AppPrefs.getInstance();
        Messages messages = prefs.getMessages();

        // first check for writability
        File parent = file.getParentFile();
        if (!parent.canWrite()) {
            Dialogs.error(messages.getCompoundMessage("cmpCannotCreateFile", parent.getAbsolutePath()));
            return;
        }

        // if the file name ends in .script, let's remove it
        if (file.getName().endsWith(".script")) {
            String fname = file.getAbsolutePath();
            file = new File(fname.substring(0, fname.indexOf(".script")));
        }

        // now check if a db already exists
        File scrFile = new File(file.getAbsolutePath() + ".script");
        File propFile = new File(file.getAbsolutePath() + ".properties");
        File dataFile = new File(file.getAbsolutePath() + ".data");
        if (scrFile.exists() || propFile.exists() || dataFile.exists()) {
            Dialogs.error(messages.getCompoundMessage("cmpCannotCreateFile_2", new Object[] { scrFile.getAbsolutePath(), propFile.getAbsolutePath(), dataFile.getAbsolutePath() }));
            return;
        }

        mf.setDatabase(DatabaseFactory.newLocalDatabase(file));
//        prefs.setProperty(AppPrefs.LAST_DIR, file.getParentFile().getAbsolutePath());
    }
}

