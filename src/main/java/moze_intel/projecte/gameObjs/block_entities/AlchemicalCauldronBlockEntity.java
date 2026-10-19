package moze_intel.projecte.gameObjs.block_entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronHeat;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronInput;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronLiquid;
import moze_intel.projecte.gameObjs.customRecipes.cauldron.CauldronRecipe;
import moze_intel.projecte.gameObjs.items.ChalkItem;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Alchemical Cauldron. It holds a liquid, the items thrown into it and the souls of what died inside its circles. The
 * heat comes from the block below, see {@link CauldronHeat}. Whenever its contents, liquid, heat, circles and souls match a
 * {@link CauldronRecipe}, it brews.
 * <p>
 * Three circles can be drawn around it with chalk, the smallest one block out from it, the largest three. A circle needs
 * open floor all the way round: a block on it, or a hole under it, wipes it out. So does rain, slowly.
 */
public class AlchemicalCauldronBlockEntity extends BlockEntity {

	public static final int MAX_STACKS = CauldronRecipe.MAX_INGREDIENTS;
	public static final int MAX_SOULS = 256;
	private static final DustParticleOptions RED_WATER = new DustParticleOptions(CauldronLiquid.RED_WATER.getColor(), 1.0F);

	private final List<ItemStack> contents = new ArrayList<>();
	private CauldronLiquid liquid = CauldronLiquid.EMPTY;
	/**
	 * One bit per circle, the smallest is bit 0.
	 */
	private int circles;
	private int souls;
	@Nullable
	private UUID owner;
	@Nullable
	private ResourceKey<Recipe<?>> brewing;
	private int progress;
	/**
	 * The length of the current brew, sent to the client for its animation.
	 */
	private int brewTicks;

