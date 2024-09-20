package shadow.systems.login.reminder;

import alix.common.messages.Messages;
import alix.common.utils.other.throwable.AlixError;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.ScheduledFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import shadow.utils.misc.packet.constructors.OutMessagePacketConstructor;
import shadow.utils.users.types.VerifiedUser;

import java.util.concurrent.TimeUnit;

public final class AuthReminder implements Runnable {

    public static final long MESSAGE_RESEND_DELAY = 3000;
    public static final ByteBuf MESSAGE;
    private final ScheduledFuture<?> future;
    private final VerifiedUser user;

    private AuthReminder(VerifiedUser user) {
        this.future = user.getChannel().eventLoop().scheduleAtFixedRate(this, 500L, MESSAGE_RESEND_DELAY, TimeUnit.MILLISECONDS);
        this.user = user;
        throw new AlixError("AuthReminder should not be used");
    }

    public void cancel() {
        this.future.cancel(false);
    }

/*    public static AuthReminder reminderFor(VerifiedUser user) {
        return new AuthReminder(user);
    }*/

    @Override
    public void run() {
        this.user.writeAndFlushConstSilently(MESSAGE);
    }

    static {
        TextComponent confirm = Component.text(Messages.get("google-auth-setting-confirm"));
        confirm = confirm.clickEvent(ClickEvent.runCommand("/confirm"));

        TextComponent cancel = Component.text(Messages.get("google-auth-setting-cancel"));
        cancel = cancel.clickEvent(ClickEvent.runCommand("/cancel"));

        TextComponent explanation = Component.text(Messages.get("google-auth-setting-explanation"));
        explanation = explanation.hoverEvent(HoverEvent.showText(concat(Messages.getSplit("google-auth-setting-explanation-hover"), "\n")));

        ComponentBuilder<?, ?> combined = Component.text();

        //for (int i = 0; i < 50; i++) combined.appendNewline();
        combined.appendNewline();
        combined.append(explanation).appendNewline();
        combined.append(confirm).appendNewline();
        combined.append(cancel);
        combined.appendNewline();

        MESSAGE = OutMessagePacketConstructor.constructConst(combined.build());
    }

    private static Component concat(String[] lines, String separator) {
        ComponentBuilder<?, ?> all = Component.text();
        TextComponent sep = Component.text(separator);
        for (int i = 0; i < lines.length; i++) {
            String s = lines[i];
            all.append(Component.text(s));
            if (i != lines.length - 1) all.append(sep);
        }
        return all.build();
    }
}