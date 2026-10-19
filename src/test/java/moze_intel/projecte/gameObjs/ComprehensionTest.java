package moze_intel.projecte.gameObjs;

import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.Map;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.EMCMappingHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
class ComprehensionTest {

	@BeforeEach
	void setup(MinecraftServer server) {
		Object2LongMap<ItemInfo> map = new Object2LongOpenHashMap<>();
		map.put(ItemInfo.fromItem(Items.IRON_INGOT), 256);
		map.put(ItemInfo.fromItem(Items.GOLD_INGOT), 2_048);
		map.put(ItemInfo.fromItem(Items.DIAMOND), 8_192);
		EMCMappingHandler.updateEmcValues(map);
	}

	@AfterEach
	void cleanup() {
		EMCMappingHandler.clearEmcMap();
	}

	@Test
	@DisplayName("Items worth up to 2,048 EMC are learned at once, anything worth more has to be studied")
	void testNeedsStudy(MinecraftServer server) {
		Assertions.assertFalse(Comprehension.needsStudy(ItemInfo.fromItem(Items.IRON_INGOT)));
		Assertions.assertFalse(Comprehension.needsStudy(ItemInfo.fromItem(Items.GOLD_INGOT)));
		Assertions.assertTrue(Comprehension.needsStudy(ItemInfo.fromItem(Items.DIAMOND)));
		//Items without a value can not be learned anyway
		Assertions.assertFalse(Comprehension.needsStudy(ItemInfo.fromItem(Items.BEDROCK)));
	}

	@Test
	@DisplayName("The progress of a player survives being saved and loaded")
	void testProgressCodec(MinecraftServer server) {
		Comprehension.Progress progress = Comprehension.Progress.CODEC.parse(JsonOps.INSTANCE,
				Comprehension.Progress.CODEC.encodeStart(JsonOps.INSTANCE, Comprehension.Progress.CODEC.parse(JsonOps.INSTANCE,
						JsonOps.INSTANCE.createMap(Map.of(JsonOps.INSTANCE.createString("minecraft:diamond"), JsonOps.INSTANCE.createInt(3))))
						.getOrThrow()).getOrThrow()).getOrThrow();
		Assertions.assertEquals(3, progress.get(ItemInfo.fromItem(Items.DIAMOND)));
		Assertions.assertEquals(0, progress.get(ItemInfo.fromItem(Items.EMERALD)));
	}
}
