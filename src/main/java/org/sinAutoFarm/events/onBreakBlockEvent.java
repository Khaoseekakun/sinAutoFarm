package org.sinAutoFarm.events;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinAutoFarm.SinAutoFarm;

import net.md_5.bungee.api.ChatColor;

public class onBreakBlockEvent implements Listener {

    static JavaPlugin plugin = SinAutoFarm.getPlugin(SinAutoFarm.class);
    private static final int MAX_PLANT_LIMIT = SinAutoFarm.config.getInt("limit-per-click", 256);

    private static final Map<Material, Material> CROP_SEEDS = Map.of(
            Material.WHEAT, Material.WHEAT_SEEDS,
            Material.BEETROOTS, Material.BEETROOT_SEEDS,
            Material.POTATOES, Material.POTATO,
            Material.CARROTS, Material.CARROT,
            Material.NETHER_WART, Material.NETHER_WART
    );

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block blockTarget = event.getBlock();

        if (blockTarget.getType() == Material.WHEAT) {
            if (player.isSneaking()) return;
            if (!(player.hasPermission("sinAutoFarm.wheat") || player.hasPermission("sinAutoFarm.all"))) {
                return;
            }

            int seedCount = getItemCount(player, Material.WHEAT_SEEDS);

            if (seedCount > 0) {
                removeItem(player, Material.WHEAT_SEEDS, 1);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> blockTarget.setType(Material.WHEAT), 1L);
            }
        }
        if (blockTarget.getType() == Material.CARROTS) {
            if (player.isSneaking()) return;
            if (!(player.hasPermission("sinAutoFarm.carrot") || player.hasPermission("sinAutoFarm.all"))) {
                return;
            }

            int seedCount = getItemCount(player, Material.CARROT);

            if (seedCount > 0) {
                removeItem(player, Material.CARROT, 1);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> blockTarget.setType(Material.CARROTS), 1L);
            }
        }
        if (blockTarget.getType() == Material.POTATOES) {
            if (player.isSneaking()) return;
            if (!(player.hasPermission("sinAutoFarm.potato") || player.hasPermission("sinAutoFarm.all"))) {
                return;
            }

            int seedCount = getItemCount(player, Material.POTATO);

            if (seedCount > 0) {
                removeItem(player, Material.POTATO, 1);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> blockTarget.setType(Material.POTATOES), 1L);
            }
        }
        if (blockTarget.getType() == Material.BEETROOTS) {
            if (player.isSneaking()) return;
            if (!(player.hasPermission("sinAutoFarm.beetroot") || player.hasPermission("sinAutoFarm.all"))) {
                return;
            }

            int seedCount = getItemCount(player, Material.BEETROOT_SEEDS);

            if (seedCount > 0) {
                removeItem(player, Material.BEETROOT_SEEDS, 1);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> blockTarget.setType(Material.BEETROOTS), 1L);
            }
        }
        if (blockTarget.getType() == Material.NETHER_WART) {
            if (player.isSneaking()) return;
            if (!(player.hasPermission("sinAutoFarm.nether_wart") || player.hasPermission("sinAutoFarm.all"))) {
                return;
            }

            int seedCount = getItemCount(player, Material.NETHER_WART);

            if (seedCount > 0) {
                removeItem(player, Material.NETHER_WART, 1);

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> blockTarget.setType(Material.NETHER_WART), 1L);
            }
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block cropTarget = event.getBlock();
        Material cropType = cropTarget.getType();

        if (!CROP_SEEDS.containsKey(cropType)) {
            return;
        }
        if (!player.isSneaking()) return;

        if (!(player.hasPermission("sinAutoFarm.fill." + cropType.name().toLowerCase()) || player.hasPermission("sinAutoFarm.fill.all"))) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (SinAutoFarm.cooldowns.containsKey(player.getUniqueId())) {
            long lastUsed = SinAutoFarm.cooldowns.get(player.getUniqueId());
            int COOLDOWN_TIME = SinAutoFarm.config.getInt("cooldown", 30) * 1000;

            long timeLeft = (COOLDOWN_TIME - (currentTime - lastUsed)) / 1000;

            if (timeLeft > 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(SinAutoFarm.config.getString("messages.cooldown")).replace("{time}", ""+timeLeft)));
                return;
            }
        }

        // Update cooldown
        SinAutoFarm.cooldowns.put(player.getUniqueId(), currentTime);

        Material seedType = CROP_SEEDS.get(cropType);
        int seedCount = getItemCount(player, seedType);
        if (seedCount < 1) {
            return;
        }


        int plantableSeeds = Math.min(seedCount, MAX_PLANT_LIMIT);
        plantableSeeds = plantableSeeds-1;
        Queue<Location> queue = new LinkedList<>();
        Set<Location> visited = new HashSet<>();
        queue.add(cropTarget.getLocation());
        visited.add(cropTarget.getLocation());

        while (!queue.isEmpty() && plantableSeeds > 0) {
            Location location = queue.poll();

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    Location farmlandLocation = location.clone().add(x, -1, z);
                    Block farmlandBlock = farmlandLocation.getBlock();

                    if (farmlandBlock.getType() == Material.FARMLAND || farmlandBlock.getType() == Material.SOUL_SAND) {
                        Location cropLocation = farmlandLocation.clone().add(0, 1, 0);
                        Block cropBlock = cropLocation.getBlock();

                        if (cropBlock.getType() == Material.AIR && !visited.contains(cropLocation)) {
                            if (plantableSeeds > 0) {
                                plantableSeeds--;
                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                    removeItem(player, seedType, 1);
                                    cropBlock.setType(cropType);
                                }, 1L);

                                queue.add(cropLocation);
                                visited.add(cropLocation);
                            }
                        }
                    }

                }
            }
        }
    }

    public static int getItemCount(Player player, Material itemType) {
        PlayerInventory inventory = player.getInventory();
        int count = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == itemType) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public static void removeItem(Player player, Material itemType, int amount) {
        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == itemType) {
                int itemAmount = item.getAmount();

                if (itemAmount > amount) {
                    item.setAmount(itemAmount - amount); // Reduce item count
                    break;
                } else {
                    inventory.remove(item); // Remove item if count is <= amount
                    amount -= itemAmount;
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }
    }
}