# CustomRPG 플러그인 종합 가이드

## 📋 개요
CustomRPG는 성능 최적화와 호환성을 중시한 마인크래프트 RPG 플러그인입니다. 평지 RPG 월드 자동 생성, 커스텀 몬스터/아이템 시스템, 그리고 다른 플러그인과의 연동을 위한 완전한 API를 제공합니다.

## 🚀 설치 방법

### 1. 시스템 요구사항
- **Minecraft**: 1.20+
- **서버**: Spigot/Paper (Paper 권장)
- **Java**: 17+
- **메모리**: 최소 2GB (4GB 권장)

### 2. 설치 단계
```bash
# 1. JAR 파일을 plugins 폴더에 복사
cp CustomRPG-1.0.0.jar /server/plugins/

# 2. 서버 시작 (자동으로 RPG 월드 생성)
java -Xms2G -Xmx4G -jar server.jar

# 3. 설정 파일 확인 및 수정
nano plugins/CustomRPG/config.yml
```

### 3. 폴더 구조
```
plugins/CustomRPG/
├── config.yml         # 메인 설정
├── items.yml          # 커스텀 아이템 정의
├── monsters.yml       # 커스텀 몬스터 및 스폰 지역 설정
├── players.yml        # 플레이어 데이터 (자동 생성)
└── api/              # API 문서 및 예제 (선택사항)
```

## ⚙️ 핵심 기능

### 1. 자동 RPG 월드 생성
- **평지 월드**: 성능 최적화를 위한 최소한의 지형 생성
- **스폰 지점**: (0, 65, 0) 고정 스폰
- **구조물 없음**: 자연 구조물/몹 스폰 비활성화
- **청크 최적화**: 필요한 청크만 로드

### 2. 스마트 몬스터 스포너
- **플레이어 감지**: 64블록 내 플레이어 있을 때만 스폰
- **청크 상태 확인**: 로드된 청크에서만 스폰
- **메모리 효율**: UUID 기반 몬스터 추적
- **자동 정리**: 죽은 몬스터 자동 제거

### 3. 최적화된 스텟 시스템
- **즉시 적용**: 장비 착용/해제 시 즉시 반영
- **NBT 저장**: 아이템에 스텟 정보 영구 저장
- **범위 랜덤**: 스텟의 50%~150% 랜덤 생성

## 🎮 사용법

### 플레이어 명령어
```bash
# RPG 월드 이동
/rpg warp rpg          # RPG 월드로 이동
/rpg warp rpg_world    # 동일 (별칭)

# 스텟 확인
/rpg stats             # 내 현재 스텟 확인
```

### 관리자 명령어
```bash
# 플러그인 관리
/rpg enable            # 플러그인 활성화
/rpg disable           # 플러그인 비활성화
/rpg reload            # 설정 다시 로드

# 아이템 관리
/customitem create flame_sword "화염검" DIAMOND_SWORD
/customitem list       # 아이템 목록
/customitem delete flame_sword

# 몬스터 관리
/custommonster create fire_zombie "화염좀비" ZOMBIE
/custommonster list    # 몬스터 목록
/custommonster delete fire_zombie

# 지역 관리
/rpg region create beginner_zone    # 현재 위치에 10x10x10 지역 생성
/rpg region list                    # 지역 목록
/rpg region delete beginner_zone

# 테스트용
/rpg spawn fire_zombie 10          # 레벨 10 화염좀비 스폰
/rpg give flame_sword PlayerName   # 플레이어에게 아이템 지급
```

## 📝 상세 설정

