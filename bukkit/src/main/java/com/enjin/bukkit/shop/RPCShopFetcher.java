package com.enjin.bukkit.shop;

import com.enjin.bukkit.command.commands.BuyCommand;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.OptionalUtil;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import com.google.common.base.Optional;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RPCShopFetcher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private UUID uuid;

    public RPCShopFetcher(Player player) {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        Optional<Player> p = OptionalUtil.getPlayer(uuid);

        if (!p.isPresent()) {
            plugin.debug("Player is not present. No longer fetching shop data.");
            return;
        }

        final Player player = p.get();
        RPCData<List<Shop>> data = EnjinServices.getService(ShopService.class).get(player.getName());

        if (data == null) {
            player.spigot().sendMessage(new ComponentBuilder("Failed to fetch shop data.").color(ChatColor.RED).create());
            return;
        }

        if (data.getError() != null) {
            player.spigot().sendMessage(new ComponentBuilder(data.getError().getMessage()).create());
            return;
        }

        List<Shop> shops = data.getResult();

        if (shops == null || shops.isEmpty()) {
            player.spigot().sendMessage(new ComponentBuilder("There are no shops available at this time.").color(ChatColor.RED).create());
            return;
        }

        if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
            PlayerShopInstance.getInstances().put(player.getUniqueId(), new PlayerShopInstance(shops));
        } else {
            PlayerShopInstance.getInstances().get(player.getUniqueId()).update(shops);
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());

        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    BuyCommand.buy(player, new String[]{});
                }
            }, 0);
        } else {
            TextShopUtil.sendTextShop(player, instance, -1);
        }
    }
}
