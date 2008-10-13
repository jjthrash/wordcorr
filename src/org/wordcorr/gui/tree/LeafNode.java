package org.wordcorr.gui.tree;

/**
 * LeafNode represents a leaf node in the tree.
 * @author Keith Hamasaki
 **/
public class LeafNode extends WNode {

    /**
     * Constructor.
     * @param obj The data object.
     **/
    public LeafNode(Object obj) {
        super(obj, false);
    }

    /**
     * This is a leaf, so this always returns true.
     **/
    public final boolean isLeaf() {
        return true;
    }
}
