package org.wordcorr.sqlrunner;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Minimal database interrogator.
 * @author Jonathan Higa
 **/
public final class SQLRunner {
    private BufferedReader IN;
    private PrintWriter OUT;

    public static void main(String[] arg) throws SQLException, IOException {
        SQLRunner sqli;
        switch (arg.length) {
            case 2:
                sqli = new SQLRunner(arg[0], arg[1]);
                break;
            case 3:
                sqli = new SQLRunner(arg[0], arg[1], arg[2]);
                break;
            case 4:
                sqli = new SQLRunner(arg[0], arg[1], arg[2], arg[3]);
                break;
            default:
                throw new IllegalArgumentException("Use: java " + SQLRunner.class.getName() + " driver[,driver...] url [login [password]]");
        }
        sqli.interact();
    }

    public SQLRunner(String drv, String url, String usr, String psw) throws SQLException {
        try {
            IN = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
            OUT = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")), true);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("utf-8");
        }

        loadDrivers(drv);
        _db = DriverManager.getConnection(url, usr, psw);
        defineCommands();
    }

    public SQLRunner(String drv, String url, String usr) throws SQLException {
        this(drv, url, usr, "");
    }

    public SQLRunner(String drv, String url) throws SQLException {
        this(drv, url, "");
    }

    public SQLRunner(Connection con) {
        _db = con;
        defineCommands();
    }

    public void interact() throws SQLException, IOException {
        try {
            OUT.println("SQL command:");
            for (String cmd; (cmd = IN.readLine()) != null; ) {
                execute(cmd);
                OUT.println("SQL command:");
            }
            doSQL();
            OUT.println("Good-bye.");
            if (!_db.getAutoCommit()) {
                _db.rollback();
            }
        } finally {
            _db.close();
        }
    }

    /**
     * Set the input stream.
     **/
    public void setIn(BufferedReader in) {
        IN = in;
    }

    /**
     * Set the output stream.
     **/
    public void setOut(PrintWriter out) {
        OUT = out;
    }

    /**
     * Touch each class in this list of drivers.
     * @param drivers comma-separated list of classes
     **/
    private void loadDrivers(String drivers) {
        for (StringTokenizer t = new StringTokenizer(drivers, " ,:;|"); t.hasMoreTokens(); ) {
            try {
                Class.forName(t.nextToken());
            } catch (ClassNotFoundException e) {
                System.err.println("WARNING: no driver " + e.getMessage());
            }
        }
    }

