package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.XMLFile;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to export xml file for metadata.
 * @author Jim Shiba
 **/
public final class ExportMetadataXML extends GenericAction {

    private static final Action _instance = new ExportMetadataXML();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private ExportMetadataXML() {
        super("mnuExportMetadataXML", "accExportMetadataXML");

        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Export16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscExportMetadataXML"));
        MainFrame.getInstance().addUserOnly(this);
    }


    /**
     * Export file.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        WordCollection collection = mf.getDatabasePane().getCurrentCollection();
        if (collection == null) {
            return;
        }

        JFileChooser fc =
            new JFileChooser(AppPrefs.getInstance().getProperty(AppPrefs.LAST_DIR, System.getProperty("user.home")));
        int ret = fc.showSaveDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = fc.getSelectedFile().getPath();
            AppPrefs.getInstance().setProperty(AppPrefs.LAST_DIR, fc.getSelectedFile().getAbsolutePath());
            
            final XMLFile file = new XMLFile();
            file.setToExport(true);
            file.setToMetadataOnly(true);
            file.setUser(mf.getDatabasePane().getCurrentUser());
            file.setCollection(collection);
            file.setFilename(filename);
            Runnable task = new Runnable() {
                public void run() {
                    file.run();
                }
            };
            Messages m = AppPrefs.getInstance().getMessages();
            Dialogs.indeterminateProgressDialog(task,m.getString("pgbWaitString"), m.getString("pgbCurrentTask") + m.getString("mnuExportMetadataXML"));
        }
    }
}
