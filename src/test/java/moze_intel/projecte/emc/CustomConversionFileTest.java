package moze_intel.projecte.emc;

import moze_intel.projecte.api.conversion.CustomConversionFile;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Checks that the conversion files from the data pack are read by the core codec.
 * <p>
 * The addon's files came in its own format, and the core silently dropped them: the log only showed
 * "Failed to read conversions", so EMC was calculated from recipes only. The test fails the build
 * if a file cannot be parsed or has no conversions left.
 */
@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Check that the custom conversion files can be read")
class CustomConversionFileTest {

	/**
	 * Folder with the addon's conversion files. The path is looked up from the project root, as the tests run with the
	 * working directory of the NeoForge test runner.
	 */
	private static final Path CONVERSIONS = findProjectRoot().resolve("src/main/resources/data/projecte/pe_custom_conversions");

	private static Path findProjectRoot() {
		Path current = Path.of("").toAbsolutePath();
		while (current != null && !Files.exists(current.resolve("gradlew"))) {
			current = current.getParent();
		}
		Assertions.assertNotNull(current, "Could not find the project root");
		return current;
	}

	@Test
	@DisplayName("The addon conversion files can be read and contain conversions")
	void testExpansionConversionsAreReadable(MinecraftServer server) throws IOException {
		HolderLookup.Provider registryAccess = server.registryAccess();
		Assertions.assertTrue(Files.isDirectory(CONVERSIONS), "Conversions folder is missing: " + CONVERSIONS);
		List<Path> files;
		try (Stream<Path> found = Files.list(CONVERSIONS)) {
			files = found.filter(path -> path.toString().endsWith(".json")).sorted().toList();
		}
		Assertions.assertFalse(files.isEmpty(), "No conversion files were found");

		int conversions = 0;
		for (Path file : files) {
			String rawJson = Files.readString(file);
			CustomConversionFile parsed = CodecTestHelper.parseJson(registryAccess, CustomConversionFile.CODEC, file.getFileName().toString(), rawJson);
			for (var group : parsed.groups().values()) {
				Assertions.assertFalse(group.conversions().isEmpty(), "The file has a group without conversions: " + file);
				conversions += group.conversions().size();
			}
		}
		//The addon conversions set the EMC of fuels, matter, collectors, relays, chests and stars. If they were not read,
		//all those items only get EMC from recipes, so the count must not be empty. The threshold is below the
		//actual 248, so adding a few conversions does not require changing the test
		Assertions.assertTrue(conversions >= 200, "Suspiciously few conversions were read: " + conversions);
	}
}
