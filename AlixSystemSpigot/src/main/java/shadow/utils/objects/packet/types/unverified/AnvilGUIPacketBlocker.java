package shadow.utils.objects.packet.types.unverified;

import alix.common.data.LoginType;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.gui.builders.VirtualAnvilPasswordBuilder;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class AnvilGUIPacketBlocker extends PacketBlocker {

    //private static final Set<Integer> WINDOW_IDS = new ConcurrentSet<>();
    private VirtualAnvilPasswordBuilder builder;

    AnvilGUIPacketBlocker(PacketBlocker previousBlocker) {
        super(previousBlocker);
    }

    AnvilGUIPacketBlocker(UnverifiedUser u, TemporaryUser t) {
        super(u, t);
        this.builder = (VirtualAnvilPasswordBuilder) u.getPasswordBuilder();
    }

    @Override
    void onReceive0(PacketPlayReceiveEvent event) {
        //has completed the captcha and is currently undergoing the anvil verification
        switch (event.getPacketType()) {
            case PLAYER_POSITION://most common packets
            case PLAYER_POSITION_AND_ROTATION:
            case PLAYER_ROTATION:
            case PLAYER_FLYING:
                this.virtualFallPhase.trySpoofPackets(event);
                break;
            case TELEPORT_CONFIRM:
                this.virtualFallPhase.tpConfirm(event);
                break;
            case NAME_ITEM:
                this.updateText(new WrapperPlayClientNameItem(event).getItemName());
                break;
            case CLICK_WINDOW:
                this.builder.spoofValidAccordingly();
                this.user.getVerificationGUI().select(new WrapperPlayClientClickWindow(event).getSlot());
                break;
            case PLUGIN_MESSAGE:
                WrapperPlayClientPluginMessage wrapper = new WrapperPlayClientPluginMessage(event);
                if (wrapper.getChannelName().equals("MC|ItemName")) {
                    this.updateText(getOldAnvilInput(wrapper.getData()));
                    event.setCancelled(true);
                }
                return;
            case CLOSE_WINDOW:
                event.getPostTasks().add(this.user::openVerificationGUI);
                break;
            case KEEP_ALIVE:
            case PONG:
                return;
        }
        event.setCancelled(true);
    }

    public static String getOldAnvilInput(byte[] data) {
        String input = new String(Arrays.copyOfRange(data, 1, data.length), StandardCharsets.UTF_8);//0th index is the length of the String, equal to data.length - 1

        if (input.startsWith("§"))
            return input.substring(Math.min(2, input.length()));

        //if(chars.length)

        //works (except if the input is the first regex char), but it's unfortunately necessary to do it another way
        //char[] chars = input.toCharArray();
        //char[] regex = PacketConstructor.AnvilGUI.USER_INPUT_CHAR_ARRAY;

        /*boolean equals = true;
        for (int i = 0; i < chars.length; i++) {
            if (i < regex.length) {
                equals &= chars[i] == regex[i];
                if (!equals) {
                    return i != 0 ? new String(Arrays.copyOfRange(chars, i, chars.length)) : input;
                }
            } else if (equals) return new String(Arrays.copyOfRange(chars, regex.length, chars.length));
            else return input;
        }*/
        return input;
    }

    private void updateText(String text) {
        String invalidityReason;
        if (!this.builder.input(text)) return;

        if (this.user.isRegistered()) invalidityReason = null;
        else invalidityReason = AlixUtils.getPasswordInvalidityReason(this.builder.getInput(), LoginType.ANVIL);

        this.builder.updateValidity(invalidityReason);

        if (this.user.isRegistered()) this.builder.spoofAllItems();
        else this.builder.spoofValidAccordingly();
    }

        /*if (user.isRegistered()) this.builder.spoofAllItems();
        else if ((invalidityReason = AlixUtils.getPasswordInvalidityReason(text, LoginType.ANVIL)) != null)
            this.builder.spoofItemsInvalidIndicate();
        else this.builder.spoofAllItems();*/

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