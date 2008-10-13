package org.wordcorr.gui.input;

/**
 * A basic option class that gets its label from the messages.
 * @author Keith Hamasaki
 **/
public class BasicOption implements Option {

    public BasicOption(String key, String value) {
        _key = key;
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public String toString() {
        return org.wordcorr.gui.AppPrefs.getInstance().getMessages().getString(_key);
    }

    private final String _key;
    private final String _value;
}
