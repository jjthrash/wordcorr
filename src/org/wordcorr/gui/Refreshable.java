package org.wordcorr.gui;

import org.wordcorr.db.DatabaseException;

/**
 * Interface for refreshable objects.
 * @author Keith Hamasaki
 **/
public interface Refreshable {
    void refresh() throws DatabaseException;
}
