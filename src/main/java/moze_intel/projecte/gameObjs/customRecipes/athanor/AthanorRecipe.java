package moze_intel.projecte.gameObjs.customRecipes.athanor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A ritual of the Athanor: the items that go on the dais (in any order), the catalyst in the centre, the stages the work
 * passes through and what comes out at the end.
 *
 * @param tier                     1 for the Athanor, 2 for the Grand Athanor. A recipe with more than three inputs needs tier 2.
 * @param keepCatalyst             whether the catalyst is left in the crucible instead of being used up
 * @param keepCatalystComponents   whether the result takes over the data of the catalyst, so an upgraded Klein Star keeps its EMC
 * @param catalystFull             whether the catalyst has to be filled to the brim with EMC, such as a charged Klein Star
 */
public record AthanorRecipe(int tier, List<Ingredient> inputs, Ingredient catalyst, boolean keepCatalyst, boolean keepCatalystComponents, boolean catalystFull,
							List<AthanorStage> stages, ItemStackTemplate result) implements Recipe<AthanorInput> {

	public static final int TIER_ONE_INPUTS = 3;
	public static final int MAX_INPUTS = 5;

	public static final MapCodec<AthanorRecipe> CODEC = RecordCodecBuilder.<AthanorRecipe>mapCodec(instance -> instance.group(
			Codec.intRange(1, 2).optionalFieldOf("tier", 1).forGetter(AthanorRecipe::tier),
			Ingredient.CODEC.listOf(1, MAX_INPUTS).fieldOf("inputs").forGetter(AthanorRecipe::inputs),
			Ingredient.CODEC.fieldOf("catalyst").forGetter(AthanorRecipe::catalyst),
			Codec.BOOL.optionalFieldOf("keep_catalyst", false).forGetter(AthanorRecipe::keepCatalyst),
			Codec.BOOL.optionalFieldOf("keep_catalyst_components", false).forGetter(AthanorRecipe::keepCatalystComponents),
			Codec.BOOL.optionalFieldOf("catalyst_full", false).forGetter(AthanorRecipe::catalystFull),
			AthanorStage.CODEC.listOf(1, 16).fieldOf("stages").forGetter(AthanorRecipe::stages),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(AthanorRecipe::result)
	).apply(instance, AthanorRecipe::new)).validate(recipe -> recipe.inputs.size() > TIER_ONE_INPUTS && recipe.tier < 2
			? DataResult.error(() -> "A ritual with more than " + TIER_ONE_INPUTS + " inputs needs tier 2")
			: DataResult.success(recipe));

	public static final StreamCodec<RegistryFriendlyByteBuf, AthanorRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, AthanorRecipe::tier,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INPUTS)), AthanorRecipe::inputs,
			Ingredient.CONTENTS_STREAM_CODEC, AthanorRecipe::catalyst,
			ByteBufCodecs.BOOL, AthanorRecipe::keepCatalyst,
			ByteBufCodecs.BOOL, AthanorRecipe::keepCatalystComponents,
			ByteBufCodecs.BOOL, AthanorRecipe::catalystFull,
			AthanorStage.STREAM_CODEC.apply(ByteBufCodecs.list(16)), AthanorRecipe::stages,
			ItemStackTemplate.STREAM_CODEC, AthanorRecipe::result,
			AthanorRecipe::new
	);

	public AthanorRecipe {
		inputs = List.copyOf(inputs);
		stages = List.copyOf(stages);
	}

	@Override
	public boolean matches(@NotNull AthanorInput input, @NotNull Level level) {
		return catalyst.test(input.catalyst()) && (!catalystFull || isFull(input.catalyst())) && inputsMatch(input.inputs());
	}

	private static boolean isFull(ItemStack stack) {
		IItemEmcHolder holder = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
		return holder != null && holder.getStoredEmc(stack) >= holder.getMaximumEmc(stack);
	}

	/**
	 * @return whether the given items, ignoring empty ones, are exactly the inputs of this recipe in some order
	 */
	public boolean inputsMatch(List<ItemStack> items) {
		List<ItemStack> present = new ArrayList<>(MAX_INPUTS);
		for (ItemStack item : items) {
			if (!item.isEmpty()) {
				present.add(item);
			}
		}
		return present.size() == inputs.size() && assign(present, 0, new boolean[present.size()]);
	}

	/**
	 * Tries to give every input of the recipe an item of its own. There are at most five, so trying every order is cheap.
	 */
	private boolean assign(List<ItemStack> items, int ingredient, boolean[] used) {
		if (ingredient == inputs.size()) {
			return true;
		}
		for (int i = 0; i < items.size(); i++) {
			if (!used[i] && inputs.get(ingredient).test(items.get(i))) {
				used[i] = true;
				if (assign(items, ingredient + 1, used)) {
					return true;
				}
				used[i] = false;
			}
		}
		return false;
	}

	/**
	 * @return whether the item could be used as one of the inputs of this recipe
	 */
	public boolean isInput(ItemStack stack) {
		for (Ingredient input : inputs) {
			if (input.test(stack)) {
				return true;
			}
		}
		return false;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull AthanorInput input) {
		if (keepCatalystComponents && !input.catalyst().isEmpty()) {
			return result.apply(input.catalyst().getComponentsPatch());
		}
		return result.create();
	}

	public long totalEmc() {
		long total = 0;
		for (AthanorStage stage : stages) {
			total += stage.emc();
		}
		return total;
	}

	public int totalTicks() {
		int total = 0;
		for (AthanorStage stage : stages) {
			total += stage.ticks();
		}
		return total;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return false;
	}

	@NotNull
	@Override
	public String group() {
		return "";
	}

	@NotNull
	@Override
	public RecipeSerializer<AthanorRecipe> getSerializer() {
		return PERecipeSerializers.ATHANOR.get();
	}

	@NotNull
	@Override
	public RecipeType<AthanorRecipe> getType() {
		return PERecipeTypes.ATHANOR.get();
	}

	@NotNull
	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@NotNull
	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}
}
