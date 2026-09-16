# Per item exp config

Sets a different experience cost for specific items. Items not listed here use `experiences` from the [common config](./common-config#experience).

**File:** `config/uncrafteverything-exp.json`<br>
**Command:** `/ueconfig exp` (creative mode or operator only)

## Format

Entries go in the `PerItemExp` object. Each entry maps an item selector to a cost.

| Part | Format | Example |
| --- | --- | --- |
| Key | An [item selector](./common-config#item-selectors): item ID, wildcard or `#tag` | `minecraft:netherite_*` |
| Value | Experience per uncraft, a positive whole number | `2` |

The cost is in levels or points, depending on [`experienceType`](./common-config#experience) in the common config.

## Example file

```json [uncrafteverything-exp.json]
{
	"PerItemExp": {
		"minecraft:netherite_*": 2,
		"minecraft:dirt": 3
	}
}
```

The default file sets netherite items to `2`.

## In-game screen

Run `/ueconfig exp` to add, edit and delete entries without touching the JSON. Only players in creative mode or with operator permission can open it.

![Per item exp config screen](/exp_config_screen.png){.screenshot}
