package org.wordcorr.gui.input;

import java.awt.Component;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.swing.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.FontCache;
import org.wordcorr.gui.Messages;
import org.wordcorr.gui.Refreshable;

/**
 * A row in a Table that has a label and a control.
 * @author Keith Hamasaki, Jim Shiba
 **/
public abstract class InputRow implements Row {

    public static final String LABEL_TYPE = "label";
    public static final String TEXTBOX_TYPE = "textbox";
    public static final String PASSWORD_TYPE = "password";
    public static final String TEXTAREA_TYPE = "textarea";
    public static final String SELECT_TYPE = "select";
    public static final String CHECKBOX_TYPE = "checkbox";
    public static final String CUSTOM_TYPE = "custom";

    private static final Class[] CONS_ARGS = {
        BeanCatalog.Property.class, Object.class, Refreshable.class
    };

    /**
     * Constructor.
     * @param name The property name
     * @param beanClass The bean class for this property. This class
     * is used to construct the message keys
     **/
    public InputRow(BeanCatalog.Property prop, Object obj) {
        _prop = prop;
        _obj = obj;
    }

    /**
     * Get an instance of input row based on the type of the given
     * property.
     **/
    public static InputRow getInstance(BeanCatalog.Property prop,
        Object obj, Refreshable refresh)
    {
        if (prop.getEditorType().equals(TEXTBOX_TYPE)) {
            return new TextRow(prop, obj, refresh);
        }
        if (prop.getEditorType().equals(TEXTAREA_TYPE)) {
            return new TextAreaRow(prop, obj, refresh);
        }
        if (prop.getEditorType().equals(PASSWORD_TYPE)) {
            return new PasswordRow(prop, obj, refresh);
        }
        if (prop.getEditorType().equals(SELECT_TYPE)) {
            return new SelectRow(prop, obj, refresh);
        }
//         if (prop.getEditorType().equals(CHECKBOX_TYPE)) {
//             return new CheckRow(prop, obj, refresh);
//         }
        if (prop.getEditorType().equals(LABEL_TYPE)) {
            return new LabelRow(prop, obj, refresh);
        }
        if (prop.getEditorType().equals(CUSTOM_TYPE)) {
            try {
                Class cl = Class.forName(prop.getEditorClass());
                Constructor cons = cl.getConstructor(CONS_ARGS);
                return (InputRow) cons.newInstance(new Object[] { prop, obj, refresh });
            } catch (Throwable e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Illegal custom editor class: " + prop.getEditorClass() + "; The class must extend InputRow and have a constructor that accepts a BeanCatalog.Property, an Object, and a Refreshable");
            }
        }
        throw new IllegalArgumentException("Unknown editor type: " + prop.getEditorType());
    }

    /**
     * Initialize with a component.
     * @param c The component to add to the table.
     * @param l The component to associate the label with.
     **/
    final protected void init(Component c, Component l) {
        if (_components != null) {
            throw new IllegalStateException("already initialized");
        }
        Messages messages = AppPrefs.getInstance().getMessages();
        JLabel label = new JLabel(getLabel());
        label.setVerticalAlignment(SwingConstants.TOP);
        if (_prop.getMnemonicKey() != null) {
            label.setDisplayedMnemonic(messages.getChar(_prop.getMnemonicKey()));
            label.setLabelFor(l);
        }

        _components = new Component[] { label, c };
    }

    /**
     * Get the components of this row.
     **/
    final public Component[] getComponents() {
        if (_components == null) {
            throw new IllegalStateException("not initialized");
        }
        return _components;
    }

    /**
     * Get the property name for this row.
     **/
    public final String getName() {
        return _prop.getID();
    }

    /**
     * Get the label for this row.
     **/
    public final String getLabel() {
        String label = AppPrefs.getInstance().getMessages().getString(_prop.getLabelKey());
        if (_prop.isRequired())
                label += " *";
        return label;
    }

    /**
     * Validate this field.
     **/
    public boolean validate() {
        Messages messages = AppPrefs.getInstance().getMessages();
        if (_prop.isRequired() && (getValue() == null ||
                String.valueOf(getValue()).trim().equals("")))
        {
        	if (_prop.getRequiredMessageKey() == null) {
	            Dialogs.error(messages.getCompoundMessage("cmpRequiredField", messages.getString(_prop.getLabelKey())));
        	} else {
	            Dialogs.error(messages.getString(_prop.getRequiredMessageKey()));
        	}
            return false;
        }
        if (_prop.isNospace() &&
        	(String.valueOf(getValue()).startsWith(" ") || String.valueOf(getValue()).endsWith(" ")))
        {
            Dialogs.error(messages.getCompoundMessage("cmpNospace", messages.getString(_prop.getLabelKey())));
            return false;
        }
        if (_prop.getType() == Type.INTEGER || _prop.getType() == Type.FLOAT) {
            if (String.valueOf(getValue()).trim().equals("")) {
                return true;
            }
            double d = Double.parseDouble(String.valueOf(getValue()));
            if (d < _prop.getMinValue() || d > _prop.getMaxValue()) {
                Dialogs.error(messages.getCompoundMessage("cmpPropRange", new Object[] { messages.getString(_prop.getLabelKey()), messages.getFormattedNumber(_prop.getMinValue()), messages.getFormattedNumber(_prop.getMaxValue()) }));
                return false;
            }
        }
        return true;
    }

