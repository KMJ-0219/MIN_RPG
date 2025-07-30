package com.customrpg.plugin;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.Random;

/**
 * RPG 전용 평지 월드 생성기
 * 성능 최적화를 위한 최소한의 청크 생성
 */
public class RPGWorldGenerator extends ChunkGenerator {
    
    private CustomRPGPlugin plugin;
    
    public RPGWorldGenerator(CustomRPGPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunkData = createChunkData(world);
        
        // 0, 0 청크에만 잔디 블록 생성 (스폰 지점)
        if (chunkX == 0 && chunkZ == 0) {
            chunkData.setBlock(0, 64, 0, Material.GRASS_BLOCK);
            // 스폰 보호를 위한 기본 플랫폼 (3x3)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    chunkData.setBlock(x + 8, 63, z + 8, Material.DIRT);
                    if (x == 0 && z == 0) {
                        chunkData.setBlock(x + 8, 64, z + 8, Material.GRASS_BLOCK);
                    }
                }
            }
        }
        
        return chunkData;
    }
    
    /**
     * 월드 생성 및 관리
     */
    public static class WorldManager {
        private static final String RPG_WORLD_NAME = "rpg_world";
        private CustomRPGPlugin plugin;
        
        public WorldManager(CustomRPGPlugin plugin) {
            this.plugin = plugin;
        }
        
        /**
         * RPG 월드 생성 또는 로드
         */
        public World createOrLoadRPGWorld() {
            World rpgWorld = plugin.getServer().getWorld(RPG_WORLD_NAME);
            
            if (rpgWorld == null) {
                plugin.getLogger().info("RPG 월드를 생성하는 중...");
                
                WorldCreator creator = new WorldCreator(RPG_WORLD_NAME);
                creator.generator(new RPGWorldGenerator(plugin));
                creator.type(WorldType.FLAT);
                creator.generateStructures(false); // 구조물 생성 비활성화 (성능 최적화)
                
                rpgWorld = creator.createWorld();
                
                if (rpgWorld != null) {
                    // 월드 설정 최적화
                    rpgWorld.setSpawnFlags(false, false); // 동물, 몬스터 자연 스폰 비활성화
                    rpgWorld.setKeepSpawnInMemory(false); // 스폰 청크 메모리 상주 비활성화
                    rpgWorld.setAutoSave(true); // 자동 저장 활성화
                    
                    // 스폰 지점 설정
                    rpgWorld.setSpawnLocation(0, 65, 0);
                    
                    plugin.getLogger().info("RPG 월드가 성공적으로 생성되었습니다: " + RPG_WORLD_NAME);
                } else {
                    plugin.getLogger().severe("RPG 월드 생성에 실패했습니다!");
                }
            } else {
                plugin.getLogger().info("기존 RPG 월드를 로드했습니다: " + RPG_WORLD_NAME);
            }
            
            return rpgWorld;
        }
        
        /**
         * 플레이어를 RPG 월드로 이동
         */
        public boolean teleportToRPGWorld(Player player) {
            World rpgWorld = plugin.getServer().getWorld(RPG_WORLD_NAME);
            if (rpgWorld == null) {
                rpgWorld = createOrLoadRPGWorld();
            }
            
            if (rpgWorld != null) {
                Location spawnLocation = new Location(rpgWorld, 0.5, 65, 0.5);
                player.teleport(spawnLocation);
                player.sendMessage(ChatColor.GREEN + "RPG 월드로 이동했습니다!");
                
                // 첫 방문 시 안내 메시지
                if (!player.hasPlayedBefore() || !player.hasPermission("customrpg.veteran")) {
                    sendWelcomeMessage(player);
                }
                
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "RPG 월드 이동에 실패했습니다.");
                return false;
            }
        }
        
        private void sendWelcomeMessage(Player player) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "=== RPG 월드에 오신 것을 환영합니다! ===");
            player.sendMessage(ChatColor.YELLOW + "• /rpg stats - 내 스텟 확인");
            player.sendMessage(ChatColor.YELLOW + "• Shift+클릭 - 커스텀 아이템 정보 확인");
            player.sendMessage(ChatColor.YELLOW + "• 몬스터를 잡아서 스텟코인과 장비를 획득하세요!");
            player.sendMessage("");
        }
        
        /**
         * RPG 월드 체크
         */
        public boolean isRPGWorld(World world) {
            return world != null && RPG_WORLD_NAME.equals(world.getName());
        }
        
        /**
         * RPG 월드 가져오기 (API용)
         */
        public World getRPGWorld() {
            return plugin.getServer().getWorld(RPG_WORLD_NAME);
        }
    }
}