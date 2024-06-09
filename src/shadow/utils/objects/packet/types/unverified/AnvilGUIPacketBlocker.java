package shadow.utils.objects.packet.types.unverified;

import alix.common.data.LoginType;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.gui.builders.VirtualAnvilPasswordBuilder;
import shadow.utils.users.types.UnverifiedUser;

public final class AnvilGUIPacketBlocker extends GUIPacketBlocker {

    //private static final Set<Integer> WINDOW_IDS = new ConcurrentSet<>();
    private VirtualAnvilPasswordBuilder builder;

    AnvilGUIPacketBlocker(PacketBlocker previousBlocker) {
        super(previousBlocker);
    }

    AnvilGUIPacketBlocker(UnverifiedUser u) {
        super(u);
        this.builder = (VirtualAnvilPasswordBuilder) u.getPasswordBuilder();
    }

    @Override
    public void onPacketReceive(PacketPlayReceiveEvent event) {
        if (!user.hasCompletedCaptcha()) {
            this.onReceiveCaptchaVerification(event);
            return;
        }
        //has completed the captcha and is currently undergoing the pin verification
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.trySpoofPackets();
                break;
            case NAME_ITEM:
                this.updateText(new WrapperPlayClientNameItem(event).getItemName());
                break;
            case CLICK_WINDOW:
                this.builder.spoofValidAccordingly();
                this.user.getVerificationGUI().select(new WrapperPlayClientClickWindow(event).getSlot());
                break;
            case CLOSE_WINDOW:
                event.getPostTasks().add(user::openPasswordBuilderGUI);
                break;
            case KEEP_ALIVE:
                return;
        }
        event.setCancelled(true);
    }

    private void updateText(String text) {
        String invalidityReason = null;

        if (user.isRegistered()) this.builder.spoofAllItems();
        else if ((invalidityReason = AlixUtils.getPasswordInvalidityReason(text, LoginType.ANVIL)) != null)
            this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();

        this.builder.input(text, invalidityReason);
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
        this.builder = (VirtualAnvilPasswordBuilder) user.getPasswordBuilder();
    }

/*    private static int getWindowOpenID(Object packet) {
        try {
            return (int) ReflectionUtils.outWindowOpenIdMethod.invoke(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/

/*    public static Integer getWindowItemsID(Object packet) {
        try {
            return (Integer) ReflectionUtils.outWindowItemsIdMethod.invoke(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/


/*    @Override
    public void onPacketSend(PacketPlaySendEvent event) {
        //if (spoofedWindowItems(msg)) return;
        if (!user.hasCompletedCaptcha()) {
            super.onSendCaptchaVerification(event);
            return;
        }
        *//*switch (event.getPacketType()) {
            case OPEN_WINDOW:
                this.builder.updateWindowId(new WrapperPlayServerOpenWindow(event).getContainerId());
                return;
            case WINDOW_ITEMS:
                this.builder.updateWindowId(new WrapperPlayServerWindowItems(event).getWindowId());
                //event.setCancelled(true);
                return;
        }*//*

        super.onPacketSend(event);
    }*/

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