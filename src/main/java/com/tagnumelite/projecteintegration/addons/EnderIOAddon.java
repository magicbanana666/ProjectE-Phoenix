/*
 * Copyright (c) 2019-2023 TagnumElite
 * MIT License, see LICENSE-ProjectEIntegration
 */

package com.tagnumelite.projecteintegration.addons;

import com.enderio.core.common.recipes.OutputStack;
import com.enderio.enderio.content.machines.alloy.AlloySmeltingRecipe;
import com.enderio.enderio.content.machines.painting.PaintingRecipe;
import com.enderio.enderio.content.machines.sag_mill.SagMillingRecipe;
import com.enderio.enderio.content.machines.slicer.SlicingRecipe;
import com.enderio.enderio.content.machines.soul_binder.SoulBindingRecipe;
import com.enderio.enderio.content.machines.vat.FermentingRecipe;
import com.enderio.enderio.foundation.MachineRecipe;
import com.enderio.enderio.init.EIORecipeTypes;
import com.tagnumelite.projecteintegration.api.recipe.ARecipeTypeMapper;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSInput;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSOutput;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class EnderIOAddon {
    protected static final String MODID = "enderio";

    protected static String NAME(String name) {
        return "EnderIO" + name + "Mapper";
    }

    protected abstract static class EIOMachineMapper<R extends MachineRecipe<?>> extends ARecipeTypeMapper<R> {
        @Override
        public NSSOutput getOutput(R recipe) {
            List<OutputStack> results = recipe.getResultStacks(registryAccess);
            if (results.isEmpty()) return null;
            if (results.size() == 1) {
                OutputStack result = results.getFirst();
                if (result.isFluid()) return new NSSOutput(result.getFluid());
                return new NSSOutput(result.getItem());
            }
            return mapOutputs(results.stream().map(o -> o.isFluid() ? o.getFluid() : o.getItem()).toArray());
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOAlloySmeltingMapper extends EIOMachineMapper<AlloySmeltingRecipe> {
        @Override
        public String getName() {
            return NAME("AlloySmelting");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.ALLOY_SMELTING.get();
        }

        @Override
        public NSSInput getInput(AlloySmeltingRecipe recipe) {
            return convertSizedIngredients(recipe.inputs());
        }
    }

    // We ignore Fire Crafting because we must

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOSagMillingMapper extends EIOMachineMapper<SagMillingRecipe> {
        @Override
        public String getName() {
            return NAME("SagMilling");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.SAG_MILLING.get();
        }

        @Override
        protected List<Ingredient> getIngredients(SagMillingRecipe recipe) {
            return List.of(recipe.input());
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOSlicingMapper extends EIOMachineMapper<SlicingRecipe> {
        @Override
        public String getName() {
            return NAME("Slicing");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.SLICING.get();
        }

        @Override
        protected List<Ingredient> getIngredients(SlicingRecipe recipe) {
            return recipe.inputs();
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOSoulBindingMapper extends EIOMachineMapper<SoulBindingRecipe> {
        @Override
        public String getName() {
            return NAME("SoulBinding");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.SOUL_BINDING.get();
        }

        // We skip the soul vials because those are returned & TODO: Look at making souls have emc
        @Override
        protected List<Ingredient> getIngredients(SoulBindingRecipe recipe) {
            return List.of(recipe.input());
        }

        @Override
        public NSSOutput getOutput(SoulBindingRecipe recipe) {
            return new NSSOutput(recipe.output());
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOPaintingMapper extends EIOMachineMapper<PaintingRecipe> {
        @Override
        public String getName() {
            return NAME("Painting");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.PAINTING.get();
        }

        @Override
        protected List<Ingredient> getIngredients(PaintingRecipe recipe) {
            return List.of(recipe.input());
        }
    }

    @RecipeTypeMapper(requiredMods = MODID, priority = 1)
    public static class EIOFermentingMapper extends EIOMachineMapper<FermentingRecipe> {
        @Override
        public String getName() {
            return NAME("Fermenting");
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == EIORecipeTypes.VAT_FERMENTING.get();
        }

        @Override
        public NSSInput getInput(FermentingRecipe recipe) {
            return getInputBuilder().addFluid(recipe.input().amount(), recipe.input().ingredient())
                    .addItemKey(recipe.firstReagent()).addItemKey(recipe.secondReagent()).build();
        }
    }
}
