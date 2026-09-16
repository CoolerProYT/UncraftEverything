# Common config

The common config controls what can be uncrafted and what it costs. On a server it is set by the server and applies to every player.

**File:** `config/uncrafteverything-common.toml`<br>
**Command:** `/ueconfig common` (creative mode or operator only)

::: warning Upgrading from before 26.1.2.3
The config was moved to a new format in `26.1.2.3`, and most settings were reset to their defaults. Some sections were also renamed, so check your old values against this page.
:::

## Options

### Experience

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `experienceType` | String | `"LEVEL"` | Whether uncrafting costs experience **levels** or **points**. `LEVEL` or `POINT`. |
| `experiences` | Integer | `1` | Experience one uncraft costs, `0` or more. Set different costs for specific items in the [per item exp config](./per-item-exp-config). |

### Restrictions

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `restrictionType` | String | `"BLACKLIST"` | How `restrictions` is used. `BLACKLIST` blocks the listed items; `WHITELIST` allows only the listed items. |
| `restrictions` | List | `[]` | Items to block or allow, depending on `restrictionType`. See the [item selector format](#item-selectors). Invalid entries reset the config when it loads. |
| `restrictAmbiguouslyCraftedItems` | Boolean | `false` | Blocks recipes that use an item tag as an ingredient, such as any planks. See [the limitation](./uncrafting-table#limitation) this avoids. |

### Enchanted

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `allowEnchantedItems` | Boolean | `true` | Allows enchanted items to be uncrafted. |
| `outputEnchantedBook` | Boolean | `false` | Also outputs an Enchanted Book with the item's enchantments. |

### UnSmithing

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `allowUnSmithing` | Boolean | `true` | Allows items made in a smithing table to be uncrafted, such as netherite gear and trimmed armor, including smithing recipes from other mods. |

### Damaged

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `allowDamaged` | Boolean | `true` | Allows damaged items to be uncrafted. Their repair material is reduced according to the durability lost. |
| `allowDamagedNonRepairable` | Boolean | `false` | Allows damaged items with no repair material (bows, crossbows, shears, fishing rods) to be uncrafted. These give back **all** ingredients whatever their durability, so this can be exploited. |
| `minimumDurability` | Decimal | `0.8` | With `allowDamagedNonRepairable` on, the durability such an item needs to be uncrafted, from `0.0` to `1.0`. `0.8` means at least 80% durability left. |

### ModdedIngredients

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `preventModdedIngredientsFromVanillaItems` | Boolean | `true` | Vanilla items are only uncrafted with vanilla recipes, never with modded ones. Prevents duplication through modded recipes. |
| `restrictedModIngredients` | List | `["productivetrees", "chipped"]` | Mod IDs whose recipes are skipped when searching for uncrafting recipes. Useful for mods with huge numbers of recipes that slow the search down. |

### RecipeSelectionOrder

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `prioritizeVanillaIngredientRecipe` | Boolean | `true` | Lists recipes with more vanilla ingredients first in the recipe list. |

### FTBQuestProgression

These settings only work when **FTB Quests** is installed. See the [FTB Quests progression config](./ftb-quest-progression-config).

| Setting | Type | Default | Description |
| --- | --- | --- | --- |
| `enableProgression` | Boolean | `false` | Locks the items listed in the progression config until the player completes the linked quest or chapter. |
| `onlyAllowDefinedProgression` | Boolean | `false` | With progression on, items that aren't listed in the progression config can't be uncrafted at all. |

## Item selectors

`restrictions` entries, and the keys of the [per item exp](./per-item-exp-config) and [progression](./ftb-quest-progression-config) configs, select items in these ways:

| Selector | Matches |
| --- | --- |
| `minecraft:oak_log` | One item. |
| `minecraft:*` | Every item from a mod (here, vanilla). |
| `minecraft:oak_*` | Items whose ID starts with `oak_`. |
| `minecraft:*_log` | Items whose ID ends with `_log`. |
| `minecraft:green_*_glass` | Items whose ID starts with `green_` and ends with `_glass`. |
| `#minecraft:planks` | Every item in an item tag. |

::: tip Finding an item's ID
Press <kbd>F3</kbd> + <kbd>H</kbd> in game to turn on advanced tooltips, then hover the item.
:::

## Example file

```toml [uncrafteverything-common.toml]
# UncraftEverything Configuration

[Experience]
	experienceType = "LEVEL"
	experiences = 1

[Restrictions]
	restrictionType = "BLACKLIST"
	restrictions = ["minecraft:crafting_table", "#minecraft:wool"]
	restrictAmbiguouslyCraftedItems = false

[Enchanted]
	allowEnchantedItems = true
	outputEnchantedBook = false

[UnSmithing]
	allowUnSmithing = true

[Damaged]
	allowDamaged = true
	allowDamagedNonRepairable = false
	minimumDurability = 0.8

[ModdedIngredients]
	preventModdedIngredientsFromVanillaItems = true
	restrictedModIngredients = ["productivetrees", "chipped"]

[RecipeSelectionOrder]
	prioritizeVanillaIngredientRecipe = true

[FTBQuestProgression]
	enableProgression = false
	onlyAllowDefinedProgression = false
```

The generated file also has a comment above each setting; they are left out here.

## In-game screen

Run `/ueconfig common` to edit these settings with toggles and text boxes. Only players in creative mode or with operator permission can open it.

![Common config screen](/common_config_screen.png){.screenshot}
