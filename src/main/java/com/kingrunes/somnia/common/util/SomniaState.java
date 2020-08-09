package com.kingrunes.somnia.common.util;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.server.ServerTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Iterator;
import java.util.List;

public enum SomniaState
{
	IDLE,
	ACTIVE,
	WAITING_PLAYERS,
	EXPIRED,
	NOT_NOW,
	COOLDOWN;
	
	public static SomniaState getState(ServerTickHandler handler)
	{
		long totalWorldTime = handler.worldServer.getTotalWorldTime();
		
		/*if (handler.currentState != ACTIVE && handler.lastSleepStart > 0 && totalWorldTime-handler.lastSleepStart < Somnia.proxy.sleepCooldown)
			return COOLDOWN;*/
		
		if (!CommonProxy.validSleepPeriod.isTimeWithin(totalWorldTime % 24000))
			return NOT_NOW;
		
		if (handler.worldServer.playerEntities.isEmpty())
			return IDLE;
		
		@SuppressWarnings("unchecked")
		List<EntityPlayer> players = handler.worldServer.playerEntities;
		
		boolean sleeping, anySleeping = false, allSleeping = true;
		
		Iterator<EntityPlayer> iter = players.iterator();
		while (iter.hasNext())
		{
			EntityPlayerMP player = (EntityPlayerMP) iter.next();
			sleeping = player.isPlayerSleeping() || ListUtils.containsRef(player, Somnia.instance.ignoreList);
			anySleeping |= sleeping;
			allSleeping &= sleeping;
		}
		
		if (allSleeping)
			return ACTIVE;
		else if (anySleeping)
			return WAITING_PLAYERS;
		else
			return IDLE;
	}
}
