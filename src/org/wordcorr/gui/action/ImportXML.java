package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.wordcorr.db.User;
import org.wordcorr.db.XMLFile;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to import xml file.
 * @author Jim Shiba
 **/
public final class ImportXML extends GenericAction {

    private static final Action _instance = new ImportXML();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private ImportXML() {
        super("mnuImportXML", "accImportXML");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Import16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscImportXML"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Import file.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        User user = mf.getDatabasePane().getCurrentUser();
        if (user == null) {
            return;
        }

        JFileChooser fc =
            new JFileChooser(AppPrefs.getInstance().getProperty(AppPrefs.LAST_DIR, System.getProperty("user.home")));
        int ret = fc.showOpenDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = fc.getSelectedFile().getPath();
            AppPrefs.getInstance().setProperty(AppPrefs.LAST_DIR, fc.getSelectedFile().getAbsolutePath());
            final XMLFile file = new XMLFile();
            file.setToExport(false);
            file.setUser(user);
            file.setFilename(filename);
            Runnable task = new Runnable() {
                public void run() {
                    file.run();
                }
            };
            Messages m = AppPrefs.getInstance().getMessages();
            Dialogs.indeterminateProgressDialog(task,m.getString("pgbWaitString"), m.getString("pgbCurrentTask") + m.getString("mnuImportXML"));
            // set as current if not deleted due to error.
            if (file.getCollection() != null) {
                mf.getDatabase().getCurrentSetting().setCollectionID(file.getCollection().getID());
                mf.getDatabasePane().refresh();
            }
        }
    }
}
