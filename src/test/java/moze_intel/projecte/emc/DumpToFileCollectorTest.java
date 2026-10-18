package moze_intel.projecte.emc;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import moze_intel.projecte.api.conversion.CustomConversion;
import moze_intel.projecte.api.conversion.CustomConversionFile;
import moze_intel.projecte.api.mapper.arithmetic.IValueArithmetic;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.emc.arithmetic.HiddenBigFractionArithmetic;
import moze_intel.projecte.emc.collector.DumpToFileCollector;
import moze_intel.projecte.emc.collector.LongToBigFractionCollector;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Test the EMC mapping dump file")
class DumpToFileCollectorTest {

	/**
	 * The ore and raw material blacklists explicitly set a value of zero for every blacklisted item. The dump format
	 * can only hold a positive value or the free sentinel, so dumping those zeroes used to make the whole file fail to
	 * encode and no dump was written at all.
	 */
	@Test
	@DisplayName("Zero values from the blacklists do not break the dump")
	void testZeroValuesDoNotBreakDump(MinecraftServer server, @TempDir Path tempDir) throws IOException {
		Path dumpFile = tempDir.resolve("mapping_dump.json");
		DumpToFileCollector<IValueArithmetic<BigFraction>> collector = newCollector(dumpFile, server);

		NormalizedSimpleStack blacklisted = NSSItem.createItem(Items.IRON_ORE);
		NormalizedSimpleStack valued = NSSItem.createItem(Items.DIAMOND);
		collector.setValueBefore(blacklisted, 0L);
		collector.setValueAfter(blacklisted, 0L);
		collector.setValueBefore(valued, 64L);
		collector.finishCollection(server.registryAccess());

		Assertions.assertTrue(Files.exists(dumpFile), "The dump was not written: " + dumpFile);
		CustomConversionFile parsed = CodecTestHelper.parseJson(server.registryAccess(), CustomConversionFile.CODEC, dumpFile.toString(), Files.readString(dumpFile));
		Assertions.assertEquals(64L, parsed.values().setValueBefore().getLong(valued), "A positive value must end up in the dump");
		Assertions.assertEquals(-1L, parsed.values().setValueBefore().getLong(blacklisted), "A zero value cannot be represented in the dump, it must be skipped");
		Assertions.assertEquals(-1L, parsed.values().setValueAfter().getLong(blacklisted), "A zero value cannot be represented in the dump, it must be skipped");
	}

	/**
	 * An ingredient with a crafting remainder is recorded with an amount of zero, since its cost is refunded. The
	 * conversion format rejects zero amounts, so those entries have to be dropped instead of dumped.
	 */
	@Test
	@DisplayName("Refunded ingredients do not break the dump")
	void testRefundedIngredientsDoNotBreakDump(MinecraftServer server, @TempDir Path tempDir) throws IOException {
		Path dumpFile = tempDir.resolve("mapping_dump.json");
		DumpToFileCollector<IValueArithmetic<BigFraction>> collector = newCollector(dumpFile, server);

		NormalizedSimpleStack container = NSSItem.createItem(Items.BUCKET);
		NormalizedSimpleStack milk = NSSItem.createItem(Items.MILK_BUCKET);
		NormalizedSimpleStack sugar = NSSItem.createItem(Items.SUGAR);
		Object2IntMap<NormalizedSimpleStack> refunded = new Object2IntLinkedOpenHashMap<>();
		refunded.put(container, 0);
		refunded.put(milk, 1);
		collector.addConversion(1, sugar, refunded, new HiddenBigFractionArithmetic());
		collector.finishCollection(server.registryAccess());

		Assertions.assertTrue(Files.exists(dumpFile), "The dump was not written: " + dumpFile);
		CustomConversionFile parsed = CodecTestHelper.parseJson(server.registryAccess(), CustomConversionFile.CODEC, dumpFile.toString(), Files.readString(dumpFile));
		CustomConversion conversion = parsed.getOrAddGroup("default").conversions().get(0);
		Assertions.assertEquals(1, conversion.ingredients().getInt(milk), "A normal ingredient must remain");
		Assertions.assertFalse(conversion.ingredients().containsKey(container), "An ingredient with a container return cannot be represented in the dump, it must be skipped");
	}

	/**
	 * A conversion whose only ingredient is refunded produces its output for free. The format cannot express an empty
	 * ingredient list, so such a conversion has to be left out of the dump.
	 */
	@Test
	@DisplayName("A conversion with only refunded ingredients is skipped")
	void testFullyRefundedConversionIsSkipped(MinecraftServer server, @TempDir Path tempDir) throws IOException {
		Path dumpFile = tempDir.resolve("mapping_dump.json");
		DumpToFileCollector<IValueArithmetic<BigFraction>> collector = newCollector(dumpFile, server);

		Object2IntMap<NormalizedSimpleStack> refunded = new Object2IntLinkedOpenHashMap<>();
		refunded.put(NSSItem.createItem(Items.BUCKET), 0);
		collector.addConversion(1, NSSItem.createItem(Items.MILK_BUCKET), refunded, new HiddenBigFractionArithmetic());
		collector.finishCollection(server.registryAccess());

		Assertions.assertTrue(Files.exists(dumpFile), "The dump was not written: " + dumpFile);
		CustomConversionFile parsed = CodecTestHelper.parseJson(server.registryAccess(), CustomConversionFile.CODEC, dumpFile.toString(), Files.readString(dumpFile));
		Assertions.assertTrue(parsed.groups().values().stream().allMatch(group -> group.conversions().isEmpty()),
				"A conversion without ingredients must not end up in the dump");
	}

	private static DumpToFileCollector<IValueArithmetic<BigFraction>> newCollector(Path dumpFile, MinecraftServer server) {
		SimpleGraphMapper<NormalizedSimpleStack, BigFraction, IValueArithmetic<BigFraction>> mapper = new SimpleGraphMapper<>(new HiddenBigFractionArithmetic());
		return new DumpToFileCollector<>(dumpFile, new LongToBigFractionCollector<>(mapper));
	}
}
