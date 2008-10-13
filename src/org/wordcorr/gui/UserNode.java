package org.wordcorr.gui;

import java.awt.Component;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import org.wordcorr.db.DatabaseException;
import org.wordcorr.db.User;
import org.wordcorr.db.Setting;
import org.wordcorr.gui.input.*;
import org.wordcorr.gui.tree.*;

/**
 * A node representing one user.
 * @author Keith Hamasaki, Jim Shiba
 **/
final class UserNode extends BranchNode implements Refreshable {

    /**
     * Constructor.
     **/
    UserNode(User user, DefaultTreeModel model) {
        super(user);
        _user = user;
        _model = model;
    }

    /**
     * Get this node's user.
     **/
    public User getUser() {
        return _user;
    }

    /**
     * Get the tree icon for this node.
     **/
    public ImageIcon getIcon() {
        return new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/development/Application16.gif"));
    }

    /**
     * Get the right side component for this user.
     **/
    public synchronized Component getRightComponent() {
        if (_rightPane == null) {
            _rightPane = new SavePane("lblUser", _user, this);
        }
        return _rightPane;
    }

    /**
     * Update the setting with this object as their current.
     **/
    public void updateSetting(Setting setting) throws DatabaseException {
        setting.setUserID(_user.getID());
        setting.setCollectionID(-1);
        setting.setViewID(-1);
        setting.save();
    }

    /**
     * Refresh this node.
     **/
    public void refresh() {
        _model.nodeChanged(this);
    }

    /**
     * Does this node equal another?
     **/
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o.getClass() != this.getClass()) {
            return false;
        }

        return ((UserNode) o)._user.equals(_user);
    }

    private final User _user;
    private final DefaultTreeModel _model;
    private Component _rightPane;
}

