package moze_intel.projecte.expansion.util;

import com.mojang.serialization.MapCodec;
import java.math.BigInteger;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.expansion.config.Config;
import moze_intel.projecte.expansion.registries.ExpansionDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEContextIntProviders;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;
import org.jetbrains.annotations.Nullable;

/**
 * Burn time of the infinite fuel: burns for the configured amount of time, as long as the owner can pay the EMC price, and pays that price here.
 * <p>
 * 26.1 made the crafting remainder a fixed value taken from the item properties, so the addon can no longer charge the owner when the furnace hands the item back.
 * Asking for the burn time is the only place left that runs exactly once per lighting of a furnace, which is when the fuel is actually used up. Since 26.3 the burn
 * time is resolved from the cooking fuel component, so the charge happens in this provider, which reads the fuel stack from the queried stack parameter.
 */
public final class InfiniteFuelBurnTime implements ContextIntProvider {

	public static final InfiniteFuelBurnTime INSTANCE = new InfiniteFuelBurnTime();

	private InfiniteFuelBurnTime() {
	}

	@Override
	public int getIntUnsafe(LootContext context) {
		ItemInstance stack = context.getOptional(NeoForgeLootContextParams.QUERIED_STACK);
		if (stack == null) return 0;
		//Mods such as Thermal query burn times before the server config is loaded
		int cost = Config.server.infiniteFuelCost.getOrDefault();
		int burnTime = Config.server.infiniteFuelBurnTime.getOrDefault();
		if (cost == 0 || burnTime == 0) return 0;
		@Nullable ExpansionDataComponentTypes.OwnerData owner = stack.get(ExpansionDataComponentTypes.OWNER);
		@Nullable IKnowledgeProvider provider = owner == null ? null : Util.getKnowledgeProvider(owner.uuid());
		if (owner == null || provider == null || provider.getEmc().compareTo(BigInteger.valueOf(cost)) < 0) return 0;
		provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(cost)));
		ServerPlayer player = Util.getPlayer(owner.uuid());
		if (player != null) provider.syncEmc(player);
		return burnTime;
	}

	@Override
	public void validate(ValidationContext context) {
	}

	@Override
	public MapCodec<InfiniteFuelBurnTime> codec() {
		return PEContextIntProviders.INFINITE_FUEL_TYPE.value();
	}
}
