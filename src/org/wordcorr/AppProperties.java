package org.wordcorr;

import java.io.*;
import java.util.Properties;

/**
 * Application configuration properties.
 * @author Keith Hamasaki
 **/
public final class AppProperties {

    private static final Properties _props = new Properties();

    static {
        try {
            _props.load(AppProperties.class.getResourceAsStream("/wordcorr.properties"));
        } catch (Exception ignored) { }
    }

    // don't make me
    private AppProperties() { }

    /**
     * Get an application property.
     **/
    public static String getProperty(String key) {
        return _props.getProperty(key);
    }

    /**
     * Get an application property, with a default.
     **/
    public static String getProperty(String key, String def) {
        return _props.getProperty(key, def);
    }
}
