package com.kingrunes.somnia.client.gui;

import com.kingrunes.somnia.Somnia;
import com.kingrunes.somnia.api.capability.CapabilityFatigue;
import com.kingrunes.somnia.api.capability.IFatigue;
import com.kingrunes.somnia.common.PacketHandler;
import com.kingrunes.somnia.common.compat.RailcraftPlugin;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class GuiSelectWakeTime extends GuiScreen
{
	private boolean resetSpawn = true;

	@Override
	public void initGui()
	{
		int i = 0;
		int buttonWidth = 90, buttonHeight = 20;
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/2)-buttonWidth/2,
				(height/4)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Noon"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*5/8)-buttonWidth/2,
				(height*3/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Mid Afternoon"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/4)-buttonWidth/2,
				(height/2)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Before Sunset"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*5/8)-buttonWidth/2,
				(height*5/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"After Sunset"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/2)-buttonWidth/2,
				(height*3/4)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Midnight"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/8)-buttonWidth/2,
				(height*5/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Before Sunrise"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width/4)-buttonWidth/2,
				(height/2)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"After Sunrise"
			)
		);
		
		buttonList.add
		(
			new GuiButton
			(
				i++,
				(width*3/8)-buttonWidth/2,
				(height*3/8)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Mid Morning"
			)
		);

		buttonList.add(
			new GuiButton(
				i++,
				(width/2)-buttonWidth/2,
				(height/7)-buttonHeight/2,
				buttonWidth,
				buttonHeight,
				"Reset spawn: "+(resetSpawn ? "Yes" : "No")
			)
		);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		int i = 0;
		switch (par1GuiButton.id)
		{
		case 0:
			i = 6000;
			break;
		case 1:
			i = 9000;
			break;
		case 2:
			i = 12000;
			break;
		case 3:
			i = 14000;
			break;
		case 4:
			i = 18000;
			break;
		case 5:
			i = 22000;
			break;
		case 6:
			i = 0;
			break;
		case 7:
			i = 3000;
			break;
		case 8:
			this.resetSpawn = !this.resetSpawn;
			par1GuiButton.displayString = "Reset spawn: "+(resetSpawn ? "Yes" : "No");
			return;
		default:
			return;
		}

		IFatigue props = mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY, null);
		if (props != null) {
			props.shouldResetSpawn(this.resetSpawn);
			Somnia.eventChannel.sendToServer(PacketHandler.buildPropUpdatePacket(0x01, 0x01, props.resetSpawn()));
		}
		Somnia.clientAutoWakeTime = Somnia.calculateWakeTime(mc.world.getTotalWorldTime(), i);
		/*
		 * Nice little hack to simulate a right click on the bed, don't try this at home kids
		 */
		RayTraceResult mouseOver = mc.objectMouseOver;
		BlockPos pos = mouseOver.getBlockPos();

		if (pos != null) { //pos is nullable!!!
			Somnia.eventChannel.sendToServer(PacketHandler.buildRightClickBlockPacket(mouseOver.getBlockPos(), mouseOver.sideHit, (float) mouseOver.hitVec.x, (float) mouseOver.hitVec.y, (float) mouseOver.hitVec.z));
		}
		else if (mouseOver.entityHit != null && mouseOver.entityHit.getClass() == RailcraftPlugin.BED_CART_CLASS) {
			Somnia.eventChannel.sendToServer(PacketHandler.buildRideEntityPacket(mouseOver.entityHit));
			RailcraftPlugin.sleepInBedCart();
		}

		mc.displayGuiScreen(null);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}