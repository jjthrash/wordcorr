package org.wordcorr.gui;

/**
 * Interface representing a task.
 * @author Jim Shiba
 **/
public interface Task {

    /**
     * Run task.
     * Return true to close dialog, false to keep open.
     **/
    boolean run();
}