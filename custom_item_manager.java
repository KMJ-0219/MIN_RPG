package com.customrpg.plugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomItemManager {
    
    private CustomRPGPlugin plugin;
    private Map<String, CustomItem> customItems;
    private NamespacedKey customItemKey;
    
    public CustomItemManager(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        this.customItems = new HashMap<>();
        this.customItemKey = new NamespacedKey(plugin, "custom_item_id");
        loadCustomItems();
    }
    
    public static class CustomItem {
        public String id;
        public String name;
        public Material material;
        public List<String> lore;
        public Map<String, Double> stats;
        public double dropChance;
        
        public CustomItem(String id, String name, Material material) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.lore = new ArrayList<>();
            this.stats = new HashMap<>();
            this.dropChance = 0.1; // 기본 10% 드롭율
        }
    }
    
    public void createCustomItem(String id, String name, Material material, Map<String, Double> stats, double dropChance) {
        CustomItem item = new CustomItem(id, name, material);
        item.stats = new HashMap<>(stats);
        item.dropChance = dropChance;
        
        // 로어 생성
        item.lore.clear();
        item.lore.add(ChatColor.GRAY + "=== 커스텀 장비 ===");
        
        if (stats.containsKey("health") && stats.get("health") > 0) {
            item.lore.add(ChatColor.RED + "체력: +" + stats.get("health"));
        }
        if (stats.containsKey("damage") && stats.get("damage") > 0) {
            item.lore.add(ChatColor.DARK_RED + "공격력: +" + stats.get("damage"));
        }
        if (stats.containsKey("defense") && stats.get("defense") > 0) {
            item.lore.add(ChatColor.BLUE + "방어력: +" + stats.get("defense"));
        }
        if (stats.containsKey("speed") && stats.get("speed") > 0) {
            item.lore.add(ChatColor.YELLOW + "속도: +" + stats.get("speed"));
        }
        if (stats.containsKey("mining") && stats.get("mining") > 0) {
            item.lore.add(ChatColor.GREEN + "채굴속도: +" + stats.get("mining"));
        }
        
        item.lore.add(ChatColor.GRAY + "드롭확률: " + (dropChance * 100) + "%");
        
        customItems.put(id, item);
        saveCustomItem(item);
    }
    
    public ItemStack createItemStack(String itemId) {
        return createItemStack(itemId, false);
    }
    
    public ItemStack createItemStack(String itemId, boolean randomStats) {
        CustomItem customItem = customItems.get(itemId);
        if (customItem == null) return null;
        
        ItemStack item = new ItemStack(customItem.material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.GOLD + customItem.name);
        
        // 랜덤 스텟 적용
        Map<String, Double> finalStats = new HashMap<>();
        List<String> finalLore = new ArrayList<>();
        finalLore.add(ChatColor.GRAY + "=== 커스텀 장비 ===");
        
        if (randomStats) {
            // 랜덤 스텟 생성 (원래 스텟의 50% ~ 150% 범위)
            Random random = new Random();
            for (Map.Entry<String, Double> entry : customItem.stats.entrySet()) {
                if (entry.getValue() > 0) {
                    double minStat = entry.getValue() * 0.5;
                    double maxStat = entry.getValue() * 1.5;
                    double randomStat = minStat + (maxStat - minStat) * random.nextDouble();
                    finalStats.put(entry.getKey(), Math.round(randomStat * 100.0) / 100.0);
                }
            }
        } else {
            finalStats = new HashMap<>(customItem.stats);
        }
        
        // 로어에 스텟 추가
        if (finalStats.containsKey("health") && finalStats.get("health") > 0) {
            finalLore.add(ChatColor.RED + "체력: +" + finalStats.get("health"));
        }
        if (finalStats.containsKey("damage") && finalStats.get("damage") > 0) {
            finalLore.add(ChatColor.DARK_RED + "공격력: +" + finalStats.get("damage"));
        }
        if (finalStats.containsKey("defense") && finalStats.get("defense") > 0) {
            finalLore.add(ChatColor.BLUE + "방어력: +" + finalStats.get("defense"));
        }
        if (finalStats.containsKey("speed") && finalStats.get("speed") > 0) {
            finalLore.add(ChatColor.YELLOW + "속도: +" + finalStats.get("speed"));
        }
        if (finalStats.containsKey("mining") && finalStats.get("mining") > 0) {
            finalLore.add(ChatColor.GREEN + "채굴속도: +" + finalStats.get("mining"));
        }
        
        meta.setLore(finalLore);
        
        // 커스텀 아이템 식별자 추가
        meta.getPersistentDataContainer().set(customItemKey, PersistentDataType.STRING, itemId);
        
        // 스텟 정보를 NBT에 저장
        for (Map.Entry<String, Double> entry : finalStats.entrySet()) {
            NamespacedKey statKey = new NamespacedKey(plugin, "stat_" + entry.getKey());
            meta.getPersistentDataContainer().set(statKey, PersistentDataType.DOUBLE, entry.getValue());
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isCustomItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(customItemKey, PersistentDataType.STRING);
    }
    
    public String getCustomItemId(ItemStack item) {
        if (!isCustomItem(item)) return null;
        return item.getItemMeta().getPersistentDataContainer().get(customItemKey, PersistentDataType.STRING);
    }
    
    public Map<String, Double> getItemStats(ItemStack item) {
        Map<String, Double> stats = new HashMap<>();
        if (!isCustomItem(item)) return stats;
        
        ItemMeta meta = item.getItemMeta();
        String[] statTypes = {"health", "damage", "defense", "speed", "mining"};
        
        for (String statType : statTypes) {
            NamespacedKey statKey = new NamespacedKey(plugin, "stat_" + statType);
            if (meta.getPersistentDataContainer().has(statKey, PersistentDataType.DOUBLE)) {
                double value = meta.getPersistentDataContainer().get(statKey, PersistentDataType.DOUBLE);
                if (value > 0) {
                    stats.put(statType, value);
                }
            }
        }
        
        return stats;
    }
    
    public void giveItemToPlayer(Player player, String itemId) {
        giveItemToPlayer(player, itemId, true);
    }
    
    public void giveItemToPlayer(Player player, String itemId, boolean randomStats) {
        ItemStack item = createItemStack(itemId, randomStats);
        if (item != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }
    
    public CustomItem getCustomItem(String id) {
        return customItems.get(id);
    }
    
    public Set<String> getCustomItemIds() {
        return customItems.keySet();
    }
    
    public void removeCustomItem(String id) {
        customItems.remove(id);
        FileConfiguration config = plugin.getItemsConfig();
        config.set("items." + id, null);
        plugin.saveItemsConfig();
    }
    
    private void saveCustomItem(CustomItem item) {
        FileConfiguration config = plugin.getItemsConfig();
        String path = "items." + item.id;
        
        config.set(path + ".name", item.name);
        config.set(path + ".material", item.material.name());
        config.set(path + ".dropChance", item.dropChance);
        
        // 스텟 저장
        for (Map.Entry<String, Double> entry : item.stats.entrySet()) {
            config.set(path + ".stats." + entry.getKey(), entry.getValue());
        }
        
        plugin.saveItemsConfig();
    }
    
    private void loadCustomItems() {
        FileConfiguration config = plugin.getItemsConfig();
        if (config.contains("items")) {
            for (String id : config.getConfigurationSection("items").getKeys(false)) {
                String path = "items." + id;
                
                String name = config.getString(path + ".name", id);
                Material material = Material.valueOf(config.getString(path + ".material", "STONE"));
                double dropChance = config.getDouble(path + ".dropChance", 0.1);
                
                Map<String, Double> stats = new HashMap<>();
                if (config.contains(path + ".stats")) {
                    for (String statType : config.getConfigurationSection(path + ".stats").getKeys(false)) {
                        stats.put(statType, config.getDouble(path + ".stats." + statType));
                    }
                }
                
                createCustomItem(id, name, material, stats, dropChance);
            }
        }
    }
}