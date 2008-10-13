package org.wordcorr.gui.tree;

/**
 * BranchNode represents a branch node in the tree, as in, not a leaf
 * node.
 * @author Keith Hamasaki
 **/
public class BranchNode extends WNode {

    /**
     * Constructor.
     * @param obj The data object.
     **/
    public BranchNode(Object obj) {
        super(obj);
    }

    /**
     * This is not a leaf, so this always returns false.
     **/
    public final boolean isLeaf() {
        return false;
    }
}
