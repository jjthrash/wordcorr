package org.wordcorr.gui;

import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.InputTable;

/**
 * Pane that holds properties of an object.
 * @author Keith Hamasaki, Jim Shiba
 **/
class PropertyPane extends JPanel {

    PropertyPane(String labelKey, Object obj, Refreshable refresh) {
    	this(labelKey, obj, refresh, null);
    }

    PropertyPane(String labelKey, Object obj, Refreshable refresh, String beanId) {
        super(new BorderLayout());
        _obj = obj;

        setBorder(BorderFactory.createEmptyBorder(4, 4, 1, 1));
        BeanCatalog.Bean bean;
        if (beanId == null) {
        	bean = BeanCatalog.getInstance().getBean(obj.getClass());
        } else {
        	bean = BeanCatalog.getInstance().getBean(beanId);
        }
        for (Iterator it = bean.getProperties().iterator(); it.hasNext(); ) {
            BeanCatalog.Property prop = (BeanCatalog.Property) it.next();
            _info.addRow(InputRow.getInstance(prop, obj, refresh));
        }
        _info.setValues(obj);
        if (labelKey != null) {
            JLabel label = new JLabel("<html><h2>" + AppPrefs.getInstance().getMessages().getString(labelKey) + "</h2></html>");
            add(label, BorderLayout.NORTH);
        }
        add(_info, BorderLayout.CENTER);
    }

    InputTable getInfo() {
        return _info;
    }

    private final Object _obj;
    private final InputTable _info = new InputTable();
}
