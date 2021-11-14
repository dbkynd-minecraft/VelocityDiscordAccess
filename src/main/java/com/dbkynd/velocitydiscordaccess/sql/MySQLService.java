package com.dbkynd.velocitydiscordaccess.sql;

import com.dbkynd.velocitydiscordaccess.VelocityDiscordAccess;
import com.dbkynd.velocitydiscordaccess.config.Config;
import com.moandjiezana.toml.Toml;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MySQLService {
    private static MySQLService single_instance = null;
    private static final Logger logger = VelocityDiscordAccess.logger;
    private static final Toml config = new Config().read();
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private MySQLConnection sql;
    private String tableName;

    public static MySQLService getInstance()
    {
        if (single_instance == null)
            single_instance = new MySQLService();

        return single_instance;
    }

    public void init() throws SQLException {
        df.setTimeZone(tz);
        String sqlHost = config.getString("mySQL.host");
        String sqlPort = config.getString("mySQL.port");
        String sqlDatabase = config.getString("mySQL.database");
        String sqlUsername = config.getString("mySQL.username");
        String sqlPassword = config.getString("mySQL.password");
        this.tableName = config.getString("mySQL.tablePrefix") + "vdaccess";
        this.sql = new MySQLConnection(sqlHost, sqlPort, sqlDatabase, sqlUsername, sqlPassword);
        this.sql.connect();
        logger.info("Connected to the mySQL database.");

        // Create table if it does not exist
        if (!sql.tableExists(this.tableName)) {
            logger.info(this.tableName + " table not found");
            sql.update("CREATE TABLE " + this.tableName + " (discord_id CHAR(18), minecraft_name VARCHAR(16), uuid CHAR(36), created_at DATETIME, updated_at DATETIME, PRIMARY KEY (discord_id));");
            // Ensure table was created before saying so
            if (sql.tableExists(this.tableName)) {
                logger.info(this.tableName + " table created");
            }
        }
    }

    public boolean itemExists(String column, String data) {
        if (data != null) {
            data = "'" + data + "'";
        }
        try {
            ResultSet results = sql.query("SELECT * FROM " + this.tableName + " WHERE " + column + "=" + data);
            while (results.next()) {
                if (results.getString(column) != null) {
                    return true;
                }
            }
        } catch (Exception localException) {
            // Do Nothing
        }
        return false;
    }

    public void setMinecraftName(String discordId, String minecraftName) throws SQLException {
        sql.set("minecraft_name", minecraftName, "discord_id", "=", discordId, this.tableName);
    }

    public void setUUID(String discordId, String minecraftUUID) throws SQLException {
        sql.set("uuid", minecraftUUID, "discord_id", "=", discordId, this.tableName);
    }

    public void updateTimestamp(String discordId) throws SQLException {
        String now = df.format(new Date());
        sql.set("updated_at", now, "discord_id", "=", discordId, this.tableName);

    }

    public void addNewPlayer(String discordId, String minecraftName, String minecraftUUID) throws SQLException {
        String now = df.format(new Date());
        sql.update("INSERT INTO " + this.tableName + " (discord_id,minecraft_name,uuid,created_at,updated_at) VALUES ('" + discordId + "','" + minecraftName + "','" + minecraftUUID + "','" + now+ "','" + now + "');");
    }

    public UserRecord getRegisteredPlayer(String column, String data) {
        ResultSet rs;

        if (itemExists(column, data)) {
            try {
                rs = sql.query("SELECT * FROM " + this.tableName + " HAVING " + column + " = " + "\'" + data + "\';");
                rs.next();
                String discordId = rs.getString("discord_id");
                String minecraftName = rs.getString("minecraft_name");
                String uuid = rs.getString("uuid");
                return new UserRecord(discordId, minecraftName, uuid);
            } catch (SQLException e) {
                logger.error("Error getting user record from database.");
                e.printStackTrace();
            }
        }
        return null;
    }
}
