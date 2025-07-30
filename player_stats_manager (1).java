package com.customrpg.plugin;

import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager {
    
    private CustomRPGPlugin plugin;
    private Map<UUID, PlayerStats> playerStatsMap;
    
    public PlayerStatsManager(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        this.playerStatsMap = new HashMap<>();
        loadAllPlayerData();
    }
    
    public static class PlayerStats {
        public double health = 0;      // 체력 증가
        public double damage = 0;      // 데미지 증가
        public double defense = 0;     // 방어력 증가
        public double speed = 0;       // 속도 증가
        public double mining = 0;      // 채굴속도 증가
        public int statCoins = 0;      // 스텟 코인
        public int coins = 0;          // 일반 코인
        
        public PlayerStats() {}
        
        public PlayerStats(double health, double damage, double defense, double speed, double mining, int statCoins, int coins) {
            this.health = health;
            this.damage = damage;
            this.defense = defense;
            this.speed = speed;
            this.mining = mining;
            this.statCoins = statCoins;
            this.coins = coins;
        }
    }
    
    public PlayerStats getPlayerStats(UUID playerId) {
        return playerStatsMap.computeIfAbsent(playerId, k -> new PlayerStats());
    }
    
    public PlayerStats getPlayerStats(Player player) {
        return getPlayerStats(player.getUniqueId());
    }
    
    public void addStatCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.statCoins += amount;
        savePlayerData(player);
    }
    
    public void addCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        stats.coins += amount;
        savePlayerData(player);
    }
    
    public boolean spendStatCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        if (stats.statCoins >= amount) {
            stats.statCoins -= amount;
            savePlayerData(player);
            return true;
        }
        return false;
    }
    
    public boolean spendCoins(Player player, int amount) {
        PlayerStats stats = getPlayerStats(player);
        if (stats.coins >= amount) {
            stats.coins -= amount;
            savePlayerData(player);
            return true;
        }
        return false;
    }
    
    public void addTemporaryStats(Player player, double health, double damage, double defense, double speed, double mining) {
        PlayerStats stats = getPlayerStats(player);
        stats.health += health;
        stats.damage += damage;
        stats.defense += defense;
        stats.speed += speed;
        stats.mining += mining;
        
        applyStatsToPlayer(player);
    }
    
    public void removeTemporaryStats(Player player, double health, double damage, double defense, double speed, double mining) {
        PlayerStats stats = getPlayerStats(player);
        stats.health -= health;
        stats.damage -= damage;
        stats.defense -= defense;
        stats.speed -= speed;
        stats.mining -= mining;
        
        // 음수가 되지 않도록 보정
        stats.health = Math.max(0, stats.health);
        stats.damage = Math.max(0, stats.damage);
        stats.defense = Math.max(0, stats.defense);
        stats.speed = Math.max(0, stats.speed);
        stats.mining = Math.max(0, stats.mining);
        
        applyStatsToPlayer(player);
    }
    
    public void applyStatsToPlayer(Player player) {
        PlayerStats stats = getPlayerStats(player);
        
        // 체력 적용
        if (stats.health > 0) {
            double baseHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth + stats.health);
        }
        
        // 속도 적용
        if (stats.speed > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 
                (int) Math.min(stats.speed / 20, 10), false, false), true);
        }
        
        // 채굴속도 적용
        if (stats.mining > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 
                (int) Math.min(stats.mining / 20, 10), false, false), true);
        }
    }
    
    public void resetPlayerStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        
        // 기본값으로 되돌리기
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        
        // 임시 스텟 초기화 (코인은 유지)
        stats.health = 0;
        stats.damage = 0;
        stats.defense = 0;
        stats.speed = 0;
        stats.mining = 0;
    }
    
    public void savePlayerData(Player player) {
        PlayerStats stats = getPlayerStats(player);
        FileConfiguration config = plugin.getPlayersConfig();
        String path = "players." + player.getUniqueId().toString();
        
        config.set(path + ".health", stats.health);
        config.set(path + ".damage", stats.damage);
        config.set(path + ".defense", stats.defense);
        config.set(path + ".speed", stats.speed);
        config.set(path + ".mining", stats.mining);
        config.set(path + ".statCoins", stats.statCoins);
        config.set(path + ".coins", stats.coins);
        config.set(path + ".name", player.getName());
        
        plugin.savePlayersConfig();
    }
    
    public void loadPlayerData(Player player) {
        FileConfiguration config = plugin.getPlayersConfig();
        String path = "players." + player.getUniqueId().toString();
        
        if (config.contains(path)) {
            PlayerStats stats = new PlayerStats(
                config.getDouble(path + ".health", 0),
                config.getDouble(path + ".damage", 0),
                config.getDouble(path + ".defense", 0),
                config.getDouble(path + ".speed", 0),
                config.getDouble(path + ".mining", 0),
                config.getInt(path + ".statCoins", 0),
                config.getInt(path + ".coins", 0)
            );
            playerStatsMap.put(player.getUniqueId(), stats);
        }
    }
    
    public void loadAllPlayerData() {
        FileConfiguration config = plugin.getPlayersConfig();
        if (config.contains("players")) {
            for (String uuidString : config.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String path = "players." + uuidString;
                    
                    PlayerStats stats = new PlayerStats(
                        config.getDouble(path + ".health", 0),
                        config.getDouble(path + ".damage", 0),
                        config.getDouble(path + ".defense", 0),
                        config.getDouble(path + ".speed", 0),
                        config.getDouble(path + ".mining", 0),
                        config.getInt(path + ".statCoins", 0),
                        config.getInt(path + ".coins", 0)
                    );
                    playerStatsMap.put(uuid, stats);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("잘못된 UUID 형식: " + uuidString);
                }
            }
        }
    }
    
    public void saveAllPlayerData() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player);
        }
    }
}