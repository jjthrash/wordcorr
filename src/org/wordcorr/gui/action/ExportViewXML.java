package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import org.wordcorr.db.View;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.XMLFile;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to export xml file for current and original view.
 * @author Jim Shiba
 **/
public final class ExportViewXML extends GenericAction {

    private static final Action _instance = new ExportViewXML();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private ExportViewXML() {
        super("mnuExportViewXML", "accExportViewXML");

        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource("/toolbarButtonGraphics/general/Export16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscExportViewXML"));
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
        
	    	// get current view
	    	View originalView = collection.getOriginalView();
	        List viewList = collection.getViews();
	        long id = collection.getDatabase().getCurrentSetting().getViewID();
	        if (id == originalView.getID()) {
	        	viewList.clear();
	        	viewList.add(originalView);
	        } else if (id != -1) {
	            for (int i = 0; i < viewList.size(); i++) {
	                View vw = (View) viewList.get(i);
	                if (id == vw.getID()) {
	                	viewList.clear();
	                	viewList.add(collection.getOriginalView());
	                	viewList.add(vw);
	                    break;
	                }
	            }
	        }
		    collection.setElementViews(viewList);
            file.setUser(mf.getDatabasePane().getCurrentUser());
            file.setCollection(collection);
            file.setFilename(filename);
            Runnable task = new Runnable() {
                public void run() {
                    file.run();
                }
            };
            Messages m = AppPrefs.getInstance().getMessages();
            Dialogs.indeterminateProgressDialog(task,m.getString("pgbWaitString"), m.getString("pgbCurrentTask") + m.getString("mnuExportViewXML"));
        }
    }
}
