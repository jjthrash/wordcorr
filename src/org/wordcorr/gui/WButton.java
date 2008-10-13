package org.wordcorr.gui;

import javax.swing.*;

/**
 * JButton subclass to fix the annoying MNEMONIC_KEY bug in JDK 1.3.
 * @author Keith Hamasaki
 **/
public class WButton extends JButton {

    public WButton() { }
    public WButton(Action action) { super(action); }
    public WButton(Icon icon) { super(icon); }
    public WButton(String text) { super(text); }
    public WButton(String text, Icon icon) { super(text, icon); }

    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        if (a != null) {
            Integer key = (Integer) a.getValue(Action.MNEMONIC_KEY);
            if (key != null) {
                setMnemonic((char) key.intValue());
            }
        }
    }
}
