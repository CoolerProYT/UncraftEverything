# Per-item experience config

**File:** `config/uncrafteverything-exp.json` on both loaders.<br>
**Command:** `/ueconfig exp` (creative mode or operator permission)

Set a cost for an [item selector](./common-config#item-selectors). Unlisted items use `experiences` from the common config. Costs follow the common `experienceType` setting: levels or points.

## File format

The 1.20.1 file is a **flat JSON object**, without a `PerItemExp` wrapper:

```json
{
  "minecraft:diamond_sword": 2,
  "minecraft:netherite_*": 3
}
```

The generated default sets `minecraft:diamond_sword` to `2`. Use `/ueconfig exp` to add, edit or delete entries in game. Existing entries are preserved when upgrading to 20.1.0.
