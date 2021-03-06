package com.enjin.bukkit.shop.gui;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.compat.MaterialResolver;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.bukkit.util.ui.MenuItem;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryList extends Menu {
    private final Map<MenuItem, Menu> lists = new HashMap<>();

    public CategoryList(Menu parent, Shop shop) {
        super(ChatColor.GOLD + shop.getName()
                                   .substring(0, shop.getName().length() >= 30 ? 30 : shop.getName().length()), 6);

        init(parent, shop, shop.getCategories());
    }

    public CategoryList(Menu parent, Shop shop, Category category) {
        super(ChatColor.GOLD + category.getName()
                                       .substring(0,
                                                  category.getName().length() >= 30 ? 30 : category.getName().length()),
              6);

        init(parent, shop, category.getCategories());
    }

    private void init(final Menu parent, final Shop shop, final List<Category> categories) {
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
        for (final Category category : categories) {
            String    name  = ChatColor.translateAlternateColorCodes('&',
                                                                     "&" + shop.getColorId()) + (i + 1) + ". " + ChatColor
                    .translateAlternateColorCodes('&',
                                                  "&" + shop.getColorName()) + category.getName();
            ItemStack stack = MaterialResolver.createItemStack(category.getIconItem(),
                                                               category.getIconDamage() != null ? category.getIconDamage() : 0);

            if (stack == null) {
                stack = new ItemStack(Material.CHEST);
            }

            MenuItem menuItem = new MenuItem(name.substring(0, name.length() >= 32 ? 32 : name.length()), stack) {
                @Override
                public void onClick(Player player) {
                    if (!lists.containsKey(this)) {
                        lists.put(this,
                                  category.getCategories() != null && !category.getCategories()
                                                                               .isEmpty() ? new CategoryList(
                                          CategoryList.this,
                                          shop,
                                          category) : new ItemList(CategoryList.this, shop, category));
                    }

                    switchMenu(EnjinMinecraftPlugin.getInstance().getMenuAPI(), player, lists.get(this));
                    ShopListener.getGuiInstances().put(player.getUniqueId(), lists.get(this));
                }
            };

            menuItem.setDescriptions(TextUtils.splitToListWithPrefix(category.getInfo(),
                                                                     30,
                                                                     ChatColor.translateAlternateColorCodes('&',
                                                                                                            "&" + shop.getColorInfo())));
            addMenuItem(menuItem, i++ + 9);
        }
    }
}
