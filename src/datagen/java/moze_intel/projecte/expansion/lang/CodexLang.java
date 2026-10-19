package moze_intel.projecte.expansion.lang;

import java.util.function.BiConsumer;

/**
 * The English text of the Alchemist's Codex. The numbers are the defaults of the server config.
 */
final class CodexLang {

	private CodexLang() {
	}

	static void add(BiConsumer<String, String> lang) {
		lang.accept("codex.projecte.title", "Alchemist's Codex");
		lang.accept("codex.projecte.contents", "Contents");
		lang.accept("codex.projecte.intro", "Everything there is to know about Equivalent Exchange, written down by those who traded their way to the stars.\n\n"
				+ "Pick a chapter on the contents page, then an entry. Click the blue names to jump to them, scroll or use the arrows to turn pages.\n\n"
				+ "All numbers are the defaults, a server may have changed them.");

		chapter(lang, "basics", "The Basics", "What EMC is, how to learn items and the first things to craft.");
		chapter(lang, "machines", "Machines", "Blocks that store, generate, move and condense EMC.");
		chapter(lang, "tools", "Tools and Armor", "Dark and Red Matter equipment and the tools that destroy, build and dig with EMC.");
		chapter(lang, "trinkets", "Rings and Trinkets", "Rings, stones and amulets. Most of them work from the hotbar or a Curios slot and are turned on with the Mode key.");
		chapter(lang, "beyond", "Beyond Red Matter", "Thirteen more tiers of matter, and the collectors, relays, power flowers and links built from them.");
		chapter(lang, "artifacts", "Alchemical Artifacts", "Huge stars, teleporting books and other rare items.");
		chapter(lang, "server", "Commands and Settings", "For players who run the server or want to change EMC values.");

		//The Basics
		topic(lang, "emc", "EMC",
				"EMC is the energy every item is worth. Cobblestone is worth 1, iron 256, gold 2048, a diamond 8192.\n\n"
				+ "Most values are calculated from recipes, so a crafted item is worth what its ingredients are worth. Items without a value can not be turned into EMC.\n\n"
				+ "The tooltip of an item shows its value.");
		topic(lang, "knowledge", "Knowledge",
				"Before you can create an item you have to learn it. Put it into a Transmutation Table: the item is turned into EMC and from then on you can take it out again whenever you have the EMC.\n\n"
				+ "Your EMC and the items you know belong to you, not to the table. Every table and tablet you use shares them.\n\n"
				+ "A Tome of Knowledge teaches every item at once.");
		topic(lang, "keybinds", "Keybinds",
				"V: Charge, Shift+V lowers it.\n"
				+ "G: Mode, or turns rings on and off.\n"
				+ "C: Extra function, such as the crafting grid of the Philosopher's Stone.\n"
				+ "R: Fire the projectile of the held item.\n"
				+ "X / Shift+X: Effects of the Gem boots and helmet.\n"
				+ "K: Opens a Transmutation Tablet worn in a Curios slot.\n\n"
				+ "All of them can be changed in the controls, under ProjectE.");
		item(lang, "philosophers_stone",
				"The first thing every alchemist makes.\n\n"
				+ "Right click a block to transmute it, sneak right click for the other result. G changes the shape (cube, panel, line), V the size. JEI lists every world transmutation.\n\n"
				+ "C opens a crafting grid. In crafting the stone converts materials: 4 coal into Alchemical Coal, 8 iron into gold, 4 gold into a diamond and back. It stays in the grid.\n\n"
				+ "R fires a Mob Randomizer.\n\n"
				+ "It can not be crafted: it is made from three False Philosopher's Stones and a Nether Star in the Athanor.");
		item(lang, "transmutation_table",
				"The heart of alchemy.\n\n"
				+ "Put an item into the burn slot to learn it and turn it into EMC. Learned items you can afford show in the ring around it, click one to take it out.\n\n"
				+ "Lock an item to only show items worth up to its value, use the search box to find one. Klein Stars in the table store EMC.\n\n"
				+ "Recipe: a False Philosopher's Stone (or the real one) surrounded by stone and obsidian. Either stone stays in the grid.");
		item(lang, "false_philosophers_stone",
				"What the old recipe makes now: a diamond surrounded by redstone and glowstone dust. An imitation with only a fragment of the power of the real stone.\n\n"
				+ "It can convert fuels: coal into Alchemical Coal, Mobius Fuel and Aeternalis Fuel and back, so Dark Matter is within reach. It also makes a Transmutation Table. Red Matter and everything else needs the real stone. Like the real one it stays in the crafting grid.\n\n"
				+ "Three of them and a Nether Star become the true Philosopher's Stone in the Athanor.");
		item(lang, "alchemical_crucible",
				"The Athanor is the alchemist's furnace, where the Magnum Opus takes place: 55 blocks, 5 by 5 wide and 6 high, built around the Alchemical Crucible. The same in every direction.\n\n"
				+ "Base: nine Athanor Plinths in a 3 by 3 square, sixteen Athanor Steps around them.\n\n"
				+ "On top of the base: the Alchemical Crucible on the middle plinth, eight Athanor Dais around it, and an Athanor Pillar on each corner step.\n\n"
				+ "Every pillar is three blocks high. On top of each pillar goes an Athanor Arch, a second arch goes one block in towards the centre and one block higher, and the Athanor Apex sits above the crucible, at the height of the second arches. The arches turn towards the centre on their own.\n\n"
				+ "When everything is in place the runes light up and a circle appears on the dais. Right click the crucible: it tells you which block is missing and where.");
		topic(lang, "magnum_opus", "The Magnum Opus",
				"Every ritual of the Athanor takes up to three items on the dais and one catalyst in the centre. Right click the crucible with an item to place it, sneak right click with an empty hand takes the last one back. Then right click with an empty hand to begin.\n\n"
				+ "A ritual passes through stages, and every stage drains personal EMC from whoever started it, bit by bit. The runes show the stage:\n\n"
				+ "Nigredo, the blackening: grey runes.\n"
				+ "Cauda Pavonis, the peacock's tail: every colour, the stage of the coloured matters.\n"
				+ "Albedo, the whitening: white runes.\n"
				+ "Citrinitas, the yellowing: gold runes. Often needs the sky.\n"
				+ "Rubedo, the reddening: red runes.\n\n"
				+ "The first ritual turns three False Philosopher's Stones and a Nether Star into the Philosopher's Stone, for about 250,000 EMC. Burn items in your Transmutation Table first.\n\n"
				+ "When you go offline, or a stage is missing the sky, the ritual waits. Sneak right click stops it: the ingredients stay, the EMC that was paid is lost.\n\n"
				+ "Rebound: when the Athanor breaks during a ritual, the runes flicker red and orange. Repair it within 5 seconds. Running out of EMC in Citrinitas or Rubedo gives you 10 seconds. "
				+ "If you are too late the work hits back, harder the further it had come: in Nigredo the catalyst burns up, in Albedo an ingredient is lost, in Citrinitas lightning strikes and half is lost, in Rubedo everything explodes.\n\n"
				+ "When the last stage ends, the result floats above the crucible. Right click to take it.");
		item(lang, "transmutation_tablet",
				"A Transmutation Table to carry around. Right click it, or press K when it is in a Curios slot.\n\n"
				+ "Recipe: Transmutation Table surrounded by stone and Dark Matter blocks.");
		item(lang, "tome",
				"Burning the Tome of Knowledge in a Transmutation Table teaches every item there is.\n\n"
				+ "It can only be crafted when the server enables craftableTome in the common config. It then takes a Klein Star Omega, a book and the three covalence dusts.");
		topic(lang, "covalence_dust", "Covalence Dust",
				"Dust used in many recipes, and to repair tools: put a damaged tool and its dust in a crafting grid.\n\n"
				+ "Low: charcoal and 8 cobblestone.\n"
				+ "Medium: iron and redstone.\n"
				+ "High: diamond and coal.\n"
				+ "Each recipe makes 40.");
		topic(lang, "fuels", "Alchemical Fuels",
				"Fuel that burns far longer than coal, and the ingredient of matter.\n\n"
				+ "Alchemical Coal: a Philosopher's Stone (a false one works too) and 4 coal.\n"
				+ "Mobius Fuel: the stone and 4 Alchemical Coal.\n"
				+ "Aeternalis Fuel: the stone and 4 Mobius Fuel.\n\n"
				+ "Nine of each make a block. Collectors can upgrade fuel into the next kind.");
		item(lang, "dark_matter",
				"The first matter. Used for tools, armor, rings and machines.\n\n"
				+ "Recipe: a diamond block surrounded by 8 Aeternalis Fuel. Four make a Dark Matter block.");
		item(lang, "red_matter",
				"Stronger matter for the better tools and machines.\n\n"
				+ "Recipe: a Philosopher's Stone (the real one), three Dark Matter and five Aeternalis Fuel. Four make a Red Matter block.");
		topic(lang, "klein_stars", "Klein Stars",
				"Batteries for EMC. Tools and rings take the EMC they need from stars in your inventory, and a Transmutation Table can store EMC in them.\n\n"
				+ "Ein 50,000, Zwei 200,000, Drei 800,000, Vier 3.2 million, Sphere 12.8 million, Omega 51.2 million.\n\n"
				+ "Charge them in a collector or relay. The first four are made in the Athanor, and the star you upgrade keeps the EMC it holds. All of them end in Citrinitas, which needs the sky:\n\n"
				+ "Ein: a Diamond, Mobius Fuel and High Covalence Dust, with an Ender Pearl in the centre.\n"
				+ "Zwei: a Ghast Tear, a Blaze Rod and Aeternalis Fuel, with a Klein Star Ein in the centre.\n"
				+ "Drei: Dragon's Breath, a Shulker Shell and Dark Matter, with a Klein Star Zwei in the centre.\n"
				+ "Vier: a Heavy Core, a Wither Skeleton Skull and Red Matter, with a Klein Star Drei in the centre.\n\n"
				+ "Sphere and Omega still take four of the previous star in a crafting grid. Stars made in the Athanor can not be learned in a Transmutation Table.");

		//Machines
		item(lang, "alchemical_chest",
				"A chest with 104 slots. Repair Talismans, Gems of Eternal Density, Black Hole Bands and Void Rings keep working inside it.\n\n"
				+ "Recipe: a chest with the three covalence dusts, stone, a diamond and iron.");
		item(lang, "alchemical_barrel",
				"The Alchemical Chest as a barrel: 104 slots, and it can face any direction.");
		topic(lang, "alchemical_bags", "Alchemical Bags",
				"A bag of 104 slots you can open anywhere. Every bag of the same colour opens the same inventory, so two red bags hold the same items.\n\n"
				+ "Dye a bag to change its colour. Recipe: an Alchemical Chest with High Covalence Dust and wool.");
		item(lang, "condenser_mk1",
				"Put an item in the slot at the top: the condenser turns everything else in it into EMC and creates copies of that item.\n\n"
				+ "It also takes EMC from collectors and relays next to it, so with enough power it needs no items at all.\n\n"
				+ "Recipe: an Alchemical Chest with diamonds and obsidian.");
		item(lang, "condenser_mk2",
				"A faster condenser with separate input and output slots. It burns a whole stack at a time, so it works well with pipes and hoppers.\n\n"
				+ "Recipe: an Energy Condenser with Dark and Red Matter blocks.");
		topic(lang, "collectors", "Energy Collectors",
				"Collectors turn light into EMC: 4, 12 and 40 EMC per second for MK1 to MK3, in full light. Place them in the sun.\n\n"
				+ "They pass their EMC to condensers, relays and other collectors next to them. Fuel in the input slots is upgraded into the next fuel, a Klein Star in the slot is charged.\n\n"
				+ "MK1: glowstone blocks, glass, a diamond block and a furnace. MK2 and MK3 add Dark and Red Matter.");
		topic(lang, "relays", "Anti-Matter Relays",
				"Relays store EMC and pass it on: 64, 192 and 640 EMC per tick for MK1 to MK3. They store 100,000, 1 million and 10 million.\n\n"
				+ "Items in the burn slot are turned into EMC, a star in the other slot is charged. A collector next to a relay gives it a bonus.\n\n"
				+ "MK1: obsidian, glass and a diamond block. MK2 and MK3 add Dark and Red Matter.");
		item(lang, "dm_furnace",
				"Smelts twice a second, with 9 input slots. Ores have a 50% chance to smelt into two.\n\n"
				+ "It runs on fuel, on EMC from collectors and relays or on a Klein Star in the fuel slot.");
		item(lang, "rm_furnace",
				"An even faster furnace with 13 input slots. Ores always smelt into two.");
		item(lang, "dm_pedestal",
				"Place a ring, stone or amulet on it and right click it with an empty hand to activate it in a 9x9x9 area.\n\n"
				+ "Watch: speeds up blocks and slows mobs. Repair Talisman: repairs the items of players. Archangel's Smite: shoots arrows. Swiftwolf's: lightning. Ignition: burns mobs. Zero: freezes. Harvest Goddess: grows crops. Body, Soul and Life Stone: feed and heal. Evertide and Volcanite: start and stop rain. Mind Stone: gathers experience. Black Hole Band: gathers items.");
		topic(lang, "interdiction", "Interdiction Torch and Lantern",
				"Pushes hostile mobs and projectiles away within 8 blocks. The lantern does the same, and gives more light.\n\n"
				+ "Recipe: a Philosopher's Stone with diamonds, glowstone dust and redstone torches, or a soul lantern.");
		topic(lang, "nova", "Nova Catalyst and Cataclysm",
				"Explosives with a short fuse. The Catalyst is a much stronger TNT, the Cataclysm is three times stronger still.\n\n"
				+ "TNT and Mobius Fuel make two Catalysts, a Catalyst and Aeternalis Fuel make two Cataclysms.");

		//Tools and Armor
		topic(lang, "dm_tools", "Dark Matter Tools",
				"Pickaxe, axe, shovel, sword, hoe, shears and hammer. They never break.\n\n"
				+ "V charges them for more speed and a bigger area. The pickaxe has modes (G) to mine 3 blocks tall, wide or long, and right clicking an ore mines the whole vein. The hammer digs 3x3, the hoe tills an area, C with the sword hits every mob around you.");
		topic(lang, "rm_tools", "Red Matter Tools",
				"Faster versions of the Dark Matter tools, with one more charge level. Crafted from the Dark Matter tool and Red Matter.\n\n"
				+ "The sword has a mode to hit only hostile mobs or everything.");
		item(lang, "rm_katar",
				"Sword, axe, shears and hoe in one. C kills everything around you. Crafted from the four Red Matter tools.");
		item(lang, "rm_morning_star",
				"Hammer, pickaxe and shovel in one, with the modes of the pickaxe.");
		topic(lang, "tridents", "Matter Tridents",
				"Tridents with four modes: loyalty, channeling (lightning for 64 EMC a bolt), riptide and shockwave. The Dark Matter trident needs rain or water for its special modes, the Red Matter trident works anywhere.");
		topic(lang, "shields", "Matter Shields",
				"Shields that never break. They accept banner patterns like a normal shield.");
		topic(lang, "dm_armor", "Dark Matter Armor",
				"Armor that never breaks. A full set blocks 80% of the damage you take and protects you from explosions. The boots stop fall damage, the helmet lets you breathe under water.");
		topic(lang, "rm_armor", "Red Matter Armor",
				"Stronger matter armor: a full set blocks 90% of the damage. Craft it by surrounding a Dark Matter piece with Red Matter.");
		topic(lang, "gem_armor", "Gem Armor",
				"The strongest armor.\n\n"
				+ "Helmet: heals, night vision (Shift+X), walking on water. Chestplate: feeds you and protects from fire and lava. Leggings: fall fast while sneaking and push mobs away. Boots: no fall damage, higher jumps, step assist (X).\n\n"
				+ "Each piece is the Red Matter piece with a Klein Star Omega and two rings or stones. Offensive abilities need the server option offensiveAbilities.");
		item(lang, "destruction_catalyst",
				"Right click a block to destroy a 3x3 area, up to 16 blocks deep depending on the charge. Costs 8 EMC per block and collects the drops. Very hard blocks are left alone.");
		item(lang, "hyperkinetic_lens",
				"Fires an explosive charge. A higher charge gives a bigger explosion and costs more EMC.");
		item(lang, "catalytic_lens",
				"The Destruction Catalyst and Hyperkinetic Lens in one. It digs up to 64 blocks deep and can fire the lens shot with R.");
		item(lang, "mercurial_eye",
				"Builds with EMC. Press C to choose the block it places and add a Klein Star.\n\n"
				+ "Modes (G): create, extend, transmute or pillar. The charge sets the size.");
		topic(lang, "divining_rods", "Divining Rods",
				"Right click the ground to find out how much EMC lies below: the low rod looks 3 blocks deep, the medium rod 16 and the high rod 64. The better rods also show the most valuable blocks they found.");
		topic(lang, "alchemical_collection", "Alchemical Collection",
				"An enchantment for mining tools. Press C to turn it on: blocks you mine are turned into EMC and learned instead of dropping.");

		//Rings and Trinkets
		item(lang, "iron_band",
				"The base of most rings. Iron surrounding a lava bucket or a Volcanite Amulet.");
		item(lang, "swiftwolf_rending_gale",
				"Lets you fly while it is in your hotbar or a Curios slot. Right click to push nearby mobs away, R calls lightning. Uses a little EMC while active.");
		item(lang, "zero_ring",
				"G freezes the water and puts out the fire around you. Right click to freeze a bigger area, depending on the charge.");
		item(lang, "ignition_ring",
				"Makes you immune to fire. G burns nearby mobs, right click lights a block and R throws a fireball.");
		item(lang, "harvest_goddess_band",
				"G makes plants around you grow fast. Right click plants seeds from your inventory, sneak right click uses bone meal on a large area.");
		item(lang, "archangel_smite",
				"A bow that fires homing arrows. Right click fires one, left click ten. G makes it fire on its own. Every arrow costs the EMC of an arrow.");
		item(lang, "black_hole_band",
				"When turned on, pulls dropped items within 7 blocks into your inventory. Right click a fluid to pick it up.");
		item(lang, "void_ring",
				"A Black Hole Band and Gem of Eternal Density in one. C teleports you to the block you are looking at, up to 64 blocks away.");
		item(lang, "arcana_ring",
				"Four rings in one: Zero, Ignition, Harvest Goddess and Swiftwolf's. Always lets you fly. G picks the ring, R fires its projectile.");
		item(lang, "body_stone",
				"When turned on (G), feeds you half a shank per second for 64 EMC.");
		item(lang, "soul_stone",
				"When turned on (G), heals you half a heart per second for 64 EMC.");
		item(lang, "life_stone",
				"The Body Stone and Soul Stone in one.");
		item(lang, "mind_stone",
				"When turned on, stores your experience. Turn it off and right click to get 50 points back per click.");
		item(lang, "watch_of_flowing_time",
				"Right click to make time in the world run faster or backwards. G speeds up machines and plants and slows down mobs around you. Costs EMC while active.");
		item(lang, "gem_of_eternal_density",
				"Turns cheap items in your inventory into iron, gold, diamonds, Dark Matter or Red Matter. Sneak right click turns it on, G picks the target, right click sets which items it may use.");
		item(lang, "evertide_amulet",
				"Endless water: right click to place water or fill a tank or cauldron. Lets you walk on water, R throws a water ball. It stays in the grid when used in crafting.");
		item(lang, "volcanite_amulet",
				"Endless lava for 32 EMC each. Lets you walk on lava, makes you immune to fire, R throws a lava ball.");
		item(lang, "repair_talisman",
				"Repairs every damaged item in your inventory by one point per second. It also works in an Alchemical Chest, a bag or on a pedestal.");

		//Beyond Red Matter
		topic(lang, "matter_tiers", "Matter Tiers",
				"After Red Matter come Magenta, Pink, Purple, Violet, Blue, Cyan, Green, Lime, Yellow, Orange, White and Fading Matter. The Final Star Shard is the last tier.\n\n"
				+ "Between the black of Nigredo and the white of Albedo the alchemists saw the peacock's tail, every colour at once. The coloured matters are that stage, the Cauda Pavonis.\n\n"
				+ "Magenta to Orange Matter are made in the Athanor: three of the matter before it on the dais, and a catalyst in the centre that you have to go out and find:\n\n"
				+ "Magenta: Chorus Fruit. Pink: Pink Petals. Purple: a Shulker Shell. Violet: an Amethyst Cluster. Blue: a Heart of the Sea. "
				+ "Cyan: an Echo Shard. Green: a Turtle Scute. Lime: a Sculk Catalyst. Yellow: a Breeze Rod. Orange: Magma Cream.\n\n"
				+ "Each ritual costs as much EMC as six fuels of the tier before it. White and Fading Matter are still made from the matter and fuel of the tier before it: three matter between six fuel. Four matter make a block.\n\n"
				+ "Matter made in the Athanor can not be learned in a Transmutation Table, and neither can its blocks.");
		topic(lang, "tier_fuels", "Coloured Fuels",
				"Every new tier has a fuel, from Magenta to White. A Philosopher's Stone and four of the previous fuel make one, Magenta Fuel starts from Aeternalis Fuel. Nine make a block.");
		topic(lang, "tier_collectors", "Collectors",
				"Collectors in sixteen tiers, from Basic to Final. Every tier makes six times as much EMC as the one before: Basic 4 EMC per second, Red 144, Violet about 187,000, Final almost 2 trillion. They store a minute of their output.\n\n"
				+ "A Compact Sun on top multiplies their output. Basic: glowstone blocks, glass, an Aeternalis Fuel block and a furnace. Each next tier: the previous collector and that tier's matter. The old MK1 to MK3 collectors convert into Basic, Dark and Red.");
		topic(lang, "compressed_collectors", "Compressed Collectors",
				"Nine collectors of a tier compressed into one. Only used as an ingredient, for example for Power Flowers.");
		topic(lang, "tier_relays", "Relays",
				"Relays in sixteen tiers. They move six times as much EMC per tier, and the Final relay has no limit. Next to a collector they get a bonus, shown in their tooltip.\n\n"
				+ "Basic: obsidian, glass and an Aeternalis Fuel block. Each next tier: the previous relay and that tier's matter.");
		topic(lang, "power_flowers", "Power Flowers",
				"A Power Flower belongs to the player who placed it, and adds EMC straight to their personal EMC every second, even while they are offline. Basic gives 102 EMC per second, every tier six times more.\n\n"
				+ "A Compact Sun below it multiplies the output. Recipe: two compressed collectors, an EMC Link and relays of the same tier.");
		topic(lang, "emc_links", "EMC Links",
				"Connect your personal EMC to pipes. Items and EMC piped into the link go into your EMC, and the items are learned.\n\n"
				+ "Right click with an item to set what it hands out, then pipes can pull it from the link. Right click with an empty hand to buy a stack yourself, with an empty bucket to buy fluid. Sneak right click with an empty hand to clear it. Each tier moves more per second.");
		item(lang, "compact_sun",
				"A sun in a block. Placed on top of a collector or under a Power Flower it multiplies the output by 10.\n\n"
				+ "Recipe: Final Stars, Final Star Shards and Yellow Matter.");
		item(lang, "matter_upgrader",
				"Right click a collector, relay, Power Flower or EMC Link you placed to upgrade it one tier, without breaking it. You pay the difference in EMC and must have learned the next tier.");
		item(lang, "condenser_mk3",
				"Five condensers in one block. Every side except the bottom has its own input and target, the bottom is the shared output. Left click it with a Philosopher's Stone to rotate it.\n\n"
				+ "Recipe: four Energy Condensers MK2 and Magenta Matter blocks.");
		item(lang, "transmutation_interface",
				"Gives pipes and storage systems such as AE2 access to your knowledge. The first slot takes items in, every learned item can be pulled out with your EMC. You have to be online.");
		topic(lang, "advanced_alchemical_chests", "Advanced Alchemical Chests",
				"A chest that opens your Alchemical Bag of the same colour, so pipes can reach your bag. Sneak right click it with a bag to set the colour. You have to be online.");
		topic(lang, "tier_table", "Tier Numbers",
				"Collector EMC per second:\n"
				+ "Basic 4, Dark 24, Red 144\n"
				+ "Magenta 864, Pink 5.2K\n"
				+ "Purple 31K, Violet 187K\n"
				+ "Blue 1.1M, Cyan 6.7M\n"
				+ "Green 40M, Lime 242M\n"
				+ "Yellow 1.5B, Orange 8.7B\n"
				+ "White 52B, Fading 313B\n"
				+ "Final 1.9T\n\n"
				+ "Relays move 16 times and Power Flowers about 25 times the collector amount of their tier.");

		//Alchemical Artifacts
		topic(lang, "big_stars", "Magnum, Colossal and Gargantuan Stars",
				"Klein Stars that keep growing. Four Klein Star Omegas make a Magnum Star Ein of 204.8 million EMC, and every star holds four times more than the one before.\n\n"
				+ "After the Magnum Omega come the Colossal stars, then the Gargantuan stars. The Gargantuan Omega holds about 3.5 quintillion EMC.");
		item(lang, "final_star_shard",
				"The last tier of matter, and an ingredient of the best items. Eight Colossal Star Omegas around a nether star.");
		item(lang, "final_star",
				"Eight Final Power Flowers around a dragon egg. For now its only use is the Compact Sun, and showing off.");
		topic(lang, "alchemical_books", "Alchemical Books",
				"Books that remember places and teleport you to them. Teleporting costs EMC for every block of distance: 1000 for the Basic book, 500 for Advanced, 100 for Master, nothing for Arcane.\n\n"
				+ "From the Advanced book on, sneak right click binds the book to you so every copy shares your places. Master and Arcane books also teleport between dimensions.");
		item(lang, "arcane_transmutation_tablet",
				"A Transmutation Tablet with a crafting grid. It can fill the grid from your knowledge and has buttons to lock, burn, unlearn, rotate and spread items.");
		item(lang, "knowledge_sharing_book",
				"Sneak right click to write your knowledge into the book. Another player who uses it learns every item you know.");
		item(lang, "infinite_fuel",
				"Fuel that never runs out: each time it lights a furnace it costs its owner 128 EMC instead. Made in the Athanor from an Aeternalis Fuel block, a Blaze Rod and a Magma Block, with a Klein Star Ein in the centre.");
		item(lang, "infinite_steak",
				"Food that never runs out. Every bite costs 64 EMC. Made in the Athanor from an Enchanted Golden Apple, a Cake and Cooked Beef, with a Klein Star Ein in the centre.");

		//Commands and Settings
		topic(lang, "commands", "Commands",
				"/projecte setemc, removeemc and resetemc change the EMC value of an item.\n"
				+ "/projecte emc and /projecte knowledge change the EMC and knowledge of a player.\n"
				+ "/projecte showbag opens the bag of a player.\n"
				+ "/px emc, /px knowledge and /px book do the same for the expansion items.\n"
				+ "/px dumpfuelmap shows the fuel order.\n\n"
				+ "Most commands are for operators.");
		topic(lang, "emc_values", "Changing EMC Values",
				"EMC values come from datapacks (pe_custom_conversions), from custom_emc.json in the config folder and from the setemc command. After a change the values are calculated again on the next reload.");
		topic(lang, "integrations", "Other Mods",
				"JEI shows world transmutation and the Philosopher's Stone recipes. Jade, The One Probe and WTHIT show EMC values. Curios adds slots for rings, amulets, stars and tablets.\n\n"
				+ "Items from Mekanism, AE2, Ender IO, Mystical Agriculture, Occultism and Farming for Blockheads get EMC values from their recipes.");
		topic(lang, "configs", "Settings",
				"The files in config/ProjectE change prices, ranges and recipes. The server file holds most of them, for example craftableTome, offensiveAbilities and the multipliers of collectors, relays and Power Flowers.");
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
