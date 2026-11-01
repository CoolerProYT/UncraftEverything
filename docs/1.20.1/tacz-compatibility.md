# TaCZ compatibility · Forge 1.20.1

20.1.0 adds optional support for **Timeless and Classics Zero (TaCZ)** in both uncrafting tables. Install TaCZ separately on Forge. Fabric receives the shared gameplay and UI update, but this Forge integration does not apply to Fabric.

## Supported items

- Guns, including enemy drops and renamed guns.
- Standalone ammunition.
- Scopes, grips, magazines and other attachments.

An item needs a matching **Gun Smith Table recipe** in TaCZ or its installed gun packs. An item with no matching recipe has no materials to recover.

Guns are matched by model rather than mutable data such as fire mode, loaded ammunition, gun experience or custom name. Ammunition calibers and attachment models are matched separately. Both older TaCZ recipe registries and the current recipe manager are supported.

## Gun materials and attachments

Uncrafting returns the full counted recipe ingredients and installed attachments. Built-in attachments do not create extra attachment items.

**Loaded magazine/chamber ammunition and NPC dummy ammunition are not returned.** To uncraft ammunition, put the standalone ammo item into the input slot.

## Exact ammunition batches

The input must contain a complete batch that returns whole ingredient counts. Large batches can be reduced by a common factor to fit the input slot; material counts are never rounded.

Examples from the tested TaCZ recipes:

| Input | Recovered materials |
| --- | --- |
| 35 rounds of 7.62×39 mm | 15 copper ingots + 3 gunpowder |
| 50 rounds of .22 WMR | 5 copper ingots + 1 gunpowder |
| ACOG TA31 scope | 10 iron ingots + 3 amethyst shards + 8 redstone dust |

The .22 WMR example comes from a 100-round recipe with 10 copper and 2 gunpowder, reduced to an exact 50-round batch. Gun-pack recipes can change these counts.

With only 34 rounds for the 35-round recipe, the input shows **Not Enough Input Item**. Adding the missing round makes the batch available without reopening the screen.

## Output space

All recovered ingredients and attachments must fit in the table's nine output slots. The complete batch is checked before input or experience is consumed. Clear the output slots if **No Suitable Output Slot** appears. Recipes whose complete outputs cannot fit in nine slots cannot be processed; materials are not silently discarded.

The integration was checked with TaCZ **1.1.4** and **1.1.8-hotfix2**.
