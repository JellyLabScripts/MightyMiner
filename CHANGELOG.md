# MightyMiner V2 Changelog

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

## Updated Coding Standards
- Avoid deeply nested switch statements when possible. Implement PROPER state machine patterns (see BlockMiner.java for a reference implementation).
- Add Javadocs and inline comments, especially for abstract classes. (Tip: ChatGPT can assist with generating documentation.)
- Standardize logging and error handling across the project. For example, all errors should be routed through the main macro class for consistency.

