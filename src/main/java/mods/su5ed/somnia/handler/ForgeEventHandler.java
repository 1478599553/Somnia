package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.SomniaAPI;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.compat.Compat;
import mods.su5ed.somnia.compat.DarkUtilsPlugin;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.network.packet.PacketOpenGUI;
import mods.su5ed.somnia.network.packet.PacketUpdateFatigue;
import mods.su5ed.somnia.network.packet.PacketWakeUpPlayer;
import mods.su5ed.somnia.util.ASMHooks;
import mods.su5ed.somnia.util.SomniaUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;

@Mod.EventBusSubscriber
public class ForgeEventHandler {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START || event.player.world.isRemote || (!event.player.isAlive() || event.player.isCreative() || event.player.isSpectator() && !event.player.isSleeping())) return;

		event.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
			double fatigue = props.getFatigue();

			boolean isSleeping = props.sleepOverride() || event.player.isSleeping();

			if (isSleeping) fatigue -= SomniaConfig.fatigueReplenishRate;
			else fatigue += SomniaConfig.fatigueRate;

			if (fatigue > 100) fatigue = 100;
			else if (fatigue < 0) fatigue = 0;

			props.setFatigue(fatigue);
			if (props.updateFatigueCounter() >= 100) {
				props.resetFatigueCounter();
				NetworkHandler.sendToClient(new PacketUpdateFatigue(fatigue), (ServerPlayerEntity) event.player);

				if (SomniaConfig.fatigueSideEffects) {
					int lastSideEffectStage = props.getSideEffectStage();
					int currentStage = getSideEffectStage(fatigue);

					if (currentStage > lastSideEffectStage || currentStage >= SomniaConfig.sideEffectStage4) {
						EffectInstance effect = getEffectForStage(currentStage, lastSideEffectStage);
						if (effect != null) event.player.addPotionEffect(effect);
					} else if (currentStage < lastSideEffectStage) {
						EffectInstance effect = getEffectForStage(lastSideEffectStage, 0);
						if (effect != null) {
							EffectInstance active = event.player.getActivePotionEffect(effect.getPotion());
							if (active != null && active.getAmplifier() == effect.getAmplifier()) event.player.removePotionEffect(effect.getPotion());
						}
					} else return;

					props.setSideEffectStage(currentStage);
				}
			}
		});
	}

	@Nullable
	public static EffectInstance getEffectForStage(int stage, int previousStage) {
		int potionID = 0;
		int duration = 0;
		int amplifier = 0;

		if (stage == SomniaConfig.sideEffectStage1 && previousStage < SomniaConfig.sideEffectStage1) {
			potionID = SomniaConfig.sideEffectStage1Potion;
			duration = SomniaConfig.sideEffectStage1Duration;
			amplifier = SomniaConfig.sideEffectStage1Amplifier;
		}
		else if (stage == SomniaConfig.sideEffectStage2 && previousStage < SomniaConfig.sideEffectStage2) {
			potionID = SomniaConfig.sideEffectStage2Potion;
			duration = SomniaConfig.sideEffectStage2Duration;
			amplifier = SomniaConfig.sideEffectStage2Amplifier;
		}
		else if (stage == SomniaConfig.sideEffectStage3 && previousStage < SomniaConfig.sideEffectStage3) {
			potionID = SomniaConfig.sideEffectStage3Potion;
			duration = SomniaConfig.sideEffectStage3Duration;
			amplifier = SomniaConfig.sideEffectStage3Amplifier;
		}
		else if (stage >= SomniaConfig.sideEffectStage4) {
			potionID = SomniaConfig.sideEffectStage4Potion;
			duration = 150;
			amplifier = SomniaConfig.sideEffectStage4Amplifier;
		}

		Effect effect = Effect.get(potionID);
		if (effect == null) return null;

		return new EffectInstance(effect, duration, amplifier);
	}

	public static int getSideEffectStage(double fatigue) {
		if (SomniaConfig.sideEffectStage4 < fatigue) return SomniaConfig.sideEffectStage4;
		else if (SomniaConfig.sideEffectStage3 < fatigue) return SomniaConfig.sideEffectStage3;
		else if (SomniaConfig.sideEffectStage2 < fatigue) return SomniaConfig.sideEffectStage2;
		else if (SomniaConfig.sideEffectStage1 < fatigue) return SomniaConfig.sideEffectStage1;

		return -1;
	}

	@SubscribeEvent
	public static void onTickEnd(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) ServerTickHandler.HANDLERS.forEach(ServerTickHandler::tickEnd);
	}

	@SubscribeEvent
	public static void onWakeUp(PlayerWakeUpEvent event) {
		PlayerEntity player = event.getPlayer();
		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> {
			if (props.shouldSleepNormally() || (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player))) {
				props.setFatigue(props.getFatigue() - SomniaUtil.getFatigueToReplenish(player));
			}
			props.maxFatigueCounter();
			props.shouldResetSpawn(true);
			props.setSleepNormally(false);
			props.setWakeTime(-1);
		});
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onSleepingTimeCheck(SleepingTimeCheckEvent event) {
		PlayerEntity player = event.getPlayer();
		if (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) return;

		Optional<IFatigue> props = player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).resolve();
		if (props.isPresent()) {
			if (props.get().shouldSleepNormally()) {
				return;
			}
		}
		if (!SomniaUtil.isEnterSleepTime()) event.setResult(Event.Result.DENY);
		else event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		if (!SomniaUtil.checkFatigue(player)) {
			player.sendStatusMessage(new TranslationTextComponent("somnia.status.cooldown"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}
		else if (!SomniaConfig.sleepWithArmor && !player.isCreative() && SomniaUtil.doesPlayerWearArmor(player)) {
			player.sendStatusMessage(new TranslationTextComponent("somnia.status.armor"), true);
			event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
		}

		player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> props.setSleepNormally(player.isSneaking()));

		if (Compat.isSleepingInBag(player)) ASMHooks.updateWakeTime(player);
	}

	@SubscribeEvent
	public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
		event.getPlayer().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.map(IFatigue::resetSpawn)
				.ifPresent(resetSpawn -> {
			if (!resetSpawn) event.setCanceled(true);
		});
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if (!world.isRemote) {
			BlockPos pos = event.getPos();
			BlockState state = world.getBlockState(pos);
			if (!state.hasProperty(HorizontalBlock.HORIZONTAL_FACING)) return;
			Direction direction = state.get(HorizontalBlock.HORIZONTAL_FACING);
			PlayerEntity player = event.getPlayer();

			if (!Compat.isBed(state, pos, world, player) || !((ServerPlayerEntity) player).func_241147_a_(pos, direction)) return;

			ItemStack stack = player.inventory.getCurrentItem();
			if (!stack.isEmpty() && stack.getItem().getRegistryName().toString().equals(SomniaConfig.wakeTimeSelectItem)) {
				NetworkHandler.sendToClient(new PacketOpenGUI(), (ServerPlayerEntity) player);
				event.setCancellationResult(ActionResultType.SUCCESS);
				event.setCanceled(true);
			}
		}

	}

	@SubscribeEvent
	public static void onLivingEntityUseItem(LivingEntityUseItemEvent.Finish event) {
		ItemStack stack = event.getItem();
		if (stack.getUseAction() == UseAction.DRINK) {
			for (Pair<ItemStack, Double> pair : SomniaAPI.getCoffeeList()) {
				if (pair.getLeft().isItemEqual(stack)) {
					event.getEntityLiving().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
						.ifPresent(props -> {
							props.setFatigue(props.getFatigue() - pair.getRight());
							props.maxFatigueCounter();
						});
				}
			}
		}
	}

	@SubscribeEvent
	public static void worldLoadHook(WorldEvent.Load event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			ServerTickHandler.HANDLERS.add(new ServerTickHandler(worldServer));
			Somnia.LOGGER.info("Registering tick handler for loading world!");
		}
	}

	@SubscribeEvent
	public static void worldUnloadHook(WorldEvent.Unload event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld worldServer = (ServerWorld) event.getWorld();
			Iterator<ServerTickHandler> iter = ServerTickHandler.HANDLERS.iterator();
			ServerTickHandler serverTickHandler;
			while (iter.hasNext()) {
				serverTickHandler = iter.next();
				if (serverTickHandler.worldServer == worldServer) {
					Somnia.LOGGER.info("Removing tick handler for unloading world!");
					iter.remove();
					break;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingHurtEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof ServerPlayerEntity && entity.isSleeping()) NetworkHandler.sendToClient(new PacketWakeUpPlayer(), (ServerPlayerEntity) entity);
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		event.getEntityLiving().getCapability(CapabilityFatigue.FATIGUE_CAPABILITY).ifPresent(props -> props.setFatigue(0));
	}
}
