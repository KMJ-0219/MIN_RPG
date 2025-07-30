# CustomRPG 플러그인 최종 설치 및 운영 가이드

## 🚀 완전 설치 가이드

### 1. 파일 구조 및 설치
```bash
# 서버 폴더 구조
server/
├── plugins/
│   └── CustomRPG.jar
├── world/              # 메인 월드
├── world_nether/       # 네더 월드
├── world_the_end/      # 엔드 월드
└── rpg_world/          # 자동 생성될 RPG 월드

# 설치 단계
1. CustomRPG.jar을 plugins/ 폴더에 복사
2. 서버 시작 (첫 실행 시 자동으로 설정 파일 생성)
3. 서버 종료 후 설정 파일 수정
4. 서버 재시작
```

### 2. plugin.yml 등록 (명령어 추가)
```yaml
# plugin.yml에 추가할 명령어
commands:
  rpgmonitor:
    description: RPG 성능 모니터링
    usage: /rpgmonitor [옵션]
    permission: customrpg.admin
    aliases: [monitor, rpgstats]
```

### 3. 첫 실행 후 자동 생성되는 설정들

#### config.yml (자동 최적화 설정)
```yaml
# CustomRPG 메인 설정
plugin-enabled: true
debug-mode: false

# RPG 시스템
settings:
  auto-save-interval: 300
  max-stat-level: 100
  stat-coin-per-level: 10

# 성능 최적화 설정  
performance:
  monster-spawn-interval: 100    # 몬스터 스폰 체크 (틱)
  cleanup-interval: 1200        # 정리 작업 간격 (틱)
  player-detection-range: 64    # 플레이어 감지 범위
  max-spawn-attempts: 5         # 최대 스폰 시도
  auto-optimization: true       # 자동 최적화 활성화

# RPG 월드 설정
world:
  name: "rpg_world"
  spawn-x: 0
  spawn-y: 65
  spawn-z: 0
  generate-structures: false
  natural-spawning: false

# 모니터링 설정
monitoring:
  enabled: true
  snapshot-interval: 30         # 성능 스냅샷 간격 (초)
  max-snapshots: 100           # 최대 저장할 스냅샷 수
  export-data: true            # 데이터 내보내기 허용
```

## 🎯 단계별 서버 설정

### 1단계: 기본 설정
```bash
# 첫 실행 후 생성되는 기본 아이템 및 몬스터 확인
/customitem list
/custommonster list

# RPG 월드 생성 확인
/rpg warp rpg

# 기본 스폰 지역 생성
/rpg region create newbie_zone
```

### 2단계: 컨텐츠 추가
```bash
# 커스텀 아이템 생성 예제
/customitem create starter_sword "초보자의 검" IRON_SWORD
/customitem create flame_armor "화염 갑옷" CHAINMAIL_CHESTPLATE

# 커스텀 몬스터 생성 예제  
/custommonster create weak_zombie "약한 좀비" ZOMBIE
/custommonster create fire_skeleton "화염 스켈레톤" SKELETON

# 추가 스폰 지역 생성
/rpg region create intermediate_zone
/rpg region create boss_zone
```

### 3단계: 세부 설정 (파일 편집)

#### items.yml 상세 설정
```yaml
items:
  # 티어 1 - 초보자용
  iron_blade:
    name: "§7강철 검날"
    material: "IRON_SWORD"
    dropChance: 0.3
    stats:
      health: 3.0
      damage: 2.0
  
  leather_guard:
    name: "§8가죽 보호구"  
    material: "LEATHER_CHESTPLATE"
    dropChance: 0.25
    stats:
      health: 5.0
      defense: 2.0

  # 티어 2 - 중급자용
  flame_sword:
    name: "§c화염의 검"
    material: "DIAMOND_SWORD"
    dropChance: 0.15
    stats:
      health: 8.0
      damage: 6.0
      speed: 2.0
      
  chain_armor:
    name: "§7강화 사슬갑옷"
    material: "CHAINMAIL_CHESTPLATE"  
    dropChance: 0.12
    stats:
      health: 12.0
      defense: 8.0

  # 티어 3 - 고급자용
  dragon_blade:
    name: "§5드래곤의 검"
    material: "NETHERITE_SWORD"
    dropChance: 0.05
    stats:
      health: 20.0
      damage: 15.0
      speed: 5.0
      
  dragon_plate:
    name: "§5드래곤 판금갑옷"
    material: "NETHERITE_CHESTPLATE"
    dropChance: 0.03
    stats:
      health: 35.0
      defense: 20.0
      speed: 3.0

  # 도구류
  miner_pick:
    name: "§b광부의 곡괭이"
    material: "DIAMOND_PICKAXE"
    dropChance: 0.08
    stats:
      mining: 20.0
      health: 5.0
```

