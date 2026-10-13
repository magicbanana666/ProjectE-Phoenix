/*
 * Copyright (c) 2019-2023 TagnumElite
 * MIT License, see LICENSE-ProjectEIntegration
 */

package com.tagnumelite.projecteintegration.addons;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import com.tagnumelite.projecteintegration.api.recipe.ARecipeTypeMapper;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSOutput;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class AppliedEnergisticsAddon {
    public static final String MODID = "ae2";

    public static String NAME(String name) {
        return "AppliedEnergistics" + name + "Mapper";
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class AEChargerMapper extends ARecipeTypeMapper<ChargerRecipe> {
        @Override
        public String getName() {
            return NAME("Charger");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AERecipeTypes.CHARGER;
        }

        @Override
        public NSSOutput getOutput(ChargerRecipe recipe) {
            return new NSSOutput(recipe.result());
        }

        @Override
        protected List<Ingredient> getIngredients(ChargerRecipe recipe) {
            return List.of(recipe.ingredient());
        }
    }

    // TODO: EntropyMapper

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class AEInscriberMapper extends ARecipeTypeMapper<InscriberRecipe> {
        @Override
        public String getName() {
            return NAME("Inscriber");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AERecipeTypes.INSCRIBER;
        }

        @Override
        public NSSOutput getOutput(InscriberRecipe recipe) {
            return new NSSOutput(recipe.result());
        }

        @Override
        protected List<Ingredient> getIngredients(InscriberRecipe recipe) {
            // The Inscribe recipe type does not consume the top and bottom input (they are presses)
            if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                return List.of(recipe.getMiddleInput());
            }
            List<Ingredient> ingredients = new ArrayList<>(3);
            recipe.getTopOptional().ifPresent(ingredients::add);
            ingredients.add(recipe.getMiddleInput());
            recipe.getBottomOptional().ifPresent(ingredients::add);
            return ingredients;
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class AETransformMapper extends ARecipeTypeMapper<TransformRecipe> {
        @Override
        public String getName() {
            return NAME("Transform");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AERecipeTypes.TRANSFORM;
        }

        @Override
        public NSSOutput getOutput(TransformRecipe recipe) {
            return new NSSOutput(recipe.result());
        }

        @Override
        protected List<Ingredient> getIngredients(TransformRecipe recipe) {
            return recipe.ingredients();
        }
    }
}
