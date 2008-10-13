package org.wordcorr.gui.input;

/**
 * An Option represents a name/value pair for controls such as
 * listboxes and comboboxes.
 * @author Keith Hamasaki
 **/
public interface Option {

    /**
     * Get the value for this option.
     **/
    String getValue();

    /**
     * Get the text for this option.
     **/
    String toString();
}
