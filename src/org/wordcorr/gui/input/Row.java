package org.wordcorr.gui.input;

import java.awt.Component;

/**
 * A Row can be added to a Table.
 * @author Keith Hamasaki
 **/
public interface Row {

    /**
     * Get the components for this row.
     **/
    Component[] getComponents();
}
