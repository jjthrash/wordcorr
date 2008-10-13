package org.wordcorr.gui.tree;

import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.Setting;

/**
 * Base class of WordCorr tree nodes.
 * @author Keith Hamasaki
 **/
public abstract class WNode extends javax.swing.tree.DefaultMutableTreeNode {

    /**
     * Constructor.
     * @param obj The data object.
     **/
    public WNode(Object obj) {
        super(obj);
    }

    /**
     * Constructor.
     * @param obj The data object.
     * @param childrenAllowed Whether or not this node allows children
     **/
    public WNode(Object obj, boolean childrenAllowed) {
        super(obj, childrenAllowed);
    }

    /**
     * Get the icon for this node. The default implementation returns
     * null, indicating that the default icon is to be used.
     **/
    public javax.swing.ImageIcon getIcon() {
        return null;
    }

    /**
     * Get the right hand pane associated with this object. The
     * default implementation returns null, which means to use an
     * empty label.
     **/
    public java.awt.Component getRightComponent() {
        return null;
    }

    /**
     * Update the setting with the database object associated with this
     * pane.
     **/
    public void updateSetting(Setting setting) throws DatabaseException {
    }
}
