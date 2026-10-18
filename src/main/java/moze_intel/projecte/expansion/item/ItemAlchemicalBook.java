package moze_intel.projecte.expansion.item;

import moze_intel.projecte.expansion.capability.CapabilityAlchemicalBookLocations;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.net.packets.to_client.PacketOpenAlchemicalBookGUI;
import moze_intel.projecte.expansion.registries.ExpansionDataComponentTypes;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.expansion.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemAlchemicalBook extends Item {
	private final Tier tier;
	public ItemAlchemicalBook(Properties properties,Tier tier) {
		super(properties.rarity(tier.getRarity()).stacksTo(1).fireResistant());
		this.tier = tier;
	}

	//26.3 appendHoverText takes a TooltipDisplay and a Consumer<Component> instead of a List<Component>
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
		tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP.translateColored(ChatFormatting.GRAY));
		switch(tier) {
			case BASIC -> tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_BASIC.translateColored(ChatFormatting.RED, CapabilityAlchemicalBookLocations.BASIC_DISTANCE_RATIO));
			case ADVANCED -> {
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_ADVANCED.translateColored(ChatFormatting.RED, CapabilityAlchemicalBookLocations.ADVANCED_DISTANCE_RATIO));
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_BIND.translateColored(ChatFormatting.GREEN));
			}
			case MASTER -> {
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_MASTER.translateColored(ChatFormatting.RED, CapabilityAlchemicalBookLocations.MASTER_DISTANCE_RATIO));
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_BIND.translateColored(ChatFormatting.GREEN));
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_ACROSS_DIMENSIONS.translateColored(ChatFormatting.GREEN));
			}
			case ARCANE -> {
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_ARCANE.translateColored(ChatFormatting.RED, CapabilityAlchemicalBookLocations.ARCANE_DISTANCE_RATIO));
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_BIND.translateColored(ChatFormatting.GREEN));
				tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_TOOLTIP_ACROSS_DIMENSIONS.translateColored(ChatFormatting.GREEN));
			}
		}
		if(getMode(stack) == Mode.PLAYER) {
			Player player = getPlayer(stack);
			tooltip.accept(Lang.Items.ALCHEMICAL_BOOK_BOUND_TO.translateColored(ChatFormatting.RED, player == null ? Component.literal(Util.getOwner(stack).name()).withStyle(ChatFormatting.DARK_AQUA) : player.getDisplayName().copy().withStyle(ChatFormatting.DARK_AQUA)));
		}
	}

	public @Nullable ServerPlayer getPlayer(ItemStack stack) {
		if (stack.getItem() instanceof ItemAlchemicalBook book && book.getMode(stack) == Mode.STACK) return null;
		ExpansionDataComponentTypes.OwnerData owner = Util.getOwner(stack);
		if (owner.isNone()) return null;
		return Util.getPlayer(owner.uuid());
	}

	public enum Mode {
		PLAYER,
		STACK;

		public static final StreamCodec<FriendlyByteBuf, Mode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Mode.class);
	}
	public Mode getMode(ItemStack stack) {
		return stack.has(ExpansionDataComponentTypes.OWNER) ? Mode.PLAYER : Mode.STACK;
	}

	public enum Tier {
		BASIC,
		ADVANCED,
		MASTER,
		ARCANE;

		@SuppressWarnings("unused")
		public boolean isAcrossDimensions() {
			return this == MASTER || this == ARCANE;
		}

		public boolean canBindToPlayer() {
			return this == ADVANCED || this == MASTER || this == ARCANE;
		}

		@SuppressWarnings("unused")
		public float distanceRatio() {
			switch(this) {
				case BASIC -> {
					return CapabilityAlchemicalBookLocations.BASIC_DISTANCE_RATIO;
				}
				case ADVANCED -> {
					return CapabilityAlchemicalBookLocations.ADVANCED_DISTANCE_RATIO;
				}
				case MASTER -> {
					return CapabilityAlchemicalBookLocations.MASTER_DISTANCE_RATIO;
				}
				case ARCANE -> {
					return CapabilityAlchemicalBookLocations.ARCANE_DISTANCE_RATIO;
				}
				default -> throw new IllegalStateException("Unexpected value: " + this);
			}
		}

		public Rarity getRarity() {
			switch(this) {
				case BASIC -> {
					return Rarity.COMMON;
				}
				case ADVANCED -> {
					return Rarity.UNCOMMON;
				}
				case MASTER -> {
					return Rarity.RARE;
				}
				case ARCANE -> {
					return Rarity.EPIC;
				}
				default -> throw new IllegalStateException("Unexpected value: " + this);
			}
		}
	}

	public Tier getTier() {
		return tier;
	}

	//26.3 Item#use returns an InteractionResult instead of an InteractionResultHolder<ItemStack>
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if(!level.isClientSide()) {
			if(player.isCrouching() && getTier().canBindToPlayer()) {
				ExpansionDataComponentTypes.OwnerData data = stack.get(ExpansionDataComponentTypes.OWNER);
				if (getMode(stack) == Mode.PLAYER && data != null) {
					if(!data.uuid().equals(player.getUUID())) {
						player.sendSystemMessage(Lang.NOT_OWNER.translateColored(ChatFormatting.RED, Component.literal(data.name()).withStyle(ChatFormatting.DARK_AQUA)));
						return InteractionResult.FAIL;
					} else {
						stack.remove(ExpansionDataComponentTypes.OWNER);
						player.sendSystemMessage(Lang.Items.ALCHEMICAL_BOOK_NO_LONGER_BOUND.translateColored(ChatFormatting.GREEN, data.name()));
					}
				} else {
					data = new ExpansionDataComponentTypes.OwnerData(player.getUUID(), player.getName().getString());
					stack.set(ExpansionDataComponentTypes.OWNER, data);
					player.sendSystemMessage(Lang.Items.ALCHEMICAL_BOOK_NOW_BOUND.translateColored(ChatFormatting.GREEN, data.name()));
				}
			} else {
				try {
					PacketDistributor.sendToPlayer((ServerPlayer) player, new PacketOpenAlchemicalBookGUI(hand, CapabilityAlchemicalBookLocations.from(stack).getLocations(), getMode(stack), canEdit(stack, (ServerPlayer) player)));
				} catch (CapabilityAlchemicalBookLocations.BookError.OwnerOfflineError ignore) {
					player.sendSystemMessage(Lang.Items.ALCHEMICAL_BOOK_OWNER_NOT_ONLINE.translateColored(ChatFormatting.RED));
					return InteractionResult.FAIL;
				}
			}
		}

		return InteractionResult.SUCCESS;
	}

	public static boolean canEdit(ItemStack stack, ServerPlayer player) {
		if(!(stack.getItem() instanceof ItemAlchemicalBook book)) return false;
		@Nullable ServerPlayer owner = book.getPlayer(stack);
		if(owner == null) return true;
		return canEdit(player, owner);
	}

	public static boolean canEdit(ServerPlayer player, ServerPlayer owner) {
		Config.AlchemicalBookEditLevel editLevel = Config.server.editOthersAlchemicalBooks.get();

		if(editLevel == Config.AlchemicalBookEditLevel.ENABLED) return true;
		//26.3 permission levels are PermissionCheck objects tested against the player's PermissionSet
		if(Commands.LEVEL_GAMEMASTERS.check(player.permissions()) && editLevel == Config.AlchemicalBookEditLevel.OP_ONLY) return true;
		return owner.equals(player);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(ExpansionDataComponentTypes.OWNER);
	}
}
