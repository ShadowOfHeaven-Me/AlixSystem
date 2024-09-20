package shadow.utils.objects.savable.data.gui;

import alix.common.utils.other.throwable.AlixError;
import org.jetbrains.annotations.NotNull;

public interface AlixBedrockVerificationGui extends AlixVerificationGui {

    @Override
    default void destroy() {
    }

    @Override
    @NotNull
    default String getPasswordBuilt(){
        throw new AlixError("getPasswordBuilt() invoked on AlixBedrockVerificationGui!");
    }

    @Override
    default void select(int slot) {
        throw new AlixError("select(int) invoked on AlixBedrockVerificationGui!");
    }
}