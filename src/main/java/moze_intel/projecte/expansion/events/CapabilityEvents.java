package moze_intel.projecte.expansion.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.expansion.block.entity.BlockEntityAdvancedAlchemicalChest;
import moze_intel.projecte.expansion.block.entity.BlockEntityCollector;
import moze_intel.projecte.expansion.block.entity.BlockEntityCondenserMK3;
import moze_intel.projecte.expansion.block.entity.BlockEntityEMCLink;
import moze_intel.projecte.expansion.block.entity.BlockEntityRelay;
import moze_intel.projecte.expansion.block.entity.BlockEntityTransmutationInterface;
import moze_intel.projecte.expansion.capability.CapabilityAlchemicalBookLocations;
import moze_intel.projecte.expansion.item.ItemAlchemicalBook;
import moze_intel.projecte.expansion.registries.ExpansionCapabilities;
import moze_intel.projecte.expansion.registries.ExpansionItems;
import moze_intel.projecte.expansion.util.IHasCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 26.3 note: {@code EventBusSubscriber} no longer has a {@code bus} attribute. FML routes each
 * {@code @SubscribeEvent} method by looking at whether its event implements {@code IModBusEvent}, and
 * {@link RegisterCapabilitiesEvent} is a mod bus event, so a plain {@code @EventBusSubscriber(modid = PECore.MODID)}
 * puts it on the mod bus.
 */
@EventBusSubscriber(modid = PECore.MODID)
public class CapabilityEvents {
	private CapabilityEvents() {}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(
				ExpansionCapabilities.ALCHEMICAL_BOOK_LOCATIONS_ENTITY,
				//26.3: the vanilla entity types moved off of the EntityType class onto EntityTypes
				EntityTypes.PLAYER,
				(player, dir) -> new CapabilityAlchemicalBookLocations(ItemAlchemicalBook.Mode.PLAYER, (ServerPlayer) player, null)
		);
		event.registerItem(
				ExpansionCapabilities.ALCHEMICAL_BOOK_LOCATIONS_ITEM,
				(stack, dir) -> new CapabilityAlchemicalBookLocations(ItemAlchemicalBook.Mode.STACK, null, stack),
				ExpansionItems.BASIC_ALCHEMICAL_BOOK.get(),
				ExpansionItems.ADVANCED_ALCHEMICAL_BOOK.get(),
				ExpansionItems.MASTER_ALCHEMICAL_BOOK.get(),
				ExpansionItems.ARCANE_ALCHEMICAL_BOOK.get()
		);

		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof IHasCapability icap) {
				icap.registerCapabilities(event);
			}
		}

		BlockEntityAdvancedAlchemicalChest.registerCapabilities(event);
		BlockEntityCollector.registerCapabilities(event);
		BlockEntityCondenserMK3.registerCapabilities(event);
		BlockEntityEMCLink.registerCapabilities(event);
		BlockEntityTransmutationInterface.registerCapabilities(event);
		BlockEntityRelay.registerCapabilities(event);
	}
}
