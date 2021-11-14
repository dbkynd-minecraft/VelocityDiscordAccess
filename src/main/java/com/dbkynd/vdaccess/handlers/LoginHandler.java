package com.dbkynd.vdaccess.handlers;

import com.dbkynd.vdaccess.VDAccess;
import com.dbkynd.vdaccess.config.Config;
import com.dbkynd.vdaccess.discord.Discord;
import com.dbkynd.vdaccess.permission.LuckPerm;
import com.dbkynd.vdaccess.sql.MySQLService;
import com.dbkynd.vdaccess.sql.UserRecord;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginHandler {
    private static final Toml config = new Config().read();
    private static final MySQLService sql = MySQLService.getInstance();
    private static final Logger logger = VDAccess.logger;

    static String kickMessage = config.getString("Discord.Messages.kickMessage");
    static List<String> allowedRoles = new ArrayList<>(config.getList("Discord.allowedRoleIds"));

    @Subscribe
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String username = event.getPlayer().getUsername();

        // Allow join if player has bypass perms
        if (LuckPerm.hasPermissions(uuid)) {
            logger.info("[" + username + "] Join allowed. Has luckperm bypass permissions.");
            return;
        }

        // Allow join if no roles were explicitly set to be required
        if (allowedRoles.size() == 0) {
            logger.info("[" + username + "] Join allowed. No roles have been explicitly required.");
            return;
        }

        // Allow if we have a user record with at least one of the required roles
        UserRecord userRecord = sql.getRegisteredPlayer("uuid", uuid.toString());
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
