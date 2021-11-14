package com.dbkynd.velocitydiscordaccess;

import com.dbkynd.velocitydiscordaccess.config.Config;
import com.dbkynd.velocitydiscordaccess.discord.Discord;
import com.dbkynd.velocitydiscordaccess.handlers.LoginHandler;
import com.dbkynd.velocitydiscordaccess.sql.MySQLService;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "velocitydiscordaccess",
        name = "Velocity Discord Access",
        version = BuildConstants.VERSION,
        authors = {"DBKynd"},
        dependencies = {
                @Dependency(id = "luckperms")
        }
)
public class VelocityDiscordAccess {

    public static ProxyServer server;
    public static Logger logger;
    public static Path dataDirectory;

    @Inject
    public void VelocityInit(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        VelocityDiscordAccess.logger = logger;
        VelocityDiscordAccess.dataDirectory = dataDirectory;
        VelocityDiscordAccess.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            new Config().init();
            Discord.main(null);
            MySQLService.getInstance().init();
        } catch (Exception error) {
            logger.error(error.getMessage());
            logger.error("Unable to process allow lists.");
            return;
        }

        server.getEventManager().register(this, new LoginHandler());
        logger.info("Ready to process allow lists.");
    }
}
