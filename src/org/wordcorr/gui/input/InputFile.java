package org.wordcorr.gui.input;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.gui.AppPrefs;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.WButton;
import org.wordcorr.gui.action.WordCorrAction;

/**
 * Input row for entering filename into an entry.
 * @author Jim Shiba
 **/
public class InputFile extends InputRow {
    /**
     * Constructor.
     **/
    public InputFile(BeanCatalog.Property prop, Object obj, Refreshable refresh) {
        super(prop, obj);
        _text.setDocument(new LimitDocument(prop.getMaxLength(), prop.getType()));
        _text.setColumns(prop.getColumns());
        if (prop.getDefault() != null) {
            setValue(prop.getDefault());
        }
        _text.getDocument().addDocumentListener(
            new DocumentChangeListener(obj, refresh, prop.getID()));
        init(_panel, _panel);
    }

    /**
     * Get the value of this row.
     **/
    public final Object getValue() {
        return _text.getText();
    }

    /**
     * Set the value of this row.
     **/
    public final void setValue(Object val) {
        _text.setText(String.valueOf(val));
    }

    /**
     * Filename panel.
     **/
    private final class FilenamePanel extends JPanel {
        FilenamePanel() {
            super(new BorderLayout());
            add(_text, BorderLayout.CENTER);
            add(new WButton(new WordCorrAction("btnBrowseFile", "") {
                public void actionPerformed(ActionEvent evt) {
                    MainFrame mf = MainFrame.getInstance();
                    JFileChooser fc =
                        new JFileChooser(AppPrefs.getInstance().getProperty(AppPrefs.LAST_DIR, "."));
                    int ret = fc.showOpenDialog(mf);
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        setValue(fc.getSelectedFile().getPath());
			            AppPrefs.getInstance().setProperty(AppPrefs.LAST_DIR, fc.getSelectedFile().getAbsolutePath());
                    }
                }
            }), BorderLayout.EAST);
        }
    }

    private JTextField _text = new JTextField();
    private final FilenamePanel _panel = new FilenamePanel();
}