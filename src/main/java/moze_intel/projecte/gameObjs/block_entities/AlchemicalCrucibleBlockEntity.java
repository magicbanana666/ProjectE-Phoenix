package moze_intel.projecte.gameObjs.block_entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.config.ServerConfig;
import moze_intel.projecte.gameObjs.CrucibleStructure;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorInput;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorRecipe;
import moze_intel.projecte.gameObjs.customRecipes.athanor.AthanorStage;
import moze_intel.projecte.gameObjs.customRecipes.athanor.OpusStage;
import moze_intel.projecte.gameObjs.customRecipes.athanor.RuneColor;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The centre of the Athanor. Items on the dais and a catalyst in the centre pass through the stages of an
 * {@link AthanorRecipe} and become something new. The ritual only runs while the whole Athanor, see
 * {@link CrucibleStructure}, is in place.
 * <p>
 * Everything that matters is decided on the server: the recipe is looked up when the ritual starts and checked again right
 * before the result is made, the owner pays the EMC of a stage bit by bit while it progresses, and the state is saved so a
 * restart or an unloaded chunk only pauses the ritual. The ingredients are never handed to item handlers, so automation can
 * not take them out halfway.
 * <p>
 * When the Athanor breaks during a ritual, or the owner runs out of EMC in one of the late stages, the runes flicker as a
 * warning. If nothing is done about it the ritual rebounds, harder the further the work has come.
 */
public class AlchemicalCrucibleBlockEntity extends BlockEntity {

	private static final DustParticleOptions FALSE_GLOW = new DustParticleOptions(0x5A0A12, 0.7F);
	private static final DustParticleOptions SILVER = new DustParticleOptions(0xE8ECF2, 1.0F);
	private static final DustParticleOptions GOLD = new DustParticleOptions(0xFFC83A, 1.0F);
	private static final DustParticleOptions SCARLET = new DustParticleOptions(0xFF1E3C, 1.2F);
	private static final DustParticleOptions WHITE_HOT = new DustParticleOptions(0xFFF0E0, 1.4F);
	private static final DustParticleOptions[] PEACOCK = {
			new DustParticleOptions(RuneColor.MAGENTA.getRgb(), 1.0F), new DustParticleOptions(RuneColor.VIOLET.getRgb(), 1.0F),
			new DustParticleOptions(RuneColor.BLUE.getRgb(), 1.0F), new DustParticleOptions(RuneColor.CYAN.getRgb(), 1.0F),
			new DustParticleOptions(RuneColor.GREEN.getRgb(), 1.0F), new DustParticleOptions(RuneColor.ORANGE.getRgb(), 1.0F)
	};
	/**
	 * How long the runes flicker before a broken Athanor rebounds.
	 */
	private static final int STRUCTURE_WARNING = 5 * 20;
	/**
	 * How long the runes flicker before running out of EMC in a late stage rebounds.
	 */
	private static final int EMC_WARNING = 10 * 20;

	private final ItemStack[] inputs = new ItemStack[AthanorRecipe.MAX_INPUTS];
	private ItemStack catalyst = ItemStack.EMPTY;
	private ItemStack result = ItemStack.EMPTY;
	@Nullable
	private ResourceKey<Recipe<?>> recipeId;
	@Nullable
	private AthanorRecipe recipe;
	private int stageIndex;
	private OpusStage stage = OpusStage.IDLE;
	private RuneColor rune = RuneColor.FORMED;
	private Stall stall = Stall.NONE;
	private int progress;
	/**
	 * Ticks left before the ritual rebounds, 0 while nothing is wrong.
	 */
	private int warning;
	/**
	 * The EMC the owner already paid for the current stage.
	 */
	private long paid;
	@Nullable
	private UUID owner;
	/**
	 * Only used on the client, to keep the animation smooth between the updates of the server.
	 */
	private float clientProgress;
	/**
	 * The length of the current stage as the server sent it, the client does not know the recipes.
	 */
	private int clientDuration;
	/**
	 * Whether every block of the Athanor is in place, checked once a second.
	 */
	private boolean formed;
	/**
	 * {@link CrucibleStructure#ATHANOR} or {@link CrucibleStructure#GRAND_ATHANOR} while formed, 0 otherwise.
	 */
	private int tier;

