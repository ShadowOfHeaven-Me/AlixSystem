package alix.common.login.auth;

import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class GoogleAuthExplanation {

    public static final Component COMBINED;

    //Was Component.text(Messages.get(...)) throughout this class - see AlixUtils#sendMessage(CommandSource,
    //String) for why that breaks hex codes. clickEvent()/hoverEvent() apply fine on top of the parsed
    //Component the same way they did on the old flat text one.
    static {
        var confirm = MessageWrapper.parseLegacy(Messages.get("google-auth-setting-confirm"));
        confirm = confirm.clickEvent(ClickEvent.runCommand("/confirm"));

        var cancel = MessageWrapper.parseLegacy(Messages.get("google-auth-setting-cancel"));
        cancel = cancel.clickEvent(ClickEvent.runCommand("/cancel"));

        var explanation = MessageWrapper.parseLegacy(Messages.get("google-auth-setting-explanation"));
        explanation = explanation.hoverEvent(HoverEvent.showText(concat(Messages.getSplit("google-auth-setting-explanation-hover"), "\n")));

        ComponentBuilder<?, ?> combined = Component.text();

        //for (int i = 0; i < 50; i++) combined.appendNewline();
        var newLine = Component.text('\n');
        combined.append(newLine);
        combined.append(explanation).append(newLine);
        combined.append(confirm).appendNewline();
        combined.append(cancel);
        combined.append(newLine);

        COMBINED = combined.build();
    }

    private static Component concat(String[] lines, String separator) {
        var all = Component.text();
        var sep = Component.text(separator);
        for (int i = 0; i < lines.length; i++) {
            String s = lines[i];
            all.append(MessageWrapper.parseLegacy(s));
            if (i != lines.length - 1) all.append(sep);
        }
        return all.build();
    }
}