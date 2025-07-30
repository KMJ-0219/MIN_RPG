package com.customrpg.plugin.monitoring;

import com.customrpg.plugin.CustomRPGPlugin;
import com.customrpg.plugin.api.CustomRPGAPI;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CustomRPG 성능 모니터링 시스템
 * - 메모리 사용량 추적
 * - 몬스터 스폰 통계
 * - 플레이어 활동 분석
 * - 자동 최적화 기능
 */
public class PerformanceMonitor extends BukkitRunnable implements CommandExecutor {
    
    private CustomRPGPlugin plugin;
    
    // 성능 데이터
    private Map<String, Long> performanceMetrics = new ConcurrentHashMap<>();
    private List<PerformanceSnapshot> snapshots = new ArrayList<>();
    private Map<String, Integer> spawnCounts = new ConcurrentHashMap<>();
    private Map<UUID, PlayerActivity> playerActivities = new ConcurrentHashMap<>();
    
    // 최적화 설정
    private boolean autoOptimization = true;
    private int maxSnapshots = 100;
    private long lastOptimization = 0;
    
    public static class PerformanceSnapshot {
        public LocalDateTime timestamp;
        public long usedMemory;
        public long freeMemory;
        public int onlinePlayers;
        public int activeMonsters;
        public int spawnRegions;
        public double tps;
        
        public PerformanceSnapshot() {
            this.timestamp = LocalDateTime.now();
            Runtime runtime = Runtime.getRuntime();
            this.usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            this.freeMemory = runtime.freeMemory() / 1024 / 1024;
        }
    }
    
    public static class PlayerActivity {
        public UUID playerId;
        public String playerName;
        public int monstersKilled = 0;
        public int itemsReceived = 0;
        public long lastActive = System.currentTimeMillis();
        public Map<String, Integer> statGains = new HashMap<>();
        
        public PlayerActivity(UUID playerId, String playerName) {
            this.playerId = playerId;
            this.playerName = playerName;
        }
    }
    
    public PerformanceMonitor(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        
        // 성능 모니터링 작업 시작 (30초마다)
        this.runTaskTimer(plugin, 600L, 600L);
        
        // 명령어 등록
        plugin.getCommand("rpgmonitor").setExecutor(this);
        
        plugin.getLogger().info("성능 모니터링 시스템이 시작되었습니다.");
    }
    
