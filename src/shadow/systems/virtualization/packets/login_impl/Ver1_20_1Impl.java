package shadow.systems.virtualization.packets.login_impl;

import io.netty.buffer.ByteBuf;

import java.util.function.Supplier;

final class Ver1_20_1Impl implements Supplier<ByteBuf> {

    @Override
    public ByteBuf get() {
        //new PacketPlayOutLogin(1, worlddata.n(), entityplayer.e.b(), entityplayer.e.c(), this.j.E(), this.v, worldserver1.aa(), worldserver1.ac(), BiomeManager.a(worldserver1.A()), this.n(), worldserver1.spigotConfig.viewDistance, worldserver1.spigotConfig.simulationDistance, flag1, !flag, worldserver1.af(), worldserver1.z(), entityplayer.gm(), entityplayer.ar());

        return null;
    }
}