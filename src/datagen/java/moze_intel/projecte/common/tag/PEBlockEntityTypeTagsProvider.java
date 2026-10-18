package moze_intel.projecte.common.tag;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.gameObjs.PETags.BlockEntities;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public class PEBlockEntityTypeTagsProvider extends TagsProvider<BlockEntityType<?>> {

	public PEBlockEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, Registries.BLOCK_ENTITY_TYPE, lookupProvider);
	}

	@Override
	protected void addTags(@NotNull HolderLookup.Provider provider) {
		tag(BlockEntities.BLACKLIST_TIME_WATCH).add(
				PEBlockEntityTypes.DARK_MATTER_PEDESTAL.getKey()
		);
	}

	@NotNull
	@Override
	public String getName() {
		return "Block Entity Type Tags";
	}
}
