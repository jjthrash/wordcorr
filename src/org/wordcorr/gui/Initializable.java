package org.wordcorr.gui;

import org.wordcorr.db.DatabaseException;

/**
 * Interface for initializable objects.
 * @author Jim Shiba
 **/
public interface Initializable {
    void init() throws DatabaseException;
}
