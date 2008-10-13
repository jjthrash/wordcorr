package org.wordcorr.gui;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import org.wordcorr.db.Alignment;

/**
 * Table that allows designated column to have aligned characters.
 *
 * @author Jim Shiba
 **/
public class AlignedTable extends JTable {

    /**
     * Constructor.
     **/
	public AlignedTable(TableModel dm, int column) {
		this(dm, column, new AlignedTableCellRenderer());
	}
    	
    /**
     * Constructor.
     **/
	public AlignedTable(TableModel dm, int column, AlignedTableCellRenderer renderer) {
		super (dm);
		_column = column;
		_alignedRenderer = renderer;
                getColumnModel().getColumn(column).setCellRenderer(renderer);
	}
    	
//    /**
//     * Get cell renderer for designated column.
//     **/
//    public TableCellRenderer getCellRenderer(int row, int column) {
//        if (column == _column) {
//            return _alignedRenderer;
//        }
//        return super.getCellRenderer(row, column);
//    }
    
    /**
     * Set number of character widths for each position
     * from row data list and data extractor.
     **/
    public void setAlignedPositionWidths(List rows, AlignedDataExtractor extractor) {
        List maxWidths = new ArrayList();
    	for (Iterator it = rows.iterator(); it.hasNext();) {
            String data = extractor.getColumnData(it.next());
            
            // skip non grapheme cluster data
            if (data.indexOf(Alignment.GRAPHEME_CLUSTER_START) == -1)
            	continue;

            // extract widths
            Integer one = new Integer(1);
            boolean gc = false;
            int pos = 0;
            int width = 0;
            for (int i = 0; i < data.length(); i++) {
                switch (data.charAt(i)) {
                    case Alignment.GRAPHEME_CLUSTER_START :
                    	gc = true;
                        break;
                    case Alignment.GRAPHEME_CLUSTER_END :
                    	// initialize
                    	if (pos >= maxWidths.size())
                    		maxWidths.add(one);
                    		
                		if (width > ((Integer)maxWidths.get(pos)).intValue())
                			maxWidths.set(pos, new Integer(width));
                			
                    	pos++;
                    	width = 0;
                        gc = false;
                        break;
                    default :
                    	if (!gc) {
	                    	// initialize
	                    	if (pos >= maxWidths.size())
	                    		maxWidths.add(one);
	                    	pos++;
                    	} else {
                    		// grapheme cluster
                    		width++;
                    	}
                        break;
                }
            }
        }
        if (maxWidths.isEmpty()) {
	        setAlignedPositionWidths(null);
        } else {
	        // convert to int array
	        int[] positionWidth = new int[maxWidths.size()];
	        int i = 0;
	        for (Iterator it = maxWidths.iterator(); it.hasNext();) {
	        	positionWidth[i++] = ((Integer)it.next()).intValue();
	        }
	
	        setAlignedPositionWidths(positionWidth);
        }
    }

    /**
     * Set number of character widths for each position.
     **/
    public void setAlignedPositionWidths(int[] positionWidth) {
    	_alignedRenderer.setPositionWidths(positionWidth);
    }
    
    int _column;
	AlignedTableCellRenderer _alignedRenderer;
}
