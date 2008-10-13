package org.wordcorr.gui.action;

import javax.swing.*;

/**
 * Base class for actions with an icon.
 * @author Keith Hamasaki
 **/
public abstract class IconAction extends AbstractAction {
    public IconAction(String iconname) {
        putValue(SMALL_ICON, new ImageIcon(getClass().getResource(iconname)));
    }
}
