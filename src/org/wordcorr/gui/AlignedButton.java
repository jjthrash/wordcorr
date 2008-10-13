package org.wordcorr.gui;

import javax.swing.*;

/**
 * JButton subclass to align text.
 * @author Keith Hamasaki
 **/
public class AlignedButton extends WButton {

    /**
     * Constructor.
     * @param labelKey The message key for the label.
     * @param mnemonicKey The message key for the mnemonic.
     * @param align The alignment.
     **/
    public AlignedButton(String labelKey, String mnemonicKey, int align) {
        super(org.wordcorr.gui.AppPrefs.getInstance().getMessages().getString(labelKey));
        setMnemonic(org.wordcorr.gui.AppPrefs.getInstance().getMessages().getChar(mnemonicKey));
        setHorizontalAlignment(align);
    }
}