	public AlchemicalCrucibleBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.ALCHEMICAL_CRUCIBLE.get(), pos, state);
		Arrays.fill(inputs, ItemStack.EMPTY);
	}

	/**
	 * @return how many items the dais can hold: three on the Athanor, five on the Grand Athanor
	 */
	public int getCapacity() {
		return tier >= CrucibleStructure.GRAND_ATHANOR ? AthanorRecipe.MAX_INPUTS : AthanorRecipe.TIER_ONE_INPUTS;
	}

	public int getTier() {
		return tier;
	}

	private int inputCount() {
		int count = 0;
		for (ItemStack input : inputs) {
			if (!input.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private List<ItemStack> inputList() {
		return Arrays.asList(inputs);
	}

	private static List<RecipeHolder<AthanorRecipe>> getRecipes(ServerLevel level) {
		return List.copyOf(level.recipeAccess().recipeMap().byType(PERecipeTypes.ATHANOR.get()));
	}

	/**
	 * @return the ritual that the items in the crucible make, if there is one the Athanor can do
	 */
	private Optional<RecipeHolder<AthanorRecipe>> findRecipe(ServerLevel level) {
		return findRecipe(level, getTier());
	}

	private Optional<RecipeHolder<AthanorRecipe>> findRecipe(ServerLevel level, int maxTier) {
		AthanorInput input = new AthanorInput(inputList(), catalyst);
		for (RecipeHolder<AthanorRecipe> holder : getRecipes(level)) {
			if (holder.value().tier() <= maxTier && holder.value().matches(input, level)) {
				return Optional.of(holder);
			}
		}
		return Optional.empty();
	}

	/**
	 * @return whether the stack should go to the centre: it is only ever a catalyst, or the dais is already full
	 */
	private boolean belongsInCentre(ServerLevel level, ItemStack stack) {
		boolean isCatalyst = false;
		boolean isInput = false;
		for (RecipeHolder<AthanorRecipe> holder : getRecipes(level)) {
			isCatalyst |= holder.value().catalyst().test(stack);
			isInput |= holder.value().isInput(stack);
		}
		if (isCatalyst && !isInput) {
			return true;
		}
		return isCatalyst && inputCount() >= getCapacity();
	}

	private boolean isUsedByAnyRecipe(ServerLevel level, ItemStack stack) {
		for (RecipeHolder<AthanorRecipe> holder : getRecipes(level)) {
			if (holder.value().catalyst().test(stack) || holder.value().isInput(stack)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles a player using the crucible, on the server.
	 */
	public InteractionResult use(Player player, ItemStack held) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (stage == OpusStage.COMPLETE) {
			if (!result.isEmpty()) {
				player.getInventory().placeItemBackInInventory(result);
				result = ItemStack.EMPTY;
			}
			if (!catalyst.isEmpty()) {
				//A catalyst the ritual did not use up goes back too
				player.getInventory().placeItemBackInInventory(catalyst);
				catalyst = ItemStack.EMPTY;
			}
			reset();
			return InteractionResult.SUCCESS;
		}
		if (stage.isActive()) {
			if (player.isShiftKeyDown() && held.isEmpty() && (player.getUUID().equals(owner) || player.isCreative())) {
				//Stopping keeps the ingredients, only the EMC that was paid is lost
				reset();
				player.sendOverlayMessage(PELang.RITUAL_CANCELLED.translateColored(ChatFormatting.GRAY));
				serverLevel.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
			} else if (player.isShiftKeyDown() && held.isEmpty()) {
				player.sendOverlayMessage(PELang.RITUAL_NOT_OWNER.translateColored(ChatFormatting.RED));
			} else {
				player.sendOverlayMessage(getStatus());
			}
			return InteractionResult.SUCCESS;
		}
		//Idle: put ingredients in, take them out or start the ritual
		if (!held.isEmpty()) {
			if (!isUsedByAnyRecipe(serverLevel, held)) {
				return InteractionResult.PASS;
			}
			if (catalyst.isEmpty() && belongsInCentre(serverLevel, held)) {
				catalyst = held.split(1);
				serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
				changed();
				return InteractionResult.SUCCESS;
			}
			for (int i = 0; i < getCapacity(); i++) {
				if (inputs[i].isEmpty()) {
					inputs[i] = held.split(1);
					serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 0.6F);
					changed();
					return InteractionResult.SUCCESS;
				}
			}
			player.sendOverlayMessage(getIngredientMessage());
			return InteractionResult.SUCCESS;
		}
		if (player.isShiftKeyDown()) {
			ItemStack taken = takeLast();
			if (!taken.isEmpty()) {
				player.getInventory().placeItemBackInInventory(taken);
				changed();
			}
			return InteractionResult.SUCCESS;
		}
		Optional<RecipeHolder<AthanorRecipe>> found = findRecipe(serverLevel);
		if (found.isEmpty()) {
			if (inputCount() == 0 && catalyst.isEmpty()) {
				player.sendOverlayMessage(getIngredientMessage());
			} else if (findRecipe(serverLevel, CrucibleStructure.GRAND_ATHANOR).isPresent()) {
				player.sendOverlayMessage(PELang.RITUAL_NEEDS_GRAND.translateColored(ChatFormatting.GOLD));
			} else {
				player.sendOverlayMessage(PELang.RITUAL_NO_RECIPE.translateColored(ChatFormatting.GRAY));
			}
			return InteractionResult.SUCCESS;
		}
		checkStructure();
		if (!formed) {
			player.sendOverlayMessage(getStructureMessage());
			return InteractionResult.SUCCESS;
		}
		owner = player.getUUID();
		recipeId = found.get().id();
		recipe = found.get().value();
		startStage(0);
		player.sendOverlayMessage(PELang.RITUAL_STARTED.translateColored(ChatFormatting.DARK_PURPLE));
		return InteractionResult.SUCCESS;
	}

	private ItemStack takeLast() {
		if (!catalyst.isEmpty()) {
			ItemStack taken = catalyst;
			catalyst = ItemStack.EMPTY;
			return taken;
		}
		for (int i = inputs.length - 1; i >= 0; i--) {
			if (!inputs[i].isEmpty()) {
				ItemStack taken = inputs[i];
				inputs[i] = ItemStack.EMPTY;
				return taken;
			}
		}
		return ItemStack.EMPTY;
	}

	private Component getStructureMessage() {
		CrucibleStructure.Part missing = CrucibleStructure.findMissing(level, worldPosition);
		if (missing == null) {
			return Component.empty();
		}
		BlockPos at = worldPosition.offset(missing.offset());
		return PELang.RITUAL_INCOMPLETE.translateColored(ChatFormatting.RED, CrucibleStructure.countMissing(level, worldPosition),
				missing.block().getBlock().getName(), at.getX(), at.getY(), at.getZ());
	}

	private Component getIngredientMessage() {
		Component centre = catalyst.isEmpty() ? PELang.RITUAL_NO_CATALYST.translate() : catalyst.getHoverName();
		return PELang.RITUAL_INGREDIENTS.translateColored(ChatFormatting.GRAY, inputCount(), getCapacity(), centre);
	}

	public Component getStatus() {
		Component status = PELang.RITUAL_PROGRESS.translateColored(stage.getColor(), stage.getName(), Mth.floor(100F * progress / Math.max(1, getDuration())));
		Component reason = switch (stall) {
			case NONE -> null;
			case EMC -> PELang.RITUAL_STALLED_EMC.translateColored(ChatFormatting.RED);
			case OFFLINE -> PELang.RITUAL_STALLED_OFFLINE.translateColored(ChatFormatting.RED);
			case STRUCTURE -> PELang.RITUAL_STALLED_STRUCTURE.translateColored(ChatFormatting.RED);
			case REQUIREMENT -> getRequirementMessage();
		};
		if (reason == null) {
			return status;
		}
		Component full = status.copy().append(" ").append(reason);
		if (warning > 0) {
			full = full.copy().append(" ").append(PELang.RITUAL_REBOUND_WARNING.translateColored(ChatFormatting.DARK_RED, Mth.positiveCeilDiv(warning, 20)));
		}
		return full;
	}

	private Component getRequirementMessage() {
		AthanorStage current = getCurrentStage();
		AthanorStage.Requirement needs = current == null ? null : current.needs().orElse(null);
		if (needs == null) {
			return Component.empty();
		}
		return switch (needs) {
			case SKY -> PELang.RITUAL_STALLED_SKY.translateColored(ChatFormatting.RED, stage.getName());
			case DAY -> PELang.RITUAL_STALLED_DAY.translateColored(ChatFormatting.RED, stage.getName());
			case NIGHT -> PELang.RITUAL_STALLED_NIGHT.translateColored(ChatFormatting.RED, stage.getName());
			case THUNDER -> PELang.RITUAL_STALLED_THUNDER.translateColored(ChatFormatting.RED, stage.getName());
		};
	}

	@Nullable
	private AthanorStage getCurrentStage() {
		if (recipe == null || stageIndex < 0 || stageIndex >= recipe.stages().size()) {
			return null;
		}
		return recipe.stages().get(stageIndex);
	}

	private int getDuration() {
		AthanorStage current = getCurrentStage();
		if (current == null) {
			return clientDuration;
		}
		return Math.max(1, Mth.floor(current.ticks() * ProjectEConfig.server.ritual.durationMultiplier.get()));
	}

	private long getCost() {
		AthanorStage current = getCurrentStage();
		return current == null ? 0 : (long) (current.emc() * ProjectEConfig.server.ritual.emcMultiplier.get());
	}

	private void reset() {
		owner = null;
		recipe = null;
		recipeId = null;
		stageIndex = 0;
		progress = 0;
		paid = 0;
		warning = 0;
		stall = Stall.NONE;
		setStage(OpusStage.IDLE, RuneColor.FORMED);
	}

	private void startStage(int index) {
		stageIndex = index;
		AthanorStage current = getCurrentStage();
		if (current == null) {
			reset();
			return;
		}
		setStage(current.stage(), current.rune());
	}

	private void setStage(OpusStage newStage, RuneColor newRune) {
		stage = newStage;
		rune = newRune;
		progress = 0;
		paid = 0;
		warning = 0;
		if (level != null) {
			updateGlow();
			if (newStage.getStartSound() != null) {
				level.playSound(null, worldPosition, newStage.getStartSound(), SoundSource.BLOCKS, 1.5F, newStage.getPitch());
			}
		}
		changed();
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	public static void tickServer(Level level, BlockPos pos, BlockState state, AlchemicalCrucibleBlockEntity core) {
		if ((level.getGameTime() + pos.asLong()) % 20 == 0) {
			core.checkStructure();
		}
		if (core.stage.isActive()) {
			core.tickRitual((ServerLevel) level);
		}
	}

	private void checkStructure() {
		if (level == null || level.isClientSide()) {
			return;
		}
		int nowTier = CrucibleStructure.getTier(level, worldPosition);
		if (nowTier != tier) {
			tier = nowTier;
			formed = tier > 0;
			changed();
		}
		//Also lights up parts that were replaced since the last check
		updateGlow();
	}

	private void updateGlow() {
		if (level != null && !level.isClientSide()) {
			CrucibleStructure.update(level, worldPosition, getGlow().ordinal());
		}
	}

	private RuneColor getGlow() {
		if (warning > 0) {
			//The runes flicker between red and orange while the ritual is about to rebound
			return (warning / 10) % 2 == 0 ? RuneColor.RUBEDO : RuneColor.ORANGE;
		} else if (!formed) {
			return RuneColor.OFF;
		} else if (stage.isActive()) {
			return rune;
		}
		return stage == OpusStage.COMPLETE ? RuneColor.COMPLETE : RuneColor.FORMED;
	}

	/**
	 * Finds the recipe of a ritual that was loaded from disk, or that was started before rituals came from recipes.
	 *
	 * @return false if there is no recipe for it anymore, in which case the ritual is called off and the ingredients stay
	 */
	private boolean resolveRecipe(ServerLevel level) {
		if (recipe != null) {
			return true;
		}
		if (recipeId != null) {
			Optional<RecipeHolder<?>> holder = level.recipeAccess().byKey(recipeId);
			if (holder.isPresent() && holder.get().value() instanceof AthanorRecipe athanorRecipe) {
				recipe = athanorRecipe;
			}
		}
		if (recipe == null) {
			Optional<RecipeHolder<AthanorRecipe>> found = findRecipe(level);
			if (found.isPresent()) {
				recipeId = found.get().id();
				recipe = found.get().value();
				//Continue in the first stage of the same kind as the one that was saved
				for (int i = 0; i < recipe.stages().size(); i++) {
					if (recipe.stages().get(i).stage() == stage) {
						stageIndex = i;
						break;
					}
				}
			}
		}
		if (recipe == null || getCurrentStage() == null) {
			reset();
			return false;
		}
		return true;
	}

	private void tickRitual(ServerLevel level) {
		if (!resolveRecipe(level)) {
			return;
		}
		AthanorStage current = getCurrentStage();
		ServerConfig.Ritual settings = ProjectEConfig.server.ritual;
		int duration = getDuration();
		long cost = getCost();
		//What the owner should have paid once this tick is done, so the EMC drains evenly over the whole stage
		long target = (long) ((double) cost * (progress + 1) / duration);
		long due = Math.max(0, target - paid);
		Stall newStall = Stall.NONE;
		ServerPlayer player = owner == null ? null : level.getServer().getPlayerList().getPlayer(owner);
		IKnowledgeProvider provider = null;
		if (tier < recipe.tier()) {
			//Broken, or no longer grand enough for the ritual it is doing
			newStall = Stall.STRUCTURE;
		} else if (player == null) {
			newStall = Stall.OFFLINE;
		} else if (!current.isMet(level, worldPosition)) {
			newStall = Stall.REQUIREMENT;
		} else if (due > 0) {
			provider = player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
			if (provider == null || provider.getEmc().compareTo(BigInteger.valueOf(due)) < 0) {
				newStall = Stall.EMC;
			}
		}
		if (newStall != stall) {
			stall = newStall;
			warning = 0;
			if (settings.rebound.get()) {
				if (stall == Stall.STRUCTURE) {
					warning = STRUCTURE_WARNING;
				} else if (stall == Stall.EMC && stage.reboundsWithoutEmc()) {
					warning = EMC_WARNING;
				}
			}
			if (stall != Stall.NONE) {
				if (player != null) {
					player.sendOverlayMessage(getStatus());
				}
				level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 0.5F);
			}
			updateGlow();
			changed();
		}
		if (stall != Stall.NONE) {
			if (warning > 0) {
				warning--;
				if (warning % 10 == 0) {
					updateGlow();
					level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
				}
				if (warning % 20 == 0 && player != null) {
					player.sendOverlayMessage(getStatus());
				}
				if (warning == 0) {
					rebound(level, player);
				}
			}
			return;
		}
		if (provider != null) {
			provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(due)));
			paid += due;
			if (progress % 10 == 0) {
				provider.syncEmc(player);
			}
		}
		progress++;
		setChanged();
		playAmbience(level, duration);
		if (progress >= duration) {
			if (provider != null) {
				provider.syncEmc(player);
			}
			if (stageIndex + 1 >= recipe.stages().size()) {
				complete(level, player);
			} else {
				startStage(stageIndex + 1);
			}
		} else if (progress % 20 == 0) {
			//Keep the clients roughly in step, they animate the rest on their own
			changed();
		}
	}

	private void complete(ServerLevel level, @Nullable ServerPlayer player) {
		//Check once more before anything is used up, if something is off the ingredients stay where they are
		AthanorInput input = new AthanorInput(inputList(), catalyst);
		if (recipe == null || !recipe.matches(input, level)) {
			reset();
			return;
		}
		ItemStack made = recipe.assemble(input);
		Arrays.fill(inputs, ItemStack.EMPTY);
		if (!recipe.keepCatalyst()) {
			catalyst = ItemStack.EMPTY;
		}
		result = made;
		owner = null;
		setStage(OpusStage.COMPLETE, RuneColor.COMPLETE);
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 1.2;
		double z = worldPosition.getZ() + 0.5;
		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0);
		level.sendParticles(WHITE_HOT, x, y, z, 80, 0.6, 0.6, 0.6, 0.2);
		level.sendParticles(SCARLET, x, worldPosition.getY() + 0.2, z, 120, 2.5, 0.05, 2.5, 0);
		level.sendParticles(ParticleTypes.END_ROD, x, y + 2, z, 60, 0.05, 2.5, 0.05, 0.02);
		level.playSound(null, worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
		level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.8F, 1.4F);
		if (player != null) {
			player.sendOverlayMessage(PELang.RITUAL_COMPLETE.translateColored(ChatFormatting.GOLD));
		}
	}

	/**
	 * The ritual failed and the work hits back. What is lost, and how hard it hits, depends on how far the work had come.
	 */
	private void rebound(ServerLevel level, @Nullable ServerPlayer owner) {
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 1.2;
		double z = worldPosition.getZ() + 0.5;
		RandomSource random = level.getRandom();
		OpusStage failedIn = stage;
		switch (failedIn) {
			case NIGREDO -> {
				//The blackening burns up the catalyst, the rest survives
				catalyst = ItemStack.EMPTY;
				level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 60, 1, 0.5, 1, 0.02);
				level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.5F, 0.5F);
			}
			case CAUDA_PAVONIS, ALBEDO -> {
				//One of the ingredients is lost, and everyone close by feels it
				loseInputs(random, 1);
				for (Player nearby : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(6))) {
					nearby.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 10 * 20));
					nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30 * 20));
				}
				level.sendParticles(ParticleTypes.WHITE_ASH, x, y, z, 120, 1.5, 0.8, 1.5, 0.05);
				level.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.5F, 0.5F);
			}
			case CITRINITAS -> {
				//Lightning strikes the crucible, half the ingredients and the catalyst are lost
				loseInputs(random, Mth.positiveCeilDiv(inputCount(), 2));
				catalyst = ItemStack.EMPTY;
				LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
				if (bolt != null) {
					bolt.snapTo(Vec3.atBottomCenterOf(worldPosition.above()));
					bolt.setVisualOnly(true);
					level.addFreshEntity(bolt);
				}
				for (Player nearby : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(5))) {
					nearby.hurtServer(level, level.damageSources().lightningBolt(), 6);
				}
			}
			default -> {
				//Rubedo: everything is lost in an explosion, and the owner pays for it
				Arrays.fill(inputs, ItemStack.EMPTY);
				catalyst = ItemStack.EMPTY;
				level.explode(null, x, y, z, 3.0F, false, ProjectEConfig.server.ritual.reboundBreaksBlocks.get()
															? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
				if (owner != null) {
					owner.addEffect(new MobEffectInstance(MobEffects.WITHER, 15 * 20, 1));
				}
			}
		}
		if (owner != null) {
			owner.sendOverlayMessage(PELang.RITUAL_REBOUND.translateColored(ChatFormatting.DARK_RED, failedIn.getName()));
		}
		reset();
	}

	private void loseInputs(RandomSource random, int amount) {
		List<Integer> filled = new ArrayList<>();
		for (int i = 0; i < inputs.length; i++) {
			if (!inputs[i].isEmpty()) {
				filled.add(i);
			}
		}
		for (int i = 0; i < amount && !filled.isEmpty(); i++) {
			inputs[filled.remove(random.nextInt(filled.size()))] = ItemStack.EMPTY;
		}
	}

	private void playAmbience(ServerLevel level, int duration) {
		if (stage.getAmbience() != null && progress % stage.getAmbienceInterval() == 0) {
			//The work beats faster as it gets closer to the end of the stage
			level.playSound(null, worldPosition, stage.getAmbience(), SoundSource.BLOCKS, 0.8F, stage.getPitch() + 0.4F * progress / duration);
		}
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, AlchemicalCrucibleBlockEntity core) {
		if (core.stage.isActive() && core.stall == Stall.NONE) {
			core.clientProgress = Math.min(core.clientProgress + 1, core.clientDuration);
		}
		double density = ProjectEConfig.client.ritualParticles.get();
		if (density > 0) {
			core.spawnParticles(level, pos, density);
		}
	}

	private void spawnParticles(Level level, BlockPos pos, double density) {
		RandomSource random = level.getRandom();
		double cx = pos.getX() + 0.5;
		double cy = pos.getY() + 1.5;
		double cz = pos.getZ() + 0.5;
		float fraction = getProgressFraction();
		if (stage.isActive() && stall == Stall.NONE) {
			ParticleOptions stream = switch (stage) {
				case NIGREDO -> ParticleTypes.ASH;
				case CAUDA_PAVONIS -> PEACOCK[random.nextInt(PEACOCK.length)];
				case ALBEDO -> SILVER;
				case CITRINITAS -> GOLD;
				default -> SCARLET;
			};
			fromPillars(level, random, stream, cx, cy, cz, count(1 + 2 * fraction, density, random));
		} else if (stall != Stall.NONE && random.nextFloat() < 0.3F * density) {
			level.addParticle(ParticleTypes.SMOKE, cx + random.nextGaussian() * 0.3, cy, cz + random.nextGaussian() * 0.3, 0, 0.02, 0);
		}
		switch (stage) {
			case IDLE -> {
				if (inputCount() > 0 && random.nextFloat() < 0.1F * density) {
					level.addParticle(FALSE_GLOW, cx + random.nextGaussian() * 0.3, cy, cz + random.nextGaussian() * 0.3, 0, 0, 0);
				}
			}
			case NIGREDO -> {
				//Thick smoke, and darkness pulled out of the ingredients into the catalyst
				for (int i = 0; i < count(3, density, random); i++) {
					level.addParticle(random.nextBoolean() ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SQUID_INK,
							cx + random.nextGaussian() * 0.4, pos.getY() + 0.3 + random.nextFloat() * 0.6, cz + random.nextGaussian() * 0.4, 0, 0.02, 0);
				}
				pullIntoCentre(level, random, ParticleTypes.ASH, cx, cy, cz, count(3, density, random));
			}
			case CAUDA_PAVONIS -> {
				//The peacock's tail: every colour at once, fanning out of the centre
				for (int i = 0; i < count(4, density, random); i++) {
					double angle = random.nextDouble() * Math.PI * 2;
					level.addParticle(PEACOCK[random.nextInt(PEACOCK.length)], cx + Math.cos(angle) * 0.3, cy, cz + Math.sin(angle) * 0.3,
							Math.cos(angle) * 0.15, 0.02, Math.sin(angle) * 0.15);
				}
				beams(level, random, PEACOCK[random.nextInt(PEACOCK.length)], cx, cy, cz, count(1, density, random));
			}
			case ALBEDO -> {
				for (int i = 0; i < count(3, density, random); i++) {
					level.addParticle(random.nextBoolean() ? SILVER : ParticleTypes.WHITE_ASH,
							cx + random.nextGaussian() * 0.5, pos.getY() + 0.15, cz + random.nextGaussian() * 0.5, 0, 0.06, 0);
				}
				beams(level, random, ParticleTypes.END_ROD, cx, cy, cz, count(1, density, random));
			}
			case CITRINITAS -> {
				beams(level, random, GOLD, cx, cy, cz, count(3, density, random));
				if (random.nextFloat() < 0.3F * density) {
					double angle = random.nextDouble() * Math.PI * 2;
					level.addParticle(ParticleTypes.WAX_ON, cx, cy, cz, Math.cos(angle) * 0.3, 0.02, Math.sin(angle) * 0.3);
				}
			}
			case RUBEDO -> {
				beams(level, random, SCARLET, cx, cy, cz, count(3 + 4 * fraction, density, random));
				if (random.nextFloat() < fraction * density) {
					level.addParticle(ParticleTypes.FLAME, cx + random.nextGaussian() * 0.1, cy, cz + random.nextGaussian() * 0.1, 0, 0.05, 0);
				}
				if (fraction > 0.85F) {
					//The beam that breaks out of the crucible right before the fusion
					for (int i = 0; i < count(4, density, random); i++) {
						level.addParticle(WHITE_HOT, cx, cy + random.nextFloat() * 6, cz, 0, 0.2, 0);
					}
				}
			}
			case COMPLETE -> {
				if (random.nextFloat() < 0.3F * density) {
					level.addParticle(random.nextBoolean() ? SCARLET : GOLD, cx + random.nextGaussian() * 0.2, cy + 0.2, cz + random.nextGaussian() * 0.2, 0, 0.01, 0);
				}
			}
		}
	}

	/**
	 * Particles somewhere on the line from the top of one of the four corner pillars of the Athanor to the centre.
	 */
	private static void fromPillars(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		for (int i = 0; i < count; i++) {
			double angle = Math.PI / 4 + random.nextInt(4) * Math.PI / 2;
			double along = random.nextDouble();
			double x = cx + Math.cos(angle) * 2.83 * (1 - along);
			double z = cz + Math.sin(angle) * 2.83 * (1 - along);
			double y = cy + 1.4 * (1 - along);
			level.addParticle(particle, x, y, z, 0, 0, 0);
		}
	}

	private static int count(float base, double density, RandomSource random) {
		double amount = base * density;
		int whole = (int) amount;
		return whole + (random.nextDouble() < amount - whole ? 1 : 0);
	}

	private static void pullIntoCentre(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			level.addParticle(particle, cx + Math.cos(angle) * 0.8, cy + random.nextGaussian() * 0.2, cz + Math.sin(angle) * 0.8, 0, 0, 0);
		}
	}

	/**
	 * Particles that run from one of the ingredients to the catalyst in the centre.
	 */
	private void beams(Level level, RandomSource random, ParticleOptions particle, double cx, double cy, double cz, int count) {
		int slots = Math.max(1, inputCount());
		for (int i = 0; i < count; i++) {
			double angle = getInputAngle(random.nextInt(slots), slots, level.getGameTime());
			double radius = getOrbitRadius() * random.nextDouble();
			level.addParticle(particle, cx + Math.cos(angle) * radius, cy, cz + Math.sin(angle) * radius, 0, 0, 0);
		}
	}

	/**
	 * @return how far the current stage is, from 0 to 1
	 */
	public float getProgressFraction() {
		boolean client = level != null && level.isClientSide();
		int duration = client ? clientDuration : getDuration();
		if (duration <= 0) {
			return 0;
		}
		return Mth.clamp((client ? clientProgress : progress) / duration, 0, 1);
	}

	/**
	 * @return the distance of the ingredients from the catalyst, they spiral inwards during Rubedo
	 */
	public float getOrbitRadius() {
		return stage == OpusStage.RUBEDO ? Mth.lerp(getProgressFraction(), 0.75F, 0.08F) : 0.75F;
	}

	/**
	 * @param index the place of the ingredient among the ingredients that are there
	 * @param count how many ingredients there are
	 *
	 * @return the angle of an ingredient around the catalyst. They form a regular shape that starts to turn during Citrinitas.
	 */
	public double getInputAngle(int index, int count, double time) {
		double base = index * Math.PI * 2 / Math.max(1, count) + Math.PI / 2;
		return base + switch (stage) {
			case IDLE, NIGREDO -> time * 0.01;
			case CAUDA_PAVONIS, ALBEDO -> time * 0.02;
			case CITRINITAS -> time * (0.05 + 0.1 * getProgressFraction());
			case RUBEDO -> time * (0.15 + 0.5 * getProgressFraction());
			case COMPLETE -> 0;
		};
	}

	public OpusStage getStage() {
		return stage;
	}

	public RuneColor getRune() {
		return getGlow();
	}

	public boolean isFormed() {
		return formed;
	}

	public boolean isStalled() {
		return stall != Stall.NONE;
	}

	public ItemStack getInput(int index) {
		return inputs[index];
	}

	public ItemStack getCatalystStack() {
		return catalyst;
	}

	public ItemStack getResult() {
		return result;
	}

	/**
	 * Drops everything that is in the crucible. Called when it is broken.
	 */
	public void dropContents(Level level, BlockPos pos) {
		for (int i = 0; i < inputs.length; i++) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, inputs[i]);
			inputs[i] = ItemStack.EMPTY;
		}
		Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, catalyst);
		Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, result);
		catalyst = ItemStack.EMPTY;
		result = ItemStack.EMPTY;
	}

	@Override
	public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
		if (level != null && !level.isClientSide()) {
			dropContents(level, pos);
			//Without its centre the rest of the Athanor goes dark
			CrucibleStructure.update(level, pos, RuneColor.OFF.ordinal());
		}
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	protected void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		for (int i = 0; i < inputs.length; i++) {
			//Before rituals came from recipes the ingredients were always False Philosopher's Stones, saved as stones
			String legacy = "stone_" + i;
			inputs[i] = input.read("input_" + i, ItemStack.CODEC).or(() -> input.read(legacy, ItemStack.CODEC)).orElse(ItemStack.EMPTY);
		}
		catalyst = input.read("catalyst", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		result = input.read("result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		ResourceKey<Recipe<?>> loadedId = input.read("recipe", Recipe.KEY_CODEC).orElse(null);
		if (loadedId == null || !loadedId.equals(recipeId)) {
			//Looked up again on the next tick of the server
			recipe = null;
		}
		recipeId = loadedId;
		stageIndex = input.getIntOr("stage_index", 0);
		stage = OpusStage.byName(input.getStringOr("stage", OpusStage.IDLE.getSerializedName()));
		rune = RuneColor.byGlow(input.getIntOr("rune", stage.getDefaultRune().ordinal()));
		stall = Stall.byName(input.getStringOr("stall", Stall.NONE.name()));
		progress = input.getIntOr("progress", 0);
		warning = input.getIntOr("warning", 0);
		paid = input.getLongOr("paid", 0);
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		clientProgress = progress;
		clientDuration = input.getIntOr("duration", 0);
		formed = input.getBooleanOr("formed", false);
		tier = input.getIntOr("tier", formed ? CrucibleStructure.ATHANOR : 0);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		for (int i = 0; i < inputs.length; i++) {
			if (!inputs[i].isEmpty()) {
				output.store("input_" + i, ItemStack.CODEC, inputs[i]);
			}
		}
		if (!catalyst.isEmpty()) {
			output.store("catalyst", ItemStack.CODEC, catalyst);
		}
		if (!result.isEmpty()) {
			output.store("result", ItemStack.CODEC, result);
		}
		if (recipeId != null) {
			output.store("recipe", Recipe.KEY_CODEC, recipeId);
		}
		output.putInt("stage_index", stageIndex);
		output.putString("stage", stage.getSerializedName());
		output.putInt("rune", rune.ordinal());
		output.putString("stall", stall.name());
		output.putInt("progress", progress);
		output.putInt("warning", warning);
		output.putLong("paid", paid);
		output.putInt("duration", stage.isActive() ? getDuration() : 0);
		output.putBoolean("formed", formed);
		output.putInt("tier", tier);
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
	}

	@NotNull
	@Override
	public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private enum Stall {
		NONE,
		EMC,
		OFFLINE,
		REQUIREMENT,
		STRUCTURE;

		private static Stall byName(String name) {
			for (Stall stall : values()) {
				if (stall.name().equals(name)) {
					return stall;
				}
			}
			//Saved before stages could need more than the sky
			return "SKY".equals(name) ? REQUIREMENT : NONE;
		}
	}
}
