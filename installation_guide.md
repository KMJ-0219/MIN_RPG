# CustomRPG 플러그인 가이드

## 📋 개요
CustomRPG는 마인크래프트에서 RPG 요소를 추가하는 플러그인입니다. 커스텀 몬스터, 장비, 스텟 시스템을 제공합니다.

## 🚀 설치 방법

### 1. 필요 사항
- Minecraft 1.20+ 서버
- Java 17+
- Spigot/Paper 서버

### 2. 설치 단계
1. 플러그인 JAR 파일을 `plugins/` 폴더에 복사
2. 서버 재시작
3. 자동 생성된 설정 파일들 확인

### 3. 폴더 구조
```
plugins/CustomRPG/
├── config.yml      # 메인 설정
├── items.yml       # 커스텀 아이템 설정
├── monsters.yml    # 커스텀 몬스터 및 스폰 지역 설정
└── players.yml     # 플레이어 데이터 (자동 생성)
```

## ⚙️ 주요 기능

### 1. 스텟 시스템
- **체력 (Health)**: 최대 체력 증가
- **공격력 (Damage)**: 추가 데미지
- **방어력 (Defense)**: 받는 피해 감소
- **속도 (Speed)**: 이동 속도 증가
- **채굴속도 (Mining)**: 블록 파괴 속도 증가

### 2. 커스텀 장비
- 기존 마인크래프트 아이템에 특별한 스텟 부여
- 랜덤 스텟 범위 (원래 스텟의 50%~150%)
- 장비 착용 시 자동 스텟 적용
- NBT 데이터를 통한 영구 저장

### 3. 커스텀 몬스터
- 기존 몬스터를 기반으로 한 강화 버전
- 레벨 시스템 (레벨에 따른 체력/공격력 증가)
- 커스텀 드롭 아이템 및 확률 설정
- 스텟코인, 일반코인 보상

### 4. 스폰 지역 시스템
- 특정 지역에 몬스터 자동 스폰
- 레벨 범위 설정
- 지역별 최대 몬스터 수 제한
- 다중 몬스터 타입 지원

## 🎮 플레이어 명령어

### 기본 명령어
```
/rpg stats              # 내 스텟 확인
/rpg warp <월드이름>    # RPG 월드로 이동
```

## 🛠️ 관리자 명령어

### RPG 관리
```
/rpg enable             # 플러그인 활성화
/rpg disable            # 플러그인 비활성화
/rpg reload             # 설정 다시 로드
/rpg give <아이템ID> [플레이어]  # 커스텀 아이템 지급
/rpg spawn <몬스터ID> [레벨]    # 몬스터 스폰
```

### 지역 관리
```
/rpg region create <이름>     # 현재 위치에 스폰 지역 생성
/rpg region list             # 스폰 지역 목록 확인
/rpg region delete <이름>    # 스폰 지역 삭제
```

### 커스텀 아이템 관리
```
/customitem create <ID> <이름> <재료>  # 새 아이템 생성
/customitem list                      # 아이템 목록 확인
/customitem delete <ID>               # 아이템 삭제
```

### 커스텀 몬스터 관리
```
/custommonster create <ID> <이름> <엔티티타입>  # 새 몬스터 생성
/custommonster list                           # 몬스터 목록 확인
/custommonster delete <ID>                    # 몬스터 삭제
```

## 📝 설정 가이드

### 커스텀 아이템 생성 예시 (items.yml)
```yaml
items:
  flame_sword:
    name: "화염의 검"
    material: "NETHERITE_SWORD"
    dropChance: 0.05
    stats:
      health: 15.0
      damage: 12.0
      speed: 3.0
```

### 커스텀 몬스터 생성 예시 (monsters.yml)
```yaml
monsters:
  shadow_creeper:
    name: "그림자 크리퍼"
    entityType: "CREEPER"
    baseHealth: 60.0
    baseDamage: 8.0
    minLevel: 5
    maxLevel: 20
    statCoinReward: 4
    coinReward: 40
    dropItems:
      - "flame_sword"
    dropChances:
      flame_sword: 0.15
```

### 스폰 지역 설정 예시
```yaml
regions:
  desert_zone:
    corner1:
      world: "rpg_world"
      x: 0.0
      y: 60.0
      z: 0.0
    corner2:
      world: "rpg_world"
      x: 100.0
      y: 80.0
      z: 100.0
    minLevel: 10
    maxLevel: 25
    monsterIds:
      - "shadow_creeper"
      - "flame_zombie"
    maxMonstersPerRegion: 15
```

## 🎯 사용 팁

### 플레이어용
1. **장비 착용**: 커스텀 장비를 착용하거나 손에 들면 자동으로 스텯이 적용됩니다
2. **스텟 확인**: `/rpg stats` 명령어로 현재 스텟을 확인하세요
3. **아이템 정보**: 커스텀 아이템을 Shift+클릭하면 상세 정보를 볼 수 있습니다
4. **몬스터 사냥**: 높은 레벨 몬스터일수록 더 많은 보상을 드롭합니다

### 관리자용
1. **지역 설정**: `/rpg region create` 명령어는 현재 위치 기준 10x10x10 영역을 생성합니다
2. **균형 조정**: monsters.yml과 items.yml에서 스텟과 드롭율을 세밀하게 조정하세요
3. **레벨 범위**: 몬스터 레벨은 지역의 minLevel~maxLevel 범위에서 랜덤 생성됩니다
4. **드롭 확률**: 0.0~1.0 사이의 값으로 설정 (0.1 = 10% 확률)

## 🔧 권한 시스템

- `customrpg.use`: 기본 RPG 기능 사용 (기본: 모든 플레이어)
- `customrpg.admin`: 관리자 기능 사용 (기본: OP만)
- `customrpg.*`: 모든 권한 (기본: OP만)

## 🐛 문제 해결

### 자주 발생하는 문제들

1. **몬스터가 스폰되지 않음**
   - 스폰 지역이 올바르게 설정되었는지 확인
   - 플러그인이 활성화되어 있는지 확인 (`/rpg enable`)
   - 월드가 존재하는지 확인

2. **아이템 스텟이 적용되지 않음**
   - 플레이어가 서버에 재접속해보세요
   - `/rpg reload` 명령어로 플러그인을 다시 로드하세요

3. **설정 파일 오류**
   - YAML 문법이 올바른지 확인하세요
   - 들여쓰기는 스페이스 2개를 사용하세요

## 📊 성능 최적화

- 스폰 지역당 최대 몬스터 수를 적절히 설정하세요 (권장: 5-15마리)
- 너무 많은 커스텀 아이템을 동시에 생성하지 마세요
- 정기적으로 플레이어 데이터를 백업하세요

## 🔄 업데이트 및 백업

### 백업 권장 파일들
- `plugins/CustomRPG/players.yml` (플레이어 데이터)
- `plugins/CustomRPG/config.yml` (메인 설정)
- `plugins/CustomRPG/items.yml` (커스텀 아이템)
- `plugins/CustomRPG/monsters.yml` (커스텀 몬스터)

### 업데이트 시 주의사항
1. 기존 설정 파일들을 백업하세요
2. 플러그인을 비활성화한 후 교체하세요
3. 서버 재시작 후 정상 작동 확인하세요