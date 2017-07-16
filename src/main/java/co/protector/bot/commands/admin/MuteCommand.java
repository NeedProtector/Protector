package co.protector.bot.commands.admin;

import co.protector.bot.core.Database;
import co.protector.bot.core.listener.command.Command;
import co.protector.bot.util.Emoji;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.bson.Document;

import java.util.List;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class MuteCommand extends Command {

    @Override
    public String getTrigger() {
        return "mute";
    }

    @Override
    public String[] getAliases() {
        return new String[]{};
    }

    @Override
    public String getDescription() {
        return "Mutes a user, disabling there ability to speak in the server";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    private boolean canManageRole(Member author) {
        return PermissionUtil.checkPermission(author.getGuild().getSelfMember(), Permission.MANAGE_ROLES)
                &&
                PermissionUtil.canInteract(author, author.getGuild().getSelfMember());
    }

    private boolean checks(Message trigger) {
        if (!PermissionUtil.checkPermission(trigger.getMember(), Permission.KICK_MEMBERS)) {
            trigger.getChannel().sendMessage(Emoji.REDX + " **You require the `Kick Members` permission in order to mute**").queue();
            return false;
        }
        if (!canManageRole(trigger.getMember())) {
            trigger.getChannel().sendMessage(Emoji.REDX + " **I cannot assign this user roles**").queue();
            return false;
        }
        return true;
    }

    @Override
    public void execute(Message trigger, String args) {
        List<User> mentions = trigger.getMentionedUsers();
        if (mentions.isEmpty()) {
            trigger.getChannel().sendMessage(Emoji.REDX + " **You must mention a user you want to mute**").queue();
            return;
        }
        Guild guild = trigger.getGuild();
        Member muting = guild.getMember(mentions.get(0));
        if(muting.getUser().getId().equals(trigger.getJDA().getSelfUser().getId())) {
            trigger.getChannel().sendMessage(Emoji.EYES + " **Thats pretty rude!**").queue();
            return;
        }
        if (!checks(trigger)) return;
        String id = Database.getMutedRole(guild.getId());
        if (muting.getRoles().stream().anyMatch(role -> role.getId().equals(id))) {
            trigger.getChannel().sendMessage(Emoji.WARN +
                    " **User is already muted in this server, use the `unmmute` command in order to unmute**").queue();
            return;
        }
        if (id == null) {
            trigger.getChannel().sendMessage("**Muted role not found, configuring.**").queue();
            Role role = createRole(guild);
            setMutedRole(role.getId(), guild.getId());
            assignMutedRole(role, guild, trigger.getChannel(), muting);
        } else {
            Role role = guild.getRoleById(id);
            if (role == null) {
                trigger.getChannel().sendMessage(Emoji.WARN + " **Muted role not found, configuring.**").queue();
                role = createRole(guild);
                setMutedRole(role.getId(), guild.getId());
            }
            assignMutedRole(role, guild, trigger.getChannel(), muting);
        }
    }

    private void assignMutedRole(Role role, Guild guild, MessageChannel channel, Member member) {
        guild.getController().addRolesToMember(member, role).queue();
        channel.sendMessage(Emoji.GREEN_TICK + " **Muted** `" + member.getEffectiveName() + "`").queue();
    }

    private void setMutedRole(String id, String guildid) {
        Database.saveConfigField("mute",
                new Document().append("role", id), guildid);
    }

    private Role createRole(Guild guild) {
        Role role = guild.getController().createRole().setName("Muted").complete();
        guild.getTextChannels().forEach(textChannel ->
                textChannel.createPermissionOverride(role).setDeny(Permission.MESSAGE_WRITE).queue());
        return role;
    }

}