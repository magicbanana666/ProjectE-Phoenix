package moze_intel.projecte.expansion.lang;

import java.util.function.BiConsumer;

/**
 * The English text of the Alchemist's Codex. The numbers are the defaults of the server config.
 * <p>
 * The codex is written as the notes of an alchemist: what things do and what they cost. How to make an item is left to the
 * recipe pages that follow the text, and how to use the book is left to the book.
 */
final class CodexLang {

	private CodexLang() {
	}

	static void add(BiConsumer<String, String> lang) {
		lang.accept("codex.projecte.title", "Alchemist's Codex");
		lang.accept("codex.projecte.contents", "Contents");
		lang.accept("codex.projecte.home", "Home");
		lang.accept("codex.projecte.back", "Back");
		lang.accept("codex.projecte.chapters", "Chapters");
		lang.accept("codex.projecte.page.crafting", "Crafting");
		lang.accept("codex.projecte.page.shapeless", "Any arrangement");
		lang.accept("codex.projecte.page.ritual", "Athanor Ritual");
		lang.accept("codex.projecte.page.cauldron", "Alchemical Cauldron");
		lang.accept("codex.projecte.cauldron.conditions", "In %1$s, on %2$s, inside %3$s circles. %4$s seconds.");
		lang.accept("codex.projecte.cauldron.souls", "Takes %1$s souls.");
		lang.accept("codex.projecte.cauldron.becomes", "The cauldron is left with: %1$s.");
		lang.accept("codex.projecte.page.structure", "The Athanor");
		lang.accept("codex.projecte.structure.layer", "Layer %1$s of %2$s");
		lang.accept("codex.projecte.ritual.keeps_catalyst", "The catalyst stays");
		lang.accept("codex.projecte.ritual.stage", "%1$s seconds, %2$s EMC");
		lang.accept("codex.projecte.ritual.total", "Total: %1$s EMC over %2$s seconds");
		lang.accept("codex.projecte.ritual.needs", "Needs %1$s");
		lang.accept("codex.projecte.ritual.needs.sky", "open sky");
		lang.accept("codex.projecte.ritual.needs.day", "daylight");
		lang.accept("codex.projecte.ritual.needs.night", "the night");
		lang.accept("codex.projecte.ritual.needs.thunder", "a thunderstorm overhead");
		lang.accept("codex.projecte.intro", "Nothing is made from nothing. Every thing has its worth, and whoever knows it can take a thing apart and make another.\n\n"
				+ "These are my notes on that exchange, from the first false stone to the stars. A server may keep its own figures.");

		chapter(lang, "basics", "The Basics", "EMC, knowledge, the stones, and the first matter.");
		chapter(lang, "machines", "Machines", "Chests, collectors, relays and condensers: storing, gathering and moving EMC.");
		chapter(lang, "tools", "Tools and Armor", "Equipment of Dark and Red Matter, and the tools that dig, destroy and build with EMC.");
		chapter(lang, "trinkets", "Rings and Trinkets", "Rings, stones and amulets. Most work from the hotbar or a Curios slot and are switched with the Mode key.");
		chapter(lang, "beyond", "Beyond Red Matter", "The peacock's tail: twelve coloured matters, and the collectors, relays, flowers and links built from them.");
		chapter(lang, "artifacts", "Alchemical Artifacts", "Great stars, books that travel, and things that never run out.");
		chapter(lang, "server", "Commands and Settings", "For those who keep the server and set its prices.");

		//The Basics
		topic(lang, "emc", "EMC",
				"Every item has a worth in EMC. Cobblestone is 1, iron 256, gold 2,048, a diamond 8,192.\n\n"
				+ "Most values follow from recipes: a crafted thing is worth what went into it. What has no value can not be broken down.\n\n"
				+ "The worth of an item is in its tooltip.");
		topic(lang, "knowledge", "Knowledge",
				"To make a thing you must first understand it. Burn an item in a Transmutation Table and you learn it: from then on you can take it out again whenever you can pay its worth.\n\n"
				+ "Cheap things are understood at a glance. Anything worth more than 2,048 has to be studied first: burn four of it, and only then do you know it. Locking an item in the table teaches nothing you do not already understand.\n\n"
				+ "Your EMC and your knowledge belong to you, not to the table. Every table and tablet you touch shares them.\n\n"
				+ "Some things can only be made, never learned. The Athanor guards those.");
		topic(lang, "keybinds", "Keys",
				"V charges an item, Shift+V lowers the charge.\n"
				+ "G switches the mode, or turns a ring on and off.\n"
				+ "C is the extra function, such as the crafting grid of the stone.\n"
				+ "R fires what the held item can fire.\n"
				+ "X and Shift+X work the Gem boots and helmet.\n"
				+ "K opens a tablet worn in a Curios slot.\n\n"
				+ "All of them are in the controls, under ProjectE.");
		item(lang, "philosophers_stone",
				"The true stone. It can not be crafted: three false stones and a Nether Star are fused into it in the Athanor.\n\n"
				+ "Use it on a block to transmute it, sneak to get the other result. G sets the shape, V the size.\n\n"
				+ "C opens a crafting grid. In crafting it converts: coal into Alchemical Coal, iron into gold, gold into diamonds, and back. The stone stays in the grid.\n\n"
				+ "R throws a Mob Randomizer.");
		item(lang, "transmutation_table",
				"Where exchange happens. Burn an item to learn it and add its worth to your EMC. Whatever you know and can afford waits in the ring around the burn slot.\n\n"
				+ "Lock an item to show only things worth up to it. A Klein Star in the table stores EMC.\n\n"
				+ "Either stone makes one, false or true.");
		item(lang, "alchemical_cauldron",
				"Every work begins in the pot. Fill it with water, put what you want refined in it, and give it heat from below.\n\n"
				+ "A lit campfire or magma gives low heat. Fire or lava gives high heat. Soul fire gives soul heat, the only heat that works on souls. Ice keeps it cold.\n\n"
				+ "When what is in it, the water, the heat, the circles and the souls are right, it brews on its own. Sneak with an empty hand to take back what went in last.");
		topic(lang, "tria_prima", "The Tria Prima",
				"Salt, the body. Sulphur, the soul. Mercury, the spirit. Everything is made of the three, and the three are hidden in the covalence dusts.\n\n"
				+ "Each dust is refined in the cauldron: salt from the low dust on a gentle heat, sulphur from the medium dust on a strong one inside the salt circle, quicksilver from the high dust inside two circles.\n\n"
				+ "With calcite and bone meal, each principle becomes a chalk.");
		topic(lang, "circles", "Circles",
				"Chalk draws circles around a cauldron: salt the smallest, a block out, sulphur the next, mercury the outer one, three blocks out. Use the chalk on the cauldron.\n\n"
				+ "A circle needs open floor all the way round. Build on it, or dig under it, and it is gone. Rain washes the chalk away, slowly.\n\n"
				+ "The deeper works need more circles. A chalk lasts four.");
		topic(lang, "souls", "Souls",
				"Whatever dies inside the outer circle leaves its soul in the cauldron. An animal gives one, a monster two, a great beast fifty.\n\n"
				+ "A villager gives ten. The village notices. Every villager who hears of it remembers who keeps the cauldron: prices climb, and after a few the iron golems come for you.\n\n"
				+ "Souls are spent by the works that need them, on soul heat.");
		item(lang, "false_philosophers_stone",
				"Red Water hardened around a diamond. Brew the Red Water first, from redstone, nether wart and glowstone on soul heat with four souls. Then a diamond in it, on high heat inside all three circles, with sixteen more.\n\n"
				+ "An imitation, red with the power it barely holds. It converts fuels, from coal up to Aeternalis Fuel and back, and it makes a Transmutation Table. That is enough to reach Dark Matter. Every use wears it down, and after 256 it crumbles.\n\n"
				+ "Red Matter and everything after it needs the true stone. Three false stones and a Nether Star become one in the Athanor.");
		item(lang, "alchemical_crucible",
				"The Athanor is the alchemist's furnace, built around the Alchemical Crucible: 5 by 5 wide and 6 high, the same from every side.\n\n"
				+ "The base is a square of nine Plinths inside a ring of sixteen Steps. On it stand the Crucible in the middle, eight Dais around it, and a Pillar three blocks high on each corner.\n\n"
				+ "An Arch caps every pillar. A second ring of arches sits one block in and one higher, with the Apex over the crucible.\n\n"
				+ "When it is whole the runes wake and a circle burns on the dais. If a block is missing, the crucible says which and where.");
		topic(lang, "magnum_opus", "The Magnum Opus",
				"A ritual takes up to three items on the dais and one catalyst in the centre. Place them on the crucible one by one, sneak with an empty hand to take the last one back, and use the crucible with an empty hand to begin.\n\n"
				+ "The work passes through stages, and each one draws on the EMC of the alchemist who began it. The runes show where it is:\n\n"
				+ "Nigredo, the blackening.\n"
				+ "Cauda Pavonis, the peacock's tail, every colour at once.\n"
				+ "Albedo, the whitening.\n"
				+ "Citrinitas, the yellowing. It often wants the sky.\n"
				+ "Rubedo, the reddening.\n\n"
				+ "If the alchemist leaves, or a stage lacks what it needs, the work waits. Sneaking with an empty hand ends it: the ingredients stay, the EMC paid is gone.\n\n"
				+ "A broken Athanor or an empty purse in the late stages makes the runes flicker red. Five seconds to repair the Athanor, ten to find the EMC. After that the work rebounds, and it takes more the further it had come: in Nigredo the catalyst, in Albedo an ingredient, in Citrinitas half of everything and a bolt of lightning, in Rubedo all of it.");
		item(lang, "transmutation_tablet",
				"A Transmutation Table to carry. Worn in a Curios slot it opens with K.");
		item(lang, "tome",
				"Burned in a table, it teaches every item there is. Only craftable when the server allows it, through craftableTome in the common config.");
		topic(lang, "covalence_dust", "Covalence Dust",
				"Dust of three grades, used in many recipes and to mend tools: a damaged tool and dust of its grade in a crafting grid. Every recipe makes 40.");
		topic(lang, "fuels", "Alchemical Fuels",
				"Fuels that burn far longer than coal, and the stuff matter is made of. Each grade is four of the one before and a stone, false or true. Nine make a block.\n\n"
				+ "A collector turns fuel in its slots into the next grade.");
		item(lang, "dark_matter",
				"The first matter, dense enough to swallow light. Tools, armor, rings and machines start here. Four make a block.");
		item(lang, "red_matter",
				"Dark Matter refined with the true stone. Faster tools, stronger armor, better machines. Four make a block.");
		topic(lang, "klein_stars", "Klein Stars",
				"Stars that hold EMC. Tools and rings draw from any star you carry, and a Transmutation Table can fill them.\n\n"
				+ "Ein holds 50,000, Zwei 200,000, Drei 800,000, Vier 3.2 million, Sphere 12.8 million, Omega 51.2 million.\n\n"
				+ "The first four are made in the Athanor. The star you upgrade goes in the centre and keeps what it holds, and every one of these rituals ends under open sky. Sphere and Omega are still four of the star before them.\n\n"
				+ "A star made in the Athanor can not be learned.");

		//Machines
		item(lang, "alchemical_chest",
				"A chest of 104 slots. Repair Talismans, Gems of Eternal Density, Black Hole Bands and Void Rings keep working inside it.");
		item(lang, "alchemical_barrel",
				"The Alchemical Chest as a barrel. 104 slots, and it faces any direction.");
		topic(lang, "alchemical_bags", "Alchemical Bags",
				"104 slots you can open anywhere. Every bag of a colour opens the same inventory: two red bags hold the same things. Dye a bag to change its colour.");
		item(lang, "condenser_mk1",
				"Give it a target and it turns everything else inside into copies of that target.\n\n"
				+ "It also drinks EMC from collectors and relays beside it. With enough of those it needs no items at all.");
		item(lang, "condenser_mk2",
				"A faster condenser with separate input and output. It takes a whole stack at once, which suits pipes and hoppers.");
		topic(lang, "collectors", "Energy Collectors",
				"Collectors turn light into EMC: 4, 12 and 40 a second for MK1 to MK3, in full sun.\n\n"
				+ "They pass their EMC to condensers, relays and other collectors beside them. Fuel in their slots climbs to the next grade, and a star in the slot is charged.");
		topic(lang, "relays", "Anti-Matter Relays",
				"Relays store EMC and pass it on: 64, 192 and 640 a tick for MK1 to MK3, holding 100,000, 1 million and 10 million.\n\n"
				+ "They burn what is in their burn slot and charge a star in the other. A collector beside a relay gives it a bonus.");
		item(lang, "dm_furnace",
				"Smelts twice a second through nine slots. Ores have an even chance to smelt into two. It runs on fuel, on EMC from collectors and relays, or on a star in its fuel slot.");
		item(lang, "rm_furnace",
				"Faster still, with thirteen slots. Ores always smelt into two.");
		item(lang, "dm_pedestal",
				"Set a ring, stone or amulet on it and wake it with an empty hand. Its power then reaches a 9 by 9 by 9 area.\n\n"
				+ "The Watch speeds blocks and slows mobs. The Repair Talisman mends what players carry. Archangel's Smite looses arrows, Swiftwolf's calls lightning, Ignition burns and Zero freezes. The Harvest Goddess grows crops. Body, Soul and Life Stone feed and heal. Evertide and Volcanite start and end rain. The Mind Stone gathers experience, the Black Hole Band items.");
		topic(lang, "interdiction", "Interdiction Torch and Lantern",
				"Hostile mobs and their projectiles are pushed back from anything within 8 blocks. The lantern does the same and gives more light.");
		topic(lang, "nova", "Nova Catalyst and Cataclysm",
				"Explosives on a short fuse. The Catalyst is far stronger than TNT, the Cataclysm three times stronger again.");

		//Tools and Armor
		topic(lang, "dm_tools", "Dark Matter Tools",
				"Pickaxe, axe, shovel, sword, hoe, shears and hammer. None of them break.\n\n"
				+ "Charge them for speed and reach. The pickaxe mines three tall, wide or deep, and takes a whole vein from a single ore. The hammer digs 3 by 3, the hoe tills a field, and the sword strikes every mob around you.");
		topic(lang, "rm_tools", "Red Matter Tools",
				"The Dark Matter tools, faster, with one more charge. The sword can spare everything that is not hostile.");
		item(lang, "rm_katar",
				"Sword, axe, shears and hoe in one blade. Its extra function cuts down everything around you.");
		item(lang, "rm_morning_star",
				"Hammer, pickaxe and shovel in one, with the modes of the pickaxe.");
		topic(lang, "tridents", "Matter Tridents",
				"Four modes: loyalty, channeling at 64 EMC a bolt, riptide and shockwave. The Dark Matter trident needs rain or water for its gifts, the Red Matter one works anywhere.");
		topic(lang, "shields", "Matter Shields",
				"Shields that never break. They take banner patterns like any other.");
		topic(lang, "dm_armor", "Dark Matter Armor",
				"Armor that never breaks. A full set stops 80% of the damage you take and shrugs off explosions. The boots end fall damage, the helmet lets you breathe under water.");
		topic(lang, "rm_armor", "Red Matter Armor",
				"Each piece is a Dark Matter piece wrapped in Red Matter. A full set stops 90% of the damage.");
		topic(lang, "gem_armor", "Gem Armor",
				"The strongest armor there is.\n\n"
				+ "The helmet heals, sees in the dark (Shift+X) and walks on water. The chestplate feeds you and keeps fire and lava off. The leggings drop you fast while sneaking and push mobs away. The boots end fall damage, jump higher and step up blocks (X).\n\n"
				+ "Its offensive gifts only work if the server allows offensiveAbilities.");
		item(lang, "destruction_catalyst",
				"Clears a 3 by 3 hole up to 16 blocks deep, by charge, for 8 EMC a block, and keeps what falls. The hardest blocks are spared.");
		item(lang, "hyperkinetic_lens",
				"Fires an explosive charge. More charge, bigger blast, more EMC.");
		item(lang, "catalytic_lens",
				"The Destruction Catalyst and the Hyperkinetic Lens in one. It digs up to 64 blocks deep and fires the lens shot with R.");
		item(lang, "mercurial_eye",
				"Builds with EMC. Its extra function picks the block it places and holds a Klein Star. It creates, extends, transmutes or raises pillars; the charge sets the size.");
		topic(lang, "divining_rods", "Divining Rods",
				"Struck into the ground, a rod tells how much EMC lies beneath: 3 blocks deep for the low rod, 16 for the medium, 64 for the high. The better rods also name the richest blocks they found.");
		topic(lang, "alchemical_collection", "Alchemical Collection",
				"An enchantment for mining tools. Switched on with the extra function, it turns what you mine into EMC and teaches it to you instead of dropping it.");

		//Rings and Trinkets
		item(lang, "iron_band",
				"The band most rings are forged on.");
		item(lang, "swiftwolf_rending_gale",
				"Flight, while it sits in your hotbar or a Curios slot. Use it to throw mobs back, R calls lightning. It sips EMC while it works.");
		item(lang, "zero_ring",
				"Freezes the water and smothers the fire around you. Used, it freezes a wider area by charge.");
		item(lang, "ignition_ring",
				"Fire will not touch you. Switched on, it burns the mobs around you. Use it to light a block, R to throw a fireball.");
		item(lang, "harvest_goddess_band",
				"Switched on, plants around you grow fast. Use it to sow seeds from your inventory, sneak to spread bone meal over a wide area.");
		item(lang, "archangel_smite",
				"A bow whose arrows find their mark. One arrow on use, ten on a swing, and in its other mode it shoots on its own. Each arrow costs the worth of an arrow.");
		item(lang, "black_hole_band",
				"Switched on, it pulls dropped items within 7 blocks into your inventory. Use it on a fluid to take it.");
		item(lang, "void_ring",
				"A Black Hole Band and a Gem of Eternal Density in one. Its extra function carries you to the block you look at, up to 64 away.");
		item(lang, "arcana_ring",
				"Zero, Ignition, Harvest Goddess and Swiftwolf's in one ring, and flight that never ends. The mode picks the ring, R fires its shot.");
		item(lang, "body_stone",
				"Switched on, it feeds you half a shank a second for 64 EMC.");
		item(lang, "soul_stone",
				"Switched on, it heals half a heart a second for 64 EMC.");
		item(lang, "life_stone",
				"The Body Stone and the Soul Stone in one.");
		item(lang, "mind_stone",
				"Switched on, it keeps your experience. Switched off, each use gives 50 points back.");
		item(lang, "watch_of_flowing_time",
				"Turns the time of the world faster or backwards. Switched on, it speeds machines and plants around you and slows mobs. It costs EMC while it runs.");
		item(lang, "gem_of_eternal_density",
				"Compresses cheap things in your inventory into iron, gold, diamonds, Dark Matter or Red Matter. Sneak to switch it on, the mode picks the target, use it to choose what it may eat.");
		item(lang, "evertide_amulet",
				"Water without end. Places water, fills tanks and cauldrons, carries you across the surface, and R throws a ball of it. It stays in a crafting grid.");
		item(lang, "volcanite_amulet",
				"Lava at 32 EMC a bucket. You walk on lava and fire will not touch you. R throws a ball of it.");
		item(lang, "repair_talisman",
				"Mends every damaged item you carry, a point a second. It also works in an Alchemical Chest, a bag or on a pedestal.");

		//Beyond Red Matter
		topic(lang, "matter_tiers", "Matter Tiers",
				"Between the black of Nigredo and the white of Albedo the old alchemists saw the peacock's tail, every colour at once. The coloured matters are that stage.\n\n"
				+ "After Red come Magenta, Pink, Purple, Violet, Blue, Cyan, Green, Lime, Yellow, Orange, White and Fading. The Final Star Shard stands above them all.\n\n"
				+ "Magenta to Orange are made in the Athanor, three of the matter before on the dais and a catalyst from the world in the centre: Chorus Fruit, Pink Petals, a Shulker Shell, an Amethyst Cluster, a Heart of the Sea, an Echo Shard, a Turtle Scute, a Sculk Catalyst, a Breeze Rod, Magma Cream. Each ritual costs what six fuels of the tier before were worth.\n\n"
				+ "White and Fading are still three matter between six fuel. Matter from the Athanor, and its blocks, can not be learned.");
		topic(lang, "tier_fuels", "Coloured Fuels",
				"Every coloured tier has its fuel, from Magenta to White: four of the fuel before it and a true stone. Magenta starts from Aeternalis. Nine make a block.");
		topic(lang, "tier_collectors", "Collectors",
				"Collectors in sixteen tiers, from Basic to Final, each six times stronger than the last: Basic 4 EMC a second, Red 144, Violet some 187,000, Final almost 2 trillion. They hold a minute of their output.\n\n"
				+ "A Compact Sun above multiplies them. Each tier is the collector before it and that tier's matter. Old MK1 to MK3 collectors convert into Basic, Dark and Red.");
		topic(lang, "compressed_collectors", "Compressed Collectors",
				"Nine collectors of a tier pressed into one. Only an ingredient, for Power Flowers among others.");
		topic(lang, "tier_relays", "Relays",
				"Relays in sixteen tiers, each moving six times what the last did. The Final relay has no limit. Beside a collector they gain a bonus, shown in their tooltip.");
		topic(lang, "power_flowers", "Power Flowers",
				"A Power Flower belongs to whoever planted it and pours EMC straight into their own, every second, even while they are away. Basic gives 102 a second, every tier six times more. A Compact Sun below it multiplies that.");
		topic(lang, "emc_links", "EMC Links",
				"A link between your EMC and pipes. What is piped in becomes EMC and is learned.\n\n"
				+ "Use it with an item to set what it hands out, and pipes can draw that from it. Use it empty handed to buy a stack yourself, with an empty bucket to buy fluid, and sneak to clear it. Each tier moves more.");
		item(lang, "compact_sun",
				"A sun in a block. On a collector, or under a Power Flower, it multiplies the output by ten.");
		item(lang, "matter_upgrader",
				"Raises a collector, relay, Power Flower or EMC Link you placed by one tier without breaking it. You pay the difference, and you must already know the next tier.");
		item(lang, "condenser_mk3",
				"Five condensers in one. Each side but the bottom has its own input and target, the bottom is the shared output. Strike it with a Philosopher's Stone to turn it.");
		item(lang, "transmutation_interface",
				"Opens your knowledge to pipes and storage networks such as AE2. Items go in through the first slot, and anything you know can be drawn out against your EMC. You have to be online.");
		topic(lang, "advanced_alchemical_chests", "Advanced Alchemical Chests",
				"A chest that opens your Alchemical Bag of the same colour, so pipes can reach it. Sneak with a bag to set the colour. You have to be online.");
		topic(lang, "tier_table", "Tier Numbers",
				"Collector EMC a second:\n"
				+ "Basic 4, Dark 24, Red 144\n"
				+ "Magenta 864, Pink 5.2K\n"
				+ "Purple 31K, Violet 187K\n"
				+ "Blue 1.1M, Cyan 6.7M\n"
				+ "Green 40M, Lime 242M\n"
				+ "Yellow 1.5B, Orange 8.7B\n"
				+ "White 52B, Fading 313B\n"
				+ "Final 1.9T\n\n"
				+ "Relays move 16 times and Power Flowers about 25 times what the collector of their tier makes.");

		//Alchemical Artifacts
		topic(lang, "boss_drops", "Hearts of the Great Beasts",
				"Some things can not be bought, burned or brewed. They have to be taken.\n\n"
				+ "The Warden leaves its heart, and it still listens. The Wither leaves a Blighted Soul beside its star. The Elder Guardian leaves an eye. The Ender Dragon's heart falls onto the exit portal, every time it dies.\n\n"
				+ "The greatest works of the Athanor will ask for them. Keep them.");
		topic(lang, "big_stars", "Magnum, Colossal and Gargantuan Stars",
				"Stars that keep growing. Four Klein Star Omegas make a Magnum Star Ein of 204.8 million EMC, and every star holds four times the one before.\n\n"
				+ "After the Magnum Omega come the Colossal stars, then the Gargantuan. The Gargantuan Omega holds some 3.5 quintillion.");
		item(lang, "final_star_shard",
				"The last tier of matter, and part of the finest things there are.");
		item(lang, "final_star",
				"Eight Final Power Flowers around a dragon egg. It goes into the Compact Sun, and otherwise it is a statement.");
		topic(lang, "alchemical_books", "Alchemical Books",
				"Books that remember places and take you back to them, for EMC by the block: 1,000 for the Basic book, 500 for the Advanced, 100 for the Master, nothing for the Arcane.\n\n"
				+ "From the Advanced book on, sneaking binds a book to you, so every copy shares your places. Master and Arcane books cross between dimensions.");
		item(lang, "arcane_transmutation_tablet",
				"A Transmutation Tablet with a crafting grid that fills itself from your knowledge, and buttons to lock, burn, unlearn, rotate and spread items.");
		item(lang, "knowledge_sharing_book",
				"Sneak to write your knowledge into it. Whoever reads it next learns everything you know.");
		item(lang, "infinite_fuel",
				"Fuel that never burns out. Each time it lights a furnace its owner pays 128 EMC instead.");
		item(lang, "infinite_steak",
				"A meal that never ends. Every bite costs 64 EMC.");

		//Commands and Settings
		topic(lang, "commands", "Commands",
				"/projecte setemc, removeemc and resetemc change the worth of an item.\n"
				+ "/projecte emc and /projecte knowledge change what a player has and knows.\n"
				+ "/projecte showbag opens the bag of a player.\n"
				+ "/px emc, /px knowledge and /px book do the same for the expansion items.\n"
				+ "/px dumpfuelmap shows the order of the fuels.\n\n"
				+ "Most of these are for operators.");
		topic(lang, "emc_values", "Changing EMC Values",
				"Values come from datapacks (pe_custom_conversions), from custom_emc.json in the config folder, and from setemc. After a change, everything is worked out again on the next reload.");
		topic(lang, "integrations", "Other Mods",
				"JEI and EMI show world transmutation and the stone recipes. Jade, The One Probe and WTHIT show worth in the world. Curios adds slots for rings, amulets, stars and tablets.\n\n"
				+ "Items of Mekanism, AE2, Ender IO, Mystical Agriculture, Occultism and Farming for Blockheads get their worth from their recipes.");
		topic(lang, "configs", "Settings",
				"The files in config/ProjectE set prices, ranges and recipes. The server file holds most of them: craftableTome, offensiveAbilities, the multipliers of collectors, relays and Power Flowers, and the costs of the Athanor.");
	}

	private static void chapter(BiConsumer<String, String> lang, String id, String title, String intro) {
		lang.accept("codex.projecte.chapter." + id, title);
		lang.accept("codex.projecte.chapter." + id + ".intro", intro);
	}

	private static void topic(BiConsumer<String, String> lang, String id, String title, String text) {
		lang.accept("codex.projecte.entry." + id, title);
		item(lang, id, text);
	}

	private static void item(BiConsumer<String, String> lang, String id, String text) {
		lang.accept("codex.projecte.entry." + id + ".text", text);
	}
}