    /**
     * Execute a SQL command.
     * @param cmd SQL command
     **/
    private void execute(String cmd) throws SQLException {
        String key = getToken(cmd, 0).toLowerCase();
        try {
            Command q = (Command)_sql.get(key);
            (q == null ? _sql0 : q).execute(cmd);
        } catch (SQLException e) {
            error(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            error(e.toString());
            throw e;
        }
    }

    /**
     * @return whatever the user typed
     **/
    private String input(BufferedReader in) throws IOException {
        OUT.println("SQL command:");
        StringBuffer buf = new StringBuffer();
        String cmd;
        while ((cmd = in.readLine()) != null && (cmd.length() > 0 || buf.toString().trim().length() == 0)) {
            OUT.println("Input: " + cmd);
            buf.append(cmd).append('\n');
        }
        return cmd == null && isBlank(cmd = buf.toString()) ? null : buf.toString();
    }

    private void print(ResultSet res) throws SQLException {
        final ResultSetMetaData info = res.getMetaData();
        final int n = info.getColumnCount();
        final int[] width = new int[n];
        // Print headers.
        for (int j = 1; j <= n; j++) {
            final String name = info.getColumnLabel(j);
            OUT.print('|');
            OUT.print(pad(name, width[j-1] = Math.min(_width, Math.max(info.getColumnDisplaySize(j), name.length()))));
        }
        OUT.println('|');
        for (int j = 0; j < n; j++) {
            OUT.print('+');
            OUT.print(fill('-', width[j]));
        }
        OUT.println('+');
        while (res.next()) {
            for (int j = 1; j <= n; j++) {
                OUT.print('|');
                OUT.print(pad(res.getString(j), width[j-1]));
            }
            OUT.println('|');
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    private void error(String msg) {
        OUT.print("ERROR: ");
        OUT.println(msg.trim());
        OUT.flush();
    }

    private static String pad(String s, int n) {
        if (s == null) {
            return fill('@', n);
        }
        int m = s.length();
        return m==n ? s : m>n ? s.substring(0,n) : s+fill(' ',n-m);
    }

    private static String fill(char c, int n) {
        StringWriter buf = new StringWriter();
        for (int i = 0; i < n; i++) {
            buf.write(c);
        }
        return buf.toString();
    }

    private void defineCommands() {
        defineCommand("", new Execute());
        defineCommand("--", new Comment());
        defineCommand("\\help", new Help(), 2);
        defineCommand("\\autogo", new AutoExecute(), 2);
        defineCommand("\\tables", new Tables(), 2);
        defineCommand("\\describe", new Describe(), 2);
        defineCommand("\\begin", new Begin(), 2);
        defineCommand("\\commit", new Commit(), 2);
        defineCommand("\\rollback", new Rollback(), 2);
        defineCommand("\\width", new Width(), 2);
    }

    private void defineCommand(String cmd, Command exec, int len) {
        _help.add(cmd);
        for (int n = cmd.length(); n >= len; n--) {
            _sql.put(cmd.substring(0, n), exec);
        }
    }

    private void defineCommand(String cmd, Command exec) {
        _sql.put(cmd, exec);
    }

    private void doSQL() throws SQLException {
        String sql = _cmd.toString();
        _cmd.setLength(0);
        if (sql.trim().length() == 0) {
            return;
        }
        OUT.println("Executing.");
        Statement st = _db.createStatement();
        try {
            for (boolean t = st.execute(sql); ; t = st.getMoreResults()) {
                if (t) {
                    print(st.getResultSet());
                } else {
                    int n = st.getUpdateCount();
                    switch (n) {
                        case -1:
                            return;
                        case 0:
                            OUT.println("No record was touched.");
                            break;
                        case 1:
                            OUT.println("1 record was touched.");
                            break;
                        default:
                            OUT.print(n);
                            OUT.println(" records were touched.");
                    }
                }
            }
        } finally {
            st.close();
        }
    }

    private static String getToken(String s, int n) {
        return getToken(s, n, "");
    }

    private static String getToken(String s, int n, String z) {
        StringTokenizer t = new StringTokenizer(s, " \t\n\r\f;");
        while (t.hasMoreTokens()) {
            s = t.nextToken();
            if (n == 0) {
                return s;
            }
            n--;
        }
        return z;
    }

    private class Input implements Command {
        public void execute(String sql) throws SQLException {
            _cmd.append(sql).append('\n');
            if (_exec) {
                doSQL();
            }
        }
    }

    private class Execute implements Command {
        public void execute(String sql) throws SQLException {
            doSQL();
        }
    }

    private class AutoExecute implements Command {
        public void execute(String sql) throws SQLException {
            _exec = !_exec;
            OUT.print("AutoExecute: ");
            OUT.println(_exec);
        }
    }

    private class Help implements Command {
        public void execute(String ignore) {
            for (Iterator i = _help.iterator(); i.hasNext(); ) {
                OUT.println(i.next());
            }
        }
    }

    private class Begin implements Command {
        public void execute(String ignore) throws SQLException {
            OUT.println("Beginning transaction.");
            _db.setAutoCommit(false);
        }
    }

    private class Commit implements Command {
        public void execute(String ignore) throws SQLException {
            OUT.println("Committing transaction.");
            try {
                _db.commit();
            } finally {
                _db.setAutoCommit(true);
            }
        }
    }

    private class Rollback implements Command {
        public void execute(String ignore) throws SQLException {
            OUT.println("Rolling back transaction.");
            try {
                _db.rollback();
            } finally {
                _db.setAutoCommit(true);
            }
        }
    }

    private class Tables implements Command {
        public void execute(String cmd) throws SQLException {
            String t = getToken(cmd, 1, "%");
            OUT.print("Tables in schema '");
            OUT.print(t);
            OUT.println("':");
            print(_db.getMetaData().getTables(null, t, "%", null));
        }
    }

    private class Describe implements Command {
        public void execute(String cmd) throws SQLException {
            String t = getToken(cmd, 1);
            OUT.print("Columns in table '");
            OUT.print(t);
            OUT.println("':");
            Statement sql = _db.createStatement();
            try {
                ResultSetMetaData r = sql.executeQuery("select * from " + t).getMetaData();
                final int n = r.getColumnCount();
                for (int i = 1; i <= n; i++) {
                    OUT.print(r.getColumnName(i));
                    OUT.print(' ');
                    OUT.print(r.getColumnTypeName(i));
                    try {
                        if (r.getPrecision(i) > 0) {
                            OUT.print('(');
                            OUT.print(r.getPrecision(i));
                            try {
                                if (r.getScale(i) > 0) {
                                    OUT.print(',');
                                    OUT.print(r.getScale(i));
                                }
                            } catch (SQLException e) {}
                            OUT.print(')');
                        }
                    } catch (SQLException e) {}
                    if (r.isNullable(i) == r.columnNoNulls) {
                        OUT.print(" not null");
                    }
                    OUT.println();
                }
            } finally {
                sql.close();
            }
        }
    }

    private class Width implements Command {
        public void execute(String cmd) {
            String arg = getToken(cmd, 1);
            if (arg.length() == 0) {
                OUT.print("Column width: ");
                OUT.println(_width);
            } else {
                _width = Math.max(1, Integer.parseInt(arg));
            }
        }
    }

    private final Connection _db;
    private final Map _sql = new HashMap();
    private final Collection _help = new TreeSet();
    private final Command _sql0 = new Input();
    private final StringBuffer _cmd = new StringBuffer();
    private boolean _exec = false;
    private int _width = 40;
}
