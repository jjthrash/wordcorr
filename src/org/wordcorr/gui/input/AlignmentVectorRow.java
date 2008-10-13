package org.wordcorr.gui.input;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.JPanel;
import java.util.*;
import org.wordcorr.BeanCatalog;
import org.wordcorr.db.Alignment;
import org.wordcorr.gui.FontCache;
import org.wordcorr.gui.Refreshable;
import org.wordcorr.gui.WButton;
import org.wordcorr.gui.action.WordCorrAction;

/**
 * Input row for editing an alignment vector.
 * @author Keith Hamasaki, Jim Shiba
 **/
public class AlignmentVectorRow extends InputRow {

    /**
     * Constructor.
     **/
    public AlignmentVectorRow(
        BeanCatalog.Property prop,
        Object obj,
        Refreshable refresh) {
        super(prop, obj);
        _alignment = (Alignment) obj;
        _text = new AlignmentVectorTextField();
        _text.setAlignment(_alignment);
        _text.setFont(FontCache.getIPA());

        JPanel btnpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnpanel
            .add(new WButton(new WordCorrAction(
                "btnGraphemeClusterDefine",
                "accGraphemeClusterDefine") {
            public void actionPerformed(ActionEvent evt) {
                _text.defineGraphemeCluster();
            }
        }), BorderLayout.NORTH);
        btnpanel
            .add(new WButton(new WordCorrAction(
                "btnGraphemeClusterUncluster",
                "accGraphemeClusterUncluster") {
            public void actionPerformed(ActionEvent evt) {
                _text.unclusterGraphemeCluster();
            }
        }), BorderLayout.NORTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(_text, BorderLayout.NORTH);
        panel.add(btnpanel, BorderLayout.CENTER);
        init(panel, _text);
    }

    /**
     * Get the value of this row.
     **/
    public final Object getValue() {
        return _text.getValue();
    }

    /**
     * Set the value of this row.
     **/
    public final void setValue(Object val) {
        _text.setValue(val);
    }

    private List _viewGraphemeClusters;
    private final Alignment _alignment;
    private final AlignmentVectorTextField _text;
}