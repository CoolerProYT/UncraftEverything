# Common config

These settings apply to **20.1.0 for Minecraft 1.20.1**, on both loaders. Server rules determine uncrafting behaviour.

| Loader | File |
| --- | --- |
| Forge | `config/uncrafteverything-common.toml` |
| Fabric | `config/uncrafteverything_common.toml` |

Run `/ueconfig common` in creative mode or as an operator to edit the rules in game. Existing values are retained when upgrading; the two loaders keep their existing common-config filenames.

## Experience

Under `[Experience]`, `experienceType` defaults to `"LEVEL"` and also accepts `"POINT"`. `experiences` defaults to `1` and sets the cost per uncraft. Use `0` for free uncrafting. [Per-item costs](./per-item-exp-config) override this default.

The manual table uses player experience; the automatic table uses stored experience.

## Restrictions

Under `[Restrictions]`, `restrictionType` defaults to `"BLACKLIST"`; `"WHITELIST"` allows only matching items. The default `restrictions` list contains `uncrafteverything:uncrafting_table` and `minecraft:crafting_table`.

### Item selectors

Use item IDs (`minecraft:diamond_sword`), wildcards (`minecraft:netherite_*`, `examplemod:*`) or item tags prefixed with `#` (`#minecraft:planks`). These selectors also work in per-item experience and progression files.

## Other rules

| TOML section | Setting | Default | Effect |
| --- | --- | --- | --- |
| `AllowEnchantedItems` | `allowEnchantedItems` | `true` | Allow enchanted items. |
| `AllowUnSmithing` | `allowUnSmithing` | `true` | Allow reversing supported smithing results. |
| `AllowDamaged` | `allowDamaged` | `true` | Allow damaged items. |
| `PreventModdedIngredientsFromVanillaItems` | `preventModdedIngredientsFromVanillaItems` | `true` | Prevent vanilla items from using reverse recipes with modded ingredients. |
| `RestrictedModIngredients` | `restrictedModIngredients` | `["productivetrees", "chipped"]` | Exclude ingredient alternatives from the listed mod IDs. |
| `RecipeSelectionOrder` | `prioritizeVanillaIngredientRecipe` | `true` | Prefer recipes with more vanilla ingredients. |
| `OutputEnchantedBook` | `outputEnchantedBook` | `false` | Return an enchanted book for enchanted items. |

## FTBQuestProgression

Under `[FTBQuestProgression]`, `enableProgression` defaults to `false` and enables [quest-based restrictions](./ftb-quest-progression-config) when FTB Quests is installed. `onlyAllowDefinedProgression` also defaults to `false`; turning it on blocks items without a progression entry.

These are the 1.20.1 settings. Options documented for newer Minecraft versions, such as `restrictAmbiguouslyCraftedItems` and the newer durability rules, are not part of this release.
