# ProjectE Phoenix: Beta 2 design

What is in the game after alpha-1, and what beta 2 (and later) adds on top of it.

## What alpha-1 already has

- **False Philosopher's Stone**: the old philo stone recipe. It only converts fuels up to Aeternalis and makes a Transmutation Table.
- **Alchemical Crucible**: 5x5 base, 8 dais, 4 pillars, arches and apex (`CrucibleStructure`). It runs one hardcoded ritual
  (`AlchemicalCrucibleBlockEntity`): 3 False Stones + Nether Star (configurable) go through Nigredo, Albedo, Citrinitas and Rubedo
  and become the Philosopher's Stone. The owner pays EMC during every stage. Citrinitas needs open sky, and a stalled Rubedo resets.
- **Red Matter** needs the real stone.
- **Alchemist's Codex**: one-page book with text links (`GUIAlchemistsCodex`).

Beta 2 builds on all of this. Nothing gets thrown away.

## Design rules

1. **Equivalent exchange.** Everything strong has a price: EMC, time, souls, hearts or a boss fight. Nothing comes from nothing.
2. **Dark, not edgy.** The strongest shortcuts cost something the player feels: villager souls, max hearts. There is always a slower, clean way too.
3. **The Athanor is the spine.** Every progression step (stones, stars, matters, endgame) goes through it.
4. **Never a chore.** Cheap and common items work as they always did. The friction is for the big moments.

---

## 1. The Athanor (rename of the Alchemical Crucible)

Rename the multiblock to the **Athanor**. The crucible block in the middle can keep the name *Alchemical Crucible*, and
the structure is the Athanor. Block ids can stay (`alchemical_crucible`, `crucible_*`), so worlds are not broken. Only the lang names change.

> **Decision needed:** the brief for the 3D artist describes a 3x3x3 structure, but the code is a 5x5 base with pillars
> and arches up to 5 high. Pick one before the artist starts. I'd keep the 5x5 Athanor and let the artist redesign the
> blocks it already uses (plinth, step, dais, pillar, arch, apex, crucible). Those are 7 models, plus a few extra for tier II.

### 1.1 Data-driven rituals

Replace the hardcoded ritual with a recipe type `projecte:athanor`:

```json
{
  "type": "projecte:athanor",
  "tier": 1,
  "catalyst": { "item": "minecraft:nether_star" },
  "inputs": [
    { "item": "projecte:false_philosophers_stone" },
    { "item": "projecte:false_philosophers_stone" },
    { "item": "projecte:false_philosophers_stone" }
  ],
  "souls": 0,
  "stages": [
    { "stage": "nigredo",    "rune": "black",  "ticks": 600, "emc": 50000 },
    { "stage": "albedo",     "rune": "white",  "ticks": 600, "emc": 100000 },
    { "stage": "citrinitas", "rune": "yellow", "ticks": 600, "emc": 200000, "needs": "sky" },
    { "stage": "rubedo",     "rune": "red",    "ticks": 900, "emc": 400000, "rebound": "explosion" }
  ],
  "result": { "id": "projecte:philosophers_stone" }
}
```

- `inputs` are 3 items (tier I) or 5 (tier II). The order doesn't matter.
- `stages` is a list, so a recipe can use any sequence of stages and rune colours, not only the four of the Magnum Opus.
  Extra stage names for later recipes: `cauda_pavonis` (rainbow cycle), `calcination`, `coagulation`.
