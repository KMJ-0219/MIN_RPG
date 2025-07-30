package com.customrpg.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class CustomMonsterManager {
    
    private CustomRPGPlugin plugin;
    private Map<String, CustomMonster> customMonsters;
    private Map<String, SpawnRegion> spawnRegions;
    private NamespacedKey customMonsterKey;
    private NamespacedKey monsterLevelKey;
    
    public CustomMonsterManager(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        this.customMonsters = new HashMap<>();
        this.spawnRegions = new HashMap<>();
        this.customMonsterKey = new NamespacedKey(plugin, "custom_monster_id");
        this.monsterLevelKey = new NamespacedKey(plugin, "monster_level");
        loadCustomMonsters();
        startMonsterSpawning();
    }
    
    public static class CustomMonster {
        public String id;
        public String name;
        public EntityType entityType;
        public double baseHealth;
        public double baseDamage;
        public int minLevel;
        public int maxLevel;
        public int statCoinReward;
        public int coinReward;
        public List<String> dropItems;
        public Map<String, Double> dropChances;
        
        public CustomMonster(String id, String name, EntityType entityType) {
            this.id = id;
            this.name = name;
            this.entityType = entityType;
            this.baseHealth = 20.0;
            this.baseDamage = 2.0;
            this.minLevel = 1;
            this.maxLevel = 10;
            this.statCoinReward = 1;
            this.coinReward = 10;
            this.dropItems = new ArrayList<>();
            this.dropChances = new HashMap<>();
        }
    }
    
    public static class SpawnRegion {
        public String name;
        public Location corner1;
        public Location corner2;
        public int minLevel;
        public int maxLevel;
        public List<String> monsterIds;
        public int maxMonstersPerRegion;
        public long spawnInterval; // 틱 단위
        
        public SpawnRegion(String name, Location corner1, Location corner2) {
            this.name = name;
            this.corner1 = corner1;
            this.corner2 = corner2;
            this.minLevel = 1;
            this.maxLevel = 10;
            this.monsterIds = new ArrayList<>();
            this.maxMonstersPerRegion = 10;
            this.spawnInterval = 200L; // 10초
        }
        
        public boolean contains(Location location) {
            double minX = Math.min(corner1.getX(), corner2.getX());
            double maxX = Math.max(corner1.getX(), corner2.getX());
            double minZ = Math.min(corner1.getZ(), corner2.getZ());
            double maxZ = Math.max(corner1.getZ(), corner2.getZ());
            double minY = Math.min(corner1.getY(), corner2.getY());
            double maxY = Math.max(corner1.getY(), corner2.getY());
            
            return location.getX() >= minX && location.getX() <= maxX &&
                   location.getZ() >= minZ && location.getZ() <= maxZ &&
                   location.getY() >= minY && location.getY() <= maxY;
        }
        
        public Location getRandomLocation() {
            Random random = new Random();
            double x = Math.min(corner1.getX(), corner2.getX()) + 
                      random.nextDouble() * Math.abs(corner1.getX() - corner2.getX());
            double z = Math.min(corner1.getZ(), corner2.getZ()) + 
                      random.nextDouble() * Math.abs(corner1.getZ() - corner2.getZ());
            double y = Math.max(corner1.getY(), corner2.getY());
            
            return new Location(corner1.getWorld(), x, y, z);
        }
    }
    
    public void createCustomMonster(String id, String name, EntityType entityType, double baseHealth, 
                                  double baseDamage, int minLevel, int maxLevel, int statCoinReward, 
                                  int coinReward, List<String> dropItems, Map<String, Double> dropChances) {
        CustomMonster monster = new CustomMonster(id, name, entityType);
        monster.baseHealth = baseHealth;
        monster.baseDamage = baseDamage;
        monster.minLevel = minLevel;
        monster.maxLevel = maxLevel;
        monster.statCoinReward = statCoinReward;
        monster.coinReward = coinReward;
        monster.dropItems = new ArrayList<>(dropItems);
        monster.dropChances = new HashMap<>(dropChances);
        
        customMonsters.put(id, monster);
        saveCustomMonster(monster);
    }
    
    public void createSpawnRegion(String name, Location corner1, Location corner2, int minLevel, 
                                int maxLevel, List<String> monsterIds, int maxMonstersPerRegion) {
        SpawnRegion region = new SpawnRegion(name, corner1, corner2);
        region.minLevel = minLevel;
        region.maxLevel = maxLevel;
        region.monsterIds = new ArrayList<>(monsterIds);
        region.maxMonstersPerRegion = maxMonstersPerRegion;
        
        spawnRegions.put(name, region);
        saveSpawnRegion(region);
    }
    
    public LivingEntity spawnCustomMonster(String monsterId, Location location, int level) {
        CustomMonster customMonster = customMonsters.get(monsterId);
        if (customMonster == null) return null;
        
        // 레벨 제한 확인
        level = Math.max(customMonster.minLevel, Math.min(level, customMonster.maxLevel));
        
        // 몬스터 스폰
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, customMonster.entityType);
        
        // 커스텀 이름 설정
        entity.setCustomName(ChatColor.RED + customMonster.name + ChatColor.YELLOW + " [Lv." + level + "]");
        entity.setCustomNameVisible(true);
        
        // 레벨에 따른 스텟 조정
        double healthMultiplier = 1.0 + (level - 1) * 0.2; // 레벨당 20% 증가
        double damageMultiplier = 1.0 + (level - 1) * 0.15; // 레벨당 15% 증가
        
        double finalHealth = customMonster.baseHealth * healthMultiplier;
        double finalDamage = customMonster.baseDamage * damageMultiplier;
        
        // 체력 설정
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalHealth);
        entity.setHealth(finalHealth);
        
        // 공격력 설정
        if (entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(finalDamage);
        }
        
        // 커스텀 몬스터 식별자와 레벨 저장
        entity.getPersistentDataContainer().set(customMonsterKey, PersistentDataType.STRING, monsterId);
        entity.getPersistentDataContainer().set(monsterLevelKey, PersistentDataType.INTEGER, level);
        
        return entity;
    }
    
    public void handleMonsterDeath(LivingEntity entity, Player killer) {
        if (!isCustomMonster(entity)) return;
        
        String monsterId = getCustomMonsterId(entity);
        int level = getMonsterLevel(entity);
        CustomMonster monster = customMonsters.get(monsterId);
        
        if (monster == null || killer == null) return;
        
        // 보상 지급
        int statCoinReward = monster.statCoinReward + (level - 1);
        int coinReward = monster.coinReward + (level - 1) * 5;
        
        plugin.getStatsManager().addStatCoins(killer, statCoinReward);
        plugin.getStatsManager().addCoins(killer, coinReward);
        
        // 드롭 아이템 처리
        Random random = new Random();
        for (String itemId : monster.dropItems) {
            double dropChance = monster.dropChances.getOrDefault(itemId, 0.1);
            if (random.nextDouble() < dropChance) {
                plugin.getItemManager().giveItemToPlayer(killer, itemId, true);
            }
        }
        
        // 메시지 전송
        killer.sendMessage(ChatColor.GREEN + monster.name + " [Lv." + level + "]을(를) 처치했습니다!");
        killer.sendMessage(ChatColor.YELLOW + "스텟 코인 +" + statCoinReward + ", 코인 +" + coinReward);
    }
    
    public boolean isCustomMonster(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return entity.getPersistentDataContainer().has(customMonsterKey, PersistentDataType.STRING);
    }
    
    public String getCustomMonsterId(Entity entity) {
        if (!isCustomMonster(entity)) return null;
        return entity.getPersistentDataContainer().get(customMonsterKey, PersistentDataType.STRING);
    }
    
    public int getMonsterLevel(Entity entity) {
        if (!isCustomMonster(entity)) return 1;
        return entity.getPersistentDataContainer().get(monsterLevelKey, PersistentDataType.INTEGER);
    }
    
    private void startMonsterSpawning() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!plugin.isPluginEnabled()) return;
            
            for (SpawnRegion region : spawnRegions.values()) {
                // 현재 지역의 몬스터 수 확인
                int currentMonsters = countMonstersInRegion(region);
                
                if (currentMonsters < region.maxMonstersPerRegion && !region.monsterIds.isEmpty()) {
                    // 랜덤 위치에 몬스터 스폰
                    Location spawnLocation = region.getRandomLocation();
                    
                    // 랜덤 몬스터 선택
                    String monsterId = region.monsterIds.get(new Random().nextInt(region.monsterIds.size()));
                    
                    // 랜덤 레벨 설정
                    int level = region.minLevel + new Random().nextInt(region.maxLevel - region.minLevel + 1);
                    
                    spawnCustomMonster(monsterId, spawnLocation, level);
                }
            }
        }, 200L, 200L); // 10초마다 실행
    }
    
    private int countMonstersInRegion(SpawnRegion region) {
        int count = 0;
        for (Entity entity : region.corner1.getWorld().getEntities()) {
            if (isCustomMonster(entity) && region.contains(entity.getLocation())) {
                count++;
            }
        }
        return count;
    }
    
    public CustomMonster getCustomMonster(String id) {
        return customMonsters.get(id);
    }
    
    public SpawnRegion getSpawnRegion(String name) {
        return spawnRegions.get(name);
    }
    
    public Set<String> getCustomMonsterIds() {
        return customMonsters.keySet();
    }
    
    public Set<String> getSpawnRegionNames() {
        return spawnRegions.keySet();
    }
    
    public void removeCustomMonster(String id) {
        customMonsters.remove(id);
        FileConfiguration config = plugin.getMonstersConfig();
        config.set("monsters." + id, null);
        plugin.saveMonstersConfig();
    }
    
    public void removeSpawnRegion(String name) {
        spawnRegions.remove(name);
        FileConfiguration config = plugin.getMonstersConfig();
        config.set("regions." + name, null);
        plugin.saveMonstersConfig();
    }
    
    private void saveCustomMonster(CustomMonster monster) {
        FileConfiguration config = plugin.getMonstersConfig();
        String path = "monsters." + monster.id;
        
        config.set(path + ".name", monster.name);
        config.set(path + ".entityType", monster.entityType.name());
        config.set(path + ".baseHealth", monster.baseHealth);
        config.set(path + ".baseDamage", monster.baseDamage);
        config.set(path + ".minLevel", monster.minLevel);
        config.set(path + ".maxLevel", monster.maxLevel);
        config.set(path + ".statCoinReward", monster.statCoinReward);
        config.set(path + ".coinReward", monster.coinReward);
        config.set(path + ".dropItems", monster.dropItems);
        
        // 드롭 확률 저장
        for (Map.Entry<String, Double> entry : monster.dropChances.entrySet()) {
            config.set(path + ".dropChances." + entry.getKey(), entry.getValue());
        }
        
        plugin.saveMonstersConfig();
    }
    
    private void saveSpawnRegion(SpawnRegion region) {
        FileConfiguration config = plugin.getMonstersConfig();
        String path = "regions." + region.name;
        
        config.set(path + ".corner1.world", region.corner1.getWorld().getName());
        config.set(path + ".corner1.x", region.corner1.getX());
        config.set(path + ".corner1.y", region.corner1.getY());
        config.set(path + ".corner1.z", region.corner1.getZ());
        config.set(path + ".corner2.world", region.corner2.getWorld().getName());
        config.set(path + ".corner2.x", region.corner2.getX());
        config.set(path + ".corner2.y", region.corner2.getY());
        config.set(path + ".corner2.z", region.corner2.getZ());
        config.set(path + ".minLevel", region.minLevel);
        config.set(path + ".maxLevel", region.maxLevel);
        config.set(path + ".monsterIds", region.monsterIds);
        config.set(path + ".maxMonstersPerRegion", region.maxMonstersPerRegion);
        
        plugin.saveMonstersConfig();
    }
    
    private void loadCustomMonsters() {
        FileConfiguration config = plugin.getMonstersConfig();
        
        // 커스텀 몬스터 로드
        if (config.contains("monsters")) {
            for (String id : config.getConfigurationSection("monsters").getKeys(false)) {
                String path = "monsters." + id;
                
                String name = config.getString(path + ".name", id);
                EntityType entityType = EntityType.valueOf(config.getString(path + ".entityType", "ZOMBIE"));
                double baseHealth = config.getDouble(path + ".baseHealth", 20.0);
                double baseDamage = config.getDouble(path + ".baseDamage", 2.0);
                int minLevel = config.getInt(path + ".minLevel", 1);
                int maxLevel = config.getInt(path + ".maxLevel", 10);
                int statCoinReward = config.getInt(path + ".statCoinReward", 1);
                int coinReward = config.getInt(path + ".coinReward", 10);
                List<String> dropItems = config.getStringList(path + ".dropItems");
                
                Map<String, Double> dropChances = new HashMap<>();
                if (config.contains(path + ".dropChances")) {
                    for (String itemId : config.getConfigurationSection(path + ".dropChances").getKeys(false)) {
                        dropChances.put(itemId, config.getDouble(path + ".dropChances." + itemId));
                    }
                }
                
                createCustomMonster(id, name, entityType, baseHealth, baseDamage, minLevel, maxLevel,
                                  statCoinReward, coinReward, dropItems, dropChances);
            }
        }
        
        // 스폰 지역 로드
        if (config.contains("regions")) {
            for (String name : config.getConfigurationSection("regions").getKeys(false)) {
                String path = "regions." + name;
                
                String worldName = config.getString(path + ".corner1.world");
                if (plugin.getServer().getWorld(worldName) == null) continue;
                
                Location corner1 = new Location(
                    plugin.getServer().getWorld(worldName),
                    config.getDouble(path + ".corner1.x"),
                    config.getDouble(path + ".corner1.y"),
                    config.getDouble(path + ".corner1.z")
                );
                
                Location corner2 = new Location(
                    plugin.getServer().getWorld(worldName),
                    config.getDouble(path + ".corner2.x"),
                    config.getDouble(path + ".corner2.y"),
                    config.getDouble(path + ".corner2.z")
                );
                
                int minLevel = config.getInt(path + ".minLevel", 1);
                int maxLevel = config.getInt(path + ".maxLevel", 10);
                List<String> monsterIds = config.getStringList(path + ".monsterIds");
                int maxMonstersPerRegion = config.getInt(path + ".maxMonstersPerRegion", 10);
                
                createSpawnRegion(name, corner1, corner2, minLevel, maxLevel, monsterIds, maxMonstersPerRegion);
            }
        }
    }