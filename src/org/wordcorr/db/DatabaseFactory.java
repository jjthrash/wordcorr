package org.wordcorr.db;

import org.wordcorr.AppProperties;
import java.io.File;

/**
 * Factory for databases
 * @author Keith Hamasaki, Jim Shiba
 **/
public final class DatabaseFactory {

    private static final Class[] CONS_ARGS = { File.class };

    // don't make me
    private DatabaseFactory() {}

    /**
     * Create a new local Database at the location indicated.
     **/
    public static Database newLocalDatabase(File location)
        throws DatabaseException {
        Database db = openLocalDatabase(location);
        db.init();
        Setting setting = (Setting) db.makeObject(Setting.class);
        setting.setName("default");
        setting.save();
        return db;
    }

    /**
     * Load a local Database at the location indicated.
     **/
    public static Database openLocalDatabase(File location) {
        String cname =
            AppProperties.getProperty("LocalDBClass", "org.wordcorr.db.HSQLDatabase");
        try {
            Class cl = Class.forName(cname);
            java.lang.reflect.Constructor cons = cl.getConstructor(CONS_ARGS);
            return (Database) cons.newInstance(new Object[] { location });
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load a remote Database at the location indicated.
     **/
    public static Database openRemoteDatabase(String userid, String password) {
        return null;
    }
}