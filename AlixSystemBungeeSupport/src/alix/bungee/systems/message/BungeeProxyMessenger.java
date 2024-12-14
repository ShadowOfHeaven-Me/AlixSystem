/*
package alix.bungee.systems.message;


import alix.bungee.utils.users.data.UserFileManager;
import alix.common.data.security.password.Password;
import alix.common.utils.other.throwable.AlixException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeProxyMessenger {

    public static final String ID_TAG = "alix:ver";
    //private static final AlixMessenger messenger = new RedirectorInterpreter();

    public static void interpret(ProxiedPlayer player, byte[] byteData) {
        ByteArrayDataInput input = ByteStreams.newDataInput(byteData);
        Intention intention = Intention.values()[input.readByte()];

        switch (intention) {
            case INFORM_PROXY_SUCCESSFUL_REGISTER:
                Password password = Password.read(input);
                UserFileManager.get(player).setPassword(password);
                break;
            case INFORM_PROXY_SUCCESSFUL_LOGIN:
                //for now nothing
                break;
        }
        throw new AlixException("Intention from Spigot Backend " + intention.name() + " is not readable!");
    }


    //intention id - byte

    //byte 0/1 -> is double verification enabled
    //main login type
    //main password

    //if double ver is enabled:
    //secondary login type
    //secondary password

    public static void writeDataToBackend(ProxiedPlayer player, PersistentUserData data) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeByte(Intention.INFORM_BACKEND_DATA.getIntentionId());

        Password.write(data.getPassword(), output);

        player.sendData(ID_TAG, output.toByteArray());
    }
}*/
