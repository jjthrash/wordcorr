package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
/**
 * Class with shortcuts for dialogs and such.
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class Dialogs {

    // don't make me
    private Dialogs() { }

    /**
     * Show a generic error dialog for an exception.
     **/
    public static void genericError(Throwable e) {
        e.printStackTrace();
        error(AppPrefs.getInstance().getMessages().getCompoundMessage("cmpGenericError", e.toString()));
    }

    /**
     * Show an error dialog.
     **/
    public static void error(String message) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), message, AppPrefs.getInstance().getMessages().getString("lblError"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show an message dialog.
     **/
    public static void msgbox(String message) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), message);
    }

    public static void indeterminateProgressDialog(final Runnable task, String title, String message) {
        indeterminateProgressDialogImpl(task, title,
                message, new JDialog(MainFrame.getInstance(),true));
    }
    
    private static void indeterminateProgressDialogImpl(final Runnable task, String title, String message, final JDialog dialog) {
        dialog.setTitle(title);
        dialog.add(new JLabel(message), BorderLayout.CENTER);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        dialog.add(bar, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        Thread t = new Thread(new Runnable() {
            public void run() {
                task.run();
                dialog.dispose();
            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        dialog.setVisible(true);
    }
    
    /**
     * Show a custom message dialog with no icon.
     * @param message Message
     * @param title Title
     **/
    public static void customMsgbox(String message, String title) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), message,
        	title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Ask the user to confirm something.
     **/
    public static boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(MainFrame.getInstance(), message, AppPrefs.getInstance().getMessages().getString("lblConfirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    /**
     * Ask the user to confirm something.
     * This will format anything between ipaLeftBoundary and ipaRightBoundary to use
     * the FontCache.getIPA() font.  For example, if ipaLeftBoundary is "(" and ipaRightBoundary is ")"
     * anything between the two will use IPA font.
     **/
    public static boolean confirm (String message, String ipaLeftBoundary, String ipaRightBoundary) {
        JPanel m = new JPanel();
        int ipaStart = message.lastIndexOf(ipaLeftBoundary);
        int ipaEnd = message.lastIndexOf(ipaRightBoundary);
        m.add(new JLabel(message.substring(0, ipaStart + 1)));
        JLabel ipa = new JLabel(message.substring(ipaStart+1, ipaEnd));
        ipa.setFont(FontCache.getIPA());
        m.add(ipa);
        m.add(new JLabel(message.substring(ipaEnd)));
        
        return (JOptionPane.showOptionDialog(MainFrame.getInstance(), m, AppPrefs.getInstance().getMessages().getString("lblConfirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) == JOptionPane.YES_OPTION;
    }

    /**
     * Ask the user for input.
     **/
    public static String prompt(String message) {
        return JOptionPane.showInputDialog(MainFrame.getInstance(), message);
    }

    /**
     * Ask the user for input, with a length limit.
     **/
    public static String prompt(String message, int maxlen) {
        String text = JOptionPane.showInputDialog(MainFrame.getInstance(), message);
        if (text != null && text.length() > maxlen) {
            Dialogs.error(AppPrefs.getInstance().getMessages().getCompoundMessage("cmpMaxLength", String.valueOf(maxlen)));
            return null;
        }
        return text;
    }

    /**
     * Show a wait cursor.
     **/
    public static void showWaitCursor(Component component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Show the default cursor.
     **/
    public static void showDefaultCursor(Component component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
