package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.wordcorr.db.User;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.XMLFile;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to export xml file.
 * @author Jim Shiba
 **/
public final class ExportXML extends GenericAction {

    private static final Action _instance = new ExportXML();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private ExportXML() {
        super("mnuExportXML", "accExportXML");

        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Export16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscExportXML"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Export file.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        User user = mf.getDatabasePane().getCurrentUser();
        WordCollection collection = mf.getDatabasePane().getCurrentCollection();
        if (user == null || collection == null)
            return;
        
        final JFileChooser fc =
                new JFileChooser(
                AppPrefs.getInstance().getProperty(
                AppPrefs.LAST_DIR,
                System.getProperty("user.home")));
        int ret = fc.showSaveDialog(mf);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = fc.getSelectedFile().getAbsolutePath();
            final XMLFile file = new XMLFile();
            file.setToExport(true);
            List viewList = collection.getViews();
            collection.setElementViews(viewList);
            file.setUser(user);
            file.setCollection(collection);
            file.setFilename(filename);
            AppPrefs.getInstance().setProperty(AppPrefs.LAST_DIR, file.getFilename());
            Runnable task = new Runnable() {
                public void run() {
                    file.run();
                    
                    // update collection export timestamp status
                    MainFrame.getInstance().updateStatus();
                }
            };
            Messages m = AppPrefs.getInstance().getMessages();
            Dialogs.indeterminateProgressDialog(task,m.getString("pgbWaitString"), m.getString("pgbCurrentTask") + m.getString("mnuExportXML"));
        }
    }
}