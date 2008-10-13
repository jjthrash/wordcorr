package org.wordcorr.db;

import java.io.*;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;
import org.apache.commons.dbcp.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wordcorr.sqlrunner.SQLRunner;

/**
 * Database implementation representing a local HSQL database.
 * @author Keith Hamasaki, Jim Shiba
 **/
class HSQLDatabase implements Database {

    private static final Properties _props = new Properties();
    private static final Class[] PERSISTENT_CONS_ARGS =
        { Database.class, Long.TYPE };

    static {
        // load the properties and driver
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            _props.load(HSQLDatabase.class.getResourceAsStream("/hsql.properties"));
        } catch (Exception ignored) {}
    }

    /**
     * Constructor.
     **/
    public HSQLDatabase(File file) {
        _file = file;
        try {
            GenericObjectPool pool = new GenericObjectPool(null);
            DriverManagerConnectionFactory factory =
                new DriverManagerConnectionFactory(
                    "jdbc:hsqldb:" + _file.getAbsolutePath(),
                    "sa",
                    "");
            PoolableConnectionFactory conFactory =
                new PoolableConnectionFactory(factory, pool, null, null, false, true);
            PoolingDriver driver = new PoolingDriver();
            driver.registerPool(file.getAbsolutePath(), pool);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the name of this database.
     **/
    public String getName() {
        return _file.getAbsolutePath();
    }

    /**
     * Test this database.
     **/
    public int test() throws DatabaseException {

        Connection con = null;
        Statement stmt = null;

        try {
            try {
                con = getConnection();
            } catch (Exception e) {
                throw new DatabaseException(e);
            }

            // check for at least a baseline version
            try {
                stmt = con.createStatement();
                stmt.executeQuery(_props.getProperty("TEST_DATABASE"));
            } catch (SQLException e) {
                return STATUS_UNINITIALIZED;
            }

            // check for the current version
            try {
                stmt = con.createStatement();
                stmt.executeQuery(_props.getProperty("TEST_CURRENT_DATABASE"));
            } catch (SQLException e) {
                return STATUS_OLD;
            }

            return STATUS_CURRENT;
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Initialize this database.
     **/
    public void init() throws DatabaseException {
        try {
            // create tables
            InputStream in = getClass().getResourceAsStream("/hsql_create_tables.sql");
            SQLRunner sql = new SQLRunner(getConnection());
            sql.setIn(new BufferedReader(new InputStreamReader(in)));
            StringWriter wrt = new StringWriter();
            sql.setOut(new PrintWriter(wrt));
            sql.interact();

            // initialize db
            in = getClass().getResourceAsStream("/hsql_initialize.sql");
            sql = new SQLRunner(getConnection());
            sql.setIn(new BufferedReader(new InputStreamReader(in)));
            wrt = new StringWriter();
            sql.setOut(new PrintWriter(wrt));
            sql.interact();
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * No migration to be done; this is for future use.
     **/
    public void migrate() {}

    /**
     * Return a list of all users in the database.
     **/
    public List getUsers() throws DatabaseException {
        return retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_USERS";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {}

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                User user = new User(db, rs.getLong(1));
                user.updateObject(rs);
                return user;
            }
        });
    }

    /**
     * Get the current setting from this database.
     **/
    public synchronized Setting getCurrentSetting() throws DatabaseException {
        if (_currentSetting == null) {
            List settings = retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_SETTINGS";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException {}

                public Object createObject(Database db, ResultSet rs) throws SQLException {
                    Setting setting = new Setting(db, rs.getLong(1));
                    setting.updateObject(rs);
                    return setting;
                }
            });
            _currentSetting = (Setting) settings.get(0);
        }
        return _currentSetting;
    }

    /**
     * Return a list of all zones in the repository.
     **/
    public List getZones() throws DatabaseException {
        if (_zones == null) {
            _zones = retrieveObjects(new RetrieveAllParameters() {
                public String getRetrieveAllSQLKey() {
                    return "GET_ZONES";
                }

                public void setRetrieveAllParameters(PreparedStatement stmt)
                    throws SQLException {}

                public Object createObject(Database db, ResultSet rs) throws SQLException {
                    Zone zone = new Zone(db, rs.getLong(1));
                    zone.updateObject(rs);
                    return zone;
                }
            });
        }
        return _zones;
    }

    /**
     * Return a zone in the database based on row and column.
     **/
    public Zone getZone(final Integer row, final Integer col)
        throws DatabaseException {
        List list = retrieveObjects(new RetrieveAllParameters() {
            public String getRetrieveAllSQLKey() {
                return "GET_ZONE";
            }

            public void setRetrieveAllParameters(PreparedStatement stmt)
                throws SQLException {
                stmt.setInt(1, row.intValue());
                stmt.setInt(2, col.intValue());
            }

            public Object createObject(Database db, ResultSet rs) throws SQLException {
                Zone zone = new Zone(db, rs.getLong(1));
                zone.updateObject(rs);
                return zone;
            }
        });
        return (list.isEmpty()) ? null : (Zone) list.get(0);
    }

    /**
     * Make an object of the given type, with no data. This does not
     * create an entry in the database, but creates an in-memory
     * object that can later be saved to the database.
     **/
    public Persistent makeObject(Class cl) throws DatabaseException {
        try {
            Constructor cons = cl.getDeclaredConstructor(PERSISTENT_CONS_ARGS);
            return (Persistent) cons.newInstance(new Object[] { this, new Integer(-1)});
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Return a list of persistent objects.
     **/
    public List retrieveObjects(RetrieveAllParameters params)
        throws DatabaseException {
        String sqlkey = params.getRetrieveAllSQLKey();
        String sql = _props.getProperty(sqlkey);
        if (sql == null) {
            return Collections.EMPTY_LIST;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            List list = new ArrayList();
            con = getConnection();
            stmt = con.prepareStatement(sql);
            params.setRetrieveAllParameters(stmt);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object object = params.createObject(this, rs);
                if (object instanceof Persistent) {
                    ((Persistent) object).clearDirty();
                }
                if (object != null) {
                    list.add(object);
                }
            }
            rs.close();
            return list;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Add a database object.
     * @return The id of the new object.
     **/
    public long createObject(DatabaseObject object) throws DatabaseException {
        String sqlkey = object.getClass().getName() + ".CREATE";
        String sql = _props.getProperty(sqlkey);
        if (sql == null) {
            return -1;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            stmt = con.prepareStatement(sql);
            object.setCreateParameters(stmt);
            stmt.executeUpdate();
            ResultSet rs = stmt.executeQuery(_props.getProperty("GET_LAST_IDENTITY"));
            if (rs.next()) {
                long id = rs.getLong(1);
                con.commit();
                return id;
            } else {
                throw new SQLException("Could not find new object identity");
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (Exception ignored) {}
            throw new DatabaseException(e);
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Save an object to the database.
     **/
    public void saveObject(DatabaseObject object) throws DatabaseException {
        String sqlkey = object.getClass().getName() + ".UPDATE";
        String sql = _props.getProperty(sqlkey);
        if (sql == null) {
            return;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            object.setUpdateParameters(stmt);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Delete an object from database and set to new state.
     **/
    public void deleteObject(DatabaseObject object) throws DatabaseException {
        String sqlkey = object.getClass().getName() + ".DELETE";
        String sql = _props.getProperty(sqlkey);
        if (sql == null) {
            return;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, object.getID());
            ResultSet rs = stmt.executeQuery();
            rs.close();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Revert an object to its database state.
     **/
    public void revertObject(DatabaseObject object) throws DatabaseException {
        String sqlkey = object.getClass().getName() + ".RETRIEVE";
        String sql = _props.getProperty(sqlkey);
        if (sql == null) {
            return;
        }

        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, object.getID());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                object.updateObject(rs);
                if (object instanceof Persistent) {
                    ((Persistent) object).clearDirty();
                }
            }
            rs.close();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            cleanup(stmt, con);
        }
    }

    /**
     * Link an object to other objects with an optional order.
     **/
    public void linkObjects(LinkParameters params) throws DatabaseException {
        String unlinkkey = params.getRemoveSQLKey();
        String linkkey = params.getCreateSQLKey();
        String updkey = params.getUpdateSQLKey();
        if (unlinkkey == null || linkkey == null || updkey == null) {
            return;
        }

        Connection con = null;
        PreparedStatement updstmt = null;
        PreparedStatement linkstmt = null;
        PreparedStatement unlinkstmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);

            updstmt = con.prepareStatement(_props.getProperty(updkey));
            linkstmt = con.prepareStatement(_props.getProperty(linkkey));
            unlinkstmt = con.prepareStatement(_props.getProperty(unlinkkey));
            for (int i = 0; i < params.getLinkCount(); i++) {
                if (params.isLinked(i)) {
                    // first try an update, then an insert
                    updstmt.clearParameters();
                    params.setUpdateParameters(updstmt, i);
                    int test = updstmt.executeUpdate();
                    if (test == 0) {
                        linkstmt.clearParameters();
                        params.setCreateParameters(linkstmt, i);
                        linkstmt.executeUpdate();
                    }
                } else {
                    unlinkstmt.clearParameters();
                    params.setRemoveParameters(unlinkstmt, i);
                    unlinkstmt.executeUpdate();
                }
            }
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {}
            throw new DatabaseException(e);
        } finally {
            try {
                if (updstmt != null)
                    updstmt.close();
            } catch (SQLException ignored) {}
            try {
                if (linkstmt != null)
                    linkstmt.close();
            } catch (SQLException ignored) {}
            cleanup(unlinkstmt, con);
        }
    }

    /**
     * Insert database records and return new record information.
     * @return The RowData containing remoteID and timestamp of the new records.
     **/
    public List insertRecords(StatementParameters parameters)
        throws DatabaseException {
        List list = new ArrayList();
        String sqlkey = parameters.getTable() + "." + parameters.getAction();
        String sql = _props.getProperty(sqlkey.toUpperCase());
        if (sql == null) {
            return list;
        }

        Connection con = null;
        PreparedStatement insertStmt = null;
        PreparedStatement queryStmt = null;
        PreparedStatement timestampStmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            insertStmt = con.prepareStatement(sql);
            queryStmt = con.prepareStatement(_props.getProperty("GET_LAST_IDENTITY"));
            timestampStmt =
                con.prepareStatement(
                    _props.getProperty(parameters.getTable().toUpperCase() + ".TIMESTAMP"));
            while (parameters.hasNext()) {
                // define query parameters and create new record
                // RowData is reused for result
                RowData data = parameters.setNext(insertStmt);
                insertStmt.executeUpdate();

                // query for new id
                ResultSet rs = queryStmt.executeQuery();
                if (rs.next()) {
                    long id = rs.getLong(1);

                    // query for timestamp
                    timestampStmt.setLong(1, id);
                    ResultSet rs2 = timestampStmt.executeQuery();
                    if (rs2.next()) {
                        // set new id and timestamp
                        data.setRemoteID(id);
                        data.setTimestamp(rs2.getTimestamp(1));
                        con.commit();
                        list.add(data);
                    }
                } else {
                    throw new SQLException("Could not find new object identity");
                }
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (Exception ignored) {}
            throw new DatabaseException(e);
        } finally {
            cleanup(insertStmt, con);
            cleanup(queryStmt, con);
            cleanup(timestampStmt, con);
        }
        return list;
    }

    /**
     * Update database records and return new record information.
     * @return The RowData containing remoteID and timestamp of the new records.
     **/
    public List updateRecords(StatementParameters parameters)
        throws DatabaseException {
        List list = new ArrayList();
        String sqlkey = parameters.getTable() + "." + parameters.getAction();
        String sql = _props.getProperty(sqlkey.toUpperCase());
        if (sql == null) {
            return list;
        }

        Connection con = null;
        PreparedStatement updateStmt = null;
        PreparedStatement timestampStmt = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            updateStmt = con.prepareStatement(sql);
            timestampStmt =
                con.prepareStatement(
                    _props.getProperty(parameters.getTable().toUpperCase() + ".TIMESTAMP"));
            while (parameters.hasNext()) {
                // define query parameters and update record
                // RowData is reused for result
                RowData data = parameters.setNext(updateStmt);
                updateStmt.executeUpdate();

                // query for timestamp
                timestampStmt.setLong(1, data.getID());
                ResultSet rs = timestampStmt.executeQuery();
                if (rs.next()) {
                    // set timestamp
                    data.setTimestamp(rs.getTimestamp(1));
                    con.commit();
                    list.add(data);
                } else {
                    throw new SQLException("Could not get timestamp");
                }
            }
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (Exception ignored) {}
            throw new DatabaseException(e);
        } finally {
            cleanup(updateStmt, con);
            cleanup(timestampStmt, con);
        }
        return list;
    }

    /**
     * Get a connection for this database.
     **/
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:apache:commons:dbcp:" + _file.getAbsolutePath());
        //return DriverManager.getConnection("jdbc:hsqldb:" + _file.getAbsolutePath(), "sa", "");
    }

    /**
     * Cleanup a statement and connection.
     **/
    void cleanup(Statement stmt, Connection con) {
        if (stmt != null)
            try {
                stmt.close();
            } catch (SQLException e) {}
        if (con != null)
            try {
                con.close();
            } catch (SQLException e) {}
    }

    private final File _file;
    private Setting _currentSetting;
    private List _zones = null;
}