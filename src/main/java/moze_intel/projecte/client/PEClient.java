package moze_intel.projecte.client;

import com.mojang.serialization.MapCodec;
import mezz.jei.api.runtime.IRecipesGui;
import moze_intel.projecte.PECore;
import moze_intel.projecte.client.rendering.PETridentRenderer;
import moze_intel.projecte.client.rendering.item.ShieldISTER;
import moze_intel.projecte.client.rendering.item.TridentISTER;
import moze_intel.projecte.gameObjs.blacklist.BlacklistManager;
import moze_intel.projecte.gameObjs.blacklist.BlacklistType;
import moze_intel.projecte.gameObjs.blacklist.GameStagesHelper;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.DMFurnaceContainer;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.gui.AbstractCollectorScreen;
import moze_intel.projecte.gameObjs.gui.AbstractCondenserScreen;
import moze_intel.projecte.gameObjs.gui.AlchBagScreen;
import moze_intel.projecte.gameObjs.gui.AlchChestScreen;
import moze_intel.projecte.gameObjs.gui.AlchemicalBarrelScreen;
import moze_intel.projecte.gameObjs.gui.GUIDMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIEternalDensity;
import moze_intel.projecte.gameObjs.gui.GUIMercurialEye;
import moze_intel.projecte.gameObjs.gui.GUIRMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK1;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK2;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK3;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import moze_intel.projecte.gameObjs.gui.PEContainerScreen;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.sound.MovingSoundSWRG;
import moze_intel.projecte.network.commands.client.DumpMissingEmc;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.EntitySpriteRenderer;
import moze_intel.projecte.rendering.LayerYue;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.rendering.AlchemicalCrucibleRenderer;
import moze_intel.projecte.rendering.TransmutationRenderingOverlay;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.PEKeybind;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

@Mod(value = PECore.MODID, dist = Dist.CLIENT)
public class PEClient {

	public static final Identifier ACTIVE_OVERRIDE = PECore.rl("active");
	public static final Identifier MODE_OVERRIDE = PECore.rl("mode");
	public static final Identifier BLOCKING_OVERRIDE = PECore.rl("blocking");
	public static final Identifier THROWING_OVERRIDE = PECore.rl("throwing");

