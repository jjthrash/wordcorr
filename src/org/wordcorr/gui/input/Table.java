package org.wordcorr.gui.input;

import java.awt.*;
import javax.swing.*;

/**
 * A component that lays out its elements in a table. It uses a
 * GridBagLayout to layout elements. Extra space is given to the
 * rightmost and lowest components.
 *
 * @author Keith Hamasaki
 **/
public class Table extends JPanel {

    /**
     * Constructor.
     **/
    public Table() {
        setLayout(_grid);

        // Blank fill label to align rows to the top of the panel.
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx   = 0;
        constraints.gridy   = 99;
        constraints.insets  = new Insets(10, 0, 0, 0);
        constraints.weighty = 1.0;
        constraints.fill    = GridBagConstraints.VERTICAL;
        add(new JLabel(), constraints);

        _rowConstraints.gridx = GridBagConstraints.RELATIVE;
        _rowConstraints.fill = GridBagConstraints.BOTH;
        _rowConstraints.insets = new Insets(2, 2, 2, 2);
        _rowConstraints.anchor = GridBagConstraints.NORTHWEST;
    }

    /**
     * Add a row to this table.
     * @param components The components to add.
     **/
    public void addRow(Component[] components) {
        addRow(components, 0.0);
    }

    /**
     * Add a row to this table.
     * @param components The components to add.
     **/
    public void addRow(Component[] components, double rowweight) {
        int weight = 0;
        _rowConstraints.gridy = _currow;
        _rowConstraints.weighty = rowweight;
        for (int i = 0; i < components.length; i++) {
            _rowConstraints.weightx = weight++;
            _grid.setConstraints(components[i], _rowConstraints);
            add(components[i]);
        }
        _currow++;
    }

    /**
     * Add a row to this table.
     * @param row The row to add.
     **/
    public void addRow(Row row) {
        addRow(row.getComponents());
    }

    private int _currow = 0;
    private GridBagLayout _grid = new GridBagLayout();
    private GridBagConstraints _rowConstraints = new GridBagConstraints();
}
