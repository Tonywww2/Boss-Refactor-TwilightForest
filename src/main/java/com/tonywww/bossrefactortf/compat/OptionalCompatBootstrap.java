package com.tonywww.bossrefactortf.compat;

import com.tonywww.bossrefactortf.BossRefactorTwilightForest;
import com.tonywww.bossrefactortf.boss.TwilightBossParryIntegration;
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

        // 特供模组只是联动：版本不兼容时记录警告并关闭联动，不影响本模组加载。
        try {
            Class<?> bootstrap = Class.forName(SENDIMS_BOOTSTRAP);
            bootstrap.getMethod("initialize").invoke(null);
        } catch (InvocationTargetException exception) {
            TwilightBossParryIntegration.disable(
                    "SenDimS compatibility failed during initialization",
                    exception.getCause());
        } catch (ReflectiveOperationException | LinkageError
                 | RuntimeException exception) {
            TwilightBossParryIntegration.disable(
                    "the installed SenDimS is not compatible", exception);
        }
    }
}