package com.enjin.bukkit.cmd.arg;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerArgumentProcessor extends AbstractArgumentProcessor<Player> {

    public static final PlayerArgumentProcessor INSTANCE = new PlayerArgumentProcessor();

    @Override
    public List<String> tab(CommandSender sender, String arg) {
        String lowerCaseArg = arg.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> p.getName().toLowerCase())
                .filter(name -> name.startsWith(lowerCaseArg))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Player> parse(CommandSender sender, String arg) {
        Optional<Player> result = Optional.empty();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(arg)
                    || player.getUniqueId().toString().equalsIgnoreCase(arg)
                    || player.getUniqueId().toString().replace("-", "").equalsIgnoreCase(arg)) {
                result = Optional.of(player);
                break;
            }
        }

        return result;
    }

}
