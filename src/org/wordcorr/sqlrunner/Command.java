package org.wordcorr.sqlrunner;

import java.sql.*;

interface Command {
    void execute(String sql) throws SQLException;
}
