package com.enjin.bukkit.shop.gui;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.compat.MaterialResolver;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.bukkit.util.ui.MenuItem;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemList extends Menu {
    private static DecimalFormat       priceFormat = new DecimalFormat("#.00");
    private final  Map<MenuItem, Menu> lists       = new HashMap<>();

    public ItemList(Menu parent, Shop shop, Category category) {
        super(ChatColor.GOLD + category.getName()
                                       .substring(0,
                                                  category.getName().length() >= 30 ? 30 : category.getName().length()),
              6);

        init(parent, shop, category.getItems());
    }

    private void init(final Menu parent, final Shop shop, final List<Item> items) {
        MenuItem back = new MenuItem(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "Back",
                                     Material.ARROW) {
            @Override
            public void onClick(Player player) {
                if (parent != null) {
                    switchMenu(EnjinMinecraftPlugin.getInstance().getMenuAPI(), player, parent);
                    ShopListener.getGuiInstances().put(player.getUniqueId(), parent);
                }
            }
        };
        addMenuItem(back, 0);

        int i = 0;
        for (final Item item : items) {
            Material material = Material.PAPER;

            for (Material mat : Material.values()) {
                if (mat.name().toLowerCase().equalsIgnoreCase(item.getIconItem())) {
                    material = mat;
                }
            }

            String    name  = ChatColor.translateAlternateColorCodes('&',
                                                                     "&" + shop.getColorId()) + (i + 1) + ". " + ChatColor
                    .translateAlternateColorCodes('&',
                                                  "&" + shop.getColorName()) + item.getName();
            ItemStack stack = MaterialResolver.createItemStack(item.getIconItem(),
                                                               item.getIconDamage() != null ? item.getIconDamage() : 0);

            if (stack == null) {
                stack = new ItemStack(Material.PAPER);
            }

            MenuItem menuItem = new MenuItem(name.substring(0, name.length() >= 32 ? 32 : name.length()), stack) {
                @Override
                public void onClick(Player player) {
                    if (!lists.containsKey(this)) {
                        lists.put(this, new ItemDetail(ItemList.this, shop, item));
                    }

                    switchMenu(EnjinMinecraftPlugin.getInstance().getMenuAPI(), player, lists.get(this));
                }
            };

            List<String> descriptions = new ArrayList<>();
            if (item.getPrice() != null) {
                descriptions.add(ChatColor.translateAlternateColorCodes('&',
                                                                        "&" + shop.getColorText()) + "PRICE: " + ChatColor
                        .translateAlternateColorCodes('&',
                                                      "&" + shop.getColorPrice()) + (item.getPrice() == 0.0 ? "FREE" : priceFormat
                        .format(item.getPrice()) + " " + shop.getCurrency()));
            }

            if (item.getPoints() != null) {
                descriptions.add(ChatColor.translateAlternateColorCodes('&',
                                                                        "&" + shop.getColorText()) + "POINTS: " + ChatColor
                        .translateAlternateColorCodes('&',
                                                      "&" + shop.getColorPrice()) + (item.getPoints() == 0 ? "FREE" : item
                        .getPoints()));
            }

            descriptions.addAll(TextUtils.splitToListWithPrefix(item.getInfo(),
                                                                30,
                                                                ChatColor.translateAlternateColorCodes('&',
                                                                                                       "&" + shop.getColorInfo())));
            menuItem.setDescriptions(descriptions);

            addMenuItem(menuItem, i++ + 9);
        }
    }
}