	public AlchemicalCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(PEBlockEntityTypes.ALCHEMICAL_CAULDRON.get(), pos, state);
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		setChanged();
	}

	@Nullable
	public UUID getOwner() {
		return owner;
	}

	/**
	 * @return how many circles are drawn, counted from the smallest outwards without gaps
	 */
	public int getCircleCount() {
		int count = 0;
		while (count < CauldronRecipe.MAX_CIRCLES && hasCircle(count + 1)) {
			count++;
		}
		return count;
	}

	public boolean hasCircle(int circle) {
		return (circles & (1 << (circle - 1))) != 0;
	}

	public int getSouls() {
		return souls;
	}

	public CauldronLiquid getLiquid() {
		return liquid;
	}

	public List<ItemStack> getContents() {
		return contents;
	}

	public boolean isBrewing() {
		return brewing != null;
	}

	public float getBrewFraction() {
		return brewTicks <= 0 ? 0 : Mth.clamp((float) progress / brewTicks, 0, 1);
	}

	public CauldronHeat getHeat() {
		return level == null ? CauldronHeat.NONE : CauldronHeat.below(level, worldPosition);
	}

	/**
	 * Adds souls, and tells whether there was room for them.
	 */
	public boolean addSouls(int amount) {
		if (souls >= MAX_SOULS) {
			return false;
		}
		souls = Math.min(MAX_SOULS, souls + amount);
		changed();
		return true;
	}

	//Using the cauldron

	public InteractionResult use(Player player, InteractionHand hand, ItemStack held) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (held.getItem() instanceof ChalkItem chalk) {
			drawCircle(serverLevel, player, hand, held, chalk.getCircle());
			return InteractionResult.SUCCESS;
		} else if (held.is(Items.WATER_BUCKET)) {
			if (liquid != CauldronLiquid.EMPTY) {
				return InteractionResult.PASS;
			}
			liquid = CauldronLiquid.WATER;
			player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.BUCKET)));
			serverLevel.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			changed();
			return InteractionResult.SUCCESS;
		} else if (held.is(Items.BUCKET)) {
			if (liquid != CauldronLiquid.WATER) {
				return InteractionResult.PASS;
			}
			liquid = CauldronLiquid.EMPTY;
			stopBrewing();
			player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.WATER_BUCKET)));
			serverLevel.playSound(null, worldPosition, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			changed();
			return InteractionResult.SUCCESS;
		} else if (!held.isEmpty()) {
			return insert(serverLevel, player, held);
		} else if (player.isShiftKeyDown()) {
			//Take back the last thing that went in
			for (int i = contents.size() - 1; i >= 0; i--) {
				ItemStack taken = contents.remove(i);
				if (!taken.isEmpty()) {
					player.getInventory().placeItemBackInInventory(taken);
					stopBrewing();
					changed();
					break;
				}
			}
			return InteractionResult.SUCCESS;
		}
		player.sendOverlayMessage(getStatus());
		return InteractionResult.SUCCESS;
	}

	private InteractionResult insert(ServerLevel level, Player player, ItemStack held) {
		if (liquid == CauldronLiquid.EMPTY) {
			player.sendOverlayMessage(PELang.CAULDRON_EMPTY.translateColored(ChatFormatting.GRAY));
			return InteractionResult.SUCCESS;
		}
		if (!isUsedByAnyRecipe(level, held)) {
			return InteractionResult.PASS;
		}
		//Merge into a stack of the same item first, a recipe counts every item of a kind as one ingredient
		for (ItemStack stack : contents) {
			if (ItemStack.isSameItemSameComponents(stack, held) && stack.getCount() < stack.getMaxStackSize()) {
				int moved = Math.min(held.getCount(), stack.getMaxStackSize() - stack.getCount());
				stack.grow(moved);
				held.shrink(moved);
				splash(level);
				return InteractionResult.SUCCESS;
			}
		}
		if (contents.size() >= MAX_STACKS) {
			player.sendOverlayMessage(PELang.CAULDRON_FULL.translateColored(ChatFormatting.GRAY));
			return InteractionResult.SUCCESS;
		}
		contents.add(held.split(held.getCount()));
		splash(level);
		return InteractionResult.SUCCESS;
	}

	private void splash(ServerLevel level) {
		level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.4F, 1.4F);
		level.sendParticles(ParticleTypes.SPLASH, worldPosition.getX() + 0.5, worldPosition.getY() + 0.95, worldPosition.getZ() + 0.5, 8, 0.2, 0, 0.2, 0);
		changed();
	}

	private static boolean isUsedByAnyRecipe(ServerLevel level, ItemStack stack) {
		for (RecipeHolder<CauldronRecipe> holder : level.recipeAccess().recipeMap().byType(PERecipeTypes.CAULDRON.get())) {
			if (holder.value().isIngredient(stack)) {
				return true;
			}
		}
		return false;
	}

	public Component getStatus() {
		Component liquidName = PELang.CAULDRON_LIQUID.translate(Component.translatable("cauldron.projecte.liquid." + liquid.getSerializedName()));
		Component heat = Component.translatable("cauldron.projecte.heat." + getHeat().getSerializedName());
		Component status = PELang.CAULDRON_STATUS.translateColored(ChatFormatting.GRAY, liquidName, heat, getCircleCount(), souls);
		if (isBrewing()) {
			return status.copy().append(" ").append(PELang.CAULDRON_BREWING.translateColored(ChatFormatting.LIGHT_PURPLE, Mth.floor(100 * getBrewFraction())));
		}
		return status;
	}

	//Circles

	/**
	 * Draws a circle around the cauldron with the chalk in the hand. The floor under the whole ring has to be clear.
	 */
	public void drawCircle(ServerLevel level, Player player, InteractionHand hand, ItemStack chalk, int circle) {
		if (hasCircle(circle)) {
			player.sendOverlayMessage(PELang.CAULDRON_CIRCLE_DRAWN.translateColored(ChatFormatting.GRAY));
			return;
		}
		BlockPos blocked = findBlocked(circle);
		if (blocked != null) {
			player.sendOverlayMessage(PELang.CAULDRON_CIRCLE_BLOCKED.translateColored(ChatFormatting.RED, blocked.getX(), blocked.getY(), blocked.getZ()));
			return;
		}
		circles |= 1 << (circle - 1);
		if (!player.isCreative()) {
			chalk.hurtAndBreak(ChalkItem.USES_PER_CIRCLE, player, hand);
		}
		level.playSound(null, worldPosition, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0F, 0.8F);
		for (BlockPos pos : ring(worldPosition, circle)) {
			level.sendParticles(ParticleTypes.WHITE_ASH, pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 3, 0.3, 0, 0.3, 0);
		}
		changed();
	}

	/**
	 * @return the blocks a circle is drawn on, all at the height of the cauldron: the square ring the given distance out
	 */
	public static List<BlockPos> ring(BlockPos centre, int circle) {
		List<BlockPos> ring = new ArrayList<>();
		for (int dx = -circle; dx <= circle; dx++) {
			for (int dz = -circle; dz <= circle; dz++) {
				if (Math.max(Math.abs(dx), Math.abs(dz)) == circle) {
					ring.add(centre.offset(dx, 0, dz));
				}
			}
		}
		return ring;
	}

	/**
	 * @return the first place of the ring that is not open floor, or null if the circle fits
	 */
	@Nullable
	private BlockPos findBlocked(int circle) {
		if (level == null) {
			return worldPosition;
		}
		for (BlockPos pos : ring(worldPosition, circle)) {
			if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
				return pos;
			}
		}
		return null;
	}

	private void checkCircles(ServerLevel level) {
		int before = circles;
		for (int circle = 1; circle <= CauldronRecipe.MAX_CIRCLES; circle++) {
			if (hasCircle(circle) && findBlocked(circle) != null) {
				eraseCircle(level, circle);
			}
		}
		//Rain wipes the chalk away, the outer circle first
		if (circles != 0 && level.isRainingAt(worldPosition.above()) && level.getRandom().nextInt(120) == 0) {
			for (int circle = CauldronRecipe.MAX_CIRCLES; circle >= 1; circle--) {
				if (hasCircle(circle)) {
					eraseCircle(level, circle);
					break;
				}
			}
		}
		if (circles != before) {
			changed();
		}
	}

	private void eraseCircle(ServerLevel level, int circle) {
		circles &= ~(1 << (circle - 1));
		for (BlockPos pos : ring(worldPosition, circle)) {
			level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 2, 0.3, 0, 0.3, 0);
		}
	}

	//Brewing

	public static void tickServer(Level level, BlockPos pos, BlockState state, AlchemicalCauldronBlockEntity cauldron) {
		ServerLevel serverLevel = (ServerLevel) level;
		if ((level.getGameTime() + pos.asLong()) % 20 == 0) {
			cauldron.checkCircles(serverLevel);
		}
		cauldron.tickBrewing(serverLevel);
	}

	private CauldronInput input() {
		return new CauldronInput(contents, liquid, getHeat(), getCircleCount(), souls);
	}

	private void tickBrewing(ServerLevel level) {
		if (liquid == CauldronLiquid.EMPTY || contents.isEmpty()) {
			stopBrewing();
			return;
		}
		boolean check = (level.getGameTime() + worldPosition.asLong()) % 10 == 0;
		if (brewing == null) {
			if (check) {
				CauldronInput input = input();
				for (RecipeHolder<CauldronRecipe> holder : level.recipeAccess().recipeMap().byType(PERecipeTypes.CAULDRON.get())) {
					if (holder.value().matches(input, level)) {
						brewing = holder.id();
						brewTicks = holder.value().ticks();
						progress = 0;
						level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
						changed();
						break;
					}
				}
			}
			return;
		}
		CauldronRecipe recipe = getBrewingRecipe(level);
		if (recipe == null || check && !recipe.matches(input(), level)) {
			//Something changed: the heat, a circle, the liquid. The brew stops and has to start over
			stopBrewing();
			return;
		}
		progress++;
		if (progress % 20 == 0) {
			changed();
		}
		if (progress >= recipe.ticks()) {
			finish(level, recipe);
		}
	}

	@Nullable
	private CauldronRecipe getBrewingRecipe(ServerLevel level) {
		if (brewing == null) {
			return null;
		}
		Optional<RecipeHolder<?>> holder = level.recipeAccess().byKey(brewing);
		return holder.isPresent() && holder.get().value() instanceof CauldronRecipe recipe ? recipe : null;
	}

	private void stopBrewing() {
		if (brewing != null) {
			brewing = null;
			progress = 0;
			brewTicks = 0;
			changed();
		}
	}

	private void finish(ServerLevel level, CauldronRecipe recipe) {
		CauldronInput input = input();
		int[] assigned = recipe.assign(contents);
		if (assigned == null) {
			stopBrewing();
			return;
		}
		ItemStack result = recipe.assemble(input);
		for (int i = 0; i < assigned.length; i++) {
			contents.get(assigned[i]).shrink(recipe.ingredients().get(i).count());
		}
		contents.removeIf(ItemStack::isEmpty);
		souls -= recipe.souls();
		recipe.resultLiquid().ifPresent(newLiquid -> liquid = newLiquid);
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 1.0;
		double z = worldPosition.getZ() + 0.5;
		if (!result.isEmpty()) {
			ItemEntity entity = new ItemEntity(level, x, y, z, result, 0, 0.25, 0);
			entity.setDefaultPickUpDelay();
			level.addFreshEntity(entity);
		}
		level.sendParticles(liquid == CauldronLiquid.RED_WATER ? RED_WATER : ParticleTypes.HAPPY_VILLAGER, x, y, z, 20, 0.3, 0.2, 0.3, 0.05);
		level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.4F);
		brewing = null;
		progress = 0;
		brewTicks = 0;
		changed();
	}

	public static void tickClient(Level level, BlockPos pos, BlockState state, AlchemicalCauldronBlockEntity cauldron) {
		if (cauldron.liquid == CauldronLiquid.EMPTY) {
			return;
		}
		RandomSource random = level.getRandom();
		double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
		double y = pos.getY() + 0.95;
		double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
		//The hotter, the more it bubbles
		switch (cauldron.getHeat()) {
			case COLD -> {
				if (random.nextInt(10) == 0) {
					level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0.02, 0);
				}
			}
			case LOW -> {
				if (random.nextInt(6) == 0) {
					level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.02, 0);
				}
			}
			case HIGH -> {
				level.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0.04, 0);
				if (random.nextInt(4) == 0) {
					level.addParticle(ParticleTypes.CLOUD, x, y + 0.1, z, 0, 0.03, 0);
				}
			}
			case SOUL -> {
				if (random.nextInt(2) == 0) {
					level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.02, 0);
				}
				if (random.nextInt(8) == 0) {
					level.addParticle(ParticleTypes.SOUL, x, y, z, 0, 0.03, 0);
				}
			}
			default -> {
			}
		}
		if (cauldron.isBrewing() && random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.WITCH, x, y + 0.1, z, 0, 0.05, 0);
		}
	}

	//Saving and syncing

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	public void dropContents(Level level, BlockPos pos) {
		for (ItemStack stack : contents) {
			Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack);
		}
		contents.clear();
	}

	@Override
	public void preRemoveSideEffects(@NotNull BlockPos pos, @NotNull BlockState state) {
		if (level != null && !level.isClientSide()) {
			dropContents(level, pos);
		}
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	protected void loadAdditional(@NotNull ValueInput input) {
		super.loadAdditional(input);
		contents.clear();
		input.read("contents", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(stacks -> {
			for (ItemStack stack : stacks) {
				if (!stack.isEmpty()) {
					contents.add(stack);
				}
			}
		});
		liquid = CauldronLiquid.byName(input.getStringOr("liquid", CauldronLiquid.EMPTY.getSerializedName()));
		circles = input.getIntOr("circles", 0);
		souls = input.getIntOr("souls", 0);
		owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
		brewing = input.read("brewing", Recipe.KEY_CODEC).orElse(null);
		progress = input.getIntOr("progress", 0);
		brewTicks = input.getIntOr("brew_ticks", 0);
	}

	@Override
	protected void saveAdditional(@NotNull ValueOutput output) {
		super.saveAdditional(output);
		if (!contents.isEmpty()) {
			output.store("contents", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(contents));
		}
		output.putString("liquid", liquid.getSerializedName());
		output.putInt("circles", circles);
		output.putInt("souls", souls);
		if (owner != null) {
			output.store("owner", UUIDUtil.CODEC, owner);
		}
		if (brewing != null) {
			output.store("brewing", Recipe.KEY_CODEC, brewing);
		}
		output.putInt("progress", progress);
		output.putInt("brew_ticks", brewTicks);
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
}