    /**
     * Clear the dirty flag for the underlying persistent object.
     **/
    public void clearDirty() {
        if(Persistent.class.isInstance(_obj)) {
                Persistent persistent = (Persistent)_obj;
                persistent.clearDirty();
        }
    }

    /**
     * Get the weight of this row in the table. Default implementation
     * returns 0.
     **/
    public double getRowWeight() {
        return 0.0;
    }

    /**
     * Get the value of this row.
     **/
    public abstract Object getValue();

    /**
     * Set the value of this row.
     **/
    public abstract void setValue(Object val);

    private Component[] _components;
    private final BeanCatalog.Property _prop;
    private final Object _obj;

    //----------------------------------------------------------------------//
    // Inner classes
    //----------------------------------------------------------------------//

    /**
     * Label Row
     **/
    private static class LabelRow extends InputRow {
        LabelRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
            super(prop, obj);
            
            // label set if content exists
            if (prop.getContent() != null)
            	_label.setText(
            		AppPrefs.getInstance().getMessages().getString(prop.getContent()));
            init(_label, _label);
            _label.setFont(FontCache.getFont(prop.getFontKey()));
        }

        public final Object getValue() { return _label.getText(); }
        public final void setValue(Object val) { _label.setText(String.valueOf(val)); }

        private final JLabel _label = new JLabel("");
    }

    /**
     * TextField
     **/
    private static class TextRow extends InputRow {
        TextRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
            super(prop, obj);
            _text.addKeyListener(new IPAKeyListener(_text));
            _text.setDocument(new LimitDocument(prop.getMaxLength(), prop.getType()));
            _text.getDocument().addDocumentListener(new DocumentChangeListener(obj, refresh, prop.getID()));
            _text.setFont(FontCache.getFont(prop.getFontKey()));

            if (prop.getDefault() != null && ((obj instanceof Persistent && ((Persistent) obj).isNew())
            	|| !(obj instanceof Persistent))) {
                setValue(prop.getDefault());
            }
            init(_text, _text);
        }

        public final Object getValue() { return _text.getText(); }
        public final void setValue(Object val) { _text.setText(String.valueOf(val)); }

        private JTextField _text = new JTextField();
    }

    /**
     * PasswordField
     **/
    private static class PasswordRow extends InputRow {
        PasswordRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
            super(prop, obj);
            _text.setDocument(new LimitDocument(prop.getMaxLength(), prop.getType()));
            _text.getDocument().addDocumentListener(new DocumentChangeListener(obj, refresh, prop.getID()));
            if (obj instanceof Persistent && ((Persistent) obj).isNew() && prop.getDefault() != null) {
                setValue(prop.getDefault());
            }
            init(_text, _text);
        }

        public final Object getValue() { return _text.getText(); }
        public final void setValue(Object val) { _text.setText(String.valueOf(val)); }

        private JTextField _text = new JPasswordField();
    }

    /**
     * TextArea
     **/
    private static class TextAreaRow extends InputRow {
        TextAreaRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
            super(prop, obj);
            _text.setLineWrap(true);
            _text.addKeyListener(new IPAKeyListener(_text));
            _text.getDocument().addDocumentListener(new DocumentChangeListener(obj, refresh, prop.getID()));
            _text.setFont(FontCache.getFont(prop.getFontKey())); //test

            if (obj instanceof Persistent && ((Persistent) obj).isNew() && prop.getDefault() != null) {
                setValue(prop.getDefault());
            }
            if (prop.getNumRows() != -1) {
                _text.setRows(prop.getNumRows());
            }
            init(new JScrollPane(_text), _text);
        }

            public double getRowWeight() {
                return _text.getRows() * 5.0;
            }


        public final void setRows(int rows) { _text.setRows(rows); }
        public final Object getValue() { return _text.getText(); }
        public final void setValue(Object val) { _text.setText(String.valueOf(val)); }

        private JTextArea _text = new JTextArea();
    }

    /**
     * Select box.
     **/
    private static class SelectRow extends InputRow {
        SelectRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
            super(prop, obj);
            List list = new ArrayList(prop.getOptions());
            _list = new JComboBox(list.toArray(new Option[0]));
            _list.setFont(FontCache.getFont(prop.getFontKey())); //test            
            init(_list, _list);
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                Option z = (Option) it.next();
                _map.put(z.getValue(), z);
            }
            _list.addActionListener(new DocumentChangeListener(obj, refresh, prop.getID()));
            if (prop.getDefault() != null) {
	            if ((obj instanceof Persistent && ((Persistent) obj).isNew()) ||
	            	!(obj instanceof Persistent)) 
		                setValue(prop.getDefault());
            } else {
                _list.setSelectedIndex(0);
        	}
        }

        public final Object getValue() {
            Object x = _list.getSelectedItem();
            return x instanceof String ? (String) x : ((Option) x).getValue();
        }

        public final void setValue(Object val) {
            _list.setSelectedItem(_map.containsKey(val) ? _map.get(val) : val);
        }

        private final JComboBox _list;
        private final Map _map = new HashMap();
    }

//     /**
//      * Boolean item.
//      **/
//     private static class CheckRow extends InputRow {
//         CheckRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
//             super(prop, obj);
//             init(_box, _box);
//         }

//         public final String getValue() { return _box.isSelected() ? "t" : ""; }
//         public final void setValue(String val) { _box.setSelected(val.length() != 0); }

//         private final JCheckBox _box = new JCheckBox("On");
//     }
}
