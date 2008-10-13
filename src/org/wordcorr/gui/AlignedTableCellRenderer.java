package org.wordcorr.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import org.wordcorr.AppProperties;
import org.wordcorr.db.Alignment;

/**
 * Table cell renderer class to align glyphs.
 * @author Jim Shiba
 *  **/
public class AlignedTableCellRenderer
    extends JComponent
    implements TableCellRenderer {

    public AlignedTableCellRenderer() {
        super();
        setFont(FontCache.getIPA()); //davisnw
        
        // necessary for background color change
        setOpaque(true);
    }

    protected void paintComponent(Graphics g) {
        // background
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // output text
        FontMetrics fm = getFontMetrics(getFont());
        int charWidth = fm.charWidth('W');
        g.setColor(getForeground());
        boolean graphemeCluster = false;
        int graphemeClusterSize = 0;
        int pos = 0;
        int x = 0;
        int y = getHeight() - fm.getMaxDescent();
        for (int i = 0; i < _value.length(); i++) {
            switch (_value.charAt(i)) {
                case Alignment.GRAPHEME_CLUSTER_START :
                    g.setColor(gcColor[pos % 2]);
                    graphemeCluster = true;
                    break;
                case Alignment.GRAPHEME_CLUSTER_END :
                    g.setColor(getForeground());
                    graphemeCluster = false;

                    // add space for remaining characters
                    if (_positionWidths != null && _positionWidths[pos] > graphemeClusterSize)
                        for (int j = graphemeClusterSize; j < _positionWidths[pos]; j++)
                            x += charWidth;
                    graphemeClusterSize = 0;
                    pos++;
                    break;
                default :
                    g.drawString(_value.substring(i, i + 1), x, y);
                    x += charWidth;

                    // add spacing for character position alignment
                    // note: _positionWidths.length can be smaller than pos because width computation
                    //   skips non grapheme cluster data.
                    if (!graphemeCluster) {
                        if (_positionWidths != null
                            && pos < _positionWidths.length
                            && _positionWidths[pos] > 1)
                            for (int j = 1; j < _positionWidths[pos]; j++)
                                x += charWidth;
                        pos++;
                    } else {
                        graphemeClusterSize++;
                    }
            }
        }
    }

    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column) {
        // set background color
        if (isSelected)
            setBackground(UIManager.getColor("Table.selectionBackground"));
        else
            setBackground(UIManager.getColor("Table.background"));

        // convert alignment object to aligned datum string
        if (value.getClass().getName().endsWith("Alignment")) {
            _value = ((Alignment) value).getAlignedDatum();
        } else {
            _value = (String) value;
        }
        return this;
    }

    public void setPositionWidths(int[] positionWidths) {
        _positionWidths = positionWidths;
    }

    protected String _value;
    protected int[] _positionWidths;
    private Color[] gcColor =
        {
            Color.decode(AppProperties.getProperty("GraphemeClusterColor0")),
            Color.decode(AppProperties.getProperty("GraphemeClusterColor1"))};
}