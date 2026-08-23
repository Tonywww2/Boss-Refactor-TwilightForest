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

The Naga cycles between Twilight Forest's original charge, a custom pounce,
and a moving venom projectile that leaves a seven-second poison cloud. Charges
and pounces can be shield-blocked or SlashBlade-parried. A shield fully absorbs
the initial venom hit and applies its cooldown, but the cloud lands 0.75 blocks
outside the shield along the incoming path; SlashBlade parries still cancel the
projectile without a cloud. Below half health, phase two multiplies attack
damage and movement speed by 1.2 and immediately starts a 300-tick fire snake.
After each fire snake ends, another starts following a configurable 600-tick
cooldown. Its flame points last 200 ticks, deal 0.3 attack damage every 20 ticks,
ignite for 100 ticks, and retain the pronounced red tint across the Naga and all
segments. Phase two also grants one stun immunity that recovers every 15
seconds. All values are configured under `naga` in
`config/bossrefactortf-common.toml`.

The Minoshroom continuously breaks eligible nearby blocks while mob griefing is
enabled and takes configured reduced damage from sources whose three-dimensional
source position is beyond the configured radius. After each cruise interval it
uses a ground stomp against nearby targets, or a two-stage dash-and-sweep
sequence against more distant targets. The dash and sweep remain shield-blockable;
the stomp bypasses shields and can only be avoided by jumping before its impact.
Sweep sequences contain at most five total rounds, with phase two guaranteeing
at least two. Phase two also strikes the stomp twice, applies the configured
damage, speed, and windup multipliers, and renders the full model red. All
timings, ranges, damage multipliers, block breaking, phase multipliers, and tint
values are configured under `minoshroom` in
`config/bossrefactortf-common.toml`.

The Snow Queen retains every original skill, including its original SUMMON,
hover, drop, and beam behavior. On a twelve-second independent cooldown outside
DROP, it additionally creates 2-4 shattered ice cores, or 3-4 in phase two;
they dash in sequence, apply seven seconds of Frosty, and retain their original
death explosion without breaking blocks. Six ice shields at different wave
heights orbit with 100% collision damage, shield blocking, and SlashBlade
parries. Linked owned summons render blue lines, gain 30% damage while the queen
takes 30% less, and their deaths trigger a delayed teleport with a 150% burst.
At 50% health or lower, the Snow Queen gains 20% damage and speed. Each DROP
phase is limited to two successful drops. Original drops lock a three-block
warning circle, emit a snowflake ring and warning sound, and deal at most one
100% current-attack-damage hit to each target across contact and impact. Phase-
two falling ice runs only during the original beam phase, once every eight
seconds, so summon, drop, and beam retain distinct attack patterns. The Snow
Queen's block-breaking routine is disabled so its skills cannot destroy the
arena floor. All Snow Queen mechanics and link visuals are configured under
`snow_queen` in `config/bossrefactortf-common.toml`.

The Lich is rebuilt as a two-phase fight. Its first phase retains the original
ranged attacks and initial shield but no longer creates the original automatic
shadow clones. Breaking the last initial shield starts phase two and grants five
new shield layers, each preventing 10% incoming damage. Phase-two melee attacks
add 20% attack damage as magic damage. In either phase, the Lich can consume one
shield layer to empower a telegraphed rectangular shield bash from 150% to 225%
attack damage.
Shield blocks and SlashBlade parries prevent that damage but not its knockback,
and leave the Lich stunned for 40 ticks. Phase two can also summon one invulnerable
ranged clone; after 15 seconds it performs one shield bash and disappears. Lich
timings, ranges, multipliers, knockback, shields, and clone lifetime are configured
under `lich` in `config/bossrefactortf-common.toml`.

The Hydra now constructs nine real multipart head containers, including every
head and five neck segments in the spawn packet. Three heads begin active; the
first transition below 50% health grows two more through the original head
lifecycle, and all custom phase-two damage is multiplied by 1.2. Ordinary
rotation keeps bite, sweep, slam, and one-head Twilight Forest flame breath.
The original bite, flame-breath, and mortar scheduling, state chains, targeting,
damage, and ignition remain owned by The Twilight Forest. Custom head sweeps and
slams wait for those original head attacks to finish. A separate evil
fireball tracks its locked player after a 40-tick windup, moves at 0.25 blocks
per tick, pulses 1.0 attack damage every 20 ticks in a three-block radius, and
explodes for 1.5 attack damage in a five-block radius without breaking blocks.
Phase-two explosions create three locked flame beams nearby. Fire Lord opens
all active heads for 200 ticks and deals `attack * (0.5 + heads * 0.2)` every
20 ticks. Hydra fire-skill damage is split into 30% physical and 70% fire damage.
Flame breath, mortar, evil fire, Fire Lord, and beams bypass shields; only the
custom sweep and slam remain shield-blockable and parryable. All values
are configured under `hydra` in `config/bossrefactortf-common.toml`.

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
.\gradlew.bat minoshroomCombatSelfTest --console=plain
.\gradlew.bat snowQueenCombatSelfTest --console=plain
.\gradlew.bat urGhastCombatSelfTest --console=plain
.\gradlew.bat nagaCombatSelfTest --console=plain
.\gradlew.bat lichCombatSelfTest --console=plain
.\gradlew.bat hydraCombatSelfTest --console=plain
.\gradlew.bat verifyHydraNineHeadContract --console=plain
```

The local SenDimS JAR is available to compile the optional compatibility layer,
but it does not by itself provide a complete runtime environment. A real
integration run must therefore use the complete SenDimS development/modpack
environment. The explicit property adds the local JAR and SlashBlade Resharped,
but additional SenDimS runtime dependencies still have to be supplied by that
environment:

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

SenDimS 1.0.21 keeps Minoshroom in its default Managed Leader set. This mod does
not modify the SenDimS JAR or its internal NBT. After SenDimS finishes processing
an entering Minoshroom, the compatibility layer checks the public `LeaderApi`
snapshot. The current Managed profile is retained with SenDimS health scaling;
once SenDimS no longer registers it, this mod activates that entity as External
and supplies the same `MULTIPLY_TOTAL +5.0` health modifier, producing six-times
maximum health.
Naga, Lich, Knight Phantom, Alpha Yeti, Hydra, and the other existing external
bosses remain strict External registrations. SenDimS owns Leader parry state and
five-times parried damage; this mod owns combat state machines and uses only
`LeaderApi` plus its public events through `compat/sendims`.