package ua.nanit.limbo.protocol.packets.play.dialog;

import alix.common.data.security.password.Password;
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData;
import com.github.retrooper.packetevents.protocol.dialog.Dialog;
import com.github.retrooper.packetevents.protocol.dialog.DialogAction;
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog;
import com.github.retrooper.packetevents.protocol.dialog.action.DialogTemplate;
import com.github.retrooper.packetevents.protocol.dialog.action.DynamicRunCommandAction;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage;
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody;
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton;
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData;
import com.github.retrooper.packetevents.protocol.dialog.input.Input;
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl;
import com.github.retrooper.packetevents.protocol.nbt.NBTLimiter;
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerShowDialog;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;

import java.io.ByteArrayInputStream;
import java.util.List;

public final class PacketConfigOutShowDialog extends OutRetrooperPacket<WrapperConfigServerShowDialog> {

    public PacketConfigOutShowDialog() {
        super(WrapperConfigServerShowDialog.class);
    }

    public PacketConfigOutShowDialog(WrapperConfigServerShowDialog wrapper) {
        super(wrapper);
    }

    public static PacketConfigOutShowDialog of(String title) {
        return new PacketConfigOutShowDialog(new WrapperConfigServerShowDialog(dialog(title)));
    }

    @SneakyThrows
    private static void a() {
        var stream = PacketConfigOutShowDialog.class.getClassLoader().getResourceAsStream("dialog/draft.json");
        DefaultNBTSerializer.INSTANCE.deserializeTag(NBTLimiter.noop(), ByteStreams.newDataInput(new ByteArrayInputStream(ByteStreams.toByteArray(stream))));
        //reader.beginArray();
    }

    private static Dialog dialog(String title) {
        return new MultiActionDialog(new CommonDialogData(
                Component.text(title), null, false, false, DialogAction.NONE,
                List.of(new PlainMessageDialogBody(new PlainMessage(Component.text("sex"), 150))),
                List.of(new Input("alix:password", new TextInputControl(200, Component.text("Enter password"), true, "xes",
                        Password.MAX_PASSWORD_LEN, null)))),
                List.of(new ActionButton(new CommonButtonData(Component.text("z pedałami"), Component.text("tu jaki inny sex"), 150),
                        new DynamicRunCommandAction(new DialogTemplate("say sex alix:password")))),
                null, 2);
    }
}