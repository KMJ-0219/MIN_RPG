package com.customrpg.plugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

// RPG 월드 매니저
class RPGWorldManager {
    private CustomRPGPlugin plugin;
    private Map<String, Location> warpPoints;
    
    public RPGWorldManager(CustomRPGPlugin plugin) {
        this.plugin = plugin;
        this.warpPoints = new HashMap<>();
        loadWarpPoints();
    }
    
    public void setWarpPoint(String name, Location location) {
        warpPoints.put(name, location);
        saveWarpPoint(name, location);
    }
    
    public Location getWarpPoint(String name) {
        return warpPoints.get(name);
    }
    
    public boolean warpPlayer(Player player, String warpName) {
        Location warpLocation = warpPoints.get(warpName);
        if (warpLocation == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 워프 지점입니다: " + warpName);
            return false;
        }
        
        player.teleport(warpLocation);
        player.sendMessage(ChatColor.GREEN + warpName + " 지점으로 이동했습니다!");
        return true;
    }
    
    private void saveWarpPoint(String name, Location location) {
        String path = "warps." + name;
        plugin.getConfig().set(path + ".world", location.getWorld().getName());
        plugin.getConfig().set(path + ".x", location.getX());
        plugin.getConfig().set(path + ".y", location.getY());
        plugin.getConfig().set(path + ".z", location.getZ());
        plugin.getConfig().set(path + ".yaw", location.getYaw());
        plugin.getConfig().set(path + ".pitch", location.getPitch());
        plugin.saveConfig();
    }
    
    private void loadWarpPoints() {
        if (plugin.getConfig().contains("warps")) {
            for (String name : plugin.getConfig().getConfigurationSection("warps").getKeys(false)) {
                String path = "warps." + name;
                String worldName = plugin.getConfig().getString(path + ".world");
                World world = plugin.getServer().getWorld(worldName);
                
                if (world != null) {
                    Location location = new Location(
                        world,
                        plugin.getConfig().getDouble(path + ".x"),
                        plugin.getConfig().getDouble(path + ".y"),
                        plugin.getConfig().getDouble(path + ".z"),
                        (float) plugin.getConfig().getDouble(path + ".yaw"),
                        (float) plugin.getConfig().getDouble(path + ".pitch")
                    );
                    warpPoints.put(name, location);
                }
            }
        }
    }
}

// 경제 매니저
class EconomyManager {
    private CustomRPGPlugin plugin;
    
    public EconomyManager(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean buyItem(Player player, String itemId, int cost) {
        if (plugin.getStatsManager().spendCoins(player, cost)) {
            plugin.getItemManager().giveItemToPlayer(player, itemId, true);
            player.sendMessage(ChatColor.GREEN + "아이템을 구매했습니다: " + itemId);
            player.sendMessage(ChatColor.YELLOW + "소모된 코인: " + cost);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "코인이 부족합니다. 필요한 코인: " + cost);
            return false;
        }
    }
    
    public boolean upgradeStats(Player player, String statType, int levels) {
        int costPerLevel = 10; // 스텟 업그레이드당 10 스텟코인
        int totalCost = costPerLevel * levels;
        
        if (plugin.getStatsManager().spendStatCoins(player, totalCost)) {
            PlayerStatsManager.PlayerStats stats = plugin.getStatsManager().getPlayerStats(player);
            
            switch (statType.toLowerCase()) {
                case "health":
                    stats.health += levels * 2; // 레벨당 체력 2 증가
                    break;
                case "damage":
                    stats.damage += levels * 1; // 레벨당 공격력 1 증가
                    break;
                case "defense":
                    stats.defense += levels * 1; // 레벨당 방어력 1 증가
                    break;
                case "speed":
                    stats.speed += levels * 1; // 레벨당 속도 1 증가
                    break;
                case "mining":
                    stats.mining += levels * 1; // 레벨당 채굴속도 1 증가
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "잘못된 스텟 타입입니다: " + statType);
                    return false;
            }
            
            plugin.getStatsManager().savePlayerData(player);
            plugin.getStatsManager().applyStatsToPlayer(player);
            
            player.sendMessage(ChatColor.GREEN + statType + " 스텟이 " + levels + " 만큼 증가했습니다!");
            player.sendMessage(ChatColor.YELLOW + "소모된 스텟코인: " + totalCost);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "스텟코인이 부족합니다. 필요한 스텟코인: " + totalCost);
            return false;
        }
    }
}

// 아이템 명령어 실행기
class ItemCommandExecutor implements org.bukkit.command.CommandExecutor {
    private CustomRPGPlugin plugin;
    
