/*
 * Copyright (c) 2019-2023 TagnumElite
 * MIT License, see LICENSE-ProjectEIntegration
 */

package com.tagnumelite.projecteintegration.addons;

import com.klikli_dev.occultism.crafting.recipe.CrushingRecipe;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.klikli_dev.occultism.crafting.recipe.SpiritFireRecipe;
import com.klikli_dev.occultism.crafting.recipe.SpiritTradeRecipe;
import com.klikli_dev.occultism.registry.OccultismRecipes;
import com.tagnumelite.projecteintegration.api.recipe.ARecipeTypeMapper;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSOutput;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class OccultismAddon {
    public static final String MODID = "occultism";

    public static String NAME(String name) {
        return "Occultism" + name + "Mapper";
    }

    private static NSSOutput output(ItemStack stack) {
        return stack.isEmpty() ? null : new NSSOutput(stack);
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class OccultismCrushingMapper extends ARecipeTypeMapper<CrushingRecipe> {
        @Override
        public String getName() {
            return NAME("Crushing");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == OccultismRecipes.CRUSHING_TYPE.get();
        }

        @Override
        public NSSOutput getOutput(CrushingRecipe recipe) {
            return output(recipe.getResultItem());
        }

        @Override
        protected List<Ingredient> getIngredients(CrushingRecipe recipe) {
            return recipe.getIngredients();
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class OccultismRitualMapper extends ARecipeTypeMapper<RitualRecipe> {
        @Override
        public String getName() {
            return NAME("Ritual");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == OccultismRecipes.RITUAL_TYPE.get();
        }

        @Override
        public NSSOutput getOutput(RitualRecipe recipe) {
            return output(recipe.getResult());
        }

        @Override
        protected List<Ingredient> getIngredients(RitualRecipe recipe) {
            List<Ingredient> ingredients = new ArrayList<>(recipe.getIngredients());
            ingredients.add(recipe.getActivationItem());
            return ingredients;
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class OccultismSpiritFireMapper extends ARecipeTypeMapper<SpiritFireRecipe> {
        @Override
        public String getName() {
            return NAME("SpiritFire");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == OccultismRecipes.SPIRIT_FIRE_TYPE.get();
        }

        @Override
        protected List<Ingredient> getIngredients(SpiritFireRecipe recipe) {
            return recipe.getIngredients();
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class OccultismSpiritTradeMapper extends ARecipeTypeMapper<SpiritTradeRecipe> {
        @Override
        public String getName() {
            return NAME("SpiritTrade");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == OccultismRecipes.SPIRIT_TRADE_TYPE.get();
        }

        @Override
        public NSSOutput getOutput(SpiritTradeRecipe recipe) {
            return output(recipe.getResultItem());
        }

        @Override
        protected List<Ingredient> getIngredients(SpiritTradeRecipe recipe) {
            return recipe.getIngredients();
        }
    }
}
