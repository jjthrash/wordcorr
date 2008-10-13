/*
 * AliasTextRow.java
 *
 * Created on December 1, 2005, 8:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wordcorr.gui.input;

import javax.swing.JTextField;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Persistent;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.FontCache;
import org.wordcorr.gui.Refreshable;

/**
 *
 * @author Nathan
 */

//necessary to make original view name uneditable yet still have consistency
//in a localized version.
public class AliasTextRow extends InputRow {
    DocumentChangeListener listener;
    public AliasTextRow(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
        super(prop, obj);
        _text.addKeyListener(new IPAKeyListener(_text));
        _text.setDocument(new LimitDocument(prop.getMaxLength(), prop.getType()));
        listener=new DocumentChangeListener(obj, refresh, prop.getID());
        _text.getDocument().addDocumentListener(listener);
        _text.setFont(FontCache.getFont(prop.getFontKey()));
        
        if (prop.getDefault() != null && ((obj instanceof Persistent && ((Persistent) obj).isNew())
        || !(obj instanceof Persistent))) {
            setValue(prop.getDefault());
        }
        init(_text, _text);
    }
    
    public final Object getValue() { 
        if (alias == null) {
            return _text.getText();
        } else {
            return value;
        }
    }
    public final void setValue(Object val) {
        value=val;
        if (alias == null) {
            _text.setText(String.valueOf(val));
        } else {
            if (String.valueOf(val).equals(alias)) {
                _text.getDocument().removeDocumentListener(listener);
                _text.setText(alias);
                _text.setEditable(false);
            } else {
                _text.setText(String.valueOf(val));
                _text.setEditable(true);
                _text.getDocument().addDocumentListener(listener);
            }
        }
    }
    
    /** whenever setValue is given the argument of equal to triggerValue,
     * alias will be displayed instead.  Additionally, the textRow will
     * be set to non editable when the triggerValue is encountered;
     * giving a null alias will turn aliasing off.
     * Aliasing will not affect the value returned by getValue;
     **/
    public void setAliasing(String triggerValue, String alias) {
        if (this.alias != null && alias == null) {
            _text.setText(String.valueOf(value));
        }
        
        this.triggerValue = triggerValue;
        this.alias = alias;
        
        if (alias != null) {
            if (String.valueOf(value).equals(triggerValue)) {
                _text.getDocument().removeDocumentListener(listener);
                _text.setText(alias);
                _text.setEditable(false);
            }
        }
    }
    
    private JTextField _text = new JTextField();
    private Object value=null;
    private String triggerValue=null;
    private String alias = null;
}
