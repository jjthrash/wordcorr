package org.wordcorr.gui;

import java.io.*;
import java.util.*;

/**
 * Keeps track of user preferences.
 * @author Keith Hamasaki
 **/
public class AppPrefs {

    public static final String LAST_DIR         = "LAST_DIR";
    public static final String LAST_PANE        = "LAST_PANE";
    public static final String LOCALE_CODE      = "LOCALE_CODE";
    public static final String LOCALE_COUNTRY   = "LOCALE_COUNTRY";
    public static final String DIVIDER_LOCATION = "DIVIDER_LOCATION";
    public static final String HIDE_LOCATION    = "HIDE_LOCATION";
    public static final String LOCATION_X       = "LOCATION_X";
    public static final String LOCATION_Y       = "LOCATION_Y";
    public static final String WIDTH            = "WIDTH";
    public static final String HEIGHT           = "HEIGHT";
    public static final String FONT             = "FONT";
    public static final String IPA_FONT         = "IPA_FONT";
    public static final String PRIMARY_GLOSS_FONT = "PRIMARY_GLOSS_FONT";
    public static final String SECONDARY_GLOSS_FONT = "SECONDARY_GLOSS_FONT";

    private static final AppPrefs _instance = new AppPrefs();

    public static AppPrefs getInstance() {
        return _instance;
    }

    private AppPrefs() {
        load();
    }

    /**
     * Get the config directory.
     **/
    public File getConfigDir() {
        File userDir = new File(System.getProperty("user.home"));
        File prefDir = new File(userDir, ".wordcorr");
        if (prefDir.isFile()) {
            prefDir.delete();
        }
        if (!prefDir.exists()) {
            prefDir.mkdirs();
        }
        return prefDir;
    }

    /**
     * Get the config file.
     **/
    private File getConfigFile() {
        return new File(getConfigDir(), "config");
    }

    /**
     * Load the preferences from the preferences file.
     **/
    private void load() {
        File prefFile = getConfigFile();
        if (prefFile.exists()) {
            try {
                InputStream inp = new FileInputStream(prefFile);
                _props.load(inp);
                inp.close();
            } catch (Exception e) {
                Dialogs.error(Messages.getInstance().getString("msgErrLoadingPref").trim() + " " + e);
            }
        }

        // Load the message handler
        String code = _props.getProperty(LOCALE_CODE, "en");
        String country = _props.getProperty(LOCALE_COUNTRY, "US");
        _messages = Messages.getInstance(code, country);
    }

    /**
     * Save the preferences to the preferences file.
     **/
    public void save() {
        // save user prefs
        File prefFile = getConfigFile();
        try {
            OutputStream out = new FileOutputStream(prefFile);
            _props.store(out, "WordCorr Preferences");
            out.close();
        } catch (Exception e) {
            Dialogs.error(_messages.getString("msgErrSavingPref") + " " + e);
        }
    }

    /**
     * Get the messages object.
     **/
    public Messages getMessages() {
        return _messages;
    }

    /**
     * Get a single property.
     **/
    public String getProperty(String key) {
        return _props.getProperty(key);
    }

    /**
     * Get a single property, with a default value.
     **/
    public String getProperty(String key, String def) {
        return _props.getProperty(key, def);
    }

    /**
     * Set a property.
     **/
    public void setProperty(String name, String value) {
        _props.setProperty(name, value);
    }

    /**
     * Get an int property.
     **/
    public int getIntProperty(String name, int def) {
        String prop = _props.getProperty(name);
        if (prop == null) {
            return def;
        }
        try {
            return Integer.parseInt(prop);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Set an int property.
     **/
    public void setIntProperty(String name, int value) {
        _props.setProperty(name, String.valueOf(value));
    }

    /**
     * Remove a property.
     **/
    public void removeProperty(String name) {
        _props.remove(name);
    }

    private final Properties _props = new Properties();
    private Messages _messages;
}
