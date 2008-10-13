package org.wordcorr.gui.action;

import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Messages;

/**
 * Action to show the about box.
 * @author Keith Hamasaki
 **/
public final class About extends WordCorrAction {

    private static final Action _instance = new About();

    public static Action getInstance() {
        return _instance;
    }

    private About() {
        super("mnuAbout", "accAbout");
    }

    /**
     * Show the about box.
     **/
    public void actionPerformed(ActionEvent evt) {
        _dialog.pack();
        _dialog.setLocationRelativeTo(MainFrame.getInstance());
        _dialog.setVisible(true);
    }

    private AboutDialog _dialog = new AboutDialog();

    /**
     * Dialog box.
     **/
    private final class AboutDialog extends JDialog {

        AboutDialog() {
            super(MainFrame.getInstance(), true);
            Messages messages = AppPrefs.getInstance().getMessages();
            setTitle(messages.getString("pgtAboutDialog"));
            setResizable(false);

            JLabel iconLabel = new JLabel(new ImageIcon(this.getClass().getResource("/logo.gif")));

            Box pane = new Box(BoxLayout.Y_AXIS);
            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 12));
            getContentPane().setBackground(Color.white);
            pane.add(iconLabel);
            pane.add(new JLabel(messages.getString("msgAboutRelease")));
            pane.add(pane.createVerticalStrut(12));
            pane.add(new JLabel(messages.getString("msgAboutCopyright")));
            pane.add(pane.createVerticalStrut(12));
            pane.add(new JLabel(messages.getString("msgAboutThirdParty1")));
            pane.add(new JLabel(messages.getString("msgAboutThirdParty2")));
            pane.add(new JLabel(messages.getString("msgAboutThirdParty3")));
            pane.add(new JLabel(messages.getString("msgAboutThirdParty4")));
            pane.add(pane.createVerticalStrut(12));

            Runtime jvm = Runtime.getRuntime();
            long free = jvm.freeMemory();
            long total = jvm.totalMemory();
            double dfree = (free / 1024.) / 1024.;
            double dtotal = (total / 1024.) / 1024.;

            pane.add(new JLabel(messages.getCompoundMessage("cmpAboutFreeMemory", messages.getFormattedNumber(dfree))));
            pane.add(new JLabel(messages.getCompoundMessage("cmpAboutTotalMemory", messages.getFormattedNumber(dtotal))));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.white);
            JButton okButton = new JButton(messages.getString("btnOK"));
            okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        setVisible(false);
                    }
                });
            getRootPane().setDefaultButton(okButton);
            buttonPanel.add(okButton);
            pane.add(buttonPanel);
            getContentPane().add(pane);

            addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent evt) {
                        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            setVisible(false);
                        }
                    }
                });
        }
    }
}
