package moze_intel.projecte.gameObjs.registries;

import java.util.EnumMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class PEArmorMaterials {

	private PEArmorMaterials() {
	}

	//An empty tag matches nothing, so the armor is never repairable in an anvil (same as the old Ingredient.EMPTY behavior).
	private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, PECore.rl("empty_repair"));

	private static final Map<ArmorType, Integer> DIAMOND_RESISTANCES = Util.make(new EnumMap<>(ArmorType.class), map -> {
		map.put(ArmorType.BOOTS, 3);
		map.put(ArmorType.LEGGINGS, 6);
		map.put(ArmorType.CHESTPLATE, 8);
		map.put(ArmorType.HELMET, 3);
		map.put(ArmorType.BODY, 11);
	});

	public static final ArmorMaterial DARK_MATTER = new ArmorMaterial(
			37, DIAMOND_RESISTANCES, 1, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, 0.1F, NO_REPAIR, assetId("dark_matter")
	);
	public static final ArmorMaterial RED_MATTER = new ArmorMaterial(
			37, DIAMOND_RESISTANCES, 1, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, 0.2F, NO_REPAIR, assetId("red_matter")
	);
	public static final ArmorMaterial GEM_ARMOR = new ArmorMaterial(
			37, DIAMOND_RESISTANCES, 1, SoundEvents.ARMOR_EQUIP_NETHERITE, 2.0F, 0.25F, NO_REPAIR, assetId("gem_armor")
	);

	//Cloth, about as tough as leather
	public static final ArmorMaterial ALCHEMISTS_GARB = new ArmorMaterial(
			7, Util.make(new EnumMap<>(ArmorType.class), map -> {
				map.put(ArmorType.BOOTS, 1);
				map.put(ArmorType.LEGGINGS, 2);
				map.put(ArmorType.CHESTPLATE, 3);
				map.put(ArmorType.HELMET, 1);
				map.put(ArmorType.BODY, 3);
			}), 18, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, TagKey.create(Registries.ITEM, PECore.rl("repairs_alchemists_garb")), assetId("alchemists_garb")
	);

	private static ResourceKey<EquipmentAsset> assetId(String path) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, PECore.rl(path));
	}
}
