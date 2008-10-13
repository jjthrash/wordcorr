package org.wordcorr.gui;

/**
 * Null Refreshable.
 * @author Jim Shiba
 **/
public class NullRefreshable implements Refreshable {

    private static final Refreshable _instance = new NullRefreshable();

    public static Refreshable getInstance() {
        return _instance;
    }

    private NullRefreshable() {
    }

    public void refresh() {
    }
}
