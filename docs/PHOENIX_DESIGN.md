# ProjectE Phoenix: design

What the alchemy of ProjectE Phoenix is, as built. The Alchemist's Codex in game covers the same ground for players; this file
is for whoever works on the mod. Numbers are the defaults.

## Rules

1. **Equivalent exchange.** Everything strong has a price: EMC, time, souls, hearts or a boss fight.
2. **Dark, not edgy.** The strongest shortcuts cost something the player feels. There is always a slower, clean way too.
3. **The Athanor is the spine.** Stones, stars, matters and the endgame go through it.
4. **Never a chore.** Cheap and common items work as they always did.

## Progression

1. **Cauldron.** Covalence dusts are refined into Alchemical Salt, Sulphur and Quicksilver, which become chalk. Chalk draws
   circles, circles catch souls, souls brew Red Water, Red Water and a diamond become the False Philosopher's Stone.
2. **Athanor.** Three False Stones and a Nether Star become the Philosopher's Stone. Klein Stars, the coloured matters and the
   infinite items follow.
3. **Grand Athanor.** Five ingredients: the last Klein Stars, White Matter, the Final Stars, the Ouroboros Regalia, the Phoenix.
4. **The Great Work.** The forbidden ritual, the homunculus, chimeras, the Phoenix.

## Alchemical Cauldron

`AlchemicalCauldronBlockEntity`, recipes of type `projecte:cauldron` (`data/projecte/recipe/cauldron`).

- Holds water or Red Water, up to six ingredient stacks, and up to 256 souls.
- **Heat** from the block below, by block tag: `cauldron_heat/cold` (ice), `low` (lit campfire, magma), `high` (fire, lava),
  `soul` (soul fire, lit soul campfire).
- **Circles**: Salt, Sulphur and Mercury Chalk draw rings one, two and three blocks out, used on the cauldron. A ring needs open
  floor; a block on it, a hole under it or rain wipes it out. A chalk lasts four circles.
- **Souls**: anything that dies inside the outer ring: animals 1, monsters 2, villagers and traders 10, bosses 50. A villager's
  death makes villagers within 48 blocks hold it against the cauldron's owner (vanilla reputation, `MAJOR_NEGATIVE` +10).
- **Soul Vial**: takes 16 souls out, sneak to pour them back.
- A recipe names its liquid, heat, circles, souls, ingredients with counts, time, and an item result, a liquid result or both.
  It brews when everything matches; any change stops it.

| Brew | Ingredients | Liquid | Heat | Circles | Souls |
|---|---|---|---|---|---|
| Alchemical Salt | 8 Low Covalence Dust | water | low | 0 | |
| Alchemical Sulphur | 8 Medium Covalence Dust | water | high | 1 | |
| Quicksilver | 8 High Covalence Dust | water | high | 2 | |
| Red Water | 4 redstone, 2 nether wart, 4 glowstone | water | soul | 2 | 4 |
| False Philosopher's Stone | diamond | Red Water, used up | high | 3 | 16 |
| Alkahest | 2 Quicksilver, glass bottle | Red Water, used up | cold | 2 | 4 |
| Alchemist's Garb (4) | leather piece, 2 Salt, 2 Sulphur | water | soul | 1 | 2 |
| Chimera charms (4) | 2 full Soul Vials, two beast parts | Red Water | soul | 3 | |
| Homunculus Flask | Malformed Homunculus, bottle, 2 Quicksilver | Red Water, used up | soul | 3 | 8 |

The False Stone wears out after 256 crafts; `FalsePhilosophersStone.onItemCrafted` wears it during the crafting event, so the
crafting remainder stays the same item and EMC mapping keeps working.

## Athanor

`AlchemicalCrucibleBlockEntity`, `CrucibleStructure`, recipes of type `projecte:athanor` (`data/projecte/recipe/athanor`).

- 5 by 5 base, eight dais, four pillars, arches and the apex around the Alchemical Crucible.
- **Grand Athanor**: the four inner arches gilded (Gilded Athanor Arch) and a Quintessence Apex. Holds five ingredients instead
  of three. Placeholder models until the real assets arrive.
- A ritual has a tier, up to three (tier 1) or five (tier 2) inputs in any order, a catalyst, a list of stages and a result.
  Options: `keep_catalyst`, `keep_catalyst_components` (an upgraded Klein Star keeps its EMC), `catalyst_full` (a charged Klein
  Star), `forbidden`.
- **Stages**: `nigredo`, `cauda_pavonis`, `albedo`, `citrinitas`, `rubedo`, each with a rune colour, ticks, EMC paid by the owner
  bit by bit, and optionally `needs` (`sky`, `day`, `night`, `thunder`).
