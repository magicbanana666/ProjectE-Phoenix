/*
 * Copyright (c) 2019-2023 TagnumElite
 * MIT License, see LICENSE-ProjectEIntegration
 */

package com.tagnumelite.projecteintegration.addons;

import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.tagnumelite.projecteintegration.api.Utils;
import com.tagnumelite.projecteintegration.api.recipe.ARecipeTypeMapper;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSInput;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSOutput;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class MysticalAgricultureAddon {
    public static final String MODID = "mysticalagriculture";

    public static String NAME(String name) {
        return "MysticalAgriculture" + name + "Mapper";
    }

    /**
     * Mystical Agriculture 26.1 recipes keep their result private and do not implement display(), so the result
     * template is read from the recipe object.
     */
    protected abstract static class MAMapper<R extends Recipe<?>> extends ARecipeTypeMapper<R> {
        @Override
        public NSSOutput getOutput(R recipe) {
            ItemStackTemplate result = Utils.findResultTemplate(recipe);
            return result == null ? null : new NSSOutput(result);
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class MAAwakeningMapper extends MAMapper<IAwakeningRecipe> {
        @Override
        public String getName() {
            return NAME("Awakening");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == ModRecipeTypes.AWAKENING.get();
        }

        @Override
        public NSSInput getInput(IAwakeningRecipe recipe) {
            NSSInput.Builder builder = getInputBuilder();
            builder.addIngredient(recipe.getAltarIngredient());
            recipe.getPedestalIngredients().forEach(builder::addIngredient);
            recipe.getEssenceIngredients().forEach(builder::addSizedIngredient);
            return builder.build();
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class MAInfusionMapper extends MAMapper<IInfusionRecipe> {
        @Override
        public String getName() {
            return NAME("Infusion");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == ModRecipeTypes.INFUSION.get();
        }

        @Override
        protected List<Ingredient> getIngredients(IInfusionRecipe recipe) {
            List<Ingredient> ingredients = new ArrayList<>(recipe.getPedestalIngredients());
            ingredients.add(recipe.getAltarIngredient());
            return ingredients;
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class MAReprocessorMapper extends MAMapper<IReprocessorRecipe> {
        @Override
        public String getName() {
            return NAME("Reprocessor");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == ModRecipeTypes.REPROCESSOR.get();
        }

        @Override
        protected List<Ingredient> getIngredients(IReprocessorRecipe recipe) {
            return List.of(recipe.getIngredient());
        }
    }
}
