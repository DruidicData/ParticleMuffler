# Particle Muffler (NeoForge 1.21.1)

A **client-side only** mod for picking which particles you see, either everywhere or only inside
configurable areas. It is the particle counterpart of
[Extreme Sound Muffler](https://github.com/LeoBeliik/ExtremeSoundMuffler) by LeoBeliik. It uses the
same GUI layout, textures and workflow, with the sounds swapped for particles.

It does not need to be installed on the server and works on any vanilla or modded server.

## How it works

1. Open the screen with the **Particles** button in your inventory, or bind the
   *Open particle muffler screen* key (Controls → Miscellaneous).
2. Click the muffle button (sparkle icon) next to a particle. Muffled rows turn aqua.
3. Optional: drag the slider on a muffled row to set its **density**. 0% hides every particle
   of that type, and 50% lets about half of them through. The maximum is 90%, the same as ESM's volume slider.
4. Optional: press the play button to **preview** the particle. A short burst appears to the left
   and right of the GUI.
5. Use the bottom-left button to switch between the **Recent** (particles spawned around you),
   **All** (every registered particle type) and **Muffled** lists. You can also use the search bar.
6. The bottom-right button turns all muffling on or off. The trash button clears the current
   muffled list. Hold **Shift** while clicking it to clear the Recent list instead.

## Anchors (area-based muffling)

Anchors muffle particles only inside a sphere around a point:

1. Click one of the 10 numbered buttons at the top.
2. Click the marker button in the right panel to place the Anchor where you are standing.
3. Optional: click the edit button next to it to rename the Anchor and change its radius.
4. Muffle particles as usual. They are hidden only within that Anchor's radius and dimension.
   In the Recent list, the **EVERYTHING** row hides every particle inside the Anchor.

Set Anchors appear in green. Click the selected Anchor again to go back to the global list.
If several Anchors overlap, the lowest density wins.

## Config (`config/particlemuffler-client.toml`, also editable in Mods → Particle Muffler → Config)

| Option | What it does |
| --- | --- |
| `forbiddenParticles` | Particles hidden from the Recent list (substring match, e.g. `"block"`) |
| `modsMuffled` | Mod-wide muffling, e.g. `"somemod:0"` hides every particle from `somemod`, `"minecraft:50"` halves vanilla particles |
| `lawfulAllList` | Also hide blacklisted particles from the All list |
| `defaultMuteDensity` | Density applied when you press muffle (default 0) |
| `leftButtons` | Put the muffle/preview buttons on the left |
| `showTip`, `useDarkTheme` | Tips at the bottom, dark texture |
| `disableInventoryButton`, `invButtonX/Y`, creative equivalents | Inventory button. You can also move it in game by holding right-click on it and dragging |
| `disableAnchors`, `anchorRange` | Turn Anchors off, and set the maximum Anchor radius |

## Saved data

Stored as JSON in `<minecraft dir>/ParticleMuffler/`:
* `particlesMuffled.json`: the global muffled list, shared by all worlds.
* `world_<name>/anchors.json` or `server_<address>/anchors.json`: Anchors for each world or server.

## What it can and cannot hide

The mod intercepts `ParticleEngine#createParticle`, which every normal particle goes through
(`Level#addParticle`, `/particle`, block and entity effects, emitters and most modded particles).
It also intercepts block breaking and block hitting particles, which skip that method. A few modded effects
that build particle objects themselves and add them directly to the particle engine cannot be identified
by type, so they cannot be muffled.

## Building

Requires Java 21.

```bash
./gradlew build
```

The jar is written to `build/libs/`. Use `./gradlew runClient` to start a development client.

## License / credits

LGPL-3.0, the same license as Extreme Sound Muffler. The GUI layout, textures (with redrawn icons), the Anchor
system and the general design are adapted from
[Extreme Sound Muffler](https://github.com/LeoBeliik/ExtremeSoundMuffler) by LeoBeliik.
