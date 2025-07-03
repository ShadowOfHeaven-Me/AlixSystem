/*
package alix.common.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class AdventureUtil {

    private static final MiniMessage miniMessage = MiniMessage.builder()
            .postProcessor(new LegacyColorProcessor())
            .build();

    public static String miniMessage(String s) {
        return miniMessage.serialize()
    }

    private static final class LegacyColorProcessor implements UnaryOperator<Component> {

        @Override
        public Component apply(Component component) {
            return component.replaceText(builder -> builder.match(Pattern.compile(".*"))
                    .replacement((matchResult, builder1) -> Legacy.component(matchResult.group())));
        }
    }
}*/
