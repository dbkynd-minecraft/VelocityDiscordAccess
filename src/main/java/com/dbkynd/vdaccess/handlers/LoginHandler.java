package com.dbkynd.vdaccess.handlers;

import com.dbkynd.vdaccess.VDAccess;
import com.dbkynd.vdaccess.config.Config;
import com.dbkynd.vdaccess.discord.Discord;
import com.dbkynd.vdaccess.sql.MySQLService;
import com.dbkynd.vdaccess.sql.UserRecord;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LoginHandler {
    private static final Toml config = new Config().read();
    private static final MySQLService sql = MySQLService.getInstance();
    private static final Logger logger = VDAccess.logger;

    static String kickMessage = config.getString("Discord.Messages.kickMessage");
    static List<String> allowedRoles = new ArrayList<>(config.getList("Discord.allowedRoleIds"));

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();
        String firstChar = username.substring(0,1);
        String name = firstChar.equals(".") ? username.substring(1) : username;

        if (VDAccess.server.getPlayer(name).isPresent() || VDAccess.server.getPlayer("." + name).isPresent()) {
            logger.info("[" + username + "] Join denied.");
            TextComponent reason = Component.text("The user ").color(NamedTextColor.GOLD)
                    .append(Component.text(name).color(NamedTextColor.AQUA))
                            .append(Component.text(" is already logged into the server.").color(NamedTextColor.GOLD));
            event.setResult(ResultedEvent.ComponentResult.denied(reason));
            return;
        }

        // Allow join if player has bypass perms
        if (player.hasPermission("vdaccess.bypass") || player.hasPermission("vdaccess.*")) {
            logger.info("[" + username + "] Join allowed. Has bypass permissions.");
            return;
        }

        // Allow join if no roles were explicitly set to be required
        if (allowedRoles.size() == 0) {
            logger.info("[" + username + "] Join allowed. No roles have been explicitly required.");
            return;
        }


        // Allow if we have a user record with at least one of the required roles
        UserRecord userRecord = sql.getRegisteredPlayer("minecraft_name", name);
        if (userRecord != null) {
            if (Discord.hasRole(userRecord.getDiscordId(), username)) {
                return;
            }
        }

        // Kick the user
        logger.info("[" + username + "] Join denied.");
        TextComponent reason = Component.text(kickMessage).color(NamedTextColor.GOLD);
        event.setResult(ResultedEvent.ComponentResult.denied(reason));
    }
}
