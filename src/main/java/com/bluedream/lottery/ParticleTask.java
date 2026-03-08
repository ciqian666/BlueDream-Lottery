package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class ParticleTask extends BukkitRunnable {
    private final BlueDreamLottery plugin;
    private double ticks = 0;

    public ParticleTask(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ticks += 0.5;
        LotteryManager manager = plugin.getManager();
        Map<Location, String> blocks = manager.getCachedLocations();

        for (Map.Entry<Location, String> entry : blocks.entrySet()) {
            Location loc = entry.getKey();
            if (loc == null || loc.getWorld() == null) continue;
            
            boolean anyPlayerNearby = false;
            for (org.bukkit.entity.Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(loc) < 1024) {
                    anyPlayerNearby = true;
                    break;
                }
            }
            if (!anyPlayerNearby) continue;

            LotteryPool pool = manager.getPool(entry.getValue());
            if (pool == null) continue;
            
            String pType = pool.getParticleType();
            if (pType == null || pType.equals("NONE")) continue;

            renderEffect(loc, pType);
        }
    }

    private void renderEffect(Location blockLoc, String type) {
        World world = blockLoc.getWorld();
        if (world == null) return;
        
        Location center = blockLoc.clone().add(0.5, 0.5, 0.5);

        switch (type.toUpperCase()) {
            case "VORTEX":
                renderVortex(center);
                break;
            case "HALO":
                renderHalo(center);
                break;
            case "STAR_DUST":
                renderStarDust(center);
                break;
            case "FLAME_RING":
                renderFlameRing(center);
                break;
            case "MAGIC_AURA":
                renderMagicAura(center);
                break;
        }
    }

    private void renderVortex(Location center) {
        double radius = 0.8;
        for (int i = 0; i < 2; i++) {
            double angle = ticks * 0.2 + (i * Math.PI);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (Math.sin(ticks * 0.1 + (i * Math.PI)) * 0.5) + 0.5;
            
            Location pLoc = center.clone().add(x, y, z);
            Adapter.spawnParticle(pLoc, "DRAGON_BREATH", 1, 0, 0, 0, 0.02);
            Adapter.spawnParticle(pLoc, "END_ROD", 1, 0, 0, 0, 0.01);
        }
    }

    private void renderHalo(Location center) {
        double radius = 0.6;
        for (int i = 0; i < 5; i++) {
            double angle = (ticks * 0.15) + (i * (Math.PI * 2 / 5));
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 1.2 + Math.sin(ticks * 0.1) * 0.1;
            
            Location pLoc = center.clone().add(x, y, z);
            Adapter.spawnParticle(pLoc, "FIREWORKS_SPARK", 1, 0, 0, 0, 0.01);
            if (ticks % 2 == 0) {
                Adapter.spawnParticle(pLoc, "GLOW", 1, 0, 0, 0, 0);
            }
        }
    }

    private void renderStarDust(Location center) {
        if (ticks % 2 == 0) {
            for (int i = 0; i < 3; i++) {
                double x = (Math.random() - 0.5) * 1.5;
                double y = Math.random() * 1.5;
                double z = (Math.random() - 0.5) * 1.5;
                Location pLoc = center.clone().add(x, y, z);
                Adapter.spawnParticle(pLoc, "GLOW", 1, 0.1, 0.1, 0.1, 0.02);
                Adapter.spawnParticle(pLoc, "INSTANT_EFFECT", 1, 0, 0, 0, 0);
            }
        }
    }

    private void renderFlameRing(Location center) {
        double radius = 0.7 + Math.sin(ticks * 0.1) * 0.1;
        double angle = ticks * 0.1;
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        
        Adapter.spawnParticle(center.clone().add(x, 0.2, z), "FLAME", 2, 0.05, 0.05, 0.05, 0.02);
        Adapter.spawnParticle(center.clone().add(-x, 0.2, -z), "FLAME", 2, 0.05, 0.05, 0.05, 0.02);
        Adapter.spawnParticle(center.clone().add(0, 0.1, 0), "LAVA", 1, 0.3, 0.1, 0.3, 0);
    }

    private void renderMagicAura(Location center) {
        double radius = 0.5;
        double angle = ticks * 0.2;
        for (double y = 0; y < 1.5; y += 0.3) {
            double currentAngle = angle + (y * 2);
            double x = Math.cos(currentAngle) * radius;
            double z = Math.sin(currentAngle) * radius;
            Location pLoc = center.clone().add(x, y, z);
            Adapter.spawnParticle(pLoc, "WITCH", 1, 0, 0, 0, 0);
            if (ticks % 4 == 0) {
                Adapter.spawnParticle(pLoc, "SOUL_FIRE_FLAME", 1, 0, 0, 0, 0.01);
            }
        }
    }
}
