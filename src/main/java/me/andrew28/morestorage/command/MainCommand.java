package me.andrew28.morestorage.command;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The /morestorage command
 *
 * @author xXAndrew28Xx
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    private MoreStorage moreStorage;
    private Messages messages;

    public MainCommand(MoreStorage moreStorage) {
        this.moreStorage = moreStorage;
        this.messages = moreStorage.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages.Replacer labelReplacer = new Messages.Replacer("%label", label);
        if (args.length == 0 || args[0].equals("help")) {
            messages.send(sender, "commands.main.help", labelReplacer,
                    new Messages.Replacer("%version", moreStorage.getDescription().getVersion()));
            return true;
        }

        String mainArgument = args[0];
        switch (mainArgument) {
            case "reload":
                try {
                    moreStorage.reloadAll();
                    messages.send(sender, "commands.main.reload.success");
                } catch (Exception e) {
                    e.printStackTrace();
                    messages.send(sender, "commands.main.reload.fail");
                }
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("help", "reload");
            List<String> completions = new ArrayList<>();
            if (args[0] == null || args[0].isEmpty()) {
                completions = options;
            } else {
                StringUtil.copyPartialMatches(args[0], options, completions);
            }
            return completions;
        }
        return null;
    }
}
