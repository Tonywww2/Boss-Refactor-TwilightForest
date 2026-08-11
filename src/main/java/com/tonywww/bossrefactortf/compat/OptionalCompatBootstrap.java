package com.tonywww.bossrefactortf.compat;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.InvocationTargetException;

public final class OptionalCompatBootstrap {
    private static final String SENDIMS_MOD_ID = "slashblade_sendims";
    private static final String SENDIMS_BOOTSTRAP =
            "com.tonywww.bossrefactortf.compat.sendims.TwilightForestSenDimsCompat";

    private OptionalCompatBootstrap() {
    }

    public static void initialize() {
        if (!ModList.get().isLoaded(SENDIMS_MOD_ID)) {
            BossRefactorTwilightForest.LOGGER.info(
                    "SlashBlade SenDimS is not installed; boss parry integration is disabled");
            return;
        }

        try {
            Class<?> bootstrap = Class.forName(SENDIMS_BOOTSTRAP);
            bootstrap.getMethod("initialize").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException
                 | IllegalAccessException exception) {
            BossRefactorTwilightForest.LOGGER.error(
                    "Unable to load SenDimS boss compatibility", exception);
            throw new IllegalStateException(
                "SenDimS is installed but boss compatibility could not start",
                exception);
        } catch (InvocationTargetException exception) {
            BossRefactorTwilightForest.LOGGER.error(
                    "SenDimS boss compatibility failed during initialization",
                    exception.getCause());
            throw new IllegalStateException(
                "SenDimS boss compatibility failed during initialization",
                exception.getCause());
        } catch (LinkageError error) {
            BossRefactorTwilightForest.LOGGER.error(
                    "SenDimS boss compatibility has an incompatible API", error);
            throw new IllegalStateException(
                "SenDimS boss compatibility has an incompatible API", error);
        }
    }
}