### config.yml - 메인 설정
```yaml
# 플러그인 기본 설정
plugin-enabled: true
debug-mode: false

# RPG 시스템 설정
settings:
  auto-save-interval: 300        # 자동 저장 간격 (초)
  max-stat-level: 100           # 최대 스텟 레벨
  stat-coin-per-level: 10       # 스텟 업그레이드당 필요 코인
  
# 성능 최적화 설정
performance:
  monster-spawn-interval: 100    # 몬스터 스폰 체크 간격 (틱)
  cleanup-interval: 1200        # 정리 작업 간격 (틱)
  player-detection-range: 64    # 플레이어 감지 범위 (블록)
  max-spawn-attempts: 5         # 최대 스폰 시도 횟수
  
# 월드 설정
world:
  name: "rpg_world"             # RPG 월드 이름
  spawn-x: 0                    # 스폰 X 좌표
  spawn-y: 65                   # 스폰 Y 좌표
  spawn-z: 0                    # 스폰 Z 좌표
```

### items.yml - 커스텀 아이템 설정
```yaml
items:
  # 기본 무기
  starter_sword:
    name: "초보자의 검"
    material: "IRON_SWORD"
    dropChance: 0.2
    stats:
      health: 5.0
      damage: 3.0

  # 고급 무기  
  flame_sword:
    name: "§c화염의 검"          # 색상 코드 사용 가능
    material: "NETHERITE_SWORD"
    dropChance: 0.05
    stats:
      health: 20.0
      damage: 15.0
      speed: 5.0
      
  # 방어구
  dragon_chestplate:
    name: "§5드래곤 갑옷"
    material: "NETHERITE_CHESTPLATE"
    dropChance: 0.03
    stats:
      health: 50.0
      defense: 25.0
      
  # 도구
  master_pickaxe:
    name: "§b마스터 곡괭이"
    material: "NETHERITE_PICKAXE"
    dropChance: 0.08
    stats:
      mining: 30.0
      health: 10.0
```

### monsters.yml - 커스텀 몬스터 설정
```yaml
monsters:
  # 초보자용 몬스터
  weak_zombie:
    name: "약한 좀비"
    entityType: "ZOMBIE"
    baseHealth: 25.0
    baseDamage: 3.0
    minLevel: 1
    maxLevel: 5
    statCoinReward: 1
    coinReward: 10
    dropItems:
      - "starter_sword"
    dropChances:
      starter_sword: 0.15
      
  # 중급 몬스터
  fire_skeleton:
    name: "§c화염 스켈레톤"
    entityType: "SKELETON"  
    baseHealth: 45.0
    baseDamage: 6.0
    minLevel: 8
    maxLevel: 15
    statCoinReward: 3
    coinReward: 25
    dropItems:
      - "flame_sword"
      - "dragon_chestplate"
    dropChances:
      flame_sword: 0.08
      dragon_chestplate: 0.05
      
  # 보스급 몬스터
  dragon_spider:
    name: "§5드래곤 거미"
    entityType: "SPIDER"
    baseHealth: 150.0
    baseDamage: 12.0
    minLevel: 20
    maxLevel: 30
    statCoinReward: 15
    coinReward: 100
    dropItems:
      - "flame_sword"
      - "dragon_chestplate"
      - "master_pickaxe"
    dropChances:
      flame_sword: 0.25
      dragon_chestplate: 0.20
      master_pickaxe: 0.15

# 스폰 지역 설정
regions:
  # 초보자 지역
  newbie_zone:
    corner1:
      world: "rpg_world"
      x: -30.0
      y: 60.0
      z: -30.0
    corner2:
      world: "rpg_world"  
      x: 30.0
      y: 80.0
      z: 30.0
    minLevel: 1
    maxLevel: 5
    monsterIds:
      - "weak_zombie"
    maxMonstersPerRegion: 6
    
  # 중급자 지역
  intermediate_zone:
    corner1:
      world: "rpg_world"
      x: 50.0
      y: 60.0
      z: -100.0
    corner2:
      world: "rpg_world"
      x: 150.0
      y: 80.0
      z: 100.0
    minLevel: 8
    maxLevel: 15
    monsterIds:
      - "weak_zombie"
      - "fire_skeleton"
    maxMonstersPerRegion: 10
    
  # 보스 지역
  boss_zone:
    corner1:
      world: "rpg_world"
      x: -200.0
      y: 50.0
      z: -200.0
    corner2:
      world: "rpg_world"
      x: -100.0
      y: 90.0
      z: -100.0
    minLevel: 20
    maxLevel: 30
    monsterIds:
      - "dragon_spider"
    maxMonstersPerRegion: 3
```

