package com.tonywww.bossrefactortf.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BossRefactorTwilightForestConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final AttackTelegraphConfig ATTACK_TELEGRAPH;
        public static final KnightPhantomConfig KNIGHT_PHANTOM;
        public static final AlphaYetiConfig ALPHA_YETI;
        public static final UrGhastConfig UR_GHAST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ATTACK_TELEGRAPH = new AttackTelegraphConfig(builder);
        KNIGHT_PHANTOM = new KnightPhantomConfig(builder);
        ALPHA_YETI = new AlphaYetiConfig(builder);
        UR_GHAST = new UrGhastConfig(builder);
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

    public static final class AlphaYetiConfig {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.IntValue cruiseMinimumTicks;
        public final ForgeConfigSpec.IntValue cruiseMaximumTicks;
        public final ForgeConfigSpec.IntValue restDurationTicks;
        public final ForgeConfigSpec.IntValue rampageDurationTicks;
        public final ForgeConfigSpec.DoubleValue rampageRange;
        public final ForgeConfigSpec.IntValue pursuitRoarTicks;
        public final ForgeConfigSpec.IntValue pursuitDurationTicks;
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
                    .defineInRange("cruise_minimum_ticks", 20, 1, 1200);
            cruiseMaximumTicks = builder.comment(
                            "Maximum cruise duration after a skill, in ticks.")
                    .defineInRange("cruise_maximum_ticks", 50, 1, 1200);
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
        public final ForgeConfigSpec.IntValue modeDurationTicks;
        public final ForgeConfigSpec.IntValue patrolPointsPerMode;
        public final ForgeConfigSpec.DoubleValue offensiveSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue offensiveHeightOffset;
        public final ForgeConfigSpec.DoubleValue offensiveTrapDurationMultiplier;
        public final ForgeConfigSpec.IntValue fireballWindupTicks;
        public final ForgeConfigSpec.IntValue fireballCooldownTicks;
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

            builder.comment("Patrol and offensive mode switching.").push("modes");
            modeDurationTicks = builder.comment(
                            "Maximum duration of each unlocked mode, in ticks.")
                    .defineInRange("duration_ticks", 400, 1, 12000);
            patrolPointsPerMode = builder.comment(
                            "Visited trap positions that end patrol mode early.")
                    .defineInRange("patrol_points", 4, 1, 64);
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
}