#### monsters.yml 상세 설정
```yaml
monsters:
  # 티어 1 몬스터 (레벨 1-10)
  weak_zombie:
    name: "약한 좀비"
    entityType: "ZOMBIE"
    baseHealth: 30.0
    baseDamage: 4.0
    minLevel: 1
    maxLevel: 10
    statCoinReward: 1
    coinReward: 8
    dropItems:
      - "iron_blade"
      - "leather_guard"
    dropChances:
      iron_blade: 0.20
      leather_guard: 0.15
      
  weak_skeleton:
    name: "약한 스켈레톤"  
    entityType: "SKELETON"
    baseHealth: 25.0
    baseDamage: 3.0
    minLevel: 1
    maxLevel: 8
    statCoinReward: 1
    coinReward: 6
    dropItems:
      - "iron_blade"
    dropChances:
      iron_blade: 0.15

  # 티어 2 몬스터 (레벨 8-20)
  fire_zombie:
    name: "§c화염 좀비"
    entityType: "ZOMBIE"
    baseHealth: 60.0
    baseDamage: 8.0
    minLevel: 8
    maxLevel: 20
    statCoinReward: 3
    coinReward: 20
    dropItems:
      - "flame_sword"
      - "chain_armor"
      - "miner_pick"
    dropChances:
      flame_sword: 0.12
      chain_armor: 0.08
      miner_pick: 0.06
      
  ice_skeleton:
    name: "§b얼음 스켈레톤"
    entityType: "SKELETON"
    baseHealth: 50.0
    baseDamage: 7.0
    minLevel: 10
    maxLevel: 18
    statCoinReward: 2
    coinReward: 15
    dropItems:
      - "flame_sword"
      - "chain_armor"
    dropChances:
      flame_sword: 0.10
      chain_armor: 0.12

  # 티어 3 몬스터 (레벨 18-30) - 보스급
  dragon_spider:
    name: "§5드래곤 거미"
    entityType: "SPIDER"
    baseHealth: 150.0
    baseDamage: 15.0
    minLevel: 18
    maxLevel: 30
    statCoinReward: 10
    coinReward: 80
    dropItems:
      - "dragon_blade"
      - "dragon_plate"
    dropChances:
      dragon_blade: 0.15
      dragon_plate: 0.12
      
  shadow_creeper:
    name: "§8그림자 크리퍼"
    entityType: "CREEPER"
    baseHealth: 120.0
    baseDamage: 12.0
    minLevel: 15
    maxLevel: 25
    statCoinReward: 8
    coinReward: 60
    dropItems:
      - "dragon_blade"
      - "miner_pick"
    dropChances:
      dragon_blade: 0.10
      miner_pick: 0.20

# 스폰 지역 설정
regions:
  # 초보자 지역 (레벨 1-10)
  newbie_forest:
    corner1:
      world: "rpg_world"
      x: -40.0
      y: 60.0
      z: -40.0
    corner2:
      world: "rpg_world"
      x: 40.0
      y: 85.0
      z: 40.0
    minLevel: 1
    maxLevel: 10
    monsterIds:
      - "weak_zombie"
      - "weak_skeleton"
    maxMonstersPerRegion: 8

  # 중급자 지역 (레벨 8-20)
  burning_plains:
    corner1:
      world: "rpg_world"
      x: 80.0
      y: 60.0
      z: -80.0
    corner2:
      world: "rpg_world"
      x: 200.0
      y: 90.0
      z: 80.0
    minLevel: 8
    maxLevel: 20
    monsterIds:
      - "fire_zombie"
      - "ice_skeleton"
      - "weak_zombie"  # 일부 약한 몬스터도 섞어서
    maxMonstersPerRegion: 12

  # 고급자 지역 (레벨 18-30)
  dragon_lair:
    corner1:
      world: "rpg_world"
      x: -300.0
      y: 50.0
      z: -300.0
    corner2:
      world: "rpg_world"
      x: -150.0
      y: 100.0
      z: -150.0
    minLevel: 18
    maxLevel: 30
    monsterIds:
      - "dragon_spider"
      - "shadow_creeper"
    maxMonstersPerRegion: 5  # 보스급이므로 적은 수

  # 혼합 지역 (모든 레벨)
  chaos_zone:
    corner1:
      world: "rpg_world"
      x: 250.0
      y: 60.0
      z: 250.0
    corner2:
      world: "rpg_world"
      x: 400.0
      y: 90.0
      z: 400.0
    minLevel: 1
    maxLevel: 30
    monsterIds:
      - "weak_zombie"
      - "fire_zombie"  
      - "dragon_spider"
    maxMonstersPerRegion: 15
```

