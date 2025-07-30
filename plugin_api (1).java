package com.customrpg.plugin.api;

import com.customrpg.plugin.CustomRPGPlugin;
import com.customrpg.plugin.PlayerStatsManager;
import com.customrpg.plugin.CustomItemManager;
import com.customrpg.plugin.CustomMonsterManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * CustomRPG 플러그인 API
 * 다른 플러그인에서 CustomRPG 기능을 사용할 수 있도록 제공하는 API
 * 
 * @version 1.0.0
 * @author YourName
 */
public class CustomRPGAPI {
    
    private static CustomRPGPlugin plugin;
    
    /**
     * API 초기화 (플러그인 내부에서만 호출)
     */
    public static void initialize(CustomRPGPlugin pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * 플러그인 인스턴스 가져오기
     */
    public static CustomRPGPlugin getPlugin() {
        return plugin;
    }
    
    // ==================== 플레이어 스텟 관련 API ====================
    
    /**
     * 플레이어의 현재 스텟 정보 조회
     * @param player 대상 플레이어
     * @return PlayerStats 객체
     */
    public static PlayerStatsManager.PlayerStats getPlayerStats(Player player) {
        return plugin.getStatsManager().getPlayerStats(player);
    }
    
    /**
     * 플레이어에게 스텟코인 지급
     * @param player 대상 플레이어  
     * @param amount 지급할 스텟코인 양
     */
    public static void addStatCoins(Player player, int amount) {
        plugin.getStatsManager().addStatCoins(player, amount);
    }
    
    /**
     * 플레이어에게 일반코인 지급
     * @param player 대상 플레이어
     * @param amount 지급할 코인 양
     */
    public static void addCoins(Player player, int amount) {
        plugin.getStatsManager().addCoins(player, amount);
    }
    
    /**
     * 플레이어의 스텟코인 소모
     * @param player 대상 플레이어
     * @param amount 소모할 스텟코인 양
     * @return 성공 여부
     */
    public static boolean spendStatCoins(Player player, int amount) {
        return plugin.getStatsManager().spendStatCoins(player, amount);
    }
    
    /**
     * 플레이어의 일반코인 소모
     * @param player 대상 플레이어
     * @param amount 소모할 코인 양
     * @return 성공 여부
     */
    public static boolean spendCoins(Player player, int amount) {
        return plugin.getStatsManager().spendCoins(player, amount);
    }
    
    /**
     * 플레이어에게 임시 스텟 적용
     * @param player 대상 플레이어
     * @param health 체력 증가량
     * @param damage 공격력 증가량
     * @param defense 방어력 증가량
     * @param speed 속도 증가량
     * @param mining 채굴속도 증가량
     */
    public static void addTemporaryStats(Player player, double health, double damage, 
                                       double defense, double speed, double mining) {
        plugin.getStatsManager().addTemporaryStats(player, health, damage, defense, speed, mining);
    }
    
    /**
     * 플레이어의 임시 스텟 제거
     * @param player 대상 플레이어
     * @param health 체력 감소량
     * @param damage 공격력 감소량
     * @param defense 방어력 감소량
     * @param speed 속도 감소량
     * @param mining 채굴속도 감소량
     */
    public static void removeTemporaryStats(Player player, double health, double damage,
                                          double defense, double speed, double mining) {
        plugin.getStatsManager().removeTemporaryStats(player, health, damage, defense, speed, mining);
    }
    
    // ==================== 커스텀 아이템 관련 API ====================
    
    /**
     * 커스텀 아이템 생성
     * @param id 아이템 ID
     * @param name 아이템 이름
     * @param material 아이템 재료
     * @param stats 스텟 맵 (health, damage, defense, speed, mining)
     * @param dropChance 드롭 확률 (0.0 ~ 1.0)
     */
    public static void createCustomItem(String id, String name, org.bukkit.Material material, 
                                      Map<String, Double> stats, double dropChance) {
        plugin.getItemManager().createCustomItem(id, name, material, stats, dropChance);
    }
    
    /**
     * 커스텀 아이템 스택 생성
     * @param itemId 아이템 ID
     * @param randomStats 랜덤 스텟 적용 여부
     * @return ItemStack 객체 (null if 아이템이 존재하지 않음)
     */
    public static ItemStack createItemStack(String itemId, boolean randomStats) {
        return plugin.getItemManager().createItemStack(itemId, randomStats);
    }
    
    /**
     * 아이템이 커스텀 아이템인지 확인
     * @param item 확인할 아이템
     * @return 커스텀 아이템 여부
     */
    public static boolean isCustomItem(ItemStack item) {
        return plugin.getItemManager().isCustomItem(item);
    }
    
    /**
     * 커스텀 아이템의 ID 조회
     * @param item 아이템 스택
     * @return 커스텀 아이템 ID (null if 커스텀 아이템이 아님)
     */
    public static String getCustomItemId(ItemStack item) {
        return plugin.getItemManager().getCustomItemId(item);
    }
    
    /**
     * 아이템의 스텟 정보 조회
     * @param item 아이템 스택
     * @return 스텟 맵
     */
    public static Map<String, Double> getItemStats(ItemStack item) {
        return plugin.getItemManager().getItemStats(item);
    }
    
    /**
     * 플레이어에게 커스텀 아이템 지급
     * @param player 대상 플레이어
     * @param itemId 아이템 ID
     * @param randomStats 랜덤 스텟 적용 여부
     */
    public static void giveItemToPlayer(Player player, String itemId, boolean randomStats) {
        plugin.getItemManager().giveItemToPlayer(player, itemId, randomStats);
    }
    
    /**
     * 등록된 모든 커스텀 아이템 ID 조회
     * @return 아이템 ID 집합
     */
    public static Set<String> getCustomItemIds() {
        return plugin.getItemManager().getCustomItemIds();
    }
    
    // ==================== 커스텀 몬스터 관련 API ====================
    
    /**
     * 커스텀 몬스터 생성
     * @param id 몬스터 ID
     * @param name 몬스터 이름
     * @param entityType 엔티티 타입
     * @param baseHealth 기본 체력
     * @param baseDamage 기본 공격력
     * @param minLevel 최소 레벨
     * @param maxLevel 최대 레벨
     * @param statCoinReward 스텟코인 보상
     * @param coinReward 일반코인 보상
     * @param dropItems 드롭 아이템 리스트
     * @param dropChances 드롭 확률 맵
     */
    public static void createCustomMonster(String id, String name, org.bukkit.entity.EntityType entityType,
                                         double baseHealth, double baseDamage, int minLevel, int maxLevel,
                                         int statCoinReward, int coinReward, java.util.List<String> dropItems,
                                         Map<String, Double> dropChances) {
        plugin.getMonsterManager().createCustomMonster(id, name, entityType, baseHealth, baseDamage,
                minLevel, maxLevel, statCoinReward, coinReward, dropItems, dropChances);
    }
    
    /**
     * 커스텀 몬스터 스폰
     * @param monsterId 몬스터 ID
     * @param location 스폰 위치
     * @param level 몬스터 레벨
     * @return 스폰된 몬스터 엔티티 (null if 실패)
     */
    public static LivingEntity spawnCustomMonster(String monsterId, Location location, int level) {
        return plugin.getMonsterManager().spawnCustomMonster(monsterId, location, level);
    }
    
    /**
     * 엔티티가 커스텀 몬스터인지 확인
     * @param entity 확인할 엔티티
     * @return 커스텀 몬스터 여부
     */
    public static boolean isCustomMonster(org.bukkit.entity.Entity entity) {
        return plugin.getMonsterManager().isCustomMonster(entity);
    }
    
    /**
     * 커스텀 몬스터의 ID 조회
     * @param entity 몬스터 엔티티
     * @return 커스텀 몬스터 ID (null if 커스텀 몬스터가 아님)
     */
    public static String getCustomMonsterId(org.bukkit.entity.Entity entity) {
        return plugin.getMonsterManager().getCustomMonsterId(entity);
    }
    
    /**
     * 커스텀 몬스터의 레벨 조회
     * @param entity 몬스터 엔티티
     * @return 몬스터 레벨
     */
    public static int getMonsterLevel(org.bukkit.entity.Entity entity) {
        return plugin.getMonsterManager().getMonsterLevel(entity);
    }
    
    /**
     * 스폰 지역 생성
     * @param name 지역 이름
     * @param corner1 첫 번째 모서리
     * @param corner2 두 번째 모서리
     * @param minLevel 최소 레벨
     * @param maxLevel 최대 레벨
     * @param monsterIds 스폰할 몬스터 ID 리스트
     * @param maxMonstersPerRegion 지역당 최대 몬스터 수
     */
    public static void createSpawnRegion(String name, Location corner1, Location corner2,
                                       int minLevel, int maxLevel, java.util.List<String> monsterIds,
                                       int maxMonstersPerRegion) {
        plugin.getMonsterManager().createSpawnRegion(name, corner1, corner2, minLevel, maxLevel,
                monsterIds, maxMonstersPerRegion);
    }
    
    /**
     * 등록된 모든 커스텀 몬스터 ID 조회
     * @return 몬스터 ID 집합
     */
    public static Set<String> getCustomMonsterIds() {
        return plugin.getMonsterManager().getCustomMonsterIds();
    }
    
    // ==================== RPG 월드 관련 API ====================
    
    /**
     * RPG 월드 가져오기
     * @return RPG 월드 (null if 존재하지 않음)
     */
    public static World getRPGWorld() {
        return plugin.getWorldManager().getRPGWorld();
    }
    
    /**
     * 플레이어를 RPG 월드로 이동
     * @param player 대상 플레이어
     * @return 이동 성공 여부
     */
    public static boolean teleportToRPGWorld(Player player) {
        return plugin.getWorldManager().teleportToRPGWorld(player);
    }
    
    /**
     * 해당 월드가 RPG 월드인지 확인
     * @param world 확인할 월드
     * @return RPG 월드 여부
     */
    public static boolean isRPGWorld(World world) {
        return plugin.getWorldManager().isRPGWorld(world);
    }
    
    // ==================== 플러그인 상태 관리 API ====================
    
    /**
     * 플러그인 활성화 상태 확인
     * @return 활성화 여부
     */
    public static boolean isPluginEnabled() {
        return plugin.isPluginEnabled();
    }
    
    /**
     * 플러그인 활성화/비활성화 설정
     * @param enabled 활성화 여부
     */
    public static void setPluginEnabled(boolean enabled) {
        plugin.setPluginEnabled(enabled);
    }
    
    // ==================== 이벤트 API ====================
    
    /**
     * CustomRPG 이벤트 처리를 위한 이벤트 클래스들
     */
    public static class Events {
        
        /**
         * 플레이어 스텟 변경 이벤트
         */
        public static class PlayerStatsChangeEvent extends org.bukkit.event.Event {
            private static final org.bukkit.event.HandlerList handlers = new org.bukkit.event.HandlerList();
            
            private final Player player;
            private final PlayerStatsManager.PlayerStats oldStats;
            private final PlayerStatsManager.PlayerStats newStats;
            
            public PlayerStatsChangeEvent(Player player, PlayerStatsManager.PlayerStats oldStats, 
                                        PlayerStatsManager.PlayerStats newStats) {
                this.player = player;
                this.oldStats = oldStats;
                this.newStats = newStats;
            }
            
            public Player getPlayer() { return player; }
            public PlayerStatsManager.PlayerStats getOldStats() { return oldStats; }
            public PlayerStatsManager.PlayerStats getNewStats() { return newStats; }
            
            @Override
            public org.bukkit.event.HandlerList getHandlers() { return handlers; }
            public static org.bukkit.event.HandlerList getHandlerList() { return handlers; }
        }
        
        /**
         * 커스텀 몬스터 스폰 이벤트
         */
        public static class CustomMonsterSpawnEvent extends org.bukkit.event.entity.EntitySpawnEvent {
            private final String monsterId;
            private final int level;
            
            public CustomMonsterSpawnEvent(LivingEntity entity, String monsterId, int level) {
                super(entity);
                this.monsterId = monsterId;
                this.level = level;
            }
            
            public String getMonsterId() { return monsterId; }
            public int getLevel() { return level; }
        }
        
        /**
         * 커스텀 몬스터 사망 이벤트
         */
        public static class CustomMonsterDeathEvent extends org.bukkit.event.entity.EntityDeathEvent {
            private final String monsterId;
            private final int level;
            private final int statCoinReward;
            private final int coinReward;
            
            public CustomMonsterDeathEvent(LivingEntity entity, java.util.List<ItemStack> drops,
                                         int droppedExp, String monsterId, int level,
                                         int statCoinReward, int coinReward) {
                super(entity, drops, droppedExp);
                this.monsterId = monsterId;
                this.level = level;
                this.statCoinReward = statCoinReward;
                this.coinReward = coinReward;
            }
            
            public String getMonsterId() { return monsterId; }
            public int getLevel() { return level; }
            public int getStatCoinReward() { return statCoinReward; }
            public int getCoinReward() { return coinReward; }
        }
    }
    
    // ==================== 유틸리티 API ====================
    
    /**
     * 스텟 타입을 한글로 변환
     * @param statType 스텟 타입 (영문)
     * @return 한글 스텟 이름
     */
    public static String getStatDisplayName(String statType) {
        switch (statType.toLowerCase()) {
            case "health": return "체력";
            case "damage": return "공격력";
            case "defense": return "방어력";
            case "speed": return "속도";
            case "mining": return "채굴속도";
            default: return statType;
        }
    }
    
    /**
     * 레벨에 따른 스텟 배율 계산
     * @param baseValue 기본값
     * @param level 레벨
     * @param multiplierPerLevel 레벨당 배율 (기본: 0.2 = 20%)
     * @return 계산된 스텟값
     */
    public static double calculateLevelScaledStat(double baseValue, int level, double multiplierPerLevel) {
        return baseValue * (1.0 + (level - 1) * multiplierPerLevel);
    }
    
    /**
     * 랜덤 스텟 범위 계산
     * @param baseStat 기본 스텟
     * @param minMultiplier 최소 배율 (기본: 0.5)
     * @param maxMultiplier 최대 배율 (기본: 1.5)
     * @return 랜덤 스텟값
     */
    public static double calculateRandomStat(double baseStat, double minMultiplier, double maxMultiplier) {
        java.util.Random random = new java.util.Random();
        double multiplier = minMultiplier + (maxMultiplier - minMultiplier) * random.nextDouble();
        return Math.round(baseStat * multiplier * 100.0) / 100.0;
    }
    
    // ==================== 설정 관리 API ====================
    
    /**
     * 설정값 조회
     * @param path 설정 경로
     * @param defaultValue 기본값
     * @return 설정값
     */
    public static <T> T getConfigValue(String path, T defaultValue) {
        Object value = plugin.getConfig().get(path, defaultValue);
        try {
            return (T) value;
        } catch (ClassCastException e) {
            plugin.getLogger().warning("설정 타입 불일치: " + path + " (기본값 사용)");
            return defaultValue;
        }
    }
    
    /**
     * 설정값 저장
     * @param path 설정 경로
     * @param value 저장할 값
     */
    public static void setConfigValue(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }
    
    // ==================== 캐시 관리 API ====================
    
    /**
     * 플레이어 데이터 캐시 새로고침
     * @param player 대상 플레이어
     */
    public static void refreshPlayerCache(Player player) {
        plugin.getStatsManager().loadPlayerData(player);
        plugin.getStatsManager().applyStatsToPlayer(player);
    }
    
    /**
     * 모든 플레이어 데이터 저장
     */
    public static void saveAllPlayerData() {
        plugin.getStatsManager().saveAllPlayerData();
    }
    
    // ==================== 디버그 및 모니터링 API ====================
    
    /**
     * 플러그인 통계 정보 조회
     * @return 통계 맵
     */
    public static Map<String, Object> getPluginStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("customItems", plugin.getItemManager().getCustomItemIds().size());
        stats.put("customMonsters", plugin.getMonsterManager().getCustomMonsterIds().size());
        stats.put("spawnRegions", plugin.getMonsterManager().getSpawnRegionNames().size());
        stats.put("onlinePlayers", plugin.getServer().getOnlinePlayers().size());
        stats.put("pluginEnabled", plugin.isPluginEnabled());
        return stats;
    }
    
    /**
     * 성능 정보 조회 (메모리 사용량 등)
     * @return 성능 정보 맵
     */
    public static Map<String, Object> getPerformanceInfo() {
        Map<String, Object> info = new java.util.HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        info.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024); // MB
        info.put("freeMemory", runtime.freeMemory() / 1024 / 1024); // MB
        info.put("totalMemory", runtime.totalMemory() / 1024 / 1024); // MB
        info.put("maxMemory", runtime.maxMemory() / 1024 / 1024); // MB
        
        return info;
    }
}