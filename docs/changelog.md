# Changelog

Changes since `v2.0.0`. From the 26.x ports onwards, version numbers start with the Minecraft version.

## 26.3.0.2

- Turning off [`showJeiUncraftingCategory`](./client-config#behaviour) now skips building the Uncrafting recipes for JEI, instead of only hiding the category. This removes the extra load time in large modpacks. Turning it back on in-game builds the recipes then.

## 26.3.0.1

- Added [`showJeiUncraftingCategory`](./client-config#behaviour) to the client config. Turn it off to hide the Uncrafting category from JEI.

## 26.2.0.3

- Turning off [`showJeiUncraftingCategory`](./client-config#behaviour) now skips building the Uncrafting recipes for JEI, instead of only hiding the category. This removes the extra load time in large modpacks. Turning it back on in-game builds the recipes then.

## 26.2.0.2

- Added [`showJeiUncraftingCategory`](./client-config#behaviour) to the client config. Turn it off to hide the Uncrafting category from JEI.

## 26.2.0.1

- JEI's Uncrafting Table recipe lookup now respects the config.

## 26.2.0.0

- Ported to Minecraft 26.2.
- The mod now builds for NeoForge and Fabric from one codebase, with a new versioning scheme.
- Added [`restrictAmbiguouslyCraftedItems`](./common-config#restrictions) to the common config. Turn it on to block recipes that use an item tag as an ingredient.

## 26.1.2.12

- Turning off [`showJeiUncraftingCategory`](./client-config#behaviour) now skips building the Uncrafting recipes for JEI, instead of only hiding the category. This removes the extra load time in large modpacks. Turning it back on in-game builds the recipes then.

## 26.1.2.11

- Added [`showJeiUncraftingCategory`](./client-config#behaviour) to the client config. Turn it off to hide the Uncrafting category from JEI.

## 26.1.2.8

- Added compatibility with Bonded.

## 26.1.2.6

- Fixed a server crash from config syncing when the server was not available yet.

## 26.1.2.5

- Fixed the Uncrafting Table not checking and taking experience from the player correctly.

## 26.1.2.4

- Added compatibility with Traveler's Backpack backpack and upgrade recipes.

## 26.1.2.3

::: warning Config reset
The config system was rewritten with CoolerConfig. Most settings reset to their default values; see the [common config](./common-config) for the new layout.
:::

- Added [`allowDamagedNonRepairable` and `minimumDurability`](./common-config#damaged) to the common config.
- Updated the common config screen.
- Re-implemented FTB Quests compatibility.

## 2.1.6

- Turning off [`showJeiUncraftingCategory`](./client-config#behaviour) now skips building the Uncrafting recipes for JEI, instead of only hiding the category. This removes the extra load time in large modpacks. Turning it back on in-game builds the recipes then. Backported to Minecraft 1.21.1 (NeoForge).

## 2.1.5

- Added [`showJeiUncraftingCategory`](./client-config#behaviour) to the client config. Turn it off to hide the Uncrafting category from JEI. Backported to Minecraft 1.21.1 (NeoForge).

## 2.1.0

- Added [`prioritizeVanillaIngredientRecipe`](./common-config#recipeselectionorder) to the common config to sort the recipe list.

## 2.0.2

- Fixed enchanted items not uncrafting when `allowEnchantedItems` was `true` but `outputEnchantedBook` was `false`.
- Added translations for all new text (machine translated, may be inaccurate).

## 2.0.1

- Fixed the selected recipe resetting to the default when a player opened the screen.
- Fixed the recipe selection not updating while the screen stayed open during uncrafting (Fabric).
- Fixed no recipe being found for damaged netherite tools and armor.

## 2.0.0

**Added**

- [Auto Uncrafting Table](./auto-uncrafting-table).
- Commands to open the config screens.
- [Client config](./client-config).
- Output is moved to the player's inventory when **UnCraft** is clicked. Toggle it in the client config. Does not apply to the Auto Uncrafting Table.

**Changed**

- The black box and status text in the Uncrafting Table screen were replaced with a colored overlay on the input slot and a status tooltip.
- The "Experience Required" text in the screen got a new design.
- Uncrafting output is no longer laid out in the crafting shape; identical items are stacked together.
- Experience cost now works like the anvil, so recipes that cost levels are no longer unfair.

**Fixed**

- Missing filter for items that differ only by data components (NBT).