## 🔧 운영 및 관리

### 일상 관리 명령어
```bash
# 매일 확인할 명령어들
/rpgmonitor                    # 전체 성능 상태
/rpgmonitor players           # 플레이어 활동 통계
/rpgmonitor monsters          # 몬스터 현황

# 주간 관리
/rpgmonitor export            # 성능 데이터 내보내기
/rpg reload                   # 설정 다시 로드

# 문제 발생 시
/rpgmonitor optimize          # 수동 최적화
/rpg disable                  # 긴급 비활성화
/rpg enable                   # 다시 활성화
```

### 성능 튜닝 가이드

#### 소형 서버 (1-20명) 권장 설정
```yaml
performance:
  monster-spawn-interval: 200   # 스폰 간격 증가 (10초)
  cleanup-interval: 1800       # 정리 간격 (1.5분)
  player-detection-range: 48   # 감지 범위 축소
  max-spawn-attempts: 3        # 시도 횟수 감소

# 지역별 최대 몬스터 수도 줄이기
regions:
  newbie_forest:
    maxMonstersPerRegion: 5    # 8 -> 5
  burning_plains:
    maxMonstersPerRegion: 8    # 12 -> 8
  dragon_lair:
    maxMonstersPerRegion: 3    # 5 -> 3
```

#### 중형 서버 (20-50명) 권장 설정
```yaml
performance:
  monster-spawn-interval: 120   # 6초 간격
  cleanup-interval: 1200       # 1분 간격
  player-detection-range: 64   # 기본값 유지
  max-spawn-attempts: 5        # 기본값 유지

# 기본 설정 그대로 사용하되 모니터링 강화
monitoring:
  snapshot-interval: 20        # 20초마다 스냅샷
  auto-optimization: true      # 자동 최적화 활성화
```

#### 대형 서버 (50명+) 권장 설정
```yaml
performance:
  monster-spawn-interval: 80    # 4초 간격 (빠른 스폰)
  cleanup-interval: 600        # 30초 간격 (빈번한 정리)
  player-detection-range: 80   # 감지 범위 증가
  max-spawn-attempts: 7        # 시도 횟수 증가

# 지역별 몬스터 수 증가
regions:
  newbie_forest:
    maxMonstersPerRegion: 12   # 더 많은 몬스터
  burning_plains:
    maxMonstersPerRegion: 18
  dragon_lair:
    maxMonstersPerRegion: 8
  chaos_zone:
    maxMonstersPerRegion: 25   # 대형 서버용 대규모 지역
```

### 데이터베이스 연동 (고급)

#### MySQL 연동 예제 (선택사항)
```java
// CustomRPG-MySQL 확장 플러그인 예제
public class MySQLIntegration extends JavaPlugin {
    
    private Connection connection;
    
    @Override
    public void onEnable() {
        // MySQL 연결
        connectToDatabase();
        
        // 테이블 생성
        createTables();
        
        // CustomRPG 이벤트 리스닝
        getServer().getPluginManager().registerEvents(new DatabaseListener(), this);
    }
    
    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/minecraft_rpg";
            String username = "minecraft";
            String password = "your_password";
            
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("MySQL 데이터베이스에 연결되었습니다.");
            
        } catch (SQLException e) {
            getLogger().severe("데이터베이스 연결 실패: " + e.getMessage());
        }
    }
    
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // 플레이어 스텟 테이블
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid VARCHAR(36) PRIMARY KEY,
                    player_name VARCHAR(16),
                    health DOUBLE DEFAULT 0,
                    damage DOUBLE DEFAULT 0,
                    defense DOUBLE DEFAULT 0,
                    speed DOUBLE DEFAULT 0,
                    mining DOUBLE DEFAULT 0,
                    stat_coins INT DEFAULT 0,
                    coins INT DEFAULT 0,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            """);
            
            // 몬스터 처치 로그
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS monster_kills (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36),
                    monster_id VARCHAR(50),
                    monster_level INT,
                    kill_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    stat_coins_gained INT,
                    coins_gained INT
                )
            """);
            
            // 아이템 드롭 로그
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS item_drops (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36),
                    item_id VARCHAR(50),
                    drop_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    item_stats TEXT
                )
            """);
            
        } catch (SQLException e) {
            getLogger().severe("테이블 생성 실패: " + e.getMessage());
        }
    }
}
```

