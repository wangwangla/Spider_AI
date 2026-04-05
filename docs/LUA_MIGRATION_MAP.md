# Balatro Lua to LibGDX Migration Map

This project now keeps the extracted `Balatro_v0.7/assets` tree under `assets/original_lua`.

The goal is not to "translate random files one by one", but to preserve the same runtime layers that exist in the Lua version and move them into Java packages with stable ownership.

## Current bridge

- Desktop assets now resolve from `Balatro_Libgdx/assets`.
- The original Lua source tree is expected at `assets/original_lua`.
- `LuaProjectMirror` scans the extracted project and reports script, localization, shader, sound, and texture counts at startup.
- `MainScreen` shows the mirror status so the LibGDX project always exposes whether the extracted source is linked.

## Suggested Java package mapping

- `main.lua` and `conf.lua`
  Java target: `com.tony.balatro.bootstrap`
  Responsibility: LibGDX lifecycle, window config, render loop, boot sequencing.

- `globals.lua`
  Java target: `com.tony.balatro.runtime`
  Responsibility: global flags, runtime settings, save profile state, feature flags.

- `game.lua`
  Java target: `com.tony.balatro.game`
  Responsibility: startup pipeline, managers, shaders, localization, shared sprites, run state.

- `engine/*.lua`
  Java target: `com.tony.balatro.engine`
  Responsibility: events, nodes, sprites, particles, controller, save/load, sound.

- `functions/*.lua`
  Java target: `com.tony.balatro.logic`
  Responsibility: state events, common rules, UI callbacks, helper functions.

- `card.lua`, `cardarea.lua`, `blind.lua`, `back.lua`, `tag.lua`, `card_character.lua`, `challenges.lua`
  Java target: `com.tony.balatro.model`
  Responsibility: gameplay domain objects and prototype data.

- `localization/*.lua`
  Java target: `com.tony.balatro.localization`
  Responsibility: language packs and text lookup.

- `resources/*`
  Java target: `com.tony.balatro.resource`
  Responsibility: texture regions, shader programs, audio handles, font setup.

## Immediate next porting order

1. Port `globals.lua` into a Java runtime state object.
2. Port `game.lua:start_up()` into a LibGDX bootstrap service.
3. Port `engine/event.lua`, `engine/node.lua`, `engine/sprite.lua`.
4. Port `functions/state_events.lua` and `functions/common_events.lua`.
5. Port gameplay objects starting with `card.lua` and `cardarea.lua`.
