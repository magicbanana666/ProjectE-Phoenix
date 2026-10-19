package moze_intel.projecte.gameObjs.customRecipes.cauldron;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PERecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A recipe of the Alchemical Cauldron. The items are put in the cauldron, and while it is filled with the right liquid, on
 * the right heat, inside enough circles and with enough souls in it, they brew into the result. A recipe can make an item,
 * change the liquid, or both.
 *
 * @param heat    the heat it needs, any heat when absent
 * @param circles how many circles it needs, counted from the smallest
 * @param souls   the souls it uses up
 */
public record CauldronRecipe(List<SizedIngredient> ingredients, CauldronLiquid liquid, Optional<CauldronHeat> heat, int circles, int souls, int ticks,
							 Optional<ItemStackTemplate> result, Optional<CauldronLiquid> resultLiquid) implements Recipe<CauldronInput> {

	public static final int MAX_INGREDIENTS = 6;
	public static final int MAX_CIRCLES = 3;

	public static final MapCodec<CauldronRecipe> CODEC = RecordCodecBuilder.<CauldronRecipe>mapCodec(instance -> instance.group(
			SizedIngredient.NESTED_CODEC.listOf(1, MAX_INGREDIENTS).fieldOf("ingredients").forGetter(CauldronRecipe::ingredients),
			CauldronLiquid.CODEC.optionalFieldOf("liquid", CauldronLiquid.WATER).forGetter(CauldronRecipe::liquid),
			CauldronHeat.CODEC.optionalFieldOf("heat").forGetter(CauldronRecipe::heat),
			Codec.intRange(0, MAX_CIRCLES).optionalFieldOf("circles", 0).forGetter(CauldronRecipe::circles),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("souls", 0).forGetter(CauldronRecipe::souls),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("ticks", 100).forGetter(CauldronRecipe::ticks),
			ItemStackTemplate.CODEC.optionalFieldOf("result").forGetter(CauldronRecipe::result),
			CauldronLiquid.CODEC.optionalFieldOf("result_liquid").forGetter(CauldronRecipe::resultLiquid)
	).apply(instance, CauldronRecipe::new)).validate(recipe -> {
		if (recipe.result.isEmpty() && recipe.resultLiquid.isEmpty()) {
			return DataResult.error(() -> "A cauldron recipe needs a result, a result_liquid, or both");
		} else if (recipe.liquid == CauldronLiquid.EMPTY) {
			return DataResult.error(() -> "Nothing brews in an empty cauldron");
		}
		return DataResult.success(recipe);
	});

	public static final StreamCodec<RegistryFriendlyByteBuf, CauldronRecipe> STREAM_CODEC = StreamCodec.composite(
			SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_INGREDIENTS)), CauldronRecipe::ingredients,
			ByteBufCodecs.idMapper(i -> CauldronLiquid.values()[i], CauldronLiquid::ordinal), CauldronRecipe::liquid,
			ByteBufCodecs.optional(ByteBufCodecs.idMapper(i -> CauldronHeat.values()[i], CauldronHeat::ordinal)), CauldronRecipe::heat,
			ByteBufCodecs.VAR_INT, CauldronRecipe::circles,
			ByteBufCodecs.VAR_INT, CauldronRecipe::souls,
			ByteBufCodecs.VAR_INT, CauldronRecipe::ticks,
			ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), CauldronRecipe::result,
			ByteBufCodecs.optional(ByteBufCodecs.idMapper(i -> CauldronLiquid.values()[i], CauldronLiquid::ordinal)), CauldronRecipe::resultLiquid,
			CauldronRecipe::new
	);

	public CauldronRecipe {
		ingredients = List.copyOf(ingredients);
	}

	@Override
	public boolean matches(@NotNull CauldronInput input, @NotNull Level level) {
		return input.liquid() == liquid && (heat.isEmpty() || heat.get() == input.heat()) && input.circles() >= circles
			   && input.souls() >= souls && assign(input.contents()) != null;
	}

	/**
	 * Gives every ingredient a stack of its own from the contents, with enough items in it. Every stack in the cauldron has to
	 * be used, a recipe does not brew with something foreign in the pot.
	 *
	 * @return for every ingredient the index of its stack in the contents, or null if they do not fit
	 */
	@Nullable
	public int[] assign(List<ItemStack> contents) {
		List<ItemStack> present = new ArrayList<>();
		for (ItemStack stack : contents) {
			if (!stack.isEmpty()) {
				present.add(stack);
			}
		}
		if (present.size() != ingredients.size()) {
			return null;
		}
		int[] assigned = new int[ingredients.size()];
		if (!assign(present, 0, new boolean[present.size()], assigned)) {
			return null;
		}
		//Translate the indices back to the contents, which may have empty slots
		for (int i = 0; i < assigned.length; i++) {
			assigned[i] = contents.indexOf(present.get(assigned[i]));
		}
		return assigned;
	}

	private boolean assign(List<ItemStack> present, int ingredient, boolean[] used, int[] assigned) {
		if (ingredient == ingredients.size()) {
			return true;
		}
		SizedIngredient wanted = ingredients.get(ingredient);
		for (int i = 0; i < present.size(); i++) {
			if (!used[i] && wanted.ingredient().test(present.get(i)) && present.get(i).getCount() >= wanted.count()) {
				used[i] = true;
				assigned[ingredient] = i;
				if (assign(present, ingredient + 1, used, assigned)) {
					return true;
				}
				used[i] = false;
			}
		}
		return false;
	}

	public boolean isIngredient(ItemStack stack) {
		for (SizedIngredient ingredient : ingredients) {
			if (ingredient.ingredient().test(stack)) {
				return true;
			}
		}
		return false;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CauldronInput input) {
		return result.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
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
	public RecipeSerializer<CauldronRecipe> getSerializer() {
		return PERecipeSerializers.CAULDRON.get();
	}

	@NotNull
	@Override
	public RecipeType<CauldronRecipe> getType() {
		return PERecipeTypes.CAULDRON.get();
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
