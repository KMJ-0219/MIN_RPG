package com.customrpg.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 성능 최적화된 몬스터 스포너
 * - 청크 로드 상태 확인
 * - 플레이어 근처에서만 스폰
 * - 메모리 효율적인 몬스터 추적
 */
public class OptimizedMonsterSpawner {
    
    private CustomRPGPlugin plugin;
    private Map<String, SpawnTask> activeTasks;
    private Map<String, Set<UUID>> regionMonsters; // 지역별 몬스터 UUID 추적
    private BukkitTask cleanupTask;
    
    // 성능 설정
    private static final int SPAWN_CHECK_INTERVAL = 100; // 5초 (20틱 = 1초)
    private static final int CLEANUP_INTERVAL = 1200; // 1분
    private static final double PLAYER_DETECTION_RANGE = 64.0; // 플레이어 감지 범위
    private static final int MAX_SPAWN_ATTEMPTS = 5; // 최대 스폰 시도 횟수
    
    public OptimizedMonsterSpawner(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        this.activeTasks = new ConcurrentHashMap<>();
        this.regionMonsters = new ConcurrentHashMap<>();
        startCleanupTask();
    }
    
    /**
     * 스폰 지역 활성화
     */
    public void activateRegion(String regionName) {
        if (activeTasks.containsKey(regionName)) {
            return; // 이미 활성화됨
        }
        
        CustomMonsterManager.SpawnRegion region = plugin.getMonsterManager().getSpawnRegion(regionName);
        if (region == null) {
            plugin.getLogger().warning("존재하지 않는 스폰 지역: " + regionName);
            return;
        }
        
        regionMonsters.put(regionName, ConcurrentHashMap.newKeySet());
        
        SpawnTask task = new SpawnTask(regionName, region);
        task.runTaskTimer(plugin, 20L, SPAWN_CHECK_INTERVAL); // 1초 후 시작, 5초마다 실행
        
        activeTasks.put(regionName, task);
        plugin.getLogger().info("스폰 지역 활성화됨: " + regionName);
    }
    
    /**
     * 스폰 지역 비활성화
     */
    public void deactivateRegion(String regionName) {
        SpawnTask task = activeTasks.remove(regionName);
        if (task != null) {
            task.cancel();
        }
        
        // 해당 지역의 몬스터들 제거 (선택사항)
        Set<UUID> monsters = regionMonsters.remove(regionName);
        if (monsters != null) {
            for (UUID monsterId : monsters) {
                Entity entity = plugin.getServer().getEntity(monsterId);
                if (entity != null && plugin.getMonsterManager().isCustomMonster(entity)) {
                    entity.remove();
                }
            }
        }
        
        plugin.getLogger().info("스폰 지역 비활성화됨: " + regionName);
    }
    
    /**
     * 모든 스폰 지역 활성화
     */
    public void activateAllRegions() {
        for (String regionName : plugin.getMonsterManager().getSpawnRegionNames()) {
            activateRegion(regionName);
        }
    }
    
    /**
     * 모든 스폰 지역 비활성화
     */
    public void deactivateAllRegions() {
        for (String regionName : new HashSet<>(activeTasks.keySet())) {
            deactivateRegion(regionName);
        }
    }
    
    /**
     * 몬스터 사망 시 추적에서 제거
     */
    public void onMonsterDeath(UUID monsterId) {
        for (Set<UUID> monsters : regionMonsters.values()) {
            monsters.remove(monsterId);
        }
    }
    
    /**
     * 스폰 작업 클래스
     */
    private class SpawnTask extends BukkitRunnable {
        private final String regionName;
        private final CustomMonsterManager.SpawnRegion region;
        
        public SpawnTask(String regionName, CustomMonsterManager.SpawnRegion region) {
            this.regionName = regionName;
            this.region = region;
        }
        
        @Override
        public void run() {
            if (!plugin.isPluginEnabled()) {
                return;
            }
            
            try {
                performSpawnCheck();
            } catch (Exception e) {
                plugin.getLogger().warning("스폰 작업 중 오류 발생 (" + regionName + "): " + e.getMessage());
            }
        }
        
        private void performSpawnCheck() {
            World world = region.corner1.getWorld();
            if (world == null) {
                return;
            }
            
            // 플레이어가 근처에 있는지 확인 (성능 최적화)
            if (!hasNearbyPlayers(world)) {
                return;
            }
            
            // 현재 몬스터 수 확인 및 정리
            Set<UUID> monsters = regionMonsters.get(regionName);
            cleanupDeadMonsters(monsters);
            
            int currentCount = monsters.size();
            int maxCount = region.maxMonstersPerRegion;
            
            if (currentCount >= maxCount) {
                return; // 최대 수에 도달
            }
            
            // 스폰할 몬스터 수 계산
            int spawnCount = Math.min(maxCount - currentCount, 3); // 한 번에 최대 3마리
            
            for (int i = 0; i < spawnCount; i++) {
                if (attemptSpawn()) {
                    // 스폰 성공 시 잠시 대기 (서버 부하 분산)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        private boolean hasNearbyPlayers(World world) {
            Location center = region.corner1.clone().add(region.corner2).multiply(0.5);
            
            return world.getPlayers().stream()
                .anyMatch(player -> player.getLocation().distance(center) <= PLAYER_DETECTION_RANGE);
        }
        
        private void cleanupDeadMonsters(Set<UUID> monsters) {
            monsters.removeIf(uuid -> {
                Entity entity = plugin.getServer().getEntity(uuid);
                return entity == null || entity.isDead() || !entity.isValid();
            });
        }
        
        private boolean attemptSpawn() {
            if (region.monsterIds.isEmpty()) {
                return false;
            }
            
            for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
                Location spawnLocation = findSafeSpawnLocation();
                if (spawnLocation == null) {
                    continue;
                }
                
                // 랜덤 몬스터 및 레벨 선택
                String monsterId = region.monsterIds.get(
                    ThreadLocalRandom.current().nextInt(region.monsterIds.size())
                );
                int level = ThreadLocalRandom.current().nextInt(
                    region.minLevel, region.maxLevel + 1
                );
                
                // 몬스터 스폰
                LivingEntity entity = plugin.getMonsterManager().spawnCustomMonster(
                    monsterId, spawnLocation, level
                );
                
                if (entity != null) {
                    regionMonsters.get(regionName).add(entity.getUniqueId());
                    return true;
                }
            }
            
            return false;
        }
        
        private Location findSafeSpawnLocation() {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            
            double minX = Math.min(region.corner1.getX(), region.corner2.getX());
            double maxX = Math.max(region.corner1.getX(), region.corner2.getX());
            double minZ = Math.min(region.corner1.getZ(), region.corner2.getZ());
            double maxZ = Math.max(region.corner1.getZ(), region.corner2.getZ());
            double y = Math.max(region.corner1.getY(), region.corner2.getY());
            
            double x = random.nextDouble(minX, maxX);
            double z = random.nextDouble(minZ, maxZ);
            
            Location location = new Location(region.corner1.getWorld(), x, y, z);
            
            // 청크가 로드되어 있는지 확인
            if (!location.getChunk().isLoaded()) {
                return null;
            }
            
            // 안전한 위치인지 확인 (고체 블록 위, 충분한 공간)
            Location groundLocation = location.getWorld().getHighestBlockAt(location).getLocation().add(0, 1, 0);
            
            if (groundLocation.getBlock().getType().isSolid() || 
                groundLocation.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                return null; // 안전하지 않은 위치
            }
            
            return groundLocation;
        }
    }
    
    /**
     * 정리 작업 (메모리 누수 방지)
     */
    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isPluginEnabled()) {
                    return;
                }
                
                // 죽은 몬스터들 정리
                for (Map.Entry<String, Set<UUID>> entry : regionMonsters.entrySet()) {
                    Set<UUID> monsters = entry.getValue();
                    monsters.removeIf(uuid -> {
                        Entity entity = plugin.getServer().getEntity(uuid);
                        return entity == null || entity.isDead() || !entity.isValid();
                    });
                }
                
                // 통계 로깅 (선택사항)
                if (plugin.getConfig().getBoolean("debug-mode", false)) {
                    logSpawnStatistics();
                }
            }
        }.runTaskTimer(plugin, CLEANUP_INTERVAL, CLEANUP_INTERVAL);
    }
    
    private void logSpawnStatistics() {
        int totalMonsters = regionMonsters.values().stream()
            .mapToInt(Set::size)
            .sum();
        
        plugin.getLogger().info("스폰 통계 - 활성 지역: " + activeTasks.size() + 
                              ", 총 몬스터: " + totalMonsters);
    }
    
    /**
     * 스포너 종료
     */
    public void shutdown() {
        deactivateAllRegions();
        
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        activeTasks.clear();
        regionMonsters.clear();
    }
    
    /**
     * API: 지역별 몬스터 수 조회
     */
    public int getMonsterCount(String regionName) {
        Set<UUID> monsters = regionMonsters.get(regionName);
        return monsters != null ? monsters.size() : 0;
    }
    
    /**
     * API: 총 몬스터 수 조회
     */
    public int getTotalMonsterCount() {
        return regionMonsters.values().stream()
            .mapToInt(Set::size)
            .sum();
    }
}