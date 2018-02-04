package me.andrew28.morestorage.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xXAndrew28Xx
 */
public class Messages {
    private File file;
    private Map<String, Object> messages = new HashMap<>();

    public Messages(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void load() {
        Configuration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(true)) {
            messages.put(key, config.get(key));
        }
    }

    public String getMessage(String key) {
        return (String) messages.get(key);
    }

    public String[] getMessages(String key) {
        return (String[]) messages.get(key);
    }

    public void send(CommandSender sender, String key, Replacer... replacers) {
        Object message = messages.get(key);
        if (message == null) {
            return;
        }
        if (message instanceof String) {
            String string = (String) message;
            string = ChatColor.translateAlternateColorCodes('&', string);
            if (replacers != null) {
                for (Replacer replacer : replacers) {
                    string = replacer.replace(string);
                }
            }
            sender.sendMessage(string);
        } else if (message instanceof List) {
            List<String> strings = (List<String>) message;
            strings = strings.stream().map(string -> {
                string = ChatColor.translateAlternateColorCodes('&', string);
                for (Replacer replacer : replacers) {
                    string = replacer.replace(string);
                }
                return string;
            }).collect(Collectors.toList());
            sender.sendMessage(strings.toArray(new String[strings.size()]));
        }
    }

    public static class Replacer {
        private String needle, replacement;

        public Replacer(String needle, String replacement) {
            this.needle = needle;
            this.replacement = replacement;
        }

        String replace(String haystack) {
            return haystack.replace(needle, replacement);
        }
    }
}
