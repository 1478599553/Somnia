package com.kingrunes.somnia.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerSleepTickHandler
{
	/*
	 * A sided state for caching player data 
	 */
	public static class State
	{
		public boolean sleepOverride = false;
	}
	
	public static State clientState = new State(), serverState = new State();
	
	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		State state = event.side == Side.CLIENT ? clientState : serverState;
		if (event.phase == Phase.START)
			tickStart(state, event.player);
		else
			tickEnd(state, event.player);
	}
	
	public void tickStart(State state, EntityPlayer player)
	{
		if (player.isPlayerSleeping())
		{
			BlockPos pos = player.bedLocation;
			if (!CompatModule.shouldSimulate(player, pos)) {
				state.sleepOverride = false;
				return;
			}

			state.sleepOverride = true;
			player.sleeping = false;
			
			if (player.world.isRemote && SomniaConfig.OPTIONS.fading)
			{
				int sleepTimer = player.getSleepTimer()+1;
				if (sleepTimer >= 99)
					sleepTimer = 98;
				player.sleepTimer = sleepTimer;
			}
		}
	}

	public void tickEnd(State state, EntityPlayer player)
	{
		if (state.sleepOverride)
		{
			player.sleeping = true;
			state.sleepOverride = false;
		}
	}
}