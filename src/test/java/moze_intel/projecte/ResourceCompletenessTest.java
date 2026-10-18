package moze_intel.projecte;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that every item and block has a texture and a name.
 * <p>
 * Both checks catch mistakes that do not show up in the log: an item with a model without textures looks like
 * an empty icon, and an item without an {@code item.projecte.*} key shows the raw key in its tooltip
 * (since 26.x the tooltip of a block item takes its name from the {@code item.*} key).
 */
@DisplayName("Check that every item has a texture and a name")
class ResourceCompletenessTest {
	private static final String MODID = "projecte";
	/**
	 * The tests run with the working directory of the NeoForge test runner, so the project root has to be looked for.
	 */
	private static final Path ROOT = findProjectRoot();
	private static final Path ASSETS = ROOT.resolve("src/main/resources/assets").resolve(MODID);
	/**
	 * Items that are drawn by a custom renderer and therefore have no model and no texture of their own. The trident
	 * and the shield are rendered from the vanilla textures by {@code PETridentRenderer} and {@code ShieldISTER}, which
	 * is also why the client logs "Missing block model: projecte:trident".
	 */
	private static final List<String> RENDERED_BY_CODE = List.of(
			"dark_matter_trident", "dark_matter_shield", "red_matter_trident", "red_matter_shield");
	private static final List<Path> ASSET_ROOTS = List.of(
			ASSETS,
			ROOT.resolve("src/datagen/generated-client/assets").resolve(MODID),
			ROOT.resolve("src/datagen/generated/assets").resolve(MODID));

	private static Path findProjectRoot() {
		Path dir = Path.of("").toAbsolutePath();
		while (dir != null) {
			if (Files.isDirectory(dir.resolve("src/main/resources/assets").resolve(MODID))) {
				return dir;
			}
			dir = dir.getParent();
		}
		throw new IllegalStateException("Could not find the project root: " + Path.of("").toAbsolutePath()
				+ " has no src/main/resources/assets/" + MODID);
	}

	@Test
	@DisplayName("Every item model has at least one texture, and every referenced texture exists")
	void everyItemModelHasATexture() throws IOException {
		List<String> problems = new ArrayList<>();
		assertTrue(Files.isDirectory(ASSETS.resolve("models/item")), "The assets were not found: " + ROOT);
		//Since 26.x every item also has a definition in assets/<ns>/items, which points at the model that is used
		for (Path definition : definitions("items")) {
			checkModel(definition.getFileName() + ":", modelOf(definition), problems);
		}
		//The models themselves are checked as well, that also covers the versions without item definitions
		Path itemModels = ASSETS.resolve("models/item");
		try (Stream<Path> files = Files.list(itemModels)) {
			for (Path model : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
				checkModel(model.getFileName() + ":", parse(model), problems);
			}
		}
		assertTrue(problems.isEmpty(), () -> "Items without textures:\n" + String.join("\n", problems));
	}

	/**
	 * Checks that the model resolves to at least one texture, and that every texture it uses exists.
	 */
	private static void checkModel(String what, JsonObject model, List<String> problems) {
		if (model == null) {
			return;
		}
		String id = what.replace(".json:", "");
		if (RENDERED_BY_CODE.contains(id)) {
			return;
		}
		Set<String> textures = new HashSet<>();
		Set<String> slots = new HashSet<>();
		collectTextures(model, textures, slots, new HashSet<>());
		for (String texture : textures) {
			if (texture.startsWith("#")) {
				continue;
			}
			String[] parts = split(texture);
			if (parts[0].equals(MODID) && !exists(ASSETS.resolve("textures").resolve(parts[1] + ".png"))
					&& !exists(ROOT.resolve("src/datagen/generated-client/assets").resolve(MODID).resolve("textures").resolve(parts[1] + ".png"))) {
				problems.add(what + " missing texture " + texture);
			}
		}
		if (textures.isEmpty() && slots.isEmpty()) {
			problems.add(what + " model without textures");
		}
	}

	@Test
	@DisplayName("Every hand written item model has an item.projecte.* name")
	void everyItemHasAName() throws IOException {
		assertTrue(Files.isDirectory(ASSETS.resolve("models/item")), "The assets were not found: " + ROOT);
		JsonObject en = readLang();
		if (en == null) {
			//The language file is generated, so there is nothing to check before data generation has run
			return;
		}
		List<String> problems = new ArrayList<>();
		Path itemModels = ASSETS.resolve("models/item");
		if (Files.isDirectory(itemModels)) {
			try (Stream<Path> files = Files.list(itemModels)) {
				for (Path model : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
					String id = model.getFileName().toString().replace(".json", "");
					if (!en.has("item." + MODID + "." + id)) {
						problems.add("item." + MODID + "." + id);
					}
				}
			}
		}
		assertTrue(problems.isEmpty(), () -> "Items without a name:\n" + String.join("\n", problems));
	}

