package com.dbkynd.velocitydiscordaccess.discord;

import com.dbkynd.velocitydiscordaccess.VelocityDiscordAccess;
import com.dbkynd.velocitydiscordaccess.config.Config;
import com.moandjiezana.toml.Toml;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Discord extends ListenerAdapter {
    private static final Logger logger = VelocityDiscordAccess.logger;
    private static final Toml config = new Config().read();
    private static JDA jda;

    static String addCommand = config.getString("Discord.addCommandName");
    static String botToken = config.getString("Discord.botToken");
    static List<String> allowedRoles = new ArrayList<>(config.getList("Discord.allowedRoleIds"));

    public static void main(String[] args) throws LoginException {
        logger.info("Connecting to Discord...");

        jda = JDABuilder.createLight(botToken)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new Discord())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                new CommandData(addCommand, "Add yourself to the Minecraft Sub Server allow list.")
                        .addOptions(new OptionData(STRING, "ign", "Your Minecraft in-game username.")
                                .setRequired(true))
        );

        commands.queue();

        String link = "https://discordapp.com/oauth2/authorize?permissions=0&scope=applications.commands%20bot&client_id=" + jda.getSelfUser().getId();

        logger.info("Discord connection successful!");
        logger.info("You can add this bot to Discord using this link: " + link);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getGuild() == null) return;
        if (event.getName().equals(addCommand)) {
            AddCommandHandler.action(event, event.getOption("ign").getAsString());
        }
    }

    public static boolean hasRole(String discordId, String minecraftUsername) {
        for (Guild guild : jda.getGuilds()) {
            Member member = guild.retrieveMemberById(discordId).complete();
            if (member == null) continue;
            for (Role memberRole : member.getRoles()) {
                if (allowedRoles.contains(memberRole.getId())) {
                    logger.info("[" + minecraftUsername + "] Join allowed. " + member.getUser().getAsTag() + " (" + member.getId() + ") has the role: " + memberRole.getName() + " (" + memberRole.getId() + ").");
                    return true;
                }
            }
        }

        return false;
    }
}
