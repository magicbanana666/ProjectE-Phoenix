package moze_intel.projecte.expansion.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.expansion.util.AdvancedAlchemicalChest;
import moze_intel.projecte.expansion.util.Fuel;
import moze_intel.projecte.expansion.util.Lang;
import moze_intel.projecte.expansion.util.Matter;
import moze_intel.projecte.expansion.util.Star;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.impl.CreativeTabDeferredRegister;
import net.minecraft.world.item.CreativeModeTab;

@SuppressWarnings("unused")
public class ExpansionCreativeTabs {

	public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister(PECore.MODID, event -> {});

	//Note: Registered as "expansion" instead of "projecte" as ProjectE itself already owns the "projecte" tab.
	public static final PEDeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("expansion", Lang.ITEMGROUP, ExpansionItems.FINAL_STAR, builder ->
			builder.displayItems((displayParameters, output) -> {
				Star.setAllCreativeTab(output);
				Matter.setAllCreativeTab(output);
				Fuel.setAllCreativeTab(output);
				AdvancedAlchemicalChest.setAllCreativeTab(output);

				output.accept(ExpansionItems.FINAL_STAR_SHARD);
				output.accept(ExpansionItems.FINAL_STAR);
				output.accept(ExpansionItems.MATTER_UPGRADER);
				output.accept(ExpansionItems.INFINITE_FUEL);
				output.accept(ExpansionItems.INFINITE_STEAK);
				output.accept(ExpansionBlocks.TRANSMUTATION_INTERFACE);
				output.accept(ExpansionItems.ALCHEMISTS_CODEX);
				output.accept(ExpansionItems.KNOWLEDGE_SHARING_BOOK);
				output.accept(ExpansionItems.BASIC_ALCHEMICAL_BOOK);
				output.accept(ExpansionItems.ADVANCED_ALCHEMICAL_BOOK);
				output.accept(ExpansionItems.MASTER_ALCHEMICAL_BOOK);
				output.accept(ExpansionItems.ARCANE_ALCHEMICAL_BOOK);
				output.accept(ExpansionBlocks.COMPACT_SUN);
				output.accept(ExpansionBlocks.CONDENSER_MK3);
				output.accept(ExpansionItems.ARCANE_TRANSMUTATION_TABLET);
			})
	);
}
