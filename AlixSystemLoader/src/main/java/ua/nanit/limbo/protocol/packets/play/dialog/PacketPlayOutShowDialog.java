package ua.nanit.limbo.protocol.packets.play.dialog;

import alix.common.data.security.password.Password;
import alix.common.utils.netty.WrapperUtils;
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
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTLimiter;
import com.github.retrooper.packetevents.protocol.nbt.serializer.DefaultNBTSerializer;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import ua.nanit.limbo.connection.ClientConnection;
import ua.nanit.limbo.protocol.packets.retrooper.OutRetrooperPacket;
import ua.nanit.limbo.protocol.registry.Version;

import java.io.ByteArrayInputStream;
import java.util.List;

public final class PacketPlayOutShowDialog extends OutRetrooperPacket<WrapperPlayServerShowDialog> {

    public PacketPlayOutShowDialog() {
        super(WrapperPlayServerShowDialog.class);
    }

    public PacketPlayOutShowDialog(WrapperPlayServerShowDialog wrapper) {
        super(wrapper);
    }

    public static PacketPlayOutShowDialog of(String title) {
        return new PacketPlayOutShowDialog(new WrapperPlayServerShowDialog(dialog(title)));
    }

    public static boolean write(ClientConnection connection) {
        if (connection.getClientVersion().less(Version.V1_21_6))
            return false;

        connection.writePacket(of("sex"));

        return true;
    }

    @SneakyThrows
    private static PacketPlayOutShowDialog create() {
        var stream = PacketPlayOutShowDialog.class.getClassLoader().getResourceAsStream("dialog/draft.json");
        NBT nbt = DefaultNBTSerializer.INSTANCE.deserializeTag(NBTLimiter.noop(),
                ByteStreams.newDataInput(new ByteArrayInputStream(ByteStreams.toByteArray(stream))));

        var wrapper = WrapperUtils.allocEmpty(WrapperPlayServerShowDialog.class);

        var dialog = MultiActionDialog.decode((NBTCompound) nbt, wrapper);
        wrapper.setDialog(dialog);

        return new PacketPlayOutShowDialog(wrapper);
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