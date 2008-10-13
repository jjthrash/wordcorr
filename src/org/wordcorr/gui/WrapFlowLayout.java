package org.wordcorr.gui;

import java.awt.*;

/**
 * FlowLayout that expands to allow multiple rows for wrapping.
 * @author Jim Shiba
 **/
class WrapFlowLayout extends FlowLayout {
    /**
     * Constructor.
     **/
    public WrapFlowLayout(int align) {
        super(align);
    }

    /**
     * Compute and set layout size depending on size of components.
     **/
    public Dimension preferredLayoutSize(Container target) {
        Dimension dim = super.preferredLayoutSize(target);
        int parentWidth =
            (target.getParent() != null) ? target.getParent().getSize().width : 64;

        // compensate for insets
        parentWidth -= target.getInsets().left + target.getInsets().right + getHgap();

        // Determine number of rows needed.
        int rows = 1;
        int width = 0;
        Component[] components = target.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.isVisible()) {
                width += component.getPreferredSize().width + getHgap();
            }
            if (width > parentWidth) {
                // next row
                width = component.getPreferredSize().width;
                ++rows;
            }
        }

        return new Dimension(dim.width, dim.height * rows);
    }
}