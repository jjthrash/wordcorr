package org.wordcorr.gui;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.WordCollection;
import org.wordcorr.db.Variety;
import org.wordcorr.db.View;

/**
 * Pane that holds all of the speech varieties in the current
 * collection.
 * @author Keith Hamasaki, Jim Shiba
 **/
class VarietiesPane extends AddEditDeletePanel {

    VarietiesPane(WordCollection collection) {
        super(null, true);
        final Messages messages = AppPrefs.getInstance().getMessages();

        // change find label
        setFindButtonLabel(messages.getString("btnFindVarieties"));

        _collection = collection;
        getList().setModel(new BasicListModel());

        final MainFrame mf = MainFrame.getInstance();
        addAddEditDeleteListener(new AddEditDeleteListener() {
            public void doAdd(ActionEvent evt) throws DatabaseException {
                Variety variety = _collection.makeVariety();
                AddDialog dialog =
                    new AddDialog("lblAddVariety", variety, null, true, "VarietyAdd");
                dialog.setSize(
                    540,
                    AppPrefs.getInstance().getIntProperty(AppPrefs.HEIGHT, 480));
                dialog.setVisible(true);
                if (!dialog.isCancelled()) {
                    refresh();
                    getList().setSelectedValue(variety, true);
                }
            }

            public void doDelete(ActionEvent evt) throws DatabaseException {
                Variety variety = (Variety) getList().getSelectedValue();
                variety.delete();

                JSplitPane split = (JSplitPane) getMainComponent();
                int loc = split.getDividerLocation();
                split.setRightComponent(new JLabel(""));
                split.setDividerLocation(loc);
            }

            public void doCopy(ActionEvent evt) {}
            public void doValidate(ActionEvent evt) {}
            public void doMoveUp(ActionEvent evt) {}
            public void doMoveDown(ActionEvent evt) {}
        });
    }

    public void refreshExt() throws DatabaseException {
        ((BasicListModel) getList().getModel()).setData(
            _collection.getOriginalVarieties());
    }

    private final WordCollection _collection;
}