## 🔧 API 문서

### API 사용법 (다른 플러그인에서)

#### 1. 의존성 추가 (plugin.yml)
```yaml
depend: [CustomRPG]
# 또는
softdepend: [CustomRPG]
```

#### 2. 기본 API 사용 예제
```java
import com.customrpg.plugin.api.CustomRPGAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ExamplePlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // CustomRPG가 로드되었는지 확인
        if (getServer().getPluginManager().isPluginEnabled("CustomRPG")) {
            useCustomRPGAPI();
        }
    }
    
    private void useCustomRPGAPI() {
        // 플레이어 스텟 조회
        Player player = getServer().getPlayer("PlayerName");
        PlayerStatsManager.PlayerStats stats = CustomRPGAPI.getPlayerStats(player);
        getLogger().info("플레이어 체력: " + stats.health);
        
        // 코인 지급 
        CustomRPGAPI.addCoins(player, 100);
        CustomRPGAPI.addStatCoins(player, 10);
        
        // 커스텀 아이템 지급
        CustomRPGAPI.giveItemToPlayer(player, "flame_sword", true);
        
        // 몬스터 스폰
        Location spawnLoc = player.getLocation();
        LivingEntity monster = CustomRPGAPI.spawnCustomMonster("fire_skeleton", spawnLoc, 15);
        
        // RPG 월드로 이동
        CustomRPGAPI.teleportToRPGWorld(player);
    }
}
```

#### 3. 이벤트 리스닝 예제
```java
import com.customrpg.plugin.api.CustomRPGAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RPGEventListener implements Listener {
    
    @EventHandler
    public void onPlayerStatsChange(CustomRPGAPI.Events.PlayerStatsChangeEvent event) {
        Player player = event.getPlayer();
        PlayerStatsManager.PlayerStats newStats = event.getNewStats();
        
        player.sendMessage("스텟이 변경되었습니다!");
        player.sendMessage("새로운 체력: " + newStats.health);
    }
    
    @EventHandler
    public void onCustomMonsterDeath(CustomRPGAPI.Events.CustomMonsterDeathEvent event) {
        String monsterId = event.getMonsterId();
        int level = event.getLevel();
        Player killer = event.getEntity().getKiller();
        
        if (killer != null) {
            killer.sendMessage(monsterId + " (레벨 " + level + ")을 처치했습니다!");
            
            // 추가 보상 지급 예제
            if (level >= 20) {
                CustomRPGAPI.addStatCoins(killer, 5); // 보너스 스텟코인
            }
        }
    }
}
```

#### 4. 고급 API 사용 예제
```java
public class AdvancedRPGIntegration {
    
    // 커스텀 상점 시스템
    public void createShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, "RPG 상점");
        
        // 커스텀 아이템들을 상점에 추가
        for (String itemId : CustomRPGAPI.getCustomItemIds()) {
            ItemStack item = CustomRPGAPI.createItemStack(itemId, false);
            if (item != null) {
                shop.addItem(item);
            }
        }
        
        player.openInventory(shop);
    }
    
    // 스텟 기반 권한 시스템
    public boolean checkStatRequirement(Player player, String statType, double required) {
        PlayerStatsManager.PlayerStats stats = CustomRPGAPI.getPlayerStats(player);
        
        switch (statType.toLowerCase()) {
            case "health": return stats.health >= required;
            case "damage": return stats.damage >= required;
            case "defense": return stats.defense >= required;
            case "speed": return stats.speed >= required;
            case "mining": return stats.mining >= required;
            default: return false;
        }
    }
    
    // 동적 몬스터 생성 시스템
    public void createDynamicMonster(String id, Player targetPlayer) {
        Location playerLoc = targetPlayer.getLocation();
        PlayerStatsManager.PlayerStats playerStats = CustomRPGAPI.getPlayerStats(targetPlayer);
        
        // 플레이어 스텟에 따른 몬스터 레벨 계산
        double totalStats = playerStats.health + playerStats.damage + playerStats.defense;
        int monsterLevel = Math.max(1, (int)(totalStats / 10));
        
        // 몬스터 스폰
        LivingEntity monster = CustomRPGAPI.spawnCustomMonster(id, playerLoc, monsterLevel);
        if (monster != null) {
            targetPlayer.sendMessage("당신의 실력에 맞는 몬스터가 나타났습니다! (레벨 " + monsterLevel + ")");
        }
    }
}
```

