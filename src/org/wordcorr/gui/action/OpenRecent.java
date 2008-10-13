package org.wordcorr.gui.action;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import org.wordcorr.db.*;
import org.wordcorr.gui.MainFrame;

/**
 * Action to open a recent database.
 * @author Keith Hamasaki
 **/
public final class OpenRecent extends WordCorrAction {

    private static final int FNAME_THRESHOLD = 30;

    /**
     * Constructor.
     * @param index The index of this item
     * @param item The item to open
     **/
    public OpenRecent(int index, String item) {
        putValue(NAME, index + " " + crop(item));
        putValue(MNEMONIC_KEY, new Integer(index + '0'));
        _item = item;
    }

    /**
     * Shorten a long filename
     **/
    private String crop(String str) {
        if (str.length() <= FNAME_THRESHOLD) {
            return str;
        }

        int lastSlash = str.lastIndexOf('/');
        if (lastSlash == -1) {
            return str;
        }

        String end = str.substring(lastSlash);

        String start = str.substring(0, lastSlash);
        int lastSlash2 = start.lastIndexOf('/');
        while (lastSlash2 != -1 && (start + end).length() + 4 > FNAME_THRESHOLD) {
            start = start.substring(0, lastSlash2);
            lastSlash2 = start.lastIndexOf('/');
        }
        return start + "/..." + end;
    }

    /**
     * Open the database.
     **/
    public void actionPerformed(ActionEvent evt) {
        MainFrame.getInstance().openDatabase(new File(_item));
    }

    private final String _item;
}
