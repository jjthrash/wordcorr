package org.wordcorr.gui.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.wordcorr.gui.Refreshable;

/**
 * Listens for changes to a text document or combo box and updates an
 * object.
 * @author Keith Hamasaki, Jim Shiba
 **/
class DocumentChangeListener implements DocumentListener, ActionListener {

    /**
     * Constructor.
     **/
    DocumentChangeListener(Object obj, Refreshable refresh, String prop) {
        _obj = obj;
        _refresh = refresh;

        try {
            BeanInfo info = Introspector.getBeanInfo(_obj.getClass(), Object.class);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (int i = 0; i < descriptors.length; i++) {
                PropertyDescriptor desc = descriptors[i];
                if (desc.getName().equals(prop)) {
                    _writeMethod = desc.getWriteMethod();
                    _writeClass = desc.getPropertyType();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertUpdate(DocumentEvent evt) {
        docUpdate(evt);
    }
    public void removeUpdate(DocumentEvent evt) {
        docUpdate(evt);
    }
    public void changedUpdate(DocumentEvent evt) {
        docUpdate(evt);
    }

    /**
     * Update the persistent object on a document event.
     **/
    private void docUpdate(DocumentEvent evt) {
        try {
            Document doc = evt.getDocument();
            String text = doc.getText(0, doc.getLength());
            updateObject(text);
        } catch (javax.swing.text.BadLocationException e) {
            // this should not happen
            e.printStackTrace();
        }
    }

    /**
     * Update the persistent object on a combo box event.
     **/
    public void actionPerformed(ActionEvent evt) {
        JComboBox combo = (JComboBox) evt.getSource();
        updateObject(((Option) combo.getSelectedItem()).getValue());
    }

    /**
     * Update the persistent object with the given text value.
     **/
    private void updateObject(String text) {
        if (_writeMethod != null) {
            try {
                if (_writeClass.equals(Integer.class) || _writeClass.equals(Integer.TYPE)) {
                    if (!text.equals("") && !text.equals("-")) {
                        _writeMethod.invoke(_obj, new Object[] { new Integer(Integer.parseInt(text))});
                    } else if (text.equals("")) {
	                    _writeMethod.invoke(_obj, new Object[] { null });
                    }
                } else if (_writeClass.equals(Float.class) || _writeClass.equals(Float.TYPE)) {
                    if (!text.equals("") && !text.equals("-") && !text.equals(".") && !text.equals("-.")) {
                        _writeMethod.invoke(_obj, new Object[] { new Float(Float.parseFloat(text))});
                    }
                } else {
                    _writeMethod.invoke(_obj, new Object[] { text });
                }
                _refresh.refresh();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private final Object _obj;
    private final Refreshable _refresh;
    private java.lang.reflect.Method _writeMethod;
    private Class _writeClass;
}