package org.wordcorr.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.db.DatabaseException;

/**
 * Button that refreshes its nearest Refreshable parent
 * @author Keith Hamasaki
 **/
public class RefreshButton extends AlignedButton {

    public RefreshButton(int align) {
        super("mnuRefresh", "accRefresh", align);
        addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    Component src = ((Component) evt.getSource());
                    Dialogs.showWaitCursor(src);
                    try {
                        Component c = src;
                        do {
                            if (c instanceof Refreshable) {
                                ((Refreshable) c).refresh();
                                break;
                            }
                            c = c.getParent();
                        } while (c != null);
                    } catch(DatabaseException e) {
                        Dialogs.genericError(e);
                    } finally {
                        Dialogs.showDefaultCursor(src);
                    }
                }
            });
    }
}
