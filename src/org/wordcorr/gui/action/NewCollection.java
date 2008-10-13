package org.wordcorr.gui.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.wordcorr.db.User;
import org.wordcorr.db.WordCollection;
import org.wordcorr.gui.AddDialog;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to create a new collection.
 * @author Keith Hamasaki
 **/
public final class NewCollection extends GenericAction {

    private static final Action _instance = new NewCollection();

    public static Action getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     **/
    private NewCollection() {
        super("mnuNewCollection", "accNewCollection");
        Messages messages = AppPrefs.getInstance().getMessages();
        putValue(
            SMALL_ICON,
            new ImageIcon(
                getClass().getResource(
                    "/toolbarButtonGraphics/table/ColumnInsertBefore16.gif")));
        putValue(SHORT_DESCRIPTION, messages.getString("dscNewCollection"));
        MainFrame.getInstance().addUserOnly(this);
    }

    /**
     * Add the collection.
     **/
    public void performAction(ActionEvent evt) throws Exception {
        MainFrame mf = MainFrame.getInstance();
        Messages messages = AppPrefs.getInstance().getMessages();
        User user = mf.getDatabasePane().getCurrentUser();
        if (user == null) {
            return;
        }

        WordCollection collection = user.makeCollection();
        AddDialog dialog = new AddDialog("lblAddCollection", collection);
        dialog.setSize(
            540,
            AppPrefs.getInstance().getIntProperty(AppPrefs.HEIGHT, 480));
        dialog.setVisible(true);
        if (!dialog.isCancelled()) {
            mf.getDatabasePane().addCollection(collection);
            mf.getDatabasePane().refresh();
        }
    }
}