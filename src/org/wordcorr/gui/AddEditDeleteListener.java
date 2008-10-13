package org.wordcorr.gui;

import java.awt.event.ActionEvent;
import org.wordcorr.db.DatabaseException;

/**
 * Listener object for add/edit/delete events.
 * @author Keith Hamasaki
 **/
interface AddEditDeleteListener {

    void doAdd(ActionEvent evt) throws DatabaseException;
    void doCopy(ActionEvent evt) throws DatabaseException;
    void doDelete(ActionEvent evt) throws DatabaseException;
    void doMoveUp(ActionEvent evt) throws DatabaseException;
    void doMoveDown(ActionEvent evt) throws DatabaseException;
    void doValidate(ActionEvent evt) throws DatabaseException;
}
