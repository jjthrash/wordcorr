package org.wordcorr.gui.action;

import org.wordcorr.gui.AppPrefs;
import javax.swing.*;

/**
 * Base class of WordCorr actions and a factory for retrieving
 * actions.
 * @author Keith Hamasaki
 **/
public abstract class WordCorrAction extends AbstractAction {

    /**
     * Constructor.
     * @param tkey The resource bundle key for the menu title
     * @param mkey The resource bundle key for the menu mnemonic
     **/
    public WordCorrAction(String tkey, String mkey) {
        super(AppPrefs.getInstance().getMessages().getString(tkey));
        putValue(MNEMONIC_KEY, new Integer(AppPrefs.getInstance().getMessages().getChar(mkey)));
    }

    /**
     * Constructor.
     **/
    public WordCorrAction() { }
}