- **Rune colours**: the `glow` block state, sixteen colours with their own textures.
- **Rebound**: a broken Athanor warns for 5 seconds; running out of EMC in Citrinitas or Rubedo for 10. Then: Nigredo burns the
  catalyst, Albedo and Cauda Pavonis lose an input and sicken everyone near, Citrinitas strikes lightning and loses half,
  Rubedo explodes and withers the owner. Config: `rebound`, `reboundBreaksBlocks`. The full Ouroboros Regalia prevents it.
- **Dragon's Pulse** of 10 or more under the crucible: a quarter less EMC.
- Everything an Athanor makes is in `projecte:ritual_only`, which is in both blacklists: it can not be learned or condensed.
- `AthanorRecipeMapper` gives results the value of their inputs, the catalyst if used up, and the EMC of the stages.

Rituals: the Philosopher's Stone; Klein Stars Ein to Vier (tier 1) and Sphere, Omega (tier 2, boss drops); Magenta to Orange
Matter (tier 1, a catalyst from the world each) and White Matter (tier 2); Infinite Fuel and Steak (a full Klein Star Ein);
the Final Star Shard and Final Star; the four Ouroboros pieces; Phoenix Ash; the forbidden human transmutation.

## Dragon's Pulse and arrays

- `DragonsPulse.strength`: noise from the world seed per chunk, 0 to 15, +3 along rivers and under mountains. Divining rods
  show it.
- **Pulse Stakes**: five within 16 blocks form an array. The stake with a focus is the centre; its pulse sets the reach
  (4 + strength / 2) and the speed. Foci: Harvest Goddess Band (crops), Interdiction Torch (repel), Soul Stone (regeneration),
  Alchemical Salt (cauldrons nearby are safe from rain).

## Knowledge

- **Comprehension**: items worth more than 2,048 EMC must be burned four times before they are learned. Lock and input slots
  teach only what is understood. Config section `comprehension`. EMC Links and the Transmutation Interface skip it.
- **Monocle of Comprehension**: three seconds looking at a block counts as one burn.

## Boss drops

Warden's Heart, Blighted Soul (Wither) and Elder Eye come from loot modifiers in `data/projecte/loot_modifiers`; the Dragon
Heart drops on the exit portal every time the dragon dies (`BossDrops`). Fixed EMC values in `pe_custom_conversions/boss_drops.json`.

## The Great Work

- **The Gate** (`TheGate`): the forbidden ritual always ends at the Gate. The owner loses two max hearts for good (attribute
  modifier, kept through death), the crucible leaves a Malformed Homunculus, and the owner can **clap**: the extra function key
  with empty hands transmutes the block they look at. Forbidden rituals are hidden from the Codex and JEI; the torn journal
  pages give the ingredients.
- **Homunculus Flask**: gathers items within 8 blocks into the container below, for 1,000 of its owner's EMC a day, only
  while the owner is online.
- **Chimera charms**: night vision, water breathing, fire resistance or jump boost while carried.
- **Alchemist's Garb**: the full set gives an extra soul per kill in a circle, halves chalk wear and blocks soul fire.
- **Ouroboros Regalia**: Gem armor plus a boss heart each; keeps every Gem power and adds one (see `OuroborosRegalia`). The
  Gem armor checks are by class so subclasses keep their powers.
- **The Phoenix** (`Phoenix`): Phoenix Ash makes the player a Phoenix for good: rebirth instead of death every 30 minutes,
  flight for 64 EMC a second in the air, immune to fire and healed by it, and the Gate's toll lifted.
- **The keeper's journal**: eight torn pages in the loot of ancient cities, stronghold libraries, end cities and trial
  chambers, each read as a book.

## Codex

Two-page book (`GUIAlchemistsCodex`): home spread with chapter icons, entry lists, text pages, then recipe pages (crafting,
cauldron, Athanor, the Athanor's layers). Clicking an item opens its entry. Home, Back, search, three bookmark ribbons saved in
`config/ProjectE/codex_bookmarks.txt`. The server sends crafting, Athanor and cauldron recipes to clients for it.

## Not built (ideas for later)

- A rotating 3D preview of the Athanor and "show in world" ghost blocks in the Codex.
- Locked "?" entries in the Codex for things the player has not found.
- A dedicated Gate dimension instead of the effects and messages.
- Comprehension for EMC Links and the Transmutation Interface.
- Magnum, Colossal and Gargantuan stars as rituals.
- The real 3D models and textures for everything that has placeholder art.
