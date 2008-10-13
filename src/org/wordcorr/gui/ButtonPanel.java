package org.wordcorr.gui;

import java.awt.*;
import javax.swing.*;

/**
 * Simple class to layout a main component with a row of buttons on the top.
 * @author Keith Hamasaki, Jim Shiba
 **/
class ButtonPanel extends JPanel {

    /**
     * Constructor.
     * @param main The main component for this panel
     **/
    ButtonPanel(Component main) {
        this(main, null);
    }

    /**
     * Constructor.
     * @param main The main component for this panel
     * @param btns The buttons for this panel
     **/
    ButtonPanel(Component main, AbstractButton[] btns) {
        super(new BorderLayout());
        _main = main;
        add(main, BorderLayout.CENTER);
        add(_btnPanel, BorderLayout.NORTH);
        if (btns != null) {
            addButtons(btns);
        }
    }

    void addButton(AbstractButton btn) {
        _btnPanel.add(btn);
    }

    void addButtons(AbstractButton[] btns) {
        for (int i = 0; i < btns.length; i++) _btnPanel.add(btns[i]);
    }

    void addSeparator() {
        _btnPanel.add(new JLabel(" "));
    }

    protected Component getMainComponent() {
        return _main;
    }
    
    private final Component _main;
    private final JPanel _btnPanel = new JPanel(new WrapFlowLayout(FlowLayout.LEFT));
}