	public PEClient(ModContainer container, IEventBus modEventBus) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		modEventBus.addListener(this::registerScreens);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::registerKeybindings);
		modEventBus.addListener(this::registerOverlays);
		modEventBus.addListener(this::registerRenderers);
		modEventBus.addListener(this::addLayers);
		modEventBus.addListener(this::registerSpecialModelRenderers);
		modEventBus.addListener(this::registerRangeSelectItemModelProperties);

		//Client side of the former ProjectExpansion addon: screens, chest renderer, and the client hooks
		moze_intel.projecte.expansion.client.ExpansionClient.register(modEventBus);
		moze_intel.projecte.expansion.client.ExpansionClientHooks.install();

		NeoForge.EVENT_BUS.addListener(this::onEntityJoinWorld);
		NeoForge.EVENT_BUS.addListener(this::registerClientCommands);
		//Before recipe viewers handle the same event, so JEI finds the rituals when it reloads
		NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, moze_intel.projecte.expansion.codex.CodexRecipes::onRecipesReceived);
		NeoForge.EVENT_BUS.addListener(moze_intel.projecte.expansion.codex.CodexRecipes::onLoggingOut);
		NeoForge.EVENT_BUS.addListener(this::onDisconnect);
		NeoForge.EVENT_BUS.addListener(this::tooltipEvent);
	}

	private void onEntityJoinWorld(EntityJoinLevelEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getEntity() instanceof EntitySWRGProjectile projectile && mc.mouseHandler.isMouseGrabbed()) {
			mc.getSoundManager().play(new MovingSoundSWRG(projectile, event.getLevel().getRandom()));
		}
	}

	private void registerClientCommands(RegisterClientCommandsEvent event) {
		CommandBuildContext context = event.getBuildContext();
		//Note: We can use projecte as the base command here as it will merge the trees properly
		event.getDispatcher().register(Commands.literal("projecte")
				.then(DumpMissingEmc.register(context))
		);
	}

	private void registerScreens(RegisterMenuScreensEvent event) {
		event.register(PEContainerTypes.RM_FURNACE_CONTAINER.get(), GUIRMFurnace::new);
		//noinspection RedundantTypeArguments (necessary for it to actually compile)
		event.<DMFurnaceContainer, GUIDMFurnace<DMFurnaceContainer>>register(PEContainerTypes.DM_FURNACE_CONTAINER.get(), GUIDMFurnace::new);
		event.register(PEContainerTypes.CONDENSER_CONTAINER.get(), AbstractCondenserScreen.MK1::new);
		event.register(PEContainerTypes.CONDENSER_MK2_CONTAINER.get(), AbstractCondenserScreen.MK2::new);
		event.register(PEContainerTypes.ALCH_CHEST_CONTAINER.get(), AlchChestScreen::new);
		event.register(PEContainerTypes.ALCH_BAG_CONTAINER.get(), AlchBagScreen::new);
		event.register(PEContainerTypes.ETERNAL_DENSITY_CONTAINER.get(), GUIEternalDensity::new);
		event.register(PEContainerTypes.TRANSMUTATION_CONTAINER.get(), GUITransmutation::new);
		event.register(PEContainerTypes.RELAY_MK1_CONTAINER.get(), GUIRelayMK1::new);
		event.register(PEContainerTypes.RELAY_MK2_CONTAINER.get(), GUIRelayMK2::new);
		event.register(PEContainerTypes.RELAY_MK3_CONTAINER.get(), GUIRelayMK3::new);
		event.register(PEContainerTypes.COLLECTOR_MK1_CONTAINER.get(), AbstractCollectorScreen.MK1::new);
		event.register(PEContainerTypes.COLLECTOR_MK2_CONTAINER.get(), AbstractCollectorScreen.MK2::new);
		event.register(PEContainerTypes.COLLECTOR_MK3_CONTAINER.get(), AbstractCollectorScreen.MK3::new);
		event.register(PEContainerTypes.MERCURIAL_EYE_CONTAINER.get(), GUIMercurialEye::new);
		event.register(PEContainerTypes.ALCHEMICAL_BARREL_CONTAINER.get(), AlchemicalBarrelScreen::new);
	}

	private void clientSetup(FMLClientSetupEvent evt) {
		if (ModList.get().isLoaded("jei")) {
			//Note: This listener is only registered if JEI is loaded
			NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ScreenEvent.Opening event) -> {
				if (event.getCurrentScreen() instanceof PEContainerScreen<?> screen) {
					//If JEI is loaded and our current screen is a mekanism gui,
					// check if the new screen is a JEI recipe screen
					if (event.getNewScreen() instanceof IRecipesGui) {
						//If it is mark on our current screen that we are switching to JEI
						screen.switchingToJEI = true;
					}
				}
			});
		}
	}

	private void registerKeybindings(RegisterKeyMappingsEvent event) {
		ClientKeyHelper.registerKeyBindings(event);
	}

	private void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.CROSSHAIR, PECore.rl("transmutation_result"), new TransmutationRenderingOverlay());
	}

	private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		//Block Entity
		event.registerBlockEntityRenderer(PEBlockEntityTypes.ALCHEMICAL_CHEST.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/alchemical_chest.png"), PEBlocks.ALCHEMICAL_CHEST));
		event.registerBlockEntityRenderer(PEBlockEntityTypes.CONDENSER.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/condenser_mk1.png"), PEBlocks.CONDENSER));
		event.registerBlockEntityRenderer(PEBlockEntityTypes.CONDENSER_MK2.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/condenser_mk2.png"),PEBlocks.CONDENSER_MK2));
		event.registerBlockEntityRenderer(PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);
		event.registerBlockEntityRenderer(PEBlockEntityTypes.ALCHEMICAL_CRUCIBLE.get(), AlchemicalCrucibleRenderer::new);
		event.registerBlockEntityRenderer(PEBlockEntityTypes.ALCHEMICAL_CAULDRON.get(), moze_intel.projecte.rendering.AlchemicalCauldronRenderer::new);

		//Entities
		event.registerEntityRenderer(PEEntityTypes.WATER_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/water_orb.png")));
		event.registerEntityRenderer(PEEntityTypes.LAVA_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lava_orb.png")));
		event.registerEntityRenderer(PEEntityTypes.MOB_RANDOMIZER.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/randomizer.png")));
		event.registerEntityRenderer(PEEntityTypes.LENS_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lens_explosive.png")));
		event.registerEntityRenderer(PEEntityTypes.FIRE_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/fireball.png")));
		event.registerEntityRenderer(PEEntityTypes.SWRG_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lightning.png")));
		event.registerEntityRenderer(PEEntityTypes.NOVA_CATALYST_PRIMED.get(), TntRenderer::new);
		event.registerEntityRenderer(PEEntityTypes.NOVA_CATACLYSM_PRIMED.get(), TntRenderer::new);
		event.registerEntityRenderer(PEEntityTypes.HOMING_ARROW.get(), TippableArrowRenderer::new);
		event.registerEntityRenderer(PEEntityTypes.PE_TRIDENT.get(), PETridentRenderer::new);
	}

	private void addLayers(EntityRenderersEvent.AddLayers event) {
		for (PlayerModelType modelType : event.getSkins()) {
			AvatarRenderer<AbstractClientPlayer> renderer = event.getPlayerRenderer(modelType);
			if (renderer != null) {
				renderer.addLayer(new LayerYue(renderer));
			}
		}
	}

	private void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(PECore.rl("shield"), ShieldISTER.Unbaked.MAP_CODEC);
		event.register(PECore.rl("trident"), TridentISTER.Unbaked.MAP_CODEC);
	}

	private void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
		event.register(ACTIVE_OVERRIDE, ActiveProperty.MAP_CODEC);
		event.register(MODE_OVERRIDE, ModeProperty.MAP_CODEC);
		event.register(BLOCKING_OVERRIDE, UsingItemProperty.MAP_CODEC);
		//Each property needs its own codec instance, registering the same one twice is rejected
		event.register(THROWING_OVERRIDE, MapCodec.unit(new UsingItemProperty()));
	}

	private void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		//Note: The player is null on integrated server startup
		if (event.getPlayer() != null && GameStagesHelper.gameStagesLoaded) {
			BlacklistManager.clearBlacklist();
		}
	}

	private void tooltipEvent(ItemTooltipEvent event) {
		Player player = event.getEntity();
		if (player != null) {
			BlacklistType blacklistType = switch (player.containerMenu) {
				case CondenserContainer condenserContainer -> BlacklistType.CONDENSER;
				case TransmutationContainer transmutationContainer -> BlacklistType.LEARNING;
				default -> null;
			};
			if (blacklistType != null) {
				blacklistType.addBlacklistWarnings(player, event.getItemStack(), event.getToolTip());
			}
		}
	}

	private record ActiveProperty() implements RangeSelectItemModelProperty {
		public static final MapCodec<ActiveProperty> MAP_CODEC = MapCodec.unit(new ActiveProperty());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return stack.getOrDefault(PEDataComponentTypes.ACTIVE, false) ? 1F : 0F;
		}

		@Override
		public MapCodec<ActiveProperty> type() {
			return MAP_CODEC;
		}
	}

	private record ModeProperty() implements RangeSelectItemModelProperty {
		public static final MapCodec<ModeProperty> MAP_CODEC = MapCodec.unit(new ModeProperty());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			if (stack.is(PEItems.SWIFTWOLF_RENDING_GALE)) {
				return stack.getOrDefault(PEDataComponentTypes.SWRG_MODE, PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal();
			}
			return stack.getOrDefault(PEDataComponentTypes.ARCANA_MODE, PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal();
		}

		@Override
		public MapCodec<ModeProperty> type() {
			return MAP_CODEC;
		}
	}

	private record UsingItemProperty() implements RangeSelectItemModelProperty {
		public static final MapCodec<UsingItemProperty> MAP_CODEC = MapCodec.unit(new UsingItemProperty());

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			LivingEntity livingEntity = owner == null ? null : owner.asLivingEntity();
			return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == stack ? 1.0F : 0.0F;
		}

		@Override
		public MapCodec<UsingItemProperty> type() {
			return MAP_CODEC;
		}
	}
}