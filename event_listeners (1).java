package com.customrpg.plugin;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.ChatColor;

import java.util.Map;

// 플레이어 이벤트 리스너
class PlayerEventListener implements Listener {
    private CustomRPGPlugin plugin;
    
    public PlayerEventListener(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().loadPlayerData(player);
        plugin.getStatsManager().applyStatsToPlayer(player);
        
        // 장착된 아이템의 스텟 적용
        updatePlayerItemStats(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().savePlayerData(player);
        plugin.getStatsManager().resetPlayerStats(player);
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        // 0.1초 후에 스텟 업데이트 (아이템 변경이 완료된 후)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerItemStats(player);
        }, 2L);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        // 인벤토리 변경 후 스텟 업데이트
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerItemStats(player);
        }, 1L);
    }
    
    private void updatePlayerItemStats(Player player) {
        if (!plugin.isPluginEnabled()) return;
        
        // 기존 아이템 스텟 초기화
        plugin.getStatsManager().resetPlayerStats(player);
        
        PlayerInventory inventory = player.getInventory();
        double totalHealth = 0, totalDamage = 0, totalDefense = 0, totalSpeed = 0, totalMining = 0;
        
        // 손에 든 아이템 확인
        ItemStack mainHand = inventory.getItemInMainHand();
        if (plugin.getItemManager().isCustomItem(mainHand)) {
            Map<String, Double> stats = plugin.getItemManager().getItemStats(mainHand);
            totalHealth += stats.getOrDefault("health", 0.0);
            totalDamage += stats.getOrDefault("damage", 0.0);
            totalDefense += stats.getOrDefault("defense", 0.0);
            totalSpeed += stats.getOrDefault("speed", 0.0);
            totalMining += stats.getOrDefault("mining", 0.0);
        }
        
        // 착용한 방어구 확인
        ItemStack[] armorContents = inventory.getArmorContents();
        for (ItemStack armor : armorContents) {
            if (plugin.getItemManager().isCustomItem(armor)) {
                Map<String, Double> stats = plugin.getItemManager().getItemStats(armor);
                totalHealth += stats.getOrDefault("health", 0.0);
                totalDamage += stats.getOrDefault("damage", 0.0);
                totalDefense += stats.getOrDefault("defense", 0.0);
                totalSpeed += stats.getOrDefault("speed", 0.0);
                totalMining += stats.getOrDefault("mining", 0.0);
            }
        }
        
        // 스텟 적용
        if (totalHealth > 0 || totalDamage > 0 || totalDefense > 0 || totalSpeed > 0 || totalMining > 0) {
            plugin.getStatsManager().addTemporaryStats(player, totalHealth, totalDamage, totalDefense, totalSpeed, totalMining);
        }
    }
}

// 몬스터 이벤트 리스너
class MonsterEventListener implements Listener {
    private CustomRPGPlugin plugin;
    
    public MonsterEventListener(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.isPluginEnabled()) return;
        
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        
        if (killer != null && plugin.getMonsterManager().isCustomMonster(entity)) {
            plugin.getMonsterManager().handleMonsterDeath(entity, killer);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.isPluginEnabled()) return;
        
        // 플레이어가 공격할 때 추가 데미지 적용
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            PlayerStatsManager.PlayerStats stats = plugin.getStatsManager().getPlayerStats(player);
            
            if (stats.damage > 0) {
                event.setDamage(event.getDamage() + stats.damage);
            }
        }
        
        // 플레이어가 피해를 받을 때 방어력 적용
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerStatsManager.PlayerStats stats = plugin.getStatsManager().getPlayerStats(player);
            
            if (stats.defense > 0) {
                double reducedDamage = event.getDamage() * (1.0 - (stats.defense / (stats.defense + 100)));
                event.setDamage(Math.max(0, reducedDamage));
            }
        }
    }
}

// 아이템 이벤트 리스너
class ItemEventListener implements Listener {
    private CustomRPGPlugin plugin;
    
    public ItemEventListener(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isPluginEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        ItemStack item = event.getCurrentItem();
        if (plugin.getItemManager().isCustomItem(item)) {
            // 커스텀 아이템 클릭 시 정보 표시
            Player player = (Player) event.getWhoClicked();
            if (event.getClick().isShiftClick()) {
                showItemInfo(player, item);
            }
        }
    }
    
    private void showItemInfo(Player player, ItemStack item) {
        String itemId = plugin.getItemManager().getCustomItemId(item);
        Map<String, Double> stats = plugin.getItemManager().getItemStats(item);
        
        player.sendMessage(ChatColor.GOLD + "=== 아이템 정보 ===");
        player.sendMessage(ChatColor.YELLOW + "ID: " + itemId);
        
        if (!stats.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "스텟:");
            for (Map.Entry<String, Double> entry : stats.entrySet()) {
                String statName = getStatDisplayName(entry.getKey());
                player.sendMessage(ChatColor.WHITE + "  " + statName + ": +" + entry.getValue());
            }
        }
    }
    
    private String getStatDisplayName(String statKey) {
        switch (statKey) {
            case "health": return "체력";
            case "damage": return "공격력";
            case "defense": return "방어력";
            case "speed": return "속도";
            case "mining": return "채굴속도";
            default: return statKey;
        }
    }
}