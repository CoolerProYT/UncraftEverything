# 20.1.1

## Recipe viewer fixes

- Reduced smithing trim preview generation work by matching registered trim materials and patterns before expanding armor inputs, avoiding repeated registry scans for each ingredient combination.
- Fixed trim previews modifying shared recipe ingredient stacks and using the wrong material or template.
- Skip empty or unsupported trim combinations rather than creating invalid previews.
- Removed the redundant server config request during recipe viewer registration; configuration is already synchronized on login.
- Added JEI registration timing logs to help diagnose slow recipe generation or indexing in modpacks.

# 20.1.0

## Multiloader release

- Combined Minecraft 1.20.1 Forge and Fabric into one multiloader project with a shared release version.
- Updated version numbers and JAR filenames to match the other multiloader releases.
- Shared gameplay, recipe handling, configuration, packet handlers, screens and assets across both loaders, with native platform-service adapters.

## Fabric: major feature and UI update
Fabric moves from the 1.9.2 feature set to parity with Forge 2.2.1.

- Added the Automatic Uncrafting Table, including redstone control and automated item input/output.
- Added stored experience and experience deposit/withdrawal controls for the automatic table.
- Updated the manual table and recipe-selection UI to match the current Forge version.
- Added recipe-selection history, client settings and configurable status colors.
- Brought the current common, experience and progression configuration screens and commands to Fabric.
- Updated recipe handling for NBT-sensitive items, including filled shulker boxes, tipped arrows and smithing recipes.
- Updated recipe-viewer integration; REI no longer intercepts clicks on table controls, recipe selection or configuration screens.
- Preserved existing configuration values and supported loading the older manual-table block entity and inventory format, including item counts, NBT and damage.

## Forge: optional TaCZ compatibility

- Both uncrafting tables now support TaCZ guns (including enemy drops), ammunition, scopes and other attachments when a matching Gun Smith Table recipe exists.
- Ammunition consumes complete material batches with exact ingredient counts. Oversized batches use whole-material ratios without rounding.
- Calibers and attachment models are matched separately.
- Gun matching uses the gun model, so fire mode, loaded ammunition, experience, custom names and attachments do not prevent recipe discovery.
- Return the full counted recipe materials and installed attachments. Loaded magazine/chamber ammunition and NPC dummy ammunition are not returned.
- Support both the current recipe manager and older TaCZ recipe registries without duplicate recipes.
- Check the complete output batch before consuming the input; recipes that exceed the nine output slots are rejected without losing materials.

## Fixes

- Renamed items can be uncrafted while retaining the existing restrictions, progression and experience checks.
