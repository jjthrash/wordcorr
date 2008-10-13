package org.wordcorr.gui;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.*;

/**
 * Dialog for finding an element in a list.
 * @author Keith Hamasaki
 **/
public class FindDialog extends GenericDialog {

    public static final String MATCH_WHOLE = "whole";
    public static final String MATCH_ANY   = "any";
    public static final String MATCH_START = "start";

    /**
     * Constructor.
     **/
    public FindDialog() {
        super(true);
        setTitle(AppPrefs.getInstance().getMessages().getString("btnFind"));
        init();
    }
    
    public FindDialog(String beanId) {
        super(true);
        this.beanId = beanId;
        setTitle(AppPrefs.getInstance().getMessages().getString("btnFind"));
        init();        
    }

    /**
     * Get the main panel for this dialog box.
     **/
    protected Component getMainPanel() {
        return new PropertyPane(null, _findProperties, NullRefreshable.getInstance(),beanId);
    }

    /**
     * Add a find listener.
     **/
    public void addFindListener(FindListener listener) {
        _listeners.add(listener);
    }

    /**
     * Remvoe a find listener.
     **/
    public void removeFindListener(FindListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Get the OK action for this dialog.
     **/
    protected Action getOKAction() {
        return new WordCorrAction("btnFindNext", "accFindNext") {
                public void actionPerformed(ActionEvent evt) {
                    for (Iterator it = _listeners.iterator(); it.hasNext(); ) {
                        ((FindListener) it.next()).find(new FindEvent());
                    }
                }
            };
    }

    /**
     * Listener interface.
     **/
    public interface FindListener extends EventListener {
        void find(FindDialog.FindEvent evt);
    }

    /**
     * Event class for find events.
     **/
    public class FindEvent extends EventObject {
        FindEvent() {
            super(FindDialog.this);
        }

        public FindProperties getProperties() {
            return _findProperties;
        }
    }

    /**
     * Bean to hold properties selected by the user.
     **/
    public final class FindProperties {

        public String getFindWhat() {
            return _findWhat;
        }

        public void setFindWhat(String findWhat) {
            _findWhat = findWhat;
        }

        public String getMatch() {
            return _match;
        }

        public void setMatch(String match) {
            _match = match;
        }

        public String toString() {
            return "Find Properties: what: " + _findWhat + ", match: " + _match;
        }

        private String _findWhat = "";
        private String _match = "any";
    }

    private final List _listeners = new ArrayList();
    private final FindProperties _findProperties = new FindProperties();
    private String beanId = null;
}
