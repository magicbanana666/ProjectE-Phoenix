![ProjectE Phoenix](/banner.png?raw=true)

# ProjectE Phoenix

ProjectE Phoenix is an unofficial port of [ProjectE](https://github.com/sinkillerj/ProjectE) to **Minecraft 26.1.2 (NeoForge)**.
It bundles ProjectE's most popular add-ons into a single mod, so you only need one jar.

ProjectE is a rewrite of Equivalent Exchange 2. You break items you don't need down into EMC (Energy-Matter Covalence)
and use that EMC to make new items. It has transmutation tables, collectors, condensers, flying rings, dark and red
matter tools, and more.

> **This is not the official ProjectE.** Please do not report bugs from this build to the ProjectE team or to the
> authors of the bundled add-ons. Report them in this repository's issues instead.

## What's included

| Part | Original project | What it adds |
|------|------------------|--------------|
| ProjectE | [sinkillerj/ProjectE](https://github.com/sinkillerj/ProjectE) | The base mod: EMC, transmutation, matter tools and armor, rings, collectors, relays, condensers |
| ProjectExtended | [pupnewfster/ProjectExtended](https://github.com/pupnewfster/ProjectExtended) | Dark and red matter tridents and shields, the Alchemical Barrel, the Interdiction Lantern, datapack blacklists and GameStages support |
| Project Expansion | [DonovanDMC/ProjectExpansion](https://github.com/DonovanDMC/ProjectExpansion) | Higher-tier collectors and relays, Power Flowers, Compact Sun, Condenser MK3, EMC Link, Transmutation Interface, Advanced Alchemical Chest, Arcane Transmutation Tablet, Matter Upgrader, Knowledge Sharing Book, infinite fuel and steak, the on-screen EMC display |
| ProjectE Integration | [TagnumElite/ProjectE-Integration](https://github.com/TagnumElite/ProjectE-Integration) | EMC values for recipes from Applied Energistics 2, Ender IO, Farming for Blockheads, Mystical Agriculture and Occultism |
| Mekanism EMC mappers | [mekanism/Mekanism](https://github.com/mekanism/Mekanism) | EMC values for Mekanism machine recipes (only active when Mekanism is installed) |

Everything uses the `projecte` namespace, so items and blocks from these add-ons now have `projecte:` ids.

This port is based on [UnQuis's 26.1 port](https://github.com/UnQuis/ProjectE), which already merged ProjectExtended
and Project Expansion and applied several open upstream fixes. This repository adds ProjectE Integration, the Mekanism
mappers and fixes found while playing.

## Installing

1. Install Minecraft **26.1.2** with **NeoForge 26.1.2.115** or newer.
2. Put `projecte-phoenix-<version>.jar` in your `mods` folder.
3. **Remove** any separate ProjectExtended, Project Expansion or ProjectE Integration jars. They are built in now and
   would conflict.

The mod is needed on both the client and the server.

### Optional mods

- **JEI** or **EMI**: shows EMC values and the Philosopher's Stone world transmutations
- **Jade**, **The One Probe** or **WTHIT**: shows a block's EMC when you look at it
- **Curios**: extra slots for rings, the Transmutation Tablet and the Arcane Transmutation Tablet
- **Mekanism** 10.7.9 or newer: EMC for Mekanism recipes (older versions are marked incompatible)
- **GameStages**: lock EMC values behind stages

## Configuration

The config files are in `config/ProjectE/`:

| File | Contents |
|------|----------|
| `client.toml` | Client options, such as the EMC display (`emcDisplay`, and `emcDisplayPosition`: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`) |
| `common.toml` | Options shared by client and server |
| `server.toml` | Gameplay options: item cooldowns, collector and relay speeds, and so on |
| `mapping.toml` | Turns individual EMC mappers on or off, including the integration mappers |
| `custom_emc.json` | Your own fixed EMC values |

You can also change EMC values with datapacks, see `example_custom_conversion.json`.

## Commands

`/projecte` (operators only) is ProjectE's own command:

- `/projecte setemc`, `/projecte removeemc`, `/projecte resetemc`: change an item's EMC value
- `/projecte emc add|remove|set|get|test <player>`: change a player's stored EMC
- `/projecte knowledge learn|unlearn|test|clear <player>`: change what a player has learned
- `/projecte showbag <player>`: open a player's Alchemical Bag

`/px` (also `/pex` and `/projectexpansion`) holds the Project Expansion commands: `book`, `emc`, `knowledge`,
`dumpfuelmap`, `reloademc`, `setowner` and `wiki`.

## Building

You need JDK 25. Then run:

```
./gradlew build
```

`build.gradle` puts the build output outside the project, in `~/.gradle-builds/<project folder>/libs/`.

## Credits and licenses

All of the original work belongs to its authors. Their license texts are kept in this repository:

- **ProjectE**: SinKillerJ, pupnewfster, MaPePeR, MozeIntel, williewillus, Lilylicious, Kolatra and contributors
  ([LICENSE](LICENSE), MIT)
- **ProjectExtended**: pupnewfster ([LICENSE-ProjectExtended](LICENSE-ProjectExtended), MIT)
- **Project Expansion**: DonovanDMC ([LICENSE-ProjectExpansion](LICENSE-ProjectExpansion), MIT)
- **ProjectE Integration**: TagnumElite ([LICENSE-ProjectEIntegration](LICENSE-ProjectEIntegration), MIT)
- **Mekanism ProjectE mappers**: Aidan C. Brady and the Mekanism team ([LICENSE-Mekanism](LICENSE-Mekanism), MIT)
- **26.1 port**: UnQuis
- **EE2**: x3n0ph0b3, the creator of Equivalent Exchange 2, allowed ProjectE to use EE2's assets.
  MidnightLightning made the EE2 GUI textures.

If you like ProjectE, support its original team through the links on the
[official ProjectE page](https://github.com/sinkillerj/ProjectE).
