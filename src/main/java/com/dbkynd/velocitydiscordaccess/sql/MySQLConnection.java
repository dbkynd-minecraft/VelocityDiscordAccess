package com.dbkynd.velocitydiscordaccess.sql;

import com.dbkynd.velocitydiscordaccess.VelocityDiscordAccess;
import org.slf4j.Logger;

import java.sql.*;

public class MySQLConnection {
    private static final Logger logger = VelocityDiscordAccess.logger;

    private Connection connection;

    private final String host;
    private final String port;
    private final String user;
    private final String password;
    private final String database;

    public MySQLConnection(String sqlHost, String sqlPort, String sqlDatabase, String sqlUser, String sqlPassword) {
        this.host = sqlHost;
        this.port = sqlPort;
        this.database = sqlDatabase;
        this.user = sqlUser;
        this.password = sqlPassword;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ex) {
            // handle the error
        }
    }

    public void connect() throws SQLException {
        if (this.connection != null) {
            this.connection.close();
        }
        this.connection = null;
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, password);
    }

    public ResultSet query(String query) throws SQLException {
        if (query == null) {
            return null;
        }
        this.connect();
        ResultSet results = null;
        try {
            Statement statement = this.connection.createStatement();
            results = statement.executeQuery(query);
        } catch (Exception e) {
            logger.error("There has been an error:" + e.getMessage());
            logger.error("Failed Query in MySQL using the following query input:");
            logger.error(query);
        }
        return results;
    }

    public void update(String input) throws SQLException {
        if (input == null) {
            return;
        }
        this.connect();
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate(input);
            statement.close();
        } catch (Exception e) {
            logger.error("There has been an error:" + e.getMessage());
            logger.error("Failed to update MySQL using the following update input:");
            logger.error(input);
        }
    }

    public boolean tableExists(String tableName) {
        if (tableName == null) {
            return false;
        }
        try {
            if (this.connection == null) {
                return false;
            }
            if (this.connection.getMetaData() == null) {
                return false;
            }
            ResultSet results = this.connection.getMetaData().getTables(null, null, tableName, null);
            if (results.next()) {
                return true;
            }
        } catch (Exception localException) {
            // Do Nothing
        }
        return false;
    }



    public void set(String selected, Object object, String column, String equality, String data, String table) throws SQLException {
        if (object != null) {
            object = "'" + object + "'";
        }
        if (data != null) {
            data = "'" + data + "'";
        }
        this.update("UPDATE " + table + " SET " + selected + "=" + object + " WHERE " + column + equality + data + ";");
    }

    public Object get(String selected, String column, String equality, String data, String table) throws SQLException {
        if (data != null) {
            data = "'" + data + "'";
        }
        ResultSet rs = this.query("SELECT * FROM " + table + " WHERE " + column + equality + data);
        if (rs.next()) {
            return rs.getObject(selected);
        }
        return null;
    }
}
