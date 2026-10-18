package moze_intel.projecte;

import moze_intel.projecte.client.PEBlockStateProvider;
import moze_intel.projecte.client.PEItemModelProvider;
import moze_intel.projecte.client.PESpriteSourceProvider;
import moze_intel.projecte.client.sound.PESoundProvider;
import moze_intel.projecte.common.PEAdvancementsGenerator;
import moze_intel.projecte.common.PECustomConversionProvider;
import moze_intel.projecte.common.PEPackMetadataGenerator;
import moze_intel.projecte.common.PEWorldTransmutationProvider;
import moze_intel.projecte.common.loot.PEBlockLootTable;
import moze_intel.projecte.common.recipe.PERecipeProvider;
import moze_intel.projecte.common.tag.PEBlockEntityTypeTagsProvider;
import moze_intel.projecte.common.tag.PEBlockTagsProvider;
import moze_intel.projecte.common.tag.PEDamageTypeTagsProvider;
import moze_intel.projecte.common.tag.PEEntityTypeTagsProvider;
import moze_intel.projecte.common.tag.PEItemTagsProvider;
import moze_intel.projecte.common.tag.PEPotionsTagsProvider;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.gameObjs.registries.PEDamageTypes;
import moze_intel.projecte.utils.text.PELang;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = PECore.MODID)
public class ProjectEDataGenerator {

	@SubscribeEvent
	public static void gatherRegistryEntries(GatherDataRegistryEntriesEvent event) {
		//26.3: Datapack registry entries (including recipes, advancements and loot tables, which are reloadable registries now) are gathered for both
		// data runs, they belong to the server data run's output
		if (FMLEnvironment.getDist().isClient()) {
			return;
		}
		event.gatherFor(PECore.MODID)
				.add(Registries.DAMAGE_TYPE, context -> {
					for (PEDamageTypes.PEDamageType damageType : PEDamageTypes.DAMAGE_TYPES.values()) {
						context.register(damageType.key(), new DamageType(damageType.msgId(), damageType.exhaustion()));
					}
				})
				.recipe(PERecipeProvider::new)
				.advancement(PEAdvancementsGenerator::new)
				.lootTable(new SubProviderEntry(PEBlockLootTable::new, LootContextParamSets.BLOCK));
	}

	@SubscribeEvent
	public static void gatherServerData(GatherDataEvent.Server event) {
		//Ensure that we register the configs for data component processors and the like
		EMCMappingHandler.loadMappers();

		DataGenerator gen = event.getGenerator();
		PackOutput output = gen.getPackOutput();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getWorldLookupProvider();
		event.addProvider(new PEPackMetadataGenerator(output, PELang.PACK_DESCRIPTION));
		//Tag data generators
		event.addProvider(new PEBlockTagsProvider(output, lookupProvider));
		event.addProvider(new PEItemTagsProvider(output, lookupProvider));
		event.addProvider(new PEEntityTypeTagsProvider(output, lookupProvider));
		event.addProvider(new PEBlockEntityTypeTagsProvider(output, lookupProvider));
		event.addProvider(new PEDamageTypeTagsProvider(output, lookupProvider));
		event.addProvider(new PEPotionsTagsProvider(output, lookupProvider));
		//Other generators (after tags in case we need them to exist)
		//Note: Since 26.3 furnace burn times are no longer a data map, they are the cooking fuel component of the items
		event.addProvider(new PECustomConversionProvider(output, lookupProvider));
		event.addProvider(new PEWorldTransmutationProvider(output, lookupProvider));
	}

	@SubscribeEvent
	public static void gatherClientData(GatherDataEvent.Client event) {
		DataGenerator gen = event.getGenerator();
		PackOutput output = gen.getPackOutput();
		event.addProvider(new moze_intel.projecte.expansion.lang.ExpansionLangProvider(output));
		event.addProvider(new PESoundProvider(output));
		event.addProvider(new PEBlockStateProvider(output));
		event.addProvider(new PEItemModelProvider(output));
		event.addProvider(new PESpriteSourceProvider(output, event.getWorldLookupProvider()));
	}
}
