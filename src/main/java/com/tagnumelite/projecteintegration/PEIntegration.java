package com.tagnumelite.projecteintegration;

import com.tagnumelite.projecteintegration.api.recipe.PEIRecipeMapper;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ProjectE Integration (TagnumElite, MIT) merged into ProjectE Phoenix. It is no longer a separate mod: ProjectE calls
 * {@link #commonSetup()} and its recipe mappers are picked up through ProjectE's own annotation scan.
 */
public class PEIntegration {
    public static final String MODID = "projecteintegration";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private PEIntegration() {
    }

    public static void debugLog(String msg, Object... args) {
        if (!FMLEnvironment.isProduction() || ProjectEConfig.common.debugLogging.get()) {
            LOGGER.info(msg, args);
        } else {
            LOGGER.debug(msg, args);
        }
    }

    public static Identifier RL(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static void commonSetup() {
        PEIRecipeMapper.loadMappers();
    }
}
