package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * Splash Screen.
 * @author Keith Hamasaki
 **/
class Splash extends JWindow {

    /**
     * Constructor.
     * @param img The image to use.
     **/
    Splash(ImageIcon img) {
        // add the image
        JLabel lbl = new JLabel(img);
        getContentPane().add(lbl, BorderLayout.CENTER);
        setSize(lbl.getPreferredSize());

        // center on the screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension lblSize = lbl.getPreferredSize();
        setLocation((screen.width / 2) - (lblSize.width / 2),
            (screen.height / 2) - (lblSize.height / 2));

        // register a mouse listener to close on click
        addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    setVisible(false);
                    dispose();
                }
            });
            
        setVisible(true);
    }
    
    /**
     * disposes the splash screen after the specified number of seconds
     * @param wait The time to wait (in seconds)
     **/
    public void delayedDispose(final int wait) {
        // Make a thread to close after the wait time
        final Runnable closer = new Runnable() {
                public void run() {
                    setVisible(false);
                    dispose();
                }
            };

        Thread thread = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(wait * 1000);
                        SwingUtilities.invokeAndWait(closer);
                    } catch (Exception ignored) { }
                }
            };

        // now kick off the thread
        thread.start();      
    }
}