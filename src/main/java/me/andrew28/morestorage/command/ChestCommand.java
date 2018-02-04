package me.andrew28.morestorage.command;

import me.andrew28.morestorage.ChestsLoader;
import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The /customchest command
 *
 * @author xXAndrew28Xx
 */
public class ChestCommand implements CommandExecutor, TabCompleter {
    private MoreStorage moreStorage;
    private ChestsLoader chestsLoader;
    private Messages messages;

    public ChestCommand(MoreStorage moreStorage) {
        this.moreStorage = moreStorage;
        this.chestsLoader = moreStorage.getChestsLoader();
        this.messages = moreStorage.getMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages.Replacer labelReplacer = new Messages.Replacer("%label", label);
        if (args.length == 0 || args[0].equals("help")) {
            // Help Menu
            messages.send(sender, "commands.chest.help", labelReplacer);
            return true;
        }
        String mainArgument = args[0];
        switch (mainArgument) {
            case "list":
                // List Heading
                messages.send(sender, "commands.chest.list.header");
                for (CustomChestInfo info : chestsLoader.getLastLoadedChests()) {
                    messages.send(sender, "commands.chest.list.listing",
                            new Messages.Replacer("%name", info.getName()),
                            new Messages.Replacer("%id", info.getId()));
                }
                messages.send(sender, "commands.chest.list.footer");
                break;
            case "give":
            case "get":
                if (args.length < 2) {
                    messages.send(sender, "commands.chest.give.errors.usage", labelReplacer);
                    break;
                }

                String id = args[1];
                Optional<CustomChestInfo> infoOptional = chestsLoader.getCustomChestInfoById(id);
                if (!infoOptional.isPresent()) {
                    messages.send(sender, "commands.chest.give.errors.chest-not-found",
                            labelReplacer,
                            new Messages.Replacer("%id", id));
                    break;
                }
                CustomChestInfo info = infoOptional.get();

                int amount = 1;
                if (args.length >= 3) {
                    String amountString = args[2];
                    if (!amountString.matches("\\d+")) {
                        messages.send(sender, "commands.chest.give.errors.invalid-amount",
                                new Messages.Replacer("%amount", amountString));
                        break;
                    }
                    amount = Integer.valueOf(amountString);
                }

                Player player = sender instanceof Player ? (Player) sender : null;
                if (args.length >= 4) {
                    String name = args[3];
                    player = Bukkit.getPlayerExact(name);
                    if (player == null) {
                        messages.send(sender, "commands.chest.give.errors.player-not-found",
                                new Messages.Replacer("%player", name));
                        break;
                    }
                }
                if (player == null) {
                    messages.send(sender, "commands.chest.give.errors.player-needed");
                    break;
                }

                ItemStack itemStack = info.getItemStack();
                itemStack.setAmount(amount);
                player.getInventory().addItem(itemStack);

                Messages.Replacer amountReplacer = new Messages.Replacer("%amount", Integer.toString(amount));
                Messages.Replacer idReplacer = new Messages.Replacer("%id", id);
                Messages.Replacer chestReplacer = new Messages.Replacer("%chest", info.getName());

                if (sender instanceof Player && player.equals(sender)) {
                    messages.send(sender, "commands.chest.give.self", amountReplacer, idReplacer, chestReplacer);
                } else {
                    messages.send(sender, "commands.chest.give.sender", amountReplacer, idReplacer, chestReplacer,
                            new Messages.Replacer("%player", player.getName()));
                    messages.send(sender, "commands.chest.give.recipient", amountReplacer, idReplacer,
                            chestReplacer, new Messages.Replacer("%sender", player.getName()));
                }
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> possible = Arrays.asList("help", "list", "give", "get");
            if (args[0] == null || args[0].isEmpty()) {
                completions = possible;
            } else {
                StringUtil.copyPartialMatches(args[0], possible, completions);
            }
            return completions;
        }

        String mainArgument = args[0];
        switch (mainArgument) {
            case "give":
            case "get":
                if (args.length == 2) {
                    List<String> ids = chestsLoader.getLastLoadedChests()
                            .stream()
                            .map(CustomChestInfo::getId)
                            .collect(Collectors.toList());
                    if (args[1] == null || args[1].isEmpty()) {
                        completions = ids;
                    } else {
                        StringUtil.copyPartialMatches(args[1], ids, completions);
                    }
                } else if (args.length == 3) {
                    int max = 64;
                    Optional<CustomChestInfo> infoOptional = chestsLoader.getCustomChestInfoById(args[1]);
                    if (infoOptional.isPresent()) {
                        max = infoOptional.get().getItemStack().getMaxStackSize();
                    }

                    List<String> numbers = IntStream.range(1, max)
                            .mapToObj(number -> String.format("%02d", number))
                            .collect(Collectors.toList());
                    if (args[2] == null || args[2].isEmpty()) {
                        completions = numbers;
                    } else {
                        StringUtil.copyPartialMatches(args[1], numbers, completions);
                    }
                } else if (args.length == 4) {
                    List<String> names = moreStorage.getServer().getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    if (args[3] == null || args[3].isEmpty()) {
                        completions = names;
                    } else {
                        StringUtil.copyPartialMatches(args[3], names, completions);
                    }
                }
                break;
        }
        return completions;
    }
}
