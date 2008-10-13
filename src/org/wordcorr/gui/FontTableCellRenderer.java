/*
 * FontTableCellRenderer.java
 *
 * Created on November 16, 2005, 1:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wordcorr.gui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
/**
 *
 * @author Nathan
 */
public class FontTableCellRenderer extends DefaultTableCellRenderer {
    
    private String fontKey;
    
    /** Creates a new instance of FontTableCellRenderer */
    public FontTableCellRenderer() {
        super();
    }
    
    public FontTableCellRenderer(String fontKey) {
        super();
        this.fontKey = fontKey;
    }
    
    public Component getTableCellRendererComponent
            (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
        cell.setFont(FontCache.getFont(fontKey));
        return cell;
    }
}
