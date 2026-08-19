package com.tonywww.bossrefactortf.config;

import com.tonywww.bossrefactortf.boss.naga.NagaCombatDefaults;
import net.minecraftforge.common.ForgeConfigSpec;

public final class BossRefactorTwilightForestConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final AttackTelegraphConfig ATTACK_TELEGRAPH;
        public static final KnightPhantomConfig KNIGHT_PHANTOM;
        public static final AlphaYetiConfig ALPHA_YETI;
                public static final MinoshroomConfig MINOSHROOM;
        public static final UrGhastConfig UR_GHAST;
                public static final NagaConfig NAGA;
                public static final LichConfig LICH;
        public static final HydraConfig HYDRA;
        public static final SnowQueenConfig SNOW_QUEEN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ATTACK_TELEGRAPH = new AttackTelegraphConfig(builder);
        KNIGHT_PHANTOM = new KnightPhantomConfig(builder);
        ALPHA_YETI = new AlphaYetiConfig(builder);
        MINOSHROOM = new MinoshroomConfig(builder);
        UR_GHAST = new UrGhastConfig(builder);
        NAGA = new NagaConfig(builder);
                LICH = new LichConfig(builder);
        HYDRA = new HydraConfig(builder);
        SNOW_QUEEN = new SnowQueenConfig(builder);
        COMMON_SPEC = builder.build();
    }

    private BossRefactorTwilightForestConfig() {
    }

    public static final class AttackTelegraphConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue maxRenderDistance;
        public final ForgeConfigSpec.IntValue red;
        public final ForgeConfigSpec.IntValue green;
        public final ForgeConfigSpec.IntValue blue;
        public final ForgeConfigSpec.IntValue alpha;
        public final ForgeConfigSpec.DoubleValue lineWidth;
        public final ForgeConfigSpec.BooleanValue fillEnabled;
        public final ForgeConfigSpec.IntValue fillStartAlpha;
        public final ForgeConfigSpec.IntValue fillEndAlpha;
        public final ForgeConfigSpec.IntValue curveSegments;
        public final ForgeConfigSpec.DoubleValue heightOffset;

        private AttackTelegraphConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Shared attack-range telegraph rendering settings.")
                    .push("attack_telegraph");
            enabled = builder.comment("Render attack-range telegraphs.")
                    .define("enabled", true);
            maxRenderDistance = builder.comment("Maximum render distance.")
                    .defineInRange("max_render_distance", 96.0, 8.0, 512.0);
            red = builder.comment("Red channel.")
                    .defineInRange("red", 255, 0, 255);
            green = builder.comment("Green channel.")
                    .defineInRange("green", 35, 0, 255);
            blue = builder.comment("Blue channel.")
                    .defineInRange("blue", 35, 0, 255);
            alpha = builder.comment("Outline alpha.")
                    .defineInRange("alpha", 210, 0, 255);
            lineWidth = builder.comment(
                            "Requested line width; graphics hardware may clamp it.")
                    .defineInRange("line_width", 5.0, 1.0, 12.0);
            fillEnabled = builder.comment("Fill marked attack areas.")
                    .define("fill_enabled", true);
            fillStartAlpha = builder.comment("Fill alpha at windup start.")
                    .defineInRange("fill_start_alpha", 20, 0, 255);
            fillEndAlpha = builder.comment("Fill alpha at windup completion.")
                    .defineInRange("fill_end_alpha", 150, 0, 255);
            curveSegments = builder.comment("Segments used for circles and arcs.")
                    .defineInRange("curve_segments", 48, 8, 256);
            heightOffset = builder.comment("Vertical telegraph offset.")
                    .defineInRange("height_offset", 0.08, -4.0, 4.0);
            builder.pop();
        }
    }

    public static final class MinoshroomConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue blockDestructionIntervalTicks;
        public final ForgeConfigSpec.DoubleValue blockDestructionHorizontalRange;
        public final ForgeConfigSpec.DoubleValue blockDestructionVerticalRange;
        public final ForgeConfigSpec.DoubleValue rangedDamageReductionRadius;
        public final ForgeConfigSpec.DoubleValue rangedDamageReduction;
        public final ForgeConfigSpec.IntValue cruiseMinimumTicks;
        public final ForgeConfigSpec.IntValue cruiseMaximumTicks;
        public final ForgeConfigSpec.DoubleValue cruiseNavigationSpeed;
        public final ForgeConfigSpec.IntValue sweepInitialWindupTicks;
        public final ForgeConfigSpec.DoubleValue sweepDashMaximumDistance;
        public final ForgeConfigSpec.IntValue sweepDashDurationTicks;
        public final ForgeConfigSpec.DoubleValue sweepDashNavigationSpeed;
        public final ForgeConfigSpec.DoubleValue sweepDashHitboxInflation;
        public final ForgeConfigSpec.DoubleValue sweepDashDamageMultiplier;
        public final ForgeConfigSpec.IntValue sweepStrikeWindupTicks;
        public final ForgeConfigSpec.DoubleValue sweepStrikeRadius;
        public final ForgeConfigSpec.DoubleValue sweepStrikeArcDegrees;
        public final ForgeConfigSpec.DoubleValue sweepStrikeDamageMultiplier;
        public final ForgeConfigSpec.IntValue sweepParryStunTicks;
        public final ForgeConfigSpec.DoubleValue sweepRepeatProbability;
        public final ForgeConfigSpec.IntValue sweepMaximumRepeats;
        public final ForgeConfigSpec.IntValue secondPhaseMinimumSweepRounds;
        public final ForgeConfigSpec.IntValue stompWindupTicks;
        public final ForgeConfigSpec.DoubleValue stompRadius;
        public final ForgeConfigSpec.DoubleValue stompSelectionProbability;
        public final ForgeConfigSpec.DoubleValue stompDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue stompHorizontalKnockback;
        public final ForgeConfigSpec.DoubleValue stompMinimumVerticalVelocity;
        public final ForgeConfigSpec.IntValue secondPhaseStompCount;
        public final ForgeConfigSpec.IntValue secondPhaseStompIntervalTicks;
        public final ForgeConfigSpec.DoubleValue secondPhaseHealthFraction;
        public final ForgeConfigSpec.DoubleValue secondPhaseWindupMultiplier;
        public final ForgeConfigSpec.DoubleValue secondPhaseDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue secondPhaseSpeedMultiplier;
                public final ForgeConfigSpec.DoubleValue secondPhaseRedTint;
                public final ForgeConfigSpec.DoubleValue secondPhaseGreenTint;
                public final ForgeConfigSpec.DoubleValue secondPhaseBlueTint;

        private MinoshroomConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Minoshroom combat takeover settings.")
                    .push("minoshroom");
            enabled = builder.comment("Enable the complete Minoshroom combat takeover.")
                    .define("enabled", true);

            builder.comment("Base traits.").push("base_traits");
            blockDestructionIntervalTicks = builder.comment(
                            "Ticks between close-range block destruction scans.")
                    .defineInRange("block_destruction_interval_ticks", 5, 1, 1200);
            blockDestructionHorizontalRange = builder.comment(
                            "Horizontal inflation of the block destruction bounds.")
                    .defineInRange("block_destruction_horizontal_range", 0.5, 0.0, 8.0);
            blockDestructionVerticalRange = builder.comment(
                            "Vertical inflation of the block destruction bounds.")
                    .defineInRange("block_destruction_vertical_range", 0.5, 0.0, 8.0);
            rangedDamageReductionRadius = builder.comment(
                            "Damage sources beyond this three-dimensional distance are reduced.")
                    .defineInRange("ranged_damage_reduction_radius", 4.0, 0.0, 128.0);
            rangedDamageReduction = builder.comment(
                            "Fraction of distant source-position damage prevented.")
                    .defineInRange("ranged_damage_reduction", 0.2, 0.0, 1.0);
            builder.pop();

            builder.comment("Cruise scheduling.").push("cruise");
            cruiseMinimumTicks = builder.defineInRange(
                    "minimum_ticks", 20, 1, 1200);
            cruiseMaximumTicks = builder.defineInRange(
                    "maximum_ticks", 40, 1, 1200);
            cruiseNavigationSpeed = builder.defineInRange(
                    "navigation_speed", 1.0, 0.0, 20.0);
            builder.pop();

            builder.comment("Repeated dash and sweep rounds.").push("sweep");
            sweepInitialWindupTicks = builder.defineInRange(
                    "initial_windup_ticks", 20, 1, 1200);
            sweepDashMaximumDistance = builder.defineInRange(
                    "dash_maximum_distance", 6.0, 0.0, 128.0);
            sweepDashDurationTicks = builder.defineInRange(
                    "dash_duration_ticks", 8, 1, 1200);
            sweepDashNavigationSpeed = builder.defineInRange(
                    "dash_navigation_speed", 1.5, 0.0, 20.0);
            sweepDashHitboxInflation = builder.defineInRange(
                    "dash_hitbox_inflation", 0.5, 0.0, 16.0);
            sweepDashDamageMultiplier = builder.defineInRange(
                    "dash_damage_multiplier", 1.0, 0.0, 100.0);
            sweepStrikeWindupTicks = builder.defineInRange(
                    "strike_windup_ticks", 5, 1, 1200);
            sweepStrikeRadius = builder.defineInRange(
                    "strike_radius", 4.0, 0.1, 64.0);
            sweepStrikeArcDegrees = builder.defineInRange(
                    "strike_arc_degrees", 180.0, 1.0, 360.0);
            sweepStrikeDamageMultiplier = builder.defineInRange(
                    "strike_damage_multiplier", 1.5, 0.0, 100.0);
            sweepParryStunTicks = builder.defineInRange(
                    "parry_stun_ticks", 40, 1, 1200);
            sweepRepeatProbability = builder.defineInRange(
                    "repeat_probability", 0.5, 0.0, 1.0);
            sweepMaximumRepeats = builder.comment(
                            "Maximum full rounds after the initial round.")
                    .defineInRange("maximum_repeats", 4, 0, 100);
            secondPhaseMinimumSweepRounds = builder.comment(
                            "Minimum total sweep rounds in phase two.")
                    .defineInRange("second_phase_minimum_rounds", 2, 1, 101);
            builder.pop();

            builder.comment("Ground stomp.").push("stomp");
            stompWindupTicks = builder.defineInRange(
                    "windup_ticks", 30, 1, 1200);
            stompRadius = builder.defineInRange("radius", 5.0, 0.1, 64.0);
            stompSelectionProbability = builder.comment(
                            "Chance to select stomp when the target is within range.")
                    .defineInRange("selection_probability", 0.25, 0.0, 1.0);
            stompDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.0, 0.0, 100.0);
            stompHorizontalKnockback = builder.defineInRange(
                    "horizontal_knockback", 1.0, 0.0, 16.0);
            stompMinimumVerticalVelocity = builder.defineInRange(
                    "minimum_vertical_velocity", 0.6, 0.0, 16.0);
            secondPhaseStompCount = builder.defineInRange(
                    "second_phase_count", 2, 1, 100);
            secondPhaseStompIntervalTicks = builder.defineInRange(
                    "second_phase_interval_ticks", 10, 1, 1200);
            builder.pop();

            builder.comment("Second phase.").push("second_phase");
            secondPhaseHealthFraction = builder.comment(
                            "Phase two starts strictly below this health fraction.")
                    .defineInRange("health_fraction", 0.5, 0.0, 1.0);
            secondPhaseWindupMultiplier = builder.defineInRange(
                    "windup_multiplier", 0.5, 0.0, 20.0);
            secondPhaseDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.2, 0.0, 20.0);
            secondPhaseSpeedMultiplier = builder.defineInRange(
                    "speed_multiplier", 1.2, 0.0, 20.0);
            secondPhaseRedTint = builder.comment(
                            "Red multiplier for the phase-two full-model tint.")
                    .defineInRange("red_tint", 1.0, 0.0, 1.0);
            secondPhaseGreenTint = builder.comment(
                            "Green multiplier for the phase-two full-model tint.")
                    .defineInRange("green_tint", 0.35, 0.0, 1.0);
            secondPhaseBlueTint = builder.comment(
                            "Blue multiplier for the phase-two full-model tint.")
                    .defineInRange("blue_tint", 0.35, 0.0, 1.0);
            builder.pop();
            builder.pop();
        }
    }

    public static final class AlphaYetiConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue cruiseMinimumTicks;
        public final ForgeConfigSpec.IntValue cruiseMaximumTicks;
        public final ForgeConfigSpec.IntValue restDurationTicks;
        public final ForgeConfigSpec.IntValue rampageDurationTicks;
        public final ForgeConfigSpec.DoubleValue rampageRange;
        public final ForgeConfigSpec.IntValue pursuitRoarTicks;
        public final ForgeConfigSpec.IntValue pursuitDurationTicks;
        public final ForgeConfigSpec.DoubleValue pursuitSpeedMultiplier;
        public final ForgeConfigSpec.IntValue pursuitDamageIntervalTicks;
        public final ForgeConfigSpec.DoubleValue pursuitHitboxInflation;
        public final ForgeConfigSpec.IntValue pursuitKnockbackIntervalTicks;
        public final ForgeConfigSpec.DoubleValue pursuitKnockbackStrength;
        public final ForgeConfigSpec.IntValue stompWindupTicks;
        public final ForgeConfigSpec.DoubleValue stompRadius;
        public final ForgeConfigSpec.DoubleValue stompHorizontalKnockback;
        public final ForgeConfigSpec.DoubleValue stompVerticalVelocity;
        public final ForgeConfigSpec.IntValue fallingIceRingCount;
        public final ForgeConfigSpec.DoubleValue fallingIceMaximumRadius;
        public final ForgeConfigSpec.IntValue fallingIceRingWindupTicks;
        public final ForgeConfigSpec.IntValue fallingIcePerRing;
        public final ForgeConfigSpec.DoubleValue fallingIceSpawnHeight;
        public final ForgeConfigSpec.IntValue secondPhaseIceWindupTicks;
        public final ForgeConfigSpec.DoubleValue secondPhaseIceRadius;
        public final ForgeConfigSpec.IntValue secondPhaseIceCount;
        public final ForgeConfigSpec.BooleanValue slashBladeParryEnabled;
        public final ForgeConfigSpec.IntValue parryFeedbackTicks;

        private AlphaYetiConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Alpha Yeti combat refactor settings.")
                    .push("alpha_yeti");
            enabled = builder.comment("Enable the complete Alpha Yeti combat takeover.")
                    .define("enabled", true);
            cruiseMinimumTicks = builder.comment(
                            "Minimum cruise duration after a skill, in ticks.")
                    .defineInRange("cruise_minimum_ticks", 15, 1, 1200);
            cruiseMaximumTicks = builder.comment(
                            "Maximum cruise duration after a skill, in ticks.")
                    .defineInRange("cruise_maximum_ticks", 35, 1, 1200);
            restDurationTicks = builder.comment(
                            "Rest duration after reaching three fatigue, in ticks.")
                    .defineInRange("rest_duration_ticks", 200, 1, 2400);

            builder.comment("Ice Rampage.").push("rampage");
            rampageDurationTicks = builder.comment("Rampage duration, in ticks.")
                    .defineInRange("duration_ticks", 180, 1, 2400);
            rampageRange = builder.comment(
                            "Marked maximum horizontal danger range for rampage effects.")
                    .defineInRange("range", 30.0, 1.0, 128.0);
            builder.pop();

            builder.comment("Enraged Pursuit.").push("pursuit");
            pursuitRoarTicks = builder.comment("Stationary roar windup, in ticks.")
                    .defineInRange("roar_ticks", 20, 1, 1200);
            pursuitDurationTicks = builder.comment("Pursuit duration, in ticks.")
                    .defineInRange("duration_ticks", 120, 1, 2400);
            pursuitSpeedMultiplier = builder.comment(
                            "Navigation speed multiplier during pursuit.")
                    .defineInRange("speed_multiplier", 1.5, 0.0, 20.0);
            pursuitDamageIntervalTicks = builder.comment(
                            "Ticks between 100% attack-damage pulses.")
                    .defineInRange("damage_interval_ticks", 20, 1, 1200);
            pursuitHitboxInflation = builder.comment(
                            "Extra horizontal reach around the Alpha Yeti during pursuit.")
                    .defineInRange("hitbox_inflation", 0.5, 0.0, 16.0);
            pursuitKnockbackIntervalTicks = builder.comment(
                            "Ticks between continuous pursuit knockback pulses.")
                    .defineInRange("knockback_interval_ticks", 5, 1, 1200);
            pursuitKnockbackStrength = builder.comment(
                            "Strength of each pursuit knockback pulse.")
                    .defineInRange("knockback_strength", 0.6, 0.0, 16.0);
            builder.pop();

            builder.comment("Frost Beast Stomp.").push("stomp");
            stompWindupTicks = builder.comment("Stomp windup, in ticks.")
                    .defineInRange("windup_ticks", 30, 1, 1200);
            stompRadius = builder.comment("Radius of the 150% stomp hit.")
                    .defineInRange("radius", 10.0, 1.0, 64.0);
            stompHorizontalKnockback = builder.comment(
                            "Horizontal knockback from the stomp hit.")
                    .defineInRange("horizontal_knockback", 0.8, 0.0, 16.0);
            stompVerticalVelocity = builder.comment(
                            "Minimum upward velocity from the stomp hit.")
                    .defineInRange("vertical_velocity", 1.0, 0.0, 16.0);
            fallingIceRingCount = builder.comment(
                            "Number of sequential falling-ice rings.")
                    .defineInRange("falling_ice_ring_count", 4, 1, 32);
            fallingIceMaximumRadius = builder.comment(
                            "Radius reached by the final falling-ice ring.")
                    .defineInRange("falling_ice_maximum_radius", 12.0, 1.0, 64.0);
            fallingIceRingWindupTicks = builder.comment(
                            "Warning time for each falling-ice ring, in ticks.")
                    .defineInRange("falling_ice_ring_windup_ticks", 20, 1, 1200);
            fallingIcePerRing = builder.comment(
                            "Visible falling-ice blocks spawned around each ring.")
                    .defineInRange("falling_ice_per_ring", 12, 1, 128);
            fallingIceSpawnHeight = builder.comment(
                            "Preferred height above impact points for falling ice.")
                    .defineInRange("falling_ice_spawn_height", 10.0, 2.0, 64.0);
            builder.pop();

            builder.comment("Second phase.").push("second_phase");
            secondPhaseIceWindupTicks = builder.comment(
                            "Warning time for independent falling ice, in ticks.")
                    .defineInRange("falling_ice_windup_ticks", 20, 1, 1200);
            secondPhaseIceRadius = builder.comment(
                            "Impact radius of independent falling ice.")
                    .defineInRange("falling_ice_radius", 2.5, 0.5, 32.0);
            secondPhaseIceCount = builder.comment(
                            "Visible falling-ice blocks per independent impact.")
                    .defineInRange("falling_ice_count", 6, 1, 64);
            builder.pop();

            builder.comment("Optional SlashBlade parry integration.").push("parry");
            slashBladeParryEnabled = builder.comment(
                            "Allow SlashBlade parries to add pursuit imbalance.")
                    .define("slashblade_enabled", true);
            parryFeedbackTicks = builder.comment(
                            "Feedback duration returned to the parry integration, in ticks.")
                    .defineInRange("feedback_ticks", 10, 1, 1200);
            builder.pop();

            builder.pop();
        }
    }

    public static final class UrGhastConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue baseAttackDamage;
        public final ForgeConfigSpec.IntValue hurtSoundCooldownTicks;
        public final ForgeConfigSpec.DoubleValue tantrumDamageReduction;
        public final ForgeConfigSpec.DoubleValue minionSummonHealthIntervalPercent;
        public final ForgeConfigSpec.IntValue modeDurationTicks;
        public final ForgeConfigSpec.DoubleValue offensiveSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue offensiveHeightOffset;
        public final ForgeConfigSpec.DoubleValue offensiveTrapDurationMultiplier;
        public final ForgeConfigSpec.IntValue fireballWindupTicks;
        public final ForgeConfigSpec.IntValue fireballCooldownTicks;
        public final ForgeConfigSpec.DoubleValue fireballVisualScale;
        public final ForgeConfigSpec.DoubleValue fireballDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue fireballExplosionRadius;
        public final ForgeConfigSpec.IntValue fireballIgniteTicks;
        public final ForgeConfigSpec.IntValue mistWindupTicks;
        public final ForgeConfigSpec.IntValue mistDamageDurationTicks;
        public final ForgeConfigSpec.IntValue mistCooldownTicks;
        public final ForgeConfigSpec.IntValue mistDamageIntervalTicks;
        public final ForgeConfigSpec.DoubleValue mistDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue mistRadius;
        public final ForgeConfigSpec.DoubleValue mistHeight;
        public final ForgeConfigSpec.DoubleValue mistTeleportHeight;
        public final ForgeConfigSpec.IntValue mistParticlesPerColumn;
        public final ForgeConfigSpec.IntValue tantrumRedSmokeParticlesPerTick;
        public final ForgeConfigSpec.IntValue tantrumEnergyRingIntervalTicks;
        public final ForgeConfigSpec.IntValue tantrumEnergyRingParticles;

        private UrGhastConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Ur-Ghast combat takeover settings.")
                    .push("ur_ghast");
            enabled = builder.comment("Enable the complete Ur-Ghast combat takeover.")
                    .define("enabled", true);
            baseAttackDamage = builder.comment(
                            "Base attack damage used by custom Ur-Ghast skills.")
                    .defineInRange("base_attack_damage", 10.0, 0.0, 1000.0);
            hurtSoundCooldownTicks = builder.comment(
                            "Minimum ticks between Ur-Ghast hurt sounds.")
                    .defineInRange("hurt_sound_cooldown_ticks", 20, 0, 1200);
            tantrumDamageReduction = builder.comment(
                            "Incoming damage fraction prevented while enraged. 0.75 is 75%.")
                    .defineInRange("tantrum_damage_reduction", 0.75, 0.0, 1.0);
            minionSummonHealthIntervalPercent = builder.comment(
                            "Maximum-health percentage lost between minion summon waves.")
                    .defineInRange("minion_summon_health_interval_percent",
                            6.0, 0.1, 100.0);

            builder.comment("Patrol and offensive mode switching.").push("modes");
            modeDurationTicks = builder.comment(
                            "Duration of each mode, in ticks.")
                    .defineInRange("duration_ticks", 400, 1, 12000);
            offensiveSpeedMultiplier = builder.comment(
                            "Flight-speed multiplier in offensive mode.")
                    .defineInRange("offensive_speed_multiplier", 1.3, 0.0, 10.0);
            offensiveHeightOffset = builder.comment(
                            "Offensive chase height above the target's feet.")
                    .defineInRange("offensive_height_offset", 4.0, -32.0, 64.0);
            offensiveTrapDurationMultiplier = builder.comment(
                            "Fraction of trap control retained in offensive mode.")

                    .defineInRange("offensive_trap_duration_multiplier", 0.5, 0.0, 1.0);
            builder.pop();

            builder.comment("Giant Fireball.").push("fireball");
            fireballWindupTicks = builder.comment("Fireball windup, in ticks.")
                    .defineInRange("windup_ticks", 20, 1, 1200);
            fireballCooldownTicks = builder.comment(
                            "Delay between ordinary fireball attacks, in ticks.")
                    .defineInRange("cooldown_ticks", 60, 1, 2400);
            fireballVisualScale = builder.comment(
                            "Client-side visual scale of Ur-Ghast fireballs. Vanilla is 3.0.")
                    .defineInRange("visual_scale", 9.0, 0.1, 64.0);
            fireballDamageMultiplier = builder.comment(
                            "Center damage multiplier. 1.5 is 150% attack damage.")
                    .defineInRange("damage_multiplier", 1.5, 0.0, 100.0);
            fireballExplosionRadius = builder.comment(
                            "Half-size of the moving explosion AABB.")
                    .defineInRange("explosion_radius", 4.0, 0.1, 64.0);
            fireballIgniteTicks = builder.comment(
                            "Burn duration applied by a fireball hit, in ticks.")
                    .defineInRange("ignite_ticks", 100, 0, 12000);
            builder.pop();

            builder.comment("Arsenic-Lead Mist.").push("mist");
            mistWindupTicks = builder.comment(
                            "Harmless mist warning duration, in ticks.")
                    .defineInRange("windup_ticks", 60, 1, 2400);
            mistDamageDurationTicks = builder.comment(
                            "Mist damage duration after teleporting, in ticks.")
                    .defineInRange("damage_duration_ticks", 100, 1, 2400);
            mistCooldownTicks = builder.comment("Mist skill cooldown, in ticks.")
                    .defineInRange("cooldown_ticks", 200, 1, 12000);
            mistDamageIntervalTicks = builder.comment(
                            "Ticks between mist damage pulses.")
                    .defineInRange("damage_interval_ticks", 20, 1, 1200);
            mistDamageMultiplier = builder.comment(
                            "Damage per mist pulse. 0.2 is 20% attack damage.")
                    .defineInRange("damage_multiplier", 0.2, 0.0, 100.0);
            mistRadius = builder.comment("Horizontal radius of each mist column.")
                    .defineInRange("radius", 6.0, 0.1, 64.0);
            mistHeight = builder.comment("Height of each mist column.")
                    .defineInRange("height", 24.0, 1.0, 128.0);
            mistTeleportHeight = builder.comment(
                            "Teleport height above the destination patrol point.")
                    .defineInRange("teleport_height", 20.0, 0.0, 128.0);
            mistParticlesPerColumn = builder.comment(
                            "Crimson mist particles emitted per column update.")
                    .defineInRange("particles_per_column", 8, 1, 128);
            builder.pop();

            builder.comment("Enraged visual effects.").push("tantrum_visuals");
            tantrumRedSmokeParticlesPerTick = builder.comment(
                            "Additional red smoke particles emitted each client tick.")
                    .defineInRange("red_smoke_particles_per_tick", 4, 0, 64);
            tantrumEnergyRingIntervalTicks = builder.comment(
                            "Ticks between red energy rings.")
                    .defineInRange("energy_ring_interval_ticks", 10, 1, 1200);
            tantrumEnergyRingParticles = builder.comment(
                            "Particles in each red energy ring.")
                    .defineInRange("energy_ring_particles", 24, 4, 256);
            builder.pop();

            builder.pop();
        }
    }

    public static final class HydraConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.DoubleValue baseAttackDamage;
        public final ForgeConfigSpec.DoubleValue secondPhaseDamageMultiplier;
        public final ForgeConfigSpec.IntValue ordinaryAttackIntervalTicks;
        public final ForgeConfigSpec.IntValue recoveryTicks;
        public final ForgeConfigSpec.IntValue evilFireballWindupTicks;
        public final ForgeConfigSpec.IntValue evilFireballCooldownTicks;
        public final ForgeConfigSpec.DoubleValue evilFireballSpeed;
        public final ForgeConfigSpec.DoubleValue evilFireballVisualScale;
        public final ForgeConfigSpec.IntValue evilFireballLifetimeTicks;
        public final ForgeConfigSpec.IntValue evilFireballAuraIntervalTicks;
        public final ForgeConfigSpec.DoubleValue evilFireballAuraRadius;
        public final ForgeConfigSpec.DoubleValue evilFireballAuraDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue evilFireballExplosionRadius;
        public final ForgeConfigSpec.DoubleValue evilFireballExplosionDamageMultiplier;
        public final ForgeConfigSpec.IntValue evilFireballIgniteTicks;
        public final ForgeConfigSpec.IntValue evilFireballPhaseTwoBeamCount;
        public final ForgeConfigSpec.DoubleValue evilFireballPhaseTwoBeamScatter;
        public final ForgeConfigSpec.DoubleValue evilFireballPhaseTwoBeamRadius;
        public final ForgeConfigSpec.IntValue evilFireballPhaseTwoBeamWindupTicks;
        public final ForgeConfigSpec.IntValue shieldCooldownTicks;
        public final ForgeConfigSpec.IntValue parryFeedbackTicks;
        public final ForgeConfigSpec.DoubleValue meleeVerticalRange;
        public final ForgeConfigSpec.IntValue biteWindupTicks;
        public final ForgeConfigSpec.DoubleValue biteRange;
        public final ForgeConfigSpec.DoubleValue biteHalfAngleDegrees;
        public final ForgeConfigSpec.DoubleValue biteDamageMultiplier;
        public final ForgeConfigSpec.IntValue sweepWindupTicks;
        public final ForgeConfigSpec.DoubleValue sweepRadius;
        public final ForgeConfigSpec.DoubleValue sweepHalfAngleDegrees;
        public final ForgeConfigSpec.DoubleValue sweepDamageMultiplier;
        public final ForgeConfigSpec.IntValue slamWindupTicks;
        public final ForgeConfigSpec.DoubleValue slamRadius;
        public final ForgeConfigSpec.DoubleValue slamDamageMultiplier;
        public final ForgeConfigSpec.IntValue slamImpactParticles;
        public final ForgeConfigSpec.IntValue fireLordInitialDelayTicks;
        public final ForgeConfigSpec.IntValue fireLordCooldownTicks;
        public final ForgeConfigSpec.IntValue fireLordDurationTicks;
        public final ForgeConfigSpec.DoubleValue fireLordMaximumRadius;
        public final ForgeConfigSpec.DoubleValue fireLordGroundDistance;
        public final ForgeConfigSpec.IntValue fireLordDamageIntervalTicks;
        public final ForgeConfigSpec.DoubleValue fireLordDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue fireLordDamagePerHead;
        public final ForgeConfigSpec.IntValue fireIgniteTicks;
        public final ForgeConfigSpec.IntValue fireLordBeamIntervalTicks;
        public final ForgeConfigSpec.IntValue fireLordBeamCount;
        public final ForgeConfigSpec.DoubleValue fireLordBeamScatter;
        public final ForgeConfigSpec.DoubleValue fireLordBeamRadius;
        public final ForgeConfigSpec.IntValue fireLordBeamWindupTicks;
        public final ForgeConfigSpec.IntValue secondPhaseBeamIntervalTicks;
        public final ForgeConfigSpec.IntValue secondPhaseBeamCount;
        public final ForgeConfigSpec.DoubleValue secondPhaseBeamScatter;
        public final ForgeConfigSpec.DoubleValue secondPhaseBeamRadius;
        public final ForgeConfigSpec.IntValue secondPhaseBeamWindupTicks;
        public final ForgeConfigSpec.DoubleValue beamDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue fireVerticalRange;
        public final ForgeConfigSpec.IntValue fireLordParticlesPerHead;
        public final ForgeConfigSpec.IntValue beamParticlesPerTick;
        public final ForgeConfigSpec.IntValue beamImpactParticles;
        public final ForgeConfigSpec.IntValue beamLavaParticles;
        public final ForgeConfigSpec.DoubleValue beamVisualHeight;

        private HydraConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Hydra combat takeover settings.").push("hydra");
            enabled = builder.comment("Enable the complete Hydra combat takeover.")
                    .define("enabled", true);
            baseAttackDamage = builder.comment(
                            "Base attack damage used by all custom Hydra attacks.")
                    .defineInRange("base_attack_damage", 16.0, 0.0, 1000.0);
            secondPhaseDamageMultiplier = builder.comment(
                            "Damage multiplier for all custom attacks below half health.")
                    .defineInRange("second_phase_damage_multiplier", 1.2, 0.0, 100.0);
            ordinaryAttackIntervalTicks = builder.comment(
                            "Delay before the next primary ordinary attack.")
                    .defineInRange("ordinary_attack_interval_ticks", 40, 0, 12000);
            recoveryTicks = builder.comment("Recovery after an attack chain.")
                    .defineInRange("recovery_ticks", 20, 0, 12000);

            builder.comment("Evil fireball.").push("evil_fireball");
            evilFireballWindupTicks = builder.defineInRange(
                    "windup_ticks", 40, 1, 1200);
            evilFireballCooldownTicks = builder.defineInRange(
                    "cooldown_ticks", 200, 1, 12000);
            evilFireballSpeed = builder.defineInRange(
                    "speed", 0.25, 0.01, 8.0);
            evilFireballVisualScale = builder.defineInRange(
                    "visual_scale", 4.0, 0.1, 32.0);
            evilFireballLifetimeTicks = builder.defineInRange(
                    "lifetime_ticks", 200, 1, 12000);
            evilFireballAuraIntervalTicks = builder.defineInRange(
                    "aura_interval_ticks", 20, 20, 1200);
            evilFireballAuraRadius = builder.defineInRange(
                    "aura_radius", 3.0, 0.1, 64.0);
            evilFireballAuraDamageMultiplier = builder.defineInRange(
                    "aura_damage_multiplier", 0.6, 0.0, 100.0);
            evilFireballExplosionRadius = builder.defineInRange(
                    "explosion_radius", 5.0, 0.1, 128.0);
            evilFireballExplosionDamageMultiplier = builder.defineInRange(
                    "explosion_damage_multiplier", 1.5, 0.0, 100.0);
            evilFireballIgniteTicks = builder.defineInRange(
                    "ignite_ticks", 100, 0, 12000);
            evilFireballPhaseTwoBeamCount = builder.defineInRange(
                    "phase_two_beam_count", 3, 0, 128);
            evilFireballPhaseTwoBeamScatter = builder.defineInRange(
                    "phase_two_beam_scatter", 6.0, 0.0, 128.0);
            evilFireballPhaseTwoBeamRadius = builder.defineInRange(
                    "phase_two_beam_radius", 2.0, 0.1, 64.0);
            evilFireballPhaseTwoBeamWindupTicks = builder.defineInRange(
                    "phase_two_beam_windup_ticks", 20, 1, 1200);
            builder.pop();

            shieldCooldownTicks = builder.comment(
                            "Shield cooldown after blocking an ordinary melee attack.")
                    .defineInRange("shield_cooldown_ticks", 100, 0, 12000);
            parryFeedbackTicks = builder.comment(
                            "Feedback duration returned to SenDimS after a valid parry.")
                    .defineInRange("parry_feedback_ticks", 10, 1, 1200);
            meleeVerticalRange = builder.comment(
                            "Vertical range of ordinary melee damage checks.")
                    .defineInRange("melee_vertical_range", 4.0, 0.0, 64.0);

            builder.comment("Bite.").push("bite");
            biteWindupTicks = builder.defineInRange("windup_ticks", 20, 1, 1200);
            biteRange = builder.defineInRange("range", 6.0, 0.1, 64.0);
            biteHalfAngleDegrees = builder.comment(
                            "Half-angle of the bite arc.")
                    .defineInRange("half_angle_degrees", 35.0, 1.0, 180.0);
            biteDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.0, 0.0, 100.0);
            builder.pop();

            builder.comment("Head sweep.").push("sweep");
            sweepWindupTicks = builder.defineInRange("windup_ticks", 30, 1, 1200);
            sweepRadius = builder.defineInRange("radius", 8.0, 0.1, 64.0);
            sweepHalfAngleDegrees = builder.defineInRange(
                    "half_angle_degrees", 90.0, 1.0, 180.0);
            sweepDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.0, 0.0, 100.0);
            builder.pop();

            builder.comment("Head slam.").push("slam");
            slamWindupTicks = builder.defineInRange("windup_ticks", 40, 1, 1200);
            slamRadius = builder.defineInRange("radius", 5.0, 0.1, 64.0);
            slamDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.5, 0.0, 100.0);
            slamImpactParticles = builder.defineInRange(
                    "impact_particles", 12, 0, 256);
            builder.pop();

            builder.comment("Fire Lord.").push("fire_lord");
            fireLordInitialDelayTicks = builder.defineInRange(
                    "initial_delay_ticks", 200, 0, 12000);
            fireLordCooldownTicks = builder.defineInRange(
                    "cooldown_ticks", 600, 1, 12000);
            fireLordDurationTicks = builder.defineInRange(
                    "duration_ticks", 200, 1, 12000);
            fireLordMaximumRadius = builder.defineInRange(
                    "maximum_radius", 20.0, 0.1, 128.0);
            fireLordGroundDistance = builder.comment(
                            "Preferred distance of the locked ground center in front of the Hydra.")
                    .defineInRange("ground_distance", 10.0, 0.0, 128.0);
            fireLordDamageIntervalTicks = builder.defineInRange(
                    "damage_interval_ticks", 20, 1, 1200);
            fireLordDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 0.5, 0.0, 100.0);
            fireLordDamagePerHead = builder.comment(
                            "Additional damage multiplier for each active head.")
                    .defineInRange("damage_per_active_head", 0.2, 0.0, 100.0);
            fireIgniteTicks = builder.defineInRange(
                    "ignite_ticks", 100, 0, 12000);
            fireLordParticlesPerHead = builder.comment(
                            "Golden-red flame particles emitted by each active head per tick.")
                    .defineInRange("particles_per_head", 4, 0, 128);
            builder.pop();

            builder.comment("Fire Lord flame beams.").push("fire_lord_beams");
            fireLordBeamIntervalTicks = builder.defineInRange(
                    "interval_ticks", 20, 1, 1200);
            fireLordBeamCount = builder.defineInRange("count", 2, 0, 128);
            fireLordBeamScatter = builder.defineInRange(
                    "scatter", 14.0, 0.0, 128.0);
            fireLordBeamRadius = builder.defineInRange(
                    "radius", 2.0, 0.1, 64.0);
            fireLordBeamWindupTicks = builder.defineInRange(
                    "windup_ticks", 20, 1, 1200);
            builder.pop();

            builder.comment("Phase-two flame beams.").push("second_phase_beams");
            secondPhaseBeamIntervalTicks = builder.defineInRange(
                    "interval_ticks", 160, 1, 12000);
            secondPhaseBeamCount = builder.defineInRange("count", 3, 0, 128);
            secondPhaseBeamScatter = builder.defineInRange(
                    "scatter", 16.0, 0.0, 128.0);
            secondPhaseBeamRadius = builder.defineInRange(
                    "radius", 2.0, 0.1, 64.0);
            secondPhaseBeamWindupTicks = builder.defineInRange(
                    "windup_ticks", 20, 1, 1200);
            builder.pop();

            builder.comment("Shared flame-beam effects.").push("beams");
            beamDamageMultiplier = builder.defineInRange(
                    "damage_multiplier", 1.0, 0.0, 100.0);
            fireVerticalRange = builder.comment(
                            "Vertical range of Fire Lord and flame-beam damage checks.")
                    .defineInRange("vertical_range", 6.0, 0.0, 64.0);
            beamParticlesPerTick = builder.comment(
                            "Full-bright golden-red particles emitted per pending beam tick.")
                    .defineInRange("particles_per_tick", 8, 0, 256);
            beamImpactParticles = builder.defineInRange(
                    "impact_particles", 48, 0, 1024);
            beamLavaParticles = builder.defineInRange(
                    "lava_particles", 2, 0, 256);
            beamVisualHeight = builder.comment(
                            "Visible height of each pending flame beam.")
                    .defineInRange("visual_height", 8.0, 0.0, 128.0);
            builder.pop();
            builder.pop();
        }
    }

    public static final class LichConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue secondPhaseShieldLayers;
        public final ForgeConfigSpec.DoubleValue shieldDamageReductionPerLayer;
        public final ForgeConfigSpec.DoubleValue meleeMagicDamageMultiplier;
        public final ForgeConfigSpec.IntValue meleeCooldownTicks;
        public final ForgeConfigSpec.IntValue shieldBashWindupTicks;
        public final ForgeConfigSpec.IntValue shieldBashCooldownTicks;
        public final ForgeConfigSpec.DoubleValue shieldBashLength;
        public final ForgeConfigSpec.DoubleValue shieldBashHalfWidth;
        public final ForgeConfigSpec.DoubleValue shieldBashHeight;
        public final ForgeConfigSpec.DoubleValue shieldBashDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue shieldBashShieldMultiplier;
        public final ForgeConfigSpec.DoubleValue shieldBashHorizontalKnockback;
        public final ForgeConfigSpec.DoubleValue shieldBashVerticalVelocity;
        public final ForgeConfigSpec.IntValue shieldBashStunTicks;
        public final ForgeConfigSpec.IntValue shieldBashParryFeedbackTicks;
        public final ForgeConfigSpec.IntValue summonWindupTicks;
        public final ForgeConfigSpec.IntValue summonCooldownTicks;
        public final ForgeConfigSpec.IntValue cloneLifetimeTicks;
        public final ForgeConfigSpec.IntValue cruiseMinimumTicks;
        public final ForgeConfigSpec.IntValue cruiseMaximumTicks;
        public final ForgeConfigSpec.DoubleValue meleeSpeed;

        private LichConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Lich combat takeover settings.").push("lich");
            enabled = builder.comment("Enable the complete Lich combat takeover.")
                    .define("enabled", true);

            builder.comment("Second phase shields.").push("second_phase");
            secondPhaseShieldLayers = builder.comment(
                            "Shield layers granted when the Lich first enters phase two.")
                    .defineInRange("shield_layers", 5, 0, 100);
            shieldDamageReductionPerLayer = builder.comment(
                            "Incoming damage fraction prevented by each active shield layer. 0.1 is 10%.")
                    .defineInRange("shield_damage_reduction_per_layer", 0.1, 0.0, 1.0);
            meleeMagicDamageMultiplier = builder.comment(
                            "Attack-damage multiplier for second-phase melee magic. 0.2 is 20% damage.")
                    .defineInRange("melee_magic_damage_multiplier", 0.2, 0.0, 100.0);
            meleeCooldownTicks = builder.comment(
                            "Delay between ordinary second-phase melee attacks, in ticks.")
                    .defineInRange("melee_cooldown_ticks", 20, 0, 1200);
            builder.pop();

            builder.comment("Shield bash.").push("shield_bash");
            shieldBashWindupTicks = builder.comment("Shield bash warning duration, in ticks.")
                    .defineInRange("windup_ticks", 30, 0, 1200);
            shieldBashCooldownTicks = builder.comment("Delay between shield bashes, in ticks.")
                    .defineInRange("cooldown_ticks", 120, 1, 12000);
            shieldBashLength = builder.comment("Forward length of the shield bash hit rectangle.")
                    .defineInRange("length", 8.0, 0.1, 128.0);
            shieldBashHalfWidth = builder.comment("Half-width of the shield bash hit rectangle.")
                    .defineInRange("half_width", 3.0, 0.1, 64.0);
            shieldBashHeight = builder.comment("Vertical height of the shield bash hit rectangle.")
                    .defineInRange("height", 4.0, 0.1, 64.0);
            shieldBashDamageMultiplier = builder.comment(
                            "Attack-damage multiplier for a shield bash. 1.5 is 150% damage.")
                    .defineInRange("damage_multiplier", 1.5, 0.0, 100.0);
            shieldBashShieldMultiplier = builder.comment(
                            "Additional shield bash multiplier after shield consumption. 1.5 makes 225% total damage.")
                    .defineInRange("shield_multiplier", 1.5, 0.0, 100.0);
            shieldBashHorizontalKnockback = builder.comment(
                            "Horizontal knockback applied by a shield bash.")
                    .defineInRange("horizontal_knockback", 1.5, 0.0, 16.0);
            shieldBashVerticalVelocity = builder.comment(
                            "Minimum upward velocity applied by a shield bash.")
                    .defineInRange("vertical_velocity", 0.5, 0.0, 16.0);
            shieldBashStunTicks = builder.comment(
                            "Lich stun duration after a shield bash is blocked or parried, in ticks.")
                    .defineInRange("stun_ticks", 40, 1, 1200);
            shieldBashParryFeedbackTicks = builder.comment(
                            "Feedback duration returned after parrying a shield bash, in ticks.")
                    .defineInRange("parry_feedback_ticks", 40, 1, 1200);
            builder.pop();

            builder.comment("Summoning and melee cruise.").push("summoning");
            summonWindupTicks = builder.comment("Clone summon warning duration, in ticks.")
                    .defineInRange("windup_ticks", 40, 1, 1200);
            summonCooldownTicks = builder.comment("Delay between clone summons, in ticks.")
                    .defineInRange("cooldown_ticks", 400, 1, 12000);
            cloneLifetimeTicks = builder.comment("Maximum clone lifetime, in ticks.")
                    .defineInRange("clone_lifetime_ticks", 300, 1, 12000);
            cruiseMinimumTicks = builder.comment("Minimum melee cruise duration between skills, in ticks.")
                    .defineInRange("cruise_minimum_ticks", 40, 1, 1200);
            cruiseMaximumTicks = builder.comment("Maximum melee cruise duration between skills, in ticks.")
                    .defineInRange("cruise_maximum_ticks", 80, 1, 1200);
            meleeSpeed = builder.comment("Navigation speed during ordinary melee cruise.")
                    .defineInRange("melee_speed", 0.75, 0.0, 20.0);
            builder.pop();
            builder.pop();
        }
    }

    public static final class NagaConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue cruiseMinimumTicks;
        public final ForgeConfigSpec.IntValue cruiseMaximumTicks;
        public final ForgeConfigSpec.DoubleValue cruiseSpeed;
        public final ForgeConfigSpec.IntValue shieldCooldownTicks;
        public final ForgeConfigSpec.IntValue stunDurationTicks;
        public final ForgeConfigSpec.IntValue parryFeedbackTicks;
        public final ForgeConfigSpec.IntValue windupParticlesPerTick;
        public final ForgeConfigSpec.IntValue chargeCooldownTicks;
        public final ForgeConfigSpec.IntValue pounceWindupTicks;
        public final ForgeConfigSpec.IntValue pounceFlightTicks;
        public final ForgeConfigSpec.DoubleValue pounceImpactRadius;
        public final ForgeConfigSpec.DoubleValue pounceVerticalRange;
        public final ForgeConfigSpec.DoubleValue pounceDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue pounceHorizontalSpeed;
        public final ForgeConfigSpec.DoubleValue pounceVerticalSpeed;
        public final ForgeConfigSpec.IntValue pounceCooldownTicks;
        public final ForgeConfigSpec.DoubleValue bodySegmentSpacing;
        public final ForgeConfigSpec.DoubleValue bodyTrailSampleSpacing;
        public final ForgeConfigSpec.DoubleValue bodyTrailTeleportResetDistance;
        public final ForgeConfigSpec.IntValue bodyPoseBlendTicks;
        public final ForgeConfigSpec.IntValue venomWindupTicks;
        public final ForgeConfigSpec.DoubleValue venomLength;
        public final ForgeConfigSpec.DoubleValue venomWidth;
        public final ForgeConfigSpec.DoubleValue venomProjectileSpeed;
        public final ForgeConfigSpec.DoubleValue venomProjectileRadius;
        public final ForgeConfigSpec.DoubleValue venomInitialDamageMultiplier;
        public final ForgeConfigSpec.IntValue venomCooldownTicks;
        public final ForgeConfigSpec.IntValue venomCloudDurationTicks;
        public final ForgeConfigSpec.DoubleValue venomCloudRadius;
        public final ForgeConfigSpec.DoubleValue venomCloudVerticalRange;
        public final ForgeConfigSpec.IntValue venomDamageIntervalTicks;
        public final ForgeConfigSpec.DoubleValue venomDamageMultiplier;
        public final ForgeConfigSpec.IntValue venomPoisonDurationTicks;
        public final ForgeConfigSpec.IntValue venomPoisonAmplifier;
        public final ForgeConfigSpec.IntValue venomProjectileParticles;
        public final ForgeConfigSpec.IntValue venomProjectileMistParticles;
        public final ForgeConfigSpec.IntValue venomProjectileSplashParticles;
        public final ForgeConfigSpec.IntValue venomProjectileGlowParticles;
        public final ForgeConfigSpec.IntValue venomCloudParticles;
        public final ForgeConfigSpec.IntValue venomCloudMistParticles;
        public final ForgeConfigSpec.IntValue venomCloudBubbleParticles;
        public final ForgeConfigSpec.IntValue venomCloudSplashParticles;
        public final ForgeConfigSpec.IntValue venomCloudGlowParticles;
        public final ForgeConfigSpec.IntValue venomPrimaryRed;
        public final ForgeConfigSpec.IntValue venomPrimaryGreen;
        public final ForgeConfigSpec.IntValue venomPrimaryBlue;
        public final ForgeConfigSpec.IntValue venomSecondaryRed;
        public final ForgeConfigSpec.IntValue venomSecondaryGreen;
        public final ForgeConfigSpec.IntValue venomSecondaryBlue;
        public final ForgeConfigSpec.DoubleValue venomPrimaryParticleScale;
        public final ForgeConfigSpec.DoubleValue venomSecondaryParticleScale;
        public final ForgeConfigSpec.DoubleValue venomProjectileCoreSpread;
        public final ForgeConfigSpec.DoubleValue venomProjectileMistSpread;
        public final ForgeConfigSpec.DoubleValue venomProjectileParticleSpeed;
        public final ForgeConfigSpec.DoubleValue venomCloudCoreSpread;
        public final ForgeConfigSpec.DoubleValue venomCloudMistSpread;
        public final ForgeConfigSpec.DoubleValue venomCloudParticleHeight;
        public final ForgeConfigSpec.DoubleValue venomCloudParticleSpeed;
        public final ForgeConfigSpec.DoubleValue venomBlockedCloudOffset;
        public final ForgeConfigSpec.DoubleValue secondPhaseHealthFraction;
        public final ForgeConfigSpec.DoubleValue secondPhaseAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue secondPhaseSpeedMultiplier;
        public final ForgeConfigSpec.IntValue stunImmunityRecoveryTicks;
        public final ForgeConfigSpec.IntValue fireSnakeDurationTicks;
        public final ForgeConfigSpec.IntValue fireSnakeCooldownTicks;
        public final ForgeConfigSpec.IntValue firePointDurationTicks;
        public final ForgeConfigSpec.DoubleValue firePointSpacing;
        public final ForgeConfigSpec.DoubleValue fireDamageRadius;
        public final ForgeConfigSpec.DoubleValue firePointVerticalRange;
        public final ForgeConfigSpec.DoubleValue fireStartOrbitRadius;
        public final ForgeConfigSpec.DoubleValue fireEndOrbitRadius;
        public final ForgeConfigSpec.DoubleValue fireOrbitAngularSpeed;
        public final ForgeConfigSpec.DoubleValue fireSnakeSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue fireSnakeRedMultiplier;
        public final ForgeConfigSpec.DoubleValue fireSnakeGreenMultiplier;
        public final ForgeConfigSpec.DoubleValue fireSnakeBlueMultiplier;
        public final ForgeConfigSpec.IntValue fireDamageIntervalTicks;
        public final ForgeConfigSpec.DoubleValue fireDamageMultiplier;
        public final ForgeConfigSpec.IntValue fireIgniteTicks;
        public final ForgeConfigSpec.IntValue fireParticlesPerPoint;

        private NagaConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Naga combat takeover settings.").push("naga");
            enabled = builder.comment("Enable the complete Naga combat takeover.")
                    .define("enabled", true);
            cruiseMinimumTicks = builder.comment("Minimum cruise duration between skills.")
                    .defineInRange("cruise_minimum_ticks", 20, 1, 1200);
            cruiseMaximumTicks = builder.comment("Maximum cruise duration between skills.")
                    .defineInRange("cruise_maximum_ticks", 40, 1, 1200);
            cruiseSpeed = builder.comment("Navigation speed during ordinary cruise.")
                    .defineInRange("cruise_speed", 1.0, 0.0, 20.0);
            shieldCooldownTicks = builder.comment("Shield cooldown after blocking pounce or charge.")
                    .defineInRange("shield_cooldown_ticks", 100, 0, 12000);
            stunDurationTicks = builder.comment("Stun duration after a successful defensive reaction.")
                    .defineInRange("stun_duration_ticks", 60, 1, 12000);
            parryFeedbackTicks = builder.comment("Feedback duration returned to SenDimS.")
                    .defineInRange("parry_feedback_ticks", 10, 1, 1200);
            windupParticlesPerTick = builder.comment("Warning particles emitted per windup tick.")
                    .defineInRange("windup_particles_per_tick", 4, 0, 128);

            builder.comment("Vanilla charge.").push("charge");
            chargeCooldownTicks = builder.defineInRange(
                    "cooldown_ticks",
                    NagaCombatDefaults.CHARGE_COOLDOWN_TICKS, 1, 12000);
            builder.pop();

            builder.comment("Pounce.").push("pounce");
            pounceWindupTicks = builder.defineInRange("windup_ticks", 30, 1, 1200);
            pounceFlightTicks = builder.defineInRange("flight_ticks", 20, 1, 1200);
            pounceImpactRadius = builder.defineInRange("impact_radius", 4.0, 0.1, 64.0);
            pounceVerticalRange = builder.comment("Vertical range of the pounce impact.")
                    .defineInRange("vertical_range", 4.0, 0.0, 64.0);
            pounceDamageMultiplier = builder.defineInRange("damage_multiplier", 1.5, 0.0, 100.0);
            pounceHorizontalSpeed = builder.defineInRange("horizontal_speed", 1.2, 0.0, 20.0);
            pounceVerticalSpeed = builder.defineInRange("vertical_speed", 0.55, 0.0, 20.0);
            pounceCooldownTicks = builder.defineInRange("cooldown_ticks", 120, 1, 12000);
            builder.pop();

            builder.comment("Body trail and frozen pose.").push("body_control");
            bodySegmentSpacing = builder.defineInRange("segment_spacing", 2.0, 0.1, 16.0);
            bodyTrailSampleSpacing = builder.defineInRange("trail_sample_spacing", 0.25, 0.01, 4.0);
            bodyTrailTeleportResetDistance = builder.defineInRange("teleport_reset_distance", 32.0, 1.0, 1024.0);
            bodyPoseBlendTicks = builder.comment("Ticks used to blend combat poses back into the head trail.")
                    .defineInRange("pose_blend_ticks", 6, 0, 1200);
            builder.pop();

            builder.comment("Venom breath.").push("venom_breath");
            venomWindupTicks = builder.defineInRange("windup_ticks", 30, 1, 1200);
            venomLength = builder.defineInRange("length", 16.0, 0.1, 128.0);
            venomWidth = builder.defineInRange("width", 4.0, 0.1, 64.0);
            venomProjectileSpeed = builder.defineInRange("projectile_speed", 1.0, 0.05, 20.0);
            venomProjectileRadius = builder.comment("Collision radius; 2.0 matches the default four-block corridor width.")
                    .defineInRange("projectile_radius", 2.0, 0.1, 16.0);
            venomInitialDamageMultiplier = builder.defineInRange("initial_damage_multiplier", 1.0, 0.0, 100.0);
            venomCooldownTicks = builder.defineInRange("cooldown_ticks", 160, 1, 12000);
            venomCloudDurationTicks = builder.defineInRange("cloud_duration_ticks", 140, 1, 12000);
            venomCloudRadius = builder.defineInRange("cloud_radius", 3.0, 0.1, 64.0);
            venomCloudVerticalRange = builder.comment("Vertical range of venom cloud damage.")
                    .defineInRange("cloud_vertical_range", 3.0, 0.0, 64.0);
            venomDamageIntervalTicks = builder.defineInRange("damage_interval_ticks", 20, 1, 1200);
            venomDamageMultiplier = builder.defineInRange("damage_multiplier", 0.2, 0.0, 100.0);
            venomPoisonDurationTicks = builder.defineInRange("poison_duration_ticks", 100, 1, 12000);
            venomPoisonAmplifier = builder.comment("Zero-based effect amplifier; 1 is Poison II.")
                    .defineInRange("poison_amplifier", 1, 0, 255);
            venomBlockedCloudOffset = builder.comment(
                            "Distance that a shield-blocked venom cloud is moved back along the incoming horizontal path.")
                    .defineInRange("blocked_cloud_offset", 0.75, 0.0, 16.0);
            builder.comment("Fluorescent venom colors, expressed as RGB channels from 0 to 255.")
                    .push("particles");
            venomPrimaryRed = builder.defineInRange("primary_red", 190, 0, 255);
            venomPrimaryGreen = builder.defineInRange("primary_green", 255, 0, 255);
            venomPrimaryBlue = builder.defineInRange("primary_blue", 35, 0, 255);
            venomSecondaryRed = builder.defineInRange("secondary_red", 18, 0, 255);
            venomSecondaryGreen = builder.defineInRange("secondary_green", 105, 0, 255);
            venomSecondaryBlue = builder.defineInRange("secondary_blue", 18, 0, 255);
            venomPrimaryParticleScale = builder.defineInRange("primary_scale", 1.4, 0.01, 16.0);
            venomSecondaryParticleScale = builder.defineInRange("secondary_scale", 2.2, 0.01, 16.0);

            builder.comment("Moving venom projectile particles.").push("projectile");
            venomProjectileParticles = builder.comment("Bright color-transition core particles per tick.")
                    .defineInRange("core_particles", 12, 0, 256);
            venomProjectileMistParticles = builder.comment("Dark green outer dust particles per tick.")
                    .defineInRange("mist_particles", 8, 0, 256);
            venomProjectileSplashParticles = builder.comment("Slime splash particles per tick.")
                    .defineInRange("splash_particles", 3, 0, 256);
            venomProjectileGlowParticles = builder.comment("Fluorescent entity-effect particles per tick.")
                    .defineInRange("glow_particles", 2, 0, 256);
            venomProjectileCoreSpread = builder.defineInRange("core_spread", 0.2, 0.0, 16.0);
            venomProjectileMistSpread = builder.defineInRange("mist_spread", 0.6, 0.0, 16.0);
            venomProjectileParticleSpeed = builder.defineInRange("particle_speed", 0.025, 0.0, 4.0);
            builder.pop();

            builder.comment("Low venom cloud particles.").push("cloud");
            venomCloudParticles = builder.comment("Bright color-transition core particles per tick.")
                    .defineInRange("core_particles", 8, 0, 256);
            venomCloudMistParticles = builder.comment("Dark green outer dust particles per tick.")
                    .defineInRange("mist_particles", 12, 0, 256);
            venomCloudBubbleParticles = builder.comment("Bubble pop particles per tick.")
                    .defineInRange("bubble_particles", 3, 0, 256);
            venomCloudSplashParticles = builder.comment("Slime splash particles per tick.")
                    .defineInRange("splash_particles", 3, 0, 256);
            venomCloudGlowParticles = builder.comment("Fluorescent entity-effect particles per tick.")
                    .defineInRange("glow_particles", 4, 0, 256);
            venomCloudCoreSpread = builder.defineInRange("core_spread", 1.2, 0.0, 32.0);
            venomCloudMistSpread = builder.defineInRange("mist_spread", 2.2, 0.0, 32.0);
            venomCloudParticleHeight = builder.comment("Height above the cloud center used for all cloud particles.")
                    .defineInRange("particle_height", 0.15, -4.0, 8.0);
            venomCloudParticleSpeed = builder.defineInRange("particle_speed", 0.015, 0.0, 4.0);
            builder.pop();
            builder.pop();
            builder.pop();

            builder.comment("Second phase and fire snake.").push("second_phase");
            secondPhaseHealthFraction = builder.comment("Phase two starts strictly below this health fraction.")
                    .defineInRange("health_fraction", 0.5, 0.0, 1.0);
            secondPhaseAttackMultiplier = builder.defineInRange(
                    "attack_multiplier",
                    NagaCombatDefaults.SECOND_PHASE_ATTACK_MULTIPLIER,
                    0.0, 20.0);
            secondPhaseSpeedMultiplier = builder.defineInRange(
                    "speed_multiplier",
                    NagaCombatDefaults.SECOND_PHASE_SPEED_MULTIPLIER,
                    0.0, 20.0);
            stunImmunityRecoveryTicks = builder.defineInRange("stun_immunity_recovery_ticks", 300, 1, 12000);
            fireSnakeDurationTicks = builder.defineInRange(
                    "fire_snake_duration_ticks",
                    NagaCombatDefaults.FIRE_SNAKE_DURATION_TICKS, 1, 12000);
            fireSnakeCooldownTicks = builder.defineInRange(
                    "fire_snake_cooldown_ticks",
                    NagaCombatDefaults.FIRE_SNAKE_COOLDOWN_TICKS, 0, 12000);
            firePointDurationTicks = builder.defineInRange(
                    "fire_point_duration_ticks",
                    NagaCombatDefaults.FIRE_POINT_DURATION_TICKS, 1, 12000);
            firePointSpacing = builder.defineInRange("fire_point_spacing", 1.5, 0.1, 32.0);
            fireDamageRadius = builder.defineInRange("fire_damage_radius", 1.5, 0.1, 32.0);
            firePointVerticalRange = builder.comment("Vertical range of fire point damage.")
                    .defineInRange("fire_vertical_range", 3.0, 0.0, 64.0);
            fireStartOrbitRadius = builder.defineInRange("fire_start_orbit_radius", 12.0, 0.1, 128.0);
            fireEndOrbitRadius = builder.defineInRange("fire_end_orbit_radius", 4.0, 0.1, 128.0);
            fireOrbitAngularSpeed = builder.comment("Orbit angle advanced each tick, in radians.")
                    .defineInRange("fire_orbit_angular_speed", 0.08, 0.001, 3.141592653589793);
            fireSnakeSpeedMultiplier = builder.defineInRange("fire_snake_speed_multiplier", 1.2, 0.0, 20.0);
            fireSnakeRedMultiplier = builder.comment("Red model-color multiplier while the fire snake is active.")
                    .defineInRange("fire_snake_red_multiplier", 1.0, 0.0, 4.0);
            fireSnakeGreenMultiplier = builder.comment("Green model-color multiplier while the fire snake is active.")
                    .defineInRange("fire_snake_green_multiplier", 0.25, 0.0, 4.0);
            fireSnakeBlueMultiplier = builder.comment("Blue model-color multiplier while the fire snake is active.")
                    .defineInRange("fire_snake_blue_multiplier", 0.2, 0.0, 4.0);
            fireDamageIntervalTicks = builder.defineInRange(
                    "fire_damage_interval_ticks",
                    NagaCombatDefaults.FIRE_DAMAGE_INTERVAL_TICKS, 1, 1200);
            fireDamageMultiplier = builder.defineInRange(
                    "fire_damage_multiplier",
                    NagaCombatDefaults.FIRE_DAMAGE_MULTIPLIER, 0.0, 100.0);
            fireIgniteTicks = builder.defineInRange(
                    "fire_ignite_ticks",
                    NagaCombatDefaults.FIRE_IGNITE_TICKS, 0, 12000);
            fireParticlesPerPoint = builder.defineInRange("fire_particles_per_point", 3, 0, 128);
            builder.pop();
            builder.pop();
        }
    }

    public static final class KnightPhantomConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue fireVulnerabilityEnabled;
        public final ForgeConfigSpec.DoubleValue fireDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue fallenKnightScalingEnabled;
        public final ForgeConfigSpec.DoubleValue speedBonusPerFallenKnight;
        public final ForgeConfigSpec.DoubleValue damageBonusPerFallenKnight;
        public final ForgeConfigSpec.BooleanValue playerAvoidanceEnabled;
        public final ForgeConfigSpec.DoubleValue playerAvoidanceDistance;
        public final ForgeConfigSpec.BooleanValue chargeEnabled;
        public final ForgeConfigSpec.IntValue deathHandoffDelayTicks;
        public final ForgeConfigSpec.IntValue chargeWindupTicks;
        public final ForgeConfigSpec.IntValue chargeDurationTicks;
        public final ForgeConfigSpec.DoubleValue chargeMaximumDistance;
        public final ForgeConfigSpec.IntValue postChargePossessionTicks;
        public final ForgeConfigSpec.DoubleValue chargeSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue chargeDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue chargeHitboxInflation;
        public final ForgeConfigSpec.BooleanValue chargeHitsNonPlayers;
        public final ForgeConfigSpec.BooleanValue shieldCooldownEnabled;
        public final ForgeConfigSpec.IntValue shieldCooldownTicks;
        public final ForgeConfigSpec.BooleanValue slashBladeParryEnabled;
        public final ForgeConfigSpec.IntValue parryFeedbackTicks;
        public final ForgeConfigSpec.BooleanValue parryCancelsRemainingCharges;
        public final ForgeConfigSpec.BooleanValue lastKnightEnabled;
        public final ForgeConfigSpec.BooleanValue lastKnightFullHealEnabled;
        public final ForgeConfigSpec.BooleanValue lastKnightDamageReductionEnabled;
        public final ForgeConfigSpec.DoubleValue lastKnightDamageReduction;
        public final ForgeConfigSpec.BooleanValue lastKnightMultiChargeEnabled;
        public final ForgeConfigSpec.IntValue lastKnightChargeCount;
        public final ForgeConfigSpec.IntValue lastKnightChargeWindupTicks;

        private KnightPhantomConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Knight Phantom encounter refactor settings.")
                    .push("knight_phantom");
            enabled = builder.comment("Enable all Knight Phantom refactor mechanics.")
                    .define("enabled", true);

            builder.comment("Base traits.").push("base_traits");
            fireVulnerabilityEnabled = builder.comment(
                            "Increase fire damage received by Knight Phantoms.")
                    .define("fire_vulnerability_enabled", true);
            fireDamageMultiplier = builder.comment(
                            "Multiplier applied to incoming fire damage. 2.0 is +100% damage.")
                    .defineInRange("fire_damage_multiplier", 2.0, 0.0, 100.0);
            fallenKnightScalingEnabled = builder.comment(
                            "Scale speed and damage for each fallen member of the group.")
                    .define("fallen_knight_scaling_enabled", true);
            speedBonusPerFallenKnight = builder.comment(
                            "Movement-speed bonus per fallen Knight Phantom. 0.05 is +5%.")
                    .defineInRange("speed_bonus_per_fallen_knight", 0.05, 0.0, 10.0);
            damageBonusPerFallenKnight = builder.comment(
                            "Damage bonus per fallen Knight Phantom. 0.05 is +5%.")
                    .defineInRange("damage_bonus_per_fallen_knight", 0.05, 0.0, 10.0);
            playerAvoidanceEnabled = builder.comment(
                            "Make non-possessed Knight Phantoms avoid nearby players.")
                    .define("player_avoidance_enabled", true);
            playerAvoidanceDistance = builder.comment(
                            "Minimum horizontal player distance for non-possessed knights.")
                    .defineInRange("player_avoidance_distance", 10.0, 0.0, 64.0);
            builder.pop();

            builder.comment("Sequential Knight Charge.").push("charge");
            chargeEnabled = builder.comment("Enable sequential Knight Charges.")
                    .define("enabled", true);
            deathHandoffDelayTicks = builder.comment(
                            "Delay after a possessed knight dies before the next knight begins its windup, in ticks.")
                    .defineInRange("death_handoff_delay_ticks", 30, 0, 1200);
            chargeWindupTicks = builder.comment(
                            "Windup before each knight's first charge, in ticks.")
                    .defineInRange("windup_ticks", 30, 0, 1200);
            chargeDurationTicks = builder.comment("Maximum duration of one charge, in ticks.")
                    .defineInRange("duration_ticks", 50, 1, 1200);
            chargeMaximumDistance = builder.comment(
                            "Maximum horizontal distance of one locked straight-line charge.")
                    .defineInRange("maximum_distance", 30.0, 0.0, 128.0);
            postChargePossessionTicks = builder.comment(
                            "Possessed melee-output time after the final charge segment, in ticks.")
                    .defineInRange("post_charge_possession_ticks", 60, 0, 1200);
            chargeSpeedMultiplier = builder.comment(
                            "Movement-speed multiplier while charging.")
                    .defineInRange("speed_multiplier", 3.0, 0.0, 20.0);
            chargeDamageMultiplier = builder.comment(
                            "Attack-damage multiplier for a charge hit. 1.2 is 120% damage.")
                    .defineInRange("damage_multiplier", 1.2, 0.0, 100.0);
            chargeHitboxInflation = builder.comment(
                            "Extra reach around the charging knight and its telegraph.")
                    .defineInRange("hitbox_inflation", 0.25, 0.0, 16.0);
            chargeHitsNonPlayers = builder.comment(
                            "Allow charges to hit non-allied living entities other than players.")
                    .define("hits_non_players", true);
            shieldCooldownEnabled = builder.comment(
                            "Put a player's used shield on cooldown after it blocks a charge.")
                    .define("shield_cooldown_enabled", true);
            shieldCooldownTicks = builder.comment(
                            "Shield cooldown after blocking a charge, in ticks. 60 ticks is 3 seconds.")
                    .defineInRange("shield_cooldown_ticks", 60, 0, 1200);
            slashBladeParryEnabled = builder.comment(
                            "Allow optional SlashBlade SenDimS integration to parry charges.")
                    .define("slashblade_parry_enabled", true);
            parryFeedbackTicks = builder.comment(
                            "Parry feedback and stationary vulnerability duration, in ticks.")
                    .defineInRange("parry_stun_ticks", 40, 1, 1200);
            parryCancelsRemainingCharges = builder.comment(
                            "Cancel every remaining charge of the parried knight, then continue the round.")
                    .define("parry_cancels_remaining_charges", true);
            builder.pop();

            builder.comment("Last Knight phase.").push("last_knight");
            lastKnightEnabled = builder.comment("Enable the Last Knight phase.")
                    .define("enabled", true);
            lastKnightFullHealEnabled = builder.comment(
                            "Fully heal the final Knight Phantom when the phase begins.")
                    .define("full_heal_enabled", true);
            lastKnightDamageReductionEnabled = builder.comment(
                            "Reduce incoming damage during the Last Knight phase.")
                    .define("damage_reduction_enabled", true);
            lastKnightDamageReduction = builder.comment(
                            "Fraction of incoming damage prevented. 0.5 is 50% reduction.")
                    .defineInRange("damage_reduction", 0.5, 0.0, 1.0);
            lastKnightMultiChargeEnabled = builder.comment(
                            "Make the final Knight Phantom charge repeatedly after one windup.")
                    .define("multi_charge_enabled", true);
            lastKnightChargeCount = builder.comment(
                            "Number of consecutive charges used by the final Knight Phantom.")
                    .defineInRange("charge_count", 3, 1, 100);
            lastKnightChargeWindupTicks = builder.comment(
                            "Windup before each final-knight charge segment, in ticks.")
                    .defineInRange("windup_ticks", 15, 0, 1200);
            builder.pop();

            builder.pop();
        }
    }

    public static final class SnowQueenConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue firstPhaseIceShieldCount;
        public final ForgeConfigSpec.IntValue secondPhaseIceShieldCount;
        public final ForgeConfigSpec.DoubleValue iceShieldOrbitRadius;
        public final ForgeConfigSpec.DoubleValue iceShieldAngularSpeedDegreesPerTick;
        public final ForgeConfigSpec.DoubleValue iceShieldWaveAmplitude;
        public final ForgeConfigSpec.DoubleValue iceShieldWaveSpeedDegreesPerTick;
        public final ForgeConfigSpec.DoubleValue iceShieldVerticalOffset;
        public final ForgeConfigSpec.DoubleValue iceShieldCollisionDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue iceShieldCollisionHitboxInflation;
        public final ForgeConfigSpec.IntValue iceShieldCollisionDamageCooldownTicks;
        public final ForgeConfigSpec.BooleanValue iceShieldCollisionHitsNonPlayers;
        public final ForgeConfigSpec.BooleanValue iceShieldShieldCooldownEnabled;
        public final ForgeConfigSpec.IntValue iceShieldShieldCooldownTicks;
        public final ForgeConfigSpec.BooleanValue iceShieldSlashBladeParryEnabled;
        public final ForgeConfigSpec.IntValue iceShieldParryFeedbackTicks;
        public final ForgeConfigSpec.DoubleValue chaseHeightOffset;
        public final ForgeConfigSpec.DoubleValue chaseSpeedMultiplier;
        public final ForgeConfigSpec.IntValue shatteredIceRainQueenWindupTicks;
        public final ForgeConfigSpec.IntValue shatteredIceRainFirstPhaseMinimumCount;
        public final ForgeConfigSpec.IntValue shatteredIceRainFirstPhaseMaximumCount;
        public final ForgeConfigSpec.IntValue shatteredIceRainSecondPhaseMinimumCount;
        public final ForgeConfigSpec.IntValue shatteredIceRainSecondPhaseMaximumCount;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainSpawnDistanceBehind;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainSpawnSpacing;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainSpawnVerticalOffset;
        public final ForgeConfigSpec.IntValue shatteredIceRainCrystalWindupTicks;
        public final ForgeConfigSpec.IntValue shatteredIceRainLaunchIntervalTicks;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainDashSpeed;
        public final ForgeConfigSpec.IntValue shatteredIceRainMaximumDashTicks;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainDashHitboxInflation;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainDashDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue shatteredIceRainDashHitsNonPlayers;
        public final ForgeConfigSpec.IntValue shatteredIceRainFrostedDurationTicks;
        public final ForgeConfigSpec.IntValue shatteredIceRainFrostedAmplifier;
        public final ForgeConfigSpec.IntValue shatteredIceRainExplosionDelayTicks;
        public final ForgeConfigSpec.DoubleValue shatteredIceRainExplosionRadius;
        public final ForgeConfigSpec.IntValue maximumLinks;
        public final ForgeConfigSpec.DoubleValue linkSummonDamageBonus;
        public final ForgeConfigSpec.DoubleValue linkQueenDamageReduction;
        public final ForgeConfigSpec.IntValue linkEstablishWindupTicks;
        public final ForgeConfigSpec.IntValue linkTeleportDelayTicks;
        public final ForgeConfigSpec.DoubleValue linkBurstRadius;
        public final ForgeConfigSpec.DoubleValue linkBurstDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue linkBurstHitsNonPlayers;
        public final ForgeConfigSpec.DoubleValue phaseTwoHealthThreshold;
        public final ForgeConfigSpec.DoubleValue phaseTwoDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue phaseTwoSpeedMultiplier;
        public final ForgeConfigSpec.IntValue fallingIceIntervalTicks;
        public final ForgeConfigSpec.IntValue fallingIceWindupTicks;
        public final ForgeConfigSpec.DoubleValue fallingIceRadius;
        public final ForgeConfigSpec.IntValue fallingIceCount;
        public final ForgeConfigSpec.DoubleValue fallingIceSpawnHeight;
        public final ForgeConfigSpec.DoubleValue fallingIceDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue fallingIceHitsNonPlayers;
        public final ForgeConfigSpec.IntValue linkRed;
        public final ForgeConfigSpec.IntValue linkGreen;
        public final ForgeConfigSpec.IntValue linkBlue;
        public final ForgeConfigSpec.IntValue linkAlpha;
        public final ForgeConfigSpec.DoubleValue linkLineWidth;
        public final ForgeConfigSpec.DoubleValue linkMaximumRenderDistance;

        private SnowQueenConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Snow Queen combat sidecar settings.").push("snow_queen");
            enabled = builder.comment(
                            "Enable Snow Queen combat sidecar changes.",
                            "Changing this setting requires recreating existing Snow Queens or restarting the world/session.")
                    .define("enabled", true);

            builder.comment("Orbiting ice shields.").push("ice_shields");
            firstPhaseIceShieldCount = builder.comment("Ice shield count during the first phase.")
                    .defineInRange("first_phase_count", 6, 0, 8);
            secondPhaseIceShieldCount = builder.comment("Ice shield count during the second phase.")
                    .defineInRange("second_phase_count", 6, 0, 8);
            iceShieldOrbitRadius = builder.comment("Horizontal orbit radius of ice shields.")
                    .defineInRange("orbit_radius", 1.5, 0.0, 128.0);
            iceShieldAngularSpeedDegreesPerTick = builder.comment("Ice shield orbit angle advanced each tick, in degrees.")
                    .defineInRange("angular_speed_degrees_per_tick", 5.0, 0.0, 360.0);
            iceShieldWaveAmplitude = builder.comment("Vertical wave amplitude of orbiting ice shields.")
                    .defineInRange("wave_amplitude", 0.4, 0.0, 8.0);
            iceShieldWaveSpeedDegreesPerTick = builder.comment("Ice shield vertical wave angle advanced each tick, in degrees.")
                    .defineInRange("wave_speed_degrees_per_tick", 6.0, 0.0, 360.0);
            iceShieldVerticalOffset = builder.comment("Vertical offset of orbiting ice shields.")
                    .defineInRange("vertical_offset", 0.0, -64.0, 64.0);
            iceShieldCollisionDamageMultiplier = builder.comment(
                            "Attack-damage multiplier for ice shield collisions in all phases.")
                    .defineInRange("collision_damage_multiplier", 1.0, 0.0, 20.0);
            iceShieldCollisionHitboxInflation = builder.comment("Extra collision reach around each ice shield.")
                    .defineInRange("collision_hitbox_inflation", 0.0, 0.0, 64.0);
            iceShieldCollisionDamageCooldownTicks = builder.comment("Ticks between ice shield collision damage pulses.")
                    .defineInRange("collision_damage_cooldown_ticks", 10, 0, 1200);
            iceShieldCollisionHitsNonPlayers = builder.comment("Allow ice shield collisions to hit non-player living entities.")
                    .define("collision_hits_non_players", true);
            iceShieldShieldCooldownEnabled = builder.comment("Put a used ice shield on cooldown after blocking an attack.")
                    .define("shield_cooldown_enabled", true);
            iceShieldShieldCooldownTicks = builder.comment("Ice shield cooldown after blocking an attack, in ticks.")
                    .defineInRange("shield_cooldown_ticks", 60, 0, 1200);
            iceShieldSlashBladeParryEnabled = builder.comment("Allow optional SlashBlade parries against ice shields.")
                    .define("slashblade_parry_enabled", true);
            iceShieldParryFeedbackTicks = builder.comment("Feedback duration returned after parrying an ice shield, in ticks.")
                    .defineInRange("parry_feedback_ticks", 20, 1, 1200);
            builder.pop();

            builder.comment("Original hover movement.").push("chase");
            chaseHeightOffset = builder.comment("Preferred original hover height relative to the target.")
                    .defineInRange("height_offset", 0.0, -64.0, 64.0);
            chaseSpeedMultiplier = builder.comment("Original hover movement-speed multiplier.")
                    .defineInRange("speed_multiplier", 1.3, 0.0, 20.0);
            builder.pop();

            builder.comment("Shattered ice rain.").push("shattered_ice_rain");
            shatteredIceRainQueenWindupTicks = builder.comment("Queen windup before summoning shattered ice, in ticks.")
                    .defineInRange("queen_windup_ticks", 30, 0, 1200);
            shatteredIceRainFirstPhaseMinimumCount = builder.comment("Minimum shattered ice count during the first phase.")
                    .defineInRange("first_phase_minimum_count", 2, 0, 32);
            shatteredIceRainFirstPhaseMaximumCount = builder.comment("Maximum shattered ice count during the first phase.")
                    .defineInRange("first_phase_maximum_count", 4, 0, 32);
            shatteredIceRainSecondPhaseMinimumCount = builder.comment("Minimum shattered ice count during the second phase.")
                    .defineInRange("second_phase_minimum_count", 3, 3, 32);
            shatteredIceRainSecondPhaseMaximumCount = builder.comment("Maximum shattered ice count during the second phase.")
                    .defineInRange("second_phase_maximum_count", 4, 3, 32);
            shatteredIceRainSpawnDistanceBehind = builder.comment("Distance behind the target where shattered ice starts spawning.")
                    .defineInRange("spawn_distance_behind", 3.0, 0.0, 128.0);
            shatteredIceRainSpawnSpacing = builder.comment("Spacing between shattered ice spawn positions.")
                    .defineInRange("spawn_spacing", 1.5, 0.0, 128.0);
            shatteredIceRainSpawnVerticalOffset = builder.comment("Vertical offset of shattered ice spawn positions.")
                    .defineInRange("spawn_vertical_offset", 1.0, -64.0, 64.0);
            shatteredIceRainCrystalWindupTicks = builder.comment("Windup before each shattered ice crystal launches, in ticks.")
                    .defineInRange("crystal_windup_ticks", 20, 0, 1200);
            shatteredIceRainLaunchIntervalTicks = builder.comment("Delay between shattered ice launches, in ticks.")
                    .defineInRange("launch_interval_ticks", 10, 0, 1200);
            shatteredIceRainDashSpeed = builder.comment("Dash speed of each shattered ice crystal.")
                    .defineInRange("dash_speed", 1.5, 0.0, 20.0);
            shatteredIceRainMaximumDashTicks = builder.comment("Maximum shattered ice dash duration, in ticks.")
                    .defineInRange("maximum_dash_ticks", 30, 1, 1200);
            shatteredIceRainDashHitboxInflation = builder.comment("Extra collision reach around each dashing ice crystal.")
                    .defineInRange("dash_hitbox_inflation", 0.25, 0.0, 64.0);
            shatteredIceRainDashDamageMultiplier = builder.comment("Attack-damage multiplier for a shattered ice dash hit.")
                    .defineInRange("dash_damage_multiplier", 1.0, 0.0, 20.0);
            shatteredIceRainDashHitsNonPlayers = builder.comment(
                            "Allow shattered ice dashes to hit non-player living entities.")
                    .define("dash_hits_non_players", true);
            shatteredIceRainFrostedDurationTicks = builder.comment("Frosted effect duration after a shattered ice hit, in ticks.")
                    .defineInRange("frosted_duration_ticks", 140, 0, 12000);
            shatteredIceRainFrostedAmplifier = builder.comment("Zero-based amplifier of the frosted effect.")
                    .defineInRange("frosted_amplifier", 0, 0, 255);
            shatteredIceRainExplosionDelayTicks = builder.comment("Delay before a stopped shattered ice crystal explodes, in ticks.")
                    .defineInRange("explosion_delay_ticks", 60, 1, 12000);
            shatteredIceRainExplosionRadius = builder.comment("Explosion radius of a shattered ice crystal.")
                    .defineInRange("explosion_radius", 1.0, 0.0, 128.0);
            builder.pop();

            builder.comment("Summoned links.").push("links");
            maximumLinks = builder.comment("Maximum number of active summoned links.")
                    .defineInRange("maximum_links", 2, 0, 2);
            linkSummonDamageBonus = builder.comment("Additional damage fraction granted by each summoned link.")
                    .defineInRange("summon_damage_bonus", 0.3, 0.0, 20.0);
            linkQueenDamageReduction = builder.comment("Incoming damage fraction prevented while links are active.")
                    .defineInRange("queen_damage_reduction", 0.3, 0.0, 1.0);
            linkEstablishWindupTicks = builder.comment("Windup before a summoned link is established, in ticks.")
                    .defineInRange("establish_windup_ticks", 20, 0, 1200);
            linkTeleportDelayTicks = builder.comment("Delay before a link teleports, in ticks.")
                    .defineInRange("teleport_delay_ticks", 20, 0, 1200);
            linkBurstRadius = builder.comment("Radius of a link burst.")
                    .defineInRange("burst_radius", 5.0, 0.0, 128.0);
            linkBurstDamageMultiplier = builder.comment("Attack-damage multiplier for a link burst.")
                    .defineInRange("burst_damage_multiplier", 1.5, 0.0, 20.0);
            linkBurstHitsNonPlayers = builder.comment("Allow link bursts to hit non-player living entities.")
                    .define("burst_hits_non_players", true);
            builder.pop();

            builder.comment("Second phase.").push("phase_two");
            phaseTwoHealthThreshold = builder.comment("Phase two starts at or below this health fraction.")
                    .defineInRange("health_threshold", 0.5, 0.0, 1.0);
            phaseTwoDamageMultiplier = builder.comment("Attack-damage multiplier during phase two.")
                    .defineInRange("damage_multiplier", 1.2, 0.0, 20.0);
            phaseTwoSpeedMultiplier = builder.comment("Movement-speed multiplier during phase two.")
                    .defineInRange("speed_multiplier", 1.2, 0.0, 20.0);
            fallingIceIntervalTicks = builder.comment("Ticks between phase-two falling ice attacks.")
                    .defineInRange("falling_ice_interval_ticks", 160, 1, 12000);
            fallingIceWindupTicks = builder.comment("Phase-two falling ice warning duration, in ticks.")
                    .defineInRange("falling_ice_windup_ticks", 20, 1, 1200);
            fallingIceRadius = builder.comment("Impact radius of phase-two falling ice.")
                    .defineInRange("falling_ice_radius", 2.5, 0.5, 32.0);
            fallingIceCount = builder.comment("Visible falling-ice blocks per phase-two impact.")
                    .defineInRange("falling_ice_count", 6, 1, 64);
            fallingIceSpawnHeight = builder.comment("Preferred height above phase-two impact points for falling ice.")
                    .defineInRange("falling_ice_spawn_height", 10.0, 2.0, 64.0);
            fallingIceDamageMultiplier = builder.comment("Attack-damage multiplier for phase-two falling ice.")
                    .defineInRange("falling_ice_damage_multiplier", 1.0, 0.0, 20.0);
            fallingIceHitsNonPlayers = builder.comment("Allow phase-two falling ice to hit non-player living entities.")
                    .define("falling_ice_hits_non_players", true);
            builder.pop();

            builder.comment("Link visual effects.").push("visuals");
            linkRed = builder.comment("Red channel of rendered links.")
                    .defineInRange("link_red", 80, 0, 255);
            linkGreen = builder.comment("Green channel of rendered links.")
                    .defineInRange("link_green", 180, 0, 255);
            linkBlue = builder.comment("Blue channel of rendered links.")
                    .defineInRange("link_blue", 255, 0, 255);
            linkAlpha = builder.comment("Alpha channel of rendered links.")
                    .defineInRange("link_alpha", 220, 0, 255);
            linkLineWidth = builder.comment("Requested line width for rendered links.")
                    .defineInRange("link_line_width", 3.0, 0.0, 20.0);
            linkMaximumRenderDistance = builder.comment("Maximum render distance for links.")
                    .defineInRange("link_maximum_render_distance", 96.0, 0.0, 128.0);
            builder.pop();

            builder.pop();
        }
    }
}