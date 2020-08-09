package com.kingrunes.somnia.client;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.SomniaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientTickHandler
{
	private static final String FATIGUE_FORMAT = GuiSomnia.WHITE + "Fatigue: %.2f";
	
	private boolean moddedFOV = false;
	private float fov = -1;
	
	private boolean muted = false;
	private float defVol;
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == Phase.END)
			tickEnd();
	}
	
	public void tickEnd()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player == null)
			return;
		
		GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		
		/*
		 * Fixes some rendering issues with high FOVs when the GUIs are open during sleep
		 */
		
		if (mc.currentScreen instanceof GuiSleepMP)
		{
			if (SomniaConfig.OPTIONS.vanillaBugFixes)
			{
				if (!moddedFOV)
				{
					moddedFOV = true;
					if (gameSettings.fovSetting >= 0.75352114)
					{
						fov = gameSettings.fovSetting;
						gameSettings.fovSetting = 0.7253521f;
					}
				}
			}
		}
		else if (moddedFOV)
		{
			moddedFOV = false;
			if (fov > .0f)
				Minecraft.getMinecraft().gameSettings.fovSetting = fov;
		}
		
		/*
		 * If the player is sleeping and the player has chosen the 'muteSoundWhenSleeping' option in the config,
		 * set the master volume to 0
		 */
		
		if (mc.player.isPlayerSleeping())
		{
			if (SomniaConfig.OPTIONS.muteSoundWhenSleeping)
			{
				if (!muted)
				{
					muted = true;
					defVol = gameSettings.getSoundLevel(SoundCategory.MASTER);
					gameSettings.setSoundLevel(SoundCategory.MASTER, .0f);
				}
			}
			if (mc.player.isPlayerSleeping() && !net.minecraftforge.event.ForgeEventFactory.fireSleepingLocationCheck(mc.player, mc.player.bedLocation)) Somnia.eventChannel.sendToServer(PacketHandler.buildGUIClosePacket());
		}
		else
		{
			if (muted)
			{
				muted = false;
				gameSettings.setSoundLevel(SoundCategory.MASTER, defVol);
			}
		}
		
		/*
		 * Note the isPlayerSleeping() check. Without this, the mod exploits a bug which exists in vanilla Minecraft which
		 * allows the player to teleport back to there bed from anywhere in the world at any time.
		 */
		if (Somnia.clientAutoWakeTime > -1 && mc.player.isPlayerSleeping() && mc.world.getTotalWorldTime() >= Somnia.clientAutoWakeTime)
		{
			Somnia.clientAutoWakeTime = -1;
			Somnia.eventChannel.sendToServer(PacketHandler.buildGUIClosePacket());
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if (event.phase != Phase.END || ClientProxy.playerFatigue == -1 || (mc.currentScreen != null && !(mc.currentScreen instanceof GuiIngameMenu) && !(mc.currentScreen instanceof GuiSomnia)))
			return;
		
		FontRenderer fontRenderer = mc.fontRenderer;
		ScaledResolution scaledResolution = new ScaledResolution(mc);
		String str = String.format(FATIGUE_FORMAT, ClientProxy.playerFatigue);
		int x, y, stringWidth = fontRenderer.getStringWidth(str);
		String param = SomniaConfig.FATIGUE.displayFatigue.toLowerCase();
		switch (param) {
			case "tc":
				x = (scaledResolution.getScaledWidth() / 2 ) - (stringWidth / 2);
				y = fontRenderer.FONT_HEIGHT;
				break;
			case "tl":
				x = 10;
				y = fontRenderer.FONT_HEIGHT;
				break;
			case "tr":
				x = scaledResolution.getScaledWidth() - stringWidth - 10;
				y = fontRenderer.FONT_HEIGHT;
				break;
			case "bc":
				x = (scaledResolution.getScaledWidth() / 2 ) - (stringWidth / 2);
				y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 45;
				break;
			case "bl":
				x = 10;
				y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 10;
				break;
			case "br":
				x = scaledResolution.getScaledWidth() - stringWidth - 10;
				y = scaledResolution.getScaledHeight() - fontRenderer.FONT_HEIGHT - 10;
				break;
			default:
				return;
		}
		
		fontRenderer.drawString(str, x, y, Integer.MIN_VALUE);
	}
}