## 📊 운영 통계 및 분석

### 성능 벤치마크 기준
```yaml
# 양호한 성능 기준
performance_targets:
  tps: ">= 18.0"              # TPS 18 이상
  memory_usage: "< 60%"       # 메모리 사용률 60% 미만
  monster_density: "< 0.5"    # 플레이어당 몬스터 0.5마리 미만
  response_time: "< 50ms"     # API 응답시간 50ms 미만

# 경고 기준
warning_thresholds:
  tps: "< 15.0"               # TPS 15 미만 시 경고
  memory_usage: "> 80%"       # 메모리 80% 이상 시 경고
  monster_count: "> 500"      # 총 몬스터 500마리 이상 시 경고
```

### 로그 분석 도구
```bash
# 서버 로그에서 CustomRPG 관련 정보 필터링
grep "CustomRPG" server.log | tail -100

# 성능 관련 로그만 추출
grep -E "(메모리|TPS|최적화)" server.log

# 오늘 발생한 오류만 확인
grep "$(date +%Y-%m-%d)" server.log | grep -i error
```

## 🔄 백업 및 복구

### 자동 백업 스크립트
```bash
#!/bin/bash
# CustomRPG 자동 백업 스크립트

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/customrpg"
PLUGIN_DIR="/server/plugins/CustomRPG"

# 백업 디렉토리 생성
mkdir -p $BACKUP_DIR

# 설정 파일 백업
tar -czf "$BACKUP_DIR/customrpg_config_$DATE.tar.gz" \
    "$PLUGIN_DIR/config.yml" \
    "$PLUGIN_DIR/items.yml" \
    "$PLUGIN_DIR/monsters.yml" \
    "$PLUGIN_DIR/players.yml"

# RPG 월드 백업 (선택사항)
tar -czf "$BACKUP_DIR/rpg_world_$DATE.tar.gz" "/server/rpg_world"

# 오래된 백업 삭제 (30일 이상)
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete

echo "CustomRPG 백업 완료: $DATE"
```

### 복구 절차
```bash
# 1. 서버 중지
screen -S minecraft -X stuff "stop\n"

# 2. 백업에서 복구
cd /server/plugins/CustomRPG
tar -xzf /backups/customrpg/customrpg_config_YYYYMMDD_HHMMSS.tar.gz

# 3. 권한 설정
chown -R minecraft:minecraft /server/plugins/CustomRPG

# 4. 서버 시작
screen -S minecraft -X stuff "java -jar server.jar\n"
```

## 🚨 문제 해결 체크리스트

### 일반적인 문제들

#### 1. RPG 월드가 생성되지 않는 경우
```bash
# 체크사항
□ Java 17+ 사용 확인
□ 서버 폴더 쓰기 권한 확인
□ 디스크 여유 공간 확인 (최소 1GB)
□ 다른 월드 생성 플러그인과의 충돌 확인

# 해결 방법
/rpg warp rpg_world           # 수동 월드 이동 시도
/rpg reload                   # 플러그인 다시 로드
```

#### 2. 몬스터가 스폰되지 않는 경우
```bash
# 체크사항
□ 플러그인 활성화 상태 (/rpg enable)
□ 스폰 지역 설정 확인 (/rpg region list)
□ 해당 지역에 플레이어가 64블록 내에 있는지
□ 청크가 로드되어 있는지

# 디버그 모드 활성화
debug-mode: true              # config.yml에서 설정
/rpgmonitor monsters          # 몬스터 현황 확인
```

#### 3. 스텟이 적용되지 않는 경우
```bash
# 체크사항
□ 아이템이 올바르게 착용되어 있는지
□ 아이템이 실제 커스텀 아이템인지 확인
□ 플레이어가 서버에 재접속했는지

# 해결 방법
/rpg stats                    # 현재 스텟 확인
/rpg reload                   # 강제 새로고침
```

#### 4. 성능 저하 문제
```bash
# 체크사항
□ 메모리 사용량 (/rpgmonitor memory)
□ 활성 몬스터 수 (/rpgmonitor monsters)
□ TPS 상태 (/rpgmonitor)

# 즉시 해결
/rpgmonitor optimize          # 수동 최적화
/rpg disable && /rpg enable   # 플러그인 재시작
```

## 🔗 외부 플러그인 연동 가이드

### 인기 플러그인들과의 호환성

