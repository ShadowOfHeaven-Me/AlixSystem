package shadow.utils.objects.packet.types.unverified;

import alix.common.scheduler.impl.AlixScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import shadow.utils.holders.ReflectionUtils;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.gui.builders.AnvilPasswordBuilder;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.concurrent.TimeUnit;

public final class AnvilGUIPacketBlocker extends GUIPacketBlocker {

    //private static final Set<Integer> WINDOW_IDS = new ConcurrentSet<>();
    private AnvilPasswordBuilder builder;

    protected AnvilGUIPacketBlocker(UnverifiedUser u) {
        super(u);
        this.builder = (AnvilPasswordBuilder) u.getPasswordBuilder();
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //Main.logInfo(msg.getClass().getSimpleName());
        if (user.hasCompletedCaptcha()) {
            switch (msg.getClass().getSimpleName()) {
                case "PacketPlayInItemName":
                    super.channelReadNotOverridden(ctx, msg);
                    this.updateText(msg);
                    return;
                case "PacketPlayInWindowClick":
                    super.channelRead(ctx, msg);
                    this.builder.spoofValidAccordingly();
                    return;
                case "PacketPlayInCloseWindow":
                    super.channelReadNotOverridden(ctx, msg);
                    AlixScheduler.runLaterSync(user::openPasswordBuilderGUI, 100, TimeUnit.MILLISECONDS);
                    return;
                case "PacketPlayInKeepAlive":
                    super.channelReadNotOverridden(ctx, msg);
            }
            return;
        }
        super.captchaVerification(ctx, msg);
    }

    private void updateText(Object packet) {
        try {
            String text = (String) ReflectionUtils.inItemNamePacketTextMethod.invoke(packet);

            boolean invalid = true;

            if (user.isRegistered()) this.builder.spoofAllItems();
            else if (invalid = AlixUtils.isPasswordInvalid(text)) this.builder.spoofItemsInvalidIndicate();
            else this.builder.spoofAllItems();

            this.builder.input(text, !invalid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private static int getSlot(Object packet) {
        try {
            return (int) ReflectionUtils.inWindowClickSlotField.get(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public void updateBuilder() {
        this.builder = (AnvilPasswordBuilder) user.getPasswordBuilder();
    }

/*    private static int getWindowOpenID(Object packet) {
        try {
            return (int) ReflectionUtils.outWindowOpenIdMethod.invoke(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    private static Integer getWindowItemsID(Object packet) {
        try {
            return (Integer) ReflectionUtils.outWindowItemsIdMethod.invoke(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (user.hasCompletedCaptcha() && msg.getClass() == ReflectionUtils.outWindowOpenPacketClass) {//Open Inventory
            super.writeNotOverridden(ctx, msg, promise);
            this.builder.updateWindowID();
            this.builder.spoofValidAccordingly();
            return;
        }

/*        if (msg.getClass() == ReflectionUtils.outWindowItemsPacketClass) {//Items
            Integer id = getWindowItemsID(msg);

            if (WINDOW_IDS.contains(id)) {
                super.writeNotOverridden(ctx, msg, promise);
                Main.logInfo("ID FLOWING: " + id);
                return;
            }
            Main.logInfo("ID BLOCKED: " + id);
            return;
        }*/

        //Main.logInfo(msg.getClass().getSimpleName());
        super.write(ctx, msg, promise);//super call on purpose
    }

    //WindowClick \/
    /*                    int slot = getSlot(msg);
                    Bukkit.broadcastMessage("Slot: " + slot);
                    switch (slot) {
                        case 1:
                        case 2:
                            super.channelReadNotOverridden(ctx, msg);
                    }*/
/*                    switch (slot) {
                        case 1:
                            this.syncedKick(PasswordGui.pinLeaveFeedback);
                            break;
                        case 2:
                            String password = builder.getPasswordBuilt();
                            if (user.isRegistered()) {
                                if (user.isPasswordCorrect(password)) {
                                    sendMessage(user.getPlayer(), loginSuccess);
                                    AlixScheduler.sync(user::logIn);
                                    return;
                                } else if (kickOnIncorrectPassword) syncedKick(incorrectPassword);
                                else user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                return;
                            }
                            if (AlixUtils.isPasswordInvalid(password)) {
                                //this.builder.spoofTwoFirstItems();
                                //sendMessage(user.getPlayer(), reason);
                                //user.getPlayer().playSound(user.getCurrentLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                return;
                            }
                            sendMessage(user.getPlayer(), CommandManager.passwordRegister);
                            AlixScheduler.sync(() -> user.register(password));
                            return;
                    }*/

}