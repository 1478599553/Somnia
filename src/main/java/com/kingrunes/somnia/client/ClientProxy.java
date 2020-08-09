package com.kingrunes.somnia.client;

import com.kingrunes.somnia.client.gui.GuiSelectWakeTime;
import com.kingrunes.somnia.client.gui.GuiSomnia;
import com.kingrunes.somnia.common.CommonProxy;
import com.kingrunes.somnia.common.SomniaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static double playerFatigue = -1;
	
	@Override
	public void register()
	{
		super.register();
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
	}
	
	@Override
	public void handleGUIOpenPacket(DataInputStream in) throws IOException {
		byte data = in.readByte();
		final Minecraft mc = Minecraft.getMinecraft();
		GuiScreen gui = data == 0x00 && SomniaConfig.OPTIONS.somniaGui ? new GuiSomnia() : data == 0x01 ? new GuiSelectWakeTime() : null;
		if (gui != null) mc.addScheduledTask(() -> mc.displayGuiScreen(gui));
	}

	@Override
	public void handlePropUpdatePacket(DataInputStream in) throws IOException
	{
		byte target = in.readByte();
		
		switch (target)
		{
		case 0x00:
			GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
			if (currentScreen instanceof GuiSomnia)
			{
				GuiSomnia gui = (GuiSomnia)currentScreen;
				
				int b = in.readInt();
				for (int a=0; a<b; a++)
					gui.readField(in);
			}
			break;
		case 0x01:
			int b = in.readInt();
			for (int a=0; a<b; a++)
			{
				if (in.readByte() == 0x00) {
					playerFatigue = in.readDouble();
				}
			}
			break;
		}
	}

	@Override
	public void handleGUIClosePacket(EntityPlayerMP player)
	{
		if (player.world.isRemote) Minecraft.getMinecraft().displayGuiScreen(null);
		player.wakeUpPlayer(true, true, true);
	}
}