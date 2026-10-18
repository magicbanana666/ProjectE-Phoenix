package moze_intel.projecte.gameObjs.registries;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import moze_intel.projecte.impl.capability.AlchBagImpl.AlchemicalBagAttachment;
import moze_intel.projecte.impl.capability.KnowledgeImpl.KnowledgeAttachment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class PEAttachmentTypes {

	private PEAttachmentTypes() {
	}

	public static final PEDeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = new PEDeferredRegister<>(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PECore.MODID);

	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<AlchemicalBagAttachment>> ALCHEMICAL_BAGS = ATTACHMENT_TYPES.register("alchemical_bags",
			() -> AttachmentType.builder(AlchemicalBagAttachment::new)
					.serialize(MapCodec.assumeMapUnsafe(AlchemicalBagAttachment.CODEC))
					.copyHandler(AlchemicalBagAttachment::copy)
					.copyOnDeath()
					.build()
	);

	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<KnowledgeAttachment>> KNOWLEDGE = ATTACHMENT_TYPES.register("knowledge",
			() -> AttachmentType.builder(KnowledgeAttachment::new)
					.serialize(MapCodec.assumeMapUnsafe(KnowledgeAttachment.CODEC))
					.copyHandler(KnowledgeAttachment::copy)
					.copyOnDeath()
					.build()
	);

	public static final PEDeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> GEM_ARMOR_STATE = ATTACHMENT_TYPES.register("gem_armor_state",
			() -> AttachmentType.builder(holder -> false)
					.serialize(new IAttachmentSerializer<>() {
						@Override
						public Boolean read(IAttachmentHolder holder, ValueInput input) {
							return input.getBooleanOr("value", false);
						}

						@Override
						public boolean write(Boolean attachment, ValueOutput output) {
							output.putBoolean("value", attachment);
							return true;
						}
					})
					.copyHandler((attachment, holder, provider) -> attachment ? true : null)
					.copyOnDeath()
					.build()
	);

	public static <HANDLER extends IItemHandlerModifiable> HANDLER copyHandler(IItemHandler handler, Int2ObjectFunction<HANDLER> handlerCreator) {
		int slots = handler.getSlots();
		HANDLER handlerCopy = handlerCreator.get(slots);
		for (int i = 0; i < slots; i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				handlerCopy.setStackInSlot(i, stack.copy());
			}
		}
		return handlerCopy;
	}
}