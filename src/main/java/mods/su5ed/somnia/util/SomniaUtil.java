package mods.su5ed.somnia.util;

import mods.su5ed.somnia.api.capability.FatigueCapability;
import mods.su5ed.somnia.config.SomniaConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SomniaUtil {
    public static boolean doesPlayerWearArmor(PlayerEntity player) {
        for (ItemStack stack : player.inventory.armorInventory) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    public static long calculateWakeTime(long totalWorldTime, int target) {
        long wakeTime;
        long timeInDay = totalWorldTime % 24000L;
        wakeTime = totalWorldTime - timeInDay + target;
        if (timeInDay > target) wakeTime += 24000L;
        return wakeTime;
    }

    public static boolean checkFatigue(PlayerEntity player) {
        return player.getCapability(FatigueCapability.FATIGUE_CAPABILITY, null)
                .map(props -> player.isCreative() || props.getFatigue() >= SomniaConfig.minimumFatigueToSleep)
                .orElse(false);
    }

    public static String timeStringForWorldTime(long time) {
        time += 6000; // Tick -> Time offset

        time = time % 24000;
        int hours = (int) Math.floor(time / (double)1000);
        int minutes = (int) ((time % 1000) / 1000.0d * 60);

        String lsHours = String.valueOf(hours);
        String lsMinutes = String.valueOf(minutes);

        if (lsHours.length() == 1) lsHours = "0"+lsHours;
        if (lsMinutes.length() == 1) lsMinutes = "0"+lsMinutes;

        return lsHours + ":" + lsMinutes;
    }

    public static double calculateFatigueToReplenish(PlayerEntity player) {
        long worldTime = player.world.getGameTime();
        long wakeTime = SomniaUtil.calculateWakeTime(worldTime, 0);
        return SomniaConfig.fatigueReplenishRate * (wakeTime - worldTime);
    }
}