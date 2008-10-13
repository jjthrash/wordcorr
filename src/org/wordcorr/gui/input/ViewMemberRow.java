package org.wordcorr.gui.input;

import org.wordcorr.BeanCatalog;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.View;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Refreshable;
import java.util.List;
import javax.swing.event.*;

/**
 * Wraps a MultiChooser for a view's members in an input row.
 * @author Keith Hamasaki
 **/
public class ViewMemberRow extends InputRow {

    /**
     * Constructor.
     **/
    public ViewMemberRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
        super(prop, obj);
        _view = (View) obj;
        _refresh = refresh;
        init(_chooser, _chooser);
        refresh();

        _chooser.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    MultiChooser chooser = (MultiChooser) evt.getSource();
                    _view.setMembers(chooser.getSelectedValues());
		    try {
			_refresh.refresh();
		    } catch (DatabaseException e) {
			Dialogs.genericError(e);
		    }
                }
            });
    }

    /**
     * Refresh this object.
     **/
    public void refresh() {
        try {
            _chooser.reset(_view.getCollection().getVarieties());
        } catch (DatabaseException e) {
            Dialogs.genericError(e);
        }
    }

    /**
     * Set the value of this row.
     **/
    public void setValue(Object value) {
        refresh();
        _chooser.setSelectedValues((List) value);
    }

    /**
     * Get the value of this row.
     **/
    public Object getValue() {
        return null;
    }

    /**
     * Get the weight of this row in the table. Default implementation
     * returns 0.
     **/
    public double getRowWeight() {
        return 20.0;
    }

    private final MultiChooser _chooser = new MultiChooser();
    private final View _view;
    private final Refreshable _refresh;
}