#### Vault 경제 플러그인 연동
```java
// VaultAPI를 통한 경제 연동
public void integrateWithVault() {
    if (getServer().getPluginManager().getPlugin("Vault") != null) {
        RegisteredServiceProvider<Economy> rsp = getServer()
            .getServicesManager().getRegistration(Economy.class);
        
        if (rsp != null) {
            Economy econ = rsp.getProvider();
            
            // CustomRPG 코인을 Vault 경제로 변환
            Player player = ...;
            int rpgCoins = CustomRPGAPI.getPlayerStats(player).coins;
            double vaultMoney = rpgCoins * 0.1; // 10:1 비율
            
            econ.depositPlayer(player, vaultMoney);
        }
    }
}
```

#### WorldGuard 지역 보호 연동
```java
// WorldGuard와 연동하여 특정 지역에서만 RPG 기능 활성화
@EventHandler
public void onCustomMonsterSpawn(CustomRPGAPI.Events.CustomMonsterSpawnEvent event) {
    Location loc = event.getEntity().getLocation();
    
    // WorldGuard 지역 확인
    if (isInProtectedRegion(loc, "no-rpg")) {
        event.setCancelled(true);
    }
}
```

#### PlaceholderAPI 연동
```java
// PlaceholderAPI를 통해 다른 플러그인에서 RPG 스텟 사용
public class CustomRPGPlaceholders extends PlaceholderExpansion {
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";
        
        PlayerStatsManager.PlayerStats stats = CustomRPGAPI.getPlayerStats(player);
        
        switch (identifier) {
            case "health": return String.valueOf(stats.health);
            case "damage": return String.valueOf(stats.damage);
            case "defense": return String.valueOf(stats.defense);
            case "coins": return String.valueOf(stats.coins);
            case "stat_coins": return String.valueOf(stats.statCoins);
            default: return "";
        }
    }
}
```

## 📈 확장 계획 및 업데이트

### 향후 추가 예정 기능
```yaml
# v1.1.0 계획
planned_features:
  - skill_trees: "스킬 트리 시스템"
  - guilds: "길드 시스템 내장"
  - dungeons: "인스턴스 던전"
  - crafting: "커스텀 제작 시스템"
  - achievements: "업적 시스템"

# v1.2.0 계획  
advanced_features:
  - web_dashboard: "웹 기반 관리 대시보드"
  - api_expansion: "RESTful API 제공"
  - multi_server: "멀티 서버 동기화"
  - machine_learning: "AI 기반 밸런싱"
```

### 커뮤니티 기여 가이드
```markdown
# 기여 방법
1. GitHub 이슈 제출
2. 기능 제안 및 토론
3. 코드 기여 (Pull Request)
4. 번역 지원
5. 문서 개선

# 개발 환경 설정
- Java 17+
- Maven 3.8+
- IntelliJ IDEA 권장
- Spigot/Paper API 지식 필요
```

## 🎖️ 최종 점검 체크리스트

### 운영 전 필수 체크
```bash
□ CustomRPG.jar 파일 배치 완료
□ 첫 서버 실행 후 RPG 월드 생성 확인
□ config.yml, items.yml, monsters.yml 설정 완료
□ 기본 스폰 지역 3개 이상 생성
□ 커스텀 아이템 5개 이상 생성
□ 커스텀 몬스터 5개 이상 생성
□ 성능 모니터링 활성화 확인
□ 자동 백업 스크립트 설정
□ 관리자 권한 설정 완료
□ 플레이어 가이드 작성 완료
```

### 정기 점검 항목 (주간)
```bash
□ /rpgmonitor로 전체 성능 확인
□ /rpgmonitor export로 데이터 백업
□ 플레이어 피드백 수집 및 반영
□ 새로운 컨텐츠 추가 검토
□ 서버 로그 오류 확인
□ 백업 파일 상태 점검
```

이제 CustomRPG 플러그인이 완전히 준비되었습니다! 🎉

**핵심 특징 요약:**
- ✅ 자동 RPG 월드 생성 (평지, 0,64,0 스폰)
- ✅ 최적화된 몬스터 스포너 (플레이어 감지, 청크 로드 확인)
- ✅ 완전한 API (70+ 메소드, 이벤트 시스템)
- ✅ 성능 모니터링 및 자동 최적화
- ✅ 다른 플러그인과의 호환성 보장
- ✅ 상세한 설정 가이드 및 예제 제공

서버 규모에 맞는 설정을 선택하고, 제공된 예제를 참고하여 자신만의 RPG 컨텐츠를 만들어보세요!