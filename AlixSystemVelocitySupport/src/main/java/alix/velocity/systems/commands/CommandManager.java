package alix.velocity.systems.commands;

import alix.common.commands.file.CommandsFileManager;
import alix.common.data.LoginType;
import alix.common.data.file.UserFileManager;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.login.premium.PremiumUtils;
import alix.common.messages.Messages;
import alix.common.scheduler.AlixScheduler;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.annotation.OptimizationCandidate;
import alix.velocity.Main;
import alix.velocity.server.impl.VelocityLimboIntegration;
import alix.velocity.systems.packets.gui.impl.AccountGUI;
import alix.velocity.utils.user.UserManager;
import alix.velocity.utils.user.VerifiedUser;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import ua.nanit.limbo.connection.login.packets.SoundPackets;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public final class CommandManager {

    public static void register(ProxyServer server) {
        register_Account(server);
        register_ChangePassword(server);
        AlixSystemCommand.register(server.getCommandManager());
        register_Premium(server.getCommandManager());
        //register("alixsystem", new AlixSystemCommand());
    }

    //private static final String

    private static void register_Account(ProxyServer server) {
        var cmd = command("account", ctx -> {
            if (isConsole(ctx)) return SINGLE_SUCCESS;
            Player player = (Player) ctx.getSource();

            VerifiedUser user = UserManager.getVerified(player.getUniqueId());

            /*if(user.isEncrypted()) {

                player.sendRichMessage("<red>Error - missing persistent data");
                return SINGLE_SUCCESS;
            }*/

            var data = user.getData();
            if (data == null) {
                player.sendRichMessage("<red>Error - missing persistent data");
                return SINGLE_SUCCESS;
            }
            AccountGUI.add(user);

            return SINGLE_SUCCESS;
        }).build();

        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder("account").aliases("settings").plugin(Main.PLUGIN).build(), new BrigadierCommand(cmd));
    }

    private static void register_ChangePassword(ProxyServer server) {
        var cmd = command("changepassword", ctx -> SINGLE_SUCCESS).then(BrigadierCommand.requiredArgumentBuilder("new password", StringArgumentType.word())
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();
                    VerifiedUser user = UserManager.getVerified(player.getUniqueId());

                    var data = user.getData();
                    if (data == null) {
                        player.sendRichMessage("<red>Error - missing persistent data");
                        return SINGLE_SUCCESS;
                    }
                    String password = ctx.getInput();

                    @OptimizationCandidate
                    String reason = AlixCommonUtils.getPasswordInvalidityReason(password, LoginType.ANVIL);

                    //Channel channel = user.getChannel();
                    if (reason != null) {
                        //Version version = user.getVersion();
                        //PacketUtils.write(channel, version, SoundPackets.VILLAGER_NO);
                        user.writePacketSilently(SoundPackets.wrapperOf(Sounds.ENTITY_VILLAGER_NO));
                        player.sendRichMessage(reason);
                        return SINGLE_SUCCESS;
                    }

                    data.setLoginType(LoginType.ANVIL);
                    data.setPassword(password);

                    return SINGLE_SUCCESS;
                })
        ).build();
        /*.suggests((ctx, builder) -> {

            builder.suggest("I like men", VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>Men is watashi")));
            return builder.buildFuture();
        })*/

        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder("changepassword").aliases("changepass").plugin(Main.PLUGIN).build(), new BrigadierCommand(cmd));
    }

    private static void register(String cmd, com.velocitypowered.api.command.Command command) {
        var server = Main.PLUGIN.getServer();
        var info = CommandsFileManager.getCommand(cmd);
        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder(info.getCommand()).aliases(info.getAliasesNotNull()).plugin(Main.PLUGIN).build(), command);
    }

    private static final String
            alreadyPremiumMessage = Messages.getWithPrefix("premium-command-already-premium"),
            premiumDataMessage = Messages.getWithPrefix("premium-command-premium"),
            nonPremiumDataMessage = Messages.getWithPrefix("premium-command-non-premium"),
            unknownDataMessage = Messages.getWithPrefix("premium-command-unknown");

    public static void register_Premium(com.velocitypowered.api.command.CommandManager commandManager) {
        LiteralArgumentBuilder<CommandSource> premiumLiteral = command("premium", ctx -> {
            if (isConsole(ctx)) return 0;
            Player player = (Player) ctx.getSource();
            var user = UserManager.getVerified(player.getUniqueId());
            var channel = user.getChannel();
            var uuid = VelocityLimboIntegration.getLoginAssignedUUID(channel);
            boolean canBePremium = uuid == null || uuid.version() == 4;

            if (!canBePremium) {
                user.sendMessage(nonPremiumDataMessage);
                return 0;
            }

            String name = player.getUsername();
            var data = UserFileManager.get(name);

            if (data.getPremiumData().getStatus().isPremium()) {
                user.sendMessage(alreadyPremiumMessage);
                return 0;
            }

            // Run the premium check asynchronously
            AlixScheduler.asyncBlocking(() -> {
                PremiumData premiumData = PremiumDataCache.getOrUnknown(name);
                if (premiumData.getStatus().isUnknown()) {
                    premiumData = PremiumUtils.requestPremiumData(name);
                    if (premiumData.getStatus().isKnown()) {
                        PremiumDataCache.add(name, premiumData);
                    }
                }

                switch (premiumData.getStatus()) {
                    case PREMIUM:
                        user.sendMessage(premiumDataMessage);
                        data.setPremiumData(premiumData);
                        break;
                    case NON_PREMIUM:
                        user.sendMessage(nonPremiumDataMessage);
                        break;
                    case UNKNOWN:
                        user.sendMessage(unknownDataMessage);
                        break;
                }
            });
            return 1;
        });

        commandManager.register(new BrigadierCommand(premiumLiteral));
    }
    /*private static void register_AS() {
        register("alixsystem", new AlixSystemCommand());
       *//* var cmd = command("as", ctx -> {

            CommandSource sender = ctx.getSource();

            return SINGLE_SUCCESS;
        }).then(BrigadierCommand.requiredArgumentBuilder("testiiiinnngggggg", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            suggestions.forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                .executes(ctx -> {
                    if (isConsole(ctx)) return SINGLE_SUCCESS;
                    Player player = (Player) ctx.getSource();
                    VerifiedUser user = UserManager.get(player.getUniqueId());

                    var data = user.getData();
                    if (data == null) {
                        player.sendRichMessage("<red>Error - missing persistent data");
                        return SINGLE_SUCCESS;
                    }
                    String password = ctx.getInput();

                    @OptimizationCandidate
                    String reason = AlixCommonUtils.getPasswordInvalidityReason(password, LoginType.ANVIL);

                    Channel channel = user.getChannel();
                    if (reason != null) {
                        Version version = user.getVersion();
                        PacketUtils.write(channel, version, SoundPackets.VILLAGER_NO);
                        player.sendRichMessage(reason);
                        return SINGLE_SUCCESS;
                    }

                    data.setLoginType(LoginType.ANVIL);
                    data.setPassword(password);

                    return SINGLE_SUCCESS;
                })
        ).build();
        *//**//*.suggests((ctx, builder) -> {

            builder.suggest("I like men", VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize("<rainbow>Men is watashi")));
            return builder.buildFuture();
        })*//**//*

        var manager = server.getCommandManager();

        manager.register(manager.metaBuilder("changepassword").aliases("changepass").plugin(Main.PLUGIN).build(), new BrigadierCommand(cmd));*//*
    }*/

    private static boolean isConsole(CommandContext<CommandSource> ctx) {
        if (!(ctx.getSource() instanceof Player)) {
            ctx.getSource().sendRichMessage("<red>Console cannot execute this command!</red>");
            return true;
        }
        return false;
    }

    private static LiteralArgumentBuilder<CommandSource> command(String cmd, Command<CommandSource> executor) {
        LiteralArgumentBuilder<CommandSource> builder = literal(cmd);
        return builder.executes(executor);
    }
}