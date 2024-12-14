/*
package alix.bungee.systems.message.redirector;

import alix.bungee.systems.message.AlixMessenger;
import alix.common.data.security.password.Password;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.nio.charset.StandardCharsets;

import static alix.bungee.systems.message.BungeeProxyMessenger.ID_TAG;

public final class RedirectorInterpreter implements AlixMessenger {

    @Override
    public void interpret(ProxiedPlayer player, ByteArrayDataInput rawData) {
        RedirectorIntention.values()[rawData.readByte()].read(player, rawData);
    }

    static void onSuccessfulRegister(ProxiedPlayer player, ByteArrayDataInput data) {
        byte hashId = data.readByte();
        byte length = data.readByte();

        byte[] passBytes = new byte[length];
        data.readFully(passBytes);
        Password password = Password.fromHashed(new String(passBytes, StandardCharsets.UTF_8), hashId);
    }

    public static void writeCaptchaAndRegisterInform(ProxiedPlayer player) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();

        data.writeByte(RedirectorIntention.INFORM_BACKEND_CAPTCHA_AND_REGISTER.getIntentionId());//intention id

        //finally, send the data
        player.sendData(ID_TAG, data.toByteArray());
    }

    //tell backend that the player needs to register
    public static void writeRegisterInform(ProxiedPlayer player) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();

        data.writeByte(RedirectorIntention.INFORM_BACKEND_REGISTER.getIntentionId());//intention id

        //finally, send the data
        player.sendData(ID_TAG, data.toByteArray());
    }

    public static void writeLoginInformAndData(ProxiedPlayer player, Password password) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();

        data.writeByte(RedirectorIntention.INFORM_BACKEND_LOGIN.getIntentionId());//intention id

        //password hash id
        data.writeByte(password.getHashId());

        //hashed password's string literal bytes
        byte[] passBytes = password.getHashedPassword().getBytes(StandardCharsets.UTF_8);

        data.writeByte(passBytes.length);//the length
        data.write(passBytes);//the bytes

        //finally, send the data
        player.sendData(ID_TAG, data.toByteArray());
    }
}*/
