package com.customrpg.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class RPGCommandExecutor implements CommandExecutor {
    
    private CustomRPGPlugin plugin;
    
    public RPGCommandExecutor(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "enable":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                plugin.setPluginEnabled(true);
                player.sendMessage(ChatColor.GREEN + "CustomRPG 플러그인이 활성화되었습니다.");
                break;
                
            case "disable":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                plugin.setPluginEnabled(false);
                player.sendMessage(ChatColor.RED + "CustomRPG 플러그인이 비활성화되었습니다.");
                break;
                
            case "stats":
                showPlayerStats(player);
                break;
                
            case "warp":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "사용법: /rpg warp <월드이름>");
                    return true;
                }
                warpToRPGWorld(player, args[1]);
                break;
                
            case "region":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                handleRegionCommand(player, args);
                break;
                
            case "spawn":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                handleSpawnCommand(player, args);
                break;
                
            case "give":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                handleGiveCommand(player, args);
                break;
                
            case "reload":
                if (!player.hasPermission("customrpg.admin")) {
                    player.sendMessage(ChatColor.RED + "권한이 없습니다.");
                    return true;
                }
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "설정이 다시 로드되었습니다.");
                break;
                
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== CustomRPG 명령어 ===");
        player.sendMessage(ChatColor.YELLOW + "/rpg stats - 내 스텟 확인");
        player.sendMessage(ChatColor.YELLOW + "/rpg warp <월드> - RPG 월드로 이동");
        
        if (player.hasPermission("customrpg.admin")) {
            player.sendMessage(ChatColor.AQUA + "=== 관리자 명령어 ===");
            player.sendMessage(ChatColor.AQUA + "/rpg enable/disable - 플러그인 활성화/비활성화");
            player.sendMessage(ChatColor.AQUA + "/rpg region create <이름> - 스폰 지역 생성");
            player.sendMessage(ChatColor.AQUA + "/rpg region list - 스폰 지역 목록");
            player.sendMessage(ChatColor.AQUA + "/rpg spawn <몬스터ID> [레벨] - 몬스터 스폰");
            player.sendMessage(ChatColor.AQUA + "/rpg give <아이템ID> [플레이어] - 커스텀 아이템 지급");
            player.sendMessage(ChatColor.AQUA + "/rpg reload - 설정 다시 로드");
        }
    }
    
    private void showPlayerStats(Player player) {
        PlayerStatsManager.PlayerStats stats = plugin.getStatsManager().getPlayerStats(player);
        
        player.sendMessage(ChatColor.GOLD + "=== " + player.getName() + "의 스텟 ===");
        player.sendMessage(ChatColor.RED + "체력: +" + stats.health);
        player.sendMessage(ChatColor.DARK_RED + "공격력: +" + stats.damage);
        player.sendMessage(ChatColor.BLUE + "방어력: +" + stats.defense);
        player.sendMessage(ChatColor.YELLOW + "속도: +" + stats.speed);
        player.sendMessage(ChatColor.GREEN + "채굴속도: +" + stats.mining);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "스텟 코인: " + stats.statCoins);
        player.sendMessage(ChatColor.GOLD + "코인: " + stats.coins);
    }
    
    private void warpToRPGWorld(Player player, String worldName) {
        // RPG 월드로 이동하는 경우 특별 처리
        if ("rpg".equalsIgnoreCase(worldName) || "rpg_world".equalsIgnoreCase(worldName)) {
            plugin.getWorldManager().teleportToRPGWorld(player);
            return;
        }
        
        // 기존 방식 (다른 월드)
        if (plugin.getServer().getWorld(worldName) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 월드입니다: " + worldName);
            return;
        }
        
        Location warpLocation = new Location(plugin.getServer().getWorld(worldName), 0, 100, 0);
        // 안전한 위치 찾기
        warpLocation = warpLocation.getWorld().getHighestBlockAt(warpLocation).getLocation().add(0, 1, 0);
        
        player.teleport(warpLocation);
        player.sendMessage(ChatColor.GREEN + worldName + " 월드로 이동했습니다!");
    }
    
    private void handleRegionCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "사용법: /rpg region <create|list|delete> [이름]");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "사용법: /rpg region create <이름>");
                    return;
                }
                createRegion(player, args[2]);
                break;
                
            case "list":
                listRegions(player);
                break;
                
            case "delete":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "사용법: /rpg region delete <이름>");
                    return;
                }
                deleteRegion(player, args[2]);
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "사용법: /rpg region <create|list|delete> [이름]");
                break;
        }
    }
    
    private void createRegion(Player player, String regionName) {
        // 간단한 예시: 플레이어 위치 기준으로 10x10x10 영역 생성
        Location center = player.getLocation();
        Location corner1 = center.clone().add(-5, -2, -5);
        Location corner2 = center.clone().add(5, 8, 5);
        
        plugin.getMonsterManager().createSpawnRegion(regionName, corner1, corner2, 1, 10, 
            java.util.Arrays.asList(), 5);
        
        player.sendMessage(ChatColor.GREEN + "스폰 지역 '" + regionName + "'이(가) 생성되었습니다!");
        player.sendMessage(ChatColor.YELLOW + "위치: " + corner1.getBlockX() + "," + corner1.getBlockY() + "," + corner1.getBlockZ() +
                          " ~ " + corner2.getBlockX() + "," + corner2.getBlockY() + "," + corner2.getBlockZ());
    }
    
    private void listRegions(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 스폰 지역 목록 ===");
        for (String regionName : plugin.getMonsterManager().getSpawnRegionNames()) {
            player.sendMessage(ChatColor.YELLOW + "- " + regionName);
        }
    }
    
    private void deleteRegion(Player player, String regionName) {
        if (plugin.getMonsterManager().getSpawnRegion(regionName) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 지역입니다: " + regionName);
            return;
        }
        
        plugin.getMonsterManager().removeSpawnRegion(regionName);
        player.sendMessage(ChatColor.GREEN + "스폰 지역 '" + regionName + "'이(가) 삭제되었습니다.");
    }
    
    private void handleSpawnCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "사용법: /rpg spawn <몬스터ID> [레벨]");
            return;
        }
        
        String monsterId = args[1];
        int level = 1;
        
        if (args.length >= 3) {
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "올바른 레벨을 입력해주세요.");
                return;
            }
        }
        
        if (plugin.getMonsterManager().getCustomMonster(monsterId) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 몬스터 ID입니다: " + monsterId);
            return;
        }
        
        plugin.getMonsterManager().spawnCustomMonster(monsterId, player.getLocation(), level);
        player.sendMessage(ChatColor.GREEN + monsterId + " [Lv." + level + "]을(를) 소환했습니다!");
    }
    
    private void handleGiveCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "사용법: /rpg give <아이템ID> [플레이어]");
            return;
        }
        
        String itemId = args[1];
        Player target = player;
        
        if (args.length >= 3) {
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "온라인 상태가 아닌 플레이어입니다: " + args[2]);
                return;
            }
        }
        
        if (plugin.getItemManager().getCustomItem(itemId) == null) {
            player.sendMessage(ChatColor.RED + "존재하지 않는 아이템 ID입니다: " + itemId);
            return;
        }
        
        plugin.getItemManager().giveItemToPlayer(target, itemId, true);
        player.sendMessage(ChatColor.GREEN + target.getName() + "에게 " + itemId + "을(를) 지급했습니다!");
        
        if (!target.equals(player)) {
            target.sendMessage(ChatColor.GREEN + "커스텀 아이템을 받았습니다: " + itemId);
        }
    }
}