### API 메소드 전체 목록

#### 플레이어 스텟 관리
```java
// 스텟 조회
PlayerStatsManager.PlayerStats getPlayerStats(Player player)

// 코인 관리
void addStatCoins(Player player, int amount)
void addCoins(Player player, int amount)  
boolean spendStatCoins(Player player, int amount)
boolean spendCoins(Player player, int amount)

// 임시 스텟 적용
void addTemporaryStats(Player player, double health, double damage, double defense, double speed, double mining)
void removeTemporaryStats(Player player, double health, double damage, double defense, double speed, double mining)
```

#### 커스텀 아이템 관리
```java
// 아이템 생성
void createCustomItem(String id, String name, Material material, Map<String, Double> stats, double dropChance)
ItemStack createItemStack(String itemId, boolean randomStats)

// 아이템 확인
boolean isCustomItem(ItemStack item)
String getCustomItemId(ItemStack item)
Map<String, Double> getItemStats(ItemStack item)

// 아이템 지급
void giveItemToPlayer(Player player, String itemId, boolean randomStats)
Set<String> getCustomItemIds()
```

#### 커스텀 몬스터 관리  
```java
// 몬스터 생성
void createCustomMonster(String id, String name, EntityType entityType, ...)
LivingEntity spawnCustomMonster(String monsterId, Location location, int level)

// 몬스터 확인
boolean isCustomMonster(Entity entity)
String getCustomMonsterId(Entity entity)
int getMonsterLevel(Entity entity)

// 스폰 지역 관리
void createSpawnRegion(String name, Location corner1, Location corner2, ...)
Set<String> getCustomMonsterIds()
```

#### 월드 및 유틸리티
```java
// 월드 관리
World getRPGWorld()
boolean teleportToRPGWorld(Player player)
boolean isRPGWorld(World world)

// 플러그인 상태
boolean isPluginEnabled()
void setPluginEnabled(boolean enabled)

// 유틸리티
String getStatDisplayName(String statType)
double calculateLevelScaledStat(double baseValue, int level, double multiplierPerLevel)
double calculateRandomStat(double baseStat, double minMultiplier, double maxMultiplier)
```

## 🔄 호환성 가이드라인

### 1. 다른 플러그인 개발 시 고려사항

#### 네임스페이스 충돌 방지
```java
// 좋은 예 - 네임스페이스 사용
NamespacedKey myPluginKey = new NamespacedKey(myPlugin, "my_custom_data");

// 나쁜 예 - CustomRPG 네임스페이스 침범 금지
// NamespacedKey badKey = new NamespacedKey(plugin, "custom_item_id"); // 금지!
```

#### 이벤트 우선순위 설정
```java
// CustomRPG 이벤트 후에 실행하려면
@EventHandler(priority = EventPriority.MONITOR)
public void onEntityDeath(EntityDeathEvent event) {
    // CustomRPG 처리 후 실행됨
}

// CustomRPG 이벤트 전에 실행하려면  
@EventHandler(priority = EventPriority.LOW)
public void onEntityDamage(EntityDamageEvent event) {
    // CustomRPG 처리 전 실행됨
}
```

