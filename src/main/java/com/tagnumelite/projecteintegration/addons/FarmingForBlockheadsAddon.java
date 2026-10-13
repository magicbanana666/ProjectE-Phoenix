/*
 * Copyright (c) 2019-2023 TagnumElite
 * MIT License, see LICENSE-ProjectEIntegration
 */

package com.tagnumelite.projecteintegration.addons;

import com.tagnumelite.projecteintegration.api.recipe.ARecipeTypeMapper;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSInput;
import com.tagnumelite.projecteintegration.api.recipe.nss.NSSOutput;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import net.blay09.mods.farmingforblockheads.api.Payment;
import net.blay09.mods.farmingforblockheads.recipe.MarketRecipeImpl;
import net.blay09.mods.farmingforblockheads.recipe.ModRecipes;
import net.minecraft.world.item.crafting.RecipeType;

public class FarmingForBlockheadsAddon {
    @RecipeTypeMapper(requiredMods = "farmingforblockheads", priority = 1)
    public static class FFBMarketMapper extends ARecipeTypeMapper<MarketRecipeImpl> {
        @Override
        public NSSInput getInput(MarketRecipeImpl recipe) {
            Payment payment = recipe.payment();
            return convertSingleIngredient(payment.count(), payment.ingredient());
        }

        @Override
        public NSSOutput getOutput(MarketRecipeImpl recipe) {
            return new NSSOutput(recipe.result());
        }

        @Override
        public String getName() {
            return "FarmingForBlockheadsMarketMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == ModRecipes.marketRecipe.type();
        }
    }
}
