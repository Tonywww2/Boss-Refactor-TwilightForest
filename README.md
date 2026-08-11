# Boss Refactor Twilight Forest

Forge 1.20.1 framework for replacing and extending boss fights from
The Twilight Forest.

The Knight Phantom encounter currently includes configurable fire vulnerability,
per-fallen-knight speed and damage scaling, sequential telegraphed charges,
shield cooldowns, optional SlashBlade SenDimS parries, and a fully healed final
knight with damage reduction and consecutive charges.

All encounter mechanics and numeric values are configured under
`knight_phantom` in `config/bossrefactortf-common.toml`. The default charge uses
a 30-tick windup, a locked 30-block maximum path clipped to the arena, 120%
attack damage, and a 60-tick shield cooldown. A possessed knight's death waits
30 ticks before handoff. The final knight uses a 15-tick windup and three
consecutive charge segments, re-aiming during the windup between segments.
Parrying a charge leaves that knight stationary and unable to attack for 40
ticks by default before the charge round continues.

The Alpha Yeti fight is also fully owned by this mod. Ice Rampage, Enraged
Pursuit, and Frost Beast Stomp build visible fatigue; the third point forces a
10-second rest, reduced to five seconds below half health. Pursuit deals one
attack-damage pulse per second and ends only after six seconds or three shield
blocks/SlashBlade parries. Stomp uses a marked 10-block hit followed by four
marked falling-ice rings reaching 12 blocks. Below half health, ordinary attacks
and every skill windup queue an independent marked falling-ice strike.

Alpha Yeti timings, ranges, knockback, falling-ice density, and the random
15-35 tick post-skill cruise are configured under `alpha_yeti` in
`config/bossrefactortf-common.toml`.

The Ur-Ghast fight alternates between 20-second patrol and offensive modes from
the start. The first time health falls below 50%, it immediately resets to
offensive mode and starts an Arsenic-Lead Mist. Offensive mode keeps the boss
enraged, increases flight speed by 30%, chases at a melee-accessible height,
halves ghast-trap control time, and retains Twilight Forest's original tantrum
tear rain. Each six-percent maximum-health loss threshold summons one original
minion wave, and healing above a threshold allows it to trigger again. Giant
fireballs retain Twilight Forest's projectile and renderer but use
a moving red four-block AABB marker, custom 150% attack-damage falloff, and no
vanilla explosion. Offensive mode also adds two telegraphed crimson mist columns
with a harmless windup, teleport follow-up, and one 20% attack-damage pulse per
second. Values are configured under `ur_ghast` in
`config/bossrefactortf-common.toml`.

## Development

Build and run without SenDimS:

```powershell
.\gradlew.bat build --console=plain
.\gradlew.bat runClient --console=plain
.\gradlew.bat runServer --console=plain -x downloadAssets
```

Focused dependency-free combat checks:

```powershell
.\gradlew.bat telegraphSelfTest --console=plain
.\gradlew.bat knightPhantomCombatSelfTest --console=plain
.\gradlew.bat alphaYetiCombatSelfTest --console=plain
.\gradlew.bat urGhastCombatSelfTest --console=plain
```

The local SenDimS JAR is available to compile the optional compatibility layer.
SenDimS 1.0.19 does not declare all of its hard runtime dependencies in
`mods.toml`; a real integration run must therefore use the complete SenDimS
development/modpack environment rather than this JAR alone. The explicit
property adds the local JAR and SlashBlade Resharped, but additional SenDimS
runtime dependencies still have to be supplied by that environment:

```powershell
.\gradlew.bat runClient -PenableSenDimsRuntime --console=plain
.\gradlew.bat runServer -PenableSenDimsRuntime --console=plain -x downloadAssets
```

The default runtime classpath excludes SenDimS. Core classes never link against
its API; `OptionalCompatBootstrap` loads the compatibility package only when the
mod is installed.

## Adding a boss

1. Implement the boss state machine without referencing SenDimS.
2. Add the first boss Mixin to `bossrefactortf.mixins.json` and add
   `"refmap": "bossrefactortf.refmap.json"` to that config.
3. Expose `TwilightBossParryAccess` from the boss Mixin.
4. Expose a synchronized `AttackTelegraph` when an attack needs a warning.
5. Declare the entity type through `TwilightBossParryIntegration` only if it is
   not already managed by SenDimS.
6. Open and close parry windows through the integration bridge.

SenDimS 1.0.19 already has compatibility for Naga, Minoshroom, Knight Phantom,
and Alpha Yeti. This mod deliberately registers Knight Phantom and Alpha Yeti as
external Leaders after a boss-specific compatibility review, so SenDimS owns
their parry state and five-times parried damage while this mod owns their combat
state machines. Because SenDimS does not initialize health for external Leaders,
this mod applies the same `MULTIPLY_TOTAL +5.0` modifier, producing six-times
maximum health. Never write SenDimS internal NBT directly; use `LeaderApi` and
its public events through `compat/sendims`.