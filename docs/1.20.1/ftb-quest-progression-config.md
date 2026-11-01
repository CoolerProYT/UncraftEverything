# FTB Quests progression config

**File:** `config/uncrafteverything-ftbquest-progression.json` on both loaders.<br>
**Command:** `/ueconfig progression` (creative mode or operator permission)

Install FTB Quests and turn on `FTBQuestProgression.enableProgression` in the [common config](./common-config#ftbquestprogression) to require a completed quest or chapter before an item can be uncrafted.

## File format

Map [item selectors](./common-config#item-selectors) to quest or chapter IDs. Copy the ID from the FTB Quests book in edit mode.

The 1.20.1 file is a **flat JSON object**, without a `Progressions` wrapper:

```json
{
  "minecraft:diamond_*": "6E52157866E1EC7F"
}
```

Replace the example ID with one from your own quests. Items without an entry remain available unless `onlyAllowDefinedProgression` is enabled. The in-game screen can add, edit and delete entries; it is available when FTB Quests is installed.
