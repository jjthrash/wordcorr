package org.wordcorr.gui;

import javax.swing.*;

/**
 * JMenuItem subclass to fix the annoying ACCELERATOR_KEY bug in JDK
 * 1.3.
 * @author Keith Hamasaki
 **/
class WMenuItem extends JMenuItem {

    WMenuItem() { }
    WMenuItem(Action action) { super(action); }
    WMenuItem(Icon icon) { super(icon); }
    WMenuItem(String text) { super(text); }
    WMenuItem(String text, Icon icon) { super(text, icon); }
    WMenuItem(String text, int mnemonic) { super(text, mnemonic); }

    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        if (a != null) {
            KeyStroke stroke = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
            if (stroke != null) {
                setAccelerator(stroke);
            }
        }
    }
}
