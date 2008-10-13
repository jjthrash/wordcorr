package org.wordcorr.gui.input;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A Swing text document that limits its input to a set number of
 * characters.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class LimitDocument extends PlainDocument {

    /**
     * Constructor.
     * @param size The max size of this text document.
     * @param refresh A refreshable object to refresh upon change.
     **/
    public LimitDocument(int size, Type type) {
        _size = size;
        _type = type;
    }

    /**
     * Override of insertString to check the size.
     **/
    public void insertString(int offset, String str, AttributeSet attrs)
        throws BadLocationException {
        if (_size != -1 && offset + str.length() > _size) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        if (_type == Type.INTEGER) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (!Character.isDigit(c) && (c == '-' && offset > 0)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (!str.equals("-") && !str.equals("")) {
                try {
                    long test = Long.parseLong(getText(0, getLength()) + str);
                    if (test > Integer.MAX_VALUE) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
        }

        if (_type == Type.FLOAT) {
            // check for existing decimal point
            int dotindex = getText(0, getLength()).indexOf(".");
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (!Character.isDigit(c)
                    && ((c == '.' && dotindex != -1) || (c == '-' && offset > 0))) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            if (!str.equals("-") && !str.equals(".")) {
                try {
                    double test = Double.parseDouble(getText(0, getLength()) + str);
                    if (test > Float.MAX_VALUE) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
        }

        super.insertString(offset, str, attrs);
    }

    private final int _size;
    private final Type _type;
}