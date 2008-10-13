package org.wordcorr.gui.action;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.wordcorr.AppProperties;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.DatabasePane;
import org.wordcorr.gui.MainFrame;

/**
 * Class to perform clean up and storage of preferences on exit.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class Exit extends WordCorrAction implements WindowListener {

    private static final Exit _instance = new Exit();

    public static Exit getInstance() {
        return _instance;
    }

    private Exit() {
        super("mnuExit", "accExit");
    }

    public void actionPerformed(ActionEvent evt) {
        // save current edit object
        DatabasePane pane = MainFrame.getInstance().getDatabasePane();
        if (!pane.validateCurrentEditObject())
        	return;
        if (pane != null)
            pane.saveCurrentEditObject();

        // change tabulate pane to annotate pane
        AppPrefs prefs = AppPrefs.getInstance();
        if (prefs.getIntProperty("LAST_PANE", -1) == 
        	Integer.parseInt(AppProperties.getProperty("TabulatePaneIndex")))
        	prefs.setIntProperty("LAST_PANE",
        		Integer.parseInt(AppProperties.getProperty("AnnotatePaneIndex")));

        // save user prefs
        prefs.save();
        System.exit(0);
    }

    public void windowClosing(WindowEvent evt) {
        actionPerformed(null);
    }

    // Other window listener methods
    public void windowActivated(WindowEvent evt) { }
    public void windowClosed(WindowEvent evt) { }
    public void windowDeactivated(WindowEvent evt) { }
    public void windowDeiconified(WindowEvent evt) { }
    public void windowIconified(WindowEvent evt) { }
    public void windowOpened(WindowEvent evt) { }
}
