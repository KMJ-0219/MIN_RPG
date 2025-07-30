package com.customrpg.plugin;

import com.customrpg.plugin.api.CustomRPGAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomRPGPlugin extends JavaPlugin {
    
    private static CustomRPGPlugin instance;
    private boolean pluginEnabled = true;
    
    // 매니저 클래스들
    private PlayerStatsManager statsManager;
    private CustomItemManager itemManager;
    private CustomMonsterManager monsterManager;
    private RPGWorldGenerator.WorldManager worldManager;
    private EconomyManager economyManager;
    private OptimizedMonsterSpawner monsterSpawner;
    
    // 설정 파일들
    private File itemsConfigFile;
    private FileConfiguration itemsConfig;
    private File monstersConfigFile;
    private FileConfiguration monstersConfig;
    private File playersConfigFile;
    private FileConfiguration playersConfig;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // API 초기화
        CustomRPGAPI.initialize(this);
        
        // 설정 파일 생성
        saveDefaultConfig();
        createConfigFiles();
        
        // 월드 매니저 먼저 초기화 (다른 매니저들이 의존)
        worldManager = new RPGWorldGenerator.WorldManager(this);
        
        // RPG 월드 생성/로드
        worldManager.createOrLoadRPGWorld();
        
        // 매니저 초기화
        statsManager = new PlayerStatsManager(this);
        itemManager = new CustomItemManager(this);
        monsterManager = new CustomMonsterManager(this);
        economyManager = new EconomyManager(this);
        
        // 최적화된 몬스터 스포너 초기화
        monsterSpawner = new OptimizedMonsterSpawner(this);
        
        // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        getServer().getPluginManager().registerEvents(new MonsterEventListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemEventListener(this), this);
        
        // 명령어 등록
        getCommand("rpg").setExecutor(new RPGCommandExecutor(this));
        getCommand("customitem").setExecutor(new ItemCommandExecutor(this));
        getCommand("custommonster").setExecutor(new MonsterCommandExecutor(this));
        
        // 스폰 지역 활성화 (플러그인 시작 5초 후)
        getServer().getScheduler().runTaskLater(this, () -> {
            if (pluginEnabled) {
                monsterSpawner.activateAllRegions();
                getLogger().info("모든 스폰 지역이 활성화되었습니다.");
            }
        }, 100L);
        
        // 자동 저장 작업 시작
        startAutoSaveTask();
        
        getLogger().info("CustomRPG 플러그인이 활성화되었습니다!");
        getLogger().info("API가 초기화되었습니다. 다른 플러그인에서 CustomRPGAPI를 사용할 수 있습니다.");
    }
    
    @Override
    public void onDisable() {
        // 몬스터 스포너 종료
        if (monsterSpawner != null) {
            monsterSpawner.shutdown();
        }
        
        // 플레이어 데이터 저장
        if (statsManager != null) {
            statsManager.saveAllPlayerData();
        }
        
        getLogger().info("CustomRPG 플러그인이 비활성화되었습니다!");
    }
    
    private void createConfigFiles() {
        // 아이템 설정 파일
        itemsConfigFile = new File(getDataFolder(), "items.yml");
        if (!itemsConfigFile.exists()) {
            itemsConfigFile.getParentFile().mkdirs();
            saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsConfigFile);
        
        // 몬스터 설정 파일
        monstersConfigFile = new File(getDataFolder(), "monsters.yml");
        if (!monstersConfigFile.exists()) {
            monstersConfigFile.getParentFile().mkdirs();
            saveResource("monsters.yml", false);
        }
        monstersConfig = YamlConfiguration.loadConfiguration(monstersConfigFile);
        
        // 플레이어 데이터 파일
        playersConfigFile = new File(getDataFolder(), "players.yml");
        if (!playersConfigFile.exists()) {
            playersConfigFile.getParentFile().mkdirs();
            try {
                playersConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersConfigFile);
    }
    
    private void startAutoSaveTask() {
        int interval = getConfig().getInt("settings.auto-save-interval", 300) * 20; // 초를 틱으로 변환
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (pluginEnabled) {
                statsManager.saveAllPlayerData();
                getLogger().info("플레이어 데이터 자동 저장 완료");
            }
        }, interval, interval);
    }
    
    public void saveItemsConfig() {
        try {
            itemsConfig.save(itemsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveMonstersConfig() {
        try {
            monstersConfig.save(monstersConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void savePlayersConfig() {
        try {
            playersConfig.save(playersConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Getter 메소드들
    public static CustomRPGPlugin getInstance() {
        return instance;
    }
    
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }
    
    public void setPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
        
        if (enabled) {
            monsterSpawner.activateAllRegions();
            getLogger().info("CustomRPG 플러그인이 활성화되었습니다.");
        } else {
            monsterSpawner.deactivateAllRegions();
            getLogger().info("CustomRPG 플러그인이 비활성화되었습니다.");
        }
    }
    
    public PlayerStatsManager getStatsManager() {
        return statsManager;
    }
    
    public CustomItemManager getItemManager() {
        return itemManager;
    }
    
    public CustomMonsterManager getMonsterManager() {
        return monsterManager;
    }
    
    public RPGWorldGenerator.WorldManager getWorldManager() {
        return worldManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public OptimizedMonsterSpawner getMonsterSpawner() {
        return monsterSpawner;
    }
    
    public FileConfiguration getItemsConfig() {
        return itemsConfig;
    }
    
    public FileConfiguration getMonstersConfig() {
        return monstersConfig;
    }
    
    public FileConfiguration getPlayersConfig() {
        return playersConfig;
    }
}