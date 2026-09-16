# Common Config

Common config allows players, pack developers or server admin to configure the options related to uncrafting.

## Config Options

### Experience

| Setting          | Type   | Default   | Description                                                                                                                                                  |
|------------------|--------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `experienceType` | String | `"LEVEL"` | Determines whether the cost uses experience **levels** or **points**. Allowed values: `LEVEL`, `POINT`.                                                      |
| `experiences`    | Number | `1`       | Default experience required to uncraft an item. More detailed values can be configured in [`Per Item Exp Config`](/v1.x.x/per-item-exp-config). Must be > 0. |

---

### Restrictions

| Setting           | Type   | Default                                                              | Description                                                                                                                                                                                                                                                                                     |
|-------------------|--------|----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `restrictionType` | String | `"BLACKLIST"`                                                        | Determines how the item restriction list behaves. Allowed values: `BLACKLIST`, `WHITELIST`.                                                                                                                                                                                                     |
| `restrictions`    | List   | `["uncrafteverything:uncrafting_table", "minecraft:crafting_table"]` | List of items that are allowed or denied depending on `restrictionType`. Invalid entries will reset the config at runtime. <br> **Format examples:** `modid:item`, `modid:*`, `modid:*_glass`, `#modid:item_tag`. <br>You can press **F3 + H** in-game and hover an item to see its modid:name. |

---

### AllowEnchantedItems

| Setting               | Type    | Default | Description                             |
|-----------------------|---------|---------|-----------------------------------------|
| `allowEnchantedItems` | Boolean | `true`  | Allows enchanted items to be uncrafted. |

---

### AllowUnSmithing

| Setting           | Type    | Default | Description                                                                          |
|-------------------|---------|---------|--------------------------------------------------------------------------------------|
| `allowUnSmithing` | Boolean | `true`  | Allows uncrafting of smithing-upgraded items (e.g., trimmed armor, Netherite armor). |

---

### AllowDamaged

| Setting        | Type    | Default | Description                         |
|----------------|---------|---------|-------------------------------------|
| `allowDamaged` | Boolean | `true`  | Allows uncrafting of damaged items. |

---

### PreventModdedIngredientsFromVanillaItems

| Setting                                    | Type    | Default | Description                                                                                                         |
|--------------------------------------------|---------|---------|---------------------------------------------------------------------------------------------------------------------|
| `preventModdedIngredientsFromVanillaItems` | Boolean | `true`  | Prevents vanilla items from being uncrafted using **modded** recipes to avoid unintended or exploit-prone outcomes. |

---

### RestrictedModIngredients

| Setting                    | Type | Default                          | Description                                                                                                     |
|----------------------------|------|----------------------------------|-----------------------------------------------------------------------------------------------------------------|
| `restrictedModIngredients` | List | `["productivetrees", "chipped"]` | List of **mod IDs** excluded when searching for uncrafting recipes to improve performance. <br>Format: `modid`. |

---

### FTBQuestProgression

| Setting                       | Type    | Default | Description                                                                                            |
|-------------------------------|---------|---------|--------------------------------------------------------------------------------------------------------|
| `enableProgression`           | Boolean | `false` | Enables progression-based uncrafting when **FTB Quests** is installed.                                 |
| `onlyAllowDefinedProgression` | Boolean | `false` | When progression is enabled: only items explicitly defined in the progression config can be uncrafted. |

::: info
This options only work when `FTB Quests` is installed.

See also [`FTB Quest Progression Config`](/v1.x.x/ftb-quest-progression-config).
:::

---

### OutputEnchantedBook

| Setting               | Type    | Default | Description                                                                             |
|-----------------------|---------|---------|-----------------------------------------------------------------------------------------|
| `outputEnchantedBook` | Boolean | `false` | When uncrafting enchanted items, outputs an Enchanted Book containing the enchantments. |

## Config File

The common config file can be found under `config/uncrafteverything-common.toml`. Below will include an example of how
the config file will look like:

::: code-group

```toml [uncrafteverything-common.toml]
[Experience]
	experienceType = "LEVEL"
	experiences = 1

[Restrictions]
	restrictionType = "BLACKLIST"
	restrictions = ["uncrafteverything:uncrafting_table", "minecraft:crafting_table"]

[AllowEnchantedItems]
	allowEnchantedItems = true

[AllowUnSmithing]
	allowUnSmithing = true

[AllowDamaged]
	allowDamaged = true

[PreventModdedIngredientsFromVanillaItems]
	preventModdedIngredientsFromVanillaItems = true

[RestrictedModIngredients]
	restrictedModIngredients = ["productivetrees", "chipped"]

[FTBQuestProgression]
	enableProgression = false
	onlyAllowDefinedProgression = false

[OutputEnchantedBook]
	outputEnchantedBook = false
```

:::

::: info
The file name will be `config/uncrafteverything_common.toml` for Fabric
:::

## In-game Config Screen

If you are not familiar with code syntax, there is an in-game config screen for you to edit the config by toggling.

The config screen can be accessed by clicking the settings icon button in Uncrafting Table GUI:
![](/config_button.png){.screenshot}

Common Config Screen:
![](/common_config_screen.png){.screenshot}

::: tip
Common Config Screen button only available if the player is in creative mode or has Admin Permission
:::