    public ItemCommandExecutor(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, 
                           String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("customrpg.admin")) {
            player.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            sendItemHelpMessage(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateItem(player, args);
                break;
            case "list":
                listCustomItems(player);
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "사용법: /customitem delete <아이템ID>");
                    return true;
                }
                deleteCustomItem(player, args[1]);
                break;
            default:
                sendItemHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void sendItemHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 커스텀 아이템 명령어 ===");
        player.sendMessage(ChatColor.YELLOW + "/customitem create <ID> <이름> <재료> - 새 아이템 생성");
        player.sendMessage(ChatColor.YELLOW + "/customitem list - 아이템 목록 확인");
        player.sendMessage(ChatColor.YELLOW + "/customitem delete <ID> - 아이템 삭제");
        player.sendMessage(ChatColor.AQUA + "예시: /customitem create super_sword 전설의검 DIAMOND_SWORD");
    }
    
    private void handleCreateItem(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "사용법: /customitem create <ID> <이름> <재료>");
            return;
        }
        
        String itemId = args[1];
        String itemName = args[2];
        String materialName = args[3];
        
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
            
            // 기본 스텟으로 아이템 생성
            Map<String, Double> stats = new HashMap<>();
            stats.put("health", 10.0);
            stats.put("damage", 5.0);
            
            plugin.getItemManager().createCustomItem(itemId, itemName, material, stats, 0.1);
            player.sendMessage(ChatColor.GREEN + "커스텀 아이템이 생성되었습니다: " + itemId);
            player.sendMessage(ChatColor.YELLOW + "추가 설정은 items.yml 파일에서 수정하세요.");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "잘못된 재료 타입입니다: " + materialName);
        }
    }
    
    private void listCustomItems(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 커스텀 아이템 목록 ===");
        for (String itemId : plugin.getItemManager().getCustomItemIds()) {
            CustomItemManager.CustomItem item = plugin.getItemManager().getCustomItem(itemId);
            player.sendMessage(ChatColor.YELLOW + "- " + itemId + " (" + item.name + ")");
        }
    }
    
    private void deleteCustomItem(Player player, String itemId) {
        if (plugin.getItemManager().getCustomItem(itemId) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 아이템 ID입니다: " + itemId);
            return;
        }
        
        plugin.getItemManager().removeCustomItem(itemId);
        player.sendMessage(ChatColor.GREEN + "커스텀 아이템이 삭제되었습니다: " + itemId);
    }
}

// 몬스터 명령어 실행기
class MonsterCommandExecutor implements org.bukkit.command.CommandExecutor {
    private CustomRPGPlugin plugin;
    
    public MonsterCommandExecutor(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, 
                           String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("customrpg.admin")) {
            player.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }
        
        if (args.length == 0) {
            sendMonsterHelpMessage(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateMonster(player, args);
                break;
            case "list":
                listCustomMonsters(player);
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "사용법: /custommonster delete <몬스터ID>");
                    return true;
                }
                deleteCustomMonster(player, args[1]);
                break;
            default:
                sendMonsterHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void sendMonsterHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 커스텀 몬스터 명령어 ===");
        player.sendMessage(ChatColor.YELLOW + "/custommonster create <ID> <이름> <엔티티타입> - 새 몬스터 생성");
        player.sendMessage(ChatColor.YELLOW + "/custommonster list - 몬스터 목록 확인");
        player.sendMessage(ChatColor.YELLOW + "/custommonster delete <ID> - 몬스터 삭제");
        player.sendMessage(ChatColor.AQUA + "예시: /custommonster create fire_zombie 화염좀비 ZOMBIE");
    }
    
    private void handleCreateMonster(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "사용법: /custommonster create <ID> <이름> <엔티티타입>");
            return;
        }
        
        String monsterId = args[1];
        String monsterName = args[2];
        String entityTypeName = args[3];
        
        try {
            org.bukkit.entity.EntityType entityType = org.bukkit.entity.EntityType.valueOf(entityTypeName.toUpperCase());
            
            plugin.getMonsterManager().createCustomMonster(monsterId, monsterName, entityType, 
                40.0, 4.0, 1, 20, 2, 20, 
                java.util.Arrays.asList(), new HashMap<>());
            
            player.sendMessage(ChatColor.GREEN + "커스텀 몬스터가 생성되었습니다: " + monsterId);
            player.sendMessage(ChatColor.YELLOW + "추가 설정은 monsters.yml 파일에서 수정하세요.");
            
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "잘못된 엔티티 타입입니다: " + entityTypeName);
        }
    }
    
    private void listCustomMonsters(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 커스텀 몬스터 목록 ===");
        for (String monsterId : plugin.getMonsterManager().getCustomMonsterIds()) {
            CustomMonsterManager.CustomMonster monster = plugin.getMonsterManager().getCustomMonster(monsterId);
            player.sendMessage(ChatColor.YELLOW + "- " + monsterId + " (" + monster.name + ")");
        }
    }
    
    private void deleteCustomMonster(Player player, String monsterId) {
        if (plugin.getMonsterManager().getCustomMonster(monsterId) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 몬스터 ID입니다: " + monsterId);
            return;
        }
        
        plugin.getMonsterManager().removeCustomMonster(monsterId);
        player.sendMessage(ChatColor.GREEN + "커스텀 몬스터가 삭제되었습니다: " + monsterId);
    }
}