- `needs`: `sky`, `night`, `day`, `thunder`, `dimension:<id>`, `pulse:<min>` (see Dragon's Pulse).
- The existing config values become the defaults of the philosopher's stone recipe, which ships as a datapack JSON.
- JEI/EMI category: inputs around the crucible, catalyst in the middle, stages as coloured bars with their EMC cost.

### 1.2 Rune colours

The parts already have an `IntegerProperty GLOW`. Add a second property, `RUNE` (0–15), as an index into a fixed palette:

| idx | name | use |
|---|---|---|
| 0 | off | idle, unformed |
| 1 | cyan | formed, ready |
| 2 | black/violet | Nigredo |
| 3 | white | Albedo |
| 4 | yellow | Citrinitas |
| 5 | red | Rubedo |
| 6 | green | soul stages |
| 7 | orange | catalyst consumed |
| 8 | flicker | rebound warning (renderer alternates red/orange) |
| 9–15 | magenta … white | Cauda Pavonis, one per colour matter |

The models get a separate **emissive overlay** with `tintindex: 0`. A `BlockColor` handler maps `RUNE` to RGB, so one
greyscale texture covers every colour. That's why the artist must deliver the glowing parts as a separate greyscale layer.
`CrucibleStructure.update()` already pushes the glow to every part, so it can push `RUNE` the same way.

### 1.3 EMC cost: what generation is for (answers point 2)

The Athanor has **no heat or fuel input**; heat belongs to the Alchemical Cauldron (2.2). Every stage costs the owner personal EMC, as the ritual already does in alpha-1.

- Each stage of a recipe has its own `emc` cost, paid bit by bit while it runs. If the owner runs out, the ritual stalls (and in Citrinitas or Rubedo it rebounds, see 1.5).
- The costs climb steeply per tier: the philosopher's stone costs about 750k EMC in total, and each colour matter costs more than the one before it.
- The catalyst is what makes each tier different: a trip to the End, the deep dark, a trial chamber and so on (see 3.3).

Generation stays useful. Collectors, power flowers and fuels make the EMC that pays for rituals, and each ritual tier
unlocks the next collectors and flowers. So generation feeds progression instead of only inflating EMC.

### 1.4 Tier II: the Grand Athanor (5 inputs)

Upgrade in place. Nothing gets torn down:

- Replace the apex with the **Quintessence Apex**. It's the fifth element, so the structure takes 5 inputs.
- Replace the 4 upper arches with **Gilded Arches**.
- The dais work as before, but 5 of the 8 now take items. They form a pentagram: north, plus the four diagonals (or let the artist pick 5).
- Lore: tier I is the triangle of the **Tria Prima** (salt, sulphur, mercury). Tier II is the pentagram of the
  **four elements plus quintessence**. The chalk circle on the base changes from a triangle to a pentagram.

`CrucibleStructure` gets a `tier` result: it checks the tier I parts and then whether the tier II parts are there.

### 1.5 Rebound

A ritual rebounds when something goes wrong **during** a stage. It never happens by pure chance, so the player can always prevent it:

| Cause | Warning |
|---|---|
| A part of the structure is broken | runes flicker for 5 s, put it back in time to save it |
| Owner's EMC runs out | 10 s warning, then a rebound (in Citrinitas or Rubedo; before that it stalls like now) |
| Inputs taken or swapped | immediate |
| Human transmutation | always (see 7) |

How bad the rebound is depends on the stage it happens in:

| Stage | Rebound |
|---|---|
| Nigredo | inputs turn to **Prima Materia** (some EMC comes back), smoke |
| Albedo | one input is lost, Nausea and Weakness |
| Citrinitas | lightning strikes the crucible, everyone within 5 blocks takes damage, half the inputs are lost |
| Rubedo | explosion (no block damage by default, configurable), everything lost, Wither II for the owner |

The recipe JSON can override this with `"rebound": "..."`. The existing `rubedoResetsWhenStalled` config becomes part of this.

---

## 2. Early game: chalk, circles and the cauldron

### 2.1 Tria Prima from the covalence dusts

The three dusts are already in the game. They become the three principles:

| Dust | Refined into | Chalk | Circle |
|---|---|---|---|
| Low Covalence Dust | **Alchemical Salt** (body) | White chalk | small, radius 1.5 |
| Medium Covalence Dust | **Alchemical Sulphur** (soul) | Yellow chalk | medium, radius 2.5 |
| High Covalence Dust | **Quicksilver** (spirit) | Silver chalk | large, radius 3.5 |

- The dusts are refined in the cauldron (2.2): 8 dust + water → 1 principle. Salt needs low heat, Sulphur and Quicksilver need high heat.
- **Chalk** = principle + calcite + bone meal → 4 chalk. Chalk has 64 uses (durability).
- The principles **replace diamond blocks** in the Condenser, Collector MK1 and Relay MK1 recipes, and in the Klein Star Ein ritual.
  This gives the early game its own materials instead of "just more diamonds".

### 2.2 The Alchemical Cauldron and circles

- **Alchemical Cauldron** = cauldron + iron + low covalence dust. It has a block entity and holds water, Red Water or lava.
- Right-click the floor next to it with chalk to draw that chalk's circle, centred on the cauldron:
  the small circle with white chalk, the medium with yellow and the large with silver.
  The circles are stored in the cauldron's block entity, and a `BlockEntityRenderer` draws them as flat textured rings
  on the floor. No extra blocks are needed, but the floor under a ring has to be solid and free of other blocks.
- **Effects:** each ring rotates slowly (inner clockwise, middle counter-clockwise, outer clockwise), lights up when
  in use and sends particle lines to the cauldron. Rain smudges the chalk, so a circle without a roof fades after a few days.
- Recipes are data-driven (`projecte:cauldron`): the liquid, the circles needed, the heat, items thrown in, souls, and the result.

**Heat** comes from the vanilla block directly under the cauldron, so no new block is needed. The cauldron reads it from a
block tag per level (`projecte:cauldron_heat/low`, `…/high`, `…/soul`), so packs can add their own heat sources:

| Heat | Blocks under the cauldron | Look |
|---|---|---|
| Cold | anything else, ice/packed ice/blue ice | no bubbles, frost particles on ice |
| Low | lit campfire, magma block | slow bubbles |
| High | fire, lava | fast bubbles, steam |
| Soul | soul fire, lit soul campfire | blue-green bubbles, soul particles |

- A recipe lists the heat it needs (`"heat": "high"`). The wrong heat simply does nothing, so no rebound in the cauldron.
- Recipes take time at the right heat (for example 10 s for low, 5 s for high) instead of being instant, with bubbles and a sound while it brews.
- **Soul heat** is the dark one: soul recipes (Red Water, False Stone, Soul Vial filling, Chimera) need it. That ties souls to the soul sand/soil you already have to collect in the Nether.

### 2.3 Souls (the dark currency)

Any mob that **dies inside an active outer circle** gives its soul to the cauldron:

| Mob | Souls | Price |
|---|---|---|
| Passive animals | 1 | none |
| Hostile mobs | 2 | none |
| Villagers, wandering traders, illagers | **10** | +1 **Sin** |
| Bosses | 50 | none |

- **Sin** is an attachment on the player. Sin raises villager prices, villagers flee from you,
  and iron golems turn hostile at high Sin. It slowly goes down over time (about 1 point per in-game day). Sin also raises
  the chance and strength of the rebound warning. This is the "morally grey" choice: villagers are 10x faster but cost you.
- **Soul Vial** (glass bottle + soul sand): takes 10 souls out of a cauldron to carry to the Athanor
  (recipes with `"souls": n`).
- Lore line: *"Equivalent exchange has no clause for mercy."*

### 2.4 Red Water and the False Philosopher's Stone (answers "do this for the fake stone")

1. **Red Water**: water cauldron on **soul heat**, small and medium circle, throw in redstone, nether wart and glowstone dust, 4 souls → the cauldron turns to Red Water.
2. **False Philosopher's Stone**: Red Water cauldron on **high heat** (lava), all three circles, throw in a diamond, 16 souls → the Red Water crystallises around the
   diamond into a False Stone. This **replaces the crafting recipe**.
3. The False Stone gets **durability** (for example 256 uses). It cracks visibly (model overrides like the existing
   `_nigredo/_albedo…` models) and shatters when empty. Throw it in Red Water with souls to repair it.

So: the stone is made from souls, it's unstable, and three of them become the real one in the Athanor. That's the Red Water story from FMA, told in Minecraft terms.

---

## 3. Athanor recipes

### 3.1 Tier I (3 inputs)

| Result | Catalyst | Inputs | Notes |
|---|---|---|---|
| Philosopher's Stone | Nether Star | 3 False Philosopher's Stones | exists, becomes JSON |
| Klein Star Ein | Ender Pearl | Diamond, Mobius Fuel, Quicksilver | |
| Klein Star Zwei | Klein Star Ein | Ghast Tear, Blaze Rod, Aeternalis Fuel | |
| Klein Star Drei | Klein Star Zwei | **Dragon's Breath**, Shulker Shell, Dark Matter | |
| Klein Star Vier | Klein Star Drei | Heavy Core, Wither Skeleton Skull, Red Matter | |
| Infinite Fuel | Klein Star Ein (charged) | Aeternalis Block, Blaze Rod, Magma Block | EMC cost per stage |
| Infinite Steak | Klein Star Ein (charged) | Enchanted Golden Apple, Cake, 16 passive souls | |
| Prima Materia (x8) | Salt | Sulphur, Quicksilver, any 1 block | base for later recipes |
| Magenta → Orange matter | see 3.3 | | |

Upgrading a star keeps its stored EMC.

### 3.2 Tier II (5 inputs)

| Result | Catalyst | Inputs |
|---|---|---|
| Klein Star Sphere | Klein Star Vier | Warden's Heart, Nether Star, Echo Shard, Breeze Rod, Quicksilver |
| Klein Star Omega | Klein Star Sphere | Dragon Heart, Warden's Heart, Elder Eye, Blighted Soul, Creaking Heart |
| Colossal stars | previous star | boss drops + colour matter |
| Final Star Shard | Colossal Omega | Nether Star, 4 boss drops |
| Final Star | Final Star Shard | 4 Final Power Flowers, Dragon Heart |
| White Matter | Yellow/Orange matter | see 3.3 |
| Phoenix Plume | see 8 | |

### 3.3 The colour matters: Cauda Pavonis (answers point 2)

Lore: between black (Nigredo) and white (Albedo), the alchemists saw the **peacock's tail**, every colour at once.
The Project Expansion matters are that stage.

Every colour matter becomes an Athanor ritual with its own catalyst. The inputs are 3 of the previous matter:

| Matter | Catalyst | Where you go |
|---|---|---|
| Magenta | Chorus Fruit | End |
| Pink | Cherry Blossom (Pink Petals) | Cherry grove |
| Purple | Shulker Shell | End city |
| Violet | Amethyst Cluster | Geode |
| Blue | Heart of the Sea | Shipwreck/treasure |
| Cyan | Echo Shard | Ancient city |
| Green | Turtle Scute | Beach |
| Lime | Sculk Catalyst | Deep dark |
| Yellow | Breeze Rod | Trial chamber |
| Orange | Magma Cream | Nether |
| White | Nether Star | Wither |

The ritual's middle stage is `cauda_pavonis` with that colour on the runes.

**Important:** ritual items get a tag `projecte:ritual_only`. They still have EMC (you can burn them), but they can't
be **learned** in the transmutation table, so you can't buy your way past the Athanor. This tag is also how the existing
datapack blacklist from ProjectExtended can block them.

**Colour fuels:** each colour ritual also gives 8 **Iridescent Essence** of that colour. Fuels keep their crafting
recipes, but each colour fuel needs 1 essence per 8 fuel. Mass production stays, but each fuel tier is gated by its matter.

---

## 4. Boss drops

Added with NeoForge **global loot modifiers**: datapack-driven, so packs can change them.

| Boss | New drop | Used for |
|---|---|---|
| Warden | **Warden's Heart** (always 1) | Sphere, Omega, Phoenix |
| Ender Dragon | **Dragon Heart** (every kill, including respawned dragons) | Omega, Final Star, Phoenix |
| Wither | **Blighted Soul** | Omega, human transmutation |
| Elder Guardian | **Elder Eye** | Omega |
| Evoker | (vanilla Totem) | Phoenix |
| Creaking Heart (vanilla block, silk touch) | none, use it as-is | Omega |
| Ominous vault | (vanilla Heavy Core) | Vier |

Every heart model can pulse with the existing `GLOW` idea. Tooltip lore, for example:
*Warden's Heart: "It still listens."*

---

## 5. Comprehension (learning that isn't a chore)

The FMA steps are comprehension, deconstruction and reconstruction. Make it a mechanic only where it matters:

| EMC value | How you learn it |
|---|---|
| ≤ 1,024 (config) | instant on the first burn, like now |
| 1,024 – 131,072 | **Comprehension bar**: burn it 4 times (config) in total; the table shows `Comprehension 2/4` on the item |
| > 131,072 or `projecte:ritual_only` | can't be bought, only made |

- **Monocle of Comprehension** (curio/helmet slot): look at a block in the world for 3 seconds to get a full point of
  comprehension for it. This rewards exploration (amethyst in a geode, sculk in the deep dark), and nobody has to grind burns.
- The Codex shows a "Comprehended" check on every entry you know.
- Everything is config driven. Set the thresholds to 0 for classic ProjectE.

---

## 6. Dragon's Pulse (Alkahestry)

**Name:** FMA calls this "alkahestry". Use **Dragon's Pulse** in the mod and keep *Alkahest* for the solvent item (6.3).

### 6.1 Pulse strength

Every chunk gets a **pulse strength** of 0–15, calculated from the world seed and chunk position (like slime chunks). It
costs no storage and is the same for every player. River and mountain biomes get a bonus.

- The **Divining Rods** (already in the mod) also show the pulse strength of the chunk.
- An Athanor in a strong-pulse chunk (≥10) costs 25% less EMC per stage. Recipes can need `pulse:12`.

### 6.2 Pulse arrays

**Pulse Stakes** (5 of them, crafted from Quicksilver + iron) are placed in a pentagram up to 16 blocks apart. A
complete array makes a field powered by the chunk's pulse, **not EMC**. Its effect comes from the item in the centre stake:

| Centre item | Effect |
|---|---|
| Harvest Goddess Band | crop growth (like the band, but in the field) |
| Interdiction Torch | mob repel |
| Soul Stone | regen for players in the field |
| Alchemical Salt | stops rain smudging chalk inside the field |

This makes existing items into base defences without new EMC costs, and gives a reason to build in strong-pulse chunks.

### 6.3 Alkahest

Universal solvent, made in a **cold** cauldron on blue ice (Red Water + Quicksilver + 4 souls). Throw it at a block or use it on a block:
the block dissolves and you get its EMC directly, without a table (75% of the value). Good for removing big
builds, and it fits "deconstruction" from the three steps.

---

## 7. Human transmutation and the Gate

- **Tier II ritual** with 5 inputs: Bone Block, Coal Block, Rotten Flesh, Alchemical Salt, Redstone Block. The catalyst is a
  **Soul Vial of villager souls**. The Codex hints at it but never gives the full recipe; it can be found on a torn page (9.3).
- It **always rebounds**: the screen goes white, a sound plays, and the player sees the Gate (a client-side overlay, or
  later a small dimension).
- **Toll**: −2 max hearts (permanent attribute modifier, configurable).
- **What you get**: advancement *The Toll*, and the **Clap** keybind. You can do Philosopher's Stone world
  transmutations without holding the stone, and Athanor rituals don't need chalk circles. The rebound also leaves a
  **Malformed Homunculus**, needed for the Homunculus Flask.
- The hearts can be bought back later with a Rubedo ritual that costs 500 souls (Phoenix tier).

## 8. Homunculus Flask and Chimera Array (later than beta 2)

- **Homunculus Flask**: Malformed Homunculus + glass + Red Water. Place it next to a block. The small homunculus in the
  flask collects items in an 8-block radius and feeds them into the container it faces, or keeps the Athanor's
  dais stocked. It costs 1,000 EMC per in-game day from the owner; without EMC it falls asleep. This is a custom entity, so it's the biggest piece of work.
- **Chimera Array**: a cauldron recipe on soul heat with two Soul Vials of different mobs plus an item, which gives a curio charm:
  Spider + Bat → wall climbing; Squid + Turtle → water breathing; Cat + Creeper → creepers flee; Phantom + Bat → night vision.
  It's simple to make (curio + effect) and fun to collect.

## 9. The Phoenix: the capstone (the mod's name)

In alchemy, the **phoenix** is the symbol of the completed Rubedo: rebirth from ashes. The mod itself is ProjectE reborn,
so the Phoenix is the final goal.

The Phoenix is a **power the player takes on**, not a creature or a pet.

- **The Phoenix Rite**: tier II ritual, catalyst Final Star (not consumed). Inputs: Dragon Heart, Warden's Heart, Blighted Soul,
  Totem of Undying, Red Matter Block. The stages end with a `rebirth` stage that cycles every rune colour. It costs the most EMC in the mod.
- The result is **Phoenix Ash**. Using it (hold right-click, like eating) burns the player alive for a moment, then they rise
  as a Phoenix. Because the ritual gives an item, a server can still trade it or give it to someone else.
- The power is a **player attachment** (`PEAttachmentTypes`), synced to the client and kept through death. It's permanent.

| Power | How it works | Implementation |
|---|---|---|
| **Rebirth** | Lethal damage burns you to ash; you rise at full health with Fire Resistance and Regeneration. 30 min cooldown (config), shown as an ember icon on the HUD; spending EMC shortens it | cancel `LivingDeathEvent`, like the Totem of Undying |
| **Ember Wings** | Flight, drains a little EMC per second; flame wings and an ember trail | the flight handling the Swiftwolf ring and Gem armor already use, plus a render layer for the wings |
| **Child of Fire** | Immune to fire and lava; standing in fire heals you | damage and tick events |
| **Restoration** | Gives back the hearts lost to the Gate (7) | removes the attribute modifier |

Advancement *Rebirth* is the last one in the tree.

### 9.1 Advancement tree "Magnum Opus"

Root: *Equivalent Exchange* (pick up a covalence dust). Branches: **Nigredo** (souls, Red Water, False Stone),
**Albedo** (the Athanor, the true stone), **Cauda Pavonis** (one per colour matter), **Citrinitas** (Klein stars,
boss hearts), **Rubedo** (Gate, Phoenix). Icons: Ouroboros for the root, Flamel cross for the Philosopher's Stone, a
phoenix for the last one.

### 9.2 Name and ids

`displayName` is already "ProjectE Phoenix". Keep the `projecte` namespace and mod id. Changing them breaks every world
and every pack and addon that knows ProjectE. Only display names, the Codex and the jar name use "Phoenix".

### 9.3 Lore delivery

- **Tooltip flavour**: one grey italic line per item.
- **Torn Codex Pages**: loot in ancient cities, trial chambers, strongholds and end cities. Each unlocks a lore page in
  the Codex: the journal of the last keeper of an Athanor, who tried human transmutation. Write all text yourself (no FMA
  names or quotes).

---

## 9.5 Armor

Now there are Dark Matter, Red Matter, Gem and Full Star Gem armor (from Project Expansion). Two additions:

- **Alchemist's Garb** (early, made in the cauldron from leather armor + Salt/Sulphur on soul heat). Almost no protection, but
  +1 soul per kill, chalk lasts twice as long, Sin builds up slower, and immune to soul fire. It fills the gap before Dark Matter
  and fits the dark side.
- **Ouroboros Regalia** (endgame, Grand Athanor). Each piece is a Full Star Gem piece + one boss heart, and the heart gives that piece its power:

  | Piece | Heart | Power |
  |---|---|---|
  | Helmet | Elder Eye | immune to Darkness and Mining Fatigue, see mobs through walls |
  | Chestplate | Warden's Heart | sonic boom on a keybind (costs EMC) |
  | Leggings | Blighted Soul | immune to Wither, attacks heal you a little |
  | Boots | Dragon Heart | dash in the look direction, no fall damage |

  Full set: rituals can't rebound on the wearer. It doesn't give flight or rebirth; that stays with the Phoenix.

## 10. The Codex as a real guidebook

What it has now: a vanilla-sized book, one page at a time, text links, a Contents button.

What it should become (like the Ars Nouveau "Worn Notebook" or Patchouli):

1. **Two-page spread** with a custom book texture.
2. **Home page**: a grid of chapter **icons** (Basics, The Athanor, Cauldron & Circles, Souls, Matter, Stars, Machines, Tools &
   Armor, Rings & Amulets, Boss Hearts, Lore). Hover shows the name, click opens it.
3. **Chapter page**: a list of entries, each with the item icon. Locked entries (not yet found or comprehended) show a
   greyed-out "?" so players can see there's more.
4. **Entry pages** in types:
   - `text`: like now
   - `crafting`: a rendered 3x3 grid with the result, items cycle through tags
   - `athanor`: crucible with 3 or 5 inputs, catalyst, EMC cost and coloured stage bars
   - `cauldron`: liquid, heat, circles needed, inputs, souls
   - `multiblock`: a **3D rotating preview** of the Athanor, built layer by layer with a "layer" slider, plus a
     button "show in world" that projects ghost blocks around a placed crucible
   - `image`: a picture (for example a screenshot of a finished circle)
   - `spotlight`: one large item with its EMC value
5. **Navigation**: Home button (book icon, top left), Back button (returns to the previous page, keeps history),
   bookmarks (3 ribbons on the side), and a search field.
6. **Recipes come from the game**: since 1.21.2 the client doesn't get every recipe, so the server sends the
   recipes the Codex needs (a small payload on login and `/reload`). Then datapack changes show up in the book automatically.
7. **Click an item** on any page to jump to its entry; with JEI/EMI installed, `R`/`U` work there too.

The existing `Codex.CHAPTERS`/`Entry` structure stays. Entries get a list of pages, and the page type decides how it's drawn.
Moving the content to JSON (`assets/projecte/codex/…`) later makes it translatable and easy for packs to extend.

---

## Beta 2 scope

**In beta 2:**

1. Athanor rename, `projecte:athanor` recipe type, the philosopher's stone as JSON
2. Rune colour property + tinted emissive overlay (needs the artist's greyscale layer; until then, tint the existing texture)
3. Rebound with warnings
4. Klein Stars Ein–Vier, Infinite Fuel/Steak, the colour matters magenta–orange as tier I rituals, `ritual_only` tag, Iridescent Essence
5. Tria Prima, chalk, Alchemical Cauldron with heat, circles, souls, Sin, Soul Vial, Red Water, new False Stone recipe and durability
6. Boss drops via loot modifiers
7. Comprehension thresholds (config, default on)
8. Codex: home grid with icons, home/back navigation, crafting/athanor/cauldron page types

**Beta 3 (with the artist's models):** Grand Athanor (tier II), Sphere/Omega/colossal/final stars, White Matter,
multiblock preview page, Dragon's Pulse and arrays, Alkahest, Monocle.

**Beta 4:** human transmutation and the Gate, Homunculus, Chimera charms, Phoenix, Ouroboros Regalia, torn pages and the lore journal.
Alchemist's Garb can come in beta 2 with the cauldron.

Beta 2 doesn't need tier II yet. Until it exists, the recipes that will need it (Sphere and up) keep their current crafting recipes.