	//Helpers

	private static List<Path> definitions(String folder) throws IOException {
		List<Path> result = new ArrayList<>();
		for (Path root : ASSET_ROOTS) {
			Path dir = root.resolve(folder);
			if (Files.isDirectory(dir)) {
				try (Stream<Path> files = Files.list(dir)) {
					files.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(result::add);
				}
			}
		}
		return result;
	}

	private static JsonObject readLang() throws IOException {
		for (Path root : ASSET_ROOTS) {
			Path lang = root.resolve("lang/en_us.json");
			if (Files.exists(lang)) {
				return parse(lang);
			}
		}
		return null;
	}

	private static JsonObject parse(Path path) throws IOException {
		JsonElement root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
		return root.isJsonObject() ? root.getAsJsonObject() : null;
	}

	/**
	 * Gets the model an item definition points at, following {@code select}/{@code range_dispatch}.
	 */
	private static JsonObject modelOf(Path definition) throws IOException {
		JsonElement root = parse(definition);
		return findModel(root);
	}

	private static JsonObject findModel(JsonElement node) {
		if (node == null || !node.isJsonObject()) {
			return null;
		}
		JsonObject object = node.getAsJsonObject();
		if ("minecraft:model".equals(string(object, "type")) && object.has("model")
				&& object.get("model").isJsonPrimitive()) {
			return load(object.get("model").getAsString());
		}
		for (String key : List.of("cases", "fallback", "then", "else", "model", "input")) {
			if (object.has(key)) {
				JsonObject found = findModel(object.get(key));
				if (found != null) {
					return found;
				}
			}
		}
		for (java.util.Map.Entry<String, JsonElement> entry : object.entrySet()) {
			JsonObject found = findModel(entry.getValue());
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static String string(JsonObject object, String key) {
		return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : null;
	}

	private static JsonObject load(String identifier) {
		if (!identifier.contains(":")) {
			return null;
		}
		String[] parts = split(identifier);
		if (!parts[0].equals(MODID)) {
			return null;
		}
		String relative = "models/" + parts[1] + ".json";
		for (Path root : ASSET_ROOTS) {
			Path file = root.resolve(relative);
			try {
				if (Files.exists(file)) {
					return parse(file);
				}
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	private static void collectTextures(JsonObject model, Set<String> textures, Set<String> slots, Set<JsonObject> seen) {
		if (model == null || !seen.add(model)) {
			return;
		}
		String parent = string(model, "parent");
		if (parent != null) {
			collectTextures(load(parent), textures, slots, seen);
		}
		if (model.has("textures") && model.get("textures").isJsonObject()) {
			for (var entry : model.getAsJsonObject("textures").entrySet()) {
				addTexture(entry.getValue(), textures, slots);
			}
		}
		if (model.has("elements")) {
			//Depending on the model format "elements" is either a list or a map of named elements
			for (JsonElement element : values(model.get("elements"))) {
				if (!element.isJsonObject() || !element.getAsJsonObject().has("faces")) {
					continue;
				}
				for (JsonElement face : values(element.getAsJsonObject().get("faces"))) {
					if (face.isJsonObject()) {
						addTexture(face.getAsJsonObject().get("texture"), textures, slots);
					}
				}
			}
		}
	}

	/**
	 * @return the values of a json array, or of a json object treated as a map of named values
	 */
	private static List<JsonElement> values(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return List.of();
		}
		if (element.isJsonArray()) {
			List<JsonElement> list = new ArrayList<>();
			element.getAsJsonArray().forEach(list::add);
			return list;
		}
		if (element.isJsonObject()) {
			return new ArrayList<>(element.getAsJsonObject().asMap().values());
		}
		return List.of();
	}

	private static void addTexture(JsonElement value, Set<String> textures, Set<String> slots) {
		if (value == null || !value.isJsonObject() && !value.isJsonPrimitive()) {
			return;
		}
		//Since 26.x a texture may also be an object with a sprite field
		String texture = value.isJsonObject() ? string(value.getAsJsonObject(), "sprite") : value.getAsString();
		if (texture == null) {
			return;
		}
		if (texture.startsWith("#")) {
			slots.add(texture.substring(1));
		} else {
			textures.add(texture);
		}
	}

	private static String[] split(String identifier) {
		int colon = identifier.indexOf(':');
		if (colon < 0) {
			return new String[]{"minecraft", identifier};
		}
		return new String[]{identifier.substring(0, colon), identifier.substring(colon + 1)};
	}

	private static boolean exists(Path path) {
		return Files.exists(path);
	}
}
