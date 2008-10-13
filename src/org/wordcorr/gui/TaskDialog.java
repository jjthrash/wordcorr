package org.wordcorr.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import org.wordcorr.gui.Task;
import org.wordcorr.gui.MainFrame;
import org.wordcorr.gui.action.GenericAction;
import org.wordcorr.gui.action.WordCorrAction;
import org.wordcorr.gui.input.InputRow;
import org.wordcorr.gui.input.InputTable;

/**
 * Dialog for tasks.
 *
 * @author Jim Shiba
 **/
public class TaskDialog extends GenericDialog {

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     **/
    public TaskDialog(String titleKey, Task task) {
        this(titleKey, task, null);
    }

    /**
     * Constructor.
     * @param titleKey The message key for the dialog title
     **/
    public TaskDialog(String titleKey, Task task, String beanId) {
        super(true);

        _task = task;
        Messages messages = AppPrefs.getInstance().getMessages();
        setTitle(messages.getString(titleKey));
        _propertyPane =
            new PropertyPane(null, _task, NullRefreshable.getInstance(), beanId);

        init();
        setSize(600, 400);
        setLocationRelativeTo(MainFrame.getInstance());
    }

    /**
     * Get the main panel for this dialog box.
     **/
    protected Component getMainPanel() {
        return _propertyPane;
    }

    /**
     * Set cancel button visibility.
     **/
    public void isCancelVisible(boolean show) {
        getCancelButton().setVisible(show);
    }

    /**
     * Get Input Row.
     **/
    public InputRow getRow(String name) {
        return _propertyPane.getInfo().getRow(name);
    }

    /**
     * Get the OK action for this dialog.
     **/
    protected Action getOKAction() {
        final TaskDialog thisDialog = this;
        return new GenericAction("btnOK", "accOK") {
            protected void performAction(ActionEvent evt) throws Exception {
                if (!_propertyPane.getInfo().validateFields()) {
                    return;
                }
                Dialogs.showWaitCursor(TaskDialog.this);
                setCancelled(false);
                Dialogs.indeterminateProgressDialog(
                        new Runnable(){
                    public void run() {
                        if (_task.run()) {
                            dispose();
                        }
                    }
                },AppPrefs.getInstance().getMessages().getString("pgbWaitString"),"");

                Dialogs.showDefaultCursor(TaskDialog.this);
            }
        };
    }

    private Task _task;
    private final PropertyPane _propertyPane;
}