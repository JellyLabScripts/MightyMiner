# MightyMiner V2 Changelog

## v2.7.0 (2025-18-6)

This update marks the first stable update of MightyMiner v2.7.0 

## Current Macros Included
- Commission Macro
- Powder Macro

## Changes
- Auto Refuel is now usable
- Fixed lag issues
- Fixed personal compactors not working
- Fixed accidentally clicking on players
- Fixed calling royal pigeons twice
- Detecting item names properly
- Added support for all pickaxe abilities
- Added options to disable using pickaxe ability
- New commission HUD and configs
- Enhanced mining algorithm

## v2.7.0-alpha (2025-5-1)

This update marks the official resumption of development on Mighty Miner.

## Current Macros Included
- Commission Macro
- Powder Macro
- Glacial Macro
- Mining Macro
- Route Mining Macro

## Changes
- Completely rewritten Mining Macro for improved efficiency and maintainability.
- Block Miner has been overhauled with cleaner structure and better logic separation.
- Automatically disable macro when there is an error

## Updated Coding Standards
- Avoid deeply nested switch statements when possible. Implement PROPER state machine patterns (see BlockMiner.java for a reference implementation).
- Add Javadocs and inline comments, especially for abstract classes. (Tip: ChatGPT can assist with generating documentation.)
- Standardize logging and error handling across the project. For example, all errors should be routed through the main macro class for consistency.
- Disable macro automatically when there is an error

