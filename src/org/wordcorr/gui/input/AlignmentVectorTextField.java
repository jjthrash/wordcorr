package org.wordcorr.gui.input;

import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.wordcorr.db.Alignment;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.Dialogs;
import org.wordcorr.gui.Messages;

/**
 * Alignment Vector Text Field.
 * @author Jim Shiba
 **/
public class AlignmentVectorTextField extends JTextField {
    public AlignmentVectorTextField() {
        setDocument(new AlignmentDocument(this));
        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent evt) {
                doUpdate();
            }
            public void removeUpdate(DocumentEvent evt) {
                doUpdate();
            }
            public void changedUpdate(DocumentEvent evt) {
                doUpdate();
            }

            private void doUpdate() {
                _alignment.setVector((String) getValue());
            }
        });
        addKeyListener(new CursorKeyListener(this));
    }

    public void setAlignment(Alignment alignment) {
        _alignment = alignment;
    }

    /**
     * Define Grapheme Cluster.
     **/
    public final void defineGraphemeCluster() {
        Messages messages = AppPrefs.getInstance().getMessages();
        String text = this.getSelectedText();
        if (text == null || text.length() < 2) {
            Dialogs.msgbox(messages.getString("msgEditAlignmentMinimumGraphemeCluster"));
            return;
        }

        // check for illegal characters
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == Alignment.INDEL_SYMBOL || ch == Alignment.EXCLUDE_SYMBOL) {
                Dialogs.msgbox(
                    messages.getCompoundMessage("msgEditAlignmentIllegalCharacters", text));
                this.select(0, 0);
                return;
            }
        }

        // check overlap
        if (_graphemeClusters
            .hasOverlap(this.getSelectionStart(), this.getSelectionEnd())) {
            Dialogs.msgbox(messages.getCompoundMessage("msgEditAlignmentOverlap", text));
        } else {
            // add
            _graphemeClusters.add(this.getSelectionStart(), this.getSelectionEnd());
            _alignment.setVector((String) getValue());

            // reset selection
            this.select(0, 0);
        }
    }

    /**
     * Uncluster Grapheme Cluster.
     **/
    public final void unclusterGraphemeCluster() {
        Messages messages = AppPrefs.getInstance().getMessages();

        // check selection
        GraphemeClusterPosition pos =
            _graphemeClusters.get(this.getSelectionStart(), this.getSelectionEnd());
        if (pos != null) {
            if (Dialogs
                .confirm(
                    messages.getCompoundMessage(
                        "msgEditAlignmentGraphemeClusterUncluster",
                        this.getText().substring(pos.getStart(), pos.getEnd())),"(",")")) {
                _graphemeClusters.delete(pos);
                _alignment.setVector((String) getValue());

                // reset selection
                this.select(0, 0);
            };
        } else {
            Dialogs.msgbox(
                messages.getString("msgEditAlignmentGraphemeClusterUnclusterSelection"));
        }
    }

    /**
     * Get the value of this text field.
     **/
    public final Object getValue() {
        String text = this.getText();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < text.length(); i++) {
            switch (text.charAt(i)) {
                case Alignment.INDEL_SYMBOL :
                case Alignment.EXCLUDE_SYMBOL :
                    buf.append(text.charAt(i));
                    break;
                default :
                    buf.append(Alignment.HOLD_SYMBOL);
                    break;
            }
        }

        // add grapheme cluster tags to vector
        return _graphemeClusters.addTags(buf.toString());
    }

    /**
     * Set the value of this text field.
     **/
    public final void setValue(Object val) {
        Document doc = this.getDocument();
        if (doc instanceof AlignmentVectorTextField.AlignmentDocument) {
            ((AlignmentVectorTextField.AlignmentDocument) doc).setCheck(false);
        }
        String vector = _graphemeClusters.set((String) val);
        this.setText(_alignment.getDatum().fuseWithAlignment(vector));
        if (doc instanceof AlignmentVectorTextField.AlignmentDocument) {
            ((AlignmentVectorTextField.AlignmentDocument) doc).setCheck(true);
        }
    }

    /**
     * KeyListener that controls cursor movement.
     **/
    private final class CursorKeyListener extends KeyAdapter {

        /**
         * Constructor.
         * @param text The text component that this is associated with.
         **/
        public CursorKeyListener(JTextComponent text) {
            _text = text;
        }

        /**
         * Key press event.
         **/
        public void keyPressed(KeyEvent evt) {
            if (_text.getCaretPosition() >= 0) {
                if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
                    // check selection start
                    if (_graphemeClusters
                        .hasOverlap(_text.getSelectionStart(), _text.getSelectionStart()))
                        _text.setCaretPosition(_graphemeClusters.nextRight(_text.getSelectionStart()));

                    int nextpos = _graphemeClusters.nextRight(_text.getCaretPosition() + 1);
                    if (nextpos >= 0) {
                        _text.moveCaretPosition(nextpos);
                        evt.consume();
                    }
                } else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
                    // check selection end
                    if (_graphemeClusters
                        .hasOverlap(_text.getSelectionEnd(), _text.getSelectionEnd()))
                        _text.setCaretPosition(_graphemeClusters.nextLeft(_text.getSelectionEnd()));

                    int nextpos = _graphemeClusters.nextLeft(_text.getCaretPosition() - 1);
                    if (nextpos >= 0) {
                        _text.moveCaretPosition(nextpos);
                        evt.consume();
                    }
                }
            }
        }

        private final JTextComponent _text;
    }

    /**
     * Document subclass to prevent the user from typing in anything
     * but indels or excludes and from removing any hold characters.
     **/
    public final class AlignmentDocument extends PlainDocument {
        /**
         * Constructor.
         * @param text The text component that this is associated with.
         **/
        public AlignmentDocument(JTextComponent text) {
            _text = text;
        }

        public void insertString(int offset, String str, AttributeSet attrs)
            throws BadLocationException {
            if (_disableInsertString)
            	return;
            	
            Messages messages = AppPrefs.getInstance().getMessages();

            if (_check && containsNonSpecial(str)) {
                Toolkit.getDefaultToolkit().beep();
                Dialogs.error(messages.getString("msgEditAlignmentNonSpecialCharacters"));
                return;
            }

            // check insert into grapheme cluster
            if (_graphemeClusters
                .hasOverlap(_text.getSelectionStart(), _text.getSelectionStart())) {
                Dialogs.error(messages.getString("msgEditAlignmentGraphemeClusterAnnotation"));
                return;
            }

            if (_check)
                _graphemeClusters.movePosition(offset, str.length());
            super.insertString(offset, str, attrs);
        }

        public void remove(int offset, int len) throws BadLocationException {
            if (_check && containsNonSpecial(getText(offset, len))) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            if (_check)
                _graphemeClusters.movePosition(offset, -len);
            super.remove(offset, len);
        }

        private boolean containsNonSpecial(String str) {
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                if (ch != Alignment.INDEL_SYMBOL && ch != Alignment.EXCLUDE_SYMBOL) {
                    return true;
                }
            }
            return false;
        }

        private void setCheck(boolean flag) {
            _check = flag;
        }

        public void setDisableInsertString(boolean flag) {
            _disableInsertString = flag;
        }

        private boolean _check = true;
        private boolean _disableInsertString = false;
        private final JTextComponent _text;
    }

    /**
     * Grapheme Cluster position.
     **/
    private class GraphemeClusters {

        public String set(String text) {
            _clusters.clear();

            int start = 0;
            int adjust = 0;
            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < text.length(); i++) {
                switch (text.charAt(i)) {
                    case Alignment.GRAPHEME_CLUSTER_START :
                        start = i - adjust;
                        ++adjust;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_END :
                        // add grapheme cluster
                        add(start, i - adjust);
                        ++adjust;
                        break;
                    default :
                        buf.append(text.charAt(i));
                        break;
                }
            }
            return buf.toString();
        }

        public boolean isEmpty() {
            return _clusters.isEmpty();
        }

        public boolean hasOverlap(int start, int end) {
            // check overlap with existing clusters
            for (Iterator it = _clusters.iterator(); it.hasNext();) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) it.next();

                if (pos.hasOverlap(start, end))
                    return true;
            }
            return false;
        }

        public void add(int start, int end) {
            // insert in ascending order
            int idx = 0;
            for (int i = 0; i < _clusters.size(); i++) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) _clusters.get(i);
                if (start < pos.getStart()) {
                    idx = i;
                    break;
                }
                idx = i + 1;
            }

            GraphemeClusterPosition newpos = new GraphemeClusterPosition(start, end);
            _clusters.add(idx, newpos);
        }

        public void delete(GraphemeClusterPosition pos) {
            _clusters.remove(pos);
        }

        public GraphemeClusterPosition get(int start, int end) {
            // find grapheme cluster and return cluster
            for (Iterator it = _clusters.iterator(); it.hasNext();) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) it.next();

                if (pos.hasOverlap(start, end))
                    return pos;
            }

            return null;
        }

        public void movePosition(int offset, int len) {
            for (Iterator it = _clusters.iterator(); it.hasNext();) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) it.next();

                if (offset <= pos.getStart())
                    pos.move(len);
            }
        }

        public String addTags(String text) {
            if (text.length() == 0 || _clusters.isEmpty())
                return text;

            // add grapheme cluster tags to vector
            StringBuffer buf = new StringBuffer();
            int start = 0;
            for (int i = 0; i < _clusters.size(); i++) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) _clusters.get(i);

                buf.append(text.substring(start, pos.getStart()));
                buf.append(Alignment.GRAPHEME_CLUSTER_START);
                start = pos.getEnd();
                buf.append(text.substring(pos.getStart(), start));
                buf.append(Alignment.GRAPHEME_CLUSTER_END);
            }
            buf.append(text.substring(start));
            return buf.toString();
        }

        public int nextLeft(int position) {
            // get next cursor position
            for (Iterator it = _clusters.iterator(); it.hasNext();) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) it.next();

                if (pos.hasOverlap(position, position))
                    return pos.getStart();
            }
            return -1;
        }

        public int nextRight(int position) {
            // get next cursor position
            for (Iterator it = _clusters.iterator(); it.hasNext();) {
                GraphemeClusterPosition pos = (GraphemeClusterPosition) it.next();

                if (pos.hasOverlap(position, position))
                    return pos.getEnd();
            }
            return -1;
        }
        private final ArrayList _clusters = new ArrayList();
    }

    /**
     * Grapheme Cluster position.
     **/
    private class GraphemeClusterPosition {

        public GraphemeClusterPosition(int start, int end) {
            _start = start;
            _end = end;
        }

        public int getStart() {
            return _start;
        }

        public int getEnd() {
            return _end;
        }

        public boolean hasOverlap(int start, int end) {
            return !(end <= _start || start >= _end);
        }

        public void move(int len) {
            _start += len;
            _end += len;
        }

        private int _start;
        private int _end;
    }

    private GraphemeClusters _graphemeClusters = new GraphemeClusters();
    private Alignment _alignment;
}