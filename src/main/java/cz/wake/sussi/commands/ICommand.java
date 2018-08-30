package cz.wake.sussi.commands;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public interface ICommand {

    void onCommand(User sender, MessageChannel channel, Message message, String[] args, Member member, EventWaiter w);

    String getCommand();

    String getDescription();

    String getHelp();

    CommandType getType();

    Rank getRank();

    default String[] getAliases() {
        return new String[]{};
    }

    default boolean deleteMessage() {
        return false;
    }
}
