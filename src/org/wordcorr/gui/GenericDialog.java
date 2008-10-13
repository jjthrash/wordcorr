package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.gui.action.WordCorrAction;

/**
 * Generic dialog class. Has features such as dispose on window close
 * and dispose when hitting the escape key.  Also provides a button
 * panel with two buttons.
 * @author Keith Hamasaki, Jim Shiba
 **/
public abstract class GenericDialog extends JDialog {

    /**
     * Constructor.
     **/
    public GenericDialog(boolean modal) {
        super(MainFrame.getInstance(), modal);
    }

    /**
     * Initialize this dialog.
     **/
    protected void init() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(getMainPanel(), BorderLayout.CENTER);
        main.add(new ButtonPanel(), BorderLayout.SOUTH);
        setContentPane(main);
        pack();

        // add an escape key listener
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        getRootPane().getInputMap().put(ks, "CloseAction");
        getRootPane().getActionMap().put("CloseAction", new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                preDispose();
                dispose();
            }
        });

        // add a quit window listener
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                preDispose();
                dispose();
            }
        });
    }

    /**
     * Was this dialog box cancelled?
     **/
    public final boolean isCancelled() {
        return _cancelled;
    }

    /**
     * Set the cancelled flag.
     **/
    protected void setCancelled(boolean flag) {
        _cancelled = flag;
    }

    /**
     * The bottom button panel.
     **/
    private final class ButtonPanel extends JPanel {
        ButtonPanel() {
            super(new FlowLayout(FlowLayout.CENTER));
            JButton okButton = new WButton(getOKAction());
            GenericDialog.this.getRootPane().setDefaultButton(okButton);
            this.add(okButton);
            _cancelButton = new WButton(getCancelAction());
            this.add(_cancelButton);
        }
    }

    /**
     * Get cancel button.
     **/
    protected JButton getCancelButton() {
        return _cancelButton;
    }

    /**
     * Get the cancel action for this dialog. The default
     * implementation returns an action with a cancel label and
     * disposes of the dialog after calling preDispose().
     **/
    protected Action getCancelAction() {
        return new WordCorrAction("btnCancel", "accCancel") {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        };
    }

    /**
     * Get the OK action for this dialog. The default implementation
     * returns an action with an OK label and disposes of the dialog
     * after calling preDispose() and setting the cancelled flag to
     * false.
     **/
    protected Action getOKAction() {
        return new WordCorrAction("btnOK", "accOK") {
            public void actionPerformed(ActionEvent evt) {
                _cancelled = false;
                dispose();
            }
        };
    }

    /**
     * Get the main panel for this dialog box.
     **/
    protected abstract Component getMainPanel();

    /**
     * Perform any additional cleanup work before disposing this
     * dialog. The default implementation does nothing.
     **/
    protected void preDispose() {}

    private boolean _cancelled = true;
    private JButton _cancelButton;
}