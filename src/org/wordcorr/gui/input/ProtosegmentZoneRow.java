package org.wordcorr.gui.input;

import java.util.*;
import javax.swing.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Protosegment;
import org.wordcorr.db.Zone;
import org.wordcorr.gui.BasicListModel;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Refreshable;

/**
 * Row for selecting a zone.
 * @author Jim Shiba
 **/
public class ProtosegmentZoneRow extends InputRow {

    /**
     * Constructor.
     **/
    public ProtosegmentZoneRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
        super(prop, obj);
        _protosegment = (Protosegment) obj;
        _refresh = refresh;
        init(_combo, _combo);
        refresh();
    }

    /**
     * Refresh this object.
     **/
    public void refresh() {
        try {
            ((BasicListModel) _combo.getModel()).setData(_protosegment.getDatabase().getZones());
        } catch (Exception e) {
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
                _protosegment.setZone((Zone) o);
                try {
                    _refresh.refresh();
                } catch (DatabaseException ignored) { }
            }
        });

    private final Refreshable _refresh;
    private final Protosegment _protosegment;
}
