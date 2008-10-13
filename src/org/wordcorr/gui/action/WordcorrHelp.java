package org.wordcorr.gui.action;

import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import org.wordcorr.AppProperties;

/**
 * Action to show wordcorr help contents.
 * @author Jim Shiba
 **/
public final class WordcorrHelp extends WordCorrAction {

    private static final Action _instance = new WordcorrHelp();

    public static Action getInstance() {
        return _instance;
    }

    private WordcorrHelp() {
        super("mnuWordcorrHelp", "accWordcorrHelp");
    }

    /**
     * Show the wordcorr help box.
     **/
    public void actionPerformed(ActionEvent evt) {
        // open up browser for windows platform only
        String cmd =
            "rundll32.exe url.dll,FileProtocolHandler "
                + AppProperties.getProperty("WordcorrHelp");
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}