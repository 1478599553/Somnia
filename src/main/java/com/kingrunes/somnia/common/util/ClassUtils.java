package com.kingrunes.somnia.common.util;

import com.kingrunes.somnia.Somnia;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.kingrunes.somnia.common.util.ObfuscationMappings.*;

public class ClassUtils
{
	private static Boolean mcp = null;
	
	public static boolean deobfuscatedEnvironment()
	{
		if (mcp == null)
		{
			mcp = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

			Somnia.logger.debug("Running in a" + (mcp ? " deobfuscated" : "n obfuscated") + " environment!");
		}
		
		return mcp;
	}
	
	public static void setSleepTimer(Object player, int time)
	{
		try
		{
			Field field = EntityPlayer.class.getDeclaredField(deobfuscatedEnvironment() ? DEOBF_ENTITY_PLAYER_SLEEP_TIMER : OBF_ENTITY_PLAYER_SLEEP_TIMER);
			field.setAccessible(true);
			field.set(player, time);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void setSleeping(Object player, boolean state)
	{
		try
		{
			Field field = EntityPlayer.class.getDeclaredField(deobfuscatedEnvironment() ? DEOBF_ENTITY_PLAYER_SLEEPING : OBF_ENTITY_PLAYER_SLEEPING);
			field.setAccessible(true);
			field.set(player, state);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void setSize(Object player, float f1, float f2)
	{
		try
		{
			Method method = Entity.class.getDeclaredMethod(deobfuscatedEnvironment() ? DEOBF_ENTITY_SET_SIZE : OBF_ENTITY_SET_SIZE, float.class, float.class);
			method.setAccessible(true);
			method.invoke(player, f1, f2);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	public static void call_func_71013_b(Object player, int i1)
	{
		try
		{
			Method method = EntityPlayer.class.getDeclaredMethod("func_71013_b", int.class);
			method.setAccessible(true);
			method.invoke(player, i1);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}