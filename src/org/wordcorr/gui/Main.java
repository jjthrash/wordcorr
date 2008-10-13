package org.wordcorr.gui;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 * Main class file.
 * @author Keith Hamasaki
 **/
public final class Main {

    /**
     * Main method. Create the panel and show it.
     **/
    public static void main(String[] args) {
        Splash splash = new Splash(new ImageIcon(Main.class.getResource("/splash.gif")));
                
        // load skin LF
        try {
            SkinLookAndFeel.setSkin(SkinLookAndFeel.loadDefaultThemePack());
            UIManager.setLookAndFeel(new SkinLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // startup the program
        org.wordcorr.BeanCatalog.getInstance();
        MainFrame.getInstance().init();
        MainFrame.getInstance().setVisible(true);
        splash.delayedDispose(1);
    }
}