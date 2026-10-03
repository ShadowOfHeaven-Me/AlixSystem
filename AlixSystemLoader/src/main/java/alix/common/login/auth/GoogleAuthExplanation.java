package alix.common.login.auth;

import alix.common.messages.Messages;
import alix.common.packets.message.MessageWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public final class GoogleAuthExplanation {

    public static final Component COMBINED;
    //Click-event-free fallback for when ClickEvent serialization fails - see GoogleAuth's static initializer.
    public static final Component COMBINED_NO_CLICK_EVENTS;

    //Was Component.text(Messages.get(...)) throughout this class - see AlixUtils#sendMessage(CommandSource,
    //String) for why that breaks hex codes. clickEvent()/hoverEvent() apply fine on top of the parsed
    //Component the same way they did on the old flat text one.
    static {
        COMBINED = build(true);
        COMBINED_NO_CLICK_EVENTS = build(false);
    }

    private static Component build(boolean includeClickEvents) {
        var confirm = MessageWrapper.parseLegacy(Messages.get("google-auth-setting-confirm"));
        if (includeClickEvents) confirm = confirm.clickEvent(ClickEvent.runCommand("/confirm"));
        //the bracketed text alone gives no hint it's a command when it isn't clickable - spell it out
        else confirm = confirm.append(MessageWrapper.parseLegacy(" &7(type /confirm)"));

        var cancel = MessageWrapper.parseLegacy(Messages.get("google-auth-setting-cancel"));
        if (includeClickEvents) cancel = cancel.clickEvent(ClickEvent.runCommand("/cancel"));
        else cancel = cancel.append(MessageWrapper.parseLegacy(" &7(type /cancel)"));

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

        return combined.build();
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