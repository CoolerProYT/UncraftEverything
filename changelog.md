# v2.0.0 - 1.20.1 Changelog
## Addition
- Added Auto Uncrafting Table
- Added new commands to open config screen
- Added new Client Config
- Added new function to auto move output stacks to player inventory when uncraft button clicked (Not applicable to Auto Uncrafting Table, can be toggle in Client Config)

## Changes
- Removed Black Box and Status Text from Uncrafting Table GUI, replaced with input slot color overlay and tooltip to show status of the input stack
- Replaced `Experience Required` text in Uncrafting Table GUI with better design
- Uncrafting output is no longer Shaped, it will stack all same items together instead of follow original crafting shape
- Changed exp deduction calculation for uncrafting to prevent unfair if uncrafting require Level instead of Points, new calculation should be same as how Anvil works

## Fixes
- Fixed missing filter for DataComponent/NBT based items

This is a major update, bug is expected, feel free to open an issue on GitHub if you found any.