#### 안전한 API 호출
```java
public class SafeAPIUsage {
    
    public void safeAPICall(Player player) {
        try {
            // 플러그인 활성화 상태 확인
            if (!CustomRPGAPI.isPluginEnabled()) {
                return;
            }
            
            // null 체크
            if (player == null || !player.isOnline()) {
                return;
            }
            
            // API 호출
            PlayerStatsManager.PlayerStats stats = CustomRPGAPI.getPlayerStats(player);
            
        } catch (Exception e) {
            getLogger().warning("CustomRPG API 호출 실패: " + e.getMessage());
        }
    }
}
```

### 2. 권장 통합 패턴

#### 옵션 의존성 패턴
```java
public class MyPlugin extends JavaPlugin {
    private boolean customRPGEnabled = false;
    
    @Override
    public void onEnable() {
        // CustomRPG 확인
        if (getServer().getPluginManager().isPluginEnabled("CustomRPG")) {
            customRPGEnabled = true;
            getLogger().info("CustomRPG 연동이 활성화되었습니다.");
        } else {
            getLogger().info("CustomRPG가 감지되지 않았습니다. 기본 기능만 사용합니다.");
        }
    }
    
    public void giveReward(Player player, int amount) {
        if (customRPGEnabled) {
            // CustomRPG 코인 사용
            CustomRPGAPI.addCoins(player, amount);
        } else {
            // 기본 경제 시스템 사용
            // VaultAPI 등 사용
        }
    }
}
```

#### 확장 기능 패턴
```java
public class RPGExtension {
    
    // CustomRPG 기능을 확장하는 새로운 스텟
    public void addCustomStat(Player player, String statName, double value) {
        // 기존 CustomRPG 스텟에 추가
        Map<String, Double> customStats = new HashMap<>();
        customStats.put(statName, value);
        
        // 임시 스텟으로 적용 (예: 마나, 럭 등)
        if ("mana".equals(statName)) {
            // 마나는 속도로 변환하여 적용
            CustomRPGAPI.addTemporaryStats(player, 0, 0, 0, value * 0.1, 0);
        }
    }
}
```

## 📊 성능 최적화 가이드

### 1. 서버 설정 최적화
```yaml
# server.properties
max-tick-time=60000
network-compression-threshold=256

# spigot.yml  
world-settings:
  default:
    mob-spawn-range: 4      # 몬스터 스폰 범위 축소
    entity-activation-range:
      monsters: 24          # 몬스터 활성화 범위
      animals: 16
      misc: 8
    
# paper.yml
world-settings:
  default:
    despawn-ranges:
      monster:
        soft: 28            # 몬스터 디스폰 거리
        hard: 96
```

### 2. CustomRPG 설정 최적화
```yaml
# config.yml
performance:
  monster-spawn-interval: 200    # 스폰 체크를 더 적게 (10초)
  cleanup-interval: 2400        # 정리를 더 자주 (2분)
  player-detection-range: 48    # 감지 범위 축소
  max-spawn-attempts: 3         # 스폰 시도 횟수 감소

settings:
  auto-save-interval: 600       # 자동 저장을 덜 자주 (10분)
```

### 3. 메모리 사용량 모니터링
```java
// 메모리 사용량 체크 명령어 (관리자용)
@EventHandler
public void onCommand(PlayerCommandPreprocessEvent event) {
    if (event.getMessage().equals("/rpg memory")) {
        Map<String, Object> info = CustomRPGAPI.getPerformanceInfo();
        Player player = event.getPlayer();
        
        player.sendMessage("=== CustomRPG 메모리 정보 ===");
        player.sendMessage("사용 중: " + info.get("usedMemory") + "MB");
        player.sendMessage("여유: " + info.get("freeMemory") + "MB");
        player.sendMessage("총: " + info.get("totalMemory") + "MB");
        
        event.setCancelled(true);
    }
}
```