    @Override
    public void run() {
        try {
            collectPerformanceData();
            
            if (autoOptimization && shouldOptimize()) {
                performAutoOptimization();
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("성능 모니터링 중 오류 발생: " + e.getMessage());
        }
    }
    
    private void collectPerformanceData() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        
        // 온라인 플레이어 수
        snapshot.onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        
        // 활성 몬스터 수
        snapshot.activeMonsters = plugin.getMonsterSpawner().getTotalMonsterCount();
        
        // 스폰 지역 수
        snapshot.spawnRegions = plugin.getMonsterManager().getSpawnRegionNames().size();
        
        // TPS 계산 (간단한 방식)
        long currentTime = System.currentTimeMillis();
        if (!snapshots.isEmpty()) {
            PerformanceSnapshot lastSnapshot = snapshots.get(snapshots.size() - 1);
            long timeDiff = currentTime - lastSnapshot.timestamp.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            snapshot.tps = Math.min(20.0, 20000.0 / Math.max(timeDiff, 50));
        } else {
            snapshot.tps = 20.0;
        }
        
        snapshots.add(snapshot);
        
        // 오래된 스냅샷 제거
        if (snapshots.size() > maxSnapshots) {
            snapshots.remove(0);
        }
        
        // 메트릭 업데이트
        performanceMetrics.put("memory_used", snapshot.usedMemory);
        performanceMetrics.put("memory_free", snapshot.freeMemory);
        performanceMetrics.put("players_online", (long) snapshot.onlinePlayers);
        performanceMetrics.put("monsters_active", (long) snapshot.activeMonsters);
        performanceMetrics.put("tps", (long) (snapshot.tps * 100)); // 소수점 두 자리
        
        // 로그 출력 (디버그 모드일 때만)
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info(String.format(
                "성능 모니터링 - 메모리: %dMB, 플레이어: %d, 몬스터: %d, TPS: %.1f",
                snapshot.usedMemory, snapshot.onlinePlayers, snapshot.activeMonsters, snapshot.tps
            ));
        }
    }
    
    private boolean shouldOptimize() {
        long currentTime = System.currentTimeMillis();
        
        // 최소 5분 간격으로 최적화
        if (currentTime - lastOptimization < 300000) {
            return false;
        }
        
        if (snapshots.size() < 3) {
            return false;
        }
        
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        // 최적화 조건들
        boolean highMemoryUsage = latest.usedMemory > 1024; // 1GB 이상
        boolean lowTPS = latest.tps < 15.0;
        boolean tooManyMonsters = latest.activeMonsters > 200;
        
        return highMemoryUsage || lowTPS || tooManyMonsters;
    }
    
    private void performAutoOptimization() {
        lastOptimization = System.currentTimeMillis();
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        plugin.getLogger().info("자동 최적화를 시작합니다...");
        
        List<String> actions = new ArrayList<>();
        
        // 메모리 사용량이 높은 경우
        if (latest.usedMemory > 1024) {
            // 가비지 컬렉션 요청
            System.gc();
            actions.add("가비지 컬렉션 실행");
            
            // 플레이어 데이터 저장 및 캐시 정리
            plugin.getStatsManager().saveAllPlayerData();
            actions.add("플레이어 데이터 저장");
        }
        
        // TPS가 낮은 경우
        if (latest.tps < 15.0) {
            // 몬스터 스폰 간격 증가
            plugin.getConfig().set("performance.monster-spawn-interval", 
                plugin.getConfig().getInt("performance.monster-spawn-interval", 100) + 20);
            actions.add("몬스터 스폰 간격 증가");
            
            // 정리 작업 간격 단축
            plugin.getConfig().set("performance.cleanup-interval", 
                Math.max(600, plugin.getConfig().getInt("performance.cleanup-interval", 1200) - 300));
            actions.add("정리 작업 주기 단축");
        }
        
        // 몬스터가 너무 많은 경우
        if (latest.activeMonsters > 200) {
            // 일부 지역의 최대 몬스터 수 감소
            for (String regionName : plugin.getMonsterManager().getSpawnRegionNames()) {
                int currentCount = plugin.getMonsterSpawner().getMonsterCount(regionName);
                if (currentCount > 15) {
                    // 해당 지역 일시 비활성화 후 재활성화 (몬스터 정리 효과)
                    plugin.getMonsterSpawner().deactivateRegion(regionName);
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.getMonsterSpawner().activateRegion(regionName);
                    }, 200L); // 10초 후 재활성화
                }
            }
            actions.add("과밀 지역 몬스터 정리");
        }
        
        // 설정 저장
        plugin.saveConfig();
        
        plugin.getLogger().info("자동 최적화 완료: " + String.join(", ", actions));
        
        // 관리자들에게 알림
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("customrpg.admin")) {
                player.sendMessage("§6[CustomRPG] §a자동 최적화가 실행되었습니다.");
                player.sendMessage("§7수행된 작업: " + String.join(", ", actions));
            }
        }
    }
    
    // 플레이어 활동 추적
    public void trackPlayerActivity(Player player, String activityType, Object... data) {
        UUID playerId = player.getUniqueId();
        PlayerActivity activity = playerActivities.computeIfAbsent(playerId, 
            k -> new PlayerActivity(playerId, player.getName()));
        
        activity.lastActive = System.currentTimeMillis();
        
        switch (activityType) {
            case "monster_kill":
                activity.monstersKilled++;
                spawnCounts.merge("monsters_killed", 1, Integer::sum);
                break;
                
            case "item_received":
                activity.itemsReceived++;
                spawnCounts.merge("items_given", 1, Integer::sum);
                break;
                
            case "stat_gain":
                if (data.length >= 2) {
                    String statType = (String) data[0];
                    Integer amount = (Integer) data[1];
                    activity.statGains.merge(statType, amount, Integer::sum);
                }
                break;
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customrpg.admin")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            showMainStats(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "memory":
                showMemoryStats(sender);
                break;
                
            case "players":
                showPlayerStats(sender);
                break;
                
            case "monsters":
                showMonsterStats(sender);
                break;
                
            case "optimize":
                if (sender instanceof Player) {
                    performAutoOptimization();
                    sender.sendMessage(ChatColor.GREEN + "수동 최적화를 실행했습니다.");
                }
                break;
                
            case "reset":
                resetStats();
                sender.sendMessage(ChatColor.GREEN + "통계를 초기화했습니다.");
                break;
                
            case "export":
                exportStats(sender);
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void showMainStats(CommandSender sender) {
        if (snapshots.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "아직 수집된 데이터가 없습니다.");
            return;
        }
        
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        sender.sendMessage(ChatColor.GOLD + "=== CustomRPG 성능 모니터링 ===");
        sender.sendMessage(ChatColor.YELLOW + "시간: " + latest.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        sender.sendMessage(ChatColor.GREEN + "메모리 사용량: " + latest.usedMemory + "MB");
        sender.sendMessage(ChatColor.GREEN + "여유 메모리: " + latest.freeMemory + "MB");
        sender.sendMessage(ChatColor.AQUA + "온라인 플레이어: " + latest.onlinePlayers + "명");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "활성 몬스터: " + latest.activeMonsters + "마리");
        sender.sendMessage(ChatColor.WHITE + "스폰 지역: " + latest.spawnRegions + "개");
        sender.sendMessage(ChatColor.RED + "현재 TPS: " + String.format("%.1f", latest.tps));
        
        // 성능 상태 표시
        String status = getPerformanceStatus(latest);
        sender.sendMessage(ChatColor.BOLD + "전체 상태: " + status);
    }
    
    private void showMemoryStats(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 메모리 사용량 히스토리 ===");
        
        int count = Math.min(10, snapshots.size());
        for (int i = snapshots.size() - count; i < snapshots.size(); i++) {
            PerformanceSnapshot snapshot = snapshots.get(i);
            sender.sendMessage(String.format("§7%s: §a%dMB 사용 §7/ §b%dMB 여유",
                snapshot.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                snapshot.usedMemory, snapshot.freeMemory));
        }
        
        // 평균 메모리 사용량
        double avgMemory = snapshots.stream()
            .mapToLong(s -> s.usedMemory)
            .average()
            .orElse(0.0);
        
        sender.sendMessage("§e평균 메모리 사용량: " + String.format("%.1f", avgMemory) + "MB");
    }
    
    private void showPlayerStats(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 플레이어 활동 통계 ===");
        
        List<PlayerActivity> sortedActivities = playerActivities.values().stream()
            .sorted((a, b) -> Integer.compare(b.monstersKilled, a.monstersKilled))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());
        
        sender.sendMessage(ChatColor.YELLOW + "몬스터 처치량 TOP 10:");
        for (int i = 0; i < sortedActivities.size(); i++) {
            PlayerActivity activity = sortedActivities.get(i);
            sender.sendMessage(String.format("§7%d. §f%s: §c%d마리 처치 §7/ §e%d개 아이템",
                i + 1, activity.playerName, activity.monstersKilled, activity.itemsReceived));
        }
        
        // 전체 통계
        int totalKills = spawnCounts.getOrDefault("monsters_killed", 0);
        int totalItems = spawnCounts.getOrDefault("items_given", 0);
        
        sender.sendMessage("");
        sender.sendMessage("§e전체 몬스터 처치: " + totalKills + "마리");
        sender.sendMessage("§e전체 아이템 지급: " + totalItems + "개");
    }
    
    private void showMonsterStats(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 몬스터 및 스폰 통계 ===");
        
        for (String regionName : plugin.getMonsterManager().getSpawnRegionNames()) {
            int monsterCount = plugin.getMonsterSpawner().getMonsterCount(regionName);
            sender.sendMessage(String.format("§7%s: §c%d마리 활성", regionName, monsterCount));
        }
        
        sender.sendMessage("");
        sender.sendMessage("§e총 활성 몬스터: " + plugin.getMonsterSpawner().getTotalMonsterCount() + "마리");
        sender.sendMessage("§e총 스폰 지역: " + plugin.getMonsterManager().getSpawnRegionNames().size() + "개");
    }
    
    private String getPerformanceStatus(PerformanceSnapshot snapshot) {
        if (snapshot.tps < 10.0) {
            return "§4심각 - 즉시 최적화 필요";
        } else if (snapshot.tps < 15.0) {
            return "§c경고 - 성능 저하";
        } else if (snapshot.usedMemory > 2048) {
            return "§e주의 - 메모리 사용량 높음";
        } else {
            return "§a양호 - 정상 작동";
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== RPG 모니터링 명령어 ===");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor - 전체 상태 확인");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor memory - 메모리 사용량");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor players - 플레이어 활동");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor monsters - 몬스터 통계");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor optimize - 수동 최적화");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor reset - 통계 초기화");
        sender.sendMessage(ChatColor.YELLOW + "/rpgmonitor export - 데이터 내보내기");
    }
    
    private void resetStats() {
        snapshots.clear();
        performanceMetrics.clear();
        spawnCounts.clear();
        playerActivities.clear();
        lastOptimization = 0;
    }
    
    private void exportStats(CommandSender sender) {
        try {
            // CSV 형태로 데이터 내보내기
            java.io.File exportFile = new java.io.File(plugin.getDataFolder(), "performance_export.csv");
            java.io.FileWriter writer = new java.io.FileWriter(exportFile);
            
            writer.write("timestamp,memory_used,memory_free,online_players,active_monsters,tps\n");
            
            for (PerformanceSnapshot snapshot : snapshots) {
                writer.write(String.format("%s,%d,%d,%d,%d,%.2f\n",
                    snapshot.timestamp.toString(),
                    snapshot.usedMemory,
                    snapshot.freeMemory,
                    snapshot.onlinePlayers,
                    snapshot.activeMonsters,
                    snapshot.tps
                ));
            }
            
            writer.close();
            sender.sendMessage(ChatColor.GREEN + "성능 데이터가 performance_export.csv로 내보내졌습니다.");
            
        } catch (java.io.IOException e) {
            sender.sendMessage(ChatColor.RED + "데이터 내보내기 실패: " + e.getMessage());
        }
    }
    
    // 외부에서 호출 가능한 메소드들
    public Map<String, Long> getPerformanceMetrics() {
        return new HashMap<>(performanceMetrics);
    }
    
    public List<PerformanceSnapshot> getRecentSnapshots(int count) {
        int size = snapshots.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(snapshots.subList(start, size));
    }
    
    public void setAutoOptimization(boolean enabled) {
        this.autoOptimization = enabled;
    }
    
    public boolean isAutoOptimizationEnabled() {
        return autoOptimization;
    }
}