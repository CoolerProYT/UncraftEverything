# Per Item Exp Config

Per Item Exp Config allows players, pack developers or server admin to define individual exp **levels** or **points** required for items.

## Config Options

The **Per Item Experience Config** uses a simple JSON **key–value** structure:

- **Key** → an item selector (supports wildcards and tags)
- **Value** → the amount of experience (points or levels) consumed when uncrafting  
  (depends on the [`experienceType`](/v1.x.x/common-config#experience) in the Common Config)

--- 

### Key Format

You can target specific items, groups of items, wildcard patterns, or tags.

| Key Format                | Description                                                                                         |
|---------------------------|-----------------------------------------------------------------------------------------------------|
| `minecraft:oak_log`       | Selects the item **oak_log** from the Minecraft namespace.                                          |
| `minecraft:oak*`          | Selects any item starting with **"oak"** in the Minecraft namespace.                                |
| `minecraft:*_log`         | Selects any item ending with **"_log"** in the Minecraft namespace.                                 |
| `minecraft:*`             | Selects **all items** under the Minecraft namespace.                                                |
| `minecraft:green_*_glass` | Selects items that start with **"green_"** and end with **"_glass"** under the Minecraft namespace. |
| `#minecraft:planks`       | Selects **all items inside the item tag** `minecraft:planks`.                                       |

---

### Value Format

The value is a **positive integer**, representing the experience consumed.

| Value | Description                      |
|-------|----------------------------------|
| `1`   | Any positive integer is allowed. |

The meaning of this number depends on whether your config uses:
- `LEVEL` → values represent XP levels
- `POINT` → values represent raw XP points

## Config File

The per item exp config file can be found under `config/uncrafteverything-exp.json`. Below will include an example of how
the config file will look like:

::: code-group
```json [uncrafteverything-exp.json]
{
  "minecraft:dirt":3
}
```
:::

## In-game Config Screen

If you are not familiar with json, there is an in-game config screen for you to edit the config.

The config screen can be accessed by clicking the exp bottle icon button in Uncrafting Table GUI:
![](/config_button.png){.screenshot}

Per Item Exp Config Screen:
![](/exp_config_screen.png){.screenshot}

::: tip
Per Item Exp Config Screen button only available if the player is in creative mode or has Admin Permission
:::