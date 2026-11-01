# Minecraft 1.20.1 · 20.1.0

Version **20.1.0** brings Forge and Fabric onto the same feature set and release number. Fabric upgrades from **1.9.2** to the gameplay and UI features of **Forge 2.2.1**.

## Installation

Use the **1.20.1** download for your loader from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/uncraft-everything) or [Modrinth](https://modrinth.com/mod/uncraft-everything). Install it on both the client and server, with the dependencies listed for that file. Fabric also requires Fabric API. Use Java 17.

Choose `uncrafteverything-forge-1.20.1-20.1.0.jar` or `uncrafteverything-fabric-1.20.1-20.1.0.jar`. Replace the previous Uncraft Everything JAR rather than keeping both versions installed.

## Fabric: what changes from 1.9.2?

- **Automatic Uncrafting Table:** redstone control, automated item input/output, stored experience and experience deposit/withdrawal controls.
- **Updated manual table:** the same recipe-selection UI as Forge, recipe-selection history, client settings and configurable status colors.
- **In-game configuration:** common rules, per-item experience costs and FTB Quests progression screens and commands.
- **Recipe handling:** updated support for NBT-sensitive items, including filled shulker boxes, tipped arrows and smithing recipes.
- **Recipe viewers:** updated integration, including REI click handling around table controls, recipe selection and configuration screens.

Existing configuration values are retained. Older Fabric manual tables and their inventories can be loaded, including stack counts, NBT and damage. Newly added settings receive defaults. This release does not require the configuration reset described for the newer Minecraft 26.x CoolerConfig migration.

## First uncraft

1. Craft and place an [Uncrafting Table](./uncrafting-table#obtaining).
2. Put an item in its input slot and choose a recipe from the list on the left.
3. Click **UnCraft** to recover the ingredients and pay the configured experience cost. Hold Shift while clicking to repeat.
4. If uncrafting is blocked, hover the colored input slot to read the reason.

Use the [Auto Uncrafting Table](./auto-uncrafting-table) for redstone and hopper setups. Server owners can adjust [common rules](./common-config), [per-item costs](./per-item-exp-config) and [quest progression](./ftb-quest-progression-config).

## Forge: TaCZ support

20.1.0 adds optional support for guns, ammunition, scopes and other attachments, including guns dropped by enemies. See [TaCZ compatibility](./tacz-compatibility) for recipe requirements and material counts. This integration is for Forge; it does not make the Forge TaCZ mod load on Fabric.

See the [20.1.0 changelog](./changelog) for the release details.
