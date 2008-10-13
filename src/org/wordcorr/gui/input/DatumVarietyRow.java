package org.wordcorr.gui.input;

import javax.swing.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Datum;
import org.wordcorr.db.Variety;
import org.wordcorr.gui.BasicListModel;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Refreshable;

/**
 * Row for selecting a variety.
 * @author Keith Hamasaki
 **/
public class DatumVarietyRow extends InputRow {

    /**
     * Constructor.
     **/
    public DatumVarietyRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
        super(prop, obj);
        _datum = (Datum) obj;
        _refresh = refresh;
        init(_combo, _combo);
        refresh();
    }

    /**
     * Refresh this object.
     **/
    public void refresh() {
        try {
            ((BasicListModel) _combo.getModel()).setData(_datum.getEntry().getCollection().getOriginalVarieties());
        } catch (DatabaseException e) {
            Dialogs.genericError(e);
        }
    }

    /**
     * Set the value of this row.
     **/
    public void setValue(Object value) {
        refresh();
        _combo.setSelectedItem(value);
    }

    /**
     * Get the value of this row.
     **/
    public Object getValue() {
        return _combo.getSelectedItem();
    }

    private final JComboBox _combo = new JComboBox(new BasicListModel() {
            public void setSelectedItem(Object o) {
                super.setSelectedItem(o);
                _datum.setVariety((Variety) o);
                try {
                    _refresh.refresh();
                } catch (DatabaseException ignored) { }
            }
        });

    private final Refreshable _refresh;
    private final Datum _datum;
}
