package cz.wake.sussi.commands.user;

import cz.wake.sussi.Sussi;
import cz.wake.sussi.commands.CommandType;
import cz.wake.sussi.commands.ISlashCommand;
import cz.wake.sussi.commands.Rank;
import cz.wake.sussi.utils.Constants;
import cz.wake.sussi.utils.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class RoomSlashCommand implements ISlashCommand {

    @Override
    public void onSlashCommand(User sender, MessageChannel channel, Member member, InteractionHook hook, SlashCommandEvent event) {

        String subcommandName = event.getSubcommandName();

        if (subcommandName == null) {
            return;
        }

        VoiceChannel voiceChannel = member.getGuild().getVoiceChannelById(Sussi.getInstance().getSql().getPlayerVoiceRoomIdByOwnerId(sender.getIdLong()));

        if (voiceChannel == null) {
            hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Nenacházíš se v žádném z voice kanálů. Připoj se prvně do **Vytvořit voice kanál**.").build()).queue();
            return;
        }

        switch (subcommandName) {
            case "help":
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GRAY).setTitle("Nápověda k příkazu ,room")
                        .setDescription("`,room` - Zobrazí informace o místnosti.\n" +
                                "`/room lock` - Uzamkne místnost.\n" +
                                "`/room unlock` - Odemkne místnost.\n" +
                                "`/room add @uživatel` - Přidá uživatele do místnosti.\n" +
                                "`/room remove @uživatel` - Odebere uživatele z místnosti.\n" +
                                "`/room ban @uživatel` - Zabanuje uživatele v místnosti + skryje.\n" +
                                "`/room unban @uživatel` - Odbanuje uživatele v místnosti.\n" +
                                "`/room name [text]` - Nastaví název místnosti.\n" +
                                "`/room limit [číslo]` - Nastaví limit místnosti.\n" +
                                "`/room unlimited` - Nastaví neomezený počet připojení\n" +
                                "`/room bitrate [číslo v kbps]` - Nastaví bitrate v místnosti.").build()).queue();
                break;
            case "lock":
                voiceChannel.putPermissionOverride(member.getGuild().getPublicRole()).setDeny(Permission.VOICE_CONNECT).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GRAY).setDescription(":lock: | Místnost **" + voiceChannel.getName() + "** byla uzamknuta.").build()).queue();
                break;
            case "unlock":
                voiceChannel.putPermissionOverride(member.getGuild().getPublicRole()).setAllow(Permission.VOICE_CONNECT).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GREEN).setDescription(":unlock: | Místnost ** " + voiceChannel.getName() + "** byla odemknuta.").build()).queue();
                break;
            case "add":
                Member toAdd = event.getOption("user").getAsMember();
                if (toAdd == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadaný uživatel neexistuje, chyba Discordu?!").build()).queue();
                    return;
                }
                if (toAdd.getId().equals(event.getMember().getId())) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Sám sebe pozvat nemůžeš, už tam jsi.").build()).queue();
                    return;
                }
                voiceChannel.getManager().getChannel().putPermissionOverride(toAdd).setAllow(Permission.VOICE_CONNECT).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GREEN).setDescription(Constants.GREEN_MARK + " | Uživatel " + toAdd.getAsMention() + " byl přidán do voice").build()).queue();
                break;
            case "remove":
                Member toRemove = event.getOption("user").getAsMember();
                if (toRemove == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadaný uživatel neexistuje, chyba Discordu?!").build()).queue();
                    return;
                }
                if (toRemove.getId().equals(event.getMember().getId())) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Sám sebe odebrat nemůžeš, pokud chceš místnost smazat -> odpoj se.").build()).queue();
                    return;
                }
                voiceChannel.getManager().getChannel().putPermissionOverride(toRemove).setDeny(Permission.VOICE_CONNECT).queue();
                hook.sendMessageEmbeds(MessageUtils
                        .getEmbed(Constants.GREEN).setDescription(Constants.GREEN_MARK + " | Uživatel " + toRemove.getAsMention() + " byl odebrán z kanálu").build()).queue();
            case "name":
                String channelNewName = event.getOption("text").getAsString();
                if (channelNewName == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadan text nelze použít pro název kanálu!").build()).queue();
                    return;
                }
                voiceChannel.getManager().setName(channelNewName).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GRAY).setDescription(":bookmark: | Název kanálu byl změněn na ** " + channelNewName + "**").build()).queue();
                break;
            case "bitrate":
                Long selectedBitrate = event.getOption("value").getAsLong();
                if (selectedBitrate == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Byl zadán chybně bitrate, zkus to znova!").build()).queue();
                    return;
                }
                if (selectedBitrate < 8 || selectedBitrate > 384) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Bitrate kanálu může být nastaven pouze od 8kbps do 384kbps.").build()).queue();
                    break;
                }
                voiceChannel.getManager().setBitrate((int) (selectedBitrate * 1000)).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GRAY).setDescription(":headphones: | Bitrate byl změněn na **" + selectedBitrate + "kbps**").build()).queue();
                break;
            case "unlimited":
                voiceChannel.getManager().setUserLimit(0).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GREEN).setDescription(":memo: | Limit byl změněn na **neomezeně**").build()).queue();
                break;
            case "limit":
                Long selectedLimit = event.getOption("value").getAsLong();
                if (selectedLimit == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Byl zadán chybně limit, zkus to znova!").build()).queue();
                    return;
                }
                voiceChannel.getManager().setUserLimit(Math.toIntExact(selectedLimit)).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GREEN).setDescription(":memo: | Limit byl změněn na **" + selectedLimit + "**").build()).queue();
                break;
            case "ban":
                Member toBan = event.getOption("user").getAsMember();
                if (toBan == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadaný uživatel neexistuje, chyba Discordu?!").build()).queue();
                    return;
                }
                if (toBan.getId().equals(event.getMember().getId())) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Sám sebe zabanovat nemůžeš!").build()).queue();
                    return;
                }
                if (toBan.getVoiceState().inVoiceChannel() && toBan.getVoiceState().getChannel().getId().equals(voiceChannel.getId())) {
                    voiceChannel.getGuild().kickVoiceMember(toBan).queue();
                }
                voiceChannel.getManager().getChannel().putPermissionOverride(toBan).setDeny(Permission.VIEW_CHANNEL).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.ADMIN).setDescription(":hammer: | Uživatel " + toBan.getAsMention()  + " byl zabanován v kanálu.").build()).queue();
                break;
            case "unban":
                Member toUnban = event.getOption("user").getAsMember();
                if (toUnban == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadaný uživatel neexistuje, chyba Discordu?!").build()).queue();
                    return;
                }
                if (toUnban.getId().equals(event.getMember().getId())) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Sám sebe odbanovat nemůžeš!").build()).queue();
                    return;
                }
                voiceChannel.getManager().getChannel().putPermissionOverride(toUnban).setAllow(Permission.VIEW_CHANNEL).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GRAY).setDescription(":hammer_pick: | Uživatel " + toUnban.getAsMention()  + " byl odbanován z kanálu, nyní se může připojit.").build()).queue();
                break;
            case "kick":
                Member toKick = event.getOption("user").getAsMember();
                if (toKick == null) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Zadaný uživatel neexistuje, chyba Discordu?!").build()).queue();
                    return;
                }
                if (toKick.getId().equals(event.getMember().getId())) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Sám sebe vyhodit nemůžeš, jednoduše se odpoj.").build()).queue();
                    return;
                }
                if (!voiceChannel.getMembers().contains(toKick)) {
                    hook.sendMessageEmbeds(MessageUtils.getEmbedError().setDescription("Uživatel není v místnosti připojený, nelze ho vyhodit.").build()).queue();
                    return;
                }
                voiceChannel.getGuild().kickVoiceMember(toKick).queue();
                hook.sendMessageEmbeds(MessageUtils.getEmbed(Constants.GREEN).setDescription(Constants.DELETE + " Uživatel " + toKick.getAsMention() + " byl vykopnut.").build()).queue();
                break;
        }
    }

    @Override
    public String getName() {
        return "room";
    }

    @Override
    public String getDescription() {
        return "Správa voice mistností.";
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public Rank getRank() {
        return Rank.USER;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }
}