## 🐛 문제 해결

### 자주 발생하는 문제

#### 1. RPG 월드가 생성되지 않음
```bash
# 해결책
1. 서버 로그 확인: plugins/CustomRPG/logs/
2. 권한 확인: 서버 폴더 쓰기 권한
3. 디스크 공간 확인: 충분한 여유 공간
4. Java 버전 확인: Java 17+ 필요
```

#### 2. 몬스터가 스폰되지 않음
```yaml
# 디버그 모드 활성화 (config.yml)
debug-mode: true

# 로그에서 다음 확인:
# - "스폰 지역 활성화됨: region_name"
# - "플레이어 감지: true/false"
# - "스폰 시도: location"
```

#### 3. 스텟이 적용되지 않음
```java  
// 강제 스텟 새로고침 명령어
/rpg reload        # 설정 다시 로드
/rpg stats         # 스텟 확인

// API로 강제 새로고침
CustomRPGAPI.refreshPlayerCache(player);
```

### 로그 레벨 설정
```yaml
# logging.properties (서버 루트)
com.customrpg.plugin.level=INFO

# 더 자세한 로그 원할 시
com.customrpg.plugin.level=FINE
```

## 📈 확장 및 커스터마이징

### 1. 추가 스텟 시스템 만들기
```java
public class ExtendedStatsSystem {
    
    // 새로운 스텟 (마나, 럭 등)
    public enum CustomStat {
        MANA("마나", ChatColor.BLUE),
        LUCK("행운", ChatColor.GREEN),
        CRITICAL("치명타", ChatColor.RED);
        
        private String displayName;
        private ChatColor color;
        
        CustomStat(String displayName, ChatColor color) {
            this.displayName = displayName;
            this.color = color;
        }
    }
    
    // 확장 스텟을 CustomRPG에 연동
    public void applyExtendedStats(Player player, Map<CustomStat, Double> stats) {
        double totalBonus = stats.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // 기존 RPG 스텟으로 변환하여 적용
        CustomRPGAPI.addTemporaryStats(player, 
            stats.getOrDefault(CustomStat.MANA, 0.0) * 0.5,    // 마나 -> 체력
            stats.getOrDefault(CustomStat.CRITICAL, 0.0),      // 치명타 -> 공격력  
            0, 
            stats.getOrDefault(CustomStat.LUCK, 0.0) * 0.2,    // 행운 -> 속도
            0
        );
    }
}
```

### 2. 커스텀 UI 만들기
```java
public class RPGInventoryGUI {
    
    public void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6내 RPG 스텟");
        PlayerStatsManager.PlayerStats stats = CustomRPGAPI.getPlayerStats(player);
        
        // 스텟 표시 아이템들
        gui.setItem(10, createStatItem(Material.RED_DYE, "체력", stats.health));
        gui.setItem(11, createStatItem(Material.IRON_SWORD, "공격력", stats.damage));
        gui.setItem(12, createStatItem(Material.SHIELD, "방어력", stats.defense));
        gui.setItem(13, createStatItem(Material.FEATHER, "속도", stats.speed));
        gui.setItem(14, createStatItem(Material.DIAMOND_PICKAXE, "채굴속도", stats.mining));
        
        // 코인 정보
        gui.setItem(16, createCoinItem(stats.statCoins, stats.coins));
        
        player.openInventory(gui);
    }
    
    private ItemStack createStatItem(Material material, String statName, double value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + statName);
        meta.setLore(Arrays.asList("§7현재 수치: §a+" + value));
        item.setItemMeta(meta);
        return item;
    }
}
```

이 가이드를 통해 CustomRPG 플러그인을 완전히 활용하고, 다른 플러그인과의 연동도 쉽게 구현하실 수 있습니다. API를 통해 무한한 확장이 가능하며, 성능 최적화 설정으로 대용량 서버에서도 안정적으로 운영할